//--------------------------------------------------------------------------------------------------------
// MapUtils
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import gravel.store.chain.*;
import gravel.store.data.*;
import gravel.store.hash.*;
import gravel.store.var.*;

//--------------------------------------------------------------------------------------------------------
// MapUtils
//--------------------------------------------------------------------------------------------------------

public class MapUtils implements Constants { 

//--------------------------------------------------------------------------------------------------------
// MapUtils consts
//--------------------------------------------------------------------------------------------------------
 
  public static final int          kRedundantLimit=64;
 
//--------------------------------------------------------------------------------------------------------
// createEmptyMap
//
// Fills 1D map with kNotFound
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore createEmptyMap(long inSize) {
    VarRAMStore theMap=new VarRAMStore(inSize);
    theMap.appendVars(kNotFound,inSize);
    return theMap;
  }

//--------------------------------------------------------------------------------------------------------
// createSequentia1To1Map
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore createSequentia1To1Map(long inSize) {
    VarRAMStore theMap=new VarRAMStore(inSize);
    for (long i=0; i<inSize; i++) 
      theMap.appendVar(i);
    return theMap;
  }

//--------------------------------------------------------------------------------------------------------
// createEmptyChains
//--------------------------------------------------------------------------------------------------------

  public static VarChainRAMStore createEmptyChains(long inSrcNChains, long inDstNValues) {
    int theVarSize=Conversions.calcVarLongSize(inDstNValues);
    VarChainRAMStore theChains=new VarChainRAMStore(theVarSize,inSrcNChains,inDstNValues);
    theChains.setSize(inSrcNChains);
    return theChains;
  }

  public static VarChainRAMStore createEmptyChains() { return createEmptyChains(16,64); }

//--------------------------------------------------------------------------------------------------------
// checkMapForHoles
//--------------------------------------------------------------------------------------------------------

  public static void checkMapForHoles(VarStore inSrcToDstMap) {
    long theSrcSize=inSrcToDstMap.getSize();
    for (long i=0; i<theSrcSize; i++) {
      long theDst=inSrcToDstMap.getLong(i); //  <-------------- Needs chunk up 
      if (theDst<=kNotFound)
        throw new RuntimeException("Map has hole: elmt "+i+" of "+theSrcSize+" has value "+theDst);
    }
  }

  public static void checkMapForHoles(VarDataStore inSrcToDstMap) {
    long theSrcSize=inSrcToDstMap.getSize();
    for (long i=0; i<theSrcSize; i++) {
      long theNDsts=inSrcToDstMap.getNVars(i);
      if (theNDsts==0)
        throw new RuntimeException("Map has hole: row "+i+" of "+theSrcSize+" is empty");
      for (int j=0; j<theNDsts; j++) {
        long theDst=inSrcToDstMap.getLongAtN(i,j);
        if (theDst<=kNotFound)
          throw new RuntimeException("Map has hole: row "+i+" of "+theSrcSize+" has value "+theDst);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// sanityCheck
//
// Sanity check for 1:N and N:M maps
// Checks if kNotFound or dup Dsts exist for each Src
//--------------------------------------------------------------------------------------------------------

  public static void sanityCheck(VarDataStore inSrcToDstMap) {
    long theSrcSize=inSrcToDstMap.getSize();
    long[] theDsts=new long[kRedundantLimit];
    VarHashRAMStore theUniquer=new VarHashRAMStore();  
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=(int) inSrcToDstMap.getNVars(i);
      if (theNDsts<=kRedundantLimit) {
        inSrcToDstMap.getVars(i,theDsts); //  <-------------- Needs chunk up 
        for (int j=0; j<theNDsts; j++) {
          long theDst=theDsts[j];
          if (theDst<=kNotFound) 
            throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+" has value "+theDst);
          for (int k=j+1; k<theNDsts; k++) 
            if (theDsts[k]==theDst) 
              throw new RuntimeException("Map has redundancy: row "+i+" of "+theSrcSize+
                  " has value "+theDst+" multiple times");
        }
      } else {
        theUniquer.clear();
        for (int j=0; j<theNDsts; j++) {
          long theDst=inSrcToDstMap.getLongAtN(i,j); //  <-------------- Needs chunk up 
          if (theDst<=kNotFound) 
            throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+" has value "+theDst);
          if (theUniquer.appendVar(theDst)!=kNotFound) 
            throw new RuntimeException("Map has redundancy: row "+i+" of "+theSrcSize+
                " has value "+theDst+" multiple times");
        }
      }
    }
    theUniquer.close();
  }

//--------------------------------------------------------------------------------------------------------
// removeRedundancy
//
// For each Src, Dsts should be uniq.   1 --> {3,3,4} is redundant 
//--------------------------------------------------------------------------------------------------------

  private static int removeRedundancy(long[] inDsts, int inNDsts, VarHashStore inUniquer) {
    int theNDsts=inNDsts;
    // For smallish NDsts, do a linear search,  O(N^2) but quick
    if (theNDsts<=kRedundantLimit) {
      for (int j=0; j<theNDsts; j++) {
        long theDst=inDsts[j];
        // Remove any kNotFounds
        if (theDst==kNotFound) {
          theNDsts--;
          inDsts[j]=inDsts[theNDsts];
        // Remove and dups
        } else 
          for (int k=j+1; k<theNDsts; k++) {
            if (inDsts[k]==theDst) { // Found dup
              theNDsts--;
              inDsts[k]=inDsts[theNDsts];
              k--;
            }
          }
      }
    // For larger NDsts, create a hash lookup,  O(N) but slow
    } else {
      inUniquer.clear();
      for (int j=0; j<theNDsts; j++) {
        long theDst=inDsts[j];
        if (theDst!=kNotFound)
          inUniquer.appendVar(theDst);
      }
      theNDsts=inUniquer.getAllVars(inDsts,0);
    }
    return theNDsts; 
  }

//--------------------------------------------------------------------------------------------------------
// equalMaps
//--------------------------------------------------------------------------------------------------------

