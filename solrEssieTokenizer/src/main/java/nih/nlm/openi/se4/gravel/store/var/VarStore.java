//--------------------------------------------------------------------------------------------------------
// VarStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.var;

import gravel.sort.*;
import gravel.store.hash.*;
import gravel.store.plain.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarStore
//--------------------------------------------------------------------------------------------------------

public abstract class VarStore extends NestedStore {
  
//--------------------------------------------------------------------------------------------------------
// VarStore consts
//--------------------------------------------------------------------------------------------------------

  public static final int          kIntSliceSize=SliceStore.kIntSliceSize;
  public static final int          kLongSliceSize=SliceStore.kLongSliceSize;

//--------------------------------------------------------------------------------------------------------
// VarStore member vars
//--------------------------------------------------------------------------------------------------------

  private int   mVarSize;
  
//--------------------------------------------------------------------------------------------------------
// VarStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore(ByteStore inByteStore) { 
    super(inByteStore);
    mVarSize=(int) inByteStore.getParam(0);
    if ((mVarSize<1)||(mVarSize>8))
      throw new StoreException("Invalid VarSize, "+mVarSize);
    if (kRangeChecking) 
      sanityCheck();
  }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  protected ByteStore getByteStore() { return (ByteStore) getInnerStore(); }
  
//--------------------------------------------------------------------------------------------------------
// dangerousGetByteStore
//--------------------------------------------------------------------------------------------------------

  // unprotected access to inner store - easy to screw up
  public ByteStore dangerousGetByteStore() { return (ByteStore) getInnerStore(); }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public ByteStore unwrap() { return (ByteStore) super.unwrap(); }

//--------------------------------------------------------------------------------------------------------
// getVarSize
//--------------------------------------------------------------------------------------------------------

  public int getVarSize() { return mVarSize; }

//--------------------------------------------------------------------------------------------------------
// getSize
//--------------------------------------------------------------------------------------------------------

  public long getSize() { return super.getSize()/mVarSize; }

//--------------------------------------------------------------------------------------------------------
// getByteSize
//--------------------------------------------------------------------------------------------------------

  public long getByteSize() { return super.getSize(); }

//--------------------------------------------------------------------------------------------------------
// setSize
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inSize) { super.setSize(mVarSize*inSize); }

//--------------------------------------------------------------------------------------------------------
// getCapacity
//--------------------------------------------------------------------------------------------------------

  public long getCapacity() { return super.getCapacity()/mVarSize; }

//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) { super.setCapacity(mVarSize*inCapacity); }

//--------------------------------------------------------------------------------------------------------
// ensureCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureCapacity(long inSize) { super.ensureCapacity(mVarSize*inSize); }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() { return super.getMemory()+kIntMemory; }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public void clear() { 
    super.clear(); 
    mVarSize=1;
    setParam(0,mVarSize); 
  }

