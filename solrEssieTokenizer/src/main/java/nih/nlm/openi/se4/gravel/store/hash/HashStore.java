//--------------------------------------------------------------------------------------------------------
// HashStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.store.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// HashStore
//--------------------------------------------------------------------------------------------------------

public abstract class HashStore extends NestedStore {

//--------------------------------------------------------------------------------------------------------
// HashStore consts
//--------------------------------------------------------------------------------------------------------

  public static final double    kFillFactor=0.75;

  public static final String    kHashExtension="Hash";
  public static final String    kLookupExtension="Look";
  public static final String    kPtrExtension="Ptr";

//--------------------------------------------------------------------------------------------------------
// HashStore member vars
//--------------------------------------------------------------------------------------------------------

  private VarStore     mHashStore;
  private VarStore     mLookupStore;
  private VarStore     mPtrStore;
  private long         mReindexSize;

//--------------------------------------------------------------------------------------------------------
// HashStore 
//--------------------------------------------------------------------------------------------------------

  protected HashStore(StoreInterface inKeyStore, VarStore inHashStore, VarStore inLookupStore, 
      VarStore inPtrStore) {
    super(inKeyStore);
    
    mHashStore=inHashStore;  
    mLookupStore=inLookupStore;
    mPtrStore=inPtrStore;

    if (mHashStore!=null)
      mHashStore.setSize(inKeyStore.getSize());  // Some constructors create empty HashStore - resize to match keys    
    mPtrStore.setSize(inKeyStore.getSize());     // Some constructors create empty PtrStore - resize to match keys    
    // Note that LookupStore is a different size, set by reindex or rebuild (not here)

    mReindexSize=Math.max(inKeyStore.getSize(),Math.round(mLookupStore.getSize()*kFillFactor));
    
    if (kRangeChecking) 
      sanityCheck();
  }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    super.close();
    if (mHashStore!=null) {
      mHashStore.close();
      mHashStore=null;
    }
    if (mLookupStore!=null) {
      mLookupStore.close();
      mLookupStore=null;
    }
    if (mPtrStore!=null) {
      mPtrStore.close();
      mPtrStore=null;
    }
    mReindexSize=kNotFound;
  }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public StoreInterface unwrap(boolean inClosePlumbing) { 
    if (!inClosePlumbing) {
      mHashStore=null;
      mLookupStore=null;
      mPtrStore=null;
    }
    return super.unwrap(); 
  }

  public StoreInterface unwrap() { return unwrap(true); }

//--------------------------------------------------------------------------------------------------------
// getKeyStore
//--------------------------------------------------------------------------------------------------------

  protected StoreInterface getKeyStore() { return getInnerStore(); }
  public StoreInterface dangerousGetKeyStore() { return getInnerStore(); }

//--------------------------------------------------------------------------------------------------------
// getKeepHashes
//--------------------------------------------------------------------------------------------------------

  protected boolean getKeepHashes() { return (mHashStore!=null); }

//--------------------------------------------------------------------------------------------------------
// getHashStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getHashStore() { return mHashStore; }
  public VarStore dangerousGetHashStore() { return mHashStore; }

//--------------------------------------------------------------------------------------------------------
// getLookupStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getLookupStore() { return mLookupStore; }
  public VarStore dangerousGetLookupStore() { return mLookupStore; }

//--------------------------------------------------------------------------------------------------------
// getPtrStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getPtrStore() { return mPtrStore; }
  public VarStore dangerousGetPtrStore() { return mPtrStore; }

//--------------------------------------------------------------------------------------------------------
// makeHashFilename
//--------------------------------------------------------------------------------------------------------

  public static String makeHashFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kHashExtension); }

//--------------------------------------------------------------------------------------------------------
// makeLookupFilename
//--------------------------------------------------------------------------------------------------------

  public static String makeLookupFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kLookupExtension); }

//--------------------------------------------------------------------------------------------------------
// makePtrFilename
//--------------------------------------------------------------------------------------------------------

  public static String makePtrFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kPtrExtension); }

//--------------------------------------------------------------------------------------------------------
// deleteStore
//--------------------------------------------------------------------------------------------------------

  public static void deleteStore(String inFilename) {
    ByteStore.deleteStore(inFilename); 
    ByteStore.deleteStore(makeHashFilename(inFilename));   // May not exist, but OK
    ByteStore.deleteStore(makeLookupFilename(inFilename)); 
    ByteStore.deleteStore(makePtrFilename(inFilename)); 
  }

//--------------------------------------------------------------------------------------------------------
// getReindexSize
//--------------------------------------------------------------------------------------------------------

  public long getReindexSize() { return mReindexSize; }

