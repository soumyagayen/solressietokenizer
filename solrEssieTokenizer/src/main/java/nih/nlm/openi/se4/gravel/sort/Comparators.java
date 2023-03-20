//--------------------------------------------------------------------------------------------------------
// Comparators.java
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import gravel.store.*;
import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// Comparators
//--------------------------------------------------------------------------------------------------------

public class Comparators implements Constants {

//--------------------------------------------------------------------------------------------------------
// Comparators consts
//--------------------------------------------------------------------------------------------------------

  public static final int   kLessThan=-1;
  public static final int   kEquals=0;
  public static final int   kGreaterThan=1;

//--------------------------------------------------------------------------------------------------------
// Comparators inner classes
//--------------------------------------------------------------------------------------------------------

  //------------------------------------------------------------------------------------------------------
  // Inner class Comparator
  //------------------------------------------------------------------------------------------------------

  public abstract static class Comparator {
    private boolean   mDescending;
    
    public Comparator(boolean inDescending) { mDescending=inDescending; }
    
    // Some comparators have buffers.  
    // To make them thread safe, need different buffers per thread.
    // By default, assume no buffers, don't make copy, and return original
    public abstract Comparator makeThreadCopy(); 
    // If comparator has buffers, here is a chance to release them
    // By default, do nothing
    public void close() { }

    public boolean getDescending() { return mDescending; }

    // Abstract methods implemented by child classes
    public abstract long getLength();
    public abstract long compareIndexes(long inIndex1, long inIndex2);
    public abstract void swapIndexes(long inIndex1, long inIndex2);
    
