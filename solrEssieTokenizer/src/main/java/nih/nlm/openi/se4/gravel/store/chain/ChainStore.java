//--------------------------------------------------------------------------------------------------------
// ChainStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.chain;

import gravel.store.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// ChainStore
//
// This store is a wrapper that supports adding content to many lists (called chains) simultaneously.
// Chains consist of links.  Link values are kept in the nested store.
// The nested store is a simple list that supports append
//
// Some gymnastics implements an extra chain at index 0 which holds deleted (freed) links
//
// Each link has a ptr to the next link in its chain
// The last link in the chail points back to the first, making a loop
// There is an external link to the last link in each chain
// If a chain is empty, the external link is kNotFound = -1
// There is a count of links in each chain
//
// New links are created by
//   1) If free chain external link is kNotFound, there are no links in the free chain
//        grow nested store and set of next ptrs by 1 and use new slot at end as new link
//      else there are links in the free chain, use the first free link
//        Decrement number of links in free chain
//        External link points to last link and last link points to first link
//        If first and last link are the same, this is the only link in the free chain
//          Change free chain external link kNotFound and use first link as new link
//        else there are multiple links in the free chain
//          Set last link to point past first link to first link's next link (i.e. second link)
//        The newly created link is what was the free chain's first link
//
// New links are prepended to a chain by
//   1) Increment number of links in chain
//   2) If external link to chain is kNotFound, no links in chain
//        Set external link to point to new link
//        Set new link to point to itself
//      else chain has links
//        External link points to last link and last link points to first link
//        Change new link to point to first link
//        Change last link to point to new link, making new link first link in chain
//
// New links are appended to a chain by
//   1) prepend link to chain
//   2) set external link to point to prepended link, making it the last link in chain
//
// Links are added to the free chain by
//   1) Increment number of links in free chain
//   2) If external link to free chain's last link is kNotFound, free chain is empty
//        set added link's next link to point to itself
//      else free chain has links, so insert link at end
//        set added link to point at last links next link
//        set last link's next link to point at added link
//      Set free chain external link to added link, making it the last link in the free chain
//
// Links can be removed if you know the chain, the link, and the prev link
//   1) Decrement the number of links in chain
//   2) If the link and the prev link are the same, this is the only link in the chain
//        Change external link to kNotFound to show chain is empty
//      else there are multiple links in chain
//        Set prev link to point past link to link's next link
//        If link was last link, set external link to prev link
//   3) Add removed link to free chain
//
// First links are easy to remove because you can quickly find them and their prev links
// Removing other links requires walking down the chain to find their prev link
//--------------------------------------------------------------------------------------------------------

public abstract class ChainStore extends NestedStore {

//--------------------------------------------------------------------------------------------------------
// ChainStore consts
//--------------------------------------------------------------------------------------------------------

  public static final String       kNextExtension="Next";
  public static final String       kLastExtension="Last";
  public static final String       kNExtension="N";

//--------------------------------------------------------------------------------------------------------
// ChainStore vars
//--------------------------------------------------------------------------------------------------------

  private VarStore     mNextStore;  // Ptr for every link to next link in chain
  private VarStore     mLastStore;  // Ptr for every chain to last link in chain
  private VarStore     mNStore;     // N links for every chain

//--------------------------------------------------------------------------------------------------------
// ChainStore - create
//--------------------------------------------------------------------------------------------------------

  protected ChainStore(StoreInterface inLinkStore, VarStore inNextStore, 
      VarStore inLastStore, VarStore inNStore) {
    super(inLinkStore);
    mNextStore=inNextStore;
    mLastStore=inLastStore;
    mNStore=inNStore;
    if (mLastStore.getSize()==0)
      mLastStore.appendVar(kNotFound);
    if (mNStore.getSize()==0)
      mNStore.appendVar(0);
    if (kRangeChecking) 
      sanityCheck();
  }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    super.close();
    if (mNextStore!=null) {
      mNextStore.close();
      mNextStore=null;
    }
    if (mLastStore!=null) {
      mLastStore.close();
      mLastStore=null;
    }
    if (mNStore!=null) {
      mNStore.close();
      mNStore=null;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected StoreInterface getLinkStore() { return getInnerStore(); }

//--------------------------------------------------------------------------------------------------------
// getNextStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getNextStore() { return mNextStore; }

//--------------------------------------------------------------------------------------------------------
// getLastStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getLastStore() { return mLastStore; }

//--------------------------------------------------------------------------------------------------------
// getNStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getNStore() { return mNStore; }

//--------------------------------------------------------------------------------------------------------
// makeNextFilename
//--------------------------------------------------------------------------------------------------------

  public static String makeNextFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kNextExtension); }