//--------------------------------------------------------------------------------------------------------
// setVarSize
//--------------------------------------------------------------------------------------------------------

  public void setVarSize(int inNewVarSize) {
    if ((inNewVarSize<1)||(inNewVarSize>8))
      throw new StoreException("Cannot set VarSize to "+inNewVarSize);

    int theOldVarSize=mVarSize;
    if (inNewVarSize==theOldVarSize)
      return;
    
    ByteStore theByteStore=getByteStore();
    long theSize=getSize();
    if (theSize>0) {
  
      //----------------------------------------------------------------
      // BOZO ALERT !!!
      // If you set the long size too small, you will corrupt your data
      //----------------------------------------------------------------
      
      int theDelta=inNewVarSize-theOldVarSize;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theOldBytes=theSliceStore.getByteSlice();
      byte[] theNewBytes=theSliceStore.getByteSlice();
  
      // Long size shrinking
      // For each long, drop highest order bytes
      // Advance by chunks copying longer longs to shorter longs in-place
      if (inNewVarSize<theOldVarSize) {
        int theLongChunkSize=(kByteSliceSize/theOldVarSize);
        int theOldByteChunkSize=theLongChunkSize*theOldVarSize;
        int theNewByteChunkSize=theLongChunkSize*inNewVarSize;
        long theNFullChunks=theSize/theLongChunkSize;
        for (long i=0; i<theNFullChunks; i++) {
          theByteStore.getBytes(i*theOldByteChunkSize,theOldBytes,0,theOldByteChunkSize);
          int n=0; 
          int m=0;
          for (int j=0; j<theLongChunkSize; j++) {
            n-=theDelta;
            for (int k=0; k<inNewVarSize; k++)
              theNewBytes[m++]=theOldBytes[n++];
          }
          theByteStore.setBytes(i*theNewByteChunkSize,theNewBytes,0,theNewByteChunkSize);
        }
        int theRemainder=(int) (theSize-theNFullChunks*theLongChunkSize);
        if (theRemainder>0) {
          theByteStore.getBytes(theNFullChunks*theOldByteChunkSize,theOldBytes,0,theRemainder*theOldVarSize);
          int n=0; 
          int m=0;
          for (int j=0; j<theRemainder; j++) {
            n-=theDelta;
            for (int k=0; k<inNewVarSize; k++)
              theNewBytes[m++]=theOldBytes[n++];
          }
          theByteStore.setBytes(theNFullChunks*theNewByteChunkSize,theNewBytes,0,theRemainder*inNewVarSize);
        }
        theByteStore.setSize(theSize*inNewVarSize);
  
      // Long size growing
      // For each long, add highest order bytes - 0 if positive or -1 if negative
      // Work backwards by chunks copying shorter longs to longer longs in-place
      } else {      
        theByteStore.setSize(theSize*inNewVarSize);
        int theLongChunkSize=(kByteSliceSize/inNewVarSize);
        int theOldByteChunkSize=theLongChunkSize*theOldVarSize;
        int theNewByteChunkSize=theLongChunkSize*inNewVarSize;
        long theNFullChunks=theSize/theLongChunkSize;
  
        long theOldByteSize=theSize*theOldVarSize;
        long theNewByteSize=theSize*inNewVarSize;
        for (long i=0; i<theNFullChunks; i++) {
          theByteStore.getBytes(theOldByteSize-(i+1)*theOldByteChunkSize,theOldBytes,0,theOldByteChunkSize);
          int n=theOldByteChunkSize; 
          int m=theNewByteChunkSize;
          for (int j=0; j<theLongChunkSize; j++) {
            for (int k=0; k<theOldVarSize; k++)
              theNewBytes[--m]=theOldBytes[--n];
            byte thePad=0;
            if (theOldBytes[n]<0)
              thePad=-1;
            for (int k=0; k<theDelta; k++)
              theNewBytes[--m]=thePad;
          }
          theByteStore.setBytes(theNewByteSize-(i+1)*theNewByteChunkSize,theNewBytes,0,theNewByteChunkSize);
        }
        int theRemainder=(int) (theSize-theNFullChunks*theLongChunkSize);
        if (theRemainder>0) {
          theByteStore.getBytes(0,theOldBytes,0,theRemainder*theOldVarSize);
          int n=theRemainder*theOldVarSize; 
          int m=theRemainder*inNewVarSize;
          for (int j=0; j<theRemainder; j++) {
            for (int k=0; k<theOldVarSize; k++)
              theNewBytes[--m]=theOldBytes[--n];
            byte thePad=0;
            if (theOldBytes[n]<0)
              thePad=-1;
            for (int k=0; k<theDelta; k++)
              theNewBytes[--m]=thePad;
          }
          theByteStore.setBytes(0,theNewBytes,0,theRemainder*inNewVarSize);
        }      
      }   
      theSliceStore.putByteSlice(theOldBytes);
      theSliceStore.putByteSlice(theNewBytes);
    }
    
    mVarSize=inNewVarSize;
    setParam(0,mVarSize); 
  }

