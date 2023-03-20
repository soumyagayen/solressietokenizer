//--------------------------------------------------------------------------------------------------------
// VarHashStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarHashStore
//--------------------------------------------------------------------------------------------------------

public abstract class VarHashStore extends HashStore { 

//--------------------------------------------------------------------------------------------------------
// VarHashStore
//--------------------------------------------------------------------------------------------------------

  protected VarHashStore(StoreInterface inKeyStore, VarStore inLookupStore, VarStore inPtrStore) {
    super(inKeyStore,null,inLookupStore,inPtrStore); }  // Note not keeping HashValues

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getKeyStore() { return (VarStore) getInnerStore(); }

//--------------------------------------------------------------------------------------------------------
// dangerousGetKeyStore
//--------------------------------------------------------------------------------------------------------

  // unprotected access to inner store - easy to screw up
  public VarStore dangerousGetKeyStore() { return (VarStore) getInnerStore(); }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public VarStore unwrap(boolean inClosePlumbing) { return (VarStore) super.unwrap(inClosePlumbing); }

  public VarStore unwrap() { return (VarStore) super.unwrap(); }

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
// getVar
//--------------------------------------------------------------------------------------------------------

  public long getLong(long inIndex) { return getKeyStore().getLong(inIndex); }
  public int getInt(long inIndex) { return getKeyStore().getInt(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getAllVars
//--------------------------------------------------------------------------------------------------------

  public int getAllVars(long[] ioLongs, int inLongDelta) { 
    return getKeyStore().getAllVars(ioLongs,inLongDelta); }

  public int getAllVars(int[] ioInts, int inIntDelta) { 
    return getKeyStore().getAllVars(ioInts,inIntDelta); }

//--------------------------------------------------------------------------------------------------------
// getAllVars
//--------------------------------------------------------------------------------------------------------

  public long[] getAllLongs() { return getKeyStore().getAllLongs(); }
  public int[] getAllInts() { return getKeyStore().getAllInts(); }

//--------------------------------------------------------------------------------------------------------
// getVarRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getVarRAMStore(long inIndex, long inNVars) {
    return getKeyStore().getVarRAMStore(inIndex,inNVars); }

  public VarRAMStore getVarRAMStore() { 
    return getVarRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// getVarHashRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarHashRAMStore getVarHashRAMStore() {
    return VarHashRAMStore.wrap(
        getKeyStore().getVarRAMStore(),
        getLookupStore().getVarRAMStore(),
        getPtrStore().getVarRAMStore()); }
  
//--------------------------------------------------------------------------------------------------------
// rebuild
//--------------------------------------------------------------------------------------------------------

  public void rebuild() { 
    // Normally would recalc all hash values here
    // Hash values are same as keys and not kept separately
    reindex();
  }

//--------------------------------------------------------------------------------------------------------
// getIndex
//
// If key in store, returns index, else returns kNotFound
// Note: using fact that hash is same as key 
//--------------------------------------------------------------------------------------------------------

  private long getIndex(long inLong, long inLookupIndex) {
    long theIndex=getFirstIndex(inLookupIndex);
    while (theIndex!=kNotFound) {
      // Normally would compare given hash with hash in store
      // For VarHashStore, hash is same as key and not stored separately
      long theLong=getLong(theIndex);
      if (theLong!=inLong)
        theIndex=getNextIndex(theIndex);
      else 
        return theIndex;
    }
    return kNotFound;
  }

  public long getIndex(long inLong) {
    return getIndex(inLong,hashToLookupIndex(inLong)); }

//--------------------------------------------------------------------------------------------------------
// getIndexs
//--------------------------------------------------------------------------------------------------------

  public long[] getIndexs(long[] inLongs) {
    long[] theIndexes=Allocate.newLongs(inLongs.length);
    for (int i=0; i<inLongs.length; i++)
      theIndexes[i]=getIndex(inLongs[i]);
    return theIndexes; 
  }

//--------------------------------------------------------------------------------------------------------
// isKnown
//--------------------------------------------------------------------------------------------------------

  public boolean isKnown(long inLong) { return (getIndex(inLong)!=kNotFound); }

//--------------------------------------------------------------------------------------------------------
// dangerousQuickAppendVar
//
// Assumes the key does not exist in store
// Note: using fact that hash is same as key 
//--------------------------------------------------------------------------------------------------------

  private void dangerousQuickAppendVar(long inLong, long inLookupIndex) {
    getKeyStore().appendVar(inLong);
    appendPtr(inLong,inLookupIndex);    
  }

  public void dangerousQuickAppendVar(long inLong) {
     dangerousQuickAppendVar(inLong,hashToLookupIndex(inLong)); }

//--------------------------------------------------------------------------------------------------------
// appendVar
//
// If key in store, index returned, else key is appended and kNotFound returned
// In either case, upon return, key is in store.
//--------------------------------------------------------------------------------------------------------

  public long appendVar(long inLong) {
    long theLookupIndex=hashToLookupIndex(inLong);
    long theIndex=getIndex(inLong,theLookupIndex);
    if (theIndex==kNotFound) 
      dangerousQuickAppendVar(inLong,theLookupIndex);    
    return theIndex;
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs) {
    for (int i=0; i<inLongs.length; i++)
      appendVar(inLongs[i]);
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(int[] inInts) {
    for (int i=0; i<inInts.length; i++)
      appendVar(inInts[i]);
  }

//--------------------------------------------------------------------------------------------------------
// appendVarStore
//--------------------------------------------------------------------------------------------------------

  public void appendVarStore(VarStore inVarStore) {
    for (long i=0; i<inVarStore.getSize(); i++)
      appendVar(inVarStore.getLong(i));
  }

//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(boolean inDescending) {
    return getKeyStore().getSortMap(inDescending); }

  public VarRAMStore getSortMap() { return getSortMap(false); }
  
//--------------------------------------------------------------------------------------------------------
// drawKey
//--------------------------------------------------------------------------------------------------------

  public String drawKey(long inIndex) { return String.valueOf(getLong(inIndex)); }

}

