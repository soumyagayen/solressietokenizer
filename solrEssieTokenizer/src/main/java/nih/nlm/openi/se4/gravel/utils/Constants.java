//--------------------------------------------------------------------------------------------------------
// Constants.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------------------------------------

public interface Constants {

//--------------------------------------------------------------------------------------------------------
// Constants constants
//--------------------------------------------------------------------------------------------------------

  // Used Everywhere, but especially as a return from hash lookups
  public static final int        kNotFound=-1;
  public static final int        kNotSet=-2;  

  public static final String     kUnknown="Unknown";
  public static final String     kTrue="true";
  public static final String     kFalse="false";
  public static final String     kYes="Yes";
  public static final String     kNo="No";
  public static final String     kUndecided="Undecided";

  // Memory sizes
  public static final int        k1K=1024;
  public static final int        k1M=k1K*k1K;
  public static final long       k1G=k1K*k1M;
  public static final long       k1T=k1K*k1G;
  public static final long       k1P=k1K*k1T;
  public static final long       k1X=k1K*k1P;

  // Times in ms
  public static final long       k1Sec=1000L;
  public static final long       k1Min=60L*k1Sec;
  public static final long       k1Hour=60L*k1Min;
  public static final long       k1Day=24L*k1Hour;
  public static final long       k1Week=7L*k1Day;
  public static final long       k1Year=Math.round(365.242*k1Day);

  // Primitive data element sizes
  public static final int        kByteMemory=1;
  public static final int        kBooleanMemory=1;
  public static final int        kCharMemory=2;
  public static final int        kShortMemory=2;
  public static final int        kIntMemory=4;
  public static final int        kFloatMemory=4;
  public static final int        kLongMemory=8;
  public static final int        kDoubleMemory=8;
  public static final int        kReferenceMemory=8;
  public static final int        kObjectMemory=16;
  public static final int        kArrayMemory=24;
  public static final int        kStringMemory=64;

  // Use these instead of instantiating empty arrays everywhere
  public static final byte[]     kNoBytes=new byte[0];
  public static final boolean[]  kNoBooleans=new boolean[0];
  public static final char[]     kNoChars=new char[0];
  public static final short[]    kNoShorts=new short[0];
  public static final int[]      kNoInts=new int[0];
  public static final float[]    kNoFloats=new float[0];
  public static final long[]     kNoLongs=new long[0];
  public static final double[]   kNoDoubles=new double[0];
  public static final String[]   kNoStrings=new String[0];
  public static final Object[]   kNoObjects=new Object[0];

  public static final char       kBadChar='\ufffd';
  public static final String     kEOL="\n";
  public static final String     kPCEOL="\r\n";
  public static final String     kEmptyString="";
  
  public static final String     kDigitChars="0123456789";
  public static final String     kDecimalChars=kDigitChars+"+-.eE";
  public static final String     kUpperCaseLetterChars="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static final String     kLowerCaseLetterChars="abcdefghijklmnopqrstuvwxyz";
  public static final String     kLetterChars=kLowerCaseLetterChars+kUpperCaseLetterChars;
  public static final String     kAlphaNumericChars=kLetterChars+kDigitChars;

  // Math
  public static final double     kEpsilon=1.0e-8;  // 19 digit precisison in doubles
  public static final double     kEpsilon2=kEpsilon*kEpsilon;

  public static final double     kLn2=Math.log(2.0);
  public static final double     kLn10=Math.log(10.0);
  public static final double     kExp1=Math.exp(1.0);
  
  public static final int        kSmallJob=0;      // Uses < 1% RAM - No memory check
  public static final int        kMediumJob=1;     // May use up to ~10% RAM - Quick memory check
  public static final int        kLargeJob=2;      // May use more than ~10% RAM - Extensive memory check

  public static final int        kPriorityNotSet=kNotFound; 
  public static final int        kNormalPriority=5; 
  public static final int        kLowPriority=4;
  public static final int        kLowerPriority=3;
  public static final int        kLowestPriority=2; 

}

