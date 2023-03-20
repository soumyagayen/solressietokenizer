//--------------------------------------------------------------------------------------------------------
// DataStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.data;

import gravel.sort.*;
import gravel.store.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// DataStore
//--------------------------------------------------------------------------------------------------------

public abstract class DataStore extends NestedStore {

//--------------------------------------------------------------------------------------------------------
// DataStore consts
//--------------------------------------------------------------------------------------------------------

  public static final String    kOffsetsExtension="Offs";

//--------------------------------------------------------------------------------------------------------
// DataStore member vars
//--------------------------------------------------------------------------------------------------------

  private VarStore  mOffsetStore;

//--------------------------------------------------------------------------------------------------------
// DataStore 
//--------------------------------------------------------------------------------------------------------

  protected DataStore(StoreInterface inInnerStore, VarStore inOffsetStore) {
    super(inInnerStore);
    mOffsetStore=inOffsetStore;
    if (mOffsetStore.getSize()==0) {
      if (inInnerStore.getSize()==0)
        mOffsetStore.appendVar(0);
      else 
        throw new StoreException("Missing offsets");
    } else {
      if (mOffsetStore.getLong(0)!=0)
        throw new StoreException("Corrupt offsets - starts at "+mOffsetStore.getLong(0));
      if (mOffsetStore.getLong(mOffsetStore.getSize()-1)!=inInnerStore.getSize())
        throw new StoreException("Corrupt offsets - "+
            mOffsetStore.getLong(mOffsetStore.getSize()-1)+"!="+inInnerStore.getSize());
    }
    if (kRangeChecking) 
      sanityCheck();
  }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    super.close();
    if (mOffsetStore!=null) {
      mOffsetStore.close();
      mOffsetStore=null;
    }
  }

//--------------------------------------------------------------------------------------------------------
// unwrap
//--------------------------------------------------------------------------------------------------------

  public StoreInterface unwrap(boolean inCloseOffsets) { 
    if (!inCloseOffsets)
      mOffsetStore=null;
    return super.unwrap(); 
  }

  public StoreInterface unwrap() { return unwrap(true); }

//--------------------------------------------------------------------------------------------------------
// getOffsetStore
//--------------------------------------------------------------------------------------------------------

  protected VarStore getOffsetStore() { return mOffsetStore; }

//--------------------------------------------------------------------------------------------------------
// dangerousGetOffsetStore
//--------------------------------------------------------------------------------------------------------

  // unprotected access to inner store - easy to screw up
  public VarStore dangerousGetOffsetStore() { return mOffsetStore; }

//--------------------------------------------------------------------------------------------------------
// getOffset
//--------------------------------------------------------------------------------------------------------

  public long getOffset(long inIndex) { return mOffsetStore.getLong(inIndex); }

//--------------------------------------------------------------------------------------------------------
// getLastIndexEqualOrBeforeOffset
//
// Returns last index from (0,Size-1) with offset less than or equal to inOffset
// Returns kNotFound==-1 if inOffset outside the range of (0,DataSize-1)
//--------------------------------------------------------------------------------------------------------

  public long getLastIndexEqualOrBeforeOffset(long inOffset) { 
    if ((inOffset<0)||(inOffset>=getDataSize()))
      return kNotFound;
    return FindUtils.findIndexEqualOrBefore(inOffset,mOffsetStore);
  }

//--------------------------------------------------------------------------------------------------------
// getLastIndexBeforeOffset
//
// Returns last index from (-1,Size-1) with offset less than inOffset
// Returns kNotFound==-1 if inOffset outside the range of (0,DataSize-1)
//--------------------------------------------------------------------------------------------------------

  public long getLastIndexBeforeOffset(long inOffset) { 
    if ((inOffset<0)||(inOffset>=getDataSize()))
      return kNotFound;
    return FindUtils.findIndexBefore(inOffset,mOffsetStore);
  }

//--------------------------------------------------------------------------------------------------------
// getFirstIndexEqualOrAfterOffset
//
// Returns first index from (0,Size) with offset greater than or equal to inOffset
// Returns kNotFound==-1 if inOffset outside the range of (0,DataSize-1)
//--------------------------------------------------------------------------------------------------------

  public long getFirstIndexEqualOrAfterOffset(long inOffset) {
    if ((inOffset<0)||(inOffset>=getDataSize()))
      return kNotFound;
    return FindUtils.findIndexEqualOrAfter(inOffset,mOffsetStore);
  }
  
