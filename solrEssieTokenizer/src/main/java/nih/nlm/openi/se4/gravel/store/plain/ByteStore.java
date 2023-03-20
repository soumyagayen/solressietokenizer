//--------------------------------------------------------------------------------------------------------
// ByteStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.plain;

import java.io.*;
import java.lang.reflect.*;
import java.nio.*;

import gravel.sort.*;
import gravel.store.*;
import gravel.store.hash.*;
import gravel.store.var.*;
import gravel.utils.*;
import sun.misc.*;

//--------------------------------------------------------------------------------------------------------
// ByteStore
//--------------------------------------------------------------------------------------------------------

public abstract class ByteStore implements StoreInterface {
 
//--------------------------------------------------------------------------------------------------------
// ByteStore consts
//--------------------------------------------------------------------------------------------------------
  
  public static final boolean     kNativeOrderIsBigEndian=(ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN);

  public static final Unsafe      kUnsafe;
 
//--------------------------------------------------------------------------------------------------------
// ByteStore class init
//--------------------------------------------------------------------------------------------------------

  static {
    try {
      Unsafe theUnsafe=null; 
      try {
        Constructor<Unsafe> unsafeConstructor=Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        theUnsafe=unsafeConstructor.newInstance();
      } catch (Exception e) { }
      kUnsafe=theUnsafe;
    } catch (Throwable e) {
      System.err.println(FormatUtils.formatException("Cannot init ByteStore",e));
      throw e;
    }
  }

//--------------------------------------------------------------------------------------------------------
// ByteStore member vars
//--------------------------------------------------------------------------------------------------------

  private long        mSize;
  private long        mCapacity;
  private long[]      mParams;
  private String      mCreateTrace; 

//--------------------------------------------------------------------------------------------------------
// ByteStore - create
//--------------------------------------------------------------------------------------------------------

  public ByteStore(long inSize, long inCapacity, long[] inParams) {
    mSize=inSize;
    mCapacity=Math.max(16,Math.max(inSize,inCapacity));
    if ((inParams!=null)&&(inParams.length>0))
      mParams=inParams.clone();
  }

//--------------------------------------------------------------------------------------------------------
// ByteStore - copy
//--------------------------------------------------------------------------------------------------------

  public ByteStore(ByteStore inByteStore) {
    this(inByteStore.mSize,inByteStore.mCapacity,inByteStore.mParams); }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------
  
  public void close() {
    mSize=kNotFound;
    mCapacity=kNotFound;
    mParams=null;
    mCreateTrace=null;
  }

//--------------------------------------------------------------------------------------------------------
// deleteStore
//--------------------------------------------------------------------------------------------------------