  public static boolean equalMaps(VarStore inSrcToDstMap1, VarStore inSrcToDstMap2) {
    if (inSrcToDstMap1.getSize()!=inSrcToDstMap2.getSize())
      return false;
    long theSize=inSrcToDstMap1.getSize();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts1=theSliceStore.getLongSlice();
    long[] theDsts2=theSliceStore.getLongSlice();
    long theNFullSices=theSize/theDsts1.length;
    long n=0;
    for (long i=0; i<theNFullSices; i++) {
      inSrcToDstMap1.getVars(n,theDsts1,0,theDsts1.length);
      inSrcToDstMap2.getVars(n,theDsts2,0,theDsts2.length);
      for (int j=0; j<theDsts1.length; j++) {
        if (theDsts1[j]!=theDsts2[j]) {
          theSliceStore.putLongSlice(theDsts1);
          theSliceStore.putLongSlice(theDsts2);
          return false;
        }
        n++;
      }
    }
    int theRemainder=(int) theSize%theDsts1.length;
    inSrcToDstMap1.getVars(n,theDsts1,0,theRemainder);
    inSrcToDstMap2.getVars(n,theDsts2,0,theRemainder);
    for (int j=0; j<theRemainder; j++) {
      if (theDsts1[j]!=theDsts2[j]) {
        theSliceStore.putLongSlice(theDsts1);
        theSliceStore.putLongSlice(theDsts2);
        return false;
      }
      n++;
    }
    theSliceStore.putLongSlice(theDsts1);
    theSliceStore.putLongSlice(theDsts2);
    return true;
  }

  public static boolean equalMaps(VarDataStore inSrcToDstMap1, VarDataStore inSrcToDstMap2) {
    if (inSrcToDstMap1.getSize()!=inSrcToDstMap2.getSize())
      return false;
    for (long i=0; i<inSrcToDstMap1.getSize(); i++)
      if (inSrcToDstMap1.getNVars(i)!=inSrcToDstMap2.getNVars(i))
        return false;
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts1=theSliceStore.getLongSlice();
    long[] theDsts2=theSliceStore.getLongSlice();
    for (long i=0; i<inSrcToDstMap1.getSize(); i++) {
      int theNDsts=(int) inSrcToDstMap1.getNVars(i);
      if (theNDsts>theDsts1.length) {
        theSliceStore.putLongSlice(theDsts1);
        theSliceStore.putLongSlice(theDsts2);
        theDsts1=new long[theNDsts*2];
        theDsts2=new long[theNDsts*2];
      }
      inSrcToDstMap1.getVars(i,theDsts1,0);
      inSrcToDstMap2.getVars(i,theDsts2,0);
      for (int j=0; j<theNDsts; j++) {
        boolean theFound=false;
        for (int k=j; k<theNDsts; k++) 
          if (theDsts1[j]==theDsts2[k]) {
            long theDst=theDsts2[j];
            theDsts2[j]=theDsts2[k];
            theDsts2[k]=theDst;
            theFound=true;
            break;
          }
        if (!theFound) {
          theSliceStore.putLongSlice(theDsts1);
          theSliceStore.putLongSlice(theDsts2);
          return false;
        }
      }
    }
    theSliceStore.putLongSlice(theDsts1);
    theSliceStore.putLongSlice(theDsts2);
    return true;
  }

//--------------------------------------------------------------------------------------------------------
// inflate1To1Map
//--------------------------------------------------------------------------------------------------------

  // KLUDGE alert!!! 
  // Wasting RAM - Not recommended 
  // Only useful if returning specific 1D result in more general 2D format
  public static VarDataRAMStore inflate1To1Map(VarStore inSrcToDstMap) {
    long theSrcSize=inSrcToDstMap.getSize();
    int theVarSize=Conversions.calcVarLongSize(theSrcSize);
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,theSrcSize);
    long[] theDsts=new long[1];
    for (long i=0; i<theSrcSize; i++) {
      theDsts[0]=inSrcToDstMap.getLong(i);
      if (theDsts[0]==kNotFound)
        theSrcToDstMap.appendVars(theDsts,0,0);
      else
        theSrcToDstMap.appendVars(theDsts,0,1);
    }
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

//--------------------------------------------------------------------------------------------------------
// invert1To1Map
//
// Original 1D map, dst[src]:
//   dst[0] = 1           (when src=0, dst=1)
//   dst[1] = 2           (when src=1, dst=2)
//   dst[2] = kNotFound   (when src=2, dst=kNotFound)
// Inverts to 1D map, src[dst]:
//   src[0] = kNotFound   (when dst=0, src=kNotFound)
//   src[1] = 0           (when dst=1, src=0)
//   src[2] = 1           (when dst=2, src=1)
//
// A single src points to a single dst and vice versa.
// Multiple src may not point to the same dst and vice versa.
// src[] and dst[] may be different lengths, depending on number of holes (missing values).
//
// If NoHoles:
//   all src point to a dst and vice versa.
//   src[] and dst[] are the same size.
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore invert1To1Map(VarStore inSrcToDstMap, long inDstSize, boolean inNoHoles) {
    if (inNoHoles)
      checkMapForHoles(inSrcToDstMap);
    long theSrcSize=inSrcToDstMap.getSize();
    VarRAMStore theDstToSrcMap=createEmptyMap(inDstSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    long theNFullSlices=theSrcSize/theDsts.length;
    long theSrc=0;
    for (long i=0; i<theNFullSlices; i++) {
      inSrcToDstMap.getVars(theSrc,theDsts,0,theDsts.length);
      for (int j=0; j<theDsts.length; j++) {
        long theDst=theDsts[j];
        if ((theDst<kNotFound)||(theDst>=inDstSize))
          throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+" has value "+theDst);
        else if (theDst!=kNotFound) {
          if (theDstToSrcMap.getLong(theDst)!=kNotFound)
            throw new RuntimeException("Map not 1:1, both row "+theSrc+
                " & row "+theDstToSrcMap.getLong(theDst)+
                " of "+theSrcSize+" map to "+theDst+" of "+inDstSize);
          else
            theDstToSrcMap.setVar(theDst,theSrc);
        }
        theSrc++;
      }
    }
    int theRemainder=(int) theSrcSize%theDsts.length;
    inSrcToDstMap.getVars(theSrc,theDsts,0,theRemainder);
    for (int j=0; j<theRemainder; j++) {
      long theDst=theDsts[j];
      if ((theDst<kNotFound)||(theDst>=inDstSize))
        throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+" has value "+theDst);
      else if (theDst!=kNotFound) {
        if (theDstToSrcMap.getLong(theDst)!=kNotFound)
          throw new RuntimeException("Map not 1:1, both row "+theSrc+
              " & row "+theDstToSrcMap.getLong(theDst)+
              " of "+theSrcSize+" map to "+theDst+" of "+inDstSize);
        else
          theDstToSrcMap.setVar(theDst,theSrc);
      }
      theSrc++;
    }
    theSliceStore.putLongSlice(theDsts);
    if (inNoHoles)
      checkMapForHoles(theDstToSrcMap);
    theDstToSrcMap.compact();
    return theDstToSrcMap;
  }

