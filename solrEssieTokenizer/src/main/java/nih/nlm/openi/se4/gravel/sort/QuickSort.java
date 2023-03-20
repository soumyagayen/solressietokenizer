//--------------------------------------------------------------------------------------------------------
// QuickSort
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// QuickSort
//
// Multithreaded in-place sort
//--------------------------------------------------------------------------------------------------------

public class QuickSort extends Comparators {

//--------------------------------------------------------------------------------------------------------
// quickSort
//
// Reorders entire array
//--------------------------------------------------------------------------------------------------------

  public static void quickSort(Comparator inComparator) {
    quickSort(0,inComparator.getLength(),inComparator); }

//--------------------------------------------------------------------------------------------------------
// quickSort
//
// Reorders array values between indexes inFirstIndex and inFirstIndex+inNValues-1
// Useful when data does not fill a big buffer array
//--------------------------------------------------------------------------------------------------------

  public static void quickSort(
      final long   inFirstIndex, 
      final long   inNValues, 
      Comparator   inComparator) {
    quickSort(inFirstIndex,inNValues,inFirstIndex,inFirstIndex+inNValues,inComparator); }

//--------------------------------------------------------------------------------------------------------
// quickSort
//
// Compares and changes order of values between indexes inFirstIndex and inFirstIndex+inNValues-1, but
// only completely sorts values from inMinIndex to inMaxIndex.  Other values are partially sorted, which
// allows better than N Log N sort times when only interested in top M of a large number of items.
//
// Example: find top 50 of 1,000,000 values - set inMinIndex=0 and inMaxIndex=49
// Example: find next 50 of 1,000,000 values - set inMinIndex=50 and inMaxIndex=99
// 
//--------------------------------------------------------------------------------------------------------

  public static void quickSort(
      final long   inFirstIndex, 
      final long   inNValues, 
      final long   inMinIndex, 
      final long   inMaxIndex, 
      Comparator   inComparator) {
    quickSort(inFirstIndex,inNValues,inFirstIndex,inFirstIndex+inNValues,inComparator,0); }

//--------------------------------------------------------------------------------------------------------
// quickSort
//
// Recursive routine - track depth so can alternate strategies
//--------------------------------------------------------------------------------------------------------