  public static void deleteStore(String inFilename) { 
    try {
      FileUtils.deleteFile(inFilename); 
    } catch (IOException e) {
      throw new StoreException("Could not delete store "+inFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getSize
//--------------------------------------------------------------------------------------------------------

  public long getSize() { return mSize; }

//--------------------------------------------------------------------------------------------------------
// getCapacity
//--------------------------------------------------------------------------------------------------------

  public long getCapacity() { return mCapacity; }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() { 
    return Allocate.getObjectMemory(2*kReferenceMemory+2*kLongMemory)+
        Allocate.getArrayMemory(mParams)+
        Allocate.getStringMemory(mCreateTrace); 
  }

//--------------------------------------------------------------------------------------------------------
// getNParams
//--------------------------------------------------------------------------------------------------------

  public int getNParams() { return (mParams==null)?0:mParams.length; }

//--------------------------------------------------------------------------------------------------------
// getParam
//--------------------------------------------------------------------------------------------------------

  public long getParam(int inIndex) { 
    if (inIndex>=getNParams())
      return kNotFound;
    else
      return mParams[inIndex]; 
  }

//--------------------------------------------------------------------------------------------------------
// getParams
//--------------------------------------------------------------------------------------------------------

  public long[] getParams() { 
    int theNParams=getNParams();
    long[] theParams=new long[theNParams];
    for (int i=0; i<theNParams; i++)
      theParams[i]=getParam(i);
    return theParams;
  }

//--------------------------------------------------------------------------------------------------------
// setSize
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inSize) {
    if (inSize<0) 
      throw new StoreException("Set size less than zero");
    if (inSize>mSize) 
      ensureCapacity(inSize);
    mSize=inSize;
  }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public void clear() { setSize(0); }

//--------------------------------------------------------------------------------------------------------
// truncateBy
//--------------------------------------------------------------------------------------------------------

  public void truncateBy(long inNValues) { setSize(getSize()-inNValues); }

//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) {
    if (inCapacity<0) 
      throw new StoreException("Set capacity less than zero");
    if (inCapacity<mSize) 
      throw new StoreException("Set capacity less than size");
    mCapacity=inCapacity;
  }

//--------------------------------------------------------------------------------------------------------
// compact
//--------------------------------------------------------------------------------------------------------

  public void compact() { setCapacity(getSize()); }

//--------------------------------------------------------------------------------------------------------
// ensureCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureCapacity(long inSize) {
    if (inSize>getCapacity()) {
      long theNewCapacity;
      if (inSize<4*k1K)
        theNewCapacity=inSize*2;
      else if (inSize<k1M)
        theNewCapacity=((3*(inSize/2)-1)/kByteSliceSize+1)*kByteSliceSize;
      else if (inSize<k1G)
        theNewCapacity=((5*(inSize/4)-1)/kByteSliceSize+1)*kByteSliceSize;
      else
        theNewCapacity=((7*(inSize/6)-1)/kByteSliceSize+1)*kByteSliceSize;
      setCapacity(theNewCapacity);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setNParams
//--------------------------------------------------------------------------------------------------------

  public void setNParams(int inNParams) {
    if (inNParams<0) 
      throw new StoreException("Set NParams less than zero");
    if (inNParams>4) 
      throw new StoreException("Set NParams greater than 4: "+inNParams);
    if (inNParams==0)
      mParams=null;
    else if (inNParams!=getNParams()) {
      long[] theOldParams=mParams;
      mParams=Allocate.newLongs(inNParams);
      if (theOldParams!=null)
        System.arraycopy(theOldParams,0,mParams,0,Math.min(inNParams,theOldParams.length));
    }
  }

//--------------------------------------------------------------------------------------------------------
// setParam
//--------------------------------------------------------------------------------------------------------

  public void setParam(int inIndex, long inParam) { 
    if (inIndex>=getNParams()) 
      setNParams(inIndex+1);
    mParams[inIndex]=inParam; 
  }

//--------------------------------------------------------------------------------------------------------
// setParams
//--------------------------------------------------------------------------------------------------------

  public void setParams(long[] inParams) { 
    if (inParams==null)
      setNParams(0);
    else {
      setNParams(inParams.length);
      for (int i=0; i<inParams.length; i++)
        setParam(i,inParams[i]);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getByte
//--------------------------------------------------------------------------------------------------------

  public abstract byte getByte(long inOffset);

//--------------------------------------------------------------------------------------------------------
// getBoolean
//--------------------------------------------------------------------------------------------------------

  public boolean getBoolean(long inOffset) { return (getByte(inOffset)!=0); }

//--------------------------------------------------------------------------------------------------------
// getVarLong
//--------------------------------------------------------------------------------------------------------

  public abstract long getVarLong(long inOffset, int inLongSize);

//--------------------------------------------------------------------------------------------------------
// getVarDouble
//--------------------------------------------------------------------------------------------------------

  public abstract double getVarDouble(long inOffset, int inDoubleSize);

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public abstract void getBytes(long inOffset, byte[] ioBytes, int inByteDelta, int inNBytes); 

  public void getBytes(long inOffset, byte[] ioBytes, int inNBytes) {
    getBytes(inOffset,ioBytes,0,ioBytes.length); }

  public void getBytes(long inOffset, byte[] ioBytes) {
    getBytes(inOffset,ioBytes,0,ioBytes.length); }

  public byte[] getBytes(long inOffset, int inNBytes) {
    byte[] theBytes=Allocate.newBytes(inNBytes);
    getBytes(inOffset,theBytes,0,inNBytes);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// getBooleans
//--------------------------------------------------------------------------------------------------------

  public void getBooleans(long inOffset, boolean[] ioBooleans, int inBooleanDelta, int inNBooleans) { 
    byte[] theBytes=getBytes(inOffset,inNBooleans);
    int theDelta=inBooleanDelta;
    for (int i=0; i<theBytes.length; i++)
      ioBooleans[theDelta++]=(theBytes[i]!=0);
  }

  public void getBooleans(long inOffset, boolean[] ioBooleans, int inNBooleans) {
    getBooleans(inOffset,ioBooleans,0,ioBooleans.length); }

  public void getBooleans(long inOffset, boolean[] ioBooleans) {
    getBooleans(inOffset,ioBooleans,0,ioBooleans.length); }

  public boolean[] getBooleans(long inOffset, int inNBooleans) {
    boolean[] theBooleans=Allocate.newBooleans(inNBooleans);
    getBooleans(inOffset,theBooleans,0,inNBooleans);
    return theBooleans;
  }

//--------------------------------------------------------------------------------------------------------
// getAllBytes
//--------------------------------------------------------------------------------------------------------

  public int getAllBytes(byte[] ioBytes, int inByteDelta) {
    long theSize=getSize();
    if (theSize>ioBytes.length-inByteDelta)
      throw new StoreException("Array too small");
    getBytes(0,ioBytes,inByteDelta,(int) theSize);
    return (int) theSize;
  }

  public int getAllBytes(byte[] ioBytes) { return getAllBytes(ioBytes,0); }

  public byte[] getAllBytes() {
    long theSize=getSize();
    if (theSize>k1G/kByteMemory)
      throw new StoreException("Store too big");
    return getBytes(0,(int) theSize);
  }

//--------------------------------------------------------------------------------------------------------
// getAllBooleans
//--------------------------------------------------------------------------------------------------------

  public int getAllBooleans(boolean[] ioBooleans, int inBooleanDelta) {
    long theSize=getSize();
    if (theSize>ioBooleans.length-inBooleanDelta)
      throw new StoreException("Array too small");
    getBooleans(0,ioBooleans,inBooleanDelta,(int) theSize);
    return (int) theSize;
  }

  public int getAllBooleans(boolean[] ioBooleans) { return getAllBooleans(ioBooleans,0); }

  public boolean[] getAllBooleans() {
    long theSize=getSize();
    if (theSize>k1G/kBooleanMemory)
      throw new StoreException("Store too big");
    return getBooleans(0,(int) theSize);
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8
//--------------------------------------------------------------------------------------------------------

  public char getUTF8(long inOffset) {
    byte[] theBytes=new byte[4];
    getBytes(inOffset,theBytes,0,(int) Math.min(4,getSize()-inOffset));
    return UTF8Utils.bytesToUTF8Char(theBytes,0);
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8
//--------------------------------------------------------------------------------------------------------

  public String getUTF8(long inOffset, int inNBytes) { 
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    String theString;
    if (inNBytes<kByteSliceSize) {
      getBytes(inOffset,theByteSlice,0,inNBytes);
      theString=UTF8Utils.bytesToUTF8String(theByteSlice,0,inNBytes);
    } else {
      StringBuffer theStringBuffer=new StringBuffer(inNBytes);
      char[] theCharSlice=theSliceStore.getCharSlice();
      long theOffset=inOffset;
      long theLimit=inOffset+inNBytes-SliceStore.kCharSliceSize;
      while (theOffset<=theLimit) {
        getBytes(theOffset,theByteSlice,0,SliceStore.kCharSliceSize);
        int theNBytes=UTF8Utils.backToUTF8Start(theByteSlice,SliceStore.kCharSliceSize-1);
        int theNChars=UTF8Utils.bytesToUTF8Chars(theByteSlice,0,theNBytes,theCharSlice,0);
        theStringBuffer.append(theCharSlice,0,theNChars);
        theOffset+=theNBytes;
      }
      int theRemainder=(int) (inOffset+inNBytes-theOffset);
      if (theRemainder>0) {
        getBytes(theOffset,theByteSlice,0,theRemainder);
        int theNChars=UTF8Utils.bytesToUTF8Chars(theByteSlice,0,theRemainder,theCharSlice,0);
        theStringBuffer.append(theCharSlice,0,theNChars);
      }
      theSliceStore.putCharSlice(theCharSlice);
      theString=theStringBuffer.toString();
    }
    theSliceStore.putByteSlice(theByteSlice);
    return theString;
  }

//--------------------------------------------------------------------------------------------------------
// getAllUTF8
//--------------------------------------------------------------------------------------------------------

  public String getAllUTF8() {
    long theSize=getSize();
    if (theSize>k1G/kByteMemory)
      throw new StoreException("Store too big");
    return getUTF8(0,(int) theSize);
  }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  public void getByteStore(long inOffset, long inNBytes, ByteStore inDstByteStore) { 
    inDstByteStore.ensureCapacity(inDstByteStore.getSize()+inNBytes);
    long theNFullSlices=SliceStore.getNFullByteSlices(inNBytes);
    int theRemainder=SliceStore.getByteRemainder(inNBytes);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    for (long i=0; i<theNFullSlices; i++) {
      getBytes(inOffset+i*kByteSliceSize,theByteSlice,0,kByteSliceSize);
      inDstByteStore.appendBytes(theByteSlice,0,kByteSliceSize);
    }
    if (theRemainder>0) {
      getBytes(inOffset+theNFullSlices*kByteSliceSize,theByteSlice,0,theRemainder);
      inDstByteStore.appendBytes(theByteSlice,0,theRemainder);
    }
    theSliceStore.putByteSlice(theByteSlice);
  }

  public void getByteStore(ByteStore inDstByteStore) { getByteStore(0,getSize(),inDstByteStore); }

//--------------------------------------------------------------------------------------------------------
// getByteRAMStore
//--------------------------------------------------------------------------------------------------------

  public ByteRAMStore getByteRAMStore(long inOffset, long inNBytes) {
    ByteRAMStore theByteRAMStore=new ByteRAMStore(inNBytes,getParams());
    getByteStore(inOffset,inNBytes,theByteRAMStore);
    return theByteRAMStore;
  }

  public ByteRAMStore getByteRAMStore() { return getByteRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(byte inByte) { return HashUtils.hash(inByte); }

  public long hash(byte[] inBytes, int inByteDelta, int inNBytes) { 
    return HashUtils.hash(inBytes,inByteDelta,inNBytes); }

  public long hash(byte[] inBytes) { return hash(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) { return hash(getByte(inIndex)); }

  public long getHash(long inIndex, int inNBytes) { 
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    getBytes(inIndex,theByteSlice,0,inNBytes);
    long theHash=hash(theByteSlice,0,inNBytes); 
    theSliceStore.putByteSlice(theByteSlice);
    return theHash;
  }

//--------------------------------------------------------------------------------------------------------
// setByte
//--------------------------------------------------------------------------------------------------------

  public abstract void setByte(long inOffset, byte inByte);

//--------------------------------------------------------------------------------------------------------
// setBoolean
//--------------------------------------------------------------------------------------------------------

  public void setBoolean(long inOffset, boolean inBoolean) {
    setByte(inOffset,(byte) (inBoolean?1:0)); }

//--------------------------------------------------------------------------------------------------------
// setVarLong
//--------------------------------------------------------------------------------------------------------

  public abstract void setVarLong(long inOffset, long inLong, int inLongSize);

//--------------------------------------------------------------------------------------------------------
// setVarDouble
//--------------------------------------------------------------------------------------------------------

  public abstract void setVarDouble(long inOffset, double inDouble, int inDoubleSize);

//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public abstract void setBytes(long inOffset, byte inByte, long inNCopies); 

  public abstract void setBytes(long inOffset, byte[] inBytes, int inByteDelta, int inNBytes); 

  public void setBytes(long inOffset, byte[] inBytes) {
    setBytes(inOffset,inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// setBooleans
//--------------------------------------------------------------------------------------------------------

  public void setBooleans(long inOffset, boolean inBoolean, long inNCopies) {
    setBytes(inOffset,(byte) (inBoolean?1:0),inNCopies); }

  public void setBooleans(long inOffset, boolean[] inBooleans, int inBooleanDelta, int inNBooleans) {
    byte[] theBytes=new byte[inNBooleans];
    int theDelta=inBooleanDelta;
    for (int i=0; i<inNBooleans; i++)
      theBytes[i]=(byte) (inBooleans[theDelta++]?1:0);
    setBytes(inOffset,theBytes);
  }

  public void setBooleans(long inOffset, boolean[] inBooleans) {
    setBooleans(inOffset,inBooleans,0,inBooleans.length); }

//--------------------------------------------------------------------------------------------------------
// setAllBytes
//--------------------------------------------------------------------------------------------------------

  public void setAllBytes(byte inByte) { setBytes(0,inByte,getSize()); }

//--------------------------------------------------------------------------------------------------------
// setAllBooleans
//--------------------------------------------------------------------------------------------------------

  public void setAllBooleans(boolean inBoolean) { setBooleans(0,inBoolean,getSize()); }

//--------------------------------------------------------------------------------------------------------
// setUTF8
//--------------------------------------------------------------------------------------------------------

  public int setUTF8(long inOffset, char inChar) {
    byte[] theBytes=UTF8Utils.charToUTF8Bytes(inChar);
    setBytes(inOffset,theBytes);
    return theBytes.length;
  }
  
//--------------------------------------------------------------------------------------------------------
// setUTF8
//--------------------------------------------------------------------------------------------------------

  public int setUTF8(long inOffset, String inString) {  
    int theNChars=inString.length();
    if (theNChars<=0) 
      return 0;
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      char[] theCharSlice=theSliceStore.getCharSlice();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theChunkSize=kByteSliceSize/3;
      int theNBytes;
      if (theNChars<theChunkSize) {
        inString.getChars(0,theNChars,theCharSlice,0);
        theNBytes=UTF8Utils.charsToUTF8Bytes(theCharSlice,0,theNChars,theByteSlice,0);
        setBytes(inOffset,theByteSlice,0,theNBytes);
      } else {
        int theSrcOffset=0;
        long theDstOffset=inOffset;
        long theLimit=theNChars-theChunkSize;
        while (theSrcOffset<=theLimit) {
          inString.getChars(theSrcOffset,theSrcOffset+theChunkSize,theCharSlice,0);
          theNBytes=UTF8Utils.charsToUTF8Bytes(theCharSlice,0,theChunkSize,theByteSlice,0);
          setBytes(theDstOffset,theByteSlice,0,theNBytes);
          theSrcOffset+=theChunkSize;
          theDstOffset+=theNBytes;
        }
        int theRemainder=theNChars-theSrcOffset;
        if (theRemainder>0) {
          inString.getChars(theSrcOffset,theSrcOffset+theRemainder,theCharSlice,0);
          theNBytes=UTF8Utils.charsToUTF8Bytes(theCharSlice,0,theRemainder,theByteSlice,0);
          setBytes(theDstOffset,theByteSlice,0,theNBytes);
          theDstOffset+=theNBytes;
        }
        theNBytes=(int) (theDstOffset-inOffset);
      }
      theSliceStore.putByteSlice(theByteSlice);
      theSliceStore.putCharSlice(theCharSlice);
      return theNBytes;
    }
  }

//--------------------------------------------------------------------------------------------------------
// setByteStore
//--------------------------------------------------------------------------------------------------------

  public void setByteStore(long inDstOffset, ByteStore inSrcByteStore, long inSrcOffset, long inNBytes) { 
    long theNFullSlices=SliceStore.getNFullByteSlices(inNBytes);
    int theRemainder=SliceStore.getByteRemainder(inNBytes);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    for (long i=0; i<theNFullSlices; i++) {
      inSrcByteStore.getBytes(inSrcOffset+i*kByteSliceSize,theByteSlice,0,kByteSliceSize);
      setBytes(inDstOffset+i*kByteSliceSize,theByteSlice,0,kByteSliceSize);
    }
    if (theRemainder>0) {
      inSrcByteStore.getBytes(inSrcOffset+theNFullSlices*kByteSliceSize,theByteSlice,0,theRemainder);
      setBytes(inDstOffset+theNFullSlices*kByteSliceSize,theByteSlice,0,theRemainder);
    }
    theSliceStore.putByteSlice(theByteSlice);
  }

  public void setByteStore(long inDstOffset, ByteStore inSrcByteStore) {
    setByteStore(inDstOffset,inSrcByteStore,0,inSrcByteStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// addToByte
//--------------------------------------------------------------------------------------------------------

  public void addToByte(long inOffset, byte inValue) {
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }
    if (inValue!=0)
      setByte(inOffset,(byte) (getByte(inOffset)+inValue)); 
  }

//--------------------------------------------------------------------------------------------------------
// addToBytes
//--------------------------------------------------------------------------------------------------------

  public void addToBytes(long inOffset, long inNBytes, byte inValue) { 
    if (kRangeChecking) {
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inOffset+inNBytes>getSize())
        throw new StoreException("Offset+NBytes past end: "+inOffset+"+"+inNBytes+">"+getSize());
    } 
    if ((inNBytes>0)&&(inValue!=0)) {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      if (inNBytes<kByteSliceSize) {
        int theNBytes=(int) inNBytes;
        getBytes(inOffset,theByteSlice,0,theNBytes);
        for (int j=0; j<inNBytes; j++) 
          theByteSlice[j]=(byte) (theByteSlice[j]+inValue);
        setBytes(inOffset,theByteSlice,0,theNBytes);
      } else {
        long theOffset=inOffset;
        long theLimit=inOffset+inNBytes-kByteSliceSize;
        while (theOffset<=theLimit) {
          getBytes(theOffset,theByteSlice);
          for (int j=0; j<kByteSliceSize; j++) 
            theByteSlice[j]=(byte) (theByteSlice[j]+inValue);
          setBytes(theOffset,theByteSlice);
          theOffset+=kByteSliceSize;
        }
        int theRemainder=(int) (inOffset+inNBytes-theOffset);
        if (theRemainder>0) {
          getBytes(theOffset,theByteSlice,0,theRemainder);
          for (int j=0; j<theRemainder; j++) 
            theByteSlice[j]=(byte) (theByteSlice[j]+inValue);
          setBytes(theOffset,theByteSlice,0,theRemainder);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// addToAllBytes
//--------------------------------------------------------------------------------------------------------

  public void addToAllBytes(byte inValue) { addToBytes(0,getSize(),inValue); }

//--------------------------------------------------------------------------------------------------------
// appendByte
//--------------------------------------------------------------------------------------------------------

  public void appendByte(byte inByte) {
    long theSize=getSize();
    setSize(theSize+1);
    setByte(theSize,inByte);
  }

//--------------------------------------------------------------------------------------------------------
// appendBoolean
//--------------------------------------------------------------------------------------------------------

  public void appendBoolean(boolean inBoolean) {
    long theSize=getSize();
    setSize(theSize+1);
    setBoolean(theSize,inBoolean);
  }

//--------------------------------------------------------------------------------------------------------
// appendBytes
//--------------------------------------------------------------------------------------------------------

  public void appendBytes(byte inByte, long inNCopies) {  
    if (kRangeChecking) 
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
    if (inNCopies>0) {
      long theSize=getSize();
      setSize(theSize+inNCopies);
      setBytes(theSize,inByte,inNCopies);
    }
  }

  public void appendBytes(byte[] inBytes, int inByteDelta, int inNBytes) { 
    if (inNBytes>0) {
      long theSize=getSize();
      setSize(theSize+inNBytes);
      setBytes(theSize,inBytes,inByteDelta,inNBytes);
    }
  }

  public void appendBytes(byte[] inBytes) { appendBytes(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// appendBooleans
//--------------------------------------------------------------------------------------------------------

  public void appendBooleans(boolean inBoolean, long inNCopies) {  
    if (kRangeChecking) 
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
    if (inNCopies>0) {
      long theSize=getSize();
      setSize(theSize+inNCopies);
      setBooleans(theSize,inBoolean,inNCopies);
    }
  }

  public void appendBooleans(boolean[] inBooleans, int inBooleanDelta, int inNBooleans) { 
    if (inNBooleans>0) {
      long theSize=getSize();
      setSize(theSize+inNBooleans);
      setBooleans(theSize,inBooleans,inBooleanDelta,inNBooleans);
    }
  }

  public void appendBooleans(boolean[] inBooleans) { appendBooleans(inBooleans,0,inBooleans.length); }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public int appendUTF8(char inChar) {
    byte[] theBytes=UTF8Utils.charToUTF8Bytes(inChar);
    appendBytes(theBytes);
    return theBytes.length;
  }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public int appendUTF8(char[] inChars, int inCharDelta, int inNChars) { 
    if (inNChars<=0) 
      return 0;
    else {
      ensureCapacity(getSize()+inNChars);
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theChunkSize=kByteSliceSize/3;
      long theSize=getSize();
      if (inNChars<theChunkSize) {
        int theNBytes=UTF8Utils.charsToUTF8Bytes(inChars,inCharDelta,inNChars,theByteSlice,0);
        appendBytes(theByteSlice,0,theNBytes);
      } else {
        int theOffset=inCharDelta;
        long theLimit=inCharDelta+inNChars-theChunkSize;
        while (theOffset<=theLimit) {
          int theNBytes=UTF8Utils.charsToUTF8Bytes(inChars,theOffset,theChunkSize,theByteSlice,0);
          appendBytes(theByteSlice,0,theNBytes);
          theOffset+=theChunkSize;
        }
        int theRemainder=inCharDelta+inNChars-theOffset;
        if (theRemainder>0) {
          int theNBytes=UTF8Utils.charsToUTF8Bytes(inChars,theOffset,theRemainder,theByteSlice,0);
          appendBytes(theByteSlice,0,theNBytes);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
      return (int) (getSize()-theSize);
    }
  }

  public int appendUTF8(char[] inChars) { return appendUTF8(inChars,0,inChars.length); }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public int appendUTF8(String inString) { 
    int theNChars=inString.length();
    if (theNChars<=0) 
      return 0;
    else {
      ensureCapacity(getSize()+theNChars);
      SliceStore theSliceStore=SliceStore.getSliceStore();
      char[] theCharSlice=theSliceStore.getCharSlice();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theChunkSize=kByteSliceSize/3;
      long theSize=getSize();
      if (theNChars<theChunkSize) {
        inString.getChars(0,theNChars,theCharSlice,0);
        int theNBytes=UTF8Utils.charsToUTF8Bytes(theCharSlice,0,theNChars,theByteSlice,0);
        appendBytes(theByteSlice,0,theNBytes);
      } else {
        int theOffset=0;
        long theLimit=theNChars-theChunkSize;
        while (theOffset<=theLimit) {
          inString.getChars(theOffset,theOffset+theChunkSize,theCharSlice,0);
          int theNBytes=UTF8Utils.charsToUTF8Bytes(theCharSlice,0,theChunkSize,theByteSlice,0);
          appendBytes(theByteSlice,0,theNBytes);
          theOffset+=theChunkSize;
        }
        int theRemainder=theNChars-theOffset;
        if (theRemainder>0) {
          inString.getChars(theOffset,theOffset+theRemainder,theCharSlice,0);
          int theNBytes=UTF8Utils.charsToUTF8Bytes(theCharSlice,0,theRemainder,theByteSlice,0);
          appendBytes(theByteSlice,0,theNBytes);
        }
      }
      theSliceStore.putByteSlice(theByteSlice);
      theSliceStore.putCharSlice(theCharSlice);
      return (int) (getSize()-theSize);
    }
  }

//--------------------------------------------------------------------------------------------------------
// appendByteStore
//--------------------------------------------------------------------------------------------------------

  public void appendByteStore(ByteStore inSrcByteStore, long inSrcOffset, long inNBytes) {  
    ensureCapacity(getSize()+inNBytes);
    long theNFullSlices=SliceStore.getNFullByteSlices(inNBytes);
    int theRemainder=SliceStore.getByteRemainder(inNBytes);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    for (long i=0; i<theNFullSlices; i++) {
      inSrcByteStore.getBytes(inSrcOffset+i*kByteSliceSize,theByteSlice,0,kByteSliceSize);
      appendBytes(theByteSlice,0,kByteSliceSize);
    }
    if (theRemainder>0) {
      inSrcByteStore.getBytes(inSrcOffset+theNFullSlices*kByteSliceSize,theByteSlice,0,theRemainder);
      appendBytes(theByteSlice,0,theRemainder);
    }
    theSliceStore.putByteSlice(theByteSlice);
  }

  public void appendByteStore(ByteStore inSrcByteStore) {
    appendByteStore(inSrcByteStore,0,inSrcByteStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(boolean inDescending) { return SortMapUtils.sortMap(this,inDescending); }
  
  public VarRAMStore getSortMap() { return getSortMap(false); }

}