    // Wrapper for compareIndexes()
    // Handles difference between ascending vs. descending sorts
    public long sortCompare(long inIndex1, long inIndex2) {
      long theDiff;
      if (mDescending)
        theDiff=compareIndexes(inIndex2,inIndex1);
      else 
        theDiff=compareIndexes(inIndex1,inIndex2); 
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ArrayComparator
  //
  // Parent class for all array comparators
  //------------------------------------------------------------------------------------------------------

  public abstract static class ArrayComparator extends Comparator {
    public ArrayComparator(boolean inDescending) { super(inDescending); }
    public ArrayComparator() { this(false); }

    public ArrayComparator makeThreadCopy() { return this; } 
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ByteArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class ByteArrayComparator extends ArrayComparator {
    
    private byte[]  mBytes;
    
    public ByteArrayComparator(byte[] inBytes, boolean inDescending) { 
      super(inDescending);
      mBytes=inBytes; 
    }
    
    public ByteArrayComparator(byte[] inBytes) { this(inBytes,false); }
    
    public long getLength() { return mBytes.length; }
    public byte[] getBytes() { return mBytes; }
    public byte getByte(long inIndex) { return mBytes[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) { 
      long theDiff=getByte(inIndex1)-getByte(inIndex2); 
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }
    
    public void swapIndexes(long inIndex1, long inIndex2) {
      byte theTemp=mBytes[(int) inIndex1];
      mBytes[(int) inIndex1]=mBytes[(int) inIndex2];
      mBytes[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class CharArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class CharArrayComparator extends ArrayComparator {

    private char[]  mChars;
    private byte    mHandleCase;

    public CharArrayComparator(char[] inChars, byte inHandleCase, boolean inDescending) { 
      super(inDescending);
      mChars=inChars; 
      mHandleCase=inHandleCase;
    }
    
    public CharArrayComparator(char[] inChars, byte inHandleCase) { this(inChars,inHandleCase,false); }

    public long getLength() { return mChars.length; }
    public char[] getChars() { return mChars; }
    public char getChar(long inIndex) { return mChars[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) { 
      return Comparisons.compareChars(getChar(inIndex1),getChar(inIndex2),mHandleCase); }

    public void swapIndexes(long inIndex1, long inIndex2) {
      char theTemp=mChars[(int) inIndex1];
      mChars[(int) inIndex1]=mChars[(int) inIndex2];
      mChars[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class IntArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class IntArrayComparator extends ArrayComparator {

    private int[]  mInts;

    public IntArrayComparator(int[] inInts, boolean inDescending) { 
      super(inDescending);
      mInts=inInts; 
    }
    
    public IntArrayComparator(int[] inInts) { this(inInts,false); }

    public long getLength() { return mInts.length; }
    public int[] getInts() { return mInts; }
    public int getInt(long inIndex) { return mInts[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) { 
      long theDiff=getInt(inIndex1)-getInt(inIndex2); 
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }

    public void swapIndexes(long inIndex1, long inIndex2) {
      int theTemp=mInts[(int) inIndex1];
      mInts[(int) inIndex1]=mInts[(int) inIndex2];
      mInts[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class FloatArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class FloatArrayComparator extends ArrayComparator {

    private float[]  mFloats;

    public FloatArrayComparator(float[] inFloats, boolean inDescending) { 
      super(inDescending);
      mFloats=inFloats; 
    }
    
    public FloatArrayComparator(float[] inFloats) { this(inFloats,false); }

    public long getLength() { return mFloats.length; }
    public float[] getFloats() { return mFloats; }
    public float getFloat(long inIndex) { return mFloats[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) {
      float theDiff=getFloat(inIndex1)-getFloat(inIndex2);
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }

    public void swapIndexes(long inIndex1, long inIndex2) {
      float theTemp=mFloats[(int) inIndex1];
      mFloats[(int) inIndex1]=mFloats[(int) inIndex2];
      mFloats[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class LongArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class LongArrayComparator extends ArrayComparator {

    private long[]  mLongs;

    public LongArrayComparator(long[] inLongs, boolean inDescending) { 
      super(inDescending);
      mLongs=inLongs; 
    }
    
    public LongArrayComparator(long[] inLongs) { this(inLongs,false); }

    public long getLength() { return mLongs.length; }
    public long[] getLongs() { return mLongs; }
    public long getLong(long inIndex) { return mLongs[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) {
      long theDiff=getLong(inIndex1)-getLong(inIndex2);
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }

    public void swapIndexes(long inIndex1, long inIndex2) {
      long theTemp=mLongs[(int) inIndex1];
      mLongs[(int) inIndex1]=mLongs[(int) inIndex2];
      mLongs[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class DoubleArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class DoubleArrayComparator extends ArrayComparator {

    private double[]  mDoubles;

    public DoubleArrayComparator(double[] inDoubles, boolean inDescending) { 
      super(inDescending);
      mDoubles=inDoubles;       
    }
    
    public DoubleArrayComparator(double[] inDoubles) { this(inDoubles,false); }

    public long getLength() { return mDoubles.length; }
    public double[] getDoubles() { return mDoubles; }
    public double getDouble(long inIndex) { return mDoubles[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) {
      double theDiff=getDouble(inIndex1)-getDouble(inIndex2);
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }

    public void swapIndexes(long inIndex1, long inIndex2) {
      double theTemp=mDoubles[(int) inIndex1];
      mDoubles[(int) inIndex1]=mDoubles[(int) inIndex2];
      mDoubles[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ObjectArrayComparator
  //------------------------------------------------------------------------------------------------------

  public abstract static class ObjectArrayComparator extends ArrayComparator {

    private Object[]  mObjects;

    public ObjectArrayComparator(Object[] inObjects, boolean inDescending) { 
      super(inDescending);
      mObjects=inObjects; 
    }
    
    public ObjectArrayComparator(Object[] inObjects) { this(inObjects,false); }

    public long getLength() { return mObjects.length; }
    public Object[] getObjects() { return mObjects; }
    public Object getObject(long inIndex) { return mObjects[(int) inIndex]; }

    public void swapIndexes(long inIndex1, long inIndex2) {
      Object theTemp=mObjects[(int) inIndex1];
      mObjects[(int) inIndex1]=mObjects[(int) inIndex2];
      mObjects[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ComparableArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class ComparableArrayComparator extends ObjectArrayComparator {

    public ComparableArrayComparator(Comparable[] inComparables, boolean inDescending) {
      super(inComparables,inDescending); }
    
    public ComparableArrayComparator(Comparable[] inComparables) { this(inComparables,false); }

    public Comparable[] getComparables() { return (Comparable[]) getObjects(); }
    public Comparable getComparable(long inIndex) { return (Comparable) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return getComparable(inIndex1).compareTo(getComparable(inIndex2)); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class StringArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class StringArrayComparator extends ObjectArrayComparator {

    private byte      mHandleCase;

    public StringArrayComparator(String[] inStrings, byte inHandleCase, boolean inDescending) {
      super(inStrings,inDescending);
      mHandleCase=inHandleCase;
    }
    
    public StringArrayComparator(String[] inStrings, byte inHandleCase) {
      this(inStrings,inHandleCase,false); }

    public String[] getStrings() { return (String[]) getObjects(); }
    public String getString(long inIndex) { return (String) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return Comparisons.compareStrings(getString(inIndex1),getString(inIndex2),mHandleCase); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class SubstringArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class SubstringArrayComparator extends StringArrayComparator {

    private int    mStartPos;
    private int    mEndPos;
    private byte   mHandleCase;

    public SubstringArrayComparator(String[] inStrings, int inStartPos, int inEndPos,
        byte inHandleCase, boolean inDescending) {
      super(inStrings,inHandleCase,inDescending);
      mStartPos=inStartPos;
      mEndPos=inEndPos;
      mHandleCase=inHandleCase;
    }
    
    public SubstringArrayComparator(String[] inStrings, int inStartPos, int inEndPos, byte inHandleCase) { 
      this(inStrings,inStartPos,inEndPos,inHandleCase,false); }

    public SubstringArrayComparator(String[] inStrings, int inStartPos, byte inHandleCase) {
      this(inStrings,inStartPos,kNotFound,inHandleCase); }

    public String getSubString(long inIndex) { 
      String theString=getString(inIndex);
      if (mEndPos==kNotFound)
        return theString.substring(mStartPos);
      else
        return theString.substring(mStartPos,Math.min(mEndPos,theString.length()));
    }

    public long compareIndexes(long inIndex1, long inIndex2) {
      String theString1=getString(inIndex1);
      String theString2=getString(inIndex2);
      if (mEndPos==kNotFound)
        return Comparisons.compareStrings(
            theString1.substring(mStartPos),
            theString2.substring(mStartPos),
            mHandleCase);
      else
        return Comparisons.compareStrings(
            theString1.substring(mStartPos,Math.min(mEndPos,theString1.length())),
            theString2.substring(mStartPos,Math.min(mEndPos,theString2.length())),
            mHandleCase);
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class BytesArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class BytesArrayComparator extends ObjectArrayComparator {

    private byte   mHandleCase;

    public BytesArrayComparator(byte[][] inBytes, byte inHandleCase, boolean inDescending) {
      super(inBytes,inDescending);
      mHandleCase=inHandleCase;
    }

    public BytesArrayComparator(byte[][] inBytes, byte inHandleCase) {
      this(inBytes,inHandleCase,false); }

    public byte[][] getBytess() { return (byte[][]) getObjects(); }
    public byte[] getBytes(long inIndex) { return (byte[]) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return Comparisons.compareBytes(getBytes(inIndex1),getBytes(inIndex2),mHandleCase); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class CharsArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class CharsArrayComparator extends ObjectArrayComparator {

    private byte  mHandleCase;

    public CharsArrayComparator(char[][] inChars, byte inHandleCase, boolean inDescending) {
      super(inChars,inDescending);
      mHandleCase=inHandleCase;
    }

    public CharsArrayComparator(char[][] inChars, byte inHandleCase) {
      this(inChars,inHandleCase,false); }

    public char[][] getCharss() { return (char[][]) getObjects(); }
    public char[] getChars(long inIndex) { return (char[]) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return Comparisons.compareChars(getChars(inIndex1),getChars(inIndex2),mHandleCase); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class IntsArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class IntsArrayComparator extends ObjectArrayComparator {

    public IntsArrayComparator(int[][] inInts, boolean inDescending) { super(inInts,inDescending); }

    public IntsArrayComparator(int[][] inInts) { this(inInts,false); }

    public int[][] getIntss() { return (int[][]) getObjects(); }
    public int[] getInts(long inIndex) { return (int[]) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return Comparisons.compareInts(getInts(inIndex1),getInts(inIndex2)); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class LongsArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class LongsArrayComparator extends ObjectArrayComparator {

    public LongsArrayComparator(long[][] inLongs, boolean inDescending) { super(inLongs,inDescending); }

    public LongsArrayComparator(long[][] inLongs) { this(inLongs,false); }

    public long[][] getLongss() { return (long[][]) getObjects(); }
    public long[] getLongs(long inIndex) { return (long[]) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return Comparisons.compareLongs(getLongs(inIndex1),getLongs(inIndex2)); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class DoublesArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class DoublesArrayComparator extends ObjectArrayComparator {

    public DoublesArrayComparator(double[][] inDoubles, boolean inDescending) { 
      super(inDoubles,inDescending); }

    public DoublesArrayComparator(double[][] inDoubles) { this(inDoubles,false); }

    public double[][] getDoubless() { return (double[][]) getObjects(); }
    public double[] getDoubles(long inIndex) { return (double[]) getObject(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return Comparisons.compareDoubles(getDoubles(inIndex1),getDoubles(inIndex2)); }
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  //------------------------------------------------------------------------------------------------------
  // Inner class StoreComparator
  //
  // Parent class for all store comparators
  //------------------------------------------------------------------------------------------------------

  public abstract static class StoreComparator extends Comparator {

    private StoreInterface  mStore;
    
    public StoreComparator(StoreInterface inStore, boolean inDescending) { 
      super(inDescending); 
      mStore=inStore;
    }
    
    public StoreComparator(StoreInterface inStore) { this(inStore,false); }

    public StoreComparator makeThreadCopy() { return this; } 

    public long getLength() { return mStore.getSize(); }
    public StoreInterface getStore() { return mStore; }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ByteStoreComparator
  //------------------------------------------------------------------------------------------------------

  public static class ByteStoreComparator extends StoreComparator {
    
    public ByteStoreComparator(ByteStore inByteStore, boolean inDescending) { 
      super(inByteStore,inDescending); }

    public ByteStoreComparator(ByteStore inByteStore) { this(inByteStore,false); }

    public ByteStore getByteStore() { return (ByteStore) getStore(); }
    public byte getByte(long inIndex) { return getByteStore().getByte(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) { 
      long theDiff=getByte(inIndex1)-getByte(inIndex2); 
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }

    public void swapIndexes(long inIndex1, long inIndex2) {
      ByteStore theByteStore=getByteStore();
      byte theTemp=theByteStore.getByte(inIndex1);
      theByteStore.setByte(inIndex1,theByteStore.getByte(inIndex2));
      theByteStore.setByte(inIndex2,theTemp);
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ByteDataStoreComparator
  //------------------------------------------------------------------------------------------------------

  public static class ByteDataStoreComparator extends StoreComparator {
    
    private byte    mHandleCase;
    
    private Thread  mThread;
    private byte[]  mBytes1;
    private byte[]  mBytes2;
    
    public ByteDataStoreComparator(ByteDataStore inByteDataStore, byte inHandleCase, boolean inDescending) { 
      super(inByteDataStore,inDescending);
      mHandleCase=inHandleCase;
      mThread=Thread.currentThread();
    }
    
    public ByteDataStoreComparator(ByteDataStore inByteDataStore, byte inHandleCase) { 
      this(inByteDataStore,inHandleCase,false); }
    
    // Some comparators have buffers.  
    // To make them thread safe, need different buffers per thread.
    public StoreComparator makeThreadCopy() { 
      return (mThread==Thread.currentThread())?this: 
        new ByteDataStoreComparator(getByteDataStore(),mHandleCase,getDescending());
    }
    // Release buffers
    public void close() { 
      if (mBytes1.length==SliceStore.kByteSliceSize) {
        SliceStore.getSliceStore().putByteSlice(mBytes1);
        SliceStore.getSliceStore().putByteSlice(mBytes2);
      }
    }

    public ByteDataStore getByteDataStore() { return (ByteDataStore) getStore(); }
    public byte[] getBytes(long inIndex) { return getByteDataStore().getBytes(inIndex); }
    public String getUTF8(long inIndex) { return getByteDataStore().getUTF8(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      ByteDataStore theByteDataStore=getByteDataStore();
      int theNBytes1=(int) theByteDataStore.getNBytes(inIndex1);
      int theNBytes2=(int) theByteDataStore.getNBytes(inIndex2);
      if ((mBytes1==null)||(theNBytes1>mBytes1.length)||(theNBytes2>mBytes2.length)) {
        int theBufferSize=2*Math.max(theNBytes1,theNBytes2);
        if (theBufferSize<=k1K) {
          mBytes1=Allocate.newBytes(k1K);
          mBytes2=Allocate.newBytes(k1K);
        } else if (theBufferSize<=SliceStore.kByteSliceSize) {
          mBytes1=SliceStore.getSliceStore().getByteSlice();
          mBytes2=SliceStore.getSliceStore().getByteSlice();
        } else {
          mBytes1=Allocate.newBytes(theBufferSize);
          mBytes2=Allocate.newBytes(theBufferSize);
        }
      }      
      theByteDataStore.getBytesAtN(inIndex1,0,mBytes1,0,theNBytes1);
      theByteDataStore.getBytesAtN(inIndex2,0,mBytes2,0,theNBytes2);
      return UTF8Utils.compareUTF8Bytes(mBytes1,0,theNBytes1,mBytes2,0,theNBytes2,mHandleCase); 
    }
    
    public void swapIndexes(long inIndex1, long inIndex2) { 
      throw new RuntimeException("Comparator can only be used for SortMaps, not sorts"); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class VarStoreComparator
  //------------------------------------------------------------------------------------------------------

  public static class VarStoreComparator extends StoreComparator {
    
    public VarStoreComparator(VarStore inVarStore, boolean inDescending) { 
      super(inVarStore,inDescending); }

    public VarStoreComparator(VarStore inVarStore) { this(inVarStore,false); }

    public VarStore getVarStore() { return (VarStore) getStore(); }
    public long getLong(long inIndex) { return getVarStore().getLong(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) { 
      long theDiff=getLong(inIndex1)-getLong(inIndex2); 
      if (theDiff>0)
        return kGreaterThan;
      else if (theDiff<0)
        return kLessThan;
      else
        return kEquals;
    }
    
    public void swapIndexes(long inIndex1, long inIndex2) {
      VarStore theVarStore=getVarStore();
      long theTemp=theVarStore.getLong(inIndex1);
      theVarStore.setVar(inIndex1,theVarStore.getLong(inIndex2));
      theVarStore.setVar(inIndex2,theTemp);
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class VarDataStoreComparator
  //------------------------------------------------------------------------------------------------------

  public static class VarDataStoreComparator extends StoreComparator {
    
    private Thread  mThread;
    private long[]  mVars1;
    private long[]  mVars2;

    public VarDataStoreComparator(VarDataStore inVarDataStore, boolean inDescending) { 
      super(inVarDataStore,inDescending); 
      mThread=Thread.currentThread();
    }
    
    // Some comparators have buffers.  
    // To make them thread safe, need different buffers per thread.
    public StoreComparator makeThreadCopy() { 
      return (mThread==Thread.currentThread())?this: 
        new VarDataStoreComparator(getVarDataStore(),getDescending());
    }
    // Release buffers
    public void close() { 
      if (mVars1.length==SliceStore.kLongSliceSize) {
        SliceStore.getSliceStore().putLongSlice(mVars1);
        SliceStore.getSliceStore().putLongSlice(mVars2);
      }
    }

    public VarDataStore getVarDataStore() { return (VarDataStore) getStore(); }
    public long[] getLongs(long inIndex) { return getVarDataStore().getLongs(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      VarDataStore theVarDataStore=getVarDataStore();
      int theNVars1=(int) theVarDataStore.getNVars(inIndex1);
      int theNVars2=(int) theVarDataStore.getNVars(inIndex2);
      if ((mVars1==null)||(theNVars1>mVars1.length)||(theNVars2>mVars2.length)) {
        int theBufferSize=2*Math.max(theNVars1,theNVars2);
        if (theBufferSize<=k1K) {
          mVars1=Allocate.newLongs(k1K);
          mVars2=Allocate.newLongs(k1K);
        } else if (theBufferSize<=SliceStore.kLongSliceSize) {
          mVars1=SliceStore.getSliceStore().getLongSlice();
          mVars2=SliceStore.getSliceStore().getLongSlice();
        } else {
          mVars1=Allocate.newLongs(theBufferSize);
          mVars2=Allocate.newLongs(theBufferSize);
        }
      }      
      theVarDataStore.getVars(inIndex1,mVars1,0);
      theVarDataStore.getVars(inIndex2,mVars2,0);
      return Comparisons.compareLongs(mVars1,0,theNVars1,mVars2,0,theNVars2); 
    }
    
    public void swapIndexes(long inIndex1, long inIndex2) { 
      throw new RuntimeException("Comparator can only be used for SortMaps, not sorts"); }
  }

}

