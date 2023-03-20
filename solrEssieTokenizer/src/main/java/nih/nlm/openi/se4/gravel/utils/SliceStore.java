//--------------------------------------------------------------------------------------------------------
// SliceStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// SliceStore
//--------------------------------------------------------------------------------------------------------

public class SliceStore implements Constants {

//--------------------------------------------------------------------------------------------------------
// Slice consts
//--------------------------------------------------------------------------------------------------------

  public static final int      kSliceMemory=64*k1K;            // 64K
  public static final long     kSlabMemory=256*kSliceMemory;   // 16M

  public static final int      kByteSliceSize=kSliceMemory/kByteMemory;
  public static final int      kCharSliceSize=kSliceMemory/kCharMemory;
  public static final int      kIntSliceSize=kSliceMemory/kIntMemory;
  public static final int      kLongSliceSize=kSliceMemory/kLongMemory;
  public static final int      kDoubleSliceSize=kSliceMemory/kDoubleMemory;

  private static final int     kByteSliceNBits=getNBits(kByteSliceSize);
  private static final int     kCharSliceNBits=getNBits(kCharSliceSize);
  private static final int     kIntSliceNBits=getNBits(kIntSliceSize);
  private static final int     kLongSliceNBits=getNBits(kLongSliceSize);
  private static final int     kDoubleSliceNBits=getNBits(kDoubleSliceSize);

  private static final int     kByteSliceMask=getMask(kByteSliceSize);
  private static final int     kCharSliceMask=getMask(kCharSliceSize);
  private static final int     kIntSliceMask=getMask(kIntSliceSize);
  private static final int     kLongSliceMask=getMask(kLongSliceSize);
  private static final int     kDoubleSliceMask=getMask(kDoubleSliceSize);

//--------------------------------------------------------------------------------------------------------
// SliceStore consts
//--------------------------------------------------------------------------------------------------------

  // Intended for use as a source of temporary buffers or small stack stores
  // Don't hold too many slabs (16M each) - Memory allocation not that slow
  // Note that almost all big memory allocs use slices - recycled slices are free memory

  // Have a central store which holds lots of slices and a periperal thread store for 
  //   each core that holds a few.  
  public static final int      kCentralMaxNByteSlabs=
      (int) Math.max(4,FormatUtils.getHeapMemory()/100/kSlabMemory);   // max(1%,64M)
  public static final int      kCentralMaxNByteSlices=1024;    // 2 slabs worth (32M)
  
  public static final int      kThreadMaxNByteSlabs=2;         // 32M
  public static final int      kThreadMaxNByteSlices=256;      // 1 slabs worth (16M)

  public static final int      kMaxNOtherSlices=16;            // 16*64K=1M

  public static final byte     kRecycleStamp=(byte) 0xa7;

//--------------------------------------------------------------------------------------------------------
// SliceStore class vars
//--------------------------------------------------------------------------------------------------------

  private static SliceStore    gCentralSliceStore;
  private static SliceStore[]  gPeripheralSliceStores;

//--------------------------------------------------------------------------------------------------------
// SliceStore class init
//--------------------------------------------------------------------------------------------------------