//--------------------------------------------------------------------------------------------------------
// makeLastFilename
//--------------------------------------------------------------------------------------------------------

  public static String makeLastFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kLastExtension); }

//--------------------------------------------------------------------------------------------------------
// makeNFilename
//--------------------------------------------------------------------------------------------------------

  public static String makeNFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kNExtension); }

//--------------------------------------------------------------------------------------------------------
// deleteStore
//--------------------------------------------------------------------------------------------------------

  public static void deleteStore(String inFilename) {
    ByteStore.deleteStore(inFilename); 
    ByteStore.deleteStore(makeNextFilename(inFilename)); 
    ByteStore.deleteStore(makeLastFilename(inFilename)); 
    ByteStore.deleteStore(makeNFilename(inFilename)); 
  }

//--------------------------------------------------------------------------------------------------------
// getSize
//--------------------------------------------------------------------------------------------------------

  public long getSize() { return mLastStore.getSize()-1; }

//--------------------------------------------------------------------------------------------------------
// getLinkSize
//--------------------------------------------------------------------------------------------------------

  public long getLinkSize() { return mNextStore.getSize(); }

//--------------------------------------------------------------------------------------------------------
// getCapacity
//--------------------------------------------------------------------------------------------------------

  public long getCapacity() { return mLastStore.getCapacity()-1; }

//--------------------------------------------------------------------------------------------------------
// getLinkCapacity
//--------------------------------------------------------------------------------------------------------

  public long getLinkCapacity() { return mNextStore.getCapacity(); }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() { 
    return super.getMemory()+3*kReferenceMemory+
        mNextStore.getMemory()+mLastStore.getMemory()+mNStore.getMemory(); }

//--------------------------------------------------------------------------------------------------------
// setSize  (i.e. set NChains)
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inNewSize) { 
    long theOldSize=getSize();
    mLastStore.setSize(inNewSize+1); 
    mNStore.setSize(inNewSize+1); 
    if (inNewSize>theOldSize) {
      mLastStore.setVars(theOldSize+1,kNotFound,inNewSize-theOldSize);
      mNStore.setVars(theOldSize+1,0,inNewSize-theOldSize);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) {
    throw new StoreException("Cannot get hash for chain store"); }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public void clear() { 
    super.clear();
    mNextStore.clear();
    mLastStore.clear();
    mNStore.clear();
    // Add back in free chain
    mLastStore.appendVar(kNotFound);
    mNStore.appendVar(0);
  }

//--------------------------------------------------------------------------------------------------------
// setCapacity  (in NChains)
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) { 
    mLastStore.setCapacity(inCapacity+1); 
    mNStore.setCapacity(inCapacity+1); 
  }

//--------------------------------------------------------------------------------------------------------
// ensureCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureCapacity(long inSize) { 
    mLastStore.ensureCapacity(inSize+1); 
    mNStore.ensureCapacity(inSize+1); 
  }

//--------------------------------------------------------------------------------------------------------
// compact
//--------------------------------------------------------------------------------------------------------

  public void compact() { 
    super.compact();
    mNextStore.compact();
    mLastStore.compact(); 
    mNStore.compact(); 
  }

//--------------------------------------------------------------------------------------------------------
// getNFreeIndexes
//--------------------------------------------------------------------------------------------------------

  long getNFreeIndexes() { return mNStore.getLong(0); }  // Free chain is at index 0

//--------------------------------------------------------------------------------------------------------
// getNInChain
//--------------------------------------------------------------------------------------------------------

  public long getNInChain(long inChainDx) { 
    if (inChainDx>=getSize())
      return 0;
    long theNLinks=mNStore.getLong(inChainDx+1); 
    if ((kRangeChecking)&&(theNLinks<0))
      throw new StoreException("Negative NLinks in chain: "+theNLinks);
    return theNLinks;
  }

