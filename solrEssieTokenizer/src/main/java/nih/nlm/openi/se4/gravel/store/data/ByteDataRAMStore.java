//--------------------------------------------------------------------------------------------------------
// ByteDataRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.store.*;
import gravel.store.plain.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// ByteDataRAMStore
//--------------------------------------------------------------------------------------------------------

public class ByteDataRAMStore extends ByteDataStore implements RAMStoreInterface {

//--------------------------------------------------------------------------------------------------------
// ByteDataRAMStore - load
//--------------------------------------------------------------------------------------------------------

  private ByteDataRAMStore(ByteRAMStore inByteStore, VarRAMStore inOffsetStore) {
    super(inByteStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// ByteDataRAMStore - new
//--------------------------------------------------------------------------------------------------------

  public ByteDataRAMStore(long inCapacity, long inDataCapacity) {
    this(new ByteRAMStore(inDataCapacity),new VarRAMStore(inCapacity+1)); }

  public ByteDataRAMStore() { this(16,64); }

//--------------------------------------------------------------------------------------------------------
// ByteDataRAMStore - from array
//--------------------------------------------------------------------------------------------------------

  public ByteDataRAMStore(byte[][] inData) {
    this(inData.length,inData.length*8);
    for (int i=0; i<inData.length; i++)
      appendBytes(inData[i]);
    compact();
  }

//--------------------------------------------------------------------------------------------------------
// ByteDataRAMStore - from Strings
//--------------------------------------------------------------------------------------------------------

  public ByteDataRAMStore(String[] inStrings) {
    this(inStrings.length,inStrings.length*8);
    appendUTF8s(inStrings);
    compact();
  }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public static ByteDataRAMStore wrap(ByteRAMStore inByteStore, VarRAMStore inOffsetStore) {
    return new ByteDataRAMStore(inByteStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDataRAMStore load(ByteDataStore inByteDataStore) {
    return new ByteDataRAMStore(ByteRAMStore.load(inByteDataStore.getByteStore()),
        VarRAMStore.load(inByteDataStore.getOffsetStore())); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDataRAMStore load(String inFilename) {
    ByteRAMStore theByteStore=ByteRAMStore.load(inFilename); 
    VarRAMStore theOffsetStore=VarRAMStore.load(makeOffsetFilename(inFilename)); 
    return new ByteDataRAMStore(theByteStore,theOffsetStore);
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    getByteStore().store(inFilename,inCompact); 
    getOffsetStore().store(makeOffsetFilename(inFilename),inCompact); 
  }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  protected ByteRAMStore getByteStore() { return ((ByteRAMStore) super.getByteStore()); }

//--------------------------------------------------------------------------------------------------------
// getOffsetStore
//--------------------------------------------------------------------------------------------------------

  protected VarRAMStore getOffsetStore() { return (VarRAMStore) super.getOffsetStore(); }

}