//--------------------------------------------------------------------------------------------------------
// getFirstIndexAfterOffset
//
// Returns first index from (1,Size) with offset greater than inOffset
// Returns kNotFound==-1 if inOffset outside the range of (0,DataSize-1)
//--------------------------------------------------------------------------------------------------------

  public long getFirstIndexAfterOffset(long inOffset) {
    if ((inOffset<0)||(inOffset>=getDataSize()))
      return kNotFound;
    return FindUtils.findIndexAfter(inOffset,mOffsetStore);
  }
  
//--------------------------------------------------------------------------------------------------------
// makeOffsetFilename
//--------------------------------------------------------------------------------------------------------

  public static String makeOffsetFilename(String inFilename) {
    return FileUtils.extendFilename(inFilename,kOffsetsExtension); }

//--------------------------------------------------------------------------------------------------------
// deleteStore
//--------------------------------------------------------------------------------------------------------

  public static void deleteStore(String inFilename) {
    ByteStore.deleteStore(inFilename); 
    ByteStore.deleteStore(makeOffsetFilename(inFilename)); 
  }

//--------------------------------------------------------------------------------------------------------
// getSize
//--------------------------------------------------------------------------------------------------------

  public long getSize() { return mOffsetStore.getSize()-1; }

//--------------------------------------------------------------------------------------------------------
// getDataSize
//--------------------------------------------------------------------------------------------------------

  public long getDataSize() { return super.getSize(); }

//--------------------------------------------------------------------------------------------------------
// getCapacity
//--------------------------------------------------------------------------------------------------------

  public long getCapacity() { return mOffsetStore.getCapacity()-1; }

//--------------------------------------------------------------------------------------------------------
// getDataCapacity
//--------------------------------------------------------------------------------------------------------

  public long getDataCapacity() { return super.getCapacity(); }

//--------------------------------------------------------------------------------------------------------
// getContentMemory
//--------------------------------------------------------------------------------------------------------

  public long getContentMemory() {
    return super.getContentMemory()+mOffsetStore.getContentMemory(); }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() {
    return super.getMemory()+kReferenceMemory+mOffsetStore.getMemory(); }

//--------------------------------------------------------------------------------------------------------
// setSize
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inNewSize) { 
    long theOldSize=getSize();
    if (inNewSize<theOldSize) {
      mOffsetStore.setSize(inNewSize+1); 
      super.setSize(getOffset(inNewSize));
    } else if (inNewSize>theOldSize) 
      mOffsetStore.appendVars(getDataSize(),inNewSize-theOldSize); 
  }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public void clear() { 
    super.clear(); 
    mOffsetStore.setSize(1); 
  }

//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) { mOffsetStore.setCapacity(inCapacity+1); }

//--------------------------------------------------------------------------------------------------------
// setDataCapacity
//--------------------------------------------------------------------------------------------------------

  public void setDataCapacity(long inDataCapacity) { super.setCapacity(inDataCapacity); }

//--------------------------------------------------------------------------------------------------------
// ensureCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureCapacity(long inSize) { mOffsetStore.ensureCapacity(inSize+1); }

//--------------------------------------------------------------------------------------------------------
// ensureDataCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureDataCapacity(long inDataCapacity) { super.ensureCapacity(inDataCapacity); }

//--------------------------------------------------------------------------------------------------------
// compact
//--------------------------------------------------------------------------------------------------------

  public void compact() { 
    super.setSize(getOffset(getSize()));
    super.compact(); 
    mOffsetStore.compact(); 
  }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public abstract long getHash(long inIndex);

//--------------------------------------------------------------------------------------------------------
// sanityCheck
//--------------------------------------------------------------------------------------------------------

  public void sanityCheck() {
    
    long theSize=getSize();
    if (getOffsetStore().getSize()!=theSize+1)
      throw new StoreException("Bad DataStore offset size");
    long theDataSize=getDataSize();
    if (getOffsetStore().getLong(theSize)!=theDataSize)
      throw new StoreException("Bad DataStore size");

    if (theSize>0) {
      long theStart=Math.max(0,theSize-10);
      if (getOffsetStore() instanceof VarDiskStore) 
        theStart=theSize;
      long theLastOffset=getOffset(theStart);
      for (long i=theStart; i<=theSize; i++) {
        long theOffset=getOffset(i);
        if ((theOffset<theLastOffset)||(theOffset>theDataSize))
          throw new StoreException("Bad offset");
        theLastOffset=theOffset;
      }
    }
  }

}

