//--------------------------------------------------------------------------------------------------------
// SortUtils
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// SortUtils
//
// Sorts in place
//
// Only works on fixed width items, which includes arrays of references such as String[]
// Note that DataStores (e.g. UTF8s in ByteDataStore) have variable length values, and cannot be
//   sorted in place.  Use a SortMap instead
//--------------------------------------------------------------------------------------------------------

public class SortUtils extends Comparators {
 
//--------------------------------------------------------------------------------------------------------
// sort
//--------------------------------------------------------------------------------------------------------

  public static void sort(long inFirstIndex, long inNValues, Comparator inComparator) {
    // Can replace with some other sort routine, but I like quicksort
    QuickSort.quickSort(inFirstIndex,inNValues,inComparator); }

//--------------------------------------------------------------------------------------------------------
// sort range
//
// In place sort - reorders range in array
//--------------------------------------------------------------------------------------------------------

  public static void sort(byte[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new ByteArrayComparator(inValues,inDescending)); }

  public static void sort(char[] inValues, long inFirstIndex, long inNValues, byte inHandleCase, boolean inDescending) {
    sort(inFirstIndex,inNValues,new CharArrayComparator(inValues,inHandleCase,inDescending)); }

  public static void sort(int[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new IntArrayComparator(inValues,inDescending)); }

  public static void sort(float[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new FloatArrayComparator(inValues,inDescending)); }

  public static void sort(long[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new LongArrayComparator(inValues,inDescending)); }

  public static void sort(double[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new DoubleArrayComparator(inValues,inDescending)); }

  public static void sort(Comparable[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new ComparableArrayComparator(inValues,inDescending)); }

  public static void sort(String[] inValues, long inFirstIndex, long inNValues, byte inHandleCase, boolean inDescending) {
    sort(inFirstIndex,inNValues,new StringArrayComparator(inValues,inHandleCase,inDescending)); }

  
  
  public static void sort(ByteStore inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new ByteStoreComparator(inValues,inDescending)); }

  public static void sort(VarStore inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    sort(inFirstIndex,inNValues,new VarStoreComparator(inValues,inDescending)); }

  

  public static void sort(byte[] inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }
  
  public static void sort(char[] inValues, long inFirstIndex, long inNValues, byte inHandleCase) { 
    sort(inValues,inFirstIndex,inNValues,inHandleCase,false); }
  
  public static void sort(int[] inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }
  
  public static void sort(float[] inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }
  
  public static void sort(long[] inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }
  
  public static void sort(double[] inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }
  
  public static void sort(Comparable[] inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }
  
  public static void sort(String[] inValues, long inFirstIndex, long inNValues, byte inHandleCase) { 
    sort(inValues,inFirstIndex,inNValues,inHandleCase,false); }
  
  

  public static void sort(ByteStore inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }

  public static void sort(VarStore inValues, long inFirstIndex, long inNValues) { 
    sort(inValues,inFirstIndex,inNValues,false); }

//--------------------------------------------------------------------------------------------------------
// sort
//
// In place sort - reorders array
//--------------------------------------------------------------------------------------------------------

  public static void sort(Comparator inComparator) { 
    sort(0,inComparator.getLength(),inComparator); }

  public static void sort(byte[] inValues, boolean inDescending) { 
    sort(new ByteArrayComparator(inValues,inDescending)); }
  
  public static void sort(char[] inValues, byte inHandleCase, boolean inDescending) { 
    sort(new CharArrayComparator(inValues,inHandleCase,inDescending)); }
  
  public static void sort(int[] inValues, boolean inDescending) { 
    sort(new IntArrayComparator(inValues,inDescending)); }
  
  public static void sort(float[] inValues, boolean inDescending) { 
    sort(new FloatArrayComparator(inValues,inDescending)); }
  
  public static void sort(long[] inValues, boolean inDescending) { 
    sort(new LongArrayComparator(inValues,inDescending)); }
  
  public static void sort(double[] inValues, boolean inDescending) { 
    sort(new DoubleArrayComparator(inValues,inDescending)); }
  
  public static void sort(Comparable[] inValues, boolean inDescending) { 
    sort(new ComparableArrayComparator(inValues,inDescending)); }
  
  public static void sort(String[] inValues, byte inHandleCase, boolean inDescending) {
    sort(new StringArrayComparator(inValues,inHandleCase,inDescending)); }
  
  
  
  public static void sort(ByteStore inValues, boolean inDescending) { 
    sort(new ByteStoreComparator(inValues,inDescending)); }

  public static void sort(VarStore inValues, boolean inDescending) { 
    sort(new VarStoreComparator(inValues,inDescending)); }

  
  
  public static void sort(byte[] inValues) { sort(inValues,false); }
  public static void sort(char[] inValues, byte inHandleCase) { sort(inValues,inHandleCase,false); }
  public static void sort(int[] inValues) {  sort(inValues,false); }
  public static void sort(float[] inValues) { sort(inValues,false); }
  public static void sort(long[] inValues) { sort(inValues,false); }
  public static void sort(double[] inValues) { sort(inValues,false); }
  public static void sort(Comparable[] inValues) { sort(inValues,false); }
  public static void sort(String[] inValues, byte inHandleCase) { sort(inValues,inHandleCase,false); }
  
  
  
  public static void sort(ByteStore inValues) { sort(inValues,false); }
  public static void sort(VarStore inValues) { sort(inValues,false); }

