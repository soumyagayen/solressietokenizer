//--------------------------------------------------------------------------------------------------------
// ByteDataHashDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.data.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// ByteDataHashDiskStore
//--------------------------------------------------------------------------------------------------------

public class ByteDataHashDiskStore extends ByteDataHashStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// ByteDataHashDiskStore
//--------------------------------------------------------------------------------------------------------

  private ByteDataHashDiskStore(ByteDataDiskStore inByteData, VarDiskStore inHashStore,
      VarDiskStore inLookupStore, VarDiskStore inPtrStore) {
    super(inByteData,inHashStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// ByteDataHashDiskStore - new
//--------------------------------------------------------------------------------------------------------

  public ByteDataHashDiskStore(String inFilename, int inReindexSize, long inDataCapacity) {
    this(new ByteDataDiskStore(inFilename,inReindexSize,inDataCapacity),
        new VarDiskStore(makeHashFilename(inFilename)),
        new VarDiskStore(makeLookupFilename(inFilename)),
        new VarDiskStore(makePtrFilename(inFilename))); 
    reindex(inReindexSize);
  }

  public ByteDataHashDiskStore(String inFilename) {
    this(inFilename,16,64); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDataHashDiskStore load(String inFilename) {
    ByteDataDiskStore theKeyStore=ByteDataDiskStore.load(inFilename); 
    VarDiskStore theHashStore=VarDiskStore.load(makeHashFilename(inFilename)); 
    VarDiskStore theLookupStore=VarDiskStore.load(makeLookupFilename(inFilename)); 
    VarDiskStore thePtrStore=VarDiskStore.load(makePtrFilename(inFilename)); 
    return new ByteDataHashDiskStore(theKeyStore,theHashStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDataDiskStore getKeyStore() { return (ByteDataDiskStore) super.getKeyStore(); }

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
    return getKeyStore().getFileSize()+getHashStore().getFileSize()+
        getLookupStore().getFileSize()+getPtrStore().getFileSize(); }

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

