//--------------------------------------------------------------------------------------------------------
// VarChainRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.chain;

import gravel.store.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarChainRAMStore
//--------------------------------------------------------------------------------------------------------

public class VarChainRAMStore extends VarChainStore implements RAMStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarChainRAMStore - load
//--------------------------------------------------------------------------------------------------------

  private VarChainRAMStore(VarRAMStore inLinkStore, VarRAMStore inNextStore, 
      VarRAMStore inLastStore, VarRAMStore inNStore) {
    super(inLinkStore,inNextStore,inLastStore,inNStore); }

//--------------------------------------------------------------------------------------------------------
// VarChainRAMStore - new
//--------------------------------------------------------------------------------------------------------

  public VarChainRAMStore(int inVarSize, long inCapacity, long inLinkCapacity) {
    this(new VarRAMStore(inVarSize,inLinkCapacity),new VarRAMStore(inLinkCapacity),
        new VarRAMStore(inCapacity+1),new VarRAMStore(inCapacity+1)); }

  public VarChainRAMStore() { this(2,16,64); }

//--------------------------------------------------------------------------------------------------------
// VarChainRAMStore
//--------------------------------------------------------------------------------------------------------

  private static int calcVarSize(long[][] inData) {
    long theMin=Long.MAX_VALUE;
    long theMax=Long.MIN_VALUE;
    for (int i=0; i<inData.length; i++)
      for (int j=0; j<inData[i].length; j++) {
        long theValue=inData[i][j];
        theMin=Math.min(theMin,theValue);
        theMax=Math.max(theMax,theValue);
      }
    return Math.max(Conversions.calcVarLongSize(theMin),Conversions.calcVarLongSize(theMax));
  }

  private static long calcDataSize(long[][] inData) {
    long theNValues=0;
    for (int i=0; i<inData.length; i++)
      theNValues+=inData[i].length;
    return theNValues;
  }

  public VarChainRAMStore(long[][] inChains) {
    this(calcVarSize(inChains),inChains.length,calcDataSize(inChains));
    for (int i=0; i<inChains.length; i++)
      appendVars(inChains[i]);
  }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public VarChainRAMStore wrap(VarRAMStore inLinkStore, VarRAMStore inNextStore, 
      VarRAMStore inLastStore, VarRAMStore inNStore) {
    return new VarChainRAMStore(inLinkStore,inNextStore,inLastStore,inNStore); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarChainRAMStore load(String inFilename) {
    VarRAMStore theLinkStore=VarRAMStore.load(inFilename); 
    VarRAMStore theNextStore=VarRAMStore.load(makeNextFilename(inFilename)); 
    VarRAMStore theLastStore=VarRAMStore.load(makeLastFilename(inFilename)); 
    VarRAMStore theNStore=VarRAMStore.load(makeNFilename(inFilename)); 
    return new VarChainRAMStore(theLinkStore,theNextStore,theLastStore,theNStore);
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    getLinkStore().store(inFilename,inCompact); 
    getNextStore().store(makeNextFilename(inFilename),inCompact); 
    getLastStore().store(makeLastFilename(inFilename),inCompact); 
    getNStore().store(makeNFilename(inFilename),inCompact); 
  }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// getLinkStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getLinkStore() { return (VarRAMStore) super.getLinkStore(); }

//--------------------------------------------------------------------------------------------------------
// getNextStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getNextStore() { return (VarRAMStore) super.getNextStore(); }

//--------------------------------------------------------------------------------------------------------
// getLastStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getLastStore() { return (VarRAMStore) super.getLastStore(); }

//--------------------------------------------------------------------------------------------------------
// getNStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getNStore() { return (VarRAMStore) super.getNStore(); }

}

