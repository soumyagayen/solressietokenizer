//--------------------------------------------------------------------------------------------------------
// VarDataStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.sort.*;
import gravel.store.hash.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarDataStore
//--------------------------------------------------------------------------------------------------------

public abstract class VarDataStore extends DataStore {

//--------------------------------------------------------------------------------------------------------
// VarDataStore
//--------------------------------------------------------------------------------------------------------

  protected VarDataStore(VarStore inVarStore, VarStore inOffsetStore) {
    super(inVarStore,inOffsetStore); }

//--------------------------------------------------------------------------------------------------------
// getVarStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getVarStore() { return (VarStore) getInnerStore(); }
  
//--------------------------------------------------------------------------------------------------------
// dangerousGetVarStore
//--------------------------------------------------------------------------------------------------------

  // unprotected access to inner store - easy to screw up
  public VarStore dangerousGetVarStore() { return (VarStore) getInnerStore(); }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public VarStore unwrap(boolean inCloseOffsets) { return (VarStore) super.unwrap(inCloseOffsets); }

  public VarStore unwrap() { return (VarStore) super.unwrap(); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(long[] inLongs, int inLongDelta, int inNLongs) { 
    return HashUtils.hash(inLongs,inLongDelta,inNLongs); }

  public long hash(long[] inLongs) { return hash(inLongs,0,inLongs.length); }

  public long hash(int[] inInts, int inIntDelta, int inNInts) { 
    return HashUtils.hash(inInts,inIntDelta,inNInts); }

  public long hash(int[] inInts) { return hash(inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) { 
    long theOffset=getOffset(inIndex);
    int theNVars=(int) (getOffset(inIndex+1)-theOffset);
    return getVarStore().getHash(theOffset,theNVars);
  }

//--------------------------------------------------------------------------------------------------------
// getVarSize
//--------------------------------------------------------------------------------------------------------

  public int getVarSize() { return getVarStore().getVarSize(); }

//--------------------------------------------------------------------------------------------------------
// setVarSize
//--------------------------------------------------------------------------------------------------------

  public void setVarSize(int inVarSize) { getVarStore().setVarSize(inVarSize); }

//--------------------------------------------------------------------------------------------------------
// getNVars
//--------------------------------------------------------------------------------------------------------

  public long getNVars(long inIndex) { 
    long theNVars=getOffset(inIndex+1)-getOffset(inIndex); 
    if ((kRangeChecking)&&(theNVars<0))
      throw new StoreException("Negative NVars: "+theNVars);
    return theNVars;
  }

//--------------------------------------------------------------------------------------------------------
// getMinNVars
//--------------------------------------------------------------------------------------------------------

  public long getMinNVars() {
    long theSize=getSize();
    if (theSize==0)
      return 0;
    long theMinNLongs=Long.MAX_VALUE;
    long theLastOffset=getOffset(0);
    for (long i=1; i<=theSize; i++) {
      long theOffset=getOffset(i);
      theMinNLongs=Math.min(theMinNLongs,theOffset-theLastOffset);
      theLastOffset=theOffset;
    }
    return theMinNLongs;
  }

//--------------------------------------------------------------------------------------------------------
// getMaxNVars
//--------------------------------------------------------------------------------------------------------

  public long getMaxNVars() {
    long theMaxNLongs=0;
    long theSize=getSize();
    long theLastOffset=getOffset(0);
    for (long i=1; i<=theSize; i++) {
      long theOffset=getOffset(i);
      theMaxNLongs=Math.max(theMaxNLongs,theOffset-theLastOffset);
      theLastOffset=theOffset;
    }
    return theMaxNLongs;
  }

//--------------------------------------------------------------------------------------------------------
// getVarAtN
//--------------------------------------------------------------------------------------------------------

  public long getLongAtN(long inIndex, long inN) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN>=getNVars(inIndex))
        throw new StoreException("N past end of row: "+inN+" >= "+getNVars(inIndex));
    }
    return getVarStore().getLong(getOffset(inIndex)+inN); 
  }

  public int getIntAtN(long inIndex, long inN) { return (int) getLongAtN(inIndex,inN); }

