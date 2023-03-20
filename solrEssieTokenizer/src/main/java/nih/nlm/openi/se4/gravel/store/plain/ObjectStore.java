//--------------------------------------------------------------------------------------------------------
// ObjectStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.plain;

import gravel.store.*;
import gravel.store.hash.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// ObjectStore
//--------------------------------------------------------------------------------------------------------

public class ObjectStore implements RAMStoreInterface {

//--------------------------------------------------------------------------------------------------------
// ObjectStore member vars
//--------------------------------------------------------------------------------------------------------

  private int         mSize;
  private Object[]    mObjects;

//--------------------------------------------------------------------------------------------------------
// ObjectStore - copy
//--------------------------------------------------------------------------------------------------------

  public ObjectStore(ObjectStore inObjectStore) {
    mSize=inObjectStore.mSize;
    mObjects=Allocate.newObjects(mSize);
    System.arraycopy(inObjectStore.mObjects,0,mObjects,0,mSize);
  }

//--------------------------------------------------------------------------------------------------------
// ObjectStore - new 
//--------------------------------------------------------------------------------------------------------

  public ObjectStore(long inCapacity) {
    mSize=0;
    mObjects=Allocate.newObjects(inCapacity);
  }

  public ObjectStore() { this(16); }
 
//--------------------------------------------------------------------------------------------------------
// ObjectStore - from array
//--------------------------------------------------------------------------------------------------------

  public ObjectStore(Object[] inObjects, int inObjectDelta, int inNObjects) {
    mSize=inNObjects;
    mObjects=Allocate.newObjects(mSize);
    System.arraycopy(inObjects,inObjectDelta,mObjects,0,mSize);
  }

  public ObjectStore(Object[] inObjects) { this(inObjects,0,inObjects.length); }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    mSize=kNotFound;
    mObjects=null;
  }

//--------------------------------------------------------------------------------------------------------
// getIsClosed
//--------------------------------------------------------------------------------------------------------

  public boolean getIsClosed() { return (mObjects==null); }

//--------------------------------------------------------------------------------------------------------
// getSize
//--------------------------------------------------------------------------------------------------------

  public long getSize() { return mSize; }

//--------------------------------------------------------------------------------------------------------
// getCapacity
//--------------------------------------------------------------------------------------------------------

  public long getCapacity() { return mObjects.length; }

//--------------------------------------------------------------------------------------------------------
// getContentMemory
//--------------------------------------------------------------------------------------------------------

  public long getContentMemory() { return kReferenceMemory*getSize(); }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() {  // Does not include memory of the Objects
    return Allocate.getObjectMemory(kIntMemory+kReferenceMemory)+
            Allocate.getArrayMemory(mObjects); }

//--------------------------------------------------------------------------------------------------------
// get Params
//--------------------------------------------------------------------------------------------------------

  public int getNParams() { throw new StoreException("No Params"); }
  public long getParam(int inIndex) { throw new StoreException("No Params"); }
  public long[] getParams() { throw new StoreException("No Params"); }

//--------------------------------------------------------------------------------------------------------
// setSize
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inSize) {
    if (inSize<0) 
      throw new StoreException("Set size less than zero");
    if (inSize>mSize) 
      ensureCapacity(inSize);
    else {
      for (int i=(int) inSize; i<mSize; i++)
        mObjects[i]=null;
    }
    mSize=(int) inSize;
  }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public void clear() { setSize(0); }

//--------------------------------------------------------------------------------------------------------
// truncateBy
//--------------------------------------------------------------------------------------------------------

  public void truncateBy(long inNValues) { setSize(getSize()-inNValues); }

//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) {
    if (inCapacity<0) 
      throw new StoreException("Set capacity less than zero");
    if (inCapacity<mSize) 
      throw new StoreException("Set capacity less than size");
    if (inCapacity!=mObjects.length) {
      Object[] theOldObjects=mObjects;
      mObjects=Allocate.newObjects(inCapacity);
      System.arraycopy(theOldObjects,0,mObjects,0,mSize);
    }
  }

//--------------------------------------------------------------------------------------------------------
// compact
//--------------------------------------------------------------------------------------------------------

  public void compact() { setCapacity(mSize); }

//--------------------------------------------------------------------------------------------------------
// ensureCapacity
//--------------------------------------------------------------------------------------------------------

  public void ensureCapacity(long inSize) {
    if (inSize>mObjects.length) {
      long theNewCapacity;
      if (inSize<k1K)
        theNewCapacity=inSize*2;
      else if (inSize<k1M)
        theNewCapacity=((3*(inSize/2)-1)/SliceStore.kLongSliceSize+1)*SliceStore.kLongSliceSize;
      else 
        theNewCapacity=((5*(inSize/4)-1)/SliceStore.kLongSliceSize+1)*SliceStore.kLongSliceSize;
      setCapacity(theNewCapacity);
    }
  }