//--------------------------------------------------------------------------------------------------------
// getMaxNInChains
//--------------------------------------------------------------------------------------------------------

  public long getMaxNInChains() {
    long theMaxNInChains=0;
    long theSize=getSize();
    for (long i=0; i<=theSize; i++) 
      theMaxNInChains=Math.max(theMaxNInChains,getNInChain(i));
    return theMaxNInChains;
  }

//--------------------------------------------------------------------------------------------------------
// getTotalNInChains  (i.e. NLinks)
//--------------------------------------------------------------------------------------------------------

  public long getTotalNInChains() { return mNextStore.getSize()-getNFreeIndexes(); }  

//--------------------------------------------------------------------------------------------------------
// getNextIndex
//--------------------------------------------------------------------------------------------------------

  public long getNextIndex(long inIndex) { 
    if (inIndex==kNotFound)
      throw new StoreException("Index not found");
    return mNextStore.getLong(inIndex); 
  }

//--------------------------------------------------------------------------------------------------------
// getLaterIndex
//--------------------------------------------------------------------------------------------------------

  public long getLaterIndex(long inIndex, long inN) { 
    if (inIndex==kNotFound)
      throw new StoreException("Index not found");
    long theIndex=inIndex;
    for (int i=0; i<inN; i++)
      theIndex=getNextIndex(theIndex); 
    return theIndex;
  }

//--------------------------------------------------------------------------------------------------------
// slowGetPrevIndex
//
// --  SLOW  SLOW  SLOW  SLOW  SLOW  SLOW  SLOW  SLOW  SLOW   --
//
//--------------------------------------------------------------------------------------------------------

  public long slowGetPrevIndex(long inIndex) {
    if (inIndex==kNotFound)
      throw new StoreException("Index not found");
    long theIndex=inIndex;
    long theNextIndex=getNextIndex(theIndex);
    while (theNextIndex!=inIndex) {
      theIndex=theNextIndex;
      theNextIndex=getNextIndex(theIndex);
    }
    return theIndex; 
  }

//--------------------------------------------------------------------------------------------------------
// getLastIndex
//--------------------------------------------------------------------------------------------------------

  public long getLastIndex(long inChainDx) {
    if (inChainDx>=getSize())
      return kNotFound;
    return mLastStore.getLong(inChainDx+1); // Free chain is at index 0
  }

//--------------------------------------------------------------------------------------------------------
// getFirstIndex
//--------------------------------------------------------------------------------------------------------

  public long getFirstIndex(long inChainDx) { 
    if (inChainDx>=getSize())
      return kNotFound;
    long theLastIndex=getLastIndex(inChainDx);
    if (theLastIndex==kNotFound)
      return kNotFound;
    else
      return getNextIndex(theLastIndex); 
  }

//--------------------------------------------------------------------------------------------------------
// slowGetIndexAtN
//--------------------------------------------------------------------------------------------------------

  public long slowGetIndexAtN(long inChainDx, long inN) {
    if (inN>getNInChain(inChainDx))
      throw new StoreException("Past chain end");
    long theIndex=getFirstIndex(inChainDx);
    if (theIndex!=kNotFound)
      theIndex=getLaterIndex(theIndex,inN);
    return theIndex;
  }

//--------------------------------------------------------------------------------------------------------
// getLastFreeIndex
//--------------------------------------------------------------------------------------------------------

  long getLastFreeIndex() { return mLastStore.getLong(0); } // Free chain is at index 0

//--------------------------------------------------------------------------------------------------------
// getIndexes
//--------------------------------------------------------------------------------------------------------

  public int getIndexes(long inChainDx, long[] ioIndexes, int inIndexDelta) {
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    int theNIndexes=(int) getNInChain(inChainDx);
    long theIndex=getFirstIndex(inChainDx);
    for (int i=0; i<theNIndexes; i++) {
      ioIndexes[inIndexDelta+i]=theIndex;
      theIndex=getNextIndex(theIndex);
    }
    return theNIndexes;
  }

  public int getIndexes(long inChainDx, long[] ioIndexes) { 
    return getIndexes(inChainDx,ioIndexes,0); }

  public long[] getIndexes(long inChainDx) { 
    long[] theIndexes=Allocate.newLongs((int) getNInChain(inChainDx));
    getIndexes(inChainDx,theIndexes); 
    return theIndexes;
  }