//--------------------------------------------------------------------------------------------------------
// invertNTo1Map
//
// Original 1D map, dst[src]:
//   dst[0] = 1           (when src=0, dst=1)
//   dst[1] = 1           (when src=1, dst=1)
// Inverts to 2D map, src[dst][]:
//   src[0][] = {}        (when dst=0, src={})
//   src[1][] = {0,1}     (when dst=1, src={0,1})
//
// A single src points to a single dst.
// N different srcs may point to the same dst.
// A single dst may point to N different srcs (No Redundancy!).
// Multiple dsts may not point to the same src.
// src[][] and dst[] may be different lengths, depending on number of holes (missing values).
//
// If NoHoles:
//   all src point to one dst.
//   all dst point to one or more src.
//   dst[] cannot be shorter than src[][].
//--------------------------------------------------------------------------------------------------------

  public static VarDataRAMStore invertNTo1Map(VarStore inSrcToDstMap, long inDstSize, boolean inNoHoles) {
    if (inNoHoles)
      checkMapForHoles(inSrcToDstMap);
    long theSrcSize=inSrcToDstMap.getSize();
    long theSrcDataSize=theSrcSize; // 1 wide
    VarChainRAMStore theDstToSrcChains=createEmptyChains(inDstSize,theSrcDataSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    long theNFullSlices=theSrcSize/theDsts.length;
    long theSrc=0;
    for (long i=0; i<theNFullSlices; i++) {
      inSrcToDstMap.getVars(theSrc,theDsts,0,theDsts.length);
      for (int j=0; j<theDsts.length; j++) {
        long theDst=theDsts[j];
        if ((theDst<kNotFound)||(theDst>=inDstSize))
          throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+
              " has value "+theDst+" of "+inDstSize);
        else if (theDst!=kNotFound)
          theDstToSrcChains.appendVar(theDst,theSrc);
        theSrc++;
      }
    }    
    int theRemainder=(int) theSrcSize%theDsts.length;
    inSrcToDstMap.getVars(theSrc,theDsts,0,theRemainder);
    for (int j=0; j<theRemainder; j++) {
      long theDst=theDsts[j];
      if ((theDst<kNotFound)||(theDst>=inDstSize))
        throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+
            " has value "+theDst+" of "+inDstSize);
      else if (theDst!=kNotFound)
        theDstToSrcChains.appendVar(theDst,theSrc);
      theSrc++;
    }
    theSliceStore.putLongSlice(theDsts);
    VarDataRAMStore theDstToSrcMap=theDstToSrcChains.getVarDataRAMStore();
    theDstToSrcChains.close();
    if (inNoHoles)
      checkMapForHoles(theDstToSrcMap);
    theDstToSrcMap.compact();
    return theDstToSrcMap;
  }

//--------------------------------------------------------------------------------------------------------
// invertMonotonicNTo1Map
//   Special case of N to 1 map where both src and dst are monotonicly increasing and all srcs for
//     same dst are adjacent.  Occrs often in child/parent maps
//--------------------------------------------------------------------------------------------------------

  public static VarDataRAMStore invertMonotonicNTo1Map(
      VarStore inSrcToDstMap, long inDstSize, boolean inNoHoles) {
    if (inNoHoles)
      checkMapForHoles(inSrcToDstMap);
    long theSrcSize=inSrcToDstMap.getSize();
    int theVarSize=Conversions.calcVarLongSize(theSrcSize);
    VarDataRAMStore theDstToSrcMap=new VarDataRAMStore(theVarSize,inDstSize,inSrcToDstMap.getSize());
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    long[] theSrcs=theSliceStore.getLongSlice();
    long theNFullSlices=theSrcSize/theDsts.length;
    long theSrc=0;
    int theNSrcs=0;
    long theLastDst=kNotFound; 
    for (long i=0; i<theNFullSlices; i++) {
      inSrcToDstMap.getVars(theSrc,theDsts,0,theDsts.length);
      for (int j=0; j<theDsts.length; j++) {
        long theDst=theDsts[j];
        if ((theDst<kNotFound)||(theDst>=inDstSize))
          throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+
              " has value "+theDst+" of "+inDstSize);
        // Ignore when src maps to nothing
        if (theDst!=kNotFound) {         
          // If src mapping to same dst, continue to accum, else write out accumd srcs and restart accum
          if (theDst!=theLastDst) {       
            // Nothing to write for first Dst
            if (theLastDst!=kNotFound) {  
              if (theDst<theLastDst)
                throw new RuntimeException("Map not monotonic");
              // If some dsts were skipped, write out empty src lists for them
              for (long k=theDstToSrcMap.getSize(); k<theLastDst; k++)
                theDstToSrcMap.appendVars(kNoLongs);
              // Write out accumd srcs
              theDstToSrcMap.appendVars(theSrcs,0,theNSrcs);
              theNSrcs=0;
            }
            // Restart accum
            theLastDst=theDst;
          }
          // Continue to accum
          if (theNSrcs>=theSrcs.length) {
            long[] theOldSrcs=theSrcs;
            theSrcs=new long[theNSrcs*2];
            System.arraycopy(theOldSrcs,0,theSrcs,0,theNSrcs);
            theSliceStore.putLongSlice(theOldSrcs);
          }
          theSrcs[theNSrcs]=theSrc;
          theNSrcs++;
        }
        // Next src
        theSrc++;
      }
    }    
    int theRemainder=(int) theSrcSize%theDsts.length;
    inSrcToDstMap.getVars(theSrc,theDsts,0,theRemainder);
    for (int j=0; j<theRemainder; j++) {
      long theDst=theDsts[j];
      if ((theDst<kNotFound)||(theDst>=inDstSize))
        throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+
            " has value "+theDst+" of "+inDstSize);
      if (theDst!=kNotFound) {
        if (theDst!=theLastDst) {
          if (theLastDst!=kNotFound) {
            if (theDst<theLastDst)
              throw new RuntimeException("Map not monotonic");
            for (long k=theDstToSrcMap.getSize(); k<theLastDst; k++)
              theDstToSrcMap.appendVars(kNoLongs);
            theDstToSrcMap.appendVars(theSrcs,0,theNSrcs);
            theNSrcs=0;
          }
          theLastDst=theDst;
        }
        if (theNSrcs>=theSrcs.length) {
          long[] theOldSrcs=theSrcs;
          theSrcs=new long[theNSrcs*2];
          System.arraycopy(theOldSrcs,0,theSrcs,0,theNSrcs);
          theSliceStore.putLongSlice(theOldSrcs);
        }
        theSrcs[theNSrcs]=theSrc;
        theNSrcs++;
      }
      theSrc++;
    }
    // If finished map in middle of accum
    if (theNSrcs>0) {
      // Write out last srcs accumd
      if (theLastDst!=kNotFound) {
        for (long k=theDstToSrcMap.getSize(); k<theLastDst; k++)
          theDstToSrcMap.appendVars(kNoLongs);
        theDstToSrcMap.appendVars(theSrcs,0,theNSrcs);
        theNSrcs=0;
      }
    }
    // If final dsts not included in map, write out empty src lists for them
    for (long k=theDstToSrcMap.getSize(); k<inDstSize; k++)
      theDstToSrcMap.appendVars(kNoLongs);

    theSliceStore.putLongSlice(theDsts);
    theSliceStore.putLongSlice(theSrcs);
    if (inNoHoles)
      checkMapForHoles(theDstToSrcMap);
    theDstToSrcMap.compact();
    return theDstToSrcMap;
  }

