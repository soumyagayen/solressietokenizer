//--------------------------------------------------------------------------------------------------------
// SortMapUtils
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// SortMapUtils
//--------------------------------------------------------------------------------------------------------

public class SortMapUtils extends Comparators {
  
//--------------------------------------------------------------------------------------------------------
// SortMapUtils inner classes
//--------------------------------------------------------------------------------------------------------

  //------------------------------------------------------------------------------------------------------
  // Inner class SortMapArrayComparator
  //------------------------------------------------------------------------------------------------------

  public static class SortMapArrayComparator extends Comparator {

    private int[]             mSortMap;
    private ArrayComparator   mComparator;

    public SortMapArrayComparator(ArrayComparator inComparator, int[] ioSortMap) {
      super(inComparator.getDescending());
      if (ioSortMap.length!=inComparator.getLength())
        throw new RuntimeException("Incompatible sortmap length");
      mComparator=inComparator;
      mSortMap=ioSortMap;
    }

    public SortMapArrayComparator(ArrayComparator inComparator) {
      super(inComparator.getDescending());
      mComparator=inComparator;
      long theLength=inComparator.getLength();
      mSortMap=Allocate.newInts(theLength);
      for (int i=0; i<theLength; i++)
        mSortMap[i]=i;
    }
    
    public Comparator makeThreadCopy() { 
      ArrayComparator theThreadCopy=mComparator.makeThreadCopy();
      if (theThreadCopy!=mComparator) 
        return new SortMapArrayComparator(theThreadCopy,mSortMap);
      return this;
    }
    public void close() { mComparator.close(); }

    public long getLength() { return mComparator.getLength(); }
    public int[] getSortMap() { return mSortMap; }
    public Comparator getComparator() { return mComparator; }
    public long getInnerIndex(long inIndex) { return mSortMap[(int) inIndex]; }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return mComparator.compareIndexes(mSortMap[(int) inIndex1],mSortMap[(int) inIndex2]); }

    public void swapIndexes(long inIndex1, long inIndex2) {
      int theTemp=mSortMap[(int) inIndex1];
      mSortMap[(int) inIndex1]=mSortMap[(int) inIndex2];
      mSortMap[(int) inIndex2]=theTemp;
    }
  }

  //------------------------------------------------------------------------------------------------------
  // Inner class SortMapStoreComparator
  //------------------------------------------------------------------------------------------------------

  public static class SortMapStoreComparator extends Comparator {

    private StoreComparator  mComparator;
    private VarRAMStore      mSortMap;    

    private SortMapStoreComparator(StoreComparator inComparator, VarRAMStore inSortMap) {
      super(inComparator.getDescending());
      mComparator=inComparator;
      mSortMap=inSortMap;
    }

    public SortMapStoreComparator(StoreComparator inComparator) {
      super(inComparator.getDescending());
      mComparator=inComparator;
      final long theLength=inComparator.getLength();
      mSortMap=new VarRAMStore(theLength);
      mSortMap.setSize(theLength);
      for (long i=0; i<theLength; i++)
        mSortMap.setVar(i,i);
    }
   
    public Comparator makeThreadCopy() { 
      StoreComparator theThreadCopy=mComparator.makeThreadCopy();
      if (theThreadCopy!=mComparator) 
        return new SortMapStoreComparator(theThreadCopy,mSortMap);
      return this;
    }
    public void close() { mComparator.close(); }

    public long getLength() { return mComparator.getLength(); }
    public VarRAMStore getSortMap() { return mSortMap; }
    public Comparator getComparator() { return mComparator; }
    public long getInnerIndex(long inIndex) { return getSortMap().getLong(inIndex); }

    public long compareIndexes(long inIndex1, long inIndex2) {
      return mComparator.compareIndexes(mSortMap.getLong(inIndex1),mSortMap.getLong(inIndex2)); }

    public void swapIndexes(long inIndex1, long inIndex2) {
      long theTemp=mSortMap.getLong(inIndex1);
      mSortMap.setVar(inIndex1,mSortMap.getLong(inIndex2));
      mSortMap.setVar(inIndex2,theTemp);
    }
  }

