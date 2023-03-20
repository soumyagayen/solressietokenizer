//--------------------------------------------------------------------------------------------------------
// ByteRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.plain;

import gravel.store.*;
import gravel.utils.*;
import sun.misc.*;

//--------------------------------------------------------------------------------------------------------
// ByteRAMStore
//--------------------------------------------------------------------------------------------------------

public class ByteRAMStore extends ByteStore implements RAMStoreInterface {
  
//--------------------------------------------------------------------------------------------------------
// ByteRAMStore member vars
//--------------------------------------------------------------------------------------------------------
  
  private byte[][]      mSlices; 
 
//--------------------------------------------------------------------------------------------------------
// ByteRAMStore - create
//--------------------------------------------------------------------------------------------------------

  public ByteRAMStore(long inCapacity, long[] inParams) {
    super(0,inCapacity,inParams);
    mSlices=SliceStore.getSliceStore().getByteSlices(getCapacity());
  }

  public ByteRAMStore(long inCapacity) { this(inCapacity,null); }

  public ByteRAMStore() { this(64); }

//--------------------------------------------------------------------------------------------------------
// ByteRAMStore - from array
//--------------------------------------------------------------------------------------------------------

  public ByteRAMStore(byte[] inBytes, int inByteDelta, int inNBytes, long[] inParams) {
    this(inNBytes,inParams);
    appendBytes(inBytes,inByteDelta,inNBytes);
  }

  public ByteRAMStore(byte[] inBytes, int inByteDelta, int inNBytes) {
    this(inBytes,inByteDelta,inNBytes,null); }

  public ByteRAMStore(byte[] inBytes) { this(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// ByteRAMStore - from String
//--------------------------------------------------------------------------------------------------------

  public ByteRAMStore(String inContent) { 
    this(Math.round(inContent.length()*1.10+16)); 
    appendUTF8(inContent);
  }

//--------------------------------------------------------------------------------------------------------
// ByteRAMStore - from ByteStore
//    use theByteStore.getByteRAMStore() 
//     or theByteStore.getByteRAMStore(theIndex,theNBytes);
//--------------------------------------------------------------------------------------------------------

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  // Note that RAM store will have capacity=size, as if compacted
  public static ByteRAMStore load(ByteStore inByteStore) {
    return inByteStore.getByteRAMStore(); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteRAMStore load(String inFilename) {
    ByteDiskStore theByteDiskStore=ByteDiskStore.load(inFilename); 
    ByteRAMStore theByteRAMStore=load(theByteDiskStore);
    theByteDiskStore.close();
    return theByteRAMStore;
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    if (kRangeChecking) 
      if (getIsClosed())
        throw new StoreException("Store closed");
    
    long theCapacity=getCapacity();
    if (inCompact)
      theCapacity=getSize();
    
    ByteDiskStore theByteDiskStore=new ByteDiskStore(inFilename,theCapacity,getParams());
    theByteDiskStore.appendByteStore(this); 
    theByteDiskStore.close();
  }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    byte[][] theSlices=mSlices;
    mSlices=null;
    if (theSlices!=null)
      SliceStore.getSliceStore().putByteSlices(theSlices,0,theSlices.length);
    super.close();
  }

//--------------------------------------------------------------------------------------------------------
// getIsClosed
//--------------------------------------------------------------------------------------------------------

  public boolean getIsClosed() { return (mSlices==null); }

//--------------------------------------------------------------------------------------------------------
// dangerousGetSlices
//--------------------------------------------------------------------------------------------------------

  public byte[][] dangerousGetSlices() { return mSlices; }

//--------------------------------------------------------------------------------------------------------
// dangerousGetSlice
//--------------------------------------------------------------------------------------------------------

  public byte[] dangerousGetSlice(long inIndex) { return mSlices[(int) inIndex]; }

//--------------------------------------------------------------------------------------------------------
// getContentMemory
//--------------------------------------------------------------------------------------------------------

  public long getContentMemory() { return getSize(); }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() {
    if (kRangeChecking) 
      if (getIsClosed())
        throw new StoreException("Store closed");
    long theMemory=super.getMemory()+
        2*kReferenceMemory+
        getCapacity()+
        Allocate.getArrayMemory(mSlices);
    if (mSlices!=null)
      theMemory+=mSlices.length*kArrayMemory;
    return theMemory;
  }

//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) {
    long theOldCapacity=getCapacity();
    super.setCapacity(inCapacity);
    long theNewCapacity=getCapacity();

    if (kRangeChecking) 
      if (getIsClosed())
        throw new StoreException("Store closed");

    int theOldNSlices=mSlices.length;
    int theNewNSlices=SliceStore.getNByteSlices(theNewCapacity);
    int theOldNFullSlices=SliceStore.getNFullByteSlices(theOldCapacity);
    int theNewNFullSlices=SliceStore.getNFullByteSlices(theNewCapacity);
    SliceStore theSliceStore=SliceStore.getSliceStore();

    // Allocate new slices array and copy over full slices
    byte[][] theOldSlices=mSlices;
    mSlices=new byte[theNewNSlices][];
    System.arraycopy(theOldSlices,0,mSlices,0,Math.min(theOldNFullSlices,theNewNFullSlices));
    
    if (theNewCapacity>theOldCapacity) {

      // Allocate rest of new slices - which may only be one, possibly partial
      long theNNewValues=theNewCapacity-theOldNFullSlices*(long) kByteSliceSize;
      theSliceStore.getByteSlices(mSlices,theOldNFullSlices,theNNewValues);

      // Any data on full slices is already in correct place
      // Any data on partial old slice needs to be copied
      // Note that this may be wasteful, since copying tail of capacity which may be empty of data
      int theOldRemainder=SliceStore.getByteRemainder(theOldCapacity);
      if (theOldRemainder>0)
        System.arraycopy(theOldSlices[theOldNFullSlices],0,mSlices[theOldNFullSlices],0,theOldRemainder);
      
    } else if (theNewCapacity<theOldCapacity) {

      // If there is a new partial slice, allocate it
      if (theNewNSlices>theNewNFullSlices) {
        int theNewRemainder=SliceStore.getByteRemainder(theNewCapacity);
        mSlices[theNewNFullSlices]=Allocate.newBytes(theNewRemainder);
        
        // Any data on full slices is already in correct place
        // Any data on partial new slice needs to be copied
        // Note that this may be wasteful, since copying tail of capacity which may be empty of data
        System.arraycopy(theOldSlices[theNewNFullSlices],0,mSlices[theNewNFullSlices],0,theNewRemainder);
      }

      // Recycle the extra old slices - which may only be one, possibly partial
      theSliceStore.putByteSlices(theOldSlices,theNewNFullSlices,theOldNSlices-theNewNFullSlices);

    } else {
      
      // Move last partial slice
      if (theOldNFullSlices<theOldNSlices)
        mSlices[theOldNFullSlices]=theOldSlices[theOldNFullSlices];
    }
    
    // Have kept all full slices - will free old slice array and possibly one partial slice
  }
   
//--------------------------------------------------------------------------------------------------------
// getByte
//--------------------------------------------------------------------------------------------------------

  public byte getByte(long inOffset) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }
    return mSlices[SliceStore.getNFullByteSlices(inOffset)]
                  [SliceStore.getByteRemainder(inOffset)];
  }