//--------------------------------------------------------------------------------------------------------
// findStartsInMonotonicNTo1Map
//
// Special case of N to 1 map where both src and dst are monotonicly increasing and all srcs for
//   same dst are adjacent.  Occrs often in child/parent maps
//
// See invertNTo1Map then read...
// Rather than return an array of srcs for each dst, we return the start src.
// Since monotonic, all srcs are adjacent, so src array is all srcs between two starts
// An extra start has been added to end of result so all inDstSize ranges have a start and end, so
//   Starts result has size, inDstSize+1
//
// If there are holes, some srcs may not map to any dst and invertMonotonicNTo1Map will give cleaner result
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore findStartsInMonotonicNTo1Map(VarStore inSrcToDstMap, long inDstSize, boolean inNoHoles) {
    if (inNoHoles)
      checkMapForHoles(inSrcToDstMap);
    long theSrcSize=inSrcToDstMap.getSize();
    int theVarSize=Conversions.calcVarLongSize(theSrcSize);
    VarRAMStore theDstStartsStore=new VarRAMStore(theVarSize,inDstSize+1);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    long theNFullSlices=theSrcSize/theDsts.length;
    long theSrc=0;
    long theLastDst=kNotFound;
    for (long i=0; i<theNFullSlices; i++) {
      inSrcToDstMap.getVars(theSrc,theDsts,0,theDsts.length);
      for (int j=0; j<theDsts.length; j++) {
        long theDst=theDsts[j];
        if ((theDst<kNotFound)||(theDst>=inDstSize))
          throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+
              " has value "+theDst+" of "+inDstSize);
        else if (theDst!=kNotFound)
          if (theDst!=theLastDst) {
            theDstStartsStore.appendVars(theSrc,theDst-theLastDst);
            theLastDst=theDst;
          }
        theSrc++;
      }
    }    
    int theRemainder=(int) theSrcSize%theDsts.length;
    inSrcToDstMap.getVars(theSrc,theDsts,0,theRemainder);
    for (int j=0; j<theRemainder; j++) {
      long theDst=theDsts[j];
      if ((theDst<kNotFound)||(theDst>=inDstSize))
        throw new RuntimeException("Map corrupt: row "+theSrc+" of "+theSrcSize+
            " has value "+theDst+" of "+inDstSize);
      else if (theDst!=kNotFound)
        if (theDst!=theLastDst) {
          theDstStartsStore.appendVars(theSrc,theDst-theLastDst);
          theLastDst=theDst;
        }
      theSrc++;
    }
    theDstStartsStore.appendVars(theSrcSize,inDstSize-theLastDst);
    theSliceStore.putLongSlice(theDsts);
    if (inNoHoles)
      checkMapForHoles(theDstStartsStore);
    theDstStartsStore.compact();
    return theDstStartsStore;
  }

//--------------------------------------------------------------------------------------------------------
// invertNToMMap
//
// Original 2D map, dst[src][]:
//   dst[0][] = {0,1}      (when src=0, dst={0,1})
//   dst[1][] = {1}        (when src=1, dst={1})
// Inverts to 2D map, src[dst][]:
//   src[0][] = {0}        (when dst=0, src={0})
//   src[1][] = {0,1}      (when dst=1, src={0,1})
//
// A single src may points to N different dsts (No Redundancy!).
// N different srcs may point to the same dsts.
// A single dst may points to N different srcs (No Redundancy!).
// N different dsts may point to the same srcs.
// src[][] and dst[][] may be different lengths, depending on number of holes (missing values).
//
// If NoHoles:
//   all src point to one or more dst and vice versa.
//--------------------------------------------------------------------------------------------------------

  public static VarDataRAMStore invertNToMMap(
      VarDataStore inSrcToDstMap, long inDstSize, boolean inNoHoles) {
    if (inNoHoles)
      checkMapForHoles(inSrcToDstMap);
    long theSrcSize=inSrcToDstMap.getSize();
    long theSrcDataSize=inSrcToDstMap.getDataSize();
    VarChainRAMStore theDstToSrcChains=createEmptyChains(inDstSize,theSrcDataSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=(int) inSrcToDstMap.getNVars(i);
      if (theNDsts>theDsts.length) {
        theSliceStore.putLongSlice(theDsts);
        theDsts=new long[2*theNDsts];
      }
      inSrcToDstMap.getVars(i,theDsts,0);
      for (int j=0; j<theNDsts; j++) {
        long theDst=theDsts[j];
        if ((theDst<=kNotFound)||(theDst>=inDstSize))
          throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+
              " has value "+theDst+" of "+inDstSize);
        else 
          theDstToSrcChains.appendVar(theDst,i);
      }
    }
    theSliceStore.putLongSlice(theDsts);
    VarDataRAMStore theDstToSrcMap=theDstToSrcChains.getVarDataRAMStore();
    theDstToSrcChains.close();
    if (inNoHoles)
      checkMapForHoles(theDstToSrcMap);
    theDstToSrcMap.compact();
    return theDstToSrcMap;
  }

