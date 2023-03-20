//--------------------------------------------------------------------------------------------------------
// Finders.java
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import java.util.ArrayList;

import gravel.store.*;
import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// Finders
//--------------------------------------------------------------------------------------------------------

public class Finders implements Constants {

//--------------------------------------------------------------------------------------------------------
// Finders consts
//--------------------------------------------------------------------------------------------------------

  public static final byte  kLessThan=Comparators.kLessThan;          // -1
  public static final byte  kEquals=Comparators.kEquals;              //  0
  public static final byte  kGreaterThan=Comparators.kGreaterThan;    //  1

//--------------------------------------------------------------------------------------------------------
// Finder inner classes
//--------------------------------------------------------------------------------------------------------

  //------------------------------------------------------------------------------------------------------
  // Inner class Finder
  //------------------------------------------------------------------------------------------------------

  public abstract static class Finder implements Constants {
    
    private boolean  mDescending;
    
    // Child classes implement constructor, with typed target, like:
    // public Finder(long inTarget, boolean inDescending) { 
    //   super(inDescending); 
    //   mTarget=inTarget; 
    // }

    public Finder(boolean inDescending) { mDescending=inDescending; }
    public Finder() { this(false); }
    
    // Child classes implement typed get:
    // public long getTarget() { return mTarget; } 

    public boolean getDescending() { return mDescending; }
    
    public abstract long getSize();
    public abstract int compareToIndex(long inIndex);
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ArrayFinder
  //------------------------------------------------------------------------------------------------------