//--------------------------------------------------------------------------------------------------------
// getVarLong
//--------------------------------------------------------------------------------------------------------

//  @SuppressWarnings("restriction")
  public long getVarLong(long inOffset, int inLongSize) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if ((inLongSize<1)||(inLongSize>8))
        throw new StoreException("Invalid LongSize: "+inLongSize);
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset+inLongSize>getSize())
        throw new StoreException("Offset+LongSize past end: "+inOffset+"+"+inLongSize+">"+getSize());
    }

    int theSliceOffset=SliceStore.getByteRemainder(inOffset);    
    int theSliceN=SliceStore.getNFullByteSlices(inOffset);

    byte[] theFirstSlice=mSlices[theSliceN];
    long theLong;

    // If Unsafe available, use primitive type coercion
    if ((kUnsafe!=null)&&(theSliceOffset+8<kByteSliceSize)) {
      
      theLong=kUnsafe.getLong(theFirstSlice,(long) (Unsafe.ARRAY_BYTE_BASE_OFFSET+theSliceOffset));
      if (!ByteStore.kNativeOrderIsBigEndian) 
        theLong=Long.reverseBytes(theLong);  // Must reorder bytes if not BigEndian, but it is very fast
      
      theLong>>=((8-inLongSize)<<3);  // Keeps sign

    // else build long byte by byte
    } else {
      int theEnd=theSliceOffset+inLongSize;
      
      // First byte has sign
      theLong=theFirstSlice[theSliceOffset];
      
      // If VarLong from one slice
      if (theEnd<=kByteSliceSize) {
        for (int i=theSliceOffset+1; i<theEnd; i++) {
          theLong<<=8;
          theLong|=(theFirstSlice[i]&0x00ff);
        }
  
      // VarLong crosses slice break
      } else {
        for (int i=theSliceOffset+1; i<kByteSliceSize; i++) {
          theLong<<=8;
          theLong|=(theFirstSlice[i]&0x00ff);
        }
        byte[] theSecondSlice=mSlices[theSliceN+1];
        int theSecondNBytes=inLongSize-(kByteSliceSize-theSliceOffset);
        for (int i=0; i<theSecondNBytes; i++) {
          theLong<<=8;
          theLong|=(theSecondSlice[i]&0x00ff);
        }
      }
    }
    return theLong;
  }