//--------------------------------------------------------------------------------------------------------
// invert1ToNMap
//
// Original 2D map, dst[src][]:
//   dst[0] = {0,1}        (when src=0, dst={0,1})
//   dst[1] = {2}          (when src=1, dst={2})
// Inverts to 1D map, src[dst]:
//   src[0] = 0            (when dst=0, src=0)
//   src[1] = 0            (when dst=1, src=0)
//   src[2] = 1            (when dst=2, src=1)
//
// A single src may point to N different dsts (No Redundancy!).
// Multiple srcs may not point to the same dst.
// A single dst points to a single src.
// N different dsts may point to the same src.
// src[] and dst[][] may be different lengths, depending on number of holes (missing values).
//
// If NoHoles:
//   all src point to one or more dst.
//   all dst point to one src.
//   src[] cannot be shorter than dst[][].
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore invert1ToNMap(VarDataStore inSrcToDstMap, long inDstSize, boolean inNoHoles) {
    if (inNoHoles)
      checkMapForHoles(inSrcToDstMap);
    long theSrcSize=inSrcToDstMap.getSize();
    VarRAMStore theDstToSrcMap=createEmptyMap(inDstSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=(int) inSrcToDstMap.getNVars(i);
      if (theNDsts>theDsts.length) {
        theSliceStore.putLongSlice(theDsts);
        theDsts=new long[2*theNDsts];
      }
      inSrcToDstMap.getVars(i,theDsts,0);
      for (int j=0; j<theNDsts; j++) {
        long theDst=theDsts[j];
        if ((theDst<=kNotFound)||(theDst>=inDstSize))
          throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+
              " has value "+theDst+" of "+inDstSize);
        else if (theDstToSrcMap.getLong(theDst)!=kNotFound)
          throw new RuntimeException("Map not 1:N, both row "+i+
              " & row "+theDstToSrcMap.getLong(theDst)+
              " of "+theSrcSize+" map to "+theDst+" of "+inDstSize);
        else
          theDstToSrcMap.setVar(theDst,i);
      }
    }
    theSliceStore.putLongSlice(theDsts);
    if (inNoHoles)
      checkMapForHoles(theDstToSrcMap);
    theDstToSrcMap.compact();
    return theDstToSrcMap;
  }

//--------------------------------------------------------------------------------------------------------
// cascadeMaps
//--------------------------------------------------------------------------------------------------------

  // ?:1 * ?:1 = ?:1
  public static VarRAMStore cascadeMaps(VarStore inSrcToMidMap, 
      VarStore inMidToDstMap, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToMidMap);
      checkMapForHoles(inMidToDstMap);
    }
    long theSrcSize=inSrcToMidMap.getSize();
    long theMidSize=inMidToDstMap.getSize();
    VarRAMStore theSrcToDstMap=new VarRAMStore(theSrcSize);
    for (long i=0; i<theSrcSize; i++) {
      long theMid=inSrcToMidMap.getLong(i); //  <-------------- Needs chunk up 
      if ((theMid<kNotFound)||(theMid>=theMidSize))
        throw new RuntimeException("SrcToMid map corrupt: elmt "+i+
            " of "+theSrcSize+" has value "+theMid+" of "+theMidSize);
      else if (theMid==kNotFound) 
        theSrcToDstMap.appendVar(kNotFound);
      else {
        long theDst=inMidToDstMap.getLong(theMid);  //  <-------------- Needs chunk up 
        if (theDst<kNotFound)
          throw new RuntimeException("MidToDst map corrupt: elmt "+theMid+" of "+theMidSize+
              " has value "+theDst);
        else 
          theSrcToDstMap.appendVar(theDst);
      }
    }
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

  // ?:1 * ?:N = ?:N
  public static VarDataRAMStore cascadeMaps(VarStore inSrcToMidMap, 
      VarDataStore inMidToDstMap, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToMidMap);
      checkMapForHoles(inMidToDstMap);
    }
    long theSrcSize=inSrcToMidMap.getSize();
    long theMidSize=inMidToDstMap.getSize();
    int theVarSize=inMidToDstMap.getVarSize();
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,2*theSrcSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      long theMid=inSrcToMidMap.getLong(i); //  <-------------- Needs chunk up 
      if ((theMid<kNotFound)||(theMid>=theMidSize))
        throw new RuntimeException("SrcToMidMap corrupt: elmt "+i+" of "+theSrcSize+
            " has value "+theMid);
      else if (theMid==kNotFound) 
        theSrcToDstMap.appendVars(kNoLongs);
      else {
        int theNDsts=(int) inMidToDstMap.getNVars(theMid);
        if (theNDsts>theDsts.length) {
          theSliceStore.putLongSlice(theDsts);
          theDsts=new long[2*theNDsts];
        }
        inMidToDstMap.getVars(theMid,theDsts,0);
        theSrcToDstMap.appendVars(theDsts,0,theNDsts);
      }
    }
    theSliceStore.putLongSlice(theDsts);
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

  // ?:N * ?:1 = ?:N
  // Uniques dsts
  public static VarDataRAMStore cascadeMaps(VarDataStore inSrcToMidMap, 
      VarStore inMidToDstMap, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToMidMap);
      checkMapForHoles(inMidToDstMap);
    }
    long theSrcSize=inSrcToMidMap.getSize();
    long theMidSize=inMidToDstMap.getSize();
    int theVarSize=inMidToDstMap.getVarSize();
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,2*theSrcSize);
    VarHashRAMStore theUniquer=new VarHashRAMStore();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theMids=theSliceStore.getLongSlice();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=0;
      int theNMids=(int) inSrcToMidMap.getNVars(i);
      if (theNMids>theMids.length) {
        theSliceStore.putLongSlice(theMids);
        theMids=new long[2*theNMids];
      }
      inSrcToMidMap.getVars(i,theMids,0);
      for (int j=0; j<theNMids; j++) {
        long theMid=theMids[j];
        if ((theMid<=kNotFound)||(theMid>=theMidSize))
          throw new RuntimeException("SrcToMid map corrupt: row "+i+
              " of "+theSrcSize+" has value "+theMid+" of "+theMidSize);
        long theDst=inMidToDstMap.getLong(theMid); //  <-------------- Needs chunk up 
        if (theDst!=kNotFound) {
          if (theNDsts==theDsts.length) {
            long[] theOldDsts=theDsts;
            theDsts=new long[2*theNDsts];
            System.arraycopy(theOldDsts,0,theDsts,0,theNDsts);
            theSliceStore.putLongSlice(theOldDsts);
          }
          theDsts[theNDsts]=theDst;
          theNDsts++;
        }
      }
      theNDsts=removeRedundancy(theDsts,theNDsts,theUniquer);
      theSrcToDstMap.appendVars(theDsts,0,theNDsts);
    }
    theSliceStore.putLongSlice(theMids);
    theSliceStore.putLongSlice(theDsts);
    theUniquer.close();
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

  // ?:N * ?:N = ?:N
  // Uniques dsts
  public static VarDataRAMStore cascadeMaps(VarDataStore inSrcToMidMap, 
      VarDataStore inMidToDstMap, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToMidMap);
      checkMapForHoles(inMidToDstMap);
    }
    long theSrcSize=inSrcToMidMap.getSize();
    long theMidSize=inMidToDstMap.getSize();
    int theVarSize=inMidToDstMap.getVarSize();
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,2*theSrcSize);
    VarHashRAMStore theUniquer=new VarHashRAMStore();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theMids=theSliceStore.getLongSlice();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=0;
      int theNMids=(int) inSrcToMidMap.getNVars(i);
      if (theNMids>theMids.length) {
        theSliceStore.putLongSlice(theMids);
        theMids=new long[2*theNMids];
      }
      inSrcToMidMap.getVars(i,theMids,0);
      for (int j=0; j<theNMids; j++) {
        long theMid=theMids[j];
        if ((theMid<=kNotFound)||(theMid>=theMidSize))
          throw new RuntimeException("SrcToMid map corrupt: row "+i+
              " of "+theSrcSize+" has value "+theMid+" of "+theMidSize);
        int theNewNDsts=theNDsts+(int) inMidToDstMap.getNVars(theMid);
        if (theNewNDsts>theDsts.length) {
          long[] theOldDsts=theDsts;
          theDsts=new long[2*theNewNDsts];
          System.arraycopy(theOldDsts,0,theDsts,0,theNDsts);
          theSliceStore.putLongSlice(theOldDsts);
        }
        inMidToDstMap.getVars(theMid,theDsts,theNDsts);
        theNDsts=theNewNDsts;
      }
      theNDsts=removeRedundancy(theDsts,theNDsts,theUniquer);
      theSrcToDstMap.appendVars(theDsts,0,theNDsts);
    }
    theSliceStore.putLongSlice(theDsts);
    theSliceStore.putLongSlice(theMids);
    theUniquer.close();
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

