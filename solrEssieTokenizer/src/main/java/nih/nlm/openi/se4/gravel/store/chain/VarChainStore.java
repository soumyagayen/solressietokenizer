//--------------------------------------------------------------------------------------------------------
// VarChainStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.chain;

import gravel.store.data.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// VarChainStore
//--------------------------------------------------------------------------------------------------------

public abstract class VarChainStore extends ChainStore { 

//--------------------------------------------------------------------------------------------------------
// VarChainStore - create
//--------------------------------------------------------------------------------------------------------

  protected VarChainStore(VarStore inLinkStore, VarStore inNextStore, 
      VarStore inLastStore, VarStore inNStore) {
    super(inLinkStore,inNextStore,inLastStore,inNStore); }

//--------------------------------------------------------------------------------------------------------
// getLinkStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getLinkStore() { return (VarStore) super.getLinkStore(); }

//--------------------------------------------------------------------------------------------------------
// dangerousGetLinkStore
//--------------------------------------------------------------------------------------------------------

  public VarStore dangerousGetLinkStore() { return (VarStore) super.getLinkStore(); }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public VarStore unwrap() { return (VarStore) super.unwrap(); }

//--------------------------------------------------------------------------------------------------------
// getVarSize
//--------------------------------------------------------------------------------------------------------

  public int getVarSize() { return getLinkStore().getVarSize(); }

//--------------------------------------------------------------------------------------------------------
// setVarSize
//--------------------------------------------------------------------------------------------------------

  public void setVarSize(int inVarSize) { getLinkStore().setVarSize(inVarSize); }

//--------------------------------------------------------------------------------------------------------
// getNVars
//--------------------------------------------------------------------------------------------------------

  public long getNVars(long inChainDx) { return getNInChain(inChainDx); }

//--------------------------------------------------------------------------------------------------------
// getMaxNVars
//--------------------------------------------------------------------------------------------------------

  public long getMaxNVars() { return getMaxNInChains(); }

//--------------------------------------------------------------------------------------------------------
// getVar
//--------------------------------------------------------------------------------------------------------

  public long getLong(long inIndex) { return getLinkStore().getLong(inIndex); }