//--------------------------------------------------------------------------------------------------------
// getVarDouble
//--------------------------------------------------------------------------------------------------------

  public double getVarDouble(long inOffset, int inDoubleSize) {
    long theLong=getVarLong(inOffset,inDoubleSize);
    theLong<<=(64-8*inDoubleSize);
    return Conversions.longToDouble(theLong);
  }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public void getBytes(long inOffset, byte[] ioBytes, int inByteDelta, int inNBytes) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inByteDelta<0)
        throw new StoreException("Negative ByteDelta: "+inByteDelta);
      if (inOffset+inNBytes>getSize())
        throw new StoreException("Offset+NBytes past end: "+inOffset+"+"+inNBytes+">"+getSize());
      if (inByteDelta+inNBytes>ioBytes.length)
        throw new StoreException("ByteDelta+NBytes past end: "+inByteDelta+"+"+inNBytes+">"+ioBytes.length);
    }

    if (inNBytes>0) {
      int theSliceN=SliceStore.getNFullByteSlices(inOffset);
      int theSliceOffset=SliceStore.getByteRemainder(inOffset);

      // Shortcut if small enough to fit in one slice
      if (theSliceOffset+inNBytes<=kByteSliceSize)
        System.arraycopy(mSlices[theSliceN],theSliceOffset,ioBytes,inByteDelta,inNBytes);

      // Multi-slice copy
      else {

        // First partial slice
        int thePartialNBytes=kByteSliceSize-theSliceOffset;
        System.arraycopy(mSlices[theSliceN],theSliceOffset,ioBytes,inByteDelta,thePartialNBytes);
        int theByteDelta=inByteDelta+thePartialNBytes;
        int theNBytes=inNBytes-thePartialNBytes;
        theSliceN++;
        
        // Middle full slices
        while (theNBytes>kByteSliceSize) {
          System.arraycopy(mSlices[theSliceN],0,ioBytes,theByteDelta,kByteSliceSize);
          theByteDelta+=kByteSliceSize;
          theNBytes-=kByteSliceSize;
          theSliceN++;
        }
        
        // Final partial slice
        if (theNBytes>0)
          System.arraycopy(mSlices[theSliceN],0,ioBytes,theByteDelta,theNBytes);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// setByte
//--------------------------------------------------------------------------------------------------------

  public void setByte(long inOffset, byte inByte) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }

    mSlices[SliceStore.getNFullByteSlices(inOffset)]
           [SliceStore.getByteRemainder(inOffset)]=inByte;
  }

//--------------------------------------------------------------------------------------------------------
// setVarLong
//--------------------------------------------------------------------------------------------------------
  
  public void setVarLong(long inOffset, long inLong, int inLongSize) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if ((inLongSize<1)||(inLongSize>8))
        throw new StoreException("Invalid LongSize: "+inLongSize);
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset+inLongSize>getSize())
        throw new StoreException("Offset+LongSize past end: "+inOffset+"+"+inLongSize+">"+getSize());
    }

    int theSliceN=SliceStore.getNFullByteSlices(inOffset);
    int theSliceOffset=SliceStore.getByteRemainder(inOffset);    

    byte[] theFirstSlice=mSlices[theSliceN];
    long theLong=inLong;

/*  
 * Don't think this is worth the trouble
 * Have to put 8 bytes, so 5 byte var needs to first get the other 3  
 * Messy with all the rearranging due to big/little endian difference
 * 

    long[] kVarLongByteMasks=new long[] {
        0xffffffffffffffffL,
        0x00ffffffffffffffL,
        0x0000ffffffffffffL,
        0x000000ffffffffffL,
        0x00000000ffffffffL,
        0x0000000000ffffffL,
        0x000000000000ffffL,
        0x00000000000000ffL,
        0x0000000000000000L,
    };
    
    // If Unsafe available, use primitive type coercion
    if ((kUnsafe!=null)&&(theSliceOffset+8<kByteSliceSize)) {
      theSliceOffset+=Unsafe.ARRAY_BYTE_BASE_OFFSET;
      
      if (inLongSize<8) {
        long theAlreadyThere=kUnsafe.getLong(theFirstSlice,(long) theSliceOffset);
        if (!ByteStore.kNativeOrderIsBigEndian) 
          theAlreadyThere=Long.reverseBytes(theAlreadyThere);

        int theShift=(8-inLongSize)<<3;
        theLong=((theLong<<=theShift)|(theAlreadyThere&kVarLongByteMasks[inLongSize]));
      }

      if (!ByteStore.kNativeOrderIsBigEndian) 
        theLong=Long.reverseBytes(theLong);  // Must reorder bytes if not BigEndian, but it is very fast
      kUnsafe.putLong(theFirstSlice,(long) theSliceOffset,theLong);

    } else {
*/    
    
      int theEnd=theSliceOffset+inLongSize;
      
      // if VarLong fits in one slice
      if (theEnd<=kByteSliceSize) {
        for (int i=theEnd-1; i>=theSliceOffset; i--) {
          theFirstSlice[i]=(byte) theLong;   // Most significant byte has sign and is written last
          theLong>>=8;
        }
  
      // VarLong crosses slice break
      } else {
        byte[] theSecondSlice=mSlices[theSliceN+1];
        int theSecondNBytes=inLongSize-(kByteSliceSize-theSliceOffset);
        for (int i=theSecondNBytes-1; i>=0; i--) {
          theSecondSlice[i]=(byte) theLong;
          theLong>>=8;
        }
        for (int i=kByteSliceSize-1; i>=theSliceOffset; i--) {
          theFirstSlice[i]=(byte) theLong;
          theLong>>=8;
        }
      }
      
      if (kRangeChecking)
        if ((theLong!=0)&&(theLong!=-1))
          throw new StoreException("Long, "+inLong+" did not fit in "+inLongSize+" bytes");
