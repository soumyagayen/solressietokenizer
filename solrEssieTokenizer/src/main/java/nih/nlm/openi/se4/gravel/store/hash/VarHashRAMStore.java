//--------------------------------------------------------------------------------------------------------
// VarHashRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarHashRAMStore
//--------------------------------------------------------------------------------------------------------

public class VarHashRAMStore extends VarHashStore implements RAMStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarHashRAMStore
//--------------------------------------------------------------------------------------------------------

  private VarHashRAMStore(VarRAMStore inVarStore, VarRAMStore inLookupStore, VarRAMStore inPtrStore) {
    super(inVarStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// VarHashRAMStore - new
//--------------------------------------------------------------------------------------------------------

  public VarHashRAMStore(int inVarSize, long inReindexSize) {
    this(new VarRAMStore(inVarSize,inReindexSize),new VarRAMStore(),new VarRAMStore());
    reindex(inReindexSize);
  }

  public VarHashRAMStore() { this(2,16); }

  public VarHashRAMStore(VarRAMStore inVarRAMStore) {
    this(inVarRAMStore.getVarRAMStore(),new VarRAMStore(),new VarRAMStore());
    rebuild();
  }

//--------------------------------------------------------------------------------------------------------
// VarHashRAMStore - from array
//--------------------------------------------------------------------------------------------------------

  public VarHashRAMStore(long[] inLongs) {
    this(Conversions.calcVarLongSize(inLongs),inLongs.length);
    // Safe, even if inLongs are non-unique
    for (int i=0; i<inLongs.length; i++) 
      appendVar(inLongs[i]);
    compact();
  }

  public VarHashRAMStore(int[] inInts) {
    this(Conversions.calcVarIntSize(inInts),inInts.length);
    // Safe, even if inVarStore are non-unique
    for (int i=0; i<inInts.length; i++) 
      appendVar(inInts[i]);
    compact();
  }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public static VarHashRAMStore wrap(VarRAMStore inKeyStore, VarRAMStore inLookupStore,
      VarRAMStore inPtrStore) {
    return new VarHashRAMStore(inKeyStore,inLookupStore,inPtrStore); }

  public static VarHashRAMStore wrap(VarRAMStore inKeyStore) {
    VarHashRAMStore theStore=wrap(inKeyStore,new VarRAMStore(),new VarRAMStore()); 
    theStore.rebuild();
    return theStore;
  }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarHashRAMStore load(VarHashStore inVarHashStore) {
    VarRAMStore theKeyStore=VarRAMStore.load(inVarHashStore.getKeyStore()); 
    VarRAMStore theLookupStore=VarRAMStore.load(inVarHashStore.getLookupStore()); 
    VarRAMStore thePtrStore=VarRAMStore.load(inVarHashStore.getPtrStore()); 
    return new VarHashRAMStore(theKeyStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarHashRAMStore load(String inFilename) {
    VarRAMStore theKeyStore=VarRAMStore.load(inFilename); 
    VarRAMStore theLookupStore=VarRAMStore.load(makeLookupFilename(inFilename)); 
    VarRAMStore thePtrStore=VarRAMStore.load(makePtrFilename(inFilename)); 
    return new VarHashRAMStore(theKeyStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    getKeyStore().store(inFilename,inCompact); 
    getLookupStore().store(makeLookupFilename(inFilename),inCompact); 
    getPtrStore().store(makePtrFilename(inFilename),inCompact); 
  }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getKeyStore() { return (VarRAMStore) super.getKeyStore(); }

//--------------------------------------------------------------------------------------------------------
// getLookupStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getLookupStore() { return (VarRAMStore) super.getLookupStore(); }

//--------------------------------------------------------------------------------------------------------
// getPtrStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getPtrStore() { return (VarRAMStore) super.getPtrStore(); }

}

