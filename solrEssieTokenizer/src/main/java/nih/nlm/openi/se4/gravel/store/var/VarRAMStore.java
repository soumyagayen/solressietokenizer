//--------------------------------------------------------------------------------------------------------
// VarRAMStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.var;

import gravel.store.*;
import gravel.store.plain.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarRAMStore
//--------------------------------------------------------------------------------------------------------

public class VarRAMStore extends VarStore implements RAMStoreInterface {
 
//--------------------------------------------------------------------------------------------------------
// VarRAMStore - load
//--------------------------------------------------------------------------------------------------------

  private VarRAMStore(ByteRAMStore inByteStore) { super(inByteStore); }
  
//--------------------------------------------------------------------------------------------------------
// VarRAMStore - create
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore(int inVarSize, long inCapacity) {
    this(new ByteRAMStore(inCapacity*inVarSize,new long[] {inVarSize})); }

  public VarRAMStore(long inCapacity) {
    this(Conversions.calcVarLongSize(inCapacity),inCapacity); }

  public VarRAMStore() { this(16); }

//--------------------------------------------------------------------------------------------------------
// VarRAMStore - from long array
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore(long[] inLongs, int inLongDelta, int inNLongs) {
    this(Conversions.calcVarLongSize(inLongs,inLongDelta,inNLongs),inNLongs);
    appendVars(inLongs,inLongDelta,inNLongs);
  }

  public VarRAMStore(long[] inLongs) { this(inLongs,0,inLongs.length); }

//--------------------------------------------------------------------------------------------------------
// VarRAMStore - from int array
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore(int[] inInts, int inIntDelta, int inNInts) {
    this(Conversions.calcVarIntSize(inInts,inIntDelta,inNInts),inNInts);
    appendVars(inInts,inIntDelta,inNInts);
  }

  public VarRAMStore(int[] inInts) { this(inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// VarRAMStore - from bytes
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore(int inVarSize, byte[] inBytes, int inByteDelta, int inNBytes) {
    this(new ByteRAMStore(inBytes,inByteDelta,inNBytes,new long[] {inVarSize})); }

  public VarRAMStore(int inVarSize, byte[] inBytes) { this(inVarSize,inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// wrap
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore wrap(int inVarSize, ByteRAMStore inByteRAMStore) {
    inByteRAMStore.setParam(0,inVarSize);
    return new VarRAMStore(inByteRAMStore); 
  }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore load(VarStore inVarStore) {
    return new VarRAMStore(ByteRAMStore.load(inVarStore.getByteStore())); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore load(String inFilename) {
    return new VarRAMStore(ByteRAMStore.load(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
    getByteStore().store(inFilename,inCompact); }

  public void store(String inFilename) { store(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// getByteStore
//--------------------------------------------------------------------------------------------------------

  protected ByteRAMStore getByteStore() { return (ByteRAMStore) super.getByteStore(); }

}
