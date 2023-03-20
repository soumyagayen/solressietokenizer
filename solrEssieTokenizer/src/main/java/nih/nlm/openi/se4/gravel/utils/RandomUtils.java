//--------------------------------------------------------------------------------------------------------
// RandomUtils.java
//
// RandomUtils is a static singleton, and not thread safe
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.util.*;
//import java.security.*;

//--------------------------------------------------------------------------------------------------------
// RandomUtils
//--------------------------------------------------------------------------------------------------------

public class RandomUtils {

//--------------------------------------------------------------------------------------------------------
// RandomGenerator constants
//--------------------------------------------------------------------------------------------------------

  private static final double  kNotAvailable=1.0e21;

//--------------------------------------------------------------------------------------------------------
// RandomUtils class vars
//--------------------------------------------------------------------------------------------------------

//  private static Random   gGenerator=new SecureRandom();  better but slower and does not support setSeed()
  private static Random   gRandomGenerator=new Random();
  private static double   gLastNormalDeviate=kNotAvailable;

//--------------------------------------------------------------------------------------------------------
// setSeed
//--------------------------------------------------------------------------------------------------------

  public static void setSeed(long inSeed) { 
    gRandomGenerator.setSeed(inSeed);
    gLastNormalDeviate=kNotAvailable;
  }

//--------------------------------------------------------------------------------------------------------
// randomDouble
//
// Returns x where x uniformly distributed in range  0.0 <= x < 1.0
//--------------------------------------------------------------------------------------------------------

  public static double randomDouble() { return gRandomGenerator.nextDouble(); }

//--------------------------------------------------------------------------------------------------------
// randomFloat
//
// Returns x where x uniformly distributed in range  0.0 <= x < 1.0
//--------------------------------------------------------------------------------------------------------

  public static float randomFloat() { return gRandomGenerator.nextFloat(); }

//--------------------------------------------------------------------------------------------------------
// randomLong
//--------------------------------------------------------------------------------------------------------

  public static long randomLong() { return gRandomGenerator.nextLong(); }

//--------------------------------------------------------------------------------------------------------
// randomInt
//--------------------------------------------------------------------------------------------------------

  public static int randomInt() { return gRandomGenerator.nextInt(); }

//--------------------------------------------------------------------------------------------------------
// randomByte
//--------------------------------------------------------------------------------------------------------

  public static byte randomByte() { return (byte) randomInt(); }

//--------------------------------------------------------------------------------------------------------
// chance
//--------------------------------------------------------------------------------------------------------

  public static boolean chance(double inChance) {
    if ((inChance<0.0)||(inChance>1.0))
      throw new IllegalArgumentException("Invalid chance: "+inChance);
    return randomDouble()<inChance;
  }

//--------------------------------------------------------------------------------------------------------
// randomIndex
//--------------------------------------------------------------------------------------------------------

  public static int randomIndex(int inNIndexes) {
    if (inNIndexes<0)
      throw new IllegalArgumentException("Negative NIndexes: "+inNIndexes);
    return (int) Math.floor(inNIndexes*randomDouble());
  }

  public static int randomIndex(int inLowerLimit, int inUpperLimit) {
    return inLowerLimit+randomIndex(inUpperLimit-inLowerLimit); }

  public static long randomIndex(long inNIndexes) {
    if (inNIndexes<0)
      throw new IllegalArgumentException("Negative NIndexes: "+inNIndexes);
    return (long) Math.floor(inNIndexes*randomDouble());
  }

  public static long randomIndex(long inLowerLimit, long inUpperLimit) {
    return inLowerLimit+randomIndex(inUpperLimit-inLowerLimit); }
  
//--------------------------------------------------------------------------------------------------------
// randomItem
//--------------------------------------------------------------------------------------------------------

  public static Object randomItem(Object[] inItems) {
    if ((inItems==null)||(inItems.length==0))
      return null;
    return inItems[randomIndex(inItems.length)];
  }

//--------------------------------------------------------------------------------------------------------
// dice
//--------------------------------------------------------------------------------------------------------

  public static int dice(int inNSides) {
    if (inNSides<0)
      throw new IllegalArgumentException("Negative NSides: "+inNSides);
    return randomIndex(inNSides)+1;
  }