//--------------------------------------------------------------------------------------------------------
// mergeMaps
//--------------------------------------------------------------------------------------------------------

  // ?:1 + ?:1 = ?:N
  public static VarDataRAMStore mergeMaps(VarStore inSrcToDstMap1, 
      VarStore inSrcToDstMap2, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToDstMap1);
      checkMapForHoles(inSrcToDstMap2);
    }
    long[] theDsts=new long[2];
    long theSrcSize1=inSrcToDstMap1.getSize();
    long theSrcSize2=inSrcToDstMap2.getSize();
    long theSrcSize=Math.max(theSrcSize1,theSrcSize2);
    int theVarSize=Math.max(inSrcToDstMap1.getVarSize(),inSrcToDstMap2.getVarSize());
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,theSrcSize);
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=0;
      if (i<theSrcSize1) {
        long theDst=inSrcToDstMap1.getLong(i);  //  <-------------- Needs chunk up 
        if (theDst!=kNotFound) {
          theDsts[theNDsts]=theDst;
          theNDsts++;
        }
      }
      if (i<theSrcSize2) {
        long theDst=inSrcToDstMap2.getLong(i); //  <-------------- Needs chunk up 
        if (theDst!=kNotFound) {
          theDsts[theNDsts]=theDst;
          theNDsts++;
        }
      }
      if ((theNDsts==2)&&(theDsts[0]==theDsts[1]))
        theNDsts=1;
      theSrcToDstMap.appendVars(theDsts,0,theNDsts);
    }
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

  // ?:N + ?:N = ?:N
  // Uniques dsts
  public static VarDataRAMStore mergeMaps(VarDataStore inSrcToDstMap1, 
      VarDataStore inSrcToDstMap2, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToDstMap1);
      checkMapForHoles(inSrcToDstMap2);
    }
    long theSrcSize1=inSrcToDstMap1.getSize();
    long theSrcSize2=inSrcToDstMap2.getSize();
    long theSrcSize=Math.max(theSrcSize1,theSrcSize2);
    int theVarSize=Math.max(inSrcToDstMap1.getVarSize(),inSrcToDstMap2.getVarSize());
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,theSrcSize);
    VarHashRAMStore theUniquer=new VarHashRAMStore();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=0;
      if (i<theSrcSize1) {
        theNDsts=(int) inSrcToDstMap1.getNVars(i);
        if (theNDsts>theDsts.length) {
          theSliceStore.putLongSlice(theDsts);
          theDsts=new long[2*theNDsts];
        }
        inSrcToDstMap1.getVars(i,theDsts,0);
      }
      if (i<theSrcSize2) {
        int theNewNDsts=theNDsts+(int) inSrcToDstMap2.getNVars(i);
        if (theNewNDsts>theDsts.length) {
          long[] theOldDsts=theDsts;
          theDsts=new long[2*theNewNDsts];
          System.arraycopy(theOldDsts,0,theDsts,0,theNDsts);
          theSliceStore.putLongSlice(theOldDsts);
        }
        inSrcToDstMap2.getVars(i,theDsts,theNDsts);
        theNDsts=theNewNDsts;
      }
      theNDsts=removeRedundancy(theDsts,theNDsts,theUniquer);
      theSrcToDstMap.appendVars(theDsts,0,theNDsts);
    }
    theSliceStore.putLongSlice(theDsts);
    theUniquer.close();
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

  // ?:1 + ?:N = ?:N
  // Uniques dsts
  public static VarDataRAMStore mergeMaps(VarStore inSrcToDstMap1, 
      VarDataStore inSrcToDstMap2, boolean inNoHoles) {
    if (inNoHoles) {
      checkMapForHoles(inSrcToDstMap1);
      checkMapForHoles(inSrcToDstMap2);
    }
    long theSrcSize1=inSrcToDstMap1.getSize();
    long theSrcSize2=inSrcToDstMap2.getSize();
    long theSrcSize=Math.max(theSrcSize1,theSrcSize2);
    int theVarSize=Math.max(inSrcToDstMap1.getVarSize(),inSrcToDstMap2.getVarSize());
    VarDataRAMStore theSrcToDstMap=new VarDataRAMStore(theVarSize,theSrcSize,theSrcSize);
    VarHashRAMStore theUniquer=new VarHashRAMStore();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      // Get Dst value from 1st map - may be missing (i.e. kNotFound)
      long theDst1=kNotFound;
      if (i<theSrcSize1) 
        theDst1=inSrcToDstMap1.getLong(i);
      // Get Dst values from 2nd map - may be none
      int theNDsts2=0;
      if (i<theSrcSize2) {
        // Expand Dsts array if too many to fit
        theNDsts2=(int) inSrcToDstMap2.getNVars(i);
        if (theNDsts2>=theDsts.length) 
          theDsts=new long[2*theNDsts2];
        inSrcToDstMap2.getVars(i,theDsts,0);
      }
      // Merge Dst values, making sure no dups
      // If missing value from 1st map, just store values from 2nd
      if (theDst1==kNotFound)
        theSrcToDstMap.appendVars(theDsts,0,theNDsts2);
      // Else, look through values from 2nd map for a dup of 1st
      else {
        boolean theFound=false;
        for (int j=0; j<theNDsts2; j++) 
          if (theDst1==theDsts[j]) {
            theFound=true;
            break;
          }
        // If there was a dup, Dst values from 2nd map are complete, so do nothing
        if (theFound) {
        // If no dup, include Dst value from 1st map in Dsts
        } else {
          theDsts[theNDsts2]=theDst1;
          theNDsts2++;
        }
        // Append merged Dsts
        theSrcToDstMap.appendVars(theDsts,0,theNDsts2);
      }
    }
    theSliceStore.putLongSlice(theDsts);
    theUniquer.close();
    if (inNoHoles)
      checkMapForHoles(theSrcToDstMap);
    theSrcToDstMap.compact();
    return theSrcToDstMap;
  }

  // ?:N + ?:1 = ?:N
  public static VarDataRAMStore mergeMaps(VarDataStore inSrcToDstMap1, 
      VarStore inSrcToDstMap2, boolean inNoHoles) {
    return mergeMaps(inSrcToDstMap2,inSrcToDstMap1,inNoHoles); }
  