//--------------------------------------------------------------------------------------------------------
// getContentMemory
//--------------------------------------------------------------------------------------------------------

  public long getContentMemory() {
    long theMemory=super.getContentMemory();
    if (mHashStore!=null)
      theMemory+=mHashStore.getContentMemory();
    // 7/3 is 3/3 for ptr and 4/3 for lookup 
    // Don't want to include mLookupStore.getContentMemory(), since it is full size even when empty
    theMemory+=7*mPtrStore.getContentMemory()/3; 
    return theMemory;
  }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() {
    return super.getMemory()+3*kReferenceMemory+kLongMemory+
        mLookupStore.getMemory()+mPtrStore.getMemory()+ 
        ((mHashStore==null)?0:mHashStore.getMemory());
  }

//--------------------------------------------------------------------------------------------------------
// setSize
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inNewSize) { 
    super.setSize(inNewSize); 
    if (mHashStore!=null)
      mHashStore.setSize(inNewSize);
    mPtrStore.setSize(inNewSize); 
    reindex(mReindexSize);
  }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public void clear() { 
    super.clear(); 
    if (mHashStore!=null)
      mHashStore.clear();
    mLookupStore.setVars(0,kNotFound,mLookupStore.getSize()); // Expensive
    mPtrStore.clear(); 
  }

//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) { 
    super.setCapacity(inCapacity); 
    if (mHashStore!=null)
      mHashStore.setCapacity(inCapacity); 
    mPtrStore.setCapacity(inCapacity); 
  }

//--------------------------------------------------------------------------------------------------------
// ensureCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureCapacity(long inSize) { 
    super.ensureCapacity(inSize); 
    if (mHashStore!=null)
      mHashStore.ensureCapacity(inSize); 
    mPtrStore.ensureCapacity(inSize); 
  }

//--------------------------------------------------------------------------------------------------------
// compact
//--------------------------------------------------------------------------------------------------------

  public void compact() { 
    super.compact();
    if (mHashStore!=null)
      mHashStore.compact(); 
    long theSize=Math.max(getSize(),16);
    if (theSize!=mReindexSize)
      reindex(theSize);
    else {
      mPtrStore.compact();
      mLookupStore.compact();
    }
  }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) { 
    if (mHashStore!=null)
      return mHashStore.getLong(inIndex); 
    else
      return super.getHash(inIndex);
  }

//--------------------------------------------------------------------------------------------------------
// hashToLookupIndex
//--------------------------------------------------------------------------------------------------------

  public long hashToLookupIndex(long inHash) {   
    long theNLookups=mLookupStore.getSize();
    long theLookupIndex;
    if (theNLookups==0)
      return 0;
    theLookupIndex=inHash%theNLookups;  // +1 change in hash will give different index
    if (theLookupIndex<0)
      theLookupIndex+=theNLookups;
    return theLookupIndex; 
  }

//--------------------------------------------------------------------------------------------------------
// newNLookups
//--------------------------------------------------------------------------------------------------------

  private long newNLookups(long inNewReindexSize) {
    long theNLookups=Math.round(inNewReindexSize/kFillFactor);
    if (theNLookups%2==0)
      theNLookups++;
    if (theNLookups%3==0)
      theNLookups+=2;
    if (theNLookups%5==0)
      theNLookups+=6;
    return theNLookups;
  }

//--------------------------------------------------------------------------------------------------------
// reindex
//--------------------------------------------------------------------------------------------------------

  public void reindex(long inNewReindexSize) {

    mReindexSize=Math.max(16,inNewReindexSize);
    long theNLookups=newNLookups(mReindexSize);
    int theVarSize=Conversions.calcVarLongSize(mReindexSize);
        
    // Recreate lookup 
    mLookupStore.clear();
    mLookupStore.setVarSize(theVarSize);
    mLookupStore.appendVars(kNotFound,theNLookups);
    
    // Reindex all keys in place
    long theNKeys=getKeyStore().getSize();
    if (mHashStore!=null)
      mHashStore.setSize(theNKeys);
    mPtrStore.setSize(theNKeys);
    for (long i=0; i<theNKeys; i++) {
      long theHash=getHash(i);
      long theLookupIndex=hashToLookupIndex(theHash);
      mPtrStore.setVar(i,mLookupStore.getLong(theLookupIndex));
      mLookupStore.setVar(theLookupIndex,i);
    }
  }

  public void reindex() { reindex(getReindexSize()); }

//--------------------------------------------------------------------------------------------------------
// rebuild
//--------------------------------------------------------------------------------------------------------

  // Used when hashes have changed because underlying data has changed
  public abstract void rebuild();
  
//--------------------------------------------------------------------------------------------------------
// ensureReindex
//--------------------------------------------------------------------------------------------------------

  public void ensureReindex(long inReindexSize) {
    if (inReindexSize>=getReindexSize())
      if (inReindexSize<k1M*4)
        reindex(2*inReindexSize+1);
      else
        reindex(3*(inReindexSize/2)+1);
  }

//--------------------------------------------------------------------------------------------------------
// getFirstIndex
//--------------------------------------------------------------------------------------------------------

  public long getFirstIndex(long inLookupIndex) {
    if (inLookupIndex>=mLookupStore.getSize())
      return kNotFound;
    else
      return mLookupStore.getLong(inLookupIndex);
  }