  public abstract static class ArrayFinder extends Finder {
    public ArrayFinder(boolean inDescending) { super(inDescending); }
    public ArrayFinder() { this(false); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ByteArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class ByteArrayFinder extends ArrayFinder {

    private byte[]  mSortedBytes;
    private byte    mTarget;

    public ByteArrayFinder(byte inTarget, byte[] inSortedBytes, boolean inDescending) {
      super(inDescending);
      mSortedBytes=inSortedBytes;
      mTarget=inTarget;
    }

    public byte getTarget() { return mTarget; } 
    public ByteArrayFinder(byte inTarget, byte[] inSortedBytes) {
      this(inTarget,inSortedBytes,false); }

    public long getSize() { return mSortedBytes.length; }
    public int compareToIndex(long inIndex) { 
      int theDiff=mTarget-mSortedBytes[(int) inIndex]; 
      if (theDiff>0)
        theDiff=kGreaterThan;
      else if (theDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class CharArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class CharArrayFinder extends ArrayFinder {

    private char[]  mSortedChars;
    private char    mTarget;
    private byte    mHandleCase;

    public CharArrayFinder(char inTarget, char[] inSortedChars, byte inHandleCase, boolean inDescending) {
      super(inDescending);
      mSortedChars=inSortedChars;
      mTarget=inTarget;
      mHandleCase=inHandleCase;
    }

    public CharArrayFinder(char inTarget, char[] inSortedChars, byte inHandleCase) {
      this(inTarget,inSortedChars,inHandleCase,false); }

    public char getTarget() { return mTarget; } 
    public long getSize() { return mSortedChars.length; }
    public int compareToIndex(long inIndex) { 
      int theDiff=Comparisons.compareChars(mTarget,mSortedChars[(int) inIndex],mHandleCase); 
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class IntArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class IntArrayFinder extends ArrayFinder {

    private int[]  mSortedInts;
    private int    mTarget;

    public IntArrayFinder(int inTarget, int[] inSortedInts, boolean inDescending) {
      super(inDescending);
      mSortedInts=inSortedInts;
      mTarget=inTarget;
    }

    public IntArrayFinder(int inTarget, int[] inSortedInts) {
      this(inTarget,inSortedInts,false); }

    public int getTarget() { return mTarget; } 
    public long getSize() { return mSortedInts.length; }
    public int compareToIndex(long inIndex) { 
      int theDiff=mTarget-mSortedInts[(int) inIndex]; 
      if (theDiff>0)
        theDiff=kGreaterThan;
      else if (theDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class LongArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class LongArrayFinder extends ArrayFinder {

    private long[]  mSortedLongs;
    private long    mTarget;

    public LongArrayFinder(long inTarget, long[] inSortedLongs, boolean inDescending) {
      super(inDescending);
      mSortedLongs=inSortedLongs;
      mTarget=inTarget;
    }

    public LongArrayFinder(long inTarget, long[] inSortedLongs) {
      this(inTarget,inSortedLongs,false); }

    public long getTarget() { return mTarget; } 
    public long getSize() { return mSortedLongs.length; }
    public int compareToIndex(long inIndex) {
      long theLongDiff=mTarget-mSortedLongs[(int) inIndex];
      int theDiff=0;
      if (theLongDiff>0)
        theDiff=kGreaterThan;
      else if (theLongDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class FloatArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class FloatArrayFinder extends ArrayFinder {

    private float[]  mSortedFloats;
    private float    mTarget;

    public FloatArrayFinder(float inTarget, float[] inSortedFloats, boolean inDescending) {
      super(inDescending);
      mSortedFloats=inSortedFloats;
      mTarget=inTarget;
    }

    public FloatArrayFinder(float inTarget, float[] inSortedFloats) {
      this(inTarget,inSortedFloats,false); }

    public float getTarget() { return mTarget; } 
    public long getSize() { return mSortedFloats.length; }
    public int compareToIndex(long inIndex) {
      float theFloatDiff=mTarget-mSortedFloats[(int) inIndex];
      int theDiff=0;
      if (theFloatDiff>0)
        theDiff=kGreaterThan;
      else if (theFloatDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class DoubleArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class DoubleArrayFinder extends ArrayFinder {

    private double[]  mSortedDoubles;
    private double    mTarget;

    public DoubleArrayFinder(double inTarget, double[] inSortedDoubles, boolean inDescending) {
      super(inDescending);
      mSortedDoubles=inSortedDoubles;
      mTarget=inTarget;
    }

    public DoubleArrayFinder(double inTarget, double[] inSortedDoubles) {
      this(inTarget,inSortedDoubles,false); }

    public double getTarget() { return mTarget; } 
    public long getSize() { return mSortedDoubles.length; }
    public int compareToIndex(long inIndex) {
      double theDoubleDiff=mTarget-mSortedDoubles[(int) inIndex];
      int theDiff=0;
      if (theDoubleDiff>0)
        theDiff=kGreaterThan;
      else if (theDoubleDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ObjectArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static abstract class ObjectArrayFinder extends ArrayFinder {

    private Object[]      mSortedObjects;
    private Object        mTarget;

    public ObjectArrayFinder(Object inTarget, Object[] inSortedObjects, boolean inDescending) {
      super(inDescending);
      mSortedObjects=inSortedObjects;
      mTarget=inTarget;
    }

    public ObjectArrayFinder(Object inTarget, Object[] inSortedObjects) {
      this(inTarget,inSortedObjects,false); }

    public Object getTarget() { return mTarget; } 
    public long getSize() { return mSortedObjects.length; }
    
    public abstract int compareToIndex(long inIndex);
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ComparableArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class ComparableArrayFinder extends ArrayFinder {

    private Comparable[]    mSortedComparables;
    private Comparable      mTarget;

    public ComparableArrayFinder(Comparable inTarget, Comparable[] inSortedComparables, boolean inDescending) {
      super(inDescending);
      mSortedComparables=inSortedComparables;
      mTarget=inTarget;
    }

    public ComparableArrayFinder(Comparable inTarget, Comparable[] inSortedComparables) {
      this(inTarget,inSortedComparables,false); }

    public Comparable getTarget() { return mTarget; } 
    public long getSize() { return mSortedComparables.length; }
    public int compareToIndex(long inIndex) {
      int theDiff=mTarget.compareTo(mSortedComparables[(int) inIndex]); 
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class StringArrayFinder
  //------------------------------------------------------------------------------------------------------

  public static class StringArrayFinder extends ArrayFinder {

    private String[]  mSortedStrings;
    private byte      mHandleCase;
    private String    mTarget;

    public StringArrayFinder(String inTarget, String[] inSortedStrings, byte inHandleCase, boolean inDescending) {
      super(inDescending);
      mSortedStrings=inSortedStrings;
      mHandleCase=inHandleCase;
      mTarget=inTarget;
    }

    public StringArrayFinder(String inTarget, String[] inSortedStrings, byte inHandleCase) {
      this(inTarget,inSortedStrings,inHandleCase,false); }

    public String getTarget() { return mTarget; } 
    public long getSize() { return mSortedStrings.length; }
    public int compareToIndex(long inIndex) {
      int theDiff=Comparisons.compareStrings(mTarget,mSortedStrings[(int) inIndex],mHandleCase); 
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ComparableArrayListFinder
  //------------------------------------------------------------------------------------------------------

  public static class ComparableArrayListFinder extends ArrayFinder {

    private ArrayList       mSortedComparables;
    private Comparable      mTarget;

    public ComparableArrayListFinder(Comparable inTarget, ArrayList inSortedComparables, boolean inDescending) {
      super(inDescending);
      mSortedComparables=inSortedComparables;
      mTarget=inTarget;
    }

    public ComparableArrayListFinder(Comparable inTarget, ArrayList inSortedComparables) {
      this(inTarget,inSortedComparables,false); }

    public Comparable getTarget() { return mTarget; } 
    public long getSize() { return mSortedComparables.size(); }
    public int compareToIndex(long inIndex) {
      int theDiff=mTarget.compareTo(mSortedComparables.get((int) inIndex)); 
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class StoreFinder
  //------------------------------------------------------------------------------------------------------

  public abstract static class StoreFinder extends Finder {

    private StoreInterface  mSortedStore;

    public StoreFinder(StoreInterface inSortedStore, boolean inDescending) { 
      super(inDescending);
      mSortedStore=inSortedStore; 
    }

    public StoreFinder(StoreInterface inSortedStore) {
      this(inSortedStore,false); }

    public StoreInterface getSortedStore() { return mSortedStore; }
    public long getSize() { return mSortedStore.getSize(); }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class ByteStoreFinder
  //------------------------------------------------------------------------------------------------------

  public static class ByteStoreFinder extends StoreFinder {

    private byte       mTarget;

    public ByteStoreFinder(byte inTarget, ByteStore inSortedByteStore, boolean inDescending) {
      super(inSortedByteStore,inDescending);
      mTarget=inTarget;
    }

    public ByteStoreFinder(byte inTarget, ByteStore inSortedByteStore) {
      this(inTarget,inSortedByteStore,false); }

    public byte getTarget() { return mTarget; } 
    public int compareToIndex(long inIndex) { 
      int theDiff=mTarget-((ByteStore) getSortedStore()).getByte(inIndex); 
      if (theDiff>0)
        theDiff=kGreaterThan;
      else if (theDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class VarStoreFinder
  //------------------------------------------------------------------------------------------------------

  public static class VarStoreFinder extends StoreFinder {

    private long       mTarget;

    public VarStoreFinder(long inTarget, VarStore inSortedVarStore, boolean inDescending) {
      super(inSortedVarStore,inDescending);
      mTarget=inTarget;
    }

    public VarStoreFinder(long inTarget, VarStore inSortedVarStore) {
      this(inTarget,inSortedVarStore,false); }

    public long getTarget() { return mTarget; } 
    public int compareToIndex(long inIndex) { 
      long theLongDiff=mTarget-((VarStore) getSortedStore()).getLong(inIndex); 
      int theDiff=0;
      if (theLongDiff>0)
        theDiff=kGreaterThan;
      else if (theLongDiff<0)
        theDiff=kLessThan;
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class UTF8StoreFinder
  //------------------------------------------------------------------------------------------------------

  public static class UTF8StoreFinder extends StoreFinder {

    private String       mTarget;

    public UTF8StoreFinder(String inTarget, ByteDataStore inSortedUTF8Store, boolean inDescending) {
      super(inSortedUTF8Store,inDescending);
      mTarget=inTarget;
    }

    public UTF8StoreFinder(String inTarget, ByteDataStore inSortedUTF8Store) {
      this(inTarget,inSortedUTF8Store,false); }

    public String getTarget() { return mTarget; } 
    public int compareToIndex(long inIndex) { 
      int theDiff=mTarget.compareTo(((ByteDataStore) getSortedStore()).getUTF8(inIndex)); 
      if (getDescending())
        theDiff*=-1;
      return theDiff;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class FinderCascade
  //------------------------------------------------------------------------------------------------------

  public static class FinderCascade extends Finder {

    private Finder[]  mFinders;

    public FinderCascade(Finder[] inFinders) { 
      super(false);
      mFinders=inFinders; 
    }

    public long getSize() { return mFinders[0].getSize(); }
    
    public int compareToIndex(long inIndex) { 
      for (int i=0; i<mFinders.length; i++) {
        int theDiff=mFinders[i].compareToIndex(inIndex);
        if (theDiff>0)
          return kGreaterThan;
        else if (theDiff<0)
          return kLessThan;
      }
      return kEquals; 
    }
  }

}