  public int getInt(long inIndex) { return getLinkStore().getInt(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getLastVar
//--------------------------------------------------------------------------------------------------------

  public long getLastLong(long inChainDx) { 
    long theLastIndex=getLastIndex(inChainDx);
    if (theLastIndex==kNotFound)
      return kNotFound;
    else
      return getLinkStore().getLong(theLastIndex); 
  }
  
  public int getLastInt(long inChainDx) { 
    long theLastIndex=getLastIndex(inChainDx);
    if (theLastIndex==kNotFound)
      return kNotFound;
    else
      return getLinkStore().getInt(theLastIndex); 
  }

//--------------------------------------------------------------------------------------------------------
// getFirstVar
//--------------------------------------------------------------------------------------------------------

  public long getFirstLong(long inChainDx) {
    return getLinkStore().getLong(getFirstIndex(inChainDx)); }

  public int getFirstInt(long inChainDx) {
    return getLinkStore().getInt(getFirstIndex(inChainDx)); }

//--------------------------------------------------------------------------------------------------------
// slowGetVarAtN
//--------------------------------------------------------------------------------------------------------

  public long slowGetLongAtN(long inChainDx, long inN) {
    if ((kRangeChecking)&&(inN>=getNVars(inChainDx)))
      throw new StoreException("Get past end of chain: "+inN+" >= "+getNVars(inChainDx));
    return getLinkStore().getLong(slowGetIndexAtN(inChainDx,inN));
  }

  public int slowGetIntAtN(long inChainDx, long inN) {
    if ((kRangeChecking)&&(inN>=getNVars(inChainDx)))
      throw new StoreException("Get past end of chain: "+inN+" >= "+getNVars(inChainDx));
    return getLinkStore().getInt(slowGetIndexAtN(inChainDx,inN));
  }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public int getVars(long inChainDx, long[] ioLongs, int inLongDelta) {
    VarStore theVarStore=getLinkStore();
    int theNLongs=(int) getNVars(inChainDx);
    long theIndex=getFirstIndex(inChainDx);
    for (int i=0; i<theNLongs; i++) {
      ioLongs[inLongDelta+i]=theVarStore.getLong(theIndex);
      theIndex=getNextIndex(theIndex);
    }
    return theNLongs;
  }

  public int getVars(long inChainDx, int[] ioInts, int inIntDelta) {
    VarStore theVarStore=getLinkStore();
    int theNInts=(int) getNVars(inChainDx);
    long theIndex=getFirstIndex(inChainDx);
    for (int i=0; i<theNInts; i++) {
      ioInts[inIntDelta+i]=theVarStore.getInt(theIndex);
      theIndex=getNextIndex(theIndex);
    }
    return theNInts;
  }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public int getVars(long inChainDx, long[] ioLongs) { return getVars(inChainDx,ioLongs,0); }

  public int getVars(long inChainDx, int[] ioInts) { return getVars(inChainDx,ioInts,0); }

//--------------------------------------------------------------------------------------------------------
// getVars
//--------------------------------------------------------------------------------------------------------

  public long[] getLongs(long inChainDx) {
    long[] theLongs=Allocate.newLongs((int) getNVars(inChainDx));
    getVars(inChainDx,theLongs); 
    return theLongs;
  }

  public int[] getInts(long inChainDx) {
    int[] theInts=Allocate.newInts((int) getNVars(inChainDx));
    getVars(inChainDx,theInts); 
    return theInts;
  }

//--------------------------------------------------------------------------------------------------------
// getAllVarss
//--------------------------------------------------------------------------------------------------------

  public long[][] getAllLongss() {
    long[][] theLongss=new long[(int) getSize()][];
    for (int i=0; i<theLongss.length; i++)
      theLongss[i]=getLongs(i); 
    return theLongss;
  }

  public int[][] getAllIntss() {
    int[][] theIntss=new int[(int) getSize()][];
    for (int i=0; i<theIntss.length; i++)
      theIntss[i]=getInts(i); 
    return theIntss;
  }

//--------------------------------------------------------------------------------------------------------
// slowGetVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void slowGetVarsAtN(long inChainDx, long inN, long[] ioLongs, int inLongDelta, int inNLongs) {
    if (inNLongs==0)
      return;
    VarStore theVarStore=getLinkStore();
    long theIndex=slowGetIndexAtN(inChainDx,inN);
    ioLongs[inLongDelta]=theVarStore.getLong(theIndex);
    long theFirstIndex=getFirstIndex(inChainDx);
    for (int i=1; i<inNLongs; i++) {
      theIndex=getNextIndex(theIndex);
      if (theIndex==theFirstIndex)
        throw new StoreException("Past chain end");
      ioLongs[inLongDelta+i]=theVarStore.getLong(theIndex);
    }
  }

  public void slowGetVarsAtN(long inChainDx, long inN, int[] ioInts, int inIntDelta, int inNInts) {
    if (inNInts==0)
      return;
    VarStore theVarStore=getLinkStore();
    long theIndex=slowGetIndexAtN(inChainDx,inN);
    ioInts[inIntDelta]=theVarStore.getInt(theIndex);
    long theFirstIndex=getFirstIndex(inChainDx);
    for (int i=1; i<inNInts; i++) {
      theIndex=getNextIndex(theIndex);
      if (theIndex==theFirstIndex)
        throw new StoreException("Past chain end");
      ioInts[inIntDelta+i]=theVarStore.getInt(theIndex);
    }
  }

//--------------------------------------------------------------------------------------------------------
// slowGetVarsAtN
//--------------------------------------------------------------------------------------------------------

  public long[] slowGetLongsAtN(long inChainDx, long inN, int inNLongs) { 
    long[] theLongs=Allocate.newLongs(inNLongs);
    slowGetVarsAtN(inChainDx,inN,theLongs,0,inNLongs); 
    return theLongs;
  }

  public int[] slowGetIntsAtN(long inChainDx, long inN, int inNInts) { 
    int[] theInts=Allocate.newInts(inNInts);
    slowGetVarsAtN(inChainDx,inN,theInts,0,inNInts); 
    return theInts;
  }

//--------------------------------------------------------------------------------------------------------
// getVarStore
//--------------------------------------------------------------------------------------------------------

  public long getVarStore(long inChainDx, VarStore inDstVarStore) {
    long theNLongs=getNVars(inChainDx);    
    if (theNLongs>0) {
      inDstVarStore.ensureCapacity(inDstVarStore.getSize()+theNLongs);
      if (theNLongs==1) 
        inDstVarStore.appendVar(getFirstLong(inChainDx));
      else {
        SliceStore theSliceStore=SliceStore.getSliceStore();
        long[] theLongSlice=theSliceStore.getLongSlice();
        long theNFullSlices=SliceStore.getNFullLongSlices(theNLongs);
        int theRemainder=SliceStore.getLongRemainder(theNLongs);
        long theIndex=getFirstIndex(inChainDx);
        for (long i=0; i<theNFullSlices; i++) {
          for (int j=0; j<SliceStore.kLongSliceSize; j++) {
            theLongSlice[j]=getLong(theIndex);
            theIndex=getNextIndex(theIndex);
          }
          inDstVarStore.appendVars(theLongSlice,0,SliceStore.kLongSliceSize);
        }
        if (theRemainder>0) {
          for (int j=0; j<theRemainder; j++) {
            theLongSlice[j]=getLong(theIndex);
            theIndex=getNextIndex(theIndex);
          }
          inDstVarStore.appendVars(theLongSlice,0,theRemainder);
        }
        theSliceStore.putLongSlice(theLongSlice);
      }
    }
    return theNLongs;
  }

//--------------------------------------------------------------------------------------------------------
// getVarRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarRAMStore getVarRAMStore(long inChainDx) {
    VarRAMStore theVarRAMStore=new VarRAMStore(getNVars(inChainDx));
    getVarStore(inChainDx,theVarRAMStore);
    theVarRAMStore.compact();
    return theVarRAMStore;
  }

//--------------------------------------------------------------------------------------------------------
// getVarDataStore
//--------------------------------------------------------------------------------------------------------