//--------------------------------------------------------------------------------------------------------
// set Params
//--------------------------------------------------------------------------------------------------------

  public void setNParams(int inNParams) { throw new StoreException("No Params"); }
  public void setParam(int inIndex, long inParam) { throw new StoreException("No Params"); }
  public void setParams(long[] inParams) { throw new StoreException("No Params"); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ObjectStore load(String inFilename) { 
     throw new StoreException("Not implemented in ObjectStore"); }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(String inFilename, boolean inCompact) { 
     throw new StoreException("Not implemented in ObjectStore"); }

  public void store(String inFilename) { store(inFilename,true); }
  
//--------------------------------------------------------------------------------------------------------
// getObject
//--------------------------------------------------------------------------------------------------------

  public Object getObject(long inOffset) { return mObjects[(int) inOffset]; }

//--------------------------------------------------------------------------------------------------------
// getObjects
//--------------------------------------------------------------------------------------------------------

  public void getObjects(long inOffset, Object[] ioObjects, int inObjectDelta, int inNObjects) {
    System.arraycopy(mObjects,(int) inOffset,ioObjects,inObjectDelta,inNObjects); }

//--------------------------------------------------------------------------------------------------------
// getObjects
//--------------------------------------------------------------------------------------------------------

  public void getObjects(long inOffset, Object[] ioObjects) {
    getObjects(inOffset,ioObjects,0,ioObjects.length); }

//--------------------------------------------------------------------------------------------------------
// getObjects
//--------------------------------------------------------------------------------------------------------

  public Object[] getObjects(long inOffset, int inNObjects) {
    Object[] theObjects=Allocate.newObjects(inNObjects);
    getObjects(inOffset,theObjects,0,inNObjects);
    return theObjects;
  }

//--------------------------------------------------------------------------------------------------------
// getAllObjects
//--------------------------------------------------------------------------------------------------------

  public int getAllObjects(Object[] ioObjects, int inObjectDelta) {
    long theSize=getSize();
    if (theSize>ioObjects.length-inObjectDelta)
      throw new StoreException("Array too small");
    getObjects(0,ioObjects,inObjectDelta,(int) theSize);
    return (int) theSize;
  }

  public int getAllObjects(Object[] ioObjects) { return getAllObjects(ioObjects,0); }

  public Object[] getAllObjects() {
    long theSize=getSize();
    if (theSize>k1G/kReferenceMemory)
      throw new StoreException("Store too big");
    return getObjects(0,(int) theSize);
  }
  
//--------------------------------------------------------------------------------------------------------
// getObjectStore
//--------------------------------------------------------------------------------------------------------

  public ObjectStore getObjectStore(long inOffset, long inNObjects) {
    return new ObjectStore(mObjects,(int) inOffset,(int) inNObjects); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(Object inObject) { return HashUtils.hash(inObject); }

//--------------------------------------------------------------------------------------------------------
// hash
//--------------------------------------------------------------------------------------------------------

  public long hash(Object[] inObjects, int inObjectDelta, int inNObjects) { 
    return HashUtils.hash(inObjects,inObjectDelta,inNObjects); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex) { return hash(getObject(inIndex)); }

//--------------------------------------------------------------------------------------------------------
// getHash
//--------------------------------------------------------------------------------------------------------

  public long getHash(long inIndex, int inNObjects) { 
    return hash(getObjects(inIndex,inNObjects),0,inNObjects); }

//--------------------------------------------------------------------------------------------------------
// setObject
//--------------------------------------------------------------------------------------------------------

  public void setObject(long inOffset, Object inObject) { mObjects[(int) inOffset]=inObject; }

//--------------------------------------------------------------------------------------------------------
// setObjects
//--------------------------------------------------------------------------------------------------------

  public void setObjects(long inOffset, Object inObject, long inNCopies) {
    if (kRangeChecking) 
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
    for (int i=0; i<inNCopies; i++)
      mObjects[(int) inOffset+i]=inObject;
  }

//--------------------------------------------------------------------------------------------------------
// setObjects
//--------------------------------------------------------------------------------------------------------

  public void setObjects(long inOffset, Object[] inObjects, int inObjectDelta, int inNObjects) {
    System.arraycopy(inObjects,inObjectDelta,mObjects,(int) inOffset,inNObjects); }

//--------------------------------------------------------------------------------------------------------
// setObjects
//--------------------------------------------------------------------------------------------------------

  public void setObjects(long inOffset, Object[] inObjects) {
    setObjects(inOffset,inObjects,0,inObjects.length); }

//--------------------------------------------------------------------------------------------------------
// setAllObjects
//--------------------------------------------------------------------------------------------------------

  public void setAllObjects(Object inObject) {
    long theSize=getSize();
    if (theSize>k1G/kReferenceMemory)
      throw new StoreException("Store too big");
    setObjects(0,inObject,(int) theSize);
  }

//--------------------------------------------------------------------------------------------------------
// appendObject
//--------------------------------------------------------------------------------------------------------

  public void appendObject(Object inObject) {
    long theSize=getSize();
    setSize(theSize+1);
    setObject(theSize,inObject);
  }

//--------------------------------------------------------------------------------------------------------
// appendObjects
//--------------------------------------------------------------------------------------------------------

  public void appendObjects(Object inObject, long inNCopies) {
    if (kRangeChecking) 
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
    if (inNCopies>0) {
      long theSize=getSize();
      setSize(theSize+inNCopies);
      setObjects(theSize,inObject,inNCopies);
    }
  }

//--------------------------------------------------------------------------------------------------------
// appendObjects
//--------------------------------------------------------------------------------------------------------

  public void appendObjects(Object[] inObjects, int inObjectDelta, int inNObjects) {
    if (inNObjects>0) {
      long theSize=getSize();
      setSize(theSize+inNObjects);
      setObjects(theSize,inObjects,inObjectDelta,inNObjects);
    }
  }

//--------------------------------------------------------------------------------------------------------
// appendObjects
//--------------------------------------------------------------------------------------------------------

  public void appendObjects(Object[] inObjects) { appendObjects(inObjects,0,inObjects.length); }

//--------------------------------------------------------------------------------------------------------
// copyObject
//--------------------------------------------------------------------------------------------------------

  public void copyObject(long inOffset1, long inOffset2) { copyObjects(inOffset1,1,inOffset2); }
  
//--------------------------------------------------------------------------------------------------------
// copyObjects
//--------------------------------------------------------------------------------------------------------

  public void copyObjects(long inOffset1, long inOffset2, long inNObjects) {
    if (inNObjects>0) {
      System.arraycopy(mObjects,(int) inOffset1,mObjects,(int) inOffset2,(int) inNObjects);

      // Explicitly null source to allow objects to be garbage collected
      if (inOffset1<inOffset2) {
        int theStart=(int) inOffset1;
        int theEnd=(int) Math.min(inOffset1+inNObjects,inOffset2);
        for (int i=theStart; i<theEnd; i++)
          mObjects[i]=null;
      } else {
        int theStart=(int) Math.max(inOffset1,inOffset2+inNObjects);
        int theEnd=(int) (inOffset1+inNObjects);
        for (int i=theStart; i<theEnd; i++)
          mObjects[i]=null;
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// insertGap
//--------------------------------------------------------------------------------------------------------

  public void insertGap(long inOffset, long inNObjects) {
    if (inNObjects>0) {
      ensureCapacity(mSize+inNObjects);
      if (inOffset<mSize)
        System.arraycopy(mObjects,(int) inOffset,mObjects,(int) (inOffset+inNObjects),
                (int) (mSize-inOffset));
      mSize+=inNObjects;
    }
  }

//--------------------------------------------------------------------------------------------------------
// insertGap
//--------------------------------------------------------------------------------------------------------

  public void insertGap(long inOffset) { insertGap(inOffset,1); }

//--------------------------------------------------------------------------------------------------------
// insertObject
//--------------------------------------------------------------------------------------------------------

  public void insertObject(long inOffset, Object inObject) {
    insertGap(inOffset);
    setObject(inOffset,inObject);
  }

//--------------------------------------------------------------------------------------------------------
// insertObjects
//--------------------------------------------------------------------------------------------------------

  public void insertObjects(long inOffset, Object[] inObjects, int inObjectDelta, int inNObjects) {
    if (inNObjects>0) {
      insertGap(inOffset,inNObjects);
      setObjects(inOffset,inObjects,inObjectDelta,inNObjects);
    }
  }

//--------------------------------------------------------------------------------------------------------
// insertObjects
//--------------------------------------------------------------------------------------------------------

  public void insertObjects(long inOffset, Object[] inObjects) {
    insertObjects(inOffset,inObjects,0,inObjects.length); }

//--------------------------------------------------------------------------------------------------------
// removeObjects
//--------------------------------------------------------------------------------------------------------

  public void removeObjects(long inOffset, long inNObjects) {
    if (inNObjects>0)
      if (inOffset+inNObjects>=mSize) {

        // Explicitly null contents to allow objects to be garbage collected
        for (int i=(int) inOffset; i<mSize; i++)
          mObjects[i]=null;

        mSize=(int) inOffset;

      } else {
        System.arraycopy(mObjects,(int) (inOffset+inNObjects),mObjects,(int) inOffset,
                (int) (mSize-inOffset-inNObjects));

        // Explicitly null contents to allow objects to be garbage collected
        for (int i=mSize-(int) inNObjects; i<mSize; i++)
          mObjects[i]=null;

        mSize-=inNObjects;
      }
  }

//--------------------------------------------------------------------------------------------------------
// removeObject
//--------------------------------------------------------------------------------------------------------

  public void removeObject(long inOffset) { removeObjects(inOffset,1); }

}

