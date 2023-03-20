//--------------------------------------------------------------------------------------------------------
// RecycleRing.java
//
// Gets read Refs from array
// Puts write Refs into array 
// Write index is ahead of read index:  WriteIndex = ReadIndex + NInRing
// State is:  (ReadIndex|NInRing)
// Put increments NInRing, so State --> State+1
// Get increments ReadIndex and decrements NInRing, so State --> (ReadIndex+1|NInRing-1)
//
// If a CreateLimit is supplied when ring constructed, then ring will create/destroy refs as needed
// This means that gets and puts will always succeed, and that the number of refs in existance need
//   not be related to the ring size
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.lang.ref.*;
import java.util.concurrent.atomic.*;

//--------------------------------------------------------------------------------------------------------
// RecycleRing
//--------------------------------------------------------------------------------------------------------

public class RecycleRing implements Constants {

//--------------------------------------------------------------------------------------------------------
// RecycleRing member vars
//--------------------------------------------------------------------------------------------------------

  private boolean                mSoft; 
  private int                    mSize; 
  private int                    mCreateLimit; 
  private AtomicLong             mState;
  private AtomicReferenceArray   mRefs;
  
//--------------------------------------------------------------------------------------------------------
// RecycleRing
//--------------------------------------------------------------------------------------------------------

  public RecycleRing(boolean inSoft, int inSize, int inCreateLimit) {
    if (inSize<0)
      throw new RuntimeException("Bad size: "+inSize);
    if ((inCreateLimit<0)||(inCreateLimit>inSize/2))
      throw new RuntimeException("Bad create limit: "+inCreateLimit);
    mSoft=inSoft;
    mSize=inSize;
    mCreateLimit=inCreateLimit;
    mState=new AtomicLong(mCreateLimit);
    mRefs=new AtomicReferenceArray(inSize);
    while (getNInRing()<mCreateLimit)
      put(create());
  }

  public RecycleRing(int inSize) { this(false,inSize,0); }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------
  
  public void close() { 
    mCreateLimit=0;
    while (getNInRing()>0)
      destroy(get());
  }

//--------------------------------------------------------------------------------------------------------
// getSize
//--------------------------------------------------------------------------------------------------------
  
  public int getSize() { return mSize; }

//--------------------------------------------------------------------------------------------------------
// getNInRing
//--------------------------------------------------------------------------------------------------------
  
  public int getNInRing() { return (int) (mState.get()); }

//--------------------------------------------------------------------------------------------------------
// canCreate
//--------------------------------------------------------------------------------------------------------
  
  public boolean canCreate() { return (mCreateLimit>0); }

//--------------------------------------------------------------------------------------------------------
// create
//--------------------------------------------------------------------------------------------------------

  protected Object create() { 
    // Must be implemented by child class
    throw new RuntimeException("Can't use unimplemented create method");
  }

//--------------------------------------------------------------------------------------------------------
// destroy
//--------------------------------------------------------------------------------------------------------

  protected void destroy(Object inRef) { 
    // By default, do nothing
    // Needs to be able to handle case where inRef is null
  }

//--------------------------------------------------------------------------------------------------------
// rawGet
//
// Reads ref from array 
// When array gets near empty, reads occur soon after writes that filled up a slot
// May have to wait for write to complete
//
// When array gets near empty (theNInRing<mCreateLimit), will start to create refs 
// When array completely empty, if can create refs, will create one and return it
//   otherwise returns null indicating get failed
//--------------------------------------------------------------------------------------------------------
  
  private Object rawGet() {

    while (true) {
      long theState=mState.get();

      int theNInRing=(int) theState;
      if (theNInRing==0) 
        if (!canCreate())
          return null;
        else
          return create();

      int theNewNInRing=theNInRing-1;
      
      int theReadIndex=(int) (theState>>32);
      long theNewReadIndex=theReadIndex+1;
      if (theNewReadIndex>=mSize)
        theNewReadIndex-=mSize;
      
      long theNewState=(theNewReadIndex<<32)|(theNewNInRing);
      if (mState.compareAndSet(theState,theNewState)) {
        
        while (true) {
          Object theRef=mRefs.get(theReadIndex);
          if (theRef!=null) 
            if (mRefs.compareAndSet(theReadIndex,theRef,null))             
              return theRef;
        }
      }
    
      // Only get here if there is contention between threads
      if (theNInRing<mCreateLimit)
        put(create());
    }
  }

//--------------------------------------------------------------------------------------------------------
// get
//
// Supports use of soft references
// Best effort - returns null if not available
//--------------------------------------------------------------------------------------------------------

  public Object get() {
    if (!mSoft)
      return rawGet();
    else
      while (true) {
        SoftReference theRef=(SoftReference) rawGet();
        if (theRef==null)
          return null;
        Object theObject=theRef.get();
        if (theObject!=null)
          return theObject;
      } 
  }

//--------------------------------------------------------------------------------------------------------
// get
//
// Best effort - returns N got
//--------------------------------------------------------------------------------------------------------

  public int get(Object[] ioObjects, int inObjectDelta, int inNObjects) {
    for (int i=0; i<inNObjects; i++) {
      Object theObject=get();
      if (theObject==null) 
        return i;
      ioObjects[inObjectDelta+i]=theObject;
    }
    return inNObjects;
  }

//--------------------------------------------------------------------------------------------------------
// rawPut
// 
// Writes ref into array 
// When array gets near full, writes occur soon after reads that opened up a slot
// May have to wait for read to complete
//
// When array gets near full (mSize-theNInRing<mCreateLimit), will start to destroy refs
// When array completely full, if can create refs, will destroy inRef
//   otherwise returns false indicating put failed
//--------------------------------------------------------------------------------------------------------
  
  private boolean rawPut(Object inRef) { 

    while (true) {
      long theState=mState.get();

      int theNInRing=(int) theState;
      if (theNInRing==mSize) 
        if (!canCreate()) 
          return false;
        else {
          destroy(inRef);
          return true;
        } 
      
      long theNewState=theState+1;
      if (mState.compareAndSet(theState,theNewState)) {

        int theReadIndex=(int) (theState>>32);
        int theWriteIndex=theReadIndex+theNInRing;
        if (theWriteIndex>=mSize)
          theWriteIndex-=mSize;

        // When ring almost full, may have to wait for read to complete
        // Read puts null value in array - wait till we see it
        while (true) 
          if (mRefs.compareAndSet(theWriteIndex,null,inRef)) 
            return true;
      }
      
      // Only get here if there is contention between threads
      if (mSize-theNInRing<mCreateLimit)
        destroy(get());
    }
  } 

//--------------------------------------------------------------------------------------------------------
// put
//
// Supports use of soft references
// Best effort - returns false if no room
//--------------------------------------------------------------------------------------------------------

  public boolean put(Object inObject) { 
    if (!mSoft)
      return rawPut(inObject);
    else
      return rawPut(new SoftReference(inObject)); 
  }

//--------------------------------------------------------------------------------------------------------
// put
//
// Best effort - returns N put
//--------------------------------------------------------------------------------------------------------

  public int put(Object[] ioObjects, int inObjectDelta, int inNObjects) {
    for (int i=0; i<inNObjects; i++) {
      Object theObject=ioObjects[inObjectDelta+i];
      boolean theSuccess=put(theObject);
      if (!theSuccess)
        return i;
      ioObjects[inObjectDelta+i]=null;
    }
    return inNObjects;
  }

}