//--------------------------------------------------------------------------------------------------------
// ensureVarSize
//--------------------------------------------------------------------------------------------------------

  public boolean ensureVarSize(long inLong) {
    int theNewVarSize=Conversions.calcVarLongSize(inLong);
    if (theNewVarSize>mVarSize) {
      setVarSize(theNewVarSize);
      return true;
    } else
      return false;
  }

//--------------------------------------------------------------------------------------------------------
// ensureVarSize
//--------------------------------------------------------------------------------------------------------

  public boolean ensureVarSize(long[] inLongs, int inLongDelta, int inNLongs) {
    int theNewVarSize=Conversions.calcVarLongSize(inLongs,inLongDelta,inNLongs);
    if (theNewVarSize>mVarSize) {
      setVarSize(theNewVarSize);
      return true;
    } else
      return false;
  }
  
  public boolean ensureVarSize(int[] inInts, int inIntDelta, int inNInts) {
    int theNewVarSize=Conversions.calcVarIntSize(inInts,inIntDelta,inNInts);
    if (theNewVarSize>mVarSize) {
      setVarSize(theNewVarSize);
      return true;
    } else
      return false;
  }

//--------------------------------------------------------------------------------------------------------
// ensureVarSize
//--------------------------------------------------------------------------------------------------------

  public boolean ensureVarSize(long[] inLongs) { return ensureVarSize(inLongs,0,inLongs.length); }
  public boolean ensureVarSize(int[] inInts) { return ensureVarSize(inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// getVar
//--------------------------------------------------------------------------------------------------------

  public long getLong(long inOffset) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }
    return getByteStore().getVarLong(inOffset*mVarSize,mVarSize);
  }

  public int getInt(long inOffset) { return (int) getLong(inOffset); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public void getVars(long inOffset, long[] ioLongs, int inLongDelta, int inNLongs) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNLongs<0)
        throw new StoreException("Negative NLongs: "+inNLongs);
      if (inLongDelta<0)
        throw new StoreException("Negative LongDelta: "+inLongDelta);
      if (inOffset+inNLongs>getSize())
        throw new StoreException("Offset+NLongs past end: "+inOffset+"+"+inNLongs+">"+getSize());
      if (inLongDelta+inNLongs>ioLongs.length)
        throw new StoreException("LongDelta+NLongs past end: "+inLongDelta+"+"+inNLongs+">"+ioLongs.length);
    }
    if (inNLongs>0) {
      long theNBytes=mVarSize*inNLongs;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      ByteStore theByteStore=getByteStore();
      if (theNBytes<=kByteSliceSize) {
        theByteStore.getBytes(inOffset*mVarSize,theByteSlice,0,(int) theNBytes);
        Conversions.bytesToVarLongs(theByteSlice,0,(int) theNBytes,ioLongs,inLongDelta,mVarSize);
      } else {
        int theChunkNLongs=kByteSliceSize/mVarSize;
        int theChunkNBytes=theChunkNLongs*mVarSize;
        int theDelta=inLongDelta;
        long theOffset=inOffset;
        long theLimit=inOffset+inNLongs-theChunkNLongs;
        while (theOffset<theLimit) {
          theByteStore.getBytes(theOffset*mVarSize,theByteSlice,0,theChunkNBytes);
          Conversions.bytesToVarLongs(theByteSlice,0,theChunkNBytes,ioLongs,theDelta,mVarSize);
          theOffset+=theChunkNLongs;
          theDelta+=theChunkNLongs;
        }
        int theRemainder=(int) (inOffset+inNLongs-theOffset);
        if (theRemainder>0) {
          theByteStore.getBytes(theOffset*mVarSize,theByteSlice,0,theRemainder*mVarSize);
          Conversions.bytesToVarLongs(theByteSlice,0,theRemainder*mVarSize,ioLongs,theDelta,mVarSize);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
  }

  public void getVars(long inOffset, int[] ioInts, int inIntDelta, int inNInts) {
    if (kRangeChecking) {
      if (mVarSize>4)
        throw new StoreException(mVarSize+" byte Vars do not fit in ints");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNInts<0)
        throw new StoreException("Negative NInts: "+inNInts);
      if (inIntDelta<0)
        throw new StoreException("Negative IntDelta: "+inIntDelta);
      if (inOffset+inNInts>getSize())
        throw new StoreException("Offset+NInts past end: "+inOffset+"+"+inNInts+">"+getSize());
      if (inIntDelta+inNInts>ioInts.length)
        throw new StoreException("IntDelta+NInts past end: "+inIntDelta+"+"+inNInts+">"+ioInts.length);
    }
    if (inNInts>0) {
      long theNBytes=mVarSize*inNInts;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      ByteStore theByteStore=getByteStore();
      if (theNBytes<=kByteSliceSize) {
        theByteStore.getBytes(inOffset*mVarSize,theByteSlice,0,(int) theNBytes);
        Conversions.bytesToVarInts(theByteSlice,0,(int) theNBytes,ioInts,inIntDelta,mVarSize);
      } else {
        int theChunkNInts=kByteSliceSize/mVarSize;
        int theChunkNBytes=theChunkNInts*mVarSize;
        int theDelta=inIntDelta;
        long theOffset=inOffset;
        long theLimit=inOffset+inNInts-theChunkNInts;
        while (theOffset<theLimit) {
          theByteStore.getBytes(theOffset*mVarSize,theByteSlice,0,theChunkNBytes);
          Conversions.bytesToVarInts(theByteSlice,0,theChunkNBytes,ioInts,theDelta,mVarSize);
          theOffset+=theChunkNInts;
          theDelta+=theChunkNInts;
        }
        int theRemainder=(int) (inOffset+inNInts-theOffset);
        if (theRemainder>0) {
          theByteStore.getBytes(theOffset*mVarSize,theByteSlice,0,theRemainder*mVarSize);
          Conversions.bytesToVarInts(theByteSlice,0,theRemainder*mVarSize,ioInts,theDelta,mVarSize);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public void getVars(long inOffset, long[] ioLongs) { getVars(inOffset,ioLongs,0,ioLongs.length); }
  public void getVars(long inOffset, int[] ioInts) { getVars(inOffset,ioInts,0,ioInts.length); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public long[] getLongs(long inOffset, int inNLongs) {
    long[] theLongs=Allocate.newLongs(inNLongs);
    getVars(inOffset,theLongs,0,inNLongs);
    return theLongs;
  }

  public int[] getInts(long inOffset, int inNInts) {
    int[] theInts=Allocate.newInts(inNInts);
    getVars(inOffset,theInts,0,inNInts);
    return theInts;
  }

//--------------------------------------------------------------------------------------------------------
// getAllVars
//--------------------------------------------------------------------------------------------------------

  public int getAllVars(long[] ioLongs, int inLongDelta) {
    long theSize=getSize();
    if (theSize>ioLongs.length-inLongDelta)
      throw new StoreException("Array too small");
    getVars(0,ioLongs,inLongDelta,(int) theSize);
    return (int) theSize;
  }

  public int getAllVars(int[] ioInts, int inIntDelta) {
    long theSize=getSize();
    if (theSize>ioInts.length-inIntDelta)
      throw new StoreException("Array too small");
    getVars(0,ioInts,inIntDelta,(int) getSize());
    return (int) theSize;
  }

//--------------------------------------------------------------------------------------------------------
// getAllVars
//--------------------------------------------------------------------------------------------------------

  public int getAllVars(long[] ioLongs) { return getAllVars(ioLongs,0); }
  public int getAllVars(int[] ioInts) { return getAllVars(ioInts,0); }

//--------------------------------------------------------------------------------------------------------
// getAllVars
//--------------------------------------------------------------------------------------------------------

  public long[] getAllLongs() {
    long theSize=getSize();
    if (theSize>k1G/kLongMemory)
      throw new StoreException("Store too big");
    return getLongs(0,(int) theSize);
  }

  public int[] getAllInts() {
    long theSize=getSize();
    if (theSize>k1G/kIntMemory)
      throw new StoreException("Store too big");
    return getInts(0,(int) theSize);
  }

//--------------------------------------------------------------------------------------------------------
// getVarStore
//--------------------------------------------------------------------------------------------------------

  public void getVarStore(long inOffset, long inNLongs, VarStore inDstVarStore) {
    if (inNLongs>0) {
      if (mVarSize==inDstVarStore.mVarSize)
        getByteStore().getByteStore(inOffset*mVarSize,inNLongs*mVarSize,inDstVarStore.getByteStore());
      else {
        long theNFullSlices=SliceStore.getNFullLongSlices(inNLongs);
        int theRemainder=SliceStore.getLongRemainder(inNLongs);
        SliceStore theSliceStore=SliceStore.getSliceStore();
        long[] theLongSlice=theSliceStore.getLongSlice();
        for (long i=0; i<theNFullSlices; i++) {
          getVars(inOffset+i*kLongSliceSize,theLongSlice,0,kLongSliceSize);
          inDstVarStore.appendVars(theLongSlice,0,kLongSliceSize);
        }
        if (theRemainder>0) {
          getVars(inOffset+theNFullSlices*kLongSliceSize,theLongSlice,0,theRemainder);
          inDstVarStore.appendVars(theLongSlice,0,theRemainder);
        }
        theSliceStore.putLongSlice(theLongSlice);
      }
    }
  }

  public void getVarStore(VarStore inDstVarStore) { getVarStore(0,getSize(),inDstVarStore); }

//--------------------------------------------------------------------------------------------------------
// getVarRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getVarRAMStore(long inOffset, long inNLongs) {
    VarRAMStore theVarRAMStore=new VarRAMStore(mVarSize,inNLongs);
    getVarStore(inOffset,inNLongs,theVarRAMStore);
    return theVarRAMStore;
  }

  public VarRAMStore getVarRAMStore() { return getVarRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// hash - single var
//--------------------------------------------------------------------------------------------------------

  public long hash(long inLong) { return HashUtils.hash(inLong); }
  public long hash(int inInt) { return HashUtils.hash(inInt); }

//--------------------------------------------------------------------------------------------------------
// hash - array of vars
//--------------------------------------------------------------------------------------------------------

  public long hash(long[] inLongs, int inLongDelta, int inNLongs) { 
    return HashUtils.hash(inLongs,inLongDelta,inNLongs); }

  public long hash(long[] inLongs) { return hash(inLongs,0,inLongs.length); }

  public long hash(int[] inInts, int inIntDelta, int inNInts) { 
    return HashUtils.hash(inInts,inIntDelta,inNInts); }

  public long hash(int[] inInts) { return hash(inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) { return hash(getLong(inIndex)); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex, int inNVars) { 
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theLongSlice=theSliceStore.getLongSlice();
    getVars(inIndex,theLongSlice,0,inNVars);
    long theHash=hash(theLongSlice,0,inNVars); 
    theSliceStore.putLongSlice(theLongSlice);
    return theHash;
  }

//--------------------------------------------------------------------------------------------------------
// setVar
//--------------------------------------------------------------------------------------------------------

  public void setVar(long inOffset, long inLong) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }
    ensureVarSize(inLong);
    getByteStore().setVarLong(inOffset*mVarSize,inLong,mVarSize);
  }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inOffset, long inValue, long inNCopies) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
      if (inOffset+inNCopies>getSize())
        throw new StoreException("Offset+NCopies past end: "+inOffset+"+"+inNCopies+">"+getSize());
    }
    if (inNCopies>0) {
      ensureVarSize(inValue);
      long theNBytes=mVarSize*inNCopies;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      Conversions.longToVarBytes(inValue,theByteSlice,0,mVarSize);
      ByteStore theByteStore=getByteStore();
      if (theNBytes<=kByteSliceSize) {
        for (int i=mVarSize; i<theNBytes; i++)
          theByteSlice[i]=theByteSlice[i-mVarSize];
        theByteStore.setBytes(inOffset*mVarSize,theByteSlice,0,(int) theNBytes);
      } else {
        for (int i=mVarSize; i<kByteSliceSize; i++)
          theByteSlice[i]=theByteSlice[i-mVarSize];
        int theChunkNLongs=kByteSliceSize/mVarSize;
        int theChunkNBytes=theChunkNLongs*mVarSize;
        long theOffset=inOffset;
        long theLimit=inOffset+inNCopies-theChunkNLongs;
        while (theOffset<theLimit) {
          theByteStore.setBytes(theOffset*mVarSize,theByteSlice,0,theChunkNBytes);
          theOffset+=theChunkNLongs;
        }
        int theRemainder=(int) (inOffset+inNCopies-theOffset);
        if (theRemainder>0)
          theByteStore.setBytes(theOffset*mVarSize,theByteSlice,0,theRemainder*mVarSize);
      }
      theSliceStore.putByteSlice(theByteSlice);        
    }
  }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inOffset, long[] inLongs, int inLongDelta, int inNLongs) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNLongs<0)
        throw new StoreException("Negative NLongs: "+inNLongs);
      if (inLongDelta<0)
        throw new StoreException("Negative LongDelta: "+inLongDelta);
      if (inOffset+inNLongs>getSize())
        throw new StoreException("Offset+NLongs past end: "+inOffset+"+"+inNLongs+">"+getSize());
      if (inLongDelta+inNLongs>inLongs.length)
        throw new StoreException("LongDelta+NLongs past end: "+inLongDelta+"+"+inNLongs+">"+inLongs.length);
    }
    if (inNLongs>0) {
      ensureVarSize(inLongs,inLongDelta,inNLongs);
      long theNBytes=mVarSize*inNLongs;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      ByteStore theByteStore=getByteStore();
      if (theNBytes<=kByteSliceSize) {
        Conversions.longsToVarBytes(inLongs,inLongDelta,inNLongs,theByteSlice,0,mVarSize);
        theByteStore.setBytes(inOffset*mVarSize,theByteSlice,0,inNLongs*mVarSize);
      } else {
        int theChunkNLongs=kByteSliceSize/mVarSize;
        int theChunkNBytes=theChunkNLongs*mVarSize;
        int theDelta=inLongDelta;
        long theOffset=inOffset;
        long theLimit=inOffset+inNLongs-theChunkNLongs;
        while (theOffset<theLimit) {
          Conversions.longsToVarBytes(inLongs,theDelta,theChunkNLongs,theByteSlice,0,mVarSize);
          theByteStore.setBytes(theOffset*mVarSize,theByteSlice,0,theChunkNBytes);
          theOffset+=theChunkNLongs;
          theDelta+=theChunkNLongs;
        }
        int theRemainder=(int) (inOffset+inNLongs-theOffset);
        if (theRemainder>0) {
          Conversions.longsToVarBytes(inLongs,theDelta,theRemainder,theByteSlice,0,mVarSize);
          theByteStore.setBytes(theOffset*mVarSize,theByteSlice,0,theRemainder*mVarSize);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
  }

  public void setVars(long inOffset, int[] inInts, int inIntDelta, int inNInts) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNInts<0)
        throw new StoreException("Negative NInts: "+inNInts);
      if (inIntDelta<0)
        throw new StoreException("Negative IntDelta: "+inIntDelta);
      if (inOffset+inNInts>getSize())
        throw new StoreException("Offset+NInts past end: "+inOffset+"+"+inNInts+">"+getSize());
      if (inIntDelta+inNInts>inInts.length)
        throw new StoreException("IntDelta+NInts past end: "+inIntDelta+"+"+inNInts+">"+inInts.length);
    }
    if (inNInts>0) {
      ensureVarSize(inInts,inIntDelta,inNInts);
      long theNBytes=mVarSize*inNInts;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      ByteStore theByteStore=getByteStore();
      if (theNBytes<=kByteSliceSize) {
        Conversions.intsToVarBytes(inInts,inIntDelta,inNInts,theByteSlice,0,mVarSize);
        theByteStore.setBytes(inOffset*mVarSize,theByteSlice,0,inNInts*mVarSize);
      } else {
        int theChunkNInts=kByteSliceSize/mVarSize;
        int theChunkNBytes=theChunkNInts*mVarSize;
        int theDelta=inIntDelta;
        long theOffset=inOffset;
        long theLimit=inOffset+inNInts-theChunkNInts;
        while (theOffset<theLimit) {
          Conversions.intsToVarBytes(inInts,theDelta,theChunkNInts,theByteSlice,0,mVarSize);
          theByteStore.setBytes(theOffset*mVarSize,theByteSlice,0,theChunkNBytes);
          theOffset+=theChunkNInts;
          theDelta+=theChunkNInts;
        }
        int theRemainder=(int) (inOffset+inNInts-theOffset);
        if (theRemainder>0) {
          Conversions.intsToVarBytes(inInts,theDelta,theRemainder,theByteSlice,0,mVarSize);
          theByteStore.setBytes(theOffset*mVarSize,theByteSlice,0,theRemainder*mVarSize);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inOffset, long[] inLongs) { setVars(inOffset,inLongs,0,inLongs.length); }
  public void setVars(long inOffset, int[] inInts) {setVars(inOffset,inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// setAllVars
//--------------------------------------------------------------------------------------------------------

  public void setAllVars(long inLong) { setVars(0,inLong,getSize()); }

//--------------------------------------------------------------------------------------------------------
// setVarStore
//--------------------------------------------------------------------------------------------------------

  public void setVarStore(long inDstOffset, VarStore inSrcVarStore, long inSrcOffset, long inNLongs) {
    if (inNLongs>0) {
      if (mVarSize==inSrcVarStore.mVarSize)
        getByteStore().setByteStore(inDstOffset*mVarSize,
            inSrcVarStore.getByteStore(),inSrcOffset*mVarSize,inNLongs*mVarSize);
      else {
        long theNFullSlices=SliceStore.getNFullLongSlices(inNLongs);
        int theRemainder=SliceStore.getLongRemainder(inNLongs);
        SliceStore theSliceStore=SliceStore.getSliceStore();
        long[] theLongSlice=theSliceStore.getLongSlice();
        for (long i=0; i<theNFullSlices; i++) {
          inSrcVarStore.getVars(inSrcOffset+i*kLongSliceSize,theLongSlice,0,kLongSliceSize);
          setVars(inDstOffset+i*kLongSliceSize,theLongSlice,0,kLongSliceSize);
        }
        if (theRemainder>0) {
          inSrcVarStore.getVars(inSrcOffset+theNFullSlices*kLongSliceSize,theLongSlice,0,theRemainder);
          setVars(inDstOffset+theNFullSlices*kLongSliceSize,theLongSlice,0,theRemainder);
        }
        theSliceStore.putLongSlice(theLongSlice);
      }
    }
  }

  public void setVarStore(long inDstOffset, VarStore inSrcVarStore) {
    setVarStore(inDstOffset,inSrcVarStore,0,inSrcVarStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// addToVar
//--------------------------------------------------------------------------------------------------------

  public void addToVar(long inOffset, long inValue) { 
    if (inValue!=0)
      setVar(inOffset,getLong(inOffset)+inValue); 
  }

//--------------------------------------------------------------------------------------------------------
// addToVars
//--------------------------------------------------------------------------------------------------------

  public void addToVars(long inOffset, long inNLongs, long inValue) {
    if ((inNLongs>0)&&(inValue!=0)) {
      long theOffset=inOffset;
      long theLimit=inOffset+inNLongs-kLongSliceSize;
      SliceStore theSliceStore=SliceStore.getSliceStore();
      long[] theLongSlice=theSliceStore.getLongSlice();
      while (theOffset<=theLimit) {
        getVars(theOffset,theLongSlice,0,kLongSliceSize);
        for (int i=0; i<kLongSliceSize; i++)
          theLongSlice[i]+=inValue;
        setVars(theOffset,theLongSlice,0,kLongSliceSize);
        theOffset+=kLongSliceSize;
      }
      int theRemainder=(int) (inOffset+inNLongs-theOffset);
      if (theRemainder>0) {
        getVars(theOffset,theLongSlice,0,theRemainder);
        for (int i=0; i<theRemainder; i++)
          theLongSlice[i]+=inValue;
        setVars(theOffset,theLongSlice,0,theRemainder);
      }
      theSliceStore.putLongSlice(theLongSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// addToAllVars
//--------------------------------------------------------------------------------------------------------

  public void addToAllVars(long inValue) { addToVars(0,getSize(),inValue); }

//--------------------------------------------------------------------------------------------------------
// appendVar
//--------------------------------------------------------------------------------------------------------

  public void appendVar(long inLong) {
    long theSize=getSize();
    setSize(theSize+1);
    setVar(theSize,inLong);
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long inLong, long inNCopies) {
    if (kRangeChecking) 
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
    if (inNCopies>0) {
      long theSize=getSize();
      setSize(theSize+inNCopies);
      setVars(theSize,inLong,inNCopies);
    }
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs, int inLongDelta, int inNLongs) {
    if (inNLongs>0) {
      long theSize=getSize();
      setSize(theSize+inNLongs);
      setVars(theSize,inLongs,inLongDelta,inNLongs);
    }
  }

  public void appendVars(int[] inInts, int inIntDelta, int inNInts) {
    if (inNInts>0) {
      long theSize=getSize();
      setSize(theSize+inNInts);
      setVars(theSize,inInts,inIntDelta,inNInts);
    }
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs) { appendVars(inLongs,0,inLongs.length); } 
  public void appendVars(int[] inInts) { appendVars(inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// appendVarStore
//--------------------------------------------------------------------------------------------------------

  public void appendVarStore(VarStore inSrcVarStore, long inSrcOffset, long inNLongs) {
    if (inNLongs>0) {
      if (mVarSize==inSrcVarStore.mVarSize)
        getByteStore().appendByteStore(inSrcVarStore.getByteStore(),inSrcOffset*mVarSize,inNLongs*mVarSize);
      else {
        ensureCapacity(getSize()+inNLongs);
        long theNFullSlices=SliceStore.getNFullLongSlices(inNLongs);
        int theRemainder=SliceStore.getLongRemainder(inNLongs);
        SliceStore theSliceStore=SliceStore.getSliceStore();
        long[] theLongSlice=theSliceStore.getLongSlice();
        for (long i=0; i<theNFullSlices; i++) {
          inSrcVarStore.getVars(inSrcOffset+i*kLongSliceSize,theLongSlice,0,kLongSliceSize);
          appendVars(theLongSlice,0,kLongSliceSize);
        }
        if (theRemainder>0) {
          inSrcVarStore.getVars(inSrcOffset+theNFullSlices*kLongSliceSize,theLongSlice,0,theRemainder);
          appendVars(theLongSlice,0,theRemainder);
        }
        theSliceStore.putLongSlice(theLongSlice);
      }
    }
  }

  public void appendVarStore(VarStore inSrcVarStore) {
    appendVarStore(inSrcVarStore,0,inSrcVarStore.getSize()); }
  
//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(boolean inDescending) { return SortMapUtils.sortMap(this,inDescending); }

  public VarRAMStore getSortMap() { return getSortMap(false); }

//--------------------------------------------------------------------------------------------------------
// sanityCheck
//--------------------------------------------------------------------------------------------------------

  public void sanityCheck() {
    if (getInnerStore().getSize()%getVarSize()!=0)
      throw new StoreException("Bad VarSize");
  }
  
}