//--------------------------------------------------------------------------------------------------------
// sortMap
//
// External sort - array is not reordered
//
// Returns an array, theSortMap, of indexes such that inValues[theSortMap[i]] is sorted.
// theSortMap[i] is the original object index for sort order index i
//--------------------------------------------------------------------------------------------------------

  public static void sortMap(ArrayComparator inComparator, int[] ioSortMap) {
    for (int i=0; i<ioSortMap.length; i++)
      ioSortMap[i]=i;
    SortMapArrayComparator theSortMapComparator=new SortMapArrayComparator(inComparator,ioSortMap);
    SortUtils.sort(theSortMapComparator);
  }

  public static int[] sortMap(ArrayComparator inComparator) {
    SortMapArrayComparator theSortMapComparator=new SortMapArrayComparator(inComparator);
    SortUtils.sort(theSortMapComparator);
    return theSortMapComparator.getSortMap();
  }

  public static VarRAMStore sortMap(StoreComparator inComparator) {
    SortMapStoreComparator theSortMapComparator=new SortMapStoreComparator(inComparator);
    SortUtils.sort(theSortMapComparator);
    return theSortMapComparator.getSortMap();
  }

  public static int[] sortMap(byte[] inValues, boolean inDescending) { 
    return sortMap(new ByteArrayComparator(inValues,inDescending)); }
  
  public static int[] sortMap(char[] inValues, byte inHandleCase, boolean inDescending) { 
    return sortMap(new CharArrayComparator(inValues,inHandleCase,inDescending)); }
  
  public static int[] sortMap(int[] inValues, boolean inDescending) { 
    return sortMap(new IntArrayComparator(inValues,inDescending)); }
  
  public static int[] sortMap(float[] inValues, boolean inDescending) { 
    return sortMap(new FloatArrayComparator(inValues,inDescending)); }
  
  public static int[] sortMap(long[] inValues, boolean inDescending) { 
    return sortMap(new LongArrayComparator(inValues,inDescending)); }
  
  public static int[] sortMap(double[] inValues, boolean inDescending) { 
    return sortMap(new DoubleArrayComparator(inValues,inDescending)); }
  
  public static int[] sortMap(Comparable[] inValues, boolean inDescending) { 
    return sortMap(new ComparableArrayComparator(inValues,inDescending)); }
  
  public static int[] sortMap(String[] inValues, byte inHandleCase, boolean inDescending) {
    return sortMap(new StringArrayComparator(inValues,inHandleCase,inDescending)); }

  
  
  public static VarRAMStore sortMap(ByteStore inValues, boolean inDescending) { 
    return sortMap(new ByteStoreComparator(inValues,inDescending)); }
  
  public static VarRAMStore sortMap(ByteDataStore inValues, byte inHandleCase, boolean inDescending) { 
    return sortMap(new ByteDataStoreComparator(inValues,inHandleCase,inDescending)); }

  public static VarRAMStore sortMap(VarStore inValues, boolean inDescending) {
    return sortMap(new VarStoreComparator(inValues,inDescending)); }
  
  public static VarRAMStore sortMap(VarDataStore inValues, boolean inDescending) { 
    return sortMap(new VarDataStoreComparator(inValues,inDescending)); }
 
  
  
  public static int[] sortMap(byte[] inValues) { return sortMap(inValues,false); }
  public static int[] sortMap(char[] inValues, byte inHandleCase) { return sortMap(inValues,inHandleCase,false); }
  public static int[] sortMap(int[] inValues) { return sortMap(inValues,false); }
  public static int[] sortMap(float[] inValues) { return sortMap(inValues,false); }
  public static int[] sortMap(long[] inValues) { return sortMap(inValues,false); }
  public static int[] sortMap(double[] inValues) { return sortMap(inValues,false); }
  public static int[] sortMap(Comparable[] inValues) { return sortMap(inValues,false); }
  public static int[] sortMap(String[] inValues, byte inHandleCase) { return sortMap(inValues,inHandleCase,false); }

  
  
  public static VarRAMStore sortMap(ByteStore inValues) { return sortMap(inValues,false); }
  public static VarRAMStore sortMap(ByteDataStore inValues, byte inHandleCase) { return sortMap(inValues,inHandleCase,false); }
  public static VarRAMStore sortMap(VarStore inValues) { return sortMap(inValues,false); }
  public static VarRAMStore sortMap(VarDataStore inValues) { return sortMap(inValues,false); }

//--------------------------------------------------------------------------------------------------------
// sortOrder
//
// Returns an array, theSortOrder, which is the inverse sort map
// theSortOrder[i] is the sort order index for original object index i
//--------------------------------------------------------------------------------------------------------

  public static int[] sortOrder(int[] inSortMap) {
    int theLength=inSortMap.length;
    int[] theSortOrder=Allocate.newInts(theLength);
    for (int i=0; i<theLength; i++)
      theSortOrder[inSortMap[i]]=i;
    return theSortOrder;
  }

  public static VarRAMStore sortOrder(VarRAMStore inSortMap) {
    long theLength=inSortMap.getSize();
    VarRAMStore theSortOrder=new VarRAMStore(theLength);
    theSortOrder.setSize(theLength);
    for (long i=0; i<theLength; i++)
      theSortOrder.setVar(inSortMap.getLong(i),i);
    return theSortOrder;
  }