//--------------------------------------------------------------------------------------------------------
// getIndexStore
//--------------------------------------------------------------------------------------------------------

  public long getIndexStore(long inChainDx, VarStore inIndexStore) { 
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    long theNIndexes=getNInChain(inChainDx);
    long theIndex=getFirstIndex(inChainDx);
    for (long i=0; i<theNIndexes; i++) {
      inIndexStore.appendVar(theIndex);
      theIndex=getNextIndex(theIndex);
    }
    return theNIndexes;
  }

  public VarRAMStore getIndexStore(long inChainDx) { 
    VarRAMStore theIndexStore=new VarRAMStore(mNextStore.getVarSize(),getNInChain(inChainDx));
    getIndexStore(inChainDx,theIndexStore);
    theIndexStore.compact();
    return theIndexStore;
  }

//--------------------------------------------------------------------------------------------------------
// getIndexesAtIndex
//--------------------------------------------------------------------------------------------------------

  public void getIndexesAtIndex(long inChainDx, long inIndex, 
      long[] ioIndexes, int inIndexDelta, int inNIndexes) { 
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    if (inNIndexes==0)
      return;
    long theIndex=inIndex;
    ioIndexes[inIndexDelta]=theIndex;
    long theFirstIndex=getFirstIndex(inChainDx);
    for (int i=1; i<inNIndexes; i++) {
      theIndex=getNextIndex(theIndex);
      if (theIndex==theFirstIndex)
        throw new StoreException("Past chain end");
      ioIndexes[inIndexDelta+i]=theIndex;
    }
  }

  public long[] getIndexesAtIndex(long inChainDx, long inIndex, int inNIndexes) { 
    long[] theIndexes=Allocate.newLongs(inNIndexes);
    getIndexesAtIndex(inChainDx,inIndex,theIndexes,0,inNIndexes);
    return theIndexes;
  }

//--------------------------------------------------------------------------------------------------------
// slowGetIndexesAtN
//--------------------------------------------------------------------------------------------------------

  public void slowGetIndexesAtN(long inChainDx, long inN, long[] ioIndexes, int inIndexDelta, int inNIndexes) { 
    getIndexesAtIndex(inChainDx,slowGetIndexAtN(inChainDx,inN),ioIndexes,inIndexDelta,inNIndexes);
  }

  public long[] slowGetIndexesAtN(long inChainDx, long inN, int inNIndexes) { 
    long[] theIndexes=Allocate.newLongs(inNIndexes);
    slowGetIndexesAtN(inChainDx,inN,theIndexes,0,inNIndexes);
    return theIndexes;
  }

//--------------------------------------------------------------------------------------------------------
// link
//--------------------------------------------------------------------------------------------------------

  private void link(long inPrevIndex, long inNextIndex) { 
    if ((inPrevIndex==kNotFound)||(inNextIndex==kNotFound))
      throw new StoreException("Bad Link");
    mNextStore.setVar(inPrevIndex,inNextIndex); 
  }

//--------------------------------------------------------------------------------------------------------
// appendChain
//--------------------------------------------------------------------------------------------------------

  public long appendChain() {
    long theChainDx=getSize();
    mLastStore.appendVar(kNotFound);
    mNStore.appendVar(0);
    return theChainDx;
  }

//--------------------------------------------------------------------------------------------------------
// appendChains
//--------------------------------------------------------------------------------------------------------

  public void appendChains(long inNChains) {
    mLastStore.appendVars(kNotFound,inNChains);
    mNStore.appendVars(0,inNChains);
  }

//--------------------------------------------------------------------------------------------------------
// ensureChains
//--------------------------------------------------------------------------------------------------------

  public void ensureChains(long inNChains) {
    if (getSize()<inNChains)
      appendChains(inNChains-getSize());
  }

