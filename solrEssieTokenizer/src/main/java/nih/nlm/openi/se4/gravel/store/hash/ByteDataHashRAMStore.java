//--------------------------------------------------------------------------------------------------------
// ByteDataHashRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.data.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// ByteDataHashRAMStore
//--------------------------------------------------------------------------------------------------------

public class ByteDataHashRAMStore extends ByteDataHashStore implements RAMStoreInterface { 

//--------------------------------------------------------------------------------------------------------
// ByteDataHashRAMStore
//--------------------------------------------------------------------------------------------------------

  protected ByteDataHashRAMStore(ByteDataRAMStore inByteData, VarRAMStore inHashStore, 
      VarRAMStore inLookupStore, VarRAMStore inPtrStore) {
    super(inByteData,inHashStore,inLookupStore,inPtrStore); }

//--------------------------------------------------------------------------------------------------------
// ByteDataHashRAMStore - new
//--------------------------------------------------------------------------------------------------------

  public ByteDataHashRAMStore(long inReindexSize, long inDataCapacity) {
    this(new ByteDataRAMStore(inReindexSize,inDataCapacity),
        new VarRAMStore(),new VarRAMStore(),new VarRAMStore());
    reindex(inReindexSize);
  }

  public ByteDataHashRAMStore() { this(16,64); }

//--------------------------------------------------------------------------------------------------------
// ByteDataHashRAMStore - from array
//--------------------------------------------------------------------------------------------------------

  public ByteDataHashRAMStore(byte[][] inBytes) {
    this(inBytes.length,inBytes.length*16);
    for (int i=0; i<inBytes.length; i++)
      appendBytes(inBytes[i]);
    compact();
  }

//--------------------------------------------------------------------------------------------------------
// ByteDataHashRAMStore - from strings
//--------------------------------------------------------------------------------------------------------

  public ByteDataHashRAMStore(String[] inStrings, int inStringDelta, int inNStrings) {
    this(inNStrings,inNStrings*16);
    for (int i=0; i<inNStrings; i++) 
      appendUTF8(inStrings[inStringDelta+i]);
    compact();
  }

  public ByteDataHashRAMStore(String[] inStrings) { this(inStrings,0,inStrings.length); }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public static ByteDataHashRAMStore wrap(ByteDataRAMStore inKeyStore, VarRAMStore inHashStore,
      VarRAMStore inLookupStore, VarRAMStore inPtrStore) {
    return new ByteDataHashRAMStore(inKeyStore,inHashStore,inLookupStore,inPtrStore); 
  }

  public static ByteDataHashRAMStore wrap(ByteDataRAMStore inKeyStore) {
    ByteDataHashRAMStore theStore=wrap(inKeyStore,new VarRAMStore(),new VarRAMStore(),new VarRAMStore()); 
    theStore.rebuild();
    return theStore;
  }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDataHashRAMStore load(ByteDataHashStore inByteDataHashStore) {
    ByteDataRAMStore theKeyStore=ByteDataRAMStore.load(inByteDataHashStore.getKeyStore()); 
    VarRAMStore theHashStore=null; 
    if (inByteDataHashStore.getKeepHashes())
      theHashStore=VarRAMStore.load(inByteDataHashStore.getHashStore()); 
    VarRAMStore theLookupStore=VarRAMStore.load(inByteDataHashStore.getLookupStore()); 
    VarRAMStore thePtrStore=VarRAMStore.load(inByteDataHashStore.getPtrStore()); 
    return new ByteDataHashRAMStore(theKeyStore,theHashStore,theLookupStore,thePtrStore);
  }
  
//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDataHashRAMStore load(String inFilename) {
    ByteDataRAMStore theKeyStore=ByteDataRAMStore.load(inFilename); 
    VarRAMStore theHashStore=VarRAMStore.load(makeHashFilename(inFilename)); 
    VarRAMStore theLookupStore=VarRAMStore.load(makeLookupFilename(inFilename)); 
    VarRAMStore thePtrStore=VarRAMStore.load(makePtrFilename(inFilename)); 
    return new ByteDataHashRAMStore(theKeyStore,theHashStore,theLookupStore,thePtrStore);
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

  protected ByteDataRAMStore getKeyStore() { return (ByteDataRAMStore) super.getKeyStore(); }

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