  public static int dice(int inNSides, int inNDice) {
    int theSum=0;
    for (int i=0; i<inNDice; i++)
      theSum+=dice(inNSides);
    return theSum;
  }

//--------------------------------------------------------------------------------------------------------
// uniformDeviate
//--------------------------------------------------------------------------------------------------------

  public static double uniformDeviate() { return randomDouble(); }
  public static double uniformDeviate(double inUpperLimit) { return inUpperLimit*uniformDeviate(); }
  public static double uniformDeviate(double inLowerLimit, double inUpperLimit) {
    return inLowerLimit+(inUpperLimit-inLowerLimit)*uniformDeviate(); }

//--------------------------------------------------------------------------------------------------------
// poissonDeviate
//--------------------------------------------------------------------------------------------------------

  public static double poissonDeviate() { return -Math.log(1.0-randomDouble()); }
  public static double poissonDeviate(double inMean) { return inMean*poissonDeviate(); }

//--------------------------------------------------------------------------------------------------------
// normalDeviate
//--------------------------------------------------------------------------------------------------------

  public static double normalDeviate() {

    // Normal deviates are generated in pairs.  Check to see if the extra one from the last call
    // is available
    if (gLastNormalDeviate!=kNotAvailable) {
      double theLastNormalDeviate=gLastNormalDeviate;
      gLastNormalDeviate=kNotAvailable;      
      return theLastNormalDeviate;

    // Generate the next pair of normal deviates
    } else {
      double theU;
      double theV;
      double theS2;
      do {
        theU=2.0*randomDouble()-1.0;
        theV=2.0*randomDouble()-1.0;
        theS2=theU*theU+theV*theV;
      } while ((theS2>1.0)||(theS2==0.0));
      double theROvrS=Math.sqrt(-2.0*Math.log(theS2)/theS2);
      double theNormalDeviate=theV*theROvrS;
      gLastNormalDeviate=-theU*theROvrS;      
      return theNormalDeviate;
    }
  }
  
  public static double normalDeviate(double inMean, double inStdDev) { 
    return inMean+inStdDev*normalDeviate(); }

//--------------------------------------------------------------------------------------------------------
// randomBytes
//--------------------------------------------------------------------------------------------------------

  public static void randomBytes(byte[] inBytes, int inDelta, int inNBytes) {
    int n=0;
    while (n<inNBytes) {
      int theInt=randomInt();
      for (int i=0; i<4; i++) {
        inBytes[inDelta+n]=(byte) theInt;
        theInt>>=8;
        n++;
        if (n>=inNBytes)
          break;
      }
    }
  }

  public static void randomBytes(byte[] inBytes) { randomBytes(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// randomSequence
//--------------------------------------------------------------------------------------------------------

  public static int[] randomSequence(int inNIndexes) { 
    if (inNIndexes<0)
      throw new IllegalArgumentException("Negative NIndexes: "+inNIndexes);
    int[] theSeq=new int[inNIndexes];
    for (int i=0; i<inNIndexes; i++)
      theSeq[i]=i;
    for (int i=0; i<inNIndexes; i++) {
      int j=i+randomIndex(inNIndexes-i);
      int theInt=theSeq[i];
      theSeq[i]=theSeq[j];
      theSeq[j]=theInt;
    }
    return theSeq;
  }

//--------------------------------------------------------------------------------------------------------
// randomOrder
//--------------------------------------------------------------------------------------------------------

  public static void randomOrder(int[] inValues) { 
    for (int i=0; i<inValues.length; i++) {
      int j=i+randomIndex(inValues.length-i);
      int theHold=inValues[i];
      inValues[i]=inValues[j];
      inValues[j]=theHold;
    }
  }

  public static void randomOrder(long[] inValues) { 
    for (int i=0; i<inValues.length; i++) {
      int j=i+randomIndex(inValues.length-i);
      long theHold=inValues[i];
      inValues[i]=inValues[j];
      inValues[j]=theHold;
    }
  }

  public static void randomOrder(Object[] inObjects) { 
    for (int i=0; i<inObjects.length; i++) {
      int j=i+randomIndex(inObjects.length-i);
      Object theHold=inObjects[i];
      inObjects[i]=inObjects[j];
      inObjects[j]=theHold;
    }
  }

}



