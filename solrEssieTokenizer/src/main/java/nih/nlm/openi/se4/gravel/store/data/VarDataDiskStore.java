//--------------------------------------------------------------------------------------------------------
// VarDataDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.store.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// VarDataDiskStore
//--------------------------------------------------------------------------------------------------------

public class VarDataDiskStore extends VarDataStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarDataDiskStore - load
//--------------------------------------------------------------------------------------------------------

  private VarDataDiskStore(VarDiskStore inVarStore, VarDiskStore inOffsetStore) {
    super(inVarStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// VarDataDiskStore - create
//--------------------------------------------------------------------------------------------------------

  public VarDataDiskStore(String inFilename, int inVarSize, long inCapacity, long inDataCapacity) {
    this(new VarDiskStore(inFilename,inVarSize,inDataCapacity),
        new VarDiskStore(makeOffsetFilename(inFilename),inCapacity+1)); }

  public VarDataDiskStore(String inFilename) { this(inFilename,2,16,64); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDataDiskStore load(String inFilename) {
    VarDiskStore theVarStore=VarDiskStore.load(inFilename); 
    VarDiskStore theOffsetStore=VarDiskStore.load(makeOffsetFilename(inFilename)); 
    return new VarDataDiskStore(theVarStore,theOffsetStore);
  }

//--------------------------------------------------------------------------------------------------------
// getVarStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getVarStore() { return (VarDiskStore) super.getVarStore(); } 

//--------------------------------------------------------------------------------------------------------
// getOffsetStore
//--------------------------------------------------------------------------------------------------------

  protected VarDiskStore getOffsetStore() { return (VarDiskStore) super.getOffsetStore(); }

//--------------------------------------------------------------------------------------------------------
// getFilename
//--------------------------------------------------------------------------------------------------------

  public String getFilename() { return getVarStore().getFilename(); }

//--------------------------------------------------------------------------------------------------------
// getFileSize
//--------------------------------------------------------------------------------------------------------

  public long getFileSize() { 
    return getVarStore().getFileSize()+getOffsetStore().getFileSize(); }

//--------------------------------------------------------------------------------------------------------
// copy
//--------------------------------------------------------------------------------------------------------

  public void copy(String inFilename, boolean inCompact) { 
    getVarStore().copy(inFilename,inCompact); 
    getOffsetStore().copy(makeOffsetFilename(inFilename),inCompact); 
  }

  public void copy(String inFilename) { copy(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// closeAndDelete
//--------------------------------------------------------------------------------------------------------

  public void closeAndDelete() {
    getVarStore().closeAndDelete(); 
    getOffsetStore().closeAndDelete(); 
    close();
  }

}

