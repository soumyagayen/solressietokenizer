//--------------------------------------------------------------------------------------------------------
// VarDataHashStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.sort.*;
import gravel.store.data.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarDataHashStore
//--------------------------------------------------------------------------------------------------------

public abstract class VarDataHashStore extends DataHashStore {

//--------------------------------------------------------------------------------------------------------
// VarDataHashStore
//--------------------------------------------------------------------------------------------------------

  protected VarDataHashStore(VarDataStore inKeyStore, VarStore inHashStore, VarStore inLookupStore, 
      VarStore inPtrStore) {
    super(inKeyStore,inHashStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected VarDataStore getKeyStore() { return (VarDataStore) super.getKeyStore(); }
  public VarDataStore dangerousGetKeyStore() { return (VarDataStore) super.dangerousGetKeyStore(); }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public VarDataStore unwrap(boolean inClosePlumbing) { 
    return (VarDataStore) super.unwrap(inClosePlumbing); }

  public VarDataStore unwrap() { return (VarDataStore) super.unwrap(); }

//--------------------------------------------------------------------------------------------------------
// getVarSize
//--------------------------------------------------------------------------------------------------------

  public int getVarSize() { return getKeyStore().getVarSize(); }

//--------------------------------------------------------------------------------------------------------
// setVarSize
//--------------------------------------------------------------------------------------------------------

  public void setVarSize(int inVarSize) { 
    getKeyStore().setVarSize(inVarSize); 
    rebuild();
  }
  
//--------------------------------------------------------------------------------------------------------
// getNVars
//--------------------------------------------------------------------------------------------------------

  public long getNVars(long inIndex) { return getKeyStore().getNVars(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getMaxNVars
//--------------------------------------------------------------------------------------------------------

  public long getMaxNVars() { return getKeyStore().getMaxNVars(); }

//--------------------------------------------------------------------------------------------------------
// getVarAtN
//--------------------------------------------------------------------------------------------------------

  public long getLongAtN(long inIndex, long inN) { return getKeyStore().getLongAtN(inIndex,inN); }
  public long getIntAtN(long inIndex, long inN) { return getKeyStore().getIntAtN(inIndex,inN); }

//--------------------------------------------------------------------------------------------------------
// getVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void getVarsAtN(long inIndex, long inN, long[] ioLongs, int inLongDelta, int inNLongs) { 
    getKeyStore().getVarsAtN(inIndex,inN,ioLongs,inLongDelta,inNLongs); }

  public void getVarsAtN(long inIndex, long inN, int[] ioInts, int inIntDelta, int inNInts) { 
    getKeyStore().getVarsAtN(inIndex,inN,ioInts,inIntDelta,inNInts); }

//--------------------------------------------------------------------------------------------------------
// getVarsAtN
//--------------------------------------------------------------------------------------------------------

  public long[] getLongsAtN(long inIndex, long inN, int inNLongs) { 
    return getKeyStore().getLongsAtN(inIndex,inN,inNLongs); }

  public int[] getIntsAtN(long inIndex, long inN, int inNInts) { 
    return getKeyStore().getIntsAtN(inIndex,inN,inNInts); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public int getVars(long inIndex, long[] ioLongs, int inLongDelta) { 
    return getKeyStore().getVars(inIndex,ioLongs,inLongDelta); }

  public int getVars(long inIndex, int[] ioInts, int inIntDelta) { 
    return getKeyStore().getVars(inIndex,ioInts,inIntDelta); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public int getVars(long inIndex, long[] ioLongs) { return getKeyStore().getVars(inIndex,ioLongs); }
  public int getVars(long inIndex, int[] ioInts) { return getKeyStore().getVars(inIndex,ioInts); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public long[] getLongs(long inIndex) { return getKeyStore().getLongs(inIndex); }
  public int[] getInts(long inIndex) { return getKeyStore().getInts(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getAllVars
//--------------------------------------------------------------------------------------------------------

  public long[][] getAllLongss() { return getKeyStore().getAllLongss(); }
  public int[][] getAllIntss() { return getKeyStore().getAllIntss(); }

//--------------------------------------------------------------------------------------------------------
// getVarDataRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarDataRAMStore getVarDataRAMStore(long inIndex, long inNDatas) {
    return getKeyStore().getVarDataRAMStore(inIndex,inNDatas); }

  public VarDataRAMStore getVarDataRAMStore() { return getVarDataRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// getVarDataHashRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarDataHashRAMStore getVarDataHashRAMStore() {
    return VarDataHashRAMStore.wrap(
        getKeyStore().getVarDataRAMStore(),
        getHashStore().getVarRAMStore(),
        getLookupStore().getVarRAMStore(),
        getPtrStore().getVarRAMStore()); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(long[] inLongs, int inLongDelta, int inNLongs) { 
    return HashUtils.hash(inLongs,inLongDelta,inNLongs); }

  public long hash(long[] inLongs) { return hash(inLongs,0,inLongs.length); }

  public long hash(int[] inInts, int inIntDelta, int inNInts) { 
    return HashUtils.hash(inInts,inIntDelta,inNInts); }

  public long hash(int[] inInts) { return hash(inInts,0,inInts.length); }

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
      long[] theLongSlice=theSliceStore.getLongSlice();
      for (long i=0; i<theSize; i++) {
        int theNLongs=getVars(i,theLongSlice);
        theHashStore.appendVar(hash(theLongSlice,0,theNLongs));
      }
      theSliceStore.putLongSlice(theLongSlice);
    }
    reindex();
  }

//--------------------------------------------------------------------------------------------------------
// getIndex
//
// If key in store, returns index, else returns kNotFound
//--------------------------------------------------------------------------------------------------------

  private long getIndex(long[] inLongs, int inLongDelta, int inNLongs, long inHash, long inLookupIndex) {
    boolean theKeepHashes=getKeepHashes();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theLongSlice=theSliceStore.getLongSlice();
    long theIndex=getFirstIndex(inLookupIndex);
    while (theIndex!=kNotFound) {
      if ((theKeepHashes)&&(inHash!=getHash(theIndex)))
        theIndex=getNextIndex(theIndex);
      else {
        int theNLongs=getVars(theIndex,theLongSlice,0);
        if (Comparisons.compareLongs(theLongSlice,0,theNLongs,inLongs,inLongDelta,inNLongs)!=0)
          theIndex=getNextIndex(theIndex);
        else {
          theSliceStore.putLongSlice(theLongSlice);
          return theIndex;
        }
      }
    }
    theSliceStore.putLongSlice(theLongSlice);
    return kNotFound;
  }

  public long getIndex(long[] inLongs, int inLongDelta, int inNLongs, long inHash) {
    return getIndex(inLongs,inLongDelta,inNLongs,inHash,hashToLookupIndex(inHash)); }

  public long getIndex(long[] inLongs, int inLongDelta, int inNLongs) {
    return getIndex(inLongs,inLongDelta,inNLongs,hash(inLongs,inLongDelta,inNLongs)); }

  public long getIndex(long[] inLongs, long inHash) { 
    return getIndex(inLongs,0,inLongs.length,inHash); }

  public long getIndex(long[] inLongs) { 
    return getIndex(inLongs,hash(inLongs)); }

  private long getIndex(int[] inInts, int inIntDelta, int inNInts, long inHash, long inLookupIndex) {
    boolean theKeepHashes=getKeepHashes();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    int[] theIntSlice=theSliceStore.getIntSlice();
    long theIndex=getFirstIndex(inLookupIndex);
    while (theIndex!=kNotFound) {
      if ((theKeepHashes)&&(inHash!=getHash(theIndex)))
        theIndex=getNextIndex(theIndex);
      else {
        int theNInts=getVars(theIndex,theIntSlice,0);
        if (Comparisons.compareInts(theIntSlice,0,theNInts,inInts,inIntDelta,inNInts)!=0)
          theIndex=getNextIndex(theIndex);
        else {
          theSliceStore.putIntSlice(theIntSlice);
          return theIndex;
        }
      }
    }
    if (theIntSlice!=null)
      theSliceStore.putIntSlice(theIntSlice);
    return kNotFound;
  }

  public long getIndex(int[] inInts, int inIntDelta, int inNInts, long inHash) {
    return getIndex(inInts,inIntDelta,inNInts,inHash,hashToLookupIndex(inHash)); }

  public long getIndex(int[] inInts, int inIntDelta, int inNInts) {
    return getIndex(inInts,inIntDelta,inNInts,hash(inInts,inIntDelta,inNInts)); }

  public long getIndex(int[] inInts, long inHash) { 
    return getIndex(inInts,0,inInts.length,inHash); }

  public long getIndex(int[] inInts) { 
    return getIndex(inInts,hash(inInts)); }

//--------------------------------------------------------------------------------------------------------
// isKnown
//--------------------------------------------------------------------------------------------------------

  public boolean isKnown(long[] inLongs, int inLongDelta, int inNLongs) {
    return (getIndex(inLongs,inLongDelta,inNLongs)!=kNotFound); }

  public boolean isKnown(long[] inLongs) { return (getIndex(inLongs)!=kNotFound); }

  public boolean isKnown(int[] inInts, int inIntDelta, int inNInts) {
    return (getIndex(inInts,inIntDelta,inNInts)!=kNotFound); }

  public boolean isKnown(int[] inInts) { return (getIndex(inInts)!=kNotFound); }

//--------------------------------------------------------------------------------------------------------
// dangerousQuickAppendVars
//
// Assumes the key does not exist in store
//--------------------------------------------------------------------------------------------------------

  private void dangerousQuickAppendVars(long[] inLongs, int inLongDelta, int inNLongs, long inHash, long inLookupIndex) {
    getKeyStore().appendVars(inLongs,inLongDelta,inNLongs);
    appendPtr(inHash,inLookupIndex);
  }

  public void dangerousQuickAppendVars(long[] inLongs, int inLongDelta, int inNLongs, long inHash) {
     dangerousQuickAppendVars(inLongs,inLongDelta,inNLongs,inHash,hashToLookupIndex(inHash)); }

  public void dangerousQuickAppendVars(long[] inLongs, long inHash) {
    dangerousQuickAppendVars(inLongs,0,inLongs.length,inHash); }

  private void dangerousQuickAppendVars(int[] inInts, int inIntDelta, int inNInts, long inHash, long inLookupIndex) {
    getKeyStore().appendVars(inInts,inIntDelta,inNInts);
    appendPtr(inHash,inLookupIndex);
  }

  public void dangerousQuickAppendVars(int[] inInts, int inIntDelta, int inNInts, long inHash) {
    dangerousQuickAppendVars(inInts,inIntDelta,inNInts,inHash,hashToLookupIndex(inHash)); }

  public void dangerousQuickAppendVars(int[] inInts, long inHash) {
    dangerousQuickAppendVars(inInts,0,inInts.length,inHash); }

//--------------------------------------------------------------------------------------------------------
// appendVars
//
// If key in store, index returned, else key is appended and kNotFound returned
// In either case, upon return, key is in store.
//--------------------------------------------------------------------------------------------------------

  public long appendVars(long[] inLongs, int inLongDelta, int inNLongs, long inHash) {
    long theLookupIndex=hashToLookupIndex(inHash);
    long theIndex=getIndex(inLongs,inLongDelta,inNLongs,inHash,theLookupIndex);
    if (theIndex==kNotFound) 
      dangerousQuickAppendVars(inLongs,inLongDelta,inNLongs,inHash,theLookupIndex);
    return theIndex;
  }

  public long appendVars(long[] inLongs, int inLongDelta, int inNLongs) {
    return appendVars(inLongs,inLongDelta,inNLongs,hash(inLongs,inLongDelta,inNLongs)); }

  public long appendVars(long[] inLongs, long inHash) { 
    return appendVars(inLongs,0,inLongs.length,inHash); }

  public long appendVars(long[] inLongs) { return appendVars(inLongs,hash(inLongs)); }

  public long appendVars(int[] inInts, int inIntDelta, int inNInts, long inHash) {
    long theLookupIndex=hashToLookupIndex(inHash);
    long theIndex=getIndex(inInts,inIntDelta,inNInts,inHash,theLookupIndex);
    if (theIndex==kNotFound) 
      dangerousQuickAppendVars(inInts,inIntDelta,inNInts,inHash,theLookupIndex);
    return theIndex;
  }

  public long appendVars(int[] inInts, int inIntDelta, int inNInts) {
    return appendVars(inInts,inIntDelta,inNInts,hash(inInts,inIntDelta,inNInts)); }

  public long appendVars(int[] inInts, long inHash) { 
    return appendVars(inInts,0,inInts.length,inHash); }

  public long appendVars(int[] inInts) { return appendVars(inInts,hash(inInts)); }

//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(boolean inDescending) {
    return getKeyStore().getSortMap(inDescending); }

  public VarRAMStore getSortMap() { return getSortMap(false); }
  
}