  private static void quickSort(
      final long      inFirstIndex, 
      final long      inNValues, 
      final long      inMinIndex, 
      final long      inMaxIndex, 
      Comparator      inComparator, 
      final int       inDepth) {             
    
    // Some comparators have buffers.  
    // To make them thread safe, need new comparator when running in new thread
    final Comparator theComparator=inComparator.makeThreadCopy();    
    
    if (inDepth>100) 
      throw new RuntimeException("Too much recursion in quickSort");
    if (inNValues<0)
      throw new RuntimeException("Bad NValues");
    if (inMaxIndex<inMinIndex)
      throw new RuntimeException("Bad limits");

    // Don't need to sort values that are known to be outside range inMinIndex to inMaxIndex
    final long inLastIndex=inFirstIndex+inNValues-1;
    if ((inFirstIndex>inMaxIndex)||(inLastIndex<inMinIndex))
      return;
    
    if (inNValues<=1)
      return;

    if (inNValues==2) {
      if (theComparator.sortCompare(inFirstIndex,inFirstIndex+1)>0)
        theComparator.swapIndexes(inFirstIndex,inFirstIndex+1);
      return;
    }

    // For small numbers of values, use bubble sort
    if (inNValues<8) {
      for (long j=inLastIndex; j>inFirstIndex; j--)
        for (long i=inFirstIndex; i<j; i++)
          if (theComparator.sortCompare(i,i+1)>0)
            theComparator.swapIndexes(i,i+1);
      return;
    }
    
    // Pick a pivot value from the middle of the range
    long theQuarter=(inNValues>>>2);
    long thePivotIndex=inFirstIndex+theQuarter+RandomUtils.randomIndex(theQuarter<<1);
    
    // Can avoid some pathological cases by alternating comparison of >= pivot with <= pivot
    boolean theEven=((inDepth&0x0001)==0);
    if (theEven) {

      // Don't know what kind of value it is so can't hold it in a local var
      // Move pivot to the top (i.e. to inLastIndex) to keep it out of the way
      theComparator.swapIndexes(thePivotIndex,inLastIndex);

      // Divide values into two sets 1) those less than the pivot and 2) those greater
      // than or equal to pivot.  Swap elements that don't meet this criteria.  The two sets start at
      // the top and bottom of the range of values and grow inward.  Stop division when they touch.
      long theFirstIndex=inFirstIndex;
      long theLastIndex=inLastIndex;  // Starts at a value >= the pivot value
      while (theFirstIndex<theLastIndex) {

        // Search forward from theFirstIndex until theFirstIndex == theLastIndex
        // or find value >= the pivot value
        while ((theFirstIndex<theLastIndex)&&
            (theComparator.sortCompare(theFirstIndex,inLastIndex)<0))
          theFirstIndex++;
        
        // Two cases:
        //   1) Advanced until theFirstIndex == theLastIndex
        //   2) Advanced until found value >= the pivot value and stopped
        // In both cases, theFirstIndex value >= the pivot value and all values below theFirstIndex
        // are < the pivot value

        // Search backward from theLastIndex until theFirstIndex == theLastIndex
        // or next value < the pivot value
        // Note we are checking preceding index before moving into it
        // Value at theLastIndex is always >= the pivot value
        while ((theFirstIndex<theLastIndex)&&
            (theComparator.sortCompare(theLastIndex-1,inLastIndex)>=0))
          theLastIndex--;

        // Three cases.  
        //   1) theFirstIndex advanced until theFirstIndex == theLastIndex
        // or theFirstIndex advanced until value >= the pivot value and stopped and
        //   2) theLastIndex descended until theFirstIndex == theLastIndex
        //   3) or theLastIndex descended until next value < the pivot value and stopped

        // For cases 1 & 2, we're done without need to swap and the indexs value >= the pivot value 
        // and all values below the indexs < the pivot value
        
        // For case 3, swap elements at theFirstIndex and theLastIndex-1
        if (theFirstIndex<theLastIndex) {
          theComparator.swapIndexes(theFirstIndex,theLastIndex-1);
          theFirstIndex++;  // Still sure all values below theFirstIndex < the pivot value
          theLastIndex--;   // Still sure all values at and above theLastIndex >= the pivot value
        }      
      }
       
      // Pivot smallest value of upper range
      // Put pivot value in the new "center" and sort upper and lower lists
      thePivotIndex=theLastIndex;
      theComparator.swapIndexes(thePivotIndex,inLastIndex);

    // Odd case, similar to even, but replaces comparison of >= pivot with <= pivot
    } else {

      long theTempIndex=inFirstIndex;  
      theComparator.swapIndexes(thePivotIndex,theTempIndex);
 
      long theLastIndex=inLastIndex; 
      long theFirstIndex=theTempIndex; // Starts at a value <= the pivot value
   
      while (theFirstIndex<theLastIndex) {
  
        while ((theFirstIndex<theLastIndex)&&
            (theComparator.sortCompare(theLastIndex,theTempIndex)>0))
          theLastIndex--;
  
        while ((theFirstIndex<theLastIndex)&&
            (theComparator.sortCompare(theFirstIndex+1,theTempIndex)<=0))
          theFirstIndex++;
        
        if (theFirstIndex<theLastIndex) {
          theComparator.swapIndexes(theFirstIndex+1,theLastIndex);
          theLastIndex--;   // Still sure values above theLastIndex > the pivot value
          theFirstIndex++;  // Still sure all values at and below theFirstIndex <= the pivot value
        }    
      }
      
      // Pivot biggest value of lower range
      // Put pivot value in the new "center" and sort upper and lower lists
      thePivotIndex=theFirstIndex;
      theComparator.swapIndexes(thePivotIndex,theTempIndex);
    }

    // When data is mostly a single value, there can be a problem with deep recursion 
    // To avoid, don't sort the range of values around pivot that are equal to the pivot
    // These tests will quick fail with normally distributed data, so they don't cost much
    
    // Raise bottom of top range so top range does not include values equal to pivot value
    long theBottomIndex=thePivotIndex+1; 
    while ((theBottomIndex<=inLastIndex)&&
        (theComparator.sortCompare(theBottomIndex,thePivotIndex)==0))
      theBottomIndex++;
    
    // Lower top of bottom range so bottom range does not include values equal to pivot value
    long theTopIndex=thePivotIndex-1;
    while ((theTopIndex>=inFirstIndex)&&
        (theComparator.sortCompare(theTopIndex,thePivotIndex)==0))
      theTopIndex--;

    long theNLowerValues=theTopIndex+1-inFirstIndex;
    if (theNLowerValues>1)
      quickSort(inFirstIndex,theNLowerValues,inMinIndex,inMaxIndex,theComparator,inDepth+1);

    long theNUpperValues=inLastIndex+1-theBottomIndex;
    if (theNUpperValues>1)
      quickSort(theBottomIndex,theNUpperValues,inMinIndex,inMaxIndex,theComparator,inDepth+1);        

    // If comparator was a thread copy, close it to recover buffers
    if (theComparator!=inComparator)
      theComparator.close();
  }

}






