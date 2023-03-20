//--------------------------------------------------------------------------------------------------------
// VarDataHashDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.data.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// VarDataHashDiskStore
//--------------------------------------------------------------------------------------------------------

public class VarDataHashDiskStore extends VarDataHashStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarDataHashDiskStore
//--------------------------------------------------------------------------------------------------------

  private VarDataHashDiskStore(VarDataDiskStore inVarData, VarDiskStore inHashStore,
      VarDiskStore inLookupStore, VarDiskStore inPtrStore) {
    super(inVarData,inHashStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// VarDataHashDiskStore - new
//--------------------------------------------------------------------------------------------------------

  public VarDataHashDiskStore(String inFilename, int inVarSize, long inReindexSize, long inDataCapacity) {
    this(new VarDataDiskStore(inFilename,inVarSize,inReindexSize,inDataCapacity),
        new VarDiskStore(makeHashFilename(inFilename)),
        new VarDiskStore(makeLookupFilename(inFilename)),
        new VarDiskStore(makePtrFilename(inFilename))); 
    reindex(inReindexSize);
  }

  public VarDataHashDiskStore(String inFilename) { this(inFilename,2,16,64); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDataHashDiskStore load(String inFilename) {
    VarDataDiskStore theKeyStore=VarDataDiskStore.load(inFilename); 
    VarDiskStore theHashStore=VarDiskStore.load(makeHashFilename(inFilename)); 
    VarDiskStore theLookupStore=VarDiskStore.load(makeLookupFilename(inFilename)); 
    VarDiskStore thePtrStore=VarDiskStore.load(makePtrFilename(inFilename)); 
    return new VarDataHashDiskStore(theKeyStore,theHashStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected VarDataDiskStore getKeyStore() { return (VarDataDiskStore) super.getKeyStore(); }

//--------------------------------------------------------------------------------------------------------
// getHashStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getHashStore() { return (VarDiskStore) super.getHashStore(); }

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
    return getKeyStore().getFileSize()+getLookupStore().getFileSize()+
        getPtrStore().getFileSize(); }

//--------------------------------------------------------------------------------------------------------
// copy
//--------------------------------------------------------------------------------------------------------

  public void copy(String inFilename, boolean inCompact) { 
    getKeyStore().copy(inFilename,inCompact); 
    getHashStore().copy(makeHashFilename(inFilename),inCompact); 
    getLookupStore().copy(makeLookupFilename(inFilename),inCompact); 
    getPtrStore().copy(makePtrFilename(inFilename),inCompact); 
  }

  public void copy(String inFilename) { copy(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// closeAndDelete
//--------------------------------------------------------------------------------------------------------

  public void closeAndDelete() {
    getKeyStore().closeAndDelete(); 
    getHashStore().closeAndDelete(); 
    getLookupStore().closeAndDelete(); 
    getPtrStore().closeAndDelete(); 
    close();
  }

}