//    }
  }

//--------------------------------------------------------------------------------------------------------
// setVarDouble
//--------------------------------------------------------------------------------------------------------

  public void setVarDouble(long inOffset, double inDouble, int inDoubleSize) {
    long theLong=Conversions.doubleToLong(inDouble);
    theLong>>=(64-8*inDoubleSize);
    setVarLong(inOffset,theLong,inDoubleSize);
  }

//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public void setBytes(long inOffset, byte inByte, long inNCopies) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
      if (inOffset+inNCopies>getSize())
        throw new StoreException("Offset+NCopies past end: "+inOffset+"+"+inNCopies+">"+getSize());
    }

    if (inNCopies>0) {
      int theSliceN=SliceStore.getNFullByteSlices(inOffset);
      int theSliceOffset=SliceStore.getByteRemainder(inOffset);

      long theTop=theSliceOffset+inNCopies;
      if (theTop<=kByteSliceSize) {
        byte[] theSlice=mSlices[theSliceN];
        for (int i=theSliceOffset; i<theTop; i++)
          theSlice[i]=inByte;
      } else {
        byte[] theSlice=mSlices[theSliceN];
        for (int i=theSliceOffset; i<kByteSliceSize; i++)
          theSlice[i]=inByte;
        long theNBytes=inNCopies-(kByteSliceSize-theSliceOffset);
        theSliceN++;
        while (theNBytes>kByteSliceSize) {
          theSlice=mSlices[theSliceN];
          for (int i=0; i<kByteSliceSize; i++)
            theSlice[i]=inByte;
          theNBytes-=kByteSliceSize;
          theSliceN++;
        }
        theSlice=mSlices[theSliceN];
        for (int i=0; i<theNBytes; i++)
          theSlice[i]=inByte;
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public void setBytes(long inOffset, byte[] inBytes, int inByteDelta, int inNBytes) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inByteDelta<0)
        throw new StoreException("Negative ByteDelta: "+inByteDelta);
      if (inOffset+inNBytes>getSize())
        throw new StoreException("Offset+NBytes past end: "+inOffset+"+"+inNBytes+">"+getSize());
      if (inByteDelta+inNBytes>inBytes.length)
        throw new StoreException("ByteDelta+NBytes past end: "+inByteDelta+"+"+inNBytes+">"+inBytes.length);
    }

    if (inNBytes>0) {
      int theSliceN=SliceStore.getNFullByteSlices(inOffset);
      int theSliceOffset=SliceStore.getByteRemainder(inOffset);
 
      // Shortcut if small enough to fit in one slice
      if (theSliceOffset+inNBytes<=kByteSliceSize)
        System.arraycopy(inBytes,inByteDelta,mSlices[theSliceN],theSliceOffset,inNBytes);

      // Multi-slice copy
      else {
        
        // First partial slice
        int thePartialNBytes=kByteSliceSize-theSliceOffset;
        System.arraycopy(inBytes,inByteDelta,mSlices[theSliceN],theSliceOffset,thePartialNBytes);
        int theByteDelta=inByteDelta+thePartialNBytes;
        int theNBytes=inNBytes-thePartialNBytes;
        theSliceN++;
        
        // Middle full slice
        while (theNBytes>kByteSliceSize) {
          System.arraycopy(inBytes,theByteDelta,mSlices[theSliceN],0,kByteSliceSize);
          theByteDelta+=kByteSliceSize;
          theNBytes-=kByteSliceSize;
          theSliceN++;
        }
        
        // Final partial slice
        if (theNBytes>0)
          System.arraycopy(inBytes,theByteDelta,mSlices[theSliceN],0,theNBytes);
      }
    }
  }
 
}