//--------------------------------------------------------------------------------------------------------
// rank
//
// External ranking - array is not reordered
//
// Returns an array, theRanks, which is the ranks of the original values
// theRanks[i] is the sort rank for original value index i
// If the values are unique, the ranks are the same as the sort orders
// The number of unique sort values is theRanks[inSortMap[theNValues-1]]+1
//--------------------------------------------------------------------------------------------------------

  public static int[] rank(int[] inSortMap, ArrayComparator inComparator) {
    long theLength=inComparator.getLength();
    int[] theRanks=Allocate.newInts(theLength);
    if (theLength>1) {
      int theRank=0;
      theRanks[inSortMap[0]]=theRank;
      for (int i=1; i<theLength; i++) {
        if (inComparator.sortCompare(inSortMap[i],inSortMap[i-1])>0)
          theRank++;
        theRanks[inSortMap[i]]=theRank;
      }
    }
    return theRanks;
  }
  
  public static VarRAMStore rank(VarRAMStore inSortMap, StoreComparator inComparator) {
    long theLength=inComparator.getLength();
    VarRAMStore theRanks=new VarRAMStore(theLength);
    theRanks.setSize(theLength);
    if (theLength>1) {
      long theRank=0;
      theRanks.setVar(inSortMap.getLong(0),theRank);
      for (long i=1; i<theLength; i++) {
        if (inComparator.sortCompare(inSortMap.getLong(i),inSortMap.getLong(i-1))>0)
          theRank++;
        theRanks.setVar(inSortMap.getLong(i),theRank);
      }
    }
    return theRanks;
  }

  
  
  public static int[] rank(int[] inSortMap, byte[] inValues, boolean inDescending) {
    return rank(inSortMap,new ByteArrayComparator(inValues,inDescending)); }

  public static int[] rank(int[] inSortMap, char[] inValues, byte inHandleCase, boolean inDescending) {
    return rank(inSortMap,new CharArrayComparator(inValues,inHandleCase,inDescending)); }

  public static int[] rank(int[] inSortMap, int[] inValues, boolean inDescending) {
    return rank(inSortMap,new IntArrayComparator(inValues,inDescending)); }

  public static int[] rank(int[] inSortMap, float[] inValues, boolean inDescending) {
    return rank(inSortMap,new FloatArrayComparator(inValues,inDescending)); }

  public static int[] rank(int[] inSortMap, long[] inValues, boolean inDescending) {
    return rank(inSortMap,new LongArrayComparator(inValues,inDescending)); }

  public static int[] rank(int[] inSortMap, double[] inValues, boolean inDescending) {
    return rank(inSortMap,new DoubleArrayComparator(inValues,inDescending)); }

  public static int[] rank(int[] inSortMap, Comparable[] inValues, boolean inDescending) {
    return rank(inSortMap,new ComparableArrayComparator(inValues,inDescending)); }

  public static int[] rank(int[] inSortMap, String[] inValues, byte inHandleCase, boolean inDescending) {
    return rank(inSortMap,new StringArrayComparator(inValues,inHandleCase,inDescending)); }

  

  public static VarRAMStore rank(VarRAMStore inSortMap, ByteStore inValues, boolean inDescending) {
    return rank(inSortMap,new ByteStoreComparator(inValues,inDescending)); }

  public static VarRAMStore rank(VarRAMStore inSortMap, ByteDataStore inValues, byte inHandleCase, boolean inDescending) {
    return rank(inSortMap,new ByteDataStoreComparator(inValues,inHandleCase,inDescending)); }

  public static VarRAMStore rank(VarRAMStore inSortMap, VarStore inValues, boolean inDescending) {
    return rank(inSortMap,new VarStoreComparator(inValues,inDescending)); }

  public static VarRAMStore rank(VarRAMStore inSortMap, VarDataStore inValues, boolean inDescending) {
    return rank(inSortMap,new VarDataStoreComparator(inValues,inDescending)); }

  
  
  public static int[] rank(int[] inSortMap, byte[] inValues) { 
    return rank(inSortMap,inValues,false); }
  
  public static int[] rank(int[] inSortMap, char[] inValues, byte inHandleCase) { 
    return rank(inSortMap,inValues,inHandleCase,false); }
  
  public static int[] rank(int[] inSortMap, int[] inValues) {
    return rank(inSortMap,inValues,false); }
  
  public static int[] rank(int[] inSortMap, float[] inValues) {
    return rank(inSortMap,inValues,false); }
  
  public static int[] rank(int[] inSortMap, long[] inValues) { 
    return rank(inSortMap,inValues,false); }
  
  public static int[] rank(int[] inSortMap, double[] inValues) { 
    return rank(inSortMap,inValues,false); }
  
  public static int[] rank(int[] inSortMap, Comparable[] inValues) { 
    return rank(inSortMap,inValues,false); }
  
  public static int[] rank(int[] inSortMap, String[] inValues, byte inHandleCase) { 
    return rank(inSortMap,inValues,inHandleCase,false); }

  
  
  public static VarRAMStore rank(VarRAMStore inSortMap, ByteStore inValues) { 
    return rank(inSortMap,inValues,false); }
  
  public static VarRAMStore rank(VarRAMStore inSortMap, ByteDataStore inValues, byte inHandleCase) { 
    return rank(inSortMap,inValues,inHandleCase,false); }

  public static VarRAMStore rank(VarRAMStore inSortMap, VarStore inValues) {
    return rank(inSortMap,inValues,false); }

  public static VarRAMStore rank(VarRAMStore inSortMap, VarDataStore inValues) {
    return rank(inSortMap,inValues,false); }