//--------------------------------------------------------------------------------------------------------
// newIndex
//--------------------------------------------------------------------------------------------------------

  private long newIndex() {
    long theLastFreeIndex=getLastFreeIndex();                 // Get last link in free chain
    if (theLastFreeIndex==kNotFound) {                        // No links in the free chain
      long theNewIndex=mNextStore.getSize();                  //   Grow store to create new link
      mNextStore.setSize(theNewIndex+1);
      return theNewIndex;
    } else {                                                  // There are links in the free chain, use the first free link
      mNStore.addToVar(0,-1);                                 //   Decrement number of links in free chain
      long theFirstFreeIndex=getNextIndex(theLastFreeIndex);  
      if (theFirstFreeIndex==theLastFreeIndex)                //   Use only link in the free chain
        mLastStore.setVar(0,kNotFound);                       //     Set external link to kNotFound to indicate empty chain
      else {
        long theSecondFreeIndex=getNextIndex(theFirstFreeIndex); //    Find second link
        link(theLastFreeIndex,theSecondFreeIndex);               //    Have last link point past first to second link
      }
      return theFirstFreeIndex;                               //   First link is new link
    }
  }

  private long newIndex(long inChainDx) {
    if (inChainDx<0)
      throw new StoreException("Negative chain index: "+inChainDx);
    ensureChains(inChainDx+1);
    mNStore.addToVar(inChainDx+1,1);                  // Increment chain size (free chain is at index 0)
    return newIndex();
  }

//--------------------------------------------------------------------------------------------------------
// prependIndex
//--------------------------------------------------------------------------------------------------------

  public long prependIndex(long inChainDx) {
    long theNewIndex=newIndex(inChainDx);
    long theLastIndex=getLastIndex(inChainDx);        // Get last link in chain
    if (theLastIndex==kNotFound) {                    // No links in the chain
      mLastStore.setVar(inChainDx+1,theNewIndex);     //   Set external link to new last link (free chain is at index 0)
      link(theNewIndex,theNewIndex);                  //   Set new link to point to itself
    } else {                                          // Chain has links
      long theFirstIndex=getNextIndex(theLastIndex);  //   Get first link in chain
      link(theNewIndex,theFirstIndex);                //   Change new link to point to first link
      link(theLastIndex,theNewIndex);                 //   Change last link to point to new link
    }
    return theNewIndex;
  }

//--------------------------------------------------------------------------------------------------------
// appendIndex
//--------------------------------------------------------------------------------------------------------

  public long appendIndex(long inChainDx) {
    long theNewIndex=prependIndex(inChainDx);    // Prepend link to chain
    mLastStore.setVar(inChainDx+1,theNewIndex);  // Set external link to prepended link, making it the last link in chain (free chain is at index 0)
    return theNewIndex;
  }

//--------------------------------------------------------------------------------------------------------
// insertNextIndex
//--------------------------------------------------------------------------------------------------------

  public long insertNextIndex(long inChainDx, long inIndex) {
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    long theNextIndex=getNextIndex(inIndex);
    long theNewIndex=newIndex(inChainDx);
    link(inIndex,theNewIndex);
    link(theNewIndex,theNextIndex);
    if (inIndex==getLastIndex(inChainDx))
      mLastStore.setVar(inChainDx+1,theNewIndex);
    return theNewIndex;    
  }

//--------------------------------------------------------------------------------------------------------
// slowInsertAtN
//--------------------------------------------------------------------------------------------------------

  public long slowInsertAtN(long inChainDx, long inN) {
    if (inN==0)
      return prependIndex(inChainDx);
    else
      return insertNextIndex(inChainDx,slowGetIndexAtN(inChainDx,inN-1));
  }

