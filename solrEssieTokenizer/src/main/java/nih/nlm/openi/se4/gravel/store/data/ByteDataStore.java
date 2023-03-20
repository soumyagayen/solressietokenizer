//--------------------------------------------------------------------------------------------------------
// ByteDataStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.sort.*;
import gravel.store.hash.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// ByteDataStore
//--------------------------------------------------------------------------------------------------------

public abstract class ByteDataStore extends DataStore { 

//--------------------------------------------------------------------------------------------------------
// ByteDataStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDataStore(ByteStore inByteStore, VarStore inOffsetStore) {
    super(inByteStore,inOffsetStore); }
  
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

  public ByteStore unwrap(boolean inCloseOffsets) { return (ByteStore) super.unwrap(inCloseOffsets); }
  
  public ByteStore unwrap() { return (ByteStore) super.unwrap(); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(byte[] inBytes, int inByteDelta, int inNBytes) { 
    return HashUtils.hash(inBytes,inByteDelta,inNBytes); }

  public long hash(byte[] inBytes) { return hash(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) { 
    long theOffset=getOffset(inIndex);
    int theNBytes=(int) (getOffset(inIndex+1)-theOffset);
    return getByteStore().getHash(theOffset,theNBytes);
  }

//--------------------------------------------------------------------------------------------------------
// getNBytes
//--------------------------------------------------------------------------------------------------------

  public long getNBytes(long inIndex) { 
    long theNBytes=getOffset(inIndex+1)-getOffset(inIndex); 
    if ((kRangeChecking)&&(theNBytes<0))
      throw new StoreException("Negative NBytes: "+theNBytes);
    return theNBytes;
  }

//--------------------------------------------------------------------------------------------------------
// getMaxNBytes
//--------------------------------------------------------------------------------------------------------

  public long getMaxNBytes() {
    long theMaxNBytes=0;
    long theSize=getSize();
    long theLastOffset=getOffset(0);
    for (long i=1; i<=theSize; i++) {
      long theOffset=getOffset(i);
      theMaxNBytes=Math.max(theMaxNBytes,theOffset-theLastOffset);
      theLastOffset=theOffset;
    }
    return theMaxNBytes;
  }

//--------------------------------------------------------------------------------------------------------
// getByteAtN
//--------------------------------------------------------------------------------------------------------

  public byte getByteAtN(long inIndex, long inN) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN>=getNBytes(inIndex))
        throw new StoreException("N past end of row: "+inN+" >= "+getNBytes(inIndex));
    }
    return getByteStore().getByte(getOffset(inIndex)+inN); 
  }

//--------------------------------------------------------------------------------------------------------
// getBytesAtN
//--------------------------------------------------------------------------------------------------------

  public void getBytesAtN(long inIndex, long inN, byte[] ioBytes, int inByteDelta, int inNBytes) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inByteDelta<0)
        throw new StoreException("Negative ByteDelta: "+inByteDelta);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN+inNBytes>getNBytes(inIndex))
        throw new StoreException("N+NBytes past end of row: "+inN+"+"+inNBytes+" > "+getNBytes(inIndex));
      if (inByteDelta+inNBytes>ioBytes.length)
        throw new StoreException("ByteDelta+NBytes past end: "+inByteDelta+"+"+inNBytes+">"+ioBytes.length);
    }
    getByteStore().getBytes(getOffset(inIndex)+inN,ioBytes,inByteDelta,inNBytes); 
  }