//--------------------------------------------------------------------------------------------------------
// isSorted
//--------------------------------------------------------------------------------------------------------

  public static boolean isSorted(long inFirstIndex, long inNValues, Comparator inComparator) {
    long theEndIndex=inFirstIndex+inNValues;
    for (long i=inFirstIndex+1; i<theEndIndex; i++)
      if (inComparator.sortCompare(i-1,i)>0)
        return false;
    return true;
  }

  public static boolean isSorted(byte[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new ByteArrayComparator(inValues,inDescending)); }

  public static boolean isSorted(char[] inValues, long inFirstIndex, long inNValues, byte inHandleCase, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new CharArrayComparator(inValues,inHandleCase,inDescending)); }

  public static boolean isSorted(int[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new IntArrayComparator(inValues,inDescending)); }

  public static boolean isSorted(float[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new FloatArrayComparator(inValues,inDescending)); }

  public static boolean isSorted(long[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new LongArrayComparator(inValues,inDescending)); }

  public static boolean isSorted(double[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new DoubleArrayComparator(inValues,inDescending)); }

  public static boolean isSorted(Comparable[] inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new ComparableArrayComparator(inValues,inDescending)); }

  public static boolean isSorted(String[] inValues, long inFirstIndex, long inNValues, byte inHandleCase, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new StringArrayComparator(inValues,inHandleCase,inDescending)); }

  
  
  public static boolean isSorted(ByteStore inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new ByteStoreComparator(inValues,inDescending)); }

  public static boolean isSorted(ByteDataStore inValues, long inFirstIndex, long inNValues, byte inHandleCase, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new ByteDataStoreComparator(inValues,inHandleCase,inDescending)); }

  public static boolean isSorted(VarStore inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new VarStoreComparator(inValues,inDescending)); }

  public static boolean isSorted(VarDataStore inValues, long inFirstIndex, long inNValues, boolean inDescending) {
    return isSorted(inFirstIndex,inNValues,new VarDataStoreComparator(inValues,inDescending)); }


  
  public static boolean isSorted(byte[] inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(char[] inValues, long inFirstIndex, long inNValues, byte inHandleCase) { 
    return isSorted(inValues,inFirstIndex,inNValues,inHandleCase,false); }
  
  public static boolean isSorted(int[] inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(float[] inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(long[] inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(double[] inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(Comparable[] inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(String[] inValues, long inFirstIndex, long inNValues, byte inHandleCase) { 
    return isSorted(inValues,inFirstIndex,inNValues,inHandleCase,false); }
  
  
  
  public static boolean isSorted(ByteStore inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(ByteDataStore inValues, long inFirstIndex, long inNValues, byte inHandleCase) { 
    return isSorted(inValues,inFirstIndex,inNValues,inHandleCase,false); }

  public static boolean isSorted(VarStore inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }
  
  public static boolean isSorted(VarDataStore inValues, long inFirstIndex, long inNValues) { 
    return isSorted(inValues,inFirstIndex,inNValues,false); }

//--------------------------------------------------------------------------------------------------------
// isSorted
//--------------------------------------------------------------------------------------------------------

  public static boolean isSorted(Comparator inComparator) { 
    return isSorted(0,inComparator.getLength(),inComparator); }

  public static boolean isSorted(byte[] inValues, boolean inDescending) { 
    return isSorted(new ByteArrayComparator(inValues,inDescending)); }
  
  public static boolean isSorted(char[] inValues, byte inHandleCase, boolean inDescending) { 
    return isSorted(new CharArrayComparator(inValues,inHandleCase,inDescending)); }
  
  public static boolean isSorted(int[] inValues, boolean inDescending) { 
    return isSorted(new IntArrayComparator(inValues,inDescending)); }
  
  public static boolean isSorted(float[] inValues, boolean inDescending) { 
    return isSorted(new FloatArrayComparator(inValues,inDescending)); }
  
  public static boolean isSorted(long[] inValues, boolean inDescending) { 
    return isSorted(new LongArrayComparator(inValues,inDescending)); }
  
  public static boolean isSorted(double[] inValues, boolean inDescending) { 
    return isSorted(new DoubleArrayComparator(inValues,inDescending)); }
  
  public static boolean isSorted(Comparable[] inValues, boolean inDescending) { 
    return isSorted(new ComparableArrayComparator(inValues,inDescending)); }
  
  public static boolean isSorted(String[] inValues, byte inHandleCase, boolean inDescending) {
    return isSorted(new StringArrayComparator(inValues,inHandleCase,inDescending)); }
  
  
  
  public static boolean isSorted(ByteStore inValues, boolean inDescending) { 
    return isSorted(new ByteStoreComparator(inValues,inDescending)); }
  
  public static boolean isSorted(ByteDataStore inValues, byte inHandleCase, boolean inDescending) { 
    return isSorted(new ByteDataStoreComparator(inValues,inHandleCase,inDescending)); }

  public static boolean isSorted(VarStore inValues, boolean inDescending) { 
    return isSorted(new VarStoreComparator(inValues,inDescending)); }

  public static boolean isSorted(VarDataStore inValues, boolean inDescending) { 
    return isSorted(new VarDataStoreComparator(inValues,inDescending)); }

  
  
  public static boolean isSorted(byte[] inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(char[] inValues, byte inHandleCase) { 
    return isSorted(inValues,inHandleCase,false); }
  
  public static boolean isSorted(int[] inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(float[] inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(long[] inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(double[] inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(Comparable[] inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(String[] inValues, byte inHandleCase) {
    return isSorted(inValues,inHandleCase,false); }
  
  
  
  public static boolean isSorted(ByteStore inValues) { 
    return isSorted(inValues,false); }
  
  public static boolean isSorted(ByteDataStore inValues, byte inHandleCase) { 
    return isSorted(inValues,inHandleCase,false); }

  public static boolean isSorted(VarStore inValues) { 
    return isSorted(inValues,false); }

  public static boolean isSorted(VarDataStore inValues) { 
    return isSorted(inValues,false); }

//--------------------------------------------------------------------------------------------------------
// reverseOrder
//--------------------------------------------------------------------------------------------------------

  public static void reverseOrder(Comparator inComparator) {
    long theLength=inComparator.getLength();
    long theLengthLess1=theLength-1;
    long theLengthOvr2=theLength/2;
    for (long i=0; i<theLengthOvr2; i++)
      inComparator.swapIndexes(i,theLengthLess1-i);
  }

  public static void reverseOrder(byte[] inValues) { 
    reverseOrder(new ByteArrayComparator(inValues)); }
  
  public static void reverseOrder(char[] inValues) { 
    reverseOrder(new CharArrayComparator(inValues,Comparisons.kBinary)); }
  
  public static void reverseOrder(int[] inValues) { 
    reverseOrder(new IntArrayComparator(inValues)); }
  
  public static void reverseOrder(float[] inValues) { 
    reverseOrder(new FloatArrayComparator(inValues)); }
  
  public static void reverseOrder(long[] inValues) { 
    reverseOrder(new LongArrayComparator(inValues)); }
  
  public static void reverseOrder(double[] inValues) { 
    reverseOrder(new DoubleArrayComparator(inValues)); }
  
  public static void reverseOrder(Comparable[] inValues) { 
    reverseOrder(new ComparableArrayComparator(inValues)); }
  
  public static void reverseOrder(String[] inValues) { 
    reverseOrder(new StringArrayComparator(inValues,Comparisons.kBinary)); }
  
  
  
  public static void reverseOrder(ByteStore inValues) { 
    reverseOrder(new ByteStoreComparator(inValues)); }

  public static void reverseOrder(VarStore inValues) { 
    reverseOrder(new VarStoreComparator(inValues)); }

//--------------------------------------------------------------------------------------------------------
// sortAndCrop
//--------------------------------------------------------------------------------------------------------

  public static void sortAndCrop(long inMinIndex, long inMaxIndex, Comparator inComparator) {
    QuickSort.quickSort(0,inComparator.getLength(),Math.max(0,inMinIndex),
        Math.min(inMaxIndex,inComparator.getLength()-1),inComparator); }

}






