//--------------------------------------------------------------------------------------------------------
// HashUtils
//--------------------------------------------------------------------------------------------------------

package gravel.store.hash;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// HashUtils
//--------------------------------------------------------------------------------------------------------

public class HashUtils implements Constants {
  
  public static final long    kBigPrime=0x05209480a3761225L;
  public static final long    kBigPrime2=0x069a71c046f325adL;
  public static final long    kBigPrime3=0x0710c8a4b96f1859L;
  
//--------------------------------------------------------------------------------------------------------
// hash
//
// Ints are their own hash
//
// Note that chars are essentially treated as 3 byte ints, not 2 byte shorts
//--------------------------------------------------------------------------------------------------------

  public static long hash(byte inByte) { return inByte; }
  public static long hash(int inInt) { return inInt; }
  public static long hash(long inLong) { return inLong; }  
  public static long hash(float inFloat) { return hash(Conversions.floatToInt(inFloat)); }
  public static long hash(double inDouble) { return hash(Conversions.doubleToLong(inDouble)); }

  public static long hash(char inChar) { return inChar; } 

  public static long hash(String inString) { 
    int theLength=inString.length();
    long theHash=theLength;
    boolean theIsLong=(theLength>16);
    for (int i=0; i<theLength; i++) {
      theHash*=kBigPrime;
      theHash^=inString.charAt(i);
//      theHash+=hash(inString.charAt(i));
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,theLength-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(byte[] inBytes, int inByteDelta, int inNBytes) {
    long theHash=inNBytes;
    boolean theIsLong=(inNBytes>16);
    for (int i=0; i<inNBytes; i++) {
      theHash*=kBigPrime;
      theHash^=inBytes[inByteDelta+i];
//      theHash+=hash(inBytes[inByteDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNBytes-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(byte[] inBytes) { return hash(inBytes,0,inBytes.length); }

  public static long hash(char[] inChars, int inCharDelta, int inNChars) {
    long theHash=inNChars;
    boolean theIsLong=(inNChars>16);
    for (int i=0; i<inNChars; i++) {
      theHash*=kBigPrime;
      theHash^=inChars[inCharDelta+i]; 
//      theHash+=hash(inChars[inCharDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNChars-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(char[] inChars) { return hash(inChars,0,inChars.length); }

  public static long hash(int[] inInts, int inIntDelta, int inNInts) {
    long theHash=inNInts;
    boolean theIsLong=(inNInts>16);
    for (int i=0; i<inNInts; i++) {
      theHash*=kBigPrime;
      theHash^=inInts[inIntDelta+i];
//      theHash+=hash(inInts[inIntDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNInts-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(int[] inInts) { return hash(inInts,0,inInts.length); }

  public static long hash(long[] inLongs, int inLongDelta, int inNLongs) {
    long theHash=inNLongs;
    boolean theIsLong=(inNLongs>16);
    for (int i=0; i<inNLongs; i++) {
      theHash*=kBigPrime;
      theHash^=inLongs[inLongDelta+i];
//      theHash+=hash(inLongs[inLongDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNLongs-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(long[] inLongs) { return hash(inLongs,0,inLongs.length); }

  public static long hash(float[] inFloats, int inFloatDelta, int inNFloats) {
    long theHash=inNFloats;
    boolean theIsLong=(inNFloats>16);
    for (int i=0; i<inNFloats; i++) {
      theHash*=kBigPrime;
      theHash^=hash(inFloats[inFloatDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNFloats-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(float[] inFloats) { return hash(inFloats,0,inFloats.length); }

  public static long hash(double[] inDoubles, int inDoubleDelta, int inNDoubles) {
    long theHash=inNDoubles;
    boolean theIsLong=(inNDoubles>16);
    for (int i=0; i<inNDoubles; i++) {
      theHash*=kBigPrime;
      theHash^=hash(inDoubles[inDoubleDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNDoubles-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(double[] inDoubles) { return hash(inDoubles,0,inDoubles.length); }

  public static long hash(Object inObject) { 
    if (inObject instanceof String) 
      return hash((String) inObject);
    else if (inObject.getClass().isArray()) {
      if (inObject instanceof byte[]) 
        return hash((byte[]) inObject);
      else if (inObject instanceof char[]) 
        return hash((char[]) inObject);
      else if (inObject instanceof int[]) 
        return hash((int[]) inObject);
      else if (inObject instanceof long[]) 
        return hash((long[]) inObject);
      else if (inObject instanceof double[]) 
        return hash((double[]) inObject);
      else if (inObject instanceof float[]) 
        return hash((float[]) inObject);
    }
    return inObject.hashCode(); 
  }

  public static long hash(Object[] inObjects, int inObjectDelta, int inNObjects) {
    long theHash=inNObjects;
    boolean theIsLong=(inNObjects>16);
    for (int i=0; i<inNObjects; i++) {
      theHash*=kBigPrime;
      theHash^=hash(inObjects[inObjectDelta+i]);
//      theHash^=(theHash>>>17);
      if (theIsLong)
        i+=Math.min(i,inNObjects-i)>>2;  
    }
    return (theHash&0x0000ffffffffffffL)|(theHash&0x0000ffffffffffffL>>>40); // 6 bytes
  }

  public static long hash(Object[] inObjects) { return hash(inObjects,0,inObjects.length); }

}