  public void getVarDataStore(VarDataStore inDstVarDataStore) {
    long theSize=getSize();
    if (theSize>0) {
      long theLinkSize=getLinkSize();
      VarStore theVarStore=inDstVarDataStore.dangerousGetVarStore();
      theVarStore.ensureCapacity(theVarStore.getSize()+theLinkSize);
      VarStore theOffsetStore=inDstVarDataStore.dangerousGetOffsetStore();
      theOffsetStore.ensureCapacity(theOffsetStore.getSize()+theSize);
      SliceStore theSliceStore=SliceStore.getSliceStore();
      long[] theLongSlice=theSliceStore.getLongSlice();
      int n=0;
      long theOffset=0;
      for (long i=0; i<theSize; i++) {
        long theNLongs=getNVars(i);    
        theOffset+=theNLongs;
        theOffsetStore.appendVar(theOffset);
        long theIndex=getFirstIndex(i);
        for (long j=0; j<theNLongs; j++) {
          theLongSlice[n]=getLong(theIndex);
          n++;
          if (n==theLongSlice.length) {
            theVarStore.appendVars(theLongSlice,0,theLongSlice.length);
            n=0;
          }
          theIndex=getNextIndex(theIndex);
        }
      }
      if (n>0) {
        theVarStore.appendVars(theLongSlice,0,n);
        n=0;
      }
      theSliceStore.putLongSlice(theLongSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getVarDataRAMStore
//--------------------------------------------------------------------------------------------------------

  public VarDataRAMStore getVarDataRAMStore() {
    VarDataRAMStore theVarDataRAMStore=new VarDataRAMStore(getVarSize(),getSize(),getLinkSize());
    getVarDataStore(theVarDataRAMStore);
    theVarDataRAMStore.compact();
    return theVarDataRAMStore;
  }

//--------------------------------------------------------------------------------------------------------
// setVar
//--------------------------------------------------------------------------------------------------------

  public void setVar(long inIndex, long inLong) { getLinkStore().setVar(inIndex,inLong); }

//--------------------------------------------------------------------------------------------------------
// slowSetVarAtN
//--------------------------------------------------------------------------------------------------------

  public void slowSetVarAtN(long inChainDx, long inN, long inLong) {
    if ((kRangeChecking)&&(inN>=getNVars(inChainDx)))
      throw new StoreException("Set past end of chain: "+inN+" >= "+getNVars(inChainDx));
    setVar(slowGetIndexAtN(inChainDx,inN),inLong); 
  }

//--------------------------------------------------------------------------------------------------------
// setVarsAtIndex
//--------------------------------------------------------------------------------------------------------

  public void setVarsAtIndex(long inChainDx, long inIndex, long[] inLongs, int inLongDelta, int inNLongs) {
    if (inNLongs==0)
      return;
    VarStore theVarStore=getLinkStore();
    long theIndex=inIndex;
    theVarStore.setVar(theIndex,inLongs[inLongDelta]);
    long theFirstIndex=getFirstIndex(inChainDx);
    for (int i=1; i<inNLongs; i++) {
      theIndex=getNextIndex(theIndex);
      if (theIndex==theFirstIndex)
        throw new StoreException("Past chain end");
      theVarStore.setVar(theIndex,inLongs[inLongDelta+i]);
    }
  }

  public void setVarsAtIndex(long inChainDx, long inIndex, int[] inInts, int inIntDelta, int inNInts) {
    if (inNInts==0)
      return;
    VarStore theVarStore=getLinkStore();
    long theIndex=inIndex;
    theVarStore.setVar(theIndex,inInts[inIntDelta]);
    long theFirstIndex=getFirstIndex(inChainDx);
    for (int i=1; i<inNInts; i++) {
      theIndex=getNextIndex(theIndex);
      if (theIndex==theFirstIndex)
        throw new StoreException("Past chain end");
      theVarStore.setVar(theIndex,inInts[inIntDelta+i]);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setVarsAtIndex
//--------------------------------------------------------------------------------------------------------

  public void setVarsAtIndex(long inChainDx, long inIndex, long[] inLongs) { 
    setVarsAtIndex(inChainDx,inIndex,inLongs,0,inLongs.length); }

  public void setVarsAtIndex(long inChainDx, long inIndex, int[] inInts) { 
    setVarsAtIndex(inChainDx,inIndex,inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inChainDx, long[] inLongs, int inLongDelta, int inNLongs) {
    setVarsAtIndex(inChainDx,getFirstIndex(inChainDx),inLongs,inLongDelta,inNLongs); }

  public void setVars(long inChainDx, int[] inInts, int inIntDelta, int inNInts) {
    setVarsAtIndex(inChainDx,getFirstIndex(inChainDx),inInts,inIntDelta,inNInts); }

//--------------------------------------------------------------------------------------------------------
// setVars
//--------------------------------------------------------------------------------------------------------

  public void setVars(long inChainDx, long[] inLongs) { 
    setVars(inChainDx,inLongs,0,inLongs.length); }

  public void setVars(long inChainDx, int[] inInts) { 
    setVars(inChainDx,inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// slowSetVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void slowSetVarsAtN(long inChainDx, long inN, long[] inLongs, int inLongDelta, int inNLongs) {
    if ((kRangeChecking)&&(inN+inNLongs>getNVars(inChainDx)))
      throw new StoreException("Set past end of chain: "+(inN+inNLongs)+" > "+getNVars(inChainDx));
    setVarsAtIndex(inChainDx,slowGetIndexAtN(inChainDx,inN),inLongs,inLongDelta,inNLongs); 
  }

  public void slowSetVarsAtN(long inChainDx, long inN, int[] inInts, int inIntDelta, int inNInts) {
    if ((kRangeChecking)&&(inN+inNInts>getNVars(inChainDx)))
      throw new StoreException("Set past end of chain: "+(inN+inNInts)+" > "+getNVars(inChainDx));
    setVarsAtIndex(inChainDx,slowGetIndexAtN(inChainDx,inN),inInts,inIntDelta,inNInts); 
  }

//--------------------------------------------------------------------------------------------------------
// slowSetVarsAtN
//--------------------------------------------------------------------------------------------------------

  public void slowSetVarsAtN(long inChainDx, long inN, long[] ioLongs) {
    slowSetVarsAtN(inChainDx,inN,ioLongs,0,ioLongs.length); }

  public void slowSetVarsAtN(long inChainDx, long inN, int[] ioInts) {
    slowSetVarsAtN(inChainDx,inN,ioInts,0,ioInts.length); }

//--------------------------------------------------------------------------------------------------------
// addToVar
//--------------------------------------------------------------------------------------------------------

  public void addToVar(long inIndex, long inLong) { getLinkStore().addToVar(inIndex,inLong); }

//--------------------------------------------------------------------------------------------------------
// slowAddToVarAtN
//--------------------------------------------------------------------------------------------------------

  public void slowAddToVarAtN(long inChainDx, long inN, long inLong) {
    if ((kRangeChecking)&&(inN>=getNVars(inChainDx)))
      throw new StoreException("AddTo past end of chain: "+inN+" >= "+getNVars(inChainDx));
    getLinkStore().addToVar(slowGetIndexAtN(inChainDx,inN),inLong); 
  }

//--------------------------------------------------------------------------------------------------------
// setNewVar
//--------------------------------------------------------------------------------------------------------

  private void setNewVar(long inIndex, long inLong) {
    long theNLongs=getLinkStore().getSize();
    if (inIndex<theNLongs)
      getLinkStore().setVar(inIndex,inLong); 
    else if (inIndex==theNLongs)
      getLinkStore().appendVar(inLong);
    else
      throw new StoreException("Corrupted chains");
  }

//--------------------------------------------------------------------------------------------------------
// prependVar
//--------------------------------------------------------------------------------------------------------

  public void prependVar(long inChainDx, long inLong) { setNewVar(prependIndex(inChainDx),inLong); }

//--------------------------------------------------------------------------------------------------------
// appendVar
//--------------------------------------------------------------------------------------------------------

  public void appendVar(long inChainDx, long inLong) { setNewVar(appendIndex(inChainDx),inLong); }
 
//--------------------------------------------------------------------------------------------------------
// slowFindVarIndex
//--------------------------------------------------------------------------------------------------------

  public long slowFindVarIndex(long inChainDx, long inLong) {
    VarStore theVarStore=getLinkStore();
    long theNLongs=getNVars(inChainDx);    
    long theIndex=getFirstIndex(inChainDx);
    for (long i=0; i<theNLongs; i++) {
      if (theVarStore.getLong(theIndex)==inLong) 
        return theIndex;
      theIndex=getNextIndex(theIndex);
    }
    return kNotFound;
  }

//--------------------------------------------------------------------------------------------------------
// slowAppendUniqVar
//--------------------------------------------------------------------------------------------------------

  public long slowAppendUniqVar(long inChainDx, long inLong) {
    long theIndex=slowFindVarIndex(inChainDx,inLong);
    if (theIndex==kNotFound)
      appendVar(inChainDx,inLong);
    return theIndex;
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long inChainDx, long[] inLongs, int inLongDelta, int inNLongs) {
    for (int i=0; i<inNLongs; i++)
      appendVar(inChainDx,inLongs[inLongDelta+i]);
  }

  public void appendVars(long inChainDx, int[] inInts, int inIntDelta, int inNInts) {
    for (int i=0; i<inNInts; i++)
      appendVar(inChainDx,inInts[inIntDelta+i]);
  }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long inChainDx, long[] inLongs) {
    appendVars(inChainDx,inLongs,0,inLongs.length); }

  public void appendVars(long inChainDx, int[] inInts) {
    appendVars(inChainDx,inInts,0,inInts.length); }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs, int inLongDelta, int inNLongs) {
    appendVars(appendChain(),inLongs,inLongDelta,inNLongs); }

  public void appendVars(int[] inInts, int inIntDelta, int inNInts) {
    appendVars(appendChain(),inInts,inIntDelta,inNInts); }

//--------------------------------------------------------------------------------------------------------
// appendVars
//--------------------------------------------------------------------------------------------------------

  public void appendVars(long[] inLongs) { appendVars(getSize(),inLongs); }
  public void appendVars(int[] inInts) { appendVars(getSize(),inInts); }

//--------------------------------------------------------------------------------------------------------
// appendVarDataStore
//--------------------------------------------------------------------------------------------------------

  public void appendVarDataStore(VarDataStore inVarDataStore, boolean inNewChains) {
    long theSize=inVarDataStore.getSize();
    if (!inNewChains)
      ensureChains(theSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theLongSlice=theSliceStore.getLongSlice();
    for (long i=0; i<theSize; i++) {
      long theChainDx=i;
      if (inNewChains)
        theChainDx=appendChain();
      int theNLongs=(int) inVarDataStore.getNVars(i);
      if (theNLongs>theLongSlice.length) {
        theSliceStore.putLongSlice(theLongSlice);  
        theLongSlice=new long[theNLongs*2+1];
      }
      inVarDataStore.getVars(i,theLongSlice,0);
      appendVars(theChainDx,theLongSlice,0,theNLongs);
    }
    theSliceStore.putLongSlice(theLongSlice);
  }
  
//--------------------------------------------------------------------------------------------------------
// insertNextVar
//--------------------------------------------------------------------------------------------------------

  public void insertNextVar(long inChainDx, long inIndex, long inLong) {
    setNewVar(insertNextIndex(inChainDx,inIndex),inLong); }

//--------------------------------------------------------------------------------------------------------
// slowInsertVarAtN
//--------------------------------------------------------------------------------------------------------

  public void slowInsertVarAtN(long inChainDx, long inN, long inLong) {
    if ((kRangeChecking)&&(inN>getNVars(inChainDx)))
      throw new StoreException("Insert past end of chain: "+inN+" > "+getNVars(inChainDx));
    setNewVar(slowInsertAtN(inChainDx,inN),inLong); 
  }

//--------------------------------------------------------------------------------------------------------
// removeFirstVar
//--------------------------------------------------------------------------------------------------------

  public long removeFirstVar(long inChainDx) {
    return getLinkStore().getLong(removeFirstIndex(inChainDx)); }

//--------------------------------------------------------------------------------------------------------
// removeNextVar
//--------------------------------------------------------------------------------------------------------

  public long removeNextVar(long inChainDx, long inIndex) {
    return getLinkStore().getLong(removeNextIndex(inChainDx,inIndex)); }

//--------------------------------------------------------------------------------------------------------
// slowRemoveVarAtN
//--------------------------------------------------------------------------------------------------------

  public long slowRemoveVarAtN(long inChainDx, long inN) {
    if ((kRangeChecking)&&(inN>=getNVars(inChainDx)))
      throw new StoreException("Remove past end of chain: "+inN+" >= "+getNVars(inChainDx));
    return getLinkStore().getLong(slowRemoveIndexAtN(inChainDx,inN));
  }

//--------------------------------------------------------------------------------------------------------
// slowFindAndRemoveVar
//--------------------------------------------------------------------------------------------------------

  public long slowFindAndRemoveVar(long inChainDx, long inLong) {
    VarStore theVarStore=getLinkStore();
    long theNLongs=getNVars(inChainDx);    
    long theIndex=getFirstIndex(inChainDx);
    if (theIndex!=kNotFound) {
      if (theVarStore.getLong(theIndex)==inLong) 
        return removeFirstVar(inChainDx);
      for (long i=1; i<theNLongs; i++) {
        long theNextIndex=getNextIndex(theIndex);
        if (theVarStore.getLong(theNextIndex)==inLong) 
          return removeNextVar(inChainDx,theIndex);
        theIndex=theNextIndex;
      }
    }
    return kNotFound;
  }

//--------------------------------------------------------------------------------------------------------
// removeVarsInChain
//--------------------------------------------------------------------------------------------------------

  public void removeVarsInChain(long inChainDx) { removeAllInChain(inChainDx); }

}