//--------------------------------------------------------------------------------------------------------
// getVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void getVarsAtN(long inIndex, long inN, long[] ioLongs, int inLongDelta, int inNLongs) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inLongDelta<0)
        throw new StoreException("Negative LongDelta: "+inLongDelta);
      if (inNLongs<0)
        throw new StoreException("Negative NLongs: "+inNLongs);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN+inNLongs>getNVars(inIndex))
        throw new StoreException("N+NLongs past end of row: "+inN+"+"+inNLongs+" > "+getNVars(inIndex));
      if (inLongDelta+inNLongs>ioLongs.length)
        throw new StoreException("LongDelta+NLongs past end: "+inLongDelta+"+"+inNLongs+">"+ioLongs.length);
    }
    getVarStore().getVars(getOffset(inIndex)+inN,ioLongs,inLongDelta,inNLongs); 
  }

  public void getVarsAtN(long inIndex, long inN, int[] ioInts, int inIntDelta, int inNInts) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIntDelta<0)
        throw new StoreException("Negative IntDelta: "+inIntDelta);
      if (inNInts<0)
        throw new StoreException("Negative NInts: "+inNInts);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN+inNInts>getNVars(inIndex))
        throw new StoreException("N+NInts past end of row: "+inN+"+"+inNInts+" > "+getNVars(inIndex));
      if (inIntDelta+inNInts>ioInts.length)
        throw new StoreException("IntDelta+NInts past end: "+inIntDelta+"+"+inNInts+">"+ioInts.length);
    }
    getVarStore().getVars(getOffset(inIndex)+inN,ioInts,inIntDelta,inNInts); 
  }

//--------------------------------------------------------------------------------------------------------
// getVarsAtN
//--------------------------------------------------------------------------------------------------------

  public long[] getLongsAtN(long inIndex, long inN, int inNLongs) {
    if (inNLongs==0)
      return kNoLongs;
    else {
      long[] theLongs=Allocate.newLongs(inNLongs);
      getVarsAtN(inIndex,inN,theLongs,0,inNLongs); 
      return theLongs;
    }
  }

  public int[] getIntsAtN(long inIndex, long inN, int inNInts) {
    if (inNInts==0)
      return kNoInts;
    else {
      int[] theInts=Allocate.newInts(inNInts);
      getVarsAtN(inIndex,inN,theInts,0,inNInts); 
      return theInts;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public int getVars(long inIndex, long[] ioLongs, int inLongDelta) {
    int theNLongs=(int) getNVars(inIndex);
    if (theNLongs>0)
      getVarsAtN(inIndex,0,ioLongs,inLongDelta,theNLongs); 
    return theNLongs;
  }

  public int getVars(long inIndex, int[] ioInts, int inIntDelta) {
    int theNInts=(int) getNVars(inIndex);
    if (theNInts>0)
      getVarsAtN(inIndex,0,ioInts,inIntDelta,theNInts); 
    return theNInts;
  }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public int getVars(long inIndex, long[] ioLongs) { return getVars(inIndex,ioLongs,0); }
  public int getVars(long inIndex, int[] ioInts) { return getVars(inIndex,ioInts,0); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public long[] getLongs(long inIndex) {
    long theNLongs=getNVars(inIndex);
    if (theNLongs==0)
      return kNoLongs;
    if (theNLongs>k1G/kLongMemory)
      throw new StoreException("Row too big");
    long[] theLongs=Allocate.newLongs(theNLongs);
    getVars(inIndex,theLongs); 
    return theLongs;
  }

  public int[] getInts(long inIndex) {
    long theNInts=getNVars(inIndex);
    if (theNInts==0)
      return kNoInts;
    if (theNInts>k1G/kIntMemory)
      throw new StoreException("Row too big");
    int[] theInts=Allocate.newInts(theNInts);
    getVars(inIndex,theInts); 
    return theInts;
  }

//--------------------------------------------------------------------------------------------------------
// getAllVarss
//--------------------------------------------------------------------------------------------------------

  public long[][] getAllLongss() {
    long theSize=getSize();
    if (theSize>k1G/kLongMemory)
      throw new StoreException("Store too big");
    long[][] theLongss=new long[(int) theSize][];
    for (int i=0; i<theLongss.length; i++)
      theLongss[i]=getLongs(i); 
    return theLongss;
  }

  public int[][] getAllIntss() {
    long theSize=getSize();
    if (theSize>k1G/kIntMemory)
      throw new StoreException("Store too big");
    int[][] theIntss=new int[(int) theSize][];
    for (int i=0; i<theIntss.length; i++)
      theIntss[i]=getInts(i); 
    return theIntss;
  }

//--------------------------------------------------------------------------------------------------------
// getVarStore
//--------------------------------------------------------------------------------------------------------

  public void getVarStore(long inIndex, VarStore inDstVarStore) {
    long theOffset=getOffset(inIndex);
    long theNLongs=getOffset(inIndex+1)-theOffset;
    getVarStore().getVarStore(theOffset,theNLongs,inDstVarStore);
  }

//--------------------------------------------------------------------------------------------------------
// getVarRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getVarRAMStore(long inIndex) {
    VarRAMStore theVarRAMStore=new VarRAMStore(getVarStore().getVarSize(),getNVars(inIndex));
    getVarStore(inIndex,theVarRAMStore);
    return theVarRAMStore;
  }

//--------------------------------------------------------------------------------------------------------
// getVarDataStore
//--------------------------------------------------------------------------------------------------------

  public void getVarDataStore(long inIndex, long inNDatas, VarDataStore inDstVarDataStore) {
    long theOffset=getOffset(inIndex);
    long theDataSize=getOffset(inIndex+inNDatas)-theOffset;
    long theDstSize=inDstVarDataStore.getSize();
    long theDelta=inDstVarDataStore.getDataSize()-theOffset;
    getVarStore().getVarStore(theOffset,theDataSize,inDstVarDataStore.getVarStore());
    getOffsetStore().getVarStore(inIndex+1,inNDatas,inDstVarDataStore.getOffsetStore());
    inDstVarDataStore.getOffsetStore().addToVars(theDstSize+1,inNDatas,theDelta);
  }

  public void getVarDataStore(VarDataStore inDstVarDataStore) {
    getVarDataStore(0,getSize(),inDstVarDataStore); }

//--------------------------------------------------------------------------------------------------------
// getVarDataRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarDataRAMStore getVarDataRAMStore(long inIndex, long inNDatas) {
    long theDataSize=getOffset(inIndex+inNDatas)-getOffset(inIndex);
    VarDataRAMStore theVarDataRAMStore=new VarDataRAMStore(getVarSize(),inNDatas,theDataSize);
    getVarDataStore(inIndex,inNDatas,theVarDataRAMStore);
    return theVarDataRAMStore;
  }

  public VarDataRAMStore getVarDataRAMStore() { return getVarDataRAMStore(0,getSize()); }

//--------------------------------------------------------------------------------------------------------
// setVarAtN
//--------------------------------------------------------------------------------------------------------

  public void setVarAtN(long inIndex, long inN, long inLong) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN>=getNVars(inIndex))        
        throw new StoreException("N past end of row: "+inN+" >= "+getNVars(inIndex));
    }
    getVarStore().setVar(getOffset(inIndex)+inN,inLong); 
  }