//--------------------------------------------------------------------------------------------------------
// rank
//
// External ranking - array is not reordered
//
// Returns an array, theRanks, which is the ranks of the original values
// theRanks[i] is the sort rank for original value index i
// If the values are unique, the ranks are the same as the sort orders
// and the number of unique sort values is theRanks[inSortMap[theNValues-1]]+1
//--------------------------------------------------------------------------------------------------------

  public static int[] rank(ArrayComparator inComparator) { 
    return rank(sortMap(inComparator),inComparator); }
  
  public static VarRAMStore rank(StoreComparator inComparator) { 
    return rank(sortMap(inComparator),inComparator); }
  
  public static int[] rank(byte[] inValues, boolean inDescending) { 
    return rank(new ByteArrayComparator(inValues,inDescending)); }
  
  public static int[] rank(char[] inValues, byte inHandleCase, boolean inDescending) { 
    return rank(new CharArrayComparator(inValues,inHandleCase,inDescending)); }
  
  public static int[] rank(int[] inValues, boolean inDescending) { 
    return rank(new IntArrayComparator(inValues,inDescending)); }
  
  public static int[] rank(float[] inValues, boolean inDescending) { 
    return rank(new FloatArrayComparator(inValues,inDescending)); }
  
  public static int[] rank(long[] inValues, boolean inDescending) { 
    return rank(new LongArrayComparator(inValues,inDescending)); }
  
  public static int[] rank(double[] inValues, boolean inDescending) { 
    return rank(new DoubleArrayComparator(inValues,inDescending)); }
  
  public static int[] rank(Comparable[] inValues, boolean inDescending) { 
    return rank(new ComparableArrayComparator(inValues,inDescending)); }
  
  public static int[] rank(String[] inValues, byte inHandleCase, boolean inDescending) { 
    return rank(new StringArrayComparator(inValues,inHandleCase,inDescending)); }

  
  
  public static VarRAMStore rank(ByteStore inValues, boolean inDescending) {
    return rank(new ByteStoreComparator(inValues,inDescending)); }
  
  public static VarRAMStore rank(ByteDataStore inValues, byte inHandleCase, boolean inDescending) { 
    return rank(new ByteDataStoreComparator(inValues,inHandleCase,inDescending)); }

  public static VarRAMStore rank(VarStore inValues, boolean inDescending) { 
    return rank(new VarStoreComparator(inValues,inDescending)); }

  public static VarRAMStore rank(VarDataStore inValues, boolean inDescending) { 
    return rank(new VarDataStoreComparator(inValues,inDescending)); }
 
  
  
  public static int[] rank(byte[] inValues) {
    return rank(inValues,false); }
  
  public static int[] rank(char[] inValues, byte inHandleCase) { 
    return rank(inValues,inHandleCase,false); }
  
  public static int[] rank(int[] inValues) { 
    return rank(inValues,false); }
  
  public static int[] rank(float[] inValues) { 
    return rank(inValues,false); }
  
  public static int[] rank(long[] inValues) { 
    return rank(inValues,false); }
  
  public static int[] rank(double[] inValues) { 
    return rank(inValues,false); }
  
  public static int[] rank(Comparable[] inValues) {
    return rank(inValues,false); }
  
  public static int[] rank(String[] inValues, byte inHandleCase) { 
    return rank(inValues,inHandleCase,false); }
  
  
  
  public static VarRAMStore rank(ByteStore inValues) { 
    return rank(inValues,false); }
  
  public static VarRAMStore rank(ByteDataStore inValues, byte inHandleCase) { 
    return rank(inValues,inHandleCase,false); }

  public static VarRAMStore rank(VarStore inValues) { 
    return rank(inValues,false); }

  public static VarRAMStore rank(VarDataStore inValues) { 
    return rank(inValues,false); }

