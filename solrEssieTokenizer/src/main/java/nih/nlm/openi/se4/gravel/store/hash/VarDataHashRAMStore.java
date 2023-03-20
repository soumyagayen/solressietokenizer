//--------------------------------------------------------------------------------------------------------
// VarDataHashRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.data.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarDataHashRAMStore
//--------------------------------------------------------------------------------------------------------

public class VarDataHashRAMStore extends VarDataHashStore implements RAMStoreInterface {

//--------------------------------------------------------------------------------------------------------
// VarDataHashRAMStore
//--------------------------------------------------------------------------------------------------------

  private VarDataHashRAMStore(VarDataRAMStore inVarData, VarRAMStore inHashStore, 
      VarRAMStore inLookupStore, VarRAMStore inPtrStore) {
    super(inVarData,inHashStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// VarDataHashRAMStore - new
//--------------------------------------------------------------------------------------------------------

  public VarDataHashRAMStore(int inVarSize, long inReindexSize, long inDataCapacity) {
    this(new VarDataRAMStore(inVarSize,inReindexSize,inDataCapacity),
        new VarRAMStore(),new VarRAMStore(),new VarRAMStore());
    reindex(inReindexSize);
  }

  public VarDataHashRAMStore() { this(2,16,64); }

//--------------------------------------------------------------------------------------------------------
// VarDataHashRAMStore - from array
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

  public VarDataHashRAMStore(long[][] inLongs) {
    this(calcVarSize(inLongs),inLongs.length,calcDataSize(inLongs));
    for (int i=0; i<inLongs.length; i++)
      appendVars(inLongs[i]);
  }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public static VarDataHashRAMStore wrap(VarDataRAMStore inKeyStore, VarRAMStore inHashStore,
      VarRAMStore inLookupStore, VarRAMStore inPtrStore) {
    return new VarDataHashRAMStore(inKeyStore,inHashStore,inLookupStore,inPtrStore); }

  public static VarDataHashRAMStore wrap(VarDataRAMStore inKeyStore) {
    VarDataHashRAMStore theStore=wrap(inKeyStore,new VarRAMStore(),new VarRAMStore(),new VarRAMStore()); 
    theStore.rebuild();
    return theStore;
  }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDataHashRAMStore load(VarDataHashStore inVarDataHashStore) {
    VarDataRAMStore theKeyStore=VarDataRAMStore.load(inVarDataHashStore.getKeyStore()); 
    VarRAMStore theHashStore=null; 
    if (inVarDataHashStore.getKeepHashes())
      theHashStore=VarRAMStore.load(inVarDataHashStore.getHashStore()); 
    VarRAMStore theLookupStore=VarRAMStore.load(inVarDataHashStore.getLookupStore()); 
    VarRAMStore thePtrStore=VarRAMStore.load(inVarDataHashStore.getPtrStore()); 
    return new VarDataHashRAMStore(theKeyStore,theHashStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarDataHashRAMStore load(String inFilename) {
    VarDataRAMStore theKeyStore=VarDataRAMStore.load(inFilename); 
    VarRAMStore theHashStore=VarRAMStore.load(makeHashFilename(inFilename)); 
    VarRAMStore theLookupStore=VarRAMStore.load(makeLookupFilename(inFilename)); 
    VarRAMStore thePtrStore=VarRAMStore.load(makePtrFilename(inFilename)); 
    return new VarDataHashRAMStore(theKeyStore,theHashStore,theLookupStore,thePtrStore);
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    getKeyStore().store(inFilename,inCompact); 
    getHashStore().store(makeHashFilename(inFilename),inCompact); 
    getLookupStore().store(makeLookupFilename(inFilename),inCompact); 
    getPtrStore().store(makePtrFilename(inFilename),inCompact); 
  }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected VarDataRAMStore getKeyStore() { return (VarDataRAMStore) super.getKeyStore(); }

//--------------------------------------------------------------------------------------------------------
// getHashStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getHashStore() { return (VarRAMStore) super.getHashStore(); }

//--------------------------------------------------------------------------------------------------------
// getLookupStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getLookupStore() { return (VarRAMStore) super.getLookupStore(); }

//--------------------------------------------------------------------------------------------------------
// getPtrStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getPtrStore() { return (VarRAMStore) super.getPtrStore(); }

}