//--------------------------------------------------------------------------------------------------------
// setVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void setVarsAtN(long inIndex, long inN, long[] inLongs, int inLongDelta, int inNLongs) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inLongDelta<0)
        throw new StoreException("Negative LongDelta: "+inLongDelta);
      if (inNLongs<0)
        throw new StoreException("Negative NLongs: "+inNLongs);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN+inNLongs>getNVars(inIndex))
        throw new StoreException("N+NLongs past end of row: "+inN+"+"+inNLongs+" > "+getNVars(inIndex));
      if (inLongDelta+inNLongs>inLongs.length)
        throw new StoreException("LongDelta+NLongs past end: "+inLongDelta+"+"+inNLongs+">"+inLongs.length);
    }
    getVarStore().setVars(getOffset(inIndex)+inN,inLongs,inLongDelta,inNLongs); 
  }

  public void setVarsAtN(long inIndex, long inN, int[] inInts, int inIntDelta, int inNInts) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIntDelta<0)
        throw new StoreException("Negative IntDelta: "+inIntDelta);
      if (inNInts<0)
        throw new StoreException("Negative NInts: "+inNInts);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN+inNInts>getNVars(inIndex))
        throw new StoreException("N+NInts past end of row: "+inN+"+"+inNInts+" > "+getNVars(inIndex));
      if (inIntDelta+inNInts>inInts.length)
        throw new StoreException("IntDelta+NInts past end: "+inIntDelta+"+"+inNInts+">"+inInts.length);
    }
    getVarStore().setVars(getOffset(inIndex)+inN,inInts,inIntDelta,inNInts); 
  }