//--------------------------------------------------------------------------------------------------------
// reorder
//
// Applies a sort map to another array of Values
// In place reordering
//--------------------------------------------------------------------------------------------------------

  public static void reorder(boolean[] inValues, int[] inSortMap) {
    boolean[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(byte[] inValues, int[] inSortMap) {
    byte[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(char[] inValues, int[] inSortMap) {
    char[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(int[] inValues, int[] inSortMap) {
    int[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(float[] inValues, int[] inSortMap) {
    float[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(long[] inValues, int[] inSortMap) {
    long[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(double[] inValues, int[] inSortMap) {
    double[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(Object[] inValues, int[] inSortMap) {
    Object[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(Comparable[] inValues, int[] inSortMap) {
    Comparable[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  public static void reorder(String[] inValues, int[] inSortMap) {
    String[] theTempValues=inValues.clone();
    for (int i=0; i<inSortMap.length; i++)
      inValues[i]=theTempValues[inSortMap[i]];
  }

  
  
  public static void reorder(ByteStore inValues, VarRAMStore inSortMap) {
    ByteRAMStore theTempValues=inValues.getByteRAMStore();
    for (long i=0; i<inSortMap.getSize(); i++)
      inValues.setByte(i,theTempValues.getByte(inSortMap.getLong(i)));
    theTempValues.close();
  }
  
  public static void reorder(VarStore inValues, VarRAMStore inSortMap) {
    VarRAMStore theTempValues=inValues.getVarRAMStore();
    for (long i=0; i<inSortMap.getSize(); i++)
      inValues.setVar(i,theTempValues.getLong(inSortMap.getLong(i)));
    theTempValues.close();
  }

//--------------------------------------------------------------------------------------------------------
// sortMapAndCrop
//--------------------------------------------------------------------------------------------------------

  public static int[] sortMapAndCrop(long inMinIndex, long inMaxIndex, ArrayComparator inComparator) {
    SortMapArrayComparator theSortMapComparator=new SortMapArrayComparator(inComparator);
    SortUtils.sortAndCrop(inMinIndex,inMaxIndex,theSortMapComparator);
    return theSortMapComparator.getSortMap();
  }

  public static int[] sortMapAndCrop(long inMinIndex, long inMaxIndex, 
      int[] inValues, boolean inDescending) { 
    return sortMapAndCrop(inMinIndex,inMaxIndex,new IntArrayComparator(inValues,inDescending)); }

  public static int[] sortMapAndCrop(long inMinIndex, long inMaxIndex, 
      float[] inValues, boolean inDescending) { 
    return sortMapAndCrop(inMinIndex,inMaxIndex,new FloatArrayComparator(inValues,inDescending)); }

  public static int[] sortMapAndCrop(long inMinIndex, long inMaxIndex, 
      long[] inValues, boolean inDescending) { 
    return sortMapAndCrop(inMinIndex,inMaxIndex,new LongArrayComparator(inValues,inDescending)); }

  public static int[] sortMapAndCrop(long inMinIndex, long inMaxIndex, 
      double[] inValues, boolean inDescending) { 
    return sortMapAndCrop(inMinIndex,inMaxIndex,new DoubleArrayComparator(inValues,inDescending)); }

  
  
  public static VarRAMStore sortMapAndCrop(long inMinIndex, long inMaxIndex, StoreComparator inComparator) {
    SortMapStoreComparator theSortMapComparator=new SortMapStoreComparator(inComparator);
    SortUtils.sortAndCrop(inMinIndex,inMaxIndex,theSortMapComparator);
    return theSortMapComparator.getSortMap();
  }

  public static VarRAMStore sortMapAndCrop(long inMinIndex, long inMaxIndex, 
      ByteStore inValues, boolean inDescending) { 
    return sortMapAndCrop(inMinIndex,inMaxIndex,new ByteStoreComparator(inValues,inDescending)); }

  public static VarRAMStore sortMapAndCrop(long inMinIndex, long inMaxIndex, 
      VarStore inValues, boolean inDescending) { 
    return sortMapAndCrop(inMinIndex,inMaxIndex,new VarStoreComparator(inValues,inDescending)); }

}






