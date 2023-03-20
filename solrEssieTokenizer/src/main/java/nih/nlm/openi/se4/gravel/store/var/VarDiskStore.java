//--------------------------------------------------------------------------------------------------------
// VarDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.var;

import gravel.store.*;
import gravel.store.plain.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarDiskStore
//--------------------------------------------------------------------------------------------------------

public class VarDiskStore extends VarStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarDiskStore - load
//--------------------------------------------------------------------------------------------------------

  private VarDiskStore(ByteDiskStore inByteStore) { super(inByteStore); }

//--------------------------------------------------------------------------------------------------------
// VarDiskStore - create
//--------------------------------------------------------------------------------------------------------

  public VarDiskStore(String inFilename, int inVarSize, long inCapacity) {
    this(new ByteDiskStore(inFilename,inCapacity*inVarSize,new long[] {inVarSize})); }

  public VarDiskStore(String inFilename, long inCapacity) {
    this(inFilename,Conversions.calcVarLongSize(inCapacity),inCapacity); }

  public VarDiskStore(String inFilename) {
    this(inFilename,16); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDiskStore load(String inFilename) {
    return new VarDiskStore(ByteDiskStore.load(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDiskStore getByteStore() { return (ByteDiskStore) super.getByteStore(); }

//--------------------------------------------------------------------------------------------------------
// getFilename
//--------------------------------------------------------------------------------------------------------

  public String getFilename() { return getByteStore().getFilename(); }

//--------------------------------------------------------------------------------------------------------
// getFileSize
//--------------------------------------------------------------------------------------------------------

  public long getFileSize() { return getByteStore().getFileSize(); }

//--------------------------------------------------------------------------------------------------------
// copy
//--------------------------------------------------------------------------------------------------------

  public void copy(String inFilename, boolean inCompact) { 
    getByteStore().copy(inFilename,inCompact); }
  
  public void copy(String inFilename) { copy(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// closeAndDelete
//--------------------------------------------------------------------------------------------------------

  public void closeAndDelete() { getByteStore().closeAndDelete(); }

}
