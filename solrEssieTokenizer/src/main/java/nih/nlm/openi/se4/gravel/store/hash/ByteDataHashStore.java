//--------------------------------------------------------------------------------------------------------
// ByteDataHashStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.sort.*;
import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// ByteDataHashStore
//--------------------------------------------------------------------------------------------------------

public abstract class ByteDataHashStore extends DataHashStore {

//--------------------------------------------------------------------------------------------------------
// ByteDataHashStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDataHashStore(DataStore inKeyStore, VarStore inHashStore, VarStore inLookupStore, 
      VarStore inPtrStore) {
    super(inKeyStore,inHashStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDataStore getKeyStore() { return (ByteDataStore) super.getKeyStore(); }
  public ByteDataStore dangerousGetKeyStore() { return (ByteDataStore) super.dangerousGetKeyStore(); }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public ByteDataStore unwrap(boolean inClosePlumbing) { 
    return (ByteDataStore) super.unwrap(inClosePlumbing); }

  public ByteDataStore unwrap() { return (ByteDataStore) super.unwrap(); }
  
//--------------------------------------------------------------------------------------------------------
// getNBytes
//--------------------------------------------------------------------------------------------------------

  public long getNBytes(long inIndex) { return getKeyStore().getNBytes(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getMaxNBytes
//--------------------------------------------------------------------------------------------------------

  public long getMaxNBytes() { return getKeyStore().getMaxNBytes(); }

//--------------------------------------------------------------------------------------------------------
// getByteAtN
//--------------------------------------------------------------------------------------------------------

  public byte getByteAtN(long inIndex, long inN) { 
    return getKeyStore().getByteAtN(inIndex,inN); }

//--------------------------------------------------------------------------------------------------------
// getBytesAtN
//--------------------------------------------------------------------------------------------------------

  public void getBytesAtN(long inIndex, long inN, byte[] ioBytes, int inByteDelta, int inNBytes) { 
    getKeyStore().getBytesAtN(inIndex,inN,ioBytes,inByteDelta,inNBytes); }

//--------------------------------------------------------------------------------------------------------
// getBytesAtN
//--------------------------------------------------------------------------------------------------------

  public byte[] getBytesAtN(long inIndex, long inN, int inNBytes) { 
    return getKeyStore().getBytesAtN(inIndex,inN,inNBytes); }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public int getBytes(long inIndex, byte[] ioBytes, int inByteDelta) { 
    return getKeyStore().getBytes(inIndex,ioBytes,inByteDelta); }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public int getBytes(long inIndex, byte[] ioBytes) { 
    return getKeyStore().getBytes(inIndex,ioBytes); }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public byte[] getBytes(long inIndex) { 
    return getKeyStore().getBytes(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getAllBytess
//--------------------------------------------------------------------------------------------------------

  public byte[][] getAllBytess() { 
    return getKeyStore().getAllBytess(); }

//--------------------------------------------------------------------------------------------------------
// getUTF8AtN
//--------------------------------------------------------------------------------------------------------

  public String getUTF8AtN(long inIndex, long inN, int inNBytes) { 
    return getKeyStore().getUTF8AtN(inIndex,inN,inNBytes); }

//--------------------------------------------------------------------------------------------------------
// getNUTF8Chars
//--------------------------------------------------------------------------------------------------------

  public int getNUTF8Chars(long inIndex) { 
    return getKeyStore().getNUTF8Chars(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getUTF8
//--------------------------------------------------------------------------------------------------------

  public String getUTF8(long inIndex) { 
    return getKeyStore().getUTF8(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getUTF8s
//--------------------------------------------------------------------------------------------------------

  public void getUTF8s(long inIndex, String[] ioUTF8s, int inUTF8Delta, int inNUTF8s) { 
    getKeyStore().getUTF8s(inIndex,ioUTF8s,inUTF8Delta,inNUTF8s); }

//--------------------------------------------------------------------------------------------------------
// getUTF8s
//--------------------------------------------------------------------------------------------------------

  public String[] getUTF8s(long inIndex, int inNUTF8s) { 
    return getKeyStore().getUTF8s(inIndex,inNUTF8s); }

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
    return getKeyStore().getAllUTF8s(ioUTF8s,inUTF8Delta); }

//--------------------------------------------------------------------------------------------------------
// getAllUTF8s
//--------------------------------------------------------------------------------------------------------

  public String[] getAllUTF8s() { 
    return getKeyStore().getAllUTF8s(); }

//--------------------------------------------------------------------------------------------------------
// getByteDataRAMStore
//--------------------------------------------------------------------------------------------------------

  public ByteDataRAMStore getByteDataRAMStore(long inIndex, long inNDatas) {
    return getKeyStore().getByteDataRAMStore(inIndex,inNDatas); }

  public ByteDataRAMStore getByteDataRAMStore() { return getByteDataRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// getByteDataHashRAMStore
//--------------------------------------------------------------------------------------------------------

  public ByteDataHashRAMStore getByteDataHashRAMStore() {
    return ByteDataHashRAMStore.wrap(getKeyStore().getByteDataRAMStore(),getHashStore().getVarRAMStore(),
        getLookupStore().getVarRAMStore(),getPtrStore().getVarRAMStore()); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(byte[] inBytes, int inByteDelta, int inNBytes) { 
    return HashUtils.hash(inBytes,inByteDelta,inNBytes); }

  public long hash(byte[] inBytes) { return hash(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// rebuild
//--------------------------------------------------------------------------------------------------------

  public void rebuild() { 
    // Used when hashes have changed because underlying data has changed
    if (getKeepHashes()) {
      VarStore theHashStore=getHashStore();
      theHashStore.clear();
      long theSize=getSize();
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      for (long i=0; i<theSize; i++) {
        int theNBytes=(int) getNBytes(i);
        if (theNBytes>theByteSlice.length) {
          theSliceStore.putByteSlice(theByteSlice);
          theByteSlice=Allocate.newBytes(theNBytes*2+1);
        }
        getBytes(i,theByteSlice);
        theHashStore.appendVar(hash(theByteSlice,0,theNBytes));
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
    reindex();
  }

//--------------------------------------------------------------------------------------------------------
// getIndex
//
// If key in store, returns index, else returns kNotFound
//--------------------------------------------------------------------------------------------------------

  private long getIndex(byte[] inBytes, int inByteDelta, int inNBytes, long inHash, long inLookupIndex) {
    boolean theKeepHashes=getKeepHashes();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    long theIndex=getFirstIndex(inLookupIndex);
    while (theIndex!=kNotFound) {
      if ((theKeepHashes)&&(inHash!=getHash(theIndex)))
        theIndex=getNextIndex(theIndex);
      else {
        int theNBytes=(int) getNBytes(theIndex);
        if (theNBytes>theByteSlice.length) {
          theSliceStore.putByteSlice(theByteSlice);
          theByteSlice=Allocate.newBytes(theNBytes*2+1);
        }
        getBytes(theIndex,theByteSlice,0);
        if (Comparisons.compareBytes(theByteSlice,0,theNBytes,
            inBytes,inByteDelta,inNBytes,Comparisons.kBinary)!=0)
          theIndex=getNextIndex(theIndex);
        else {
          theSliceStore.putByteSlice(theByteSlice);
          return theIndex;
        }
      }
    }
    if (theByteSlice!=null)  
      theSliceStore.putByteSlice(theByteSlice);
    return kNotFound;
  }

  public long getIndex(byte[] inBytes, int inByteDelta, int inNBytes, long inHash) {
    return getIndex(inBytes,inByteDelta,inNBytes,inHash,hashToLookupIndex(inHash)); }

  public long getIndex(byte[] inBytes, int inByteDelta, int inNBytes) {
    return getIndex(inBytes,inByteDelta,inNBytes,hash(inBytes,inByteDelta,inNBytes)); }

  public long getIndex(byte[] inBytes, long inHash) { 
    return getIndex(inBytes,0,inBytes.length,inHash); }

  public long getIndex(byte[] inBytes) { 
    return getIndex(inBytes,hash(inBytes)); }

//--------------------------------------------------------------------------------------------------------
// getIndex
//
// If key in store, returns index, else returns kNotFound
//--------------------------------------------------------------------------------------------------------

  public long getIndex(char[] inChars, int inCharDelta, int inNChars) {
    if (inNChars*2.4+10>SliceStore.kByteSliceSize)
      return getIndex(UTF8Utils.charsToUTF8Bytes(inChars,inCharDelta,inNChars));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theNBytes=UTF8Utils.charsToUTF8Bytes(inChars,inCharDelta,inNChars,theByteSlice,0);
      long theIndex=getIndex(theByteSlice,0,theNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theIndex;
    }
  }

  public long getIndex(char[] inChars) { return getIndex(inChars,0,inChars.length); }

//--------------------------------------------------------------------------------------------------------
// getIndex
//
// If key in store, returns index, else returns kNotFound
//--------------------------------------------------------------------------------------------------------

  public long getIndex(String inString) {
    int theLength=inString.length();
    if (theLength*2.4+10>SliceStore.kByteSliceSize)
      return getIndex(UTF8Utils.stringToUTF8Bytes(inString));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theNBytes=UTF8Utils.stringToUTF8Bytes(inString,theByteSlice,0);
      long theIndex=getIndex(theByteSlice,0,theNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theIndex;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getIndexs
//--------------------------------------------------------------------------------------------------------

  public long[] getIndexs(String[] inStrings) {
    long[] theIndexes=Allocate.newLongs(inStrings.length);
    for (int i=0; i<inStrings.length; i++)
      theIndexes[i]=getIndex(inStrings[i]);
    return theIndexes; 
  }

//--------------------------------------------------------------------------------------------------------
// isKnown
//--------------------------------------------------------------------------------------------------------

  public boolean isKnown(byte[] inBytes, int inByteDelta, int inNBytes) {
    return (getIndex(inBytes,inByteDelta,inNBytes)!=kNotFound); }

  public boolean isKnown(byte[] inBytes) { return (getIndex(inBytes)!=kNotFound); }

//--------------------------------------------------------------------------------------------------------
// isKnown
//--------------------------------------------------------------------------------------------------------

  public boolean isKnown(char[] inChars, int inCharDelta, int inNChars) {
    return (getIndex(inChars,inCharDelta,inNChars)!=kNotFound); }

  public boolean isKnown(char[] inChars) { return (getIndex(inChars)!=kNotFound); }

//--------------------------------------------------------------------------------------------------------
// isKnown
//--------------------------------------------------------------------------------------------------------

  public boolean isKnown(String inString) { return (getIndex(inString)!=kNotFound); }

//--------------------------------------------------------------------------------------------------------
// dangerousQuickAppendBytes
//
// Assumes the key does not exist in store
//--------------------------------------------------------------------------------------------------------

  private void dangerousQuickAppendBytes(byte[] inBytes, int inByteDelta, int inNBytes, 
      long inHash, long inLookupIndex) {
    getKeyStore().appendBytes(inBytes,inByteDelta,inNBytes);
    appendPtr(inHash,inLookupIndex);
  }

  public void dangerousQuickAppendBytes(byte[] inBytes, int inByteDelta, int inNBytes, long inHash) {
    dangerousQuickAppendBytes(inBytes,inByteDelta,inNBytes,inHash,hashToLookupIndex(inHash)); }

  public void dangerousQuickAppendBytes(byte[] inBytes, long inHash) {
    dangerousQuickAppendBytes(inBytes,0,inBytes.length,inHash); }

//--------------------------------------------------------------------------------------------------------
// appendBytes
//
// If key in store, index returned, else key is appended and kNotFound returned
// In either case, upon return, key is in store.
//--------------------------------------------------------------------------------------------------------

  public long appendBytes(byte[] inBytes, int inByteDelta, int inNBytes, long inHash) {
    long theLookupIndex=hashToLookupIndex(inHash);
    long theIndex=getIndex(inBytes,inByteDelta,inNBytes,inHash,theLookupIndex);
    if (theIndex==kNotFound) 
      dangerousQuickAppendBytes(inBytes,inByteDelta,inNBytes,inHash,theLookupIndex);
    return theIndex;
  }

  public long appendBytes(byte[] inBytes, int inByteDelta, int inNBytes) {
    return appendBytes(inBytes,inByteDelta,inNBytes,hash(inBytes,inByteDelta,inNBytes)); }

  public long appendBytes(byte[] inBytes, long inHash) { 
    return appendBytes(inBytes,0,inBytes.length,inHash); }

  public long appendBytes(byte[] inBytes) { return appendBytes(inBytes,hash(inBytes)); }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public long appendUTF8(char[] inChars, int inCharDelta, int inNChars) {
    if (inNChars*2.4+10>SliceStore.kByteSliceSize)
      return appendBytes(UTF8Utils.charsToUTF8Bytes(inChars,inCharDelta,inNChars));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theNBytes=UTF8Utils.charsToUTF8Bytes(inChars,inCharDelta,inNChars,theByteSlice,0);
      long theIndex=appendBytes(theByteSlice,0,theNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theIndex;
    } 
  }

  public long appendUTF8(char[] inChars) { return appendUTF8(inChars,0,inChars.length); }

//--------------------------------------------------------------------------------------------------------
// appendUTF8
//--------------------------------------------------------------------------------------------------------

  public long appendUTF8(String inUTF8) {
    int theLength=inUTF8.length();
    if (theLength*2.4+10>SliceStore.kByteSliceSize)
      return appendBytes(UTF8Utils.stringToUTF8Bytes(inUTF8));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theNBytes=UTF8Utils.stringToUTF8Bytes(inUTF8,theByteSlice,0);
      long theIndex=appendBytes(theByteSlice,0,theNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theIndex;
    } 
  }

//--------------------------------------------------------------------------------------------------------
// appendUTF8s
//--------------------------------------------------------------------------------------------------------

  public void appendUTF8s(String[] inUTF8s) {
    for (int i=0; i<inUTF8s.length; i++)
      appendUTF8(inUTF8s[i]);
  }

//--------------------------------------------------------------------------------------------------------
// appendByteStore
//--------------------------------------------------------------------------------------------------------

  public long appendByteStore(ByteStore inSrcByteStore, long inSrcOffset, long inNBytes) {
    if (inNBytes*2.4+20>SliceStore.kByteSliceSize)
      return appendBytes(inSrcByteStore.getBytes(inSrcOffset,(int) inNBytes));
    else {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      inSrcByteStore.getBytes(inSrcOffset,theByteSlice,0,(int) inNBytes);
      long theIndex=appendBytes(theByteSlice,0,(int) inNBytes);
      theSliceStore.putByteSlice(theByteSlice);
      return theIndex;
    } 
  }

  public long appendByteStore(ByteStore inSrcByteStore) {
    return appendByteStore(inSrcByteStore,0,inSrcByteStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// appendByteDataStore
//--------------------------------------------------------------------------------------------------------

  public void appendByteDataStore(ByteDataStore inSrcByteDataStore, long inSrcOffset, long inNDatas) {
    SliceStore theSliceStore=SliceStore.getSliceStore();
    byte[] theByteSlice=theSliceStore.getByteSlice();
    for (long i=inSrcOffset; i<inSrcOffset+inNDatas; i++) {
      long theNBytes=inSrcByteDataStore.getNBytes(i);
      if (theNBytes>theByteSlice.length) {
        theSliceStore.putByteSlice(theByteSlice);
        theByteSlice=Allocate.newBytes(theNBytes*2+1);
      }
      inSrcByteDataStore.getBytes(i,theByteSlice,0);
      appendBytes(theByteSlice,0,(int)theNBytes);
    }
    theSliceStore.putByteSlice(theByteSlice);
  }
  
  public void appendByteDataStore(ByteDataStore inSrcByteDataStore) {
    appendByteDataStore(inSrcByteDataStore,0,inSrcByteDataStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// appendByteDataHashStore
//--------------------------------------------------------------------------------------------------------

  public void appendByteDataHashStore(ByteDataHashStore inSrcByteDataHashStore, 
      long inSrcOffset, long inNDatas) {
    appendByteDataStore(inSrcByteDataHashStore.getKeyStore(),inSrcOffset,inNDatas); }
  
  public void appendByteDataHashStore(ByteDataHashStore inSrcByteDataHashStore) {
    appendByteDataStore(inSrcByteDataHashStore.getKeyStore()); }

//--------------------------------------------------------------------------------------------------------
// keepBytes
//
// Keep routines are very similar to appends, but always return the index of the key
// Appends return kNotFound if the key was not in the store, and therefore was appended to end
//--------------------------------------------------------------------------------------------------------

  public long keepBytes(byte[] inBytes, int inByteDelta, int inNBytes, long inHash) {
    long theIndex=appendBytes(inBytes,inByteDelta,inNBytes,inHash);
    if (theIndex==kNotFound) 
      theIndex=getSize()-1;
    return theIndex;
  }

  public long keepBytes(byte[] inBytes, int inByteDelta, int inNBytes) {
    return keepBytes(inBytes,inByteDelta,inNBytes,hash(inBytes,inByteDelta,inNBytes)); }

  public long keepBytes(byte[] inBytes, long inHash) { 
    return keepBytes(inBytes,0,inBytes.length,inHash); }

  public long keepBytes(byte[] inBytes) { return keepBytes(inBytes,hash(inBytes)); }

//--------------------------------------------------------------------------------------------------------
// keepUTF8
//--------------------------------------------------------------------------------------------------------

  public long keepUTF8(char[] inChars, int inCharDelta, int inNChars) {
    long theIndex=appendUTF8(inChars,inCharDelta,inNChars);
    if (theIndex==kNotFound) 
      theIndex=getSize()-1;
    return theIndex;
  }

  public long keepUTF8(char[] inChars) { return keepUTF8(inChars,0,inChars.length); }

//--------------------------------------------------------------------------------------------------------
// keepUTF8
//--------------------------------------------------------------------------------------------------------

  public long keepUTF8(String inUTF8) {
    long theIndex=appendUTF8(inUTF8);
    if (theIndex==kNotFound) 
      theIndex=getSize()-1;
    return theIndex;
  }

//--------------------------------------------------------------------------------------------------------
// keepUTF8s
//--------------------------------------------------------------------------------------------------------

  public void keepUTF8s(String[] inUTF8s) { appendUTF8s(inUTF8s); }

//--------------------------------------------------------------------------------------------------------
// keepByteStore
//--------------------------------------------------------------------------------------------------------

  public long keepByteStore(ByteStore inSrcByteStore, long inSrcOffset, long inNBytes) {
    long theIndex=appendByteStore(inSrcByteStore,inSrcOffset,inNBytes);
    if (theIndex==kNotFound) 
      theIndex=getSize()-1;
    return theIndex;
  }

  public long keepByteStore(ByteStore inSrcByteStore) {
    return keepByteStore(inSrcByteStore,0,inSrcByteStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// keepByteDataStore
//--------------------------------------------------------------------------------------------------------

  public void keepByteDataStore(ByteDataStore inSrcByteDataStore, long inSrcOffset, long inNDatas) {
    appendByteDataStore(inSrcByteDataStore,inSrcOffset,inNDatas);
  }
  
  public void keepByteDataStore(ByteDataStore inSrcByteDataStore) {
    keepByteDataStore(inSrcByteDataStore,0,inSrcByteDataStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// keepByteDataHashStore
//--------------------------------------------------------------------------------------------------------

  public void keepByteDataHashStore(ByteDataHashStore inSrcByteDataHashStore, 
      long inSrcOffset, long inNDatas) {
    keepByteDataStore(inSrcByteDataHashStore.getKeyStore(),inSrcOffset,inNDatas); }
  
  public void keepByteDataHashStore(ByteDataHashStore inSrcByteDataHashStore) {
    keepByteDataStore(inSrcByteDataHashStore.getKeyStore()); }
  
//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(byte inHandleCase, boolean inDescending) {
    return getKeyStore().getSortMap(inHandleCase,inDescending); }

  public VarRAMStore getSortMap(byte inHandleCase) { 
    return getSortMap(inHandleCase,false); }

  public VarRAMStore getSortMap(boolean inDescending) { 
    return getSortMap(Comparisons.kCaseBreaksTies,inDescending); }

  public VarRAMStore getSortMap() { 
    return getSortMap(false); }

//--------------------------------------------------------------------------------------------------------
// drawKey
//--------------------------------------------------------------------------------------------------------

  public String drawKey(long inIndex) { return getUTF8(inIndex); }

}

