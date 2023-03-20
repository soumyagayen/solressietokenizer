//--------------------------------------------------------------------------------------------------------
// VarHashDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// VarHashDiskStore
//--------------------------------------------------------------------------------------------------------

public class VarHashDiskStore extends VarHashStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarHashDiskStore
//--------------------------------------------------------------------------------------------------------

  private VarHashDiskStore(VarDiskStore inVarStore, VarDiskStore inLookupStore,
          VarDiskStore inPtrStore) {
    super(inVarStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// VarHashDiskStore - new
//--------------------------------------------------------------------------------------------------------

  public VarHashDiskStore(String inFilename, int inVarSize, long inReindexSize) {
    this(new VarDiskStore(inFilename,inVarSize,inReindexSize),
        new VarDiskStore(makeLookupFilename(inFilename)),
        new VarDiskStore(makePtrFilename(inFilename))); 
    reindex(inReindexSize);
  }

  public VarHashDiskStore(String inFilename) { this(inFilename,2,16); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarHashDiskStore load(String inFilename) {
    VarDiskStore theKeyStore=VarDiskStore.load(inFilename); 
    VarDiskStore theLookupStore=VarDiskStore.load(makeLookupFilename(inFilename)); 
    VarDiskStore thePtrStore=VarDiskStore.load(makePtrFilename(inFilename)); 
    return new VarHashDiskStore(theKeyStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getKeyStore() { return (VarDiskStore) super.getKeyStore(); }

//--------------------------------------------------------------------------------------------------------
// getLookupStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getLookupStore() { return (VarDiskStore) super.getLookupStore(); }

//--------------------------------------------------------------------------------------------------------
// getPtrStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getPtrStore() { return (VarDiskStore) super.getPtrStore(); }

//--------------------------------------------------------------------------------------------------------
// getFilename
//--------------------------------------------------------------------------------------------------------

  public String getFilename() { return getKeyStore().getFilename(); }

//--------------------------------------------------------------------------------------------------------
// getFileSize
//--------------------------------------------------------------------------------------------------------

  public long getFileSize() { 
    return getKeyStore().getFileSize()+getLookupStore().getFileSize()+getPtrStore().getFileSize(); }

//--------------------------------------------------------------------------------------------------------
// copy
//--------------------------------------------------------------------------------------------------------

  public void copy(String inFilename, boolean inCompact) { 
    getKeyStore().copy(inFilename,inCompact); 
    getLookupStore().copy(makeLookupFilename(inFilename),inCompact); 
    getPtrStore().copy(makePtrFilename(inFilename),inCompact); 
  }

  public void copy(String inFilename) { copy(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// closeAndDelete
//--------------------------------------------------------------------------------------------------------

  public void closeAndDelete() {
    getKeyStore().closeAndDelete(); 
    getLookupStore().closeAndDelete(); 
    getPtrStore().closeAndDelete(); 
    close();
  }

}

