//--------------------------------------------------------------------------------------------------------
// Comparisons.java
//--------------------------------------------------------------------------------------------------------

package gravel.sort;

import java.util.*;

import gravel.norm.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// Comparisons
//--------------------------------------------------------------------------------------------------------

public class Comparisons {

//--------------------------------------------------------------------------------------------------------
// Comparisons consts
//--------------------------------------------------------------------------------------------------------

  public static final byte  kLessThan=Comparators.kLessThan;          // -1
  public static final byte  kEquals=Comparators.kEquals;              //  0
  public static final byte  kGreaterThan=Comparators.kGreaterThan;    //  1

  public static final byte  kCaseMatters=0;
  public static final byte  kIgnoreCase=1;
  public static final byte  kCaseBreaksTies=2;
  public static final byte  kNormChars=3;       // Throws exception if char norm not loaded
  public static final byte  kRawBreaksTies=4;   // Ditto

  public static final byte  kBinary=0;          // Bytes compared as bytes - same as kCaseMatters
  public static final byte  kVarLengthNum=3;    // First byte has sign, rest are treated as unsigned ints
  
  public static final int   kToLower=('a'-'A');

//--------------------------------------------------------------------------------------------------------
// compareBytes
//--------------------------------------------------------------------------------------------------------

  public static int compareBytes(byte[] inBytes1, int inOffset1, int inLength1,
      byte[] inBytes2, int inOffset2, int inLength2, byte inHandleCase) {
    
    if ((inHandleCase==kIgnoreCase)||(inHandleCase==kCaseBreaksTies))
      return UTF8Utils.compareUTF8Bytes(inBytes1,inOffset1,inLength1,
          inBytes2,inOffset2,inLength2,inHandleCase);
    
    // Binary bytes
    if (inHandleCase==kBinary) {
      int theMinLength=Math.min(inLength1,inLength2);
      for (int i=0; i<theMinLength; i++) {
        byte theByte1=inBytes1[inOffset1+i];
        byte theByte2=inBytes2[inOffset2+i];
        if (theByte1>theByte2)
          return kGreaterThan;
        else if (theByte1<theByte2)
          return kLessThan;
      }
      if (inLength1>inLength2)
        return kGreaterThan;
      else if (inLength1<inLength2)
        return kLessThan;
      else
        return kEquals;

    // kVarLengthNum
    } else {
      // Same length
      if (inLength1==inLength2) {
        // zero = zero
        if (inLength1==0)
          return kEquals;
        // check first signed byte
        byte theByte1=inBytes1[inOffset1];
        byte theByte2=inBytes2[inOffset2];
        if (theByte1>theByte2)
          return kGreaterThan;
        else if (theByte1<theByte2)
          return kLessThan;
        // Same sign and length and first byte, check remaining unsigned bytes
        for (int i=1; i<inLength1; i++) {
          int theInt1=0x000000ff&inBytes1[inOffset1+i];
          int theInt2=0x000000ff&inBytes2[inOffset2+i];
          if (theInt1>theInt2) 
            return kGreaterThan;
          else if (theInt1<theInt2) 
            return kLessThan;
        }
        return kEquals;
        
      // Different lengths
      } else {
        // Check for different signs 
        boolean theNegative1=((inLength1>0)&&(inBytes1[inOffset1]<0));
        boolean theNegative2=((inLength2>0)&&(inBytes2[inOffset2]<0));
        if (theNegative1) {
          if ((!theNegative2)||(inLength1>inLength2))
            return kLessThan;
          else
            return kGreaterThan;
        } else {
          if ((theNegative2)||(inLength1>inLength2))
            return kGreaterThan;
          else
            return kLessThan;
        }
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// compareBytes
//--------------------------------------------------------------------------------------------------------

  public static int compareBytes(byte[] inBytes1, byte[] inBytes2, byte inHandleCase) {
    if ((inBytes1==null)&&(inBytes2==null))
      return kEquals;
    else if ((inBytes1!=null)&&(inBytes2!=null))
      return compareBytes(inBytes1,0,inBytes1.length,inBytes2,0,inBytes2.length,inHandleCase); 
    else if (inBytes1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(byte[] inBytes1, byte[] inBytes2, byte inHandleCase) {
    return (compareBytes(inBytes1,inBytes2,inHandleCase)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareBooleans
//--------------------------------------------------------------------------------------------------------

  public static int compareBooleans(boolean[] inBooleans1, int inOffset1, int inLength1,
          boolean[] inBooleans2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      boolean theBoolean1=inBooleans1[inOffset1+i];
      boolean theBoolean2=inBooleans2[inOffset2+i];
      if (theBoolean1!=theBoolean2)
        if (theBoolean1)
          return kGreaterThan;
        else
          return kLessThan;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareBooleans
//--------------------------------------------------------------------------------------------------------

  public static int compareBooleans(boolean[] inBooleans1, boolean[] inBooleans2) {
    if ((inBooleans1==null)&&(inBooleans2==null))
      return kEquals;
    else if ((inBooleans1!=null)&&(inBooleans2!=null))
      return compareBooleans(inBooleans1,0,inBooleans1.length,inBooleans2,0,inBooleans2.length); 
    else if (inBooleans1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(boolean[] inBooleans1, boolean[] inBooleans2) {
    return (compareBooleans(inBooleans1,inBooleans2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareShorts
//--------------------------------------------------------------------------------------------------------

  public static short compareShorts(short[] inShorts1, int inOffset1, int inLength1,
          short[] inShorts2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (short i=0; i<theMinLength; i++) {
      short theShort1=inShorts1[inOffset1+i];
      short theShort2=inShorts2[inOffset2+i];
      if (theShort1>theShort2)
        return kGreaterThan;
      else if (theShort1<theShort2)
        return kLessThan;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareShorts
//--------------------------------------------------------------------------------------------------------

  public static short compareShorts(short[] inShorts1, short[] inShorts2) {
    if ((inShorts1==null)&&(inShorts2==null))
      return kEquals;
    else if ((inShorts1!=null)&&(inShorts2!=null))
      return compareShorts(inShorts1,0,inShorts1.length,inShorts2,0,inShorts2.length); 
    else if (inShorts1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(short[] inShorts1, short[] inShorts2) {
    return (compareShorts(inShorts1,inShorts2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareChars
//
// Handles two cases well
//  1) Case matters = Binary comparison
//  2) Ignore case = Lower case, then compare
// 
// CaseBreaksTies is tricky.  It should apply to the entire string, not individual chars
// Only if the entire string is equal with IgnoreCase, do we retry with CaseMatters
// This gives the desired behavior of:  AB < ab < ABC < abC < abc
//
// NormChars and RawBreaksTies are even more problematic and not supported at all
// Char norm does not preserve the number of chars and is confusing chars by char
//--------------------------------------------------------------------------------------------------------

  public static int compareChars(char inChar1, char inChar2, byte inHandleCase) {
    if (inChar1==inChar2) 
      return kEquals;
    
    if (inHandleCase==kCaseMatters) {
      // Equals handled above
      if (inChar1>inChar2)
        return kGreaterThan;
      else 
        return kLessThan;
    }
    
    // Short cut if ASCII 7 (norm siplifies to lowercase for ASCII 7)
    char theChar1=inChar1;   
    char theChar2=inChar2;   
    if ((theChar1<=127)&&(theChar2<=127)) {
      
      // Lowercase compare
      if ((theChar1>='A')&&(theChar1<='Z'))
        theChar1+=kToLower;
      if ((theChar2>='A')&&(theChar2<='Z'))
        theChar2+=kToLower;
      if (theChar1>theChar2)
        return kGreaterThan;
      else if (theChar1<theChar2)
        return kLessThan;
      else 
        return kEquals;       //    <--  CaseBreaksTies weirdness
      // if CaseBreaksTies, don't check case character by character.
      // Must wait till entire string is equal with IgnoreCase, then retry with CaseMatters
      
    // Unicode case comparison
    } else {
      
      // NormChars and RawBreaksTies not supported
      if ((inHandleCase==kNormChars)||(inHandleCase==kRawBreaksTies))
        throw new RuntimeException("compareChars does not support kNormChars or kRawBreaksTies for single chars");
      
      // Equal if if upper case equal 
      // Sadly, toUpper and toLower not 1:1 - need to try both
      theChar1=Character.toUpperCase(inChar1);
      theChar2=Character.toUpperCase(inChar2);
      if (theChar1==theChar2) 
        return kEquals;

      // Lowercase compare
      theChar1=Character.toLowerCase(inChar1);
      theChar2=Character.toLowerCase(inChar2);
      if (theChar1>theChar2)
        return kGreaterThan;
      else if (theChar1<theChar2)
        return kLessThan;
      else 
        return kEquals;       //    <--  CaseBreaksTies weirdness (see above)
    }
  }

//--------------------------------------------------------------------------------------------------------
// compareChars
//
// Five cases
//  1) Case matters = Binary comparison
//  2) Ignore case = Lower case, then compare
//  3) Case breaks ties = Ignore case, then if equal Case matters
//  4) Norm chars = Norm chars, then compare
//  5) Raw breaks ties = Norm chars, then if equal Case breaks ties
// Norm chars and Raw breaks ties throw exceptions if CharNorm is not loaded
//--------------------------------------------------------------------------------------------------------
  
  public static int compareChars(char[] inChars1, int inOffset1, int inLength1,
      char[] inChars2, int inOffset2, int inLength2, byte inHandleCase) {
    
    boolean theNeedNorm=((inHandleCase==kNormChars)||(inHandleCase==kRawBreaksTies));
    if (!theNeedNorm) {
      
      int theMinLength=Math.min(inLength1,inLength2);
      for (int i=0; i<theMinLength; i++) {
        char theChar1=inChars1[inOffset1+i];
        char theChar2=inChars2[inOffset2+i];
        int theDiff=compareChars(theChar1,theChar2,inHandleCase);
        if (theDiff>0) 
          return kGreaterThan;
        else if (theDiff<0) 
          return kLessThan;
      }
      int theDiff=inLength1-inLength2;
      if (theDiff>0) 
        return kGreaterThan;
      else if (theDiff<0) 
        return kLessThan;
      else if (inHandleCase!=kCaseBreaksTies)
        return kEquals;
      else  // CaseBreaksTies 
        return compareChars(inChars1,inOffset1,inLength1,inChars2,inOffset2,inLength2,kCaseMatters);

    } else {
    
      if (!CharNorm.isLoaded())
        throw new RuntimeException("CharNorm must be loaded for compareChars to support kNormChars or kRawBreaksTies");
        
      char[] theNormChars1=SliceStore.getSliceStore().getCharSlice();
      char[] theNormChars2=SliceStore.getSliceStore().getCharSlice();
      int theLength1=CharNorm.normChars(inChars1,inOffset1,inLength1,theNormChars1,0,null);
      int theLength2=CharNorm.normChars(inChars2,inOffset2,inLength2,theNormChars2,0,null);
      int theDiff=compareChars(theNormChars1,0,theLength1,theNormChars2,0,theLength2,kBinary);
      SliceStore.getSliceStore().putCharSlice(theNormChars2);
      SliceStore.getSliceStore().putCharSlice(theNormChars1);
      if (theDiff>0) 
        return kGreaterThan;
      else if (theDiff<0) 
        return kLessThan;
      else if (inHandleCase!=kRawBreaksTies)
        return kEquals;
      else  // Raw breaks ties
        return compareChars(inChars1,inOffset1,inLength1,inChars2,inOffset2,inLength2,kCaseBreaksTies);
    }
  }

//--------------------------------------------------------------------------------------------------------
// compareChars
//--------------------------------------------------------------------------------------------------------

  public static int compareChars(char[] inChars1, char[] inChars2, byte inHandleCase) {
    if ((inChars1==null)&&(inChars2==null))
      return kEquals;
    else if ((inChars1!=null)&&(inChars2!=null))
      return compareChars(inChars1,0,inChars1.length,inChars2,0,inChars2.length,inHandleCase); 
    else if (inChars1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(char[] inChars1, char[] inChars2, byte inHandleCase) {
    return (compareChars(inChars1,inChars2,inHandleCase)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareStrings
//--------------------------------------------------------------------------------------------------------

  public static int compareStrings(String inString1, String inString2, byte inHandleCase) {
    int theLength1=(inString1==null)?-1:inString1.length();
    int theLength2=(inString2==null)?-1:inString2.length();
    if (Math.min(theLength1,theLength2)<=0) {
      int theDiff=theLength1-theLength2;
      if (theDiff>0) 
        return kGreaterThan;
      else if (theDiff<0) 
        return kLessThan;
      else 
        return kEquals;
    }
    char[] theBuffer1=SliceStore.getSliceStore().getCharSlice();
    char[] theBuffer2=SliceStore.getSliceStore().getCharSlice();
    inString1.getChars(0,theLength1,theBuffer1,0);
    inString2.getChars(0,theLength2,theBuffer2,0);
    int theDiff=compareChars(theBuffer1,0,theLength1,theBuffer2,0,theLength2,inHandleCase);
    SliceStore.getSliceStore().putCharSlice(theBuffer2);
    SliceStore.getSliceStore().putCharSlice(theBuffer1);
    return theDiff;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(String inString1, String inString2, byte inHandleCase) {
    return (compareStrings(inString1,inString2,inHandleCase)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareStrings
//--------------------------------------------------------------------------------------------------------

  public static int compareStrings(String[] inStrings1, int inOffset1, int inLength1,
          String[] inStrings2, int inOffset2, int inLength2, byte inHandleCase) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      String theString1=inStrings1[inOffset1+i];
      String theString2=inStrings2[inOffset2+i];
      int theDiff=compareStrings(theString1,theString2,inHandleCase);
      if (theDiff!=kEquals)
        return theDiff;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareStrings
//--------------------------------------------------------------------------------------------------------

  public static int compareStrings(String[] inStrings1, String[] inStrings2, byte inHandleCase) {
    if ((inStrings1==null)&&(inStrings2==null))
      return kEquals;
    else if ((inStrings1!=null)&&(inStrings2!=null))
      return compareStrings(inStrings1,0,inStrings1.length,inStrings2,0,inStrings2.length,inHandleCase); 
    else if (inStrings1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(String[] inStrings1, String[] inStrings2, byte inHandleCase) {
    return (compareStrings(inStrings1,inStrings2,inHandleCase)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareInts
//--------------------------------------------------------------------------------------------------------

  public static int compareInts(int[] inInts1, int inOffset1, int inLength1,
          int[] inInts2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      int theInt1=inInts1[inOffset1+i];
      int theInt2=inInts2[inOffset2+i];
      if (theInt1>theInt2)
        return kGreaterThan;
      else if (theInt1<theInt2)
        return kLessThan;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareInts
//--------------------------------------------------------------------------------------------------------

  public static int compareInts(int[] inInts1, int[] inInts2) {
    if ((inInts1==null)&&(inInts2==null))
      return kEquals;
    else if ((inInts1!=null)&&(inInts2!=null))
      return compareInts(inInts1,0,inInts1.length,inInts2,0,inInts2.length); 
    else if (inInts1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

  public static int compareInts(int[][] inIntss1, int[][] inIntss2) {
    if ((inIntss1==null)&&(inIntss2==null))
      return kEquals;
    else if ((inIntss1!=null)&&(inIntss2!=null)) {
      int theMinLength=Math.min(inIntss1.length,inIntss2.length);
      for (int i=0; i<theMinLength; i++) {
        int theDiff=compareInts(inIntss1[i],inIntss2[i]);
        if (theDiff!=kEquals)
          return theDiff;
      }
      if (inIntss1.length>inIntss2.length)
        return kGreaterThan;
      else if (inIntss1.length<inIntss2.length)
        return kLessThan;
      else
        return kEquals;
    } else if (inIntss1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

  public static int compareInts(int[][][] inIntsss1, int[][][] inIntsss2) {
    if ((inIntsss1==null)&&(inIntsss2==null))
      return kEquals;
    else if ((inIntsss1!=null)&&(inIntsss2!=null)) {
      int theMinLength=Math.min(inIntsss1.length,inIntsss2.length);
      for (int i=0; i<theMinLength; i++) {
        int theDiff=compareInts(inIntsss1[i],inIntsss2[i]);
        if (theDiff!=kEquals)
          return theDiff;
      }
      if (inIntsss1.length>inIntsss2.length)
        return kGreaterThan;
      else if (inIntsss1.length<inIntsss2.length)
        return kLessThan;
      else
        return kEquals;
    } else if (inIntsss1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(int[] inInts1, int[] inInts2) {
    return (compareInts(inInts1,inInts2)==kEquals); }

  public static boolean areEqual(int[][] inIntss1, int[][] inIntss2) {
    return (compareInts(inIntss1,inIntss2)==kEquals); }

  public static boolean areEqual(int[][][] inIntsss1, int[][][] inIntsss2) {
    return (compareInts(inIntsss1,inIntsss2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareFloats
//--------------------------------------------------------------------------------------------------------

  public static int compareFloats(float[] inFloats1, int inOffset1, int inLength1,
          float[] inFloats2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      float theFloat1=inFloats1[inOffset1+i];
      float theFloat2=inFloats2[inOffset2+i];
      if (theFloat1>theFloat2)
        return kGreaterThan;
      else if (theFloat1<theFloat2)
        return kLessThan;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareFloats
//--------------------------------------------------------------------------------------------------------

  public static int compareFloats(float[] inFloats1, float[] inFloats2) {
    if ((inFloats1==null)&&(inFloats2==null))
      return kEquals;
    else if ((inFloats1!=null)&&(inFloats2!=null))
      return compareFloats(inFloats1,0,inFloats1.length,inFloats2,0,inFloats2.length); 
    else if (inFloats1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

  public static float compareFloats(float[][] inFloatss1, float[][] inFloatss2) {
    if ((inFloatss1==null)&&(inFloatss2==null))
      return kEquals;
    else if ((inFloatss1!=null)&&(inFloatss2!=null)) {
      float theMinLength=Math.min(inFloatss1.length,inFloatss2.length);
      for (int i=0; i<theMinLength; i++) {
        float theDiff=compareFloats(inFloatss1[i],inFloatss2[i]);
        if (theDiff!=kEquals)
          return theDiff;
      }
      if (inFloatss1.length>inFloatss2.length)
        return kGreaterThan;
      else if (inFloatss1.length<inFloatss2.length)
        return kLessThan;
      else
        return kEquals;
    } else if (inFloatss1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

  public static float compareFloats(float[][][] inFloatsss1, float[][][] inFloatsss2) {
    if ((inFloatsss1==null)&&(inFloatsss2==null))
      return kEquals;
    else if ((inFloatsss1!=null)&&(inFloatsss2!=null)) {
      float theMinLength=Math.min(inFloatsss1.length,inFloatsss2.length);
      for (int i=0; i<theMinLength; i++) {
        float theDiff=compareFloats(inFloatsss1[i],inFloatsss2[i]);
        if (theDiff!=kEquals)
          return theDiff;
      }
      if (inFloatsss1.length>inFloatsss2.length)
        return kGreaterThan;
      else if (inFloatsss1.length<inFloatsss2.length)
        return kLessThan;
      else
        return kEquals;
    } else if (inFloatsss1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(float[] inFloats1, float[] inFloats2) {
    return (compareFloats(inFloats1,inFloats2)==kEquals); }

  public static boolean areEqual(float[][] inFloatss1, float[][] inFloatss2) {
    return (compareFloats(inFloatss1,inFloatss2)==kEquals); }

  public static boolean areEqual(float[][][] inFloatsss1, float[][][] inFloatsss2) {
    return (compareFloats(inFloatsss1,inFloatsss2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareLongs
//--------------------------------------------------------------------------------------------------------

  public static int compareLongs(long[] inLongs1, int inOffset1, int inLength1,
          long[] inLongs2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      long theLong1=inLongs1[inOffset1+i];
      long theLong2=inLongs2[inOffset2+i];
      if (theLong1>theLong2)
        return kGreaterThan;
      else if (theLong1<theLong2)
        return kLessThan;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareLongs
//--------------------------------------------------------------------------------------------------------

  public static int compareLongs(long[] inLongs1, long[] inLongs2) {
    if ((inLongs1==null)&&(inLongs2==null))
      return kEquals;
    else if ((inLongs1!=null)&&(inLongs2!=null))
      return compareLongs(inLongs1,0,inLongs1.length,inLongs2,0,inLongs2.length); 
    else if (inLongs1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(long[] inLongs1, long[] inLongs2) {
    return (compareLongs(inLongs1,inLongs2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareDoubles
//--------------------------------------------------------------------------------------------------------

  public static int compareDoubles(double[] inDoubles1, int inOffset1, int inLength1,
          double[] inDoubles2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      double theDouble1=inDoubles1[inOffset1+i];
      double theDouble2=inDoubles2[inOffset2+i];
      if (theDouble1>theDouble2)
        return kGreaterThan;
      else if (theDouble1<theDouble2)
        return kLessThan;
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareDoubles
//--------------------------------------------------------------------------------------------------------

  public static int compareDoubles(double[] inDoubles1, double[] inDoubles2) {
    if ((inDoubles1==null)&&(inDoubles2==null))
      return kEquals;
    else if ((inDoubles1!=null)&&(inDoubles2!=null))
      return compareDoubles(inDoubles1,0,inDoubles1.length,inDoubles2,0,inDoubles2.length); 
    else if (inDoubles1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(double[] inDoubles1, double[] inDoubles2) {
    return (compareDoubles(inDoubles1,inDoubles2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareObjects
//--------------------------------------------------------------------------------------------------------

  public static int compareObjects(Comparable[] inObjects1, int inOffset1, int inLength1,
      Comparable[] inObjects2, int inOffset2, int inLength2) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      Comparable theObject1=inObjects1[inOffset1+i];
      Comparable theObject2=inObjects2[inOffset2+i];
      if (theObject1!=theObject2) {
        int theDiff=theObject1.compareTo(theObject2);
        if (theDiff>0)
          return kGreaterThan;
        else if (theDiff<0)
          return kLessThan;
      }
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareObjects
//--------------------------------------------------------------------------------------------------------

  public static int compareObjects(Comparable[] inObjects1, Comparable[] inObjects2) {
    if ((inObjects1==null)&&(inObjects2==null))
      return kEquals;
    else if ((inObjects1!=null)&&(inObjects2!=null))
      return compareObjects(inObjects1,0,inObjects1.length,inObjects2,0,inObjects2.length); 
    else if (inObjects1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(Comparable[] inObjects1, Comparable[] inObjects2) {
    return (compareObjects(inObjects1,inObjects2)==kEquals); }

//--------------------------------------------------------------------------------------------------------
// compareObjects
//--------------------------------------------------------------------------------------------------------

  public static int compareObjects(Object[] inObjects1, int inOffset1, int inLength1,
      Object[] inObjects2, int inOffset2, int inLength2, Comparator inComparator) {
    int theMinLength=Math.min(inLength1,inLength2);
    for (int i=0; i<theMinLength; i++) {
      Object theObject1=inObjects1[inOffset1+i];
      Object theObject2=inObjects2[inOffset2+i];
      if (theObject1!=theObject2) {
        int theDiff=inComparator.compare(theObject1,theObject2);
        if (theDiff>0)
          return kGreaterThan;
        else if (theDiff<0)
          return kLessThan;
      }
    }
    if (inLength1>inLength2)
      return kGreaterThan;
    else if (inLength1<inLength2)
      return kLessThan;
    else
      return kEquals;
  }

//--------------------------------------------------------------------------------------------------------
// compareObjects
//--------------------------------------------------------------------------------------------------------

  public static int compareObjects(Object[] inObjects1, Object[] inObjects2, Comparator inComparator) {
    if ((inObjects1==null)&&(inObjects2==null))
      return kEquals;
    else if ((inObjects1!=null)&&(inObjects2!=null))
      return compareObjects(inObjects1,0,inObjects1.length,inObjects2,0,inObjects2.length,inComparator); 
    else if (inObjects1!=null)
      return kGreaterThan;
    else
      return kLessThan;
  }

//--------------------------------------------------------------------------------------------------------
// areEqual
//--------------------------------------------------------------------------------------------------------

  public static boolean areEqual(Object[] inObjects1, Object[] inObjects2, Comparator inComparator) {
    return (compareObjects(inObjects1,inObjects2,inComparator)==kEquals); }

}