//--------------------------------------------------------------------------------------------------------
// removeIndex
//--------------------------------------------------------------------------------------------------------

  private void removeIndex(long inChainDx, long inPrevIndex, long inIndex) {
    mNStore.addToVar(inChainDx+1,-1);               // Decrement chain size (free chain is at index 0)
    long theLastIndex=getLastIndex(inChainDx);
    if (inPrevIndex==inIndex) 
      mLastStore.setVar(inChainDx+1,kNotFound);
    else {
      long theNextIndex=getNextIndex(inIndex);    
      link(inPrevIndex,theNextIndex);
      if (theLastIndex==inIndex)
        mLastStore.setVar(inChainDx+1,inPrevIndex);
    }
    // Add link to free chain
    long theLastFreeIndex=getLastFreeIndex();
    if (theLastFreeIndex==kNotFound) 
      link(inIndex,inIndex);
    else {
      long theFirstFreeIndex=getNextIndex(theLastFreeIndex); 
      link(theLastFreeIndex,inIndex);
      link(inIndex,theFirstFreeIndex);
    }
    mLastStore.setVar(0,inIndex);
    mNStore.addToVar(0,1);
  }

//--------------------------------------------------------------------------------------------------------
// removeFirstIndex
//--------------------------------------------------------------------------------------------------------

  public long removeFirstIndex(long inChainDx) {
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    long theLastIndex=getLastIndex(inChainDx);
    if (theLastIndex==kNotFound)
      throw new StoreException("No index to remove");
    long theFirstIndex=getNextIndex(theLastIndex);
    removeIndex(inChainDx,theLastIndex,theFirstIndex);
    return theFirstIndex;
  }

//--------------------------------------------------------------------------------------------------------
// removeNextIndex
//--------------------------------------------------------------------------------------------------------

  public long removeNextIndex(long inChainDx, long inIndex) {
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    long theLastIndex=getLastIndex(inChainDx);
    if (inIndex==theLastIndex)
      throw new StoreException("Remove past end");
    long theNextIndex=getNextIndex(inIndex);
    removeIndex(inChainDx,inIndex,theNextIndex);
    if (theNextIndex==theLastIndex)
      mLastStore.setVar(inChainDx+1,inIndex);
    return theNextIndex;    
  }

//--------------------------------------------------------------------------------------------------------
// slowRemoveIndexAtN
//--------------------------------------------------------------------------------------------------------

  public long slowRemoveIndexAtN(long inChainDx, long inN) {
    if (inN==0)
      return removeFirstIndex(inChainDx);
    else
      return removeNextIndex(inChainDx,slowGetIndexAtN(inChainDx,inN-1));
  }

//--------------------------------------------------------------------------------------------------------
// removeAllInChain
//--------------------------------------------------------------------------------------------------------

  public boolean removeAllInChain(long inChainDx) {
    if (inChainDx>=getSize())
      throw new StoreException("Missing chain");
    long theNIndexes=getNInChain(inChainDx);
    if (theNIndexes==0)
      return false;
    long theLastIndex=getLastIndex(inChainDx);
    long thePrevIndex=theLastIndex;
    while (true) {
      long theIndex=getNextIndex(thePrevIndex);
      removeIndex(inChainDx,thePrevIndex,theIndex);
      if (theIndex==theLastIndex)
        return true;
      thePrevIndex=theIndex;
    } 
  }

//--------------------------------------------------------------------------------------------------------
// sanityCheck
//--------------------------------------------------------------------------------------------------------

  public void sanityCheck() {
    
    long theSize=getSize();
    long theLinkSize=getLinkSize();
    if (getNextStore().getSize()!=theLinkSize)
      throw new StoreException("Bad NextStore size");
    if (getLastStore().getSize()!=theSize+1)
      throw new StoreException("Bad LastStore size");
    if (getNStore().getSize()!=theSize+1)
      throw new StoreException("Bad NStore size");

    if (theLinkSize>0) {
      long theStart=Math.max(0,theLinkSize-30);
      for (long i=theStart; i<theLinkSize; i++) {
        long theNextIndex=getNextIndex(i);
        if ((theNextIndex<0)||(theNextIndex>=theLinkSize))
          throw new StoreException("Bad next index");
      }
    }

    if (theSize>0) {
      long theStart=Math.max(0,theSize-10);
      for (long i=theStart; i<theSize; i++) {
        long theNIndexes=getNInChain(i);
        if (theNIndexes>0) {
          long theFirstIndex=getFirstIndex(i);
          long theIndex=theFirstIndex;
          for (long j=0; j<theNIndexes; j++)
            theIndex=getNextIndex(theIndex); 
          if (theIndex!=theFirstIndex)
            throw new StoreException("Sanity lost");
        }
      }
    }
  }

}

