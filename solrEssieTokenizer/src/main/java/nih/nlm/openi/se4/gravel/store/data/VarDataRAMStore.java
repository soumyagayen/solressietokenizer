//--------------------------------------------------------------------------------------------------------
// VarDataRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.store.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarDataRAMStore
//--------------------------------------------------------------------------------------------------------

public class VarDataRAMStore extends VarDataStore implements RAMStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarDataRAMStore - load
//--------------------------------------------------------------------------------------------------------

  private VarDataRAMStore(VarRAMStore inVarStore, VarRAMStore inOffsetStore) {
    super(inVarStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// VarDataRAMStore - new
//--------------------------------------------------------------------------------------------------------

  public VarDataRAMStore(int inVarSize, long inCapacity, long inDataCapacity) {
    this(new VarRAMStore(inVarSize,inDataCapacity),new VarRAMStore(inCapacity+1)); }

  public VarDataRAMStore() { this(2,16,64); }

//--------------------------------------------------------------------------------------------------------
// VarDataRAMStore - from array
//--------------------------------------------------------------------------------------------------------

  private static int calcVarSize(long[][] inData) {
    int theVarSize=1;
    for (int i=0; i<inData.length; i++)
      theVarSize=Math.max(theVarSize,Conversions.calcVarLongSize(inData[i]));
    return theVarSize;
  }

  private static long calcDataSize(long[][] inData) {
    long theNValues=0;
    for (int i=0; i<inData.length; i++)
      theNValues+=inData[i].length;
    return theNValues;
  }

  public VarDataRAMStore(long[][] inData) { 
    this(calcVarSize(inData),inData.length,calcDataSize(inData));
    for (int i=0; i<inData.length; i++)
      appendVars(inData[i]);
  }

  private static int calcVarSize(int[][] inData) {
    int theVarSize=1;
    for (int i=0; i<inData.length; i++)
      theVarSize=Math.max(theVarSize,Conversions.calcVarIntSize(inData[i]));
    return theVarSize;
  }

  private static long calcDataSize(int[][] inData) {
    long theNValues=0;
    for (int i=0; i<inData.length; i++)
      theNValues+=inData[i].length;
    return theNValues;
  }

  public VarDataRAMStore(int[][] inData) {
    this(calcVarSize(inData),inData.length,calcDataSize(inData));
    for (int i=0; i<inData.length; i++)
      appendVars(inData[i]);
  }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public static VarDataRAMStore wrap(VarRAMStore inVarStore, VarRAMStore inOffsetStore) {
    return new VarDataRAMStore(inVarStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDataRAMStore load(VarDataStore inVarDataStore) {
    return new VarDataRAMStore(VarRAMStore.load(inVarDataStore.getVarStore()),
        VarRAMStore.load(inVarDataStore.getOffsetStore())); }
  
//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDataRAMStore load(String inFilename) {
    VarRAMStore theVarStore=VarRAMStore.load(inFilename); 
    VarRAMStore theOffsetStore=VarRAMStore.load(makeOffsetFilename(inFilename)); 
    return new VarDataRAMStore(theVarStore,theOffsetStore);
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    getVarStore().store(inFilename,inCompact); 
    getOffsetStore().store(makeOffsetFilename(inFilename),inCompact); 
  }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// getVarStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getVarStore() { return (VarRAMStore) super.getVarStore(); }

//--------------------------------------------------------------------------------------------------------
// getOffsetStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getOffsetStore() { return (VarRAMStore) super.getOffsetStore(); }

}