//--------------------------------------------------------------------------------------------------------
// countDstsForSrcs 
// returns count for each Src
//--------------------------------------------------------------------------------------------------------

  public static VarRAMStore countDstsForSrcs(VarDataStore inSrcToDstMap) {  
    long theSrcSize=inSrcToDstMap.getSize();
    VarRAMStore theCounts=new VarRAMStore(theSrcSize);
    for (int i=0; i<theSrcSize; i++) 
      theCounts.appendVar(inSrcToDstMap.getNVars(i));
    theCounts.compact();
    return theCounts;
  }
  
//--------------------------------------------------------------------------------------------------------
// countSrcsWithDsts 
//--------------------------------------------------------------------------------------------------------

  public static long countSrcsWithDsts(VarRAMStore inSrcToDstMap) {  
    long theSrcSize=inSrcToDstMap.getSize();
    long theNSrcsWithDsts=0;
    for (int i=0; i<theSrcSize; i++) 
      if (inSrcToDstMap.getLong(i)>=0)
        theNSrcsWithDsts++;
    return theNSrcsWithDsts;
  }

  public static long countSrcsWithDsts(VarDataStore inSrcToDstMap) {  
    long theSrcSize=inSrcToDstMap.getSize();
    long theNSrcsWithDsts=0;
    for (int i=0; i<theSrcSize; i++) 
      if (inSrcToDstMap.getNVars(i)>0)
        theNSrcsWithDsts++;
    return theNSrcsWithDsts;
  }

//--------------------------------------------------------------------------------------------------------
// uniqDsts
//
// returns UniqDst for DstDx (i.e. DstDxToDstMap)
//--------------------------------------------------------------------------------------------------------

  // ?:1
  public static VarHashRAMStore uniqDsts(VarStore inSrcToDstMap) {
    long theSrcSize=inSrcToDstMap.getSize();
    int theVarSize=inSrcToDstMap.getVarSize();
    VarHashRAMStore theDstDxToDstMap=new VarHashRAMStore(theVarSize,theSrcSize);
    for (long i=0; i<theSrcSize; i++) {
      long theDst=inSrcToDstMap.getLong(i);  //  <-------------- Needs chunk up 
      if (theDst<kNotFound)
        throw new RuntimeException("Map corrupt: elmt "+i+" of "+theSrcSize+" has value "+theDst);
      else if (theDst!=kNotFound) 
        theDstDxToDstMap.appendVar(theDst);
    }
    theDstDxToDstMap.compact();
    return theDstDxToDstMap;
  }

  // ?:N
  public static VarHashRAMStore uniqDsts(VarDataStore inSrcToDstMap) {
    long theSrcSize=inSrcToDstMap.getSize();
    int theVarSize=inSrcToDstMap.getVarSize();
    VarHashRAMStore theDstDxToDstMap=new VarHashRAMStore(theVarSize,theSrcSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=(int) inSrcToDstMap.getNVars(i);
      if (theNDsts>theDsts.length) {
        theSliceStore.putLongSlice(theDsts);
        theDsts=new long[2*theNDsts];
      }
      inSrcToDstMap.getVars(i,theDsts,0);
      for (int j=0; j<theNDsts; j++) {
        long theDst=theDsts[j];
        if (theDst<=kNotFound)
          throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+" has value "+theDst);
        theDstDxToDstMap.appendVar(theDst);
      }
    }
    theSliceStore.putLongSlice(theDsts);
    theDstDxToDstMap.compact();
    return theDstDxToDstMap;
  }
  
//--------------------------------------------------------------------------------------------------------
// countDstsWithSrcs
//--------------------------------------------------------------------------------------------------------

  public static long countDstsWithSrcs(VarRAMStore inSrcToDstMap) {  
    VarHashRAMStore theUniqueDsts=uniqDsts(inSrcToDstMap);
    long theNDsts=theUniqueDsts.getSize();
    theUniqueDsts.close();
    return theNDsts;
  }

  public static long countDstsWithSrcs(VarDataStore inSrcToDstMap) {  
    VarHashRAMStore theUniqueDsts=uniqDsts(inSrcToDstMap);
    long theNDsts=theUniqueDsts.getSize();
    theUniqueDsts.close();
    return theNDsts;
  }

