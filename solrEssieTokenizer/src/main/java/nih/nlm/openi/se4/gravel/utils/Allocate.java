//--------------------------------------------------------------------------------------------------------
// Allocate.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// Allocate
//--------------------------------------------------------------------------------------------------------

public class Allocate implements Constants {

//--------------------------------------------------------------------------------------------------------
// Allocate constants
//--------------------------------------------------------------------------------------------------------

  public static final int     kCusionSizeM=16*k1M;

//--------------------------------------------------------------------------------------------------------
// Allocate class vars
//--------------------------------------------------------------------------------------------------------
  
  private static byte[]       gCushion=new byte[kCusionSizeM]; // Release this when OOM thrown so error escapes
  
//--------------------------------------------------------------------------------------------------------
// getCushionExists
//--------------------------------------------------------------------------------------------------------

  public static boolean getCushionExists() { return gCushion!=null; }
  
//--------------------------------------------------------------------------------------------------------
// newBytes
//--------------------------------------------------------------------------------------------------------

  public static byte[] newBytes(long inNBytes) {
    // Try several times
    for (int n=0; n<10; n++)
      try {
        return new byte[(int) inNBytes];
      } catch (OutOfMemoryError e) {
        // If out of memory, wait and try again
        if (n<9) {
          try { Thread.sleep(3*n+1); } catch (Exception e2) {}
        } else {
          // After several tries, release memory cushion, and throw exception
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNBytes)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newBooleans
//--------------------------------------------------------------------------------------------------------

  public static boolean[] newBooleans(long inNBooleans) {
    for (int n=0; n<5; n++)
      try {
        return new boolean[(int) inNBooleans];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNBooleans)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newShorts
//--------------------------------------------------------------------------------------------------------

  public static short[] newShorts(long inNShorts) {
    for (int n=0; n<5; n++)
      try {
        return new short[(int) inNShorts];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNShorts*kShortMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newChars
//--------------------------------------------------------------------------------------------------------

  public static char[] newChars(long inNChars) {
    for (int n=0; n<5; n++)
      try {
        return new char[(int) inNChars];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNChars*kCharMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newInts
//--------------------------------------------------------------------------------------------------------

  public static int[] newInts(long inNInts) {
    for (int n=0; n<5; n++)
      try {
        return new int[(int) inNInts];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNInts*kIntMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newFloats
//--------------------------------------------------------------------------------------------------------

  public static float[] newFloats(long inNFloats) {
    for (int n=0; n<5; n++)
      try {
        return new float[(int) inNFloats];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNFloats*kFloatMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newLongs
//--------------------------------------------------------------------------------------------------------

  public static long[] newLongs(long inNLongs) {
    for (int n=0; n<5; n++)
      try {
        return new long[(int) inNLongs];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNLongs*kLongMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newDoubles
//--------------------------------------------------------------------------------------------------------

  public static double[] newDoubles(long inNDoubles) {
    for (int n=0; n<5; n++)
      try {
        return new double[(int) inNDoubles];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNDoubles*kDoubleMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// newObjects
//--------------------------------------------------------------------------------------------------------

  public static Object[] newObjects(long inNObjects) {
    for (int n=0; n<5; n++)
      try {
        return new Object[(int) inNObjects];
      } catch (OutOfMemoryError e) {
        if (n<4) {
          try { Thread.sleep(5*n+1); } catch (Exception e2) {}
        } else {
          gCushion=null;
          throw new RuntimeException("Out of memory ("+FormatUtils.formatMemory(inNObjects*kObjectMemory)+
              "requested, "+FormatUtils.formatMemory(FormatUtils.getUsedMemory())+" in use)",e);
        }
      }

    return null; // never get here
  }

//--------------------------------------------------------------------------------------------------------
// getMemory routines
//--------------------------------------------------------------------------------------------------------

  public static final long getArrayMemory(byte[] inBytes) {
    return (inBytes==null)? 0: kArrayMemory+((inBytes.length+7)/8)*8; }

  public static final long getArrayMemory(boolean[] inBooleans) {
    return (inBooleans==null)? 0: kArrayMemory+((inBooleans.length+7)/8)*8; }

  public static final long getArrayMemory(short[] inShorts) {
    return (inShorts==null)? 0: kArrayMemory+((inShorts.length+3)/4)*8; }

  public static final long getArrayMemory(char[] inChars) {
    return (inChars==null)? 0: kArrayMemory+((inChars.length+3)/4)*8; }

  public static final long getArrayMemory(int[] inInts) {
    return (inInts==null)? 0: kArrayMemory+((inInts.length+1)/2)*8; }

  public static final long getArrayMemory(float[] inFloats) {
    return (inFloats==null)? 0: kArrayMemory+((inFloats.length+1)/2)*8; }

  public static final long getArrayMemory(long[] inLongs) {
    return (inLongs==null)? 0: kArrayMemory+inLongs.length*8; }

  public static final long getArrayMemory(double[] inDoubles) {
    return (inDoubles==null)? 0: kArrayMemory+inDoubles.length*8; }

  public static final long getArrayMemory(Object[] inObjects) {
    return (inObjects==null)? 0: kArrayMemory+inObjects.length*kReferenceMemory; }

  public static long getStringMemory(String inString) {
    return (inString==null)? 0: kStringMemory+((inString.length()+3)/4)*8; }

  public static long getObjectMemory(long inMemberTotal) {
    return kObjectMemory+((inMemberTotal+7)/8)*8; }

}