//--------------------------------------------------------------------------------------------------------
// setVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void setVarsAtN(long inIndex, long inN, long[] ioLongs) {
    setVarsAtN(inIndex,inN,ioLongs,0,ioLongs.length); }

  public void setVarsAtN(long inIndex, long inN, int[] ioInts) {
    setVarsAtN(inIndex,inN,ioInts,0,ioInts.length); }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inIndex, long[] inLongs, int inLongDelta, int inNLongs) {
    setVarsAtN(inIndex,0,inLongs,inLongDelta,inNLongs); }

  public void setVars(long inIndex, int[] inInts, int inIntDelta, int inNInts) {
    setVarsAtN(inIndex,0,inInts,inIntDelta,inNInts); }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inIndex, long[] inLongs) { 
    setVars(inIndex,inLongs,0,inLongs.length); }
  
  public void setVars(long inIndex, int[] inInts) { 
    setVars(inIndex,inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// setVarStore
//--------------------------------------------------------------------------------------------------------

  public void setVarStore(long inIndex, VarStore inVarStore) {
    long theOffset=getOffset(inIndex);
    getVarStore().setVarStore(theOffset,inVarStore);
  }

//--------------------------------------------------------------------------------------------------------
// addToVarAtN
//--------------------------------------------------------------------------------------------------------

  public void addToVarAtN(long inIndex, long inN, long inLong) {
    if (kRangeChecking) {
      if (inIndex<0)
        throw new StoreException("Negative index: "+inIndex);
      if (inN<0)
        throw new StoreException("Negative N: "+inN);
      if (inIndex>=getSize())
        throw new StoreException("Index past end: "+inIndex+">="+getSize());
      if (inN>=getNVars(inIndex))        
        throw new StoreException("N past end of row: "+inN+" >= "+getNVars(inIndex));
    }
    getVarStore().addToVar(getOffset(inIndex)+inN,inLong); 
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long inLong, long inNCopies) {
    getVarStore().appendVars(inLong,inNCopies); 
    getOffsetStore().appendVar(getDataSize());
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs, int inLongDelta, int inNLongs) {
    getVarStore().appendVars(inLongs,inLongDelta,inNLongs); 
    getOffsetStore().appendVar(getDataSize());
  }

  public void appendVars(int[] inInts, int inIntDelta, int inNInts) {
    getVarStore().appendVars(inInts,inIntDelta,inNInts); 
    getOffsetStore().appendVar(getDataSize());
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs) { appendVars(inLongs,0,inLongs.length); }
  public void appendVars(int[] inInts) { appendVars(inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// appendVarStore
//--------------------------------------------------------------------------------------------------------

  public void appendVarStore(VarStore inSrcVarStore, long inSrcOffset, long inNLongs) {
    getVarStore().appendVarStore(inSrcVarStore,inSrcOffset,inNLongs);
    getOffsetStore().appendVar(getDataSize());
  }

  public void appendVarStore(VarStore inSrcVarStore) {
    appendVarStore(inSrcVarStore,0,inSrcVarStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// appendVarDataStore
//--------------------------------------------------------------------------------------------------------

  public void appendVarDataStore(VarDataStore inSrcVarDataStore, long inSrcOffset, long inNDatas) {
    if (inNDatas>0) {
      long theDstSize=getSize();
      long theDstDataSize=getDataSize();
      long theSrcLongOffset=inSrcVarDataStore.getOffset(inSrcOffset);
      long theSrcNLongs=inSrcVarDataStore.getOffset(inSrcOffset+inNDatas)-theSrcLongOffset;
      getVarStore().appendVarStore(inSrcVarDataStore.getVarStore(),theSrcLongOffset,theSrcNLongs);
      VarStore theOffsetStore=getOffsetStore();
      theOffsetStore.truncateBy(1);
      theOffsetStore.appendVarStore(inSrcVarDataStore.getOffsetStore(),inSrcOffset,inNDatas+1);
      long theDelta=theDstDataSize-theSrcLongOffset;
      theOffsetStore.addToVars(theDstSize,inNDatas+1,theDelta);
    }
  }
  
  public void appendVarDataStore(VarDataStore inSrcVarDataStore) {
    appendVarDataStore(inSrcVarDataStore,0,inSrcVarDataStore.getSize()); }

//--------------------------------------------------------------------------------------------------------
// getSortMap
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getSortMap(boolean inDescending) { return SortMapUtils.sortMap(this,inDescending); }

  public VarRAMStore getSortMap() { return getSortMap(false); }

}

