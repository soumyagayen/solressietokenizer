//--------------------------------------------------------------------------------------------------------
// ByteDataDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.store.*;
import gravel.store.plain.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// ByteDataDiskStore
//--------------------------------------------------------------------------------------------------------

public class ByteDataDiskStore extends ByteDataStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// ByteDataDiskStore - load
//--------------------------------------------------------------------------------------------------------

  private ByteDataDiskStore(ByteDiskStore inByteStore, VarDiskStore inOffsetStore) {
    super(inByteStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// ByteDataDiskStore - create
//--------------------------------------------------------------------------------------------------------

  public ByteDataDiskStore(String inFilename, long inCapacity, long inDataCapacity) {
    this(new ByteDiskStore(inFilename,inDataCapacity),
        new VarDiskStore(makeOffsetFilename(inFilename),inCapacity+1)); }

  public ByteDataDiskStore(String inFilename) { this(inFilename,16,64); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDataDiskStore load(String inFilename) {
    ByteDiskStore theByteStore=ByteDiskStore.load(inFilename); 
    VarDiskStore theOffsetStore=VarDiskStore.load(makeOffsetFilename(inFilename)); 
    return new ByteDataDiskStore(theByteStore,theOffsetStore);
  }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDiskStore getByteStore() { return (ByteDiskStore) super.getByteStore(); }

//--------------------------------------------------------------------------------------------------------
// getOffsetStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getOffsetStore() { return (VarDiskStore) super.getOffsetStore(); }

//--------------------------------------------------------------------------------------------------------
// getFilename
//--------------------------------------------------------------------------------------------------------

  public String getFilename() { return getByteStore().getFilename(); }

//--------------------------------------------------------------------------------------------------------
// getFileSize
//--------------------------------------------------------------------------------------------------------

  public long getFileSize() { 
    return getByteStore().getFileSize()+getOffsetStore().getFileSize(); }

//--------------------------------------------------------------------------------------------------------
// copy
//--------------------------------------------------------------------------------------------------------

  public void copy(String inFilename, boolean inCompact) { 
    getByteStore().copy(inFilename,inCompact); 
    getOffsetStore().copy(makeOffsetFilename(inFilename),inCompact); 
  }

  public void copy(String inFilename) { copy(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// closeAndDelete
//--------------------------------------------------------------------------------------------------------

  public void closeAndDelete() {
    getByteStore().closeAndDelete(); 
    getOffsetStore().closeAndDelete(); 
    close();
  }

}