//--------------------------------------------------------------------------------------------------------
// getBytesAtN
//--------------------------------------------------------------------------------------------------------

  public byte[] getBytesAtN(long inIndex, long inN, int inNBytes) {
    if (inNBytes==0)
      return kNoBytes;
    else {
      byte[] theBytes=Allocate.newBytes(inNBytes);
      getBytesAtN(inIndex,inN,theBytes,0,inNBytes); 
      return theBytes;
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public int getBytes(long inIndex, byte[] ioBytes, int inByteDelta) {
    long theNBytes=getNBytes(inIndex);
    if (theNBytes>0)
      getBytesAtN(inIndex,0,ioBytes,inByteDelta,(int) theNBytes); 
    return (int) theNBytes;
  }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public int getBytes(long inIndex, byte[] ioBytes) {
    return getBytes(inIndex,ioBytes,0); }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public byte[] getBytes(long inIndex) {
    long theNBytes=getNBytes(inIndex);
    if (theNBytes>k1G)
      throw new StoreException("Row too big");
    if (theNBytes==0)
      return kNoBytes;
    else {
      byte[] theBytes=Allocate.newBytes(theNBytes);
      getBytes(inIndex,theBytes); 
      return theBytes;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getAllBytess
//--------------------------------------------------------------------------------------------------------

  public byte[][] getAllBytess() {
    long theSize=getSize();
    if (theSize>k1G/kLongMemory)
      throw new StoreException("Store too big");
    byte[][] theBytess=new byte[(int) theSize][];
    for (int i=0; i<theBytess.length; i++)
      theBytess[i]=getBytes(i); 
    return theBytess;
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8AtN
//--------------------------------------------------------------------------------------------------------

  public String getUTF8AtN(long inIndex, long inN, int inNBytes) {
    if (inNBytes>SliceStore.kByteSliceSize)
      return UTF8Utils.bytesToUTF8String(getBytesAtN(inIndex,inN,inNBytes));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      getBytesAtN(inIndex,inN,theByteSlice,0,inNBytes);
      String theUTF8=UTF8Utils.bytesToUTF8String(theByteSlice,0,inNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theUTF8;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getNUTF8Chars
//--------------------------------------------------------------------------------------------------------

  public int getNUTF8Chars(long inIndex) {
    long theNBytes=getNBytes(inIndex);
    if (theNBytes>SliceStore.kByteSliceSize)
      return UTF8Utils.countUTF8Chars(getBytes(inIndex));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      getBytes(inIndex,theByteSlice,0);
      int theNUTF8Chars=UTF8Utils.countUTF8Chars(theByteSlice,0,(int) theNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theNUTF8Chars;
    }  
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8
//--------------------------------------------------------------------------------------------------------

  public String getUTF8(long inIndex) {
    long theNBytes=getNBytes(inIndex);
    if (theNBytes>SliceStore.kByteSliceSize)
      return UTF8Utils.bytesToUTF8String(getBytes(inIndex));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      getBytes(inIndex,theByteSlice,0);
      String theUTF8=UTF8Utils.bytesToUTF8String(theByteSlice,0,(int) theNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theUTF8;
    }  
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8s
//--------------------------------------------------------------------------------------------------------

  public void getUTF8s(long inIndex, String[] ioUTF8s, int inUTF8Delta, int inNUTF8s) {
    for (int i=0; i<inNUTF8s; i++)
      ioUTF8s[inUTF8Delta+i]=getUTF8(inIndex+i);
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8s
//--------------------------------------------------------------------------------------------------------

  public String[] getUTF8s(long inIndex, int inNUTF8s) {
    String[] theUTF8s=new String[inNUTF8s];
    getUTF8s(inIndex,theUTF8s,0,inNUTF8s);
    return theUTF8s;
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8s
//--------------------------------------------------------------------------------------------------------

  public String[] getUTF8s(long[] inIndexs) { 
    String[] theUTF8s=new String[inIndexs.length];
    for (int i=0; i<inIndexs.length; i++)
      theUTF8s[i]=getUTF8(inIndexs[i]);
    return theUTF8s;
  }

//--------------------------------------------------------------------------------------------------------
// getAllUTF8s
//--------------------------------------------------------------------------------------------------------

  public int getAllUTF8s(String[] ioUTF8s, int inUTF8Delta) { 
    long theSize=getSize();
    if (theSize>k1G/kLongMemory)
      throw new StoreException("Store too big");
    if (theSize>ioUTF8s.length-inUTF8Delta)
      throw new StoreException("Array too small");
    getUTF8s(0,ioUTF8s,inUTF8Delta,(int) theSize); 
    return (int) theSize;
  }

//--------------------------------------------------------------------------------------------------------
// getAllUTF8s
//--------------------------------------------------------------------------------------------------------

  public String[] getAllUTF8s() { return getUTF8s(0,(int) getSize()); }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  public void getByteStore(long inIndex, ByteStore inDstByteStore) {
    long theOffset=getOffset(inIndex);
    long theNBytes=getOffset(inIndex+1)-theOffset;
    getByteStore().getByteStore(theOffset,theNBytes,inDstByteStore);
  }

//--------------------------------------------------------------------------------------------------------
// getByteRAMStore
//--------------------------------------------------------------------------------------------------------

  public ByteRAMStore getByteRAMStore(long inIndex) {
    ByteRAMStore theByteRAMStore=new ByteRAMStore(getNBytes(inIndex));
    getByteStore(inIndex,theByteRAMStore);
    return theByteRAMStore;
  }

//--------------------------------------------------------------------------------------------------------
// getByteDataStore
//--------------------------------------------------------------------------------------------------------

  public void getByteDataStore(long inIndex, long inNDatas, ByteDataStore inDstByteDataStore) {
    long theOffset=getOffset(inIndex);
    long theDataSize=getOffset(inIndex+inNDatas)-theOffset;
    long theDstSize=inDstByteDataStore.getSize();
    long theDelta=inDstByteDataStore.getDataSize()-theOffset;
    getByteStore().getByteStore(theOffset,theDataSize,inDstByteDataStore.getByteStore());
    getOffsetStore().getVarStore(inIndex+1,inNDatas,inDstByteDataStore.getOffsetStore());
    inDstByteDataStore.getOffsetStore().addToVars(theDstSize+1,inNDatas,theDelta);
  }

  public void getByteDataStore(ByteDataStore inDstByteDataStore) {
    getByteDataStore(0,getSize(),inDstByteDataStore); }
    
//--------------------------------------------------------------------------------------------------------
// getByteDataRAMStore
//--------------------------------------------------------------------------------------------------------

  public ByteDataRAMStore getByteDataRAMStore(long inIndex, long inNDatas) {
    long theDataSize=getOffset(inIndex+inNDatas)-getOffset(inIndex);
    ByteDataRAMStore theByteDataRAMStore=new ByteDataRAMStore(inNDatas,theDataSize);
    getByteDataStore(inIndex,inNDatas,theByteDataRAMStore);
    return theByteDataRAMStore;
  }

  public ByteDataRAMStore getByteDataRAMStore() { return getByteDataRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// setByteAtN
//--------------------------------------------------------------------------------------------------------

  public void setByteAtN(long inIndex, long inN, byte inByte) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN>=getNBytes(inIndex))        
        throw new StoreException("N past end of row: "+inN+" >= "+getNBytes(inIndex));
    }
    getByteStore().setByte(getOffset(inIndex)+inN,inByte); 
  }

//--------------------------------------------------------------------------------------------------------
// setBytesAtN
//--------------------------------------------------------------------------------------------------------

  public void setBytesAtN(long inIndex, long inN, byte[] inBytes, int inByteDelta, int inNBytes) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inByteDelta<0)
        throw new StoreException("Negative ByteDelta: "+inByteDelta);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN+inNBytes>getNBytes(inIndex))
        throw new StoreException("N+NBytes past end of row: "+inN+"+"+inNBytes+" > "+getNBytes(inIndex));
      if (inByteDelta+inNBytes>inBytes.length)
        throw new StoreException("ByteDelta+NBytes past end: "+inByteDelta+"+"+inNBytes+">"+inBytes.length);
    }
    getByteStore().setBytes(getOffset(inIndex)+inN,inBytes,inByteDelta,inNBytes); 
  }

//--------------------------------------------------------------------------------------------------------
// setBytesAtN
//--------------------------------------------------------------------------------------------------------

  public void setBytesAtN(long inIndex, long inN, byte[] ioBytes) {
    setBytesAtN(inIndex,inN,ioBytes,0,ioBytes.length); }
  
//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public void setBytes(long inIndex, byte[] inBytes, int inByteDelta, int inNBytes) {
    setBytesAtN(inIndex,0,inBytes,inByteDelta,inNBytes); }

//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public void setBytes(long inIndex, byte[] inBytes) { 
    setBytes(inIndex,inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// setByteStore
//--------------------------------------------------------------------------------------------------------

  public void setByteStore(long inIndex, ByteStore inByteStore) {
    long theOffset=getOffset(inIndex);
    getByteStore().setByteStore(theOffset,inByteStore);
  }

//--------------------------------------------------------------------------------------------------------
// addToByteAtN
//--------------------------------------------------------------------------------------------------------

  public void addToByteAtN(long inIndex, long inN, byte inByte) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN>=getNBytes(inIndex))        
        throw new StoreException("N past end of row: "+inN+" >= "+getNBytes(inIndex));
    }
    getByteStore().addToByte(getOffset(inIndex)+inN,inByte); 
  }

//--------------------------------------------------------------------------------------------------------
// appendBytes
//--------------------------------------------------------------------------------------------------------

  public void appendBytes(byte inByte, long inNCopies) {
    getByteStore().appendBytes(inByte,inNCopies); 
    getOffsetStore().appendVar(getDataSize());
  }

//--------------------------------------------------------------------------------------------------------
// appendBytes
//--------------------------------------------------------------------------------------------------------

  public void appendBytes(byte[] inBytes, int inByteDelta, int inNBytes) {
    getByteStore().appendBytes(inBytes,inByteDelta,inNBytes); 
    getOffsetStore().appendVar(getDataSize());
  }

  public void appendBytes(byte[] inBytes) { appendBytes(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public void appendUTF8(char[] inChars, int inCharDelta, int inNChars) {
    getByteStore().appendUTF8(inChars,inCharDelta,inNChars); 
    getOffsetStore().appendVar(getDataSize());
  }

  public void appendUTF8(char[] inChars) { appendUTF8(inChars,0,inChars.length); }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public void appendUTF8(String inUTF8) {
    getByteStore().appendUTF8(inUTF8); 
    getOffsetStore().appendVar(getDataSize());
  }

//--------------------------------------------------------------------------------------------------------
// appendUTF8s
//--------------------------------------------------------------------------------------------------------

  public void appendUTF8s(String[] inUTF8s, int inUTF8Delta, int inNUTF8s) {
    for (int i=0; i<inNUTF8s; i++)
      appendUTF8(inUTF8s[inUTF8Delta+i]);
  }

  public void appendUTF8s(String[] inUTF8s) { appendUTF8s(inUTF8s,0,inUTF8s.length); }

//--------------------------------------------------------------------------------------------------------
// appendByteStore
//--------------------------------------------------------------------------------------------------------

  public void appendByteStore(ByteStore inSrcByteStore, long inSrcOffset, long inNBytes) {
    getByteStore().appendByteStore(inSrcByteStore,inSrcOffset,inNBytes);
    getOffsetStore().appendVar(getDataSize());
  }

  public void appendByteStore(ByteStore inSrcByteStore) {
    appendByteStore(inSrcByteStore,0,inSrcByteStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// appendByteDataStore
//--------------------------------------------------------------------------------------------------------

  public void appendByteDataStore(ByteDataStore inSrcByteDataStore, long inSrcOffset, long inNDatas) {
    if (inNDatas>0) {
      long theDstSize=getSize();
      long theDstDataSize=getDataSize();
      long theSrcByteOffset=inSrcByteDataStore.getOffset(inSrcOffset);
      long theSrcNBytes=inSrcByteDataStore.getOffset(inSrcOffset+inNDatas)-theSrcByteOffset;
      getByteStore().appendByteStore(inSrcByteDataStore.getByteStore(),theSrcByteOffset,theSrcNBytes);
      VarStore theOffsetStore=getOffsetStore();
      theOffsetStore.truncateBy(1);
      theOffsetStore.appendVarStore(inSrcByteDataStore.getOffsetStore(),inSrcOffset,inNDatas+1);
      long theDelta=theDstDataSize-theSrcByteOffset;
      theOffsetStore.addToVars(theDstSize,inNDatas+1,theDelta);
    }
  }
  
  public void appendByteDataStore(ByteDataStore inSrcByteDataStore) {
    appendByteDataStore(inSrcByteDataStore,0,inSrcByteDataStore.getSize()); }
   
//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(byte inHandleCase, boolean inDescending) { 
    return SortMapUtils.sortMap(this,inHandleCase,inDescending); }

  public VarRAMStore getSortMap(byte inHandleCase) { 
    return getSortMap(inHandleCase,false); }

  public VarRAMStore getSortMap(boolean inDescending) { 
    return getSortMap(Comparisons.kCaseBreaksTies,inDescending); }

  public VarRAMStore getSortMap() { 
    return getSortMap(false); }

}