  static {
    try {
      
      gCentralSliceStore=new SliceStore(true);
      gPeripheralSliceStores=new SliceStore[FormatUtils.getNCores()];
        
      // Allocate some slices
      gCentralSliceStore.putByteSlices(
          gCentralSliceStore.getByteSlices(kCentralMaxNByteSlices));
  
      // Cleanup in background
      Heart.addHeartTask(new HeartTask("SliceCleaner",3*k1Sec,false) { 
        public void beat() {
          // Remove slices (64K) when number of slabs (16M) over 16 (256M)
          // Slices drain over time at a min of 16*64K/3s = 1.2G/hr
          long theNByteSlabs=gCentralSliceStore.getNAvailByteSlabs();
          if (theNByteSlabs>=16) 
            for (long i=0; i<theNByteSlabs; i++) 
              gCentralSliceStore.getByteSlice();  // Get and abandon
          // Peripheral slice stores only need enough slices for temp buffers
          // Move extra peripheral slices to central slice store for big allocations
          for (int i=0; i<gPeripheralSliceStores.length; i++) {
            SliceStore theSliceStore=gPeripheralSliceStores[i];
            if ((theSliceStore!=null)&&(theSliceStore.getNAvailByteSlices()>=64))
              gCentralSliceStore.putByteSlice(theSliceStore.getByteSlice());
          }
        }
      },true);
      
    } catch (Throwable e) {
      System.err.println(FormatUtils.formatException("Cannot init SliceStore",e));
      throw e;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getCentralSliceStore
//--------------------------------------------------------------------------------------------------------

  public static SliceStore getCentralSliceStore() { return gCentralSliceStore; }

//--------------------------------------------------------------------------------------------------------
// getSliceStore
//--------------------------------------------------------------------------------------------------------

  public static SliceStore getSliceStore() {
    int theStoreDx=(int) (Thread.currentThread().getId()%gPeripheralSliceStores.length);
    SliceStore thePeripheralSliceStore=gPeripheralSliceStores[theStoreDx];
    if (thePeripheralSliceStore==null) {
      synchronized(gPeripheralSliceStores) {
        if (thePeripheralSliceStore==null) {
          thePeripheralSliceStore=new SliceStore();
          gPeripheralSliceStores[theStoreDx]=thePeripheralSliceStore;
        }
      }
    }
    return thePeripheralSliceStore;
  }

//--------------------------------------------------------------------------------------------------------
// getNBits
//--------------------------------------------------------------------------------------------------------

  private static int getNBits(int inSliceSize) { 
    int theNBits=0;
    int theSliceSize=inSliceSize;
    while (theSliceSize>1) {
      theNBits++;
      theSliceSize>>>=1;
    }
    return theNBits;
  }

//--------------------------------------------------------------------------------------------------------
// getMask
//--------------------------------------------------------------------------------------------------------

  private static int getMask(int inSliceSize) { return (-1)>>>(32-getNBits(inSliceSize)); }

//--------------------------------------------------------------------------------------------------------
// getRemainder
//--------------------------------------------------------------------------------------------------------

  private static int getRemainder(long inNValues, int inSliceMask) { 
    if (inNValues<0)
      throw new RuntimeException("Cannot get a negative remainder");
    return (int) (inNValues&inSliceMask); 
  }

  public static int getByteRemainder(long inNValues) { return getRemainder(inNValues,kByteSliceMask); }
  public static int getCharRemainder(long inNValues) { return getRemainder(inNValues,kCharSliceMask); }
  public static int getIntRemainder(long inNValues) { return getRemainder(inNValues,kIntSliceMask); }
  public static int getLongRemainder(long inNValues) { return getRemainder(inNValues,kLongSliceMask); }
  public static int getDoubleRemainder(long inNValues) { return getRemainder(inNValues,kDoubleSliceMask); }

//--------------------------------------------------------------------------------------------------------
// getNFullSlices
//--------------------------------------------------------------------------------------------------------

  private static int getNFullSlices(long inNValues, int inSliceNBits) { 
    if (inNValues<0)
      throw new RuntimeException("Cannot get a negative number of full slices");
    return (int) (inNValues>>>inSliceNBits); 
  }

  public static int getNFullByteSlabs(long inNValues) { return getNFullSlices(inNValues,kByteSliceNBits+8); }
  public static int getNFullByteSlices(long inNValues) { return getNFullSlices(inNValues,kByteSliceNBits); }
  public static int getNFullCharSlices(long inNValues) { return getNFullSlices(inNValues,kCharSliceNBits); }
  public static int getNFullIntSlices(long inNValues) { return getNFullSlices(inNValues,kIntSliceNBits); }
  public static int getNFullLongSlices(long inNValues) { return getNFullSlices(inNValues,kLongSliceNBits); }
  public static int getNFullDoubleSlices(long inNValues) { return getNFullSlices(inNValues,kDoubleSliceNBits); }

//--------------------------------------------------------------------------------------------------------
// getNSlices
//--------------------------------------------------------------------------------------------------------

  private static int getNSlices(long inNValues, int inSliceNBits) { 
    if (inNValues<0)
      throw new RuntimeException("Cannot get a negative number of slices");
    else if (inNValues==0)
      return 0;
    else
      return getNFullSlices(inNValues-1,inSliceNBits)+1; 
  }

  public static int getNByteSlabs(long inNValues) { return getNSlices(inNValues,kByteSliceNBits+8); }
  public static int getNByteSlices(long inNValues) { return getNSlices(inNValues,kByteSliceNBits); }
  public static int getNCharSlices(long inNValues) { return getNSlices(inNValues,kCharSliceNBits); }
  public static int getNIntSlices(long inNValues) { return getNSlices(inNValues,kIntSliceNBits); }
  public static int getNLongSlices(long inNValues) { return getNSlices(inNValues,kLongSliceNBits); }
  public static int getNDoubleSlices(long inNValues) { return getNSlices(inNValues,kDoubleSliceNBits); }

//--------------------------------------------------------------------------------------------------------
// SliceStore member vars
//--------------------------------------------------------------------------------------------------------

  private RecycleRing    mByteSlabCache;
  private RecycleRing    mByteSliceCache;
  private RecycleRing    mCharSliceCache;
  private RecycleRing    mIntSliceCache;
  private RecycleRing    mLongSliceCache;
  private RecycleRing    mDoubleSliceCache;

//--------------------------------------------------------------------------------------------------------
// SliceStore
//--------------------------------------------------------------------------------------------------------

  // Package access - only instantiated during class init
  SliceStore(boolean inCentralStore) {
    mByteSlabCache=new RecycleRing(false,inCentralStore?kCentralMaxNByteSlabs:kThreadMaxNByteSlabs,0);
    mByteSliceCache=new RecycleRing(false,inCentralStore?kCentralMaxNByteSlices:kThreadMaxNByteSlices,0);
    mCharSliceCache=new RecycleRing(false,kMaxNOtherSlices,0);
    mIntSliceCache=new RecycleRing(false,kMaxNOtherSlices,0);
    mLongSliceCache=new RecycleRing(false,kMaxNOtherSlices,0);
    mDoubleSliceCache=new RecycleRing(false,kMaxNOtherSlices,0);
  }

  // Public access 
  public SliceStore() { this(false); }

//--------------------------------------------------------------------------------------------------------
// stamps
//--------------------------------------------------------------------------------------------------------

  void setStamp(byte[] ioSlice) { 
    if ((ioSlice!=null)&&(ioSlice.length==kByteSliceSize)) 
      ioSlice[0]=kRecycleStamp;
  }

  void setStamps(byte[][] ioSlices, int inSliceDelta, int inNSlices) { 
    if (ioSlices!=null)
      for (int i=0; i<inNSlices; i++)
        setStamp(ioSlices[inSliceDelta+i]);
  }

  void setStamps(byte[][] ioSlices) { 
    if (ioSlices!=null) 
      setStamps(ioSlices,0,ioSlices.length); 
  }

  void setStampss(byte[][][] ioSlabs, int inSlabDelta, int inNSlabs) { 
    if (ioSlabs!=null)
      for (int i=0; i<inNSlabs; i++)
        setStamps(ioSlabs[inSlabDelta+i]);
  }

  void setStampss(byte[][][] ioSlabs) { 
    if (ioSlabs!=null)
      setStampss(ioSlabs,0,ioSlabs.length); 
  }
  
  void clearStamp(byte[] ioSlice) { 
    if ((ioSlice!=null)&&(ioSlice.length==kByteSliceSize)) {
      if (ioSlice[0]!=kRecycleStamp)
        throw new RuntimeException("Missing recycle stamp");
      ioSlice[0]=0;
    }
  }

  void clearStamps(byte[][] ioSlices, int inSliceDelta, int inNSlices) { 
    if (ioSlices!=null)
      for (int i=0; i<inNSlices; i++)
        clearStamp(ioSlices[inSliceDelta+i]);
  }

  void clearStamps(byte[][] ioSlices) { 
    if (ioSlices!=null)
      clearStamps(ioSlices,0,ioSlices.length); 
  }

  void clearStampss(byte[][][] ioSlabs, int inSlabDelta, int inNSlabs) { 
    if (ioSlabs!=null)
      for (int i=0; i<inNSlabs; i++)
        clearStamps(ioSlabs[inSlabDelta+i]);
  }

  void clearStampss(byte[][][] ioSlabs) { 
    if (ioSlabs!=null)
      clearStampss(ioSlabs,0,ioSlabs.length); 
  } 

//--------------------------------------------------------------------------------------------------------
// getNAvail
//
// Soft refs allow slabs to be garbage collected
// Slabs kept in an array in a run that may have gaps in it 
// This routine gives the length of the run, which may overstate N slabs
//--------------------------------------------------------------------------------------------------------

  long getNAvailByteSlabs() { return mByteSlabCache.getNInRing(); }
  long getNAvailByteSlices() { return mByteSliceCache.getNInRing(); }

//--------------------------------------------------------------------------------------------------------
// getAvailSlice
//
// Best effort to get from local cache - returns null if not available
//--------------------------------------------------------------------------------------------------------

  byte[][] getAvailByteSlab() { 
    byte[][] theSlab=(byte[][]) mByteSlabCache.get(); 
    clearStamps(theSlab);
    return theSlab; 
  }
  
  byte[] getAvailByteSlice() { 
    byte[] theSlice=(byte[]) mByteSliceCache.get(); 
    clearStamp(theSlice);
    return theSlice; 
  }
  
  char[] getAvailCharSlice() { return (char[]) mCharSliceCache.get(); }
  int[] getAvailIntSlice() { return (int[]) mIntSliceCache.get(); }
  long[] getAvailLongSlice() { return (long[]) mLongSliceCache.get(); }
  double[] getAvailDoubleSlice() { return (double[]) mDoubleSliceCache.get(); }

//--------------------------------------------------------------------------------------------------------
// getSlice
//
// Uses central store if necessary, allocates if necessary
//--------------------------------------------------------------------------------------------------------

  public byte[] getByteSlice() { 
    byte[] theSlice=getAvailByteSlice();
    if (theSlice!=null) 
      return theSlice;
    // If no slice, store empty, get a slab - should happen rarely 
    else {
      byte[][] theSlab=getAvailByteSlab();
      // If slab store empty, get a slab from central store - should happen rarely 
      if ((theSlab==null)&&(this!=gCentralSliceStore))
        theSlab=gCentralSliceStore.getAvailByteSlab();
      // If slab, ...
      if (theSlab!=null) {
        // Put rest of slab (all but one slice), then return remaining one
        putAvailByteSlices(theSlab,1,255);
        return theSlab[0];
      // If no slab, allocate bytes
      } else {
        return Allocate.newBytes(kByteSliceSize);
      }
    }
  }
  
  public char[] getCharSlice() { 
    char[] theSlice=getAvailCharSlice();      
    if (theSlice!=null) 
      return theSlice;
    else 
      return Allocate.newChars(kCharSliceSize);
  }
  
  public int[] getIntSlice() { 
    int[] theSlice=getAvailIntSlice();    
    if (theSlice!=null) 
      return theSlice;
    else 
      return Allocate.newInts(kIntSliceSize);
  }
  
  public long[] getLongSlice() { 
    long[] theSlice=getAvailLongSlice();
    if (theSlice!=null)
      return theSlice;
    else 
      return Allocate.newLongs(kLongSliceSize);
  }
  
  public double[] getDoubleSlice() { 
    double[] theSlice=getAvailDoubleSlice();
    if (theSlice!=null) 
      return theSlice;
    else 
      return Allocate.newDoubles(kDoubleSliceSize);
  }

//--------------------------------------------------------------------------------------------------------
// putAvailSlice
//
// Best effort to put in local cache - returns false if no room
//--------------------------------------------------------------------------------------------------------

  boolean putAvailByteSlab(byte[][] inSlab) { 
    setStamps(inSlab);
    boolean theOK=mByteSlabCache.put(inSlab); 
    if (!theOK)
      clearStamps(inSlab);
    return theOK;
  }
  
  boolean putAvailByteSlice(byte[] inSlice) { 
    setStamp(inSlice);
    boolean theOK=mByteSliceCache.put(inSlice); 
    if (!theOK)
      clearStamp(inSlice);
    return theOK;
  }
  
  boolean putAvailCharSlice(char[] inSlice) { 
    return mCharSliceCache.put(inSlice); 
  }
  
  boolean putAvailIntSlice(int[] inSlice) { 
    return mIntSliceCache.put(inSlice); 
  }
  
  boolean putAvailLongSlice(long[] inSlice) { 
    return mLongSliceCache.put(inSlice); 
  }
  
  boolean putAvailDoubleSlice(double[] inSlice) { 
    return mDoubleSliceCache.put(inSlice); 
  }

//--------------------------------------------------------------------------------------------------------
// putSlice
//
// Checks slice valid - if not, ignores which effectively frees 
// Uses central store if necessary, frees if no room
//--------------------------------------------------------------------------------------------------------

  public void putByteSlice(byte[] inSlice) { 
    if ((inSlice!=null)&&(inSlice.length==kByteSliceSize)) {
      boolean theOK=putAvailByteSlice(inSlice);          
      // If slice store full, get enough slices to make a slab - should happen rarely 
      if (!theOK) { 
        byte[][] theSlab=new byte[256][];
        // Get all but one slice, and use inSlice for that one
        int theNFound=getAvailByteSlices(theSlab,0,255);
        theSlab[theNFound]=inSlice;
        theNFound++; 
        // If softrefs turned on, it is possible that the store had lots of empty softrefs, and we don't 
        //   have enough slices to make a slab.  Put slices back - effectively compacting out all empty refs
        if (theNFound<256) {
          putAvailByteSlices(theSlab,0,theNFound);
        // If enough slices to make a slab, put it
        } else {
          theOK=putAvailByteSlab(theSlab);
          // If slab store full, put in central store - should happen rarely 
          if ((!theOK)&&(this!=gCentralSliceStore))
            theOK=gCentralSliceStore.putAvailByteSlab(theSlab);
        }
      }
    }
  }
  
  public void putCharSlice(char[] inSlice) { 
    if ((inSlice!=null)&&(inSlice.length==kCharSliceSize)) 
      putAvailCharSlice(inSlice);      
  }
  
  public void putIntSlice(int[] inSlice) { 
    if ((inSlice!=null)&&(inSlice.length==kIntSliceSize)) 
      putAvailIntSlice(inSlice);      
  }
  
  public void putLongSlice(long[] inSlice) { 
    if ((inSlice!=null)&&(inSlice.length==kLongSliceSize)) 
      putAvailLongSlice(inSlice);
  }
  
  public void putDoubleSlice(double[] inSlice) { 
    if ((inSlice!=null)&&(inSlice.length==kDoubleSliceSize)) 
      putAvailDoubleSlice(inSlice);
  }

//--------------------------------------------------------------------------------------------------------
// getAvailSlices
//
// Best effort in local cache - returns N got
//--------------------------------------------------------------------------------------------------------

  int getAvailByteSlabs(byte[][][] ioSlabs, int inSlabDelta, int inNSlabs) {
    int theNSlabs=mByteSlabCache.get(ioSlabs,inSlabDelta,inNSlabs); 
    clearStampss(ioSlabs,inSlabDelta,theNSlabs);
    return theNSlabs;
  }

  int getAvailByteSlices(byte[][] ioSlices, int inSliceDelta, int inNSlices) {
    int theNSlices=mByteSliceCache.get(ioSlices,inSliceDelta,inNSlices); 
    clearStamps(ioSlices,inSliceDelta,theNSlices);
    return theNSlices;
  }

//--------------------------------------------------------------------------------------------------------
// getSomeSlices
//
// Best effort in local and central caches
// Doesn't allocate
// Returns N got
//--------------------------------------------------------------------------------------------------------

  public int getSomeByteSlabs(byte[][][] ioSlabs, int inSlabDelta, int inNSlabs) {
    int theNSlabs=getAvailByteSlabs(ioSlabs,inSlabDelta,inNSlabs);
    if ((theNSlabs<inNSlabs)&&(this!=gCentralSliceStore))
      theNSlabs+=gCentralSliceStore.getAvailByteSlabs(
          ioSlabs,inSlabDelta+theNSlabs,inNSlabs-theNSlabs);
    return theNSlabs;
  }

  public int getSomeByteSlices(byte[][] ioSlices, int inSliceDelta, int inNSlices) {
    int theNSlices=getAvailByteSlices(ioSlices,inSliceDelta,inNSlices);
    if ((theNSlices<inNSlices)&&(this!=gCentralSliceStore))
      theNSlices+=gCentralSliceStore.getAvailByteSlices(
          ioSlices,inSliceDelta+theNSlices,inNSlices-theNSlices);
    return theNSlices;
  }

  public int getSomeByteSlabs(byte[][][] ioSlabs) {
    return getSomeByteSlabs(ioSlabs,0,ioSlabs.length); }

  public int getSomeByteSlices(byte[][] ioSlices) {
    return getSomeByteSlices(ioSlices,0,ioSlices.length); }

//--------------------------------------------------------------------------------------------------------
// getByteSlices
//
// Uses central store if necessary, allocates if necessary
//--------------------------------------------------------------------------------------------------------

  public byte[][] getByteSlices(byte[][] ioSlices, int inSliceDelta, long inNValues) {

    if (inNValues==0)
      return ioSlices;
    if (inNValues<0)
      throw new RuntimeException("Negative NValues: "+inNValues);
    if (inSliceDelta<0)
      throw new RuntimeException("Negative SliceDelta: "+inSliceDelta);
    int theSliceDelta=inSliceDelta;
    int theNFullSlices=getNFullByteSlices(inNValues);
    if (inSliceDelta+theNFullSlices>ioSlices.length)
      throw new RuntimeException("Too many slices: "+inSliceDelta+"+"+theNFullSlices+">"+ioSlices.length);

    int theNFullSlabs=theNFullSlices>>8;
    if (theNFullSlabs>0) {
      byte[][][] theByteSlabs=new byte[theNFullSlabs][][];
      int theNSlabs=getSomeByteSlabs(theByteSlabs);
      for (int j=0; j<theNSlabs; j++) {
        byte[][] theByteSlab=theByteSlabs[j];
        for (int k=0; k<256; k++)
          ioSlices[theSliceDelta++]=theByteSlab[k];
        theByteSlabs[j]=null;
      }
      theByteSlabs=null;
    }

    int theEnd=inSliceDelta+theNFullSlices;
    if (theEnd>theSliceDelta) 
      theSliceDelta+=getSomeByteSlices(ioSlices,theSliceDelta,theEnd-theSliceDelta);
    
    for (; theSliceDelta<theEnd; theSliceDelta++) 
      ioSlices[theSliceDelta]=Allocate.newBytes(kByteSliceSize);    

    int theRemainder=getByteRemainder(inNValues);
    if (theRemainder>0) 
      ioSlices[inSliceDelta+theNFullSlices]=Allocate.newBytes(theRemainder);
        
    return ioSlices;
  }

  public byte[][] getByteSlices(byte[][] ioSlices) {
    return getByteSlices(ioSlices,0,ioSlices.length*(long) kByteSliceSize); }    

  public byte[][] getByteSlices(long inNValues) { 
    return getByteSlices(new byte[getNByteSlices(inNValues)][],0,inNValues); }

//--------------------------------------------------------------------------------------------------------
// putAvailSlices
//
// Best effort in local cache - returns N put
//--------------------------------------------------------------------------------------------------------

  int putAvailByteSlabs(byte[][][] ioSlabs, int inSlabDelta, int inNSlabs) {
    setStampss(ioSlabs,inSlabDelta,inNSlabs);
    int theNSlabsPut=mByteSlabCache.put(ioSlabs,inSlabDelta,inNSlabs); 
    clearStampss(ioSlabs,inSlabDelta+theNSlabsPut,inNSlabs-theNSlabsPut);
    return theNSlabsPut;
  }

  int putAvailByteSlices(byte[][] ioSlices, int inSliceDelta, int inNSlices) {
    setStamps(ioSlices,inSliceDelta,inNSlices);
    int theNSlicesPut=mByteSliceCache.put(ioSlices,inSliceDelta,inNSlices); 
    clearStamps(ioSlices,inSliceDelta+theNSlicesPut,inNSlices-theNSlicesPut);
    return theNSlicesPut;
  }
  
//--------------------------------------------------------------------------------------------------------
// putSomeSlices
//
// Best effort in local and central caches 
// Does not free
// Returns N put
//--------------------------------------------------------------------------------------------------------

  public int putSomeByteSlabs(byte[][][] ioSlabs, int inSlabDelta, int inNSlabs) {
    int theNSlabsPut=putAvailByteSlabs(ioSlabs,inSlabDelta,inNSlabs);
    if ((theNSlabsPut<inNSlabs)&&(this!=gCentralSliceStore))
      theNSlabsPut+=gCentralSliceStore.putAvailByteSlabs(
          ioSlabs,inSlabDelta+theNSlabsPut,inNSlabs-theNSlabsPut);
    // If extra slab doesn't fit, top off local slice store
    if (theNSlabsPut<inNSlabs) {
      putAvailByteSlices(ioSlabs[theNSlabsPut],0,256);
      // Release whatever is left of that slab
      ioSlabs[theNSlabsPut]=null;
      theNSlabsPut++;
    }
    return theNSlabsPut;
  }

  public int putSomeByteSlices(byte[][] ioSlices, int inSliceDelta, int inNSlices) {
    int theNSlicesPut=putAvailByteSlices(ioSlices,inSliceDelta,inNSlices);
    if ((theNSlicesPut<inNSlices)&&(this!=gCentralSliceStore))
      theNSlicesPut+=gCentralSliceStore.putAvailByteSlices(
          ioSlices,inSliceDelta+theNSlicesPut,inNSlices-theNSlicesPut);
    return theNSlicesPut;
  }

  public int putSomeByteSlabs(byte[][][] ioSlabs) {
    return putSomeByteSlabs(ioSlabs,0,ioSlabs.length); }

  public int putSomeByteSlices(byte[][] ioSlices) {
    return putSomeByteSlices(ioSlices,0,ioSlices.length); }

//--------------------------------------------------------------------------------------------------------
// putByteSlices
//
// Uses central store if necessary, frees if necessary
//--------------------------------------------------------------------------------------------------------

  public void putByteSlices(byte[][] ioSlices, int inSliceDelta, int inNSlices) {

    if (inNSlices==0)
      return;
    if (inNSlices<0)
      throw new RuntimeException("Negative NSlices: "+inNSlices);
    if (inSliceDelta<0)
      throw new RuntimeException("Negative SliceDelta: "+inSliceDelta);
    int theSliceDelta=inSliceDelta;
    int theNFullSlices=inNSlices;
    if (inSliceDelta+inNSlices>ioSlices.length)
      throw new RuntimeException("Too many slices: "+inSliceDelta+"+"+inNSlices+">"+ioSlices.length);

    // Check whever last slice is partial - if so release
    int theLastIndex=inSliceDelta+inNSlices-1;
    int theLastSize=ioSlices[theLastIndex].length;
    if (theLastSize!=kByteSliceSize) {
      ioSlices[theLastIndex]=null;
      theNFullSlices--;
    }
    if (theNFullSlices==0)
      return;

    // If big enough put, 
    int theNFullSlabs=theNFullSlices>>8;
    if (theNFullSlabs>0) {
      // Merge slices into slabs - slices still refd by ioSlices
      int n=inSliceDelta;
      byte[][][] theByteSlabs=new byte[theNFullSlabs][][];
      for (int j=0; j<theNFullSlabs; j++) {
        byte[][] theByteSlab=new byte[256][];
        for (int k=0; k<256; k++)
          theByteSlab[k]=ioSlices[n++];
        theByteSlabs[j]=theByteSlab;
      }
      // Put as many slabs as can
      int theNSlabsPut=putSomeByteSlabs(theByteSlabs,0,theNFullSlabs);
      // Null out slices in ioSlices that were put as slabs
      for (int j=0; j<theNSlabsPut; j++) {
        for (int k=0; k<256; k++)
          ioSlices[theSliceDelta++]=null;
        theByteSlabs[j]=null;
      }
      theByteSlabs=null;
    }
    
    // Put as many slices as can
    int theEnd=inSliceDelta+theNFullSlices;
    if (theEnd>theSliceDelta) 
      theSliceDelta+=putSomeByteSlices(ioSlices,theSliceDelta,theEnd-theSliceDelta);
    
    // Release the rest
    int theNLeft=theEnd-theSliceDelta;
    if (theNLeft>0) 
      for (; theSliceDelta<theEnd; theSliceDelta++) 
        ioSlices[theSliceDelta]=null;    
  }

  public void putByteSlices(byte[][] ioSlices) { 
    putByteSlices(ioSlices,0,ioSlices.length); }

}

