//--------------------------------------------------------------------------------------------------------
// BasePool.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// BasePool
//--------------------------------------------------------------------------------------------------------

public abstract class BasePool implements Constants { 

//--------------------------------------------------------------------------------------------------------
// BasePool constants
//--------------------------------------------------------------------------------------------------------

  public static final long     kCleanupInterval=30*k1Sec;

  // Pool can hold any number of objects
  // If more exist than preferred number (PrefN), objects will be closed in the background
  // If fewer, new objects will be opened in the background

  // How long extra objects can be idle before one is closed
  public static final long     kIdleLimit=k1Min;
  
  // When prefer zero in pool, delay longer before closing the last object
  public static final long     kLastObjectIdleLimit=5*k1Min;  
  
//--------------------------------------------------------------------------------------------------------
// BasePool member vars
//--------------------------------------------------------------------------------------------------------

  private int                     mPrefNInPool;
  private long                    mTimeLastUsed;   // Track time last used so don't close objects during heavy usage

  private Object                  mPoolLock;
  private PoolObjectInterface[]   mPoolObjects;
  private int                     mNPoolObjects; 
  private long                    mNOpenFailures;  // Track failures and attempts to open objects 
  private long                    mNOpenAttempts;  // Repeated failures will slow down attepts
  private boolean                 mDrain;

//--------------------------------------------------------------------------------------------------------
// BasePool
//--------------------------------------------------------------------------------------------------------

  public BasePool(int inPrefNInPool) {
    mPrefNInPool=inPrefNInPool;
    mPoolLock=new Object();
    mPoolObjects=new PoolObjectInterface[16+inPrefNInPool];
  }

  public BasePool() { this(0); }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    synchronized(mPoolLock) {
      drain();
    }  
  }

//--------------------------------------------------------------------------------------------------------
// gets
//--------------------------------------------------------------------------------------------------------

  public int getPrefNInPool() { return mPrefNInPool; }
  public long getTimeLastUsed() { return mTimeLastUsed; }
  public long getNOpenFailures() { return mNOpenFailures; }
  public long getNOpenAttempts() { return mNOpenAttempts; }
  
//--------------------------------------------------------------------------------------------------------
// newPoolObject
//--------------------------------------------------------------------------------------------------------

  protected abstract PoolObjectInterface newPoolObject();

//--------------------------------------------------------------------------------------------------------
// openPoolObject
//--------------------------------------------------------------------------------------------------------

  protected PoolObjectInterface openPoolObject() throws Exception {
    PoolObjectInterface thePoolObject=null;
    try {
      thePoolObject=newPoolObject();
      thePoolObject.open();
      synchronized(mPoolLock) {
        mNOpenAttempts=0;
        mNOpenFailures=0;
      }
      return thePoolObject;
    } catch (Exception e) {
      synchronized(mPoolLock) {
        mNOpenFailures++;
      }
      closePoolObject(thePoolObject);
      throw e;
    }
  }

//--------------------------------------------------------------------------------------------------------
// closePoolObject
//--------------------------------------------------------------------------------------------------------

  protected void closePoolObject(PoolObjectInterface inPoolObject) {
    if (inPoolObject!=null) 
      inPoolObject.close();
  }

//--------------------------------------------------------------------------------------------------------
// getPoolObject
//--------------------------------------------------------------------------------------------------------

  public PoolObjectInterface getPoolObject() throws Exception {
    synchronized(mPoolLock) {
      mTimeLastUsed=System.currentTimeMillis();
      if (mNPoolObjects>0) {
        mNPoolObjects--;
        PoolObjectInterface thePoolObject=mPoolObjects[mNPoolObjects];
        mPoolObjects[mNPoolObjects]=null;
        return thePoolObject;
      }
    }
    return openPoolObject();
  }

//--------------------------------------------------------------------------------------------------------
// putPoolObject
//--------------------------------------------------------------------------------------------------------

  public void putPoolObject(PoolObjectInterface inPoolObject) {
    if ((inPoolObject!=null)&&(inPoolObject.canRecycle()))   
      synchronized(mPoolLock) {
        if (!mDrain) {
          mTimeLastUsed=System.currentTimeMillis();
          if (mNPoolObjects<mPoolObjects.length) 
            System.arraycopy(mPoolObjects,0,mPoolObjects,1,mNPoolObjects);
          else {
            PoolObjectInterface[] thePoolObjects=mPoolObjects;
            mPoolObjects=new PoolObjectInterface[2*mNPoolObjects+1];
            System.arraycopy(thePoolObjects,0,mPoolObjects,1,mNPoolObjects);
          } 
          mPoolObjects[0]=inPoolObject;
          mNPoolObjects++;
          return;
        }
      }
    closePoolObject(inPoolObject);
  }

//--------------------------------------------------------------------------------------------------------
// drain
//--------------------------------------------------------------------------------------------------------

  public void drain() {
    PoolObjectInterface[] thePoolObjects=null;
    synchronized(mPoolLock) {
      mDrain=true;
      if (mNPoolObjects>0) {
        thePoolObjects=mPoolObjects;
        mPoolObjects=new PoolObjectInterface[16+mPrefNInPool];
        mNPoolObjects=0;
      }
    }
    if (thePoolObjects!=null)
      for (int i=0; i<thePoolObjects.length; i++)
        closePoolObject(thePoolObjects[i]);
  }

//--------------------------------------------------------------------------------------------------------
// stopDrain
//--------------------------------------------------------------------------------------------------------

  public void stopDrain() {
    synchronized(mPoolLock) {
      mDrain=false;
    }
  }
}