//--------------------------------------------------------------------------------------------------------
// getNextIndex
//--------------------------------------------------------------------------------------------------------

  public long getNextIndex(long inIndex) { 
    long theIndex=mPtrStore.getLong(inIndex); 
    if (theIndex==inIndex)
      throw new StoreException("Circular pointer ref: "+inIndex+" --> "+inIndex);
    return theIndex;
  }

//--------------------------------------------------------------------------------------------------------
// appendPtr
//--------------------------------------------------------------------------------------------------------

  public void appendPtr(long inHash, long inLookupIndex) {
    if (mHashStore!=null) 
      mHashStore.appendVar(inHash); 
    long theSize=mPtrStore.getSize();
    if (theSize<getReindexSize()) {
      long theLookupPtr=mLookupStore.getLong(inLookupIndex);
      if ((theLookupPtr!=kNotFound)&&(theLookupPtr==theSize))
        throw new StoreException("Circular pointer ref: "+theLookupPtr+" --> "+theLookupPtr);
      mPtrStore.appendVar(theLookupPtr);
      mLookupStore.setVar(inLookupIndex,theSize);
    } else {
      if (theSize<k1K)
        reindex(kByteSliceSize-31);
      else if (theSize<k1M)
        reindex(((3*(theSize/2)-1)/kByteSliceSize+1)*kByteSliceSize-31);
      else if (theSize<k1G)
        reindex(((5*(theSize/4)-1)/kByteSliceSize+1)*kByteSliceSize-31);
      else
        reindex(((7*(theSize/6)-1)/kByteSliceSize+1)*kByteSliceSize-31);
    }    
  }

//--------------------------------------------------------------------------------------------------------
// dropLastIndex
//--------------------------------------------------------------------------------------------------------

  public void dropLastIndex() {
    getKeyStore().truncateBy(1);
    if (mHashStore!=null)  
      mHashStore.truncateBy(1);
    mPtrStore.truncateBy(1);
  }

//--------------------------------------------------------------------------------------------------------
// removeLastPtr
//--------------------------------------------------------------------------------------------------------

  private void removeLastPtr(long inLookupIndex, long inPrevIndex) {
    // Dropping last index
    long theLastIndex=getSize()-1;
    // Have prev index point past the dropped index
    if (inPrevIndex==kNotFound)
      getLookupStore().setVar(inLookupIndex,getNextIndex(theLastIndex));
    else
      getPtrStore().setVar(inPrevIndex,getNextIndex(theLastIndex));
    // Drop last index
    dropLastIndex();
  }
  
//--------------------------------------------------------------------------------------------------------
// removeLastKey
//--------------------------------------------------------------------------------------------------------

  public void removeLastKey() {
    long theLastIndex=getSize()-1;
    long thePrevIndex=kNotFound;
    long theHash=getHash(theLastIndex);
    long theLookupIndex=hashToLookupIndex(theHash);
    long theIndex=getFirstIndex(theLookupIndex);
    while (theIndex!=theLastIndex) {
      thePrevIndex=theIndex;
      theIndex=getNextIndex(theIndex);
    }
    removeLastPtr(theLookupIndex,thePrevIndex);
  }

//--------------------------------------------------------------------------------------------------------
// sanityCheck
//--------------------------------------------------------------------------------------------------------

  public void sanityCheck() {
    
    long theSize=getSize();
    if (getPtrStore().getSize()!=theSize)
      throw new StoreException("Bad PtrStore size");
    if ((getKeepHashes())&&(getKeyStore().getSize()!=theSize))
      throw new StoreException("Bad HashStore size");
    
    // Some constructors pass in an empty LookupStore which is filled by a subsequent call to reindex
    // Only perform additional checks for lookups with content 
    long theLookupSize=getLookupStore().getSize();
    if (theLookupSize>0) {
      
      if (theLookupSize<theSize)
        throw new StoreException("Bad LookupStore size");
  
      if (theSize>0) {
        long theStart=Math.max(0,theSize-10);
        if (getPtrStore() instanceof VarDiskStore)
          theStart=theSize-1;
        for (long i=theStart; i<theSize; i++) {
          long thePtr=getNextIndex(i);
          if ((thePtr<kNotFound)||(thePtr>=theSize))
            throw new StoreException("Bad pointer");
        }
      }
        
      if (theLookupSize>0) {
        long theStart=Math.max(0,theLookupSize-30);
        if (getLookupStore() instanceof VarDiskStore) 
          theStart=theLookupSize-1;
        for (long i=theStart; i<theLookupSize; i++) {
          long theIndex=getFirstIndex(i);
          while (true) {
            if (theIndex==kNotFound)
              break;
            if ((theIndex<0)||(theIndex>=theSize))
              throw new StoreException("Bad index");
            theIndex=getNextIndex(theIndex);
          }
        }
      }
    }
  }

}