//--------------------------------------------------------------------------------------------------------
// uniqAndCountDsts
//
// works when ioDstDxToDstMap and ioDstDxToDstNOccrs are already partially loaded
// returns UniqDst for DstDx (i.e. DstDxToDstMap)
// returns count for each UniqDst (i.e. DstDxToDstNOccrs)
//--------------------------------------------------------------------------------------------------------

  // ?:1
  public static void uniqAndCountDsts(VarStore inSrcToDstMap, 
      VarHashStore ioDstDxToDstMap, VarStore ioDstDxToDstNOccrs) {
    long theSrcSize=inSrcToDstMap.getSize();
    for (long i=0; i<theSrcSize; i++) {
      long theDst=inSrcToDstMap.getLong(i); //  <-------------- Needs chunk up 
      if (theDst<kNotFound)
        throw new RuntimeException("Map corrupt: elmt "+i+" of "+theSrcSize+" has value "+theDst);
      else if (theDst!=kNotFound) {
        long theDstDx=ioDstDxToDstMap.appendVar(theDst);
        if (theDstDx==kNotFound)
          ioDstDxToDstNOccrs.appendVar(1);
        else
          ioDstDxToDstNOccrs.addToVar(theDstDx,1);
      }
    }
    ioDstDxToDstMap.compact();
    ioDstDxToDstNOccrs.compact();
  }

  // ?:N
  public static void uniqAndCountDsts(VarDataStore inSrcToDstMap,
      VarHashStore ioDstDxToDstMap, VarStore ioDstDxToDstNOccrs) {
    long theSrcSize=inSrcToDstMap.getSize();
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=(int) inSrcToDstMap.getNVars(i);
      if (theNDsts>theDsts.length) {
        theSliceStore.putLongSlice(theDsts);
        theDsts=new long[2*theNDsts];
      }
      inSrcToDstMap.getVars(i,theDsts,0);
      for (int j=0; j<theNDsts; j++) {
        long theDst=theDsts[j];
        if (theDst<=kNotFound)
          throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+" has value "+theDst);
        long theDstDx=ioDstDxToDstMap.appendVar(theDst);
        if (theDstDx==kNotFound)
          ioDstDxToDstNOccrs.appendVar(1);
        else
          ioDstDxToDstNOccrs.addToVar(theDstDx,1);
      }
    }
    theSliceStore.putLongSlice(theDsts);
    ioDstDxToDstMap.compact();
    ioDstDxToDstNOccrs.compact();
  }

//--------------------------------------------------------------------------------------------------------
// uniqAndCountAndCascadeDsts
//
// works when ioDstDxToDstMap and ioDstDxToDstNOccrs are already partially loaded
// returns UniqDst for DstDx (i.e. DstDxToDstMap)
// returns count for each UniqDst (i.e. DstDxToDstNOccrs)
// returns input map with Dsts replaced by DstDxs (i.e. SrcToDstDxMap)
//--------------------------------------------------------------------------------------------------------

  // ?:1
  public static VarRAMStore uniqAndCountAndCascadeDsts(VarStore inSrcToDstMap, 
      VarHashStore ioDstDxToDstMap, VarStore ioDstNOccrs) {
    long theSrcSize=inSrcToDstMap.getSize();
    VarRAMStore theSrcToDstDxMap=new VarRAMStore(theSrcSize);
    for (long i=0; i<theSrcSize; i++) {
      long theDst=inSrcToDstMap.getLong(i); //  <-------------- Needs chunk up 
      if (theDst<kNotFound)
        throw new RuntimeException("Map corrupt: elmt "+i+" of "+theSrcSize+" has value "+theDst);
      else if (theDst!=kNotFound) {
        long theDstDx=ioDstDxToDstMap.appendVar(theDst);
        if (theDstDx==kNotFound) {
          ioDstNOccrs.appendVar(1);
          theDstDx=ioDstDxToDstMap.getSize()-1;
        } else
          ioDstNOccrs.addToVar(theDstDx,1);
        theSrcToDstDxMap.appendVar(theDstDx);
      }
    }
    ioDstDxToDstMap.compact();
    ioDstNOccrs.compact();
    theSrcToDstDxMap.compact();
    return theSrcToDstDxMap;
  }

  // ?:N
  public static VarDataRAMStore uniqAndCountAndCascadeDsts(VarDataStore inSrcToDstMap,
      VarHashStore ioDstDxToDstMap, VarStore ioDstNOccrss) {
    long theSrcSize=inSrcToDstMap.getSize();
    long theSrcDataSize=inSrcToDstMap.getDataSize();
    int theVarSize=Conversions.calcVarLongSize(theSrcSize);
    VarDataRAMStore theSrcToDstDxsMap=new VarDataRAMStore(theVarSize,theSrcSize,theSrcDataSize);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    long[] theDsts=theSliceStore.getLongSlice();
    for (long i=0; i<theSrcSize; i++) {
      int theNDsts=(int) inSrcToDstMap.getNVars(i);
      if (theNDsts>theDsts.length) {
        theSliceStore.putLongSlice(theDsts);
        theDsts=new long[2*theNDsts];
      }
      inSrcToDstMap.getVars(i,theDsts,0);
      for (int j=0; j<theNDsts; j++) {
        long theDst=theDsts[j];
        if (theDst<=kNotFound)
          throw new RuntimeException("Map corrupt: row "+i+" of "+theSrcSize+" has value "+theDst);
        long theDstDx=ioDstDxToDstMap.appendVar(theDst);
        if (theDstDx==kNotFound) {
          ioDstNOccrss.appendVar(1);
          theDstDx=ioDstDxToDstMap.getSize()-1;
        } else
          ioDstNOccrss.addToVar(theDstDx,1);
        theDsts[j]=theDstDx;
      }
      theSrcToDstDxsMap.appendVars(theDsts,0,theNDsts);
    }
    theSliceStore.putLongSlice(theDsts);
    ioDstDxToDstMap.compact();
    ioDstNOccrss.compact();
    theSrcToDstDxsMap.compact();
    return theSrcToDstDxsMap;
  }

}
