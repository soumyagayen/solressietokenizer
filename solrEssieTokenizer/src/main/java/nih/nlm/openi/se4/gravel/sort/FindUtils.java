//--------------------------------------------------------------------------------------------------------
// FindUtils
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import java.util.ArrayList;

import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// FindUtils
//--------------------------------------------------------------------------------------------------------

public class FindUtils extends Finders {

//--------------------------------------------------------------------------------------------------------
// narrow
//--------------------------------------------------------------------------------------------------------

  public static long narrow(long inLeftIndex, long inRightIndex, Finder inFinder) {

    long theLeftIndex=inLeftIndex;
    long theRightIndex=inRightIndex;

    // Find limit using binary search
    // Maintain bottom < target < top
    while (true) {
      
      // Continue until there are no more midpts:  target not in array
      if (theRightIndex-1==theLeftIndex)
        return kNotFound;

      long theMidIndex=(theLeftIndex+theRightIndex)/2;
      int theComparison=inFinder.compareToIndex(theMidIndex);
      if (theComparison==0) 
        return theMidIndex;  // Found it
      else if (theComparison>0)
        theLeftIndex=theMidIndex;
      else 
        theRightIndex=theMidIndex;
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// findIndex
//
// Returns kNotFound=-1 if target not in array
// If array multiple valued, returns any index with target value
// Used to test target value in array
//--------------------------------------------------------------------------------------------------------

  public static long findIndex(long inFirstIndex, long inNValues, Finder inFinder) {
    if (inNValues==0)
      return kNotFound;

    // Check if target outside range
    long theTopIndex=inFirstIndex+inNValues-1;
    int theComparison=inFinder.compareToIndex(theTopIndex);
    if (theComparison==0)
      return theTopIndex;  // Found it
    else if (theComparison>0)
      return kNotFound;
    else {
      // target less than or equal top - possibly in range

      long theBottomIndex=inFirstIndex;
      theComparison=inFinder.compareToIndex(theBottomIndex);
      if (theComparison==0)
        return theBottomIndex;  // Found it
      else if (theComparison<0)
        return kNotFound;
      else {
        // target greater than bottom - in range

        return narrow(theBottomIndex,theTopIndex,inFinder);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndex
//
// As above, but takes a start index, which is assumed to be near the target
//--------------------------------------------------------------------------------------------------------

  public static long findIndex(long inStartIndex, long inFirstIndex, long inNValues, Finder inFinder) {
    if (inNValues==0)
      return kNotFound;

    // Compare target with start
    int theComparison=inFinder.compareToIndex(inStartIndex);
    if (theComparison==0)
      return inStartIndex;  // Found it
    
    else if (theComparison<0) {
      // target less than start - expand left
      long theBottomIndex=inFirstIndex;
      long theLeftIndex=Math.max(theBottomIndex,inStartIndex-1);
      long theRightIndex=inStartIndex;

      // Bracket target using binary expansion
      // Maintain bottom < target < top
      while (true) {
        theComparison=inFinder.compareToIndex(theLeftIndex);
        if (theComparison==0)
          return theLeftIndex;  // Found it
        else if (theComparison>0) 
          return narrow(theLeftIndex,theRightIndex,inFinder);
        else if (theLeftIndex==theBottomIndex)
          return kNotFound;
        else {
          long theStep=theRightIndex-theLeftIndex;
          theRightIndex=theLeftIndex;
          theLeftIndex=Math.max(theBottomIndex,theLeftIndex-2*theStep);
        }
      }
      
    } else {
      // target greater than start - expand right
      long theTopIndex=inFirstIndex+inNValues-1;
      long theRightIndex=Math.min(theTopIndex,inStartIndex+1);
      long theLeftIndex=inStartIndex;

      // Bracket target using binary expansion
      // Maintain bottom < target < top
      while (true) {
        theComparison=inFinder.compareToIndex(theRightIndex);
        if (theComparison==0)
          return theRightIndex;  // Found it
        else if (theComparison<0) 
          return narrow(theLeftIndex,theRightIndex,inFinder);
        else if (theRightIndex==theTopIndex)
          return kNotFound;
        else {
          long theStep=theRightIndex-theLeftIndex;
          theLeftIndex=theRightIndex;
          theRightIndex=Math.min(theTopIndex,theRightIndex+2*theStep);
        }
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndex for ranges in arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndex(char inTarget, long inFirstIndex, long inNValues, 
      char[] inSortedValues, byte inHandleCase) {
    return findIndex(inFirstIndex,inNValues,new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndex(int inTarget, long inFirstIndex, long inNValues, 
      int[] inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inTarget, long inFirstIndex, long inNValues, 
      long[] inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(float inTarget, long inFirstIndex, long inNValues, 
      float[] inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(double inTarget, long inFirstIndex, long inNValues, 
      double[] inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(Comparable inTarget, long inFirstIndex, long inNValues, 
      Comparable[] inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(String inTarget, long inFirstIndex, long inNValues, 
      String[] inSortedValues, byte inHandleCase) {
    return findIndex(inFirstIndex,inNValues,new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndex(Comparable inTarget, long inFirstIndex, long inNValues, 
      ArrayList inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndex(byte inTarget, long inFirstIndex, long inNValues, 
      ByteStore inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inTarget, long inFirstIndex, long inNValues, 
      VarStore inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndex(String inTarget, long inFirstIndex, long inNValues, 
      ByteDataStore inSortedValues) {
    return findIndex(inFirstIndex,inNValues,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndex for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndex(Finder inFinder) {
    return findIndex(0,inFinder.getSize(),inFinder); }

  public static long findIndex(char inTarget, char[] inSortedValues, byte inHandleCase) {
    return findIndex(new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndex(int inTarget, int[] inSortedValues) {
    return findIndex(new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inTarget, long[] inSortedValues) {
    return findIndex(new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(float inTarget, float[] inSortedValues) {
    return findIndex(new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(double inTarget, double[] inSortedValues) {
    return findIndex(new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(Comparable inTarget, Comparable[] inSortedValues) {
    return findIndex(new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(String inTarget, String[] inSortedValues, byte inHandleCase) {
    return findIndex(new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndex(Comparable inTarget, ArrayList inSortedValues) {
    return findIndex(new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndex(byte inTarget, ByteStore inSortedValues) {
    return findIndex(new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inTarget, VarStore inSortedValues) {
    return findIndex(new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndex(String inTarget, ByteDataStore inSortedValues) {
    return findIndex(new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndex with start index for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndex(long inStartindex, Finder inFinder) {
    return findIndex(inStartindex,0,inFinder.getSize(),inFinder); }

  public static long findIndex(long inStartindex, char inTarget, char[] inSortedValues, byte inHandleCase) {
    return findIndex(inStartindex,new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndex(long inStartindex, int inTarget, int[] inSortedValues) {
    return findIndex(inStartindex,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, long inTarget, long[] inSortedValues) {
    return findIndex(inStartindex,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, float inTarget, float[] inSortedValues) {
    return findIndex(inStartindex,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, double inTarget, double[] inSortedValues) {
    return findIndex(inStartindex,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, Comparable inTarget, Comparable[] inSortedValues) {
    return findIndex(inStartindex,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, String inTarget, String[] inSortedValues, byte inHandleCase) {
    return findIndex(inStartindex,new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndex(long inStartindex, Comparable inTarget, ArrayList inSortedValues) {
    return findIndex(inStartindex,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, byte inTarget, ByteStore inSortedValues) {
    return findIndex(inStartindex,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, long inTarget, VarStore inSortedValues) {
    return findIndex(inStartindex,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndex(long inStartindex, String inTarget, ByteDataStore inSortedValues) {
    return findIndex(inStartindex,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// Example of findIndex for multiple targets in multiple arrays
//--------------------------------------------------------------------------------------------------------

  public static long findIndex(long inStartindex, char inTarget1, double inTarget2, 
      long inFirstIndex, long inNValues, char[] inSortedValues1, byte inHandleCase, double[] inSortedValues2) {
    return findIndex(inStartindex,inFirstIndex,inNValues,new FinderCascade(new Finder[] {
        new CharArrayFinder(inTarget1,inSortedValues1,inHandleCase),
        new DoubleArrayFinder(inTarget2,inSortedValues2)})); }

//--------------------------------------------------------------------------------------------------------
// narrowAfter
//--------------------------------------------------------------------------------------------------------

  public static long narrowAfter(long inLeftIndex, long inRightIndex, Finder inFinder) {

    long theLeftIndex=inLeftIndex;
    long theRightIndex=inRightIndex;

    // Find limit using binary search
    // Maintain bottom <= target < top
    while (true) {
      
      // Continue until find smallest index greater than target:  index-1 <= target < index
      if (theRightIndex-1==theLeftIndex)
        return theRightIndex;

      long theMidIndex=(theLeftIndex+theRightIndex)/2;
      int theComparison=inFinder.compareToIndex(theMidIndex);
      if (theComparison>=0) 
        theLeftIndex=theMidIndex;
      else 
        theRightIndex=theMidIndex;
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndexAfter
//
// Returns index of first value greater than target
// Returns inFirstIndex+inNValues if target greater than or equal all values in array
// Used to find insertion point for a new value when you want value to go after all equal values
//--------------------------------------------------------------------------------------------------------

  public static long findIndexAfter(long inFirstIndex, long inNValues, Finder inFinder) {
    if (inNValues==0)
      return inFirstIndex+inNValues;

    // Check if target outside range
    long theTopIndex=inFirstIndex+inNValues-1;
    int theComparison=inFinder.compareToIndex(theTopIndex);
    if (theComparison>=0)
      return theTopIndex+1;
    else {
      // target less than top - possibly in range

      long theBottomIndex=inFirstIndex;
      theComparison=inFinder.compareToIndex(theBottomIndex);
      if (theComparison<0)
        return theBottomIndex;
      else {
        // target greater than or equal bottom - in range

        return narrowAfter(theBottomIndex,theTopIndex,inFinder);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndexAfter
//
// As above, but takes a start index, which is assumed to be near the target
//--------------------------------------------------------------------------------------------------------

  public static long findIndexAfter(long inStartIndex, long inFirstIndex, long inNValues, Finder inFinder) {
    if (inNValues==0)
      return inFirstIndex+inNValues;

    // Compare target with start
    int theComparison=inFinder.compareToIndex(inStartIndex);
    if (theComparison<0) {
      // target less than start - expand left
      long theBottomIndex=inFirstIndex;
      long theLeftIndex=Math.max(theBottomIndex,inStartIndex-1);
      long theRightIndex=inStartIndex;

      // Bracket target using binary expansion
      // Maintain bottom <= target < top
      while (true) {
        theComparison=inFinder.compareToIndex(theLeftIndex);
        if (theComparison>=0) 
          return narrowAfter(theLeftIndex,theRightIndex,inFinder);
        else if (theLeftIndex==theBottomIndex)
          return theBottomIndex;   // Target less than bottom index
        else {
          long theStep=theRightIndex-theLeftIndex;
          theRightIndex=theLeftIndex;
          theLeftIndex=Math.max(theBottomIndex,theLeftIndex-2*theStep);
        }
      }
      
    } else {
      // target greater than or equal start - expand right
      long theTopIndex=inFirstIndex+inNValues-1;
      long theRightIndex=Math.min(theTopIndex,inStartIndex+1);
      long theLeftIndex=inStartIndex;

      // Bracket target using binary expansion
      // Maintain bottom <= target < top
      while (true) {
        theComparison=inFinder.compareToIndex(theRightIndex);
        if (theComparison<0) 
          return narrowAfter(theLeftIndex,theRightIndex,inFinder);
        else if (theRightIndex==theTopIndex)
          return theTopIndex+1;   // Target greater than or equal top index
        else {
          long theStep=theRightIndex-theLeftIndex;
          theLeftIndex=theRightIndex;
          theRightIndex=Math.min(theTopIndex,theRightIndex+2*theStep);
        }
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndexAfter for ranges in arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexAfter(char inTarget, long inFirstIndex, long inNValues,
      char[] inSortedValues, byte inHandleCase) {
    return findIndexAfter(inFirstIndex,inNValues,new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexAfter(int inTarget, long inFirstIndex, long inNValues,
      int[] inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inTarget, long inFirstIndex, long inNValues,
      long[] inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(float inTarget, long inFirstIndex, long inNValues,
      float[] inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(double inTarget, long inFirstIndex, long inNValues,
      double[] inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(Comparable inTarget, long inFirstIndex, long inNValues,
      Comparable[] inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(String inTarget, long inFirstIndex, long inNValues,
      String[] inSortedValues, byte inHandleCase) {
    return findIndexAfter(inFirstIndex,inNValues,new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexAfter(Comparable inTarget, long inFirstIndex, long inNValues, 
      ArrayList inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(byte inTarget, long inFirstIndex, long inNValues,
      ByteStore inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inTarget, long inFirstIndex, long inNValues,
      VarStore inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(String inTarget, long inFirstIndex, long inNValues,
      ByteDataStore inSortedValues) {
    return findIndexAfter(inFirstIndex,inNValues,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexAfter for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexAfter(Finder inFinder) {
    return findIndexAfter(0,inFinder.getSize(),inFinder); }

  public static long findIndexAfter(char inTarget, char[] inSortedValues, byte inHandleCase) {
    return findIndexAfter(new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexAfter(int inTarget, int[] inSortedValues) {
    return findIndexAfter(new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inTarget, long[] inSortedValues) {
    return findIndexAfter(new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(float inTarget, float[] inSortedValues) {
    return findIndexAfter(new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(double inTarget, double[] inSortedValues) {
    return findIndexAfter(new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexAfter(new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(String inTarget, String[] inSortedValues, byte inHandleCase) {
    return findIndexAfter(new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexAfter(Comparable inTarget, ArrayList inSortedValues) {
    return findIndexAfter(new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(byte inTarget, ByteStore inSortedValues) {
    return findIndexAfter(new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inTarget, VarStore inSortedValues) {
    return findIndexAfter(new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(String inTarget, ByteDataStore inSortedValues) {
    return findIndexAfter(new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexAfter with start index for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexAfter(long inStartIndex, Finder inFinder) {
    return findIndexAfter(inStartIndex,0,inFinder.getSize(),inFinder); }

  public static long findIndexAfter(long inStartIndex, char inTarget, char[] inSortedValues, byte inHandleCase) {
    return findIndexAfter(inStartIndex,new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexAfter(long inStartIndex, int inTarget, int[] inSortedValues) {
    return findIndexAfter(inStartIndex,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, long inTarget, long[] inSortedValues) {
    return findIndexAfter(inStartIndex,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, float inTarget, float[] inSortedValues) {
    return findIndexAfter(inStartIndex,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, double inTarget, double[] inSortedValues) {
    return findIndexAfter(inStartIndex,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexAfter(inStartIndex,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, String inTarget, String[] inSortedValues, byte inHandleCase) {
    return findIndexAfter(inStartIndex,new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexAfter(long inStartindex, Comparable inTarget, ArrayList inSortedValues) {
    return findIndexAfter(inStartindex,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, byte inTarget, ByteStore inSortedValues) {
    return findIndexAfter(inStartIndex,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, long inTarget, VarStore inSortedValues) {
    return findIndexAfter(inStartIndex,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexAfter(long inStartIndex, String inTarget, ByteDataStore inSortedValues) {
    return findIndexAfter(inStartIndex,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrBefore for ranges in arrays or stores
//
// Returns index of last value less than or equal to target
// Will return largest index for range of values equal to target
// Returns kNotFound=-1 if target less than all values in array
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrBefore(long inFirstIndex, long inNValues, Finder inFinder) {
    return findIndexAfter(inFirstIndex,inNValues,inFinder)-1; }

  public static long findIndexEqualOrBefore(int inTarget, long inFirstIndex, long inNValues,
      int[] inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inTarget, long inFirstIndex, long inNValues,
      long[] inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(float inTarget, long inFirstIndex, long inNValues,
      float[] inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(double inTarget, long inFirstIndex, long inNValues,
      double[] inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(Comparable inTarget, long inFirstIndex, long inNValues,
      Comparable[] inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(Comparable inTarget, long inFirstIndex, long inNValues, 
      ArrayList inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(byte inTarget, long inFirstIndex, long inNValues,
      ByteStore inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inTarget, long inFirstIndex, long inNValues,
      VarStore inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(String inTarget, long inFirstIndex, long inNValues,
      ByteDataStore inSortedValues) {
    return findIndexEqualOrBefore(inFirstIndex,inNValues,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrBefore for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrBefore(Finder inFinder) {
    return findIndexEqualOrBefore(0,inFinder.getSize(),inFinder); }

  public static long findIndexEqualOrBefore(int inTarget, int[] inSortedValues) {
    return findIndexEqualOrBefore(new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inTarget, long[] inSortedValues) {
    return findIndexEqualOrBefore(new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(double inTarget, double[] inSortedValues) {
    return findIndexEqualOrBefore(new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(float inTarget, float[] inSortedValues) {
    return findIndexEqualOrBefore(new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexEqualOrBefore(new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(Comparable inTarget, ArrayList inSortedValues) {
    return findIndexEqualOrBefore(new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(byte inTarget, ByteStore inSortedValues) {
    return findIndexEqualOrBefore(new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inTarget, VarStore inSortedValues) {
    return findIndexEqualOrBefore(new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(String inTarget, ByteDataStore inSortedValues) {
    return findIndexEqualOrBefore(new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrBefore with start index for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrBefore(long inStartIndex, Finder inFinder) {
    return findIndexAfter(inStartIndex,0,inFinder.getSize(),inFinder)-1; }

  public static long findIndexEqualOrBefore(long inStartIndex, int inTarget, int[] inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, long inTarget, long[] inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, float inTarget, float[] inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, double inTarget, double[] inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartindex, Comparable inTarget, ArrayList inSortedValues) {
    return findIndexEqualOrBefore(inStartindex,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, byte inTarget, ByteStore inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, long inTarget, VarStore inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrBefore(long inStartIndex, String inTarget, ByteDataStore inSortedValues) {
    return findIndexEqualOrBefore(inStartIndex,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// narrowEqualOrAfter
//--------------------------------------------------------------------------------------------------------

  public static long narrowEqualOrAfter(long inLeftIndex, long inRightIndex, Finder inFinder) {

    long theLeftIndex=inLeftIndex;
    long theRightIndex=inRightIndex;

    // Find limit using binary search
    // Maintain bottom < target <= top
    while (true) {
      
      // Continue until find smallest index greater than or equal target:  index-1 < target <= index
      if (theRightIndex-1==theLeftIndex)
        return theRightIndex;

      long theMidIndex=(theLeftIndex+theRightIndex)/2;
      int theComparison=inFinder.compareToIndex(theMidIndex);
      if (theComparison>0) 
        theLeftIndex=theMidIndex;
      else 
        theRightIndex=theMidIndex;
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrAfter
//
// Returns index of first value greater than or equal to target
// Will return smallest index for range of values equal to target
// Returns inFirstIndex+inNValues if target greater than all values in array
// Used to find insertion point for a new value when you want value to go before all equal values
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrAfter(long inFirstIndex, long inNValues, Finder inFinder) {
    if (inNValues==0)
      return inFirstIndex+inNValues;

    // Check if target outside range
    long theTopIndex=inFirstIndex+inNValues-1;
    int theComparison=inFinder.compareToIndex(theTopIndex);
    if (theComparison>0)
      return theTopIndex+1;
    else {
      // target less or equal top - possibly in range

      long theBottomIndex=inFirstIndex;
      theComparison=inFinder.compareToIndex(theBottomIndex);
      if (theComparison<=0)
        return theBottomIndex;
      else {
        // target greater than bottom - in range

        return narrowEqualOrAfter(theBottomIndex,theTopIndex,inFinder);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrAfter
//
// As above, but takes a start index, which is assumed to be near the target
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrAfter(long inStartIndex, long inFirstIndex, long inNValues, Finder inFinder) {
    if (inNValues==0)
      return inFirstIndex+inNValues;

    // Compare target with start
    int theComparison=inFinder.compareToIndex(inStartIndex);
    if (theComparison<=0) {
      // target less than or equal start - expand left
      long theBottomIndex=inFirstIndex;
      long theLeftIndex=Math.max(theBottomIndex,inStartIndex-1);
      long theRightIndex=inStartIndex;

      // Bracket target using binary expansion
      // Maintain bottom < target <= top
      while (true) {
        theComparison=inFinder.compareToIndex(theLeftIndex);
        if (theComparison>0) 
          return narrowEqualOrAfter(theLeftIndex,theRightIndex,inFinder);
        else if (theLeftIndex==theBottomIndex)
          return theBottomIndex;   // Target less than or equal bottom index
        else {
          long theStep=theRightIndex-theLeftIndex;
          theRightIndex=theLeftIndex;
          theLeftIndex=Math.max(theBottomIndex,theLeftIndex-2*theStep);
        }
      }
      
    } else {
      // target greater than start - expand right
      long theTopIndex=inFirstIndex+inNValues-1;
      long theRightIndex=Math.min(theTopIndex,inStartIndex+1);
      long theLeftIndex=inStartIndex;

      // Bracket target using binary expansion
      // Maintain bottom < target <= top
      while (true) {
        theComparison=inFinder.compareToIndex(theRightIndex);
        if (theComparison<=0) 
          return narrowEqualOrAfter(theLeftIndex,theRightIndex,inFinder);
        else if (theRightIndex==theTopIndex)
          return theTopIndex+1;   // Target greater than top index
        else {
          long theStep=theRightIndex-theLeftIndex;
          theLeftIndex=theRightIndex;
          theRightIndex=Math.min(theTopIndex,theRightIndex+2*theStep);
        }
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrAfter for ranges in arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrAfter(char inTarget, long inFirstIndex, long inNValues,
      char[] inSortedValues, byte inHandleCase) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexEqualOrAfter(int inTarget, long inFirstIndex, long inNValues,
      int[] inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inTarget, long inFirstIndex, long inNValues,
      long[] inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(float inTarget, long inFirstIndex, long inNValues,
      float[] inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(double inTarget, long inFirstIndex, long inNValues,
      double[] inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(Comparable inTarget, long inFirstIndex, long inNValues,
      Comparable[] inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(String inTarget, long inFirstIndex, long inNValues,
      String[] inSortedValues, byte inHandleCase) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexEqualOrAfter(Comparable inTarget, long inFirstIndex, long inNValues, 
      ArrayList inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(byte inTarget, long inFirstIndex, long inNValues,
      ByteStore inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inTarget, long inFirstIndex, long inNValues,
      VarStore inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(String inTarget, long inFirstIndex, long inNValues,
      ByteDataStore inSortedValues) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrAfter for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrAfter(Finder inFinder) {
    return findIndexEqualOrAfter(0,inFinder.getSize(),inFinder); }

  public static long findIndexEqualOrAfter(char inTarget, char[] inSortedValues, byte inHandleCase) {
    return findIndexEqualOrAfter(new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexEqualOrAfter(int inTarget, int[] inSortedValues) {
    return findIndexEqualOrAfter(new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inTarget, long[] inSortedValues) {
    return findIndexEqualOrAfter(new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(float inTarget, float[] inSortedValues) {
    return findIndexEqualOrAfter(new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(double inTarget, double[] inSortedValues) {
    return findIndexEqualOrAfter(new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexEqualOrAfter(new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(String inTarget, String[] inSortedValues, byte inHandleCase) {
    return findIndexEqualOrAfter(new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexEqualOrAfter(Comparable inTarget, ArrayList inSortedValues) {
    return findIndexEqualOrAfter(new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(byte inTarget, ByteStore inSortedValues) {
    return findIndexEqualOrAfter(new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inTarget, VarStore inSortedValues) {
    return findIndexEqualOrAfter(new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(String inTarget, ByteDataStore inSortedValues) {
    return findIndexEqualOrAfter(new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexEqualOrAfter with start index for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexEqualOrAfter(long inStartIndex, Finder inFinder) {
    return findIndexEqualOrAfter(inStartIndex,0,inFinder.getSize(),inFinder); }

  public static long findIndexEqualOrAfter(long inStartIndex, char inTarget, char[] inSortedValues, byte inHandleCase) {
    return findIndexEqualOrAfter(inStartIndex,new CharArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexEqualOrAfter(long inStartIndex, int inTarget, int[] inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, long inTarget, long[] inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, float inTarget, float[] inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, double inTarget, double[] inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, String inTarget, String[] inSortedValues, byte inHandleCase) {
    return findIndexEqualOrAfter(inStartIndex,new StringArrayFinder(inTarget,inSortedValues,inHandleCase)); }

  public static long findIndexEqualOrAfter(long inStartindex, Comparable inTarget, ArrayList inSortedValues) {
    return findIndexEqualOrAfter(inStartindex,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, byte inTarget, ByteStore inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, long inTarget, VarStore inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexEqualOrAfter(long inStartIndex, String inTarget, ByteDataStore inSortedValues) {
    return findIndexEqualOrAfter(inStartIndex,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexBefore for ranges in arrays or stores
//
// Returns index of last value less than target
// Returns kNotFound=-1 if target less than all values in array
//--------------------------------------------------------------------------------------------------------

  public static long findIndexBefore(long inFirstIndex, long inNValues, Finder inFinder) {
    return findIndexEqualOrAfter(inFirstIndex,inNValues,inFinder)-1; }

  public static long findIndexBefore(int inTarget, long inFirstIndex, long inNValues,
      int[] inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inTarget, long inFirstIndex, long inNValues,
      long[] inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(float inTarget, long inFirstIndex, long inNValues,
      float[] inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(double inTarget, long inFirstIndex, long inNValues,
      double[] inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(Comparable inTarget, long inFirstIndex, long inNValues,
      Comparable[] inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(Comparable inTarget, long inFirstIndex, long inNValues, 
      ArrayList inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(byte inTarget, long inFirstIndex, long inNValues,
      ByteStore inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inTarget, long inFirstIndex, long inNValues,
      VarStore inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(String inTarget, long inFirstIndex, long inNValues,
      ByteDataStore inSortedValues) {
    return findIndexBefore(inFirstIndex,inNValues,new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexBefore for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexBefore(Finder inFinder) {
    return findIndexBefore(0,inFinder.getSize(),inFinder); }

  public static long findIndexBefore(int inTarget, int[] inSortedValues) {
    return findIndexBefore(new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inTarget, long[] inSortedValues) {
    return findIndexBefore(new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(float inTarget, float[] inSortedValues) {
    return findIndexBefore(new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(double inTarget, double[] inSortedValues) {
    return findIndexBefore(new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexBefore(new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(Comparable inTarget, ArrayList inSortedValues) {
    return findIndexBefore(new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(byte inTarget, ByteStore inSortedValues) {
    return findIndexBefore(new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inTarget, VarStore inSortedValues) {
    return findIndexBefore(new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(String inTarget, ByteDataStore inSortedValues) {
    return findIndexBefore(new UTF8StoreFinder(inTarget,inSortedValues)); }

//--------------------------------------------------------------------------------------------------------
// findIndexBefore with start index for entire arrays or stores
//--------------------------------------------------------------------------------------------------------

  public static long findIndexBefore(long inStartIndex, Finder inFinder) {
    return findIndexEqualOrAfter(inStartIndex,0,inFinder.getSize(),inFinder)-1; }

  public static long findIndexBefore(long inStartIndex, int inTarget, int[] inSortedValues) {
    return findIndexBefore(inStartIndex,new IntArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, long inTarget, long[] inSortedValues) {
    return findIndexBefore(inStartIndex,new LongArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, float inTarget, float[] inSortedValues) {
    return findIndexBefore(inStartIndex,new FloatArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, double inTarget, double[] inSortedValues) {
    return findIndexBefore(inStartIndex,new DoubleArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, Comparable inTarget, Comparable[] inSortedValues) {
    return findIndexBefore(inStartIndex,new ComparableArrayFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartindex, Comparable inTarget, ArrayList inSortedValues) {
    return findIndexBefore(inStartindex,new ComparableArrayListFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, byte inTarget, ByteStore inSortedValues) {
    return findIndexBefore(inStartIndex,new ByteStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, long inTarget, VarStore inSortedValues) {
    return findIndexBefore(inStartIndex,new VarStoreFinder(inTarget,inSortedValues)); }

  public static long findIndexBefore(long inStartIndex, String inTarget, ByteDataStore inSortedValues) {
    return findIndexBefore(inStartIndex,new UTF8StoreFinder(inTarget,inSortedValues)); }

}






