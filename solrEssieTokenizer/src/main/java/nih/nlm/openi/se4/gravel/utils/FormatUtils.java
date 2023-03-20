//--------------------------------------------------------------------------------------------------------
// FormatUtils.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.io.*;
import java.text.*;
import java.util.*;

//--------------------------------------------------------------------------------------------------------
// FormatUtils
//--------------------------------------------------------------------------------------------------------

public class FormatUtils implements Constants {

//--------------------------------------------------------------------------------------------------------
// FormatUtils constants
//--------------------------------------------------------------------------------------------------------
  
  private static final Runtime            kRuntime=Runtime.getRuntime();
  private static final SimpleDateFormat   kStdTimeFormat=new SimpleDateFormat("yyyy:MM:dd HH:mm:ss.SSS");
  
  public static final int      kNCores=kRuntime.availableProcessors();
  public static final long     kHeapMemory=kRuntime.maxMemory();

  public static final String   kNoDifferences="No Differences";

  public static final String   kBlanks=repeatChar(80,' ');
  public static final String   kZeros=repeatChar(40,'0');
  public static final String   kDivider="//"+repeatChar(104,'-');
  
//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public static long getHeapMemory() { return kHeapMemory; }
  public static long getUsedMemory() { return kRuntime.totalMemory()-kRuntime.freeMemory(); }
  public static int getNCores() { return kNCores; }

//--------------------------------------------------------------------------------------------------------
// repeatChar
//
// Creates a string of N chars
//--------------------------------------------------------------------------------------------------------

  public static String repeatChar(int inWidth, char inChar) {
    if (inWidth<0)
      return "";
    else {
      StringBuffer theStringBuffer=new StringBuffer(inWidth);
      for (int i=0; i<inWidth; i++)
        theStringBuffer.append(inChar);
      return theStringBuffer.toString();
    }
  }

//--------------------------------------------------------------------------------------------------------
// blanks
//
// Creates a string of N blanks
//--------------------------------------------------------------------------------------------------------

  public static String blanks(int inWidth) {
    if (inWidth<0)
      return "";
    else if (inWidth<kBlanks.length())
      return kBlanks.substring(0,inWidth);
    else 
      return repeatChar(inWidth,' ');
  }

//--------------------------------------------------------------------------------------------------------
// zeros
//--------------------------------------------------------------------------------------------------------

  public static String zeros(int inWidth) {
    if (inWidth<0)
      return "";
    else if (inWidth<kZeros.length())
      return kZeros.substring(0,inWidth);
    else 
      return repeatChar(inWidth,'0');
  }

//--------------------------------------------------------------------------------------------------------
// isControlChar
//--------------------------------------------------------------------------------------------------------

  public static boolean isControlChar(char inChar) {
    return ((inChar<' ')||((inChar>='\u007f')&&(inChar<='\u009f'))); }

//--------------------------------------------------------------------------------------------------------
// isNonBreakingSpace (spaces that are not blanks)
//--------------------------------------------------------------------------------------------------------

  public static boolean isNonBreakingSpace(char inChar) {
    return ((inChar=='\u00a0')||
            ((inChar>'\u00ff')&&(
                  (inChar=='\u1680')||(inChar=='\u180e')||
                  ((inChar>='\u2000')&&(inChar<='\u200c'))||
                  (inChar=='\u2028')||(inChar=='\u2029')|| 
                  (inChar=='\u202f')||(inChar=='\u205f')||
                  (inChar=='\u2060')||(inChar=='\u3000'))));
  }

//--------------------------------------------------------------------------------------------------------
// isWhitespace (includes control chars)
//--------------------------------------------------------------------------------------------------------

  public static boolean isWhitespace(char inChar) {
    return ((inChar==' ')||isControlChar(inChar)||isNonBreakingSpace(inChar)); }

//--------------------------------------------------------------------------------------------------------
// hasRealContent
//--------------------------------------------------------------------------------------------------------

  public static boolean hasRealContent(String inString) {
    if (inString==null)
      return false;
    for (int i=0; i<inString.length(); i++) {
      char theChar=inString.charAt(i);
      if (!isWhitespace(theChar))
        return true;
    }
    return false;
  }

//--------------------------------------------------------------------------------------------------------
// squeezeWhitespace
//
// Replaces all runs of whitespace with a single blank - no trim
//--------------------------------------------------------------------------------------------------------

  public static String squeezeWhitespace(String inString) {
    if (inString==null)
      return inString;
    boolean inRun=false;
    StringBuffer theStringBuffer=new StringBuffer(inString.length());
    for (int i=0; i<inString.length(); i++) {
      char theChar=inString.charAt(i);
      if (!isWhitespace(theChar)) {
        theStringBuffer.append(theChar);
        inRun=false;
      } else if (!inRun) {
        theStringBuffer.append(' ');
        inRun=true;
      }
    }
    return theStringBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// leftPad
//
// Adds blanks to the left of a string to increase its length to the given width
//--------------------------------------------------------------------------------------------------------

  public static String leftPad(String inString, int inWidth) {
    if (inString==null)
      return blanks(inWidth);
    else
      return blanks(inWidth-inString.length())+inString;
  }

//--------------------------------------------------------------------------------------------------------
// leftPad
//
// Adds blanks to the left of a byte, int, or long to increase its length to the given width
//--------------------------------------------------------------------------------------------------------

  public static String leftPad(long inNumber, int inWidth) {
    return leftPad(String.valueOf(inNumber),inWidth); }

//--------------------------------------------------------------------------------------------------------
// leftPad
//
// Adds blanks to the left of a double to increase its length to the given width
//--------------------------------------------------------------------------------------------------------

  public static String leftPad(double inNumber, int inNDecimals, int inWidth) {
    return leftPad(FormatUtils.formatDouble(inNumber,inNDecimals),inWidth); }

  public static String leftPad(double inNumber, int inWidth) {
    return leftPad(FormatUtils.formatDouble(inNumber),inWidth); }

//--------------------------------------------------------------------------------------------------------
// rightPad
//
// Adds blanks to the right of a string to increase its length to the given width
//--------------------------------------------------------------------------------------------------------

  public static String rightPad(String inString, int inWidth) {
    if (inString==null)
      return blanks(inWidth);
    else
      return inString+blanks(inWidth-inString.length()); 
  }

//--------------------------------------------------------------------------------------------------------
// rightPad
//
// Adds blanks to the right of a byte, int, or long to increase its length to the given width
//--------------------------------------------------------------------------------------------------------

  public static String rightPad(long inNumber, int inWidth) {
    return rightPad(String.valueOf(inNumber),inWidth); }

//--------------------------------------------------------------------------------------------------------
// rightPad
//
// Adds blanks to the right of a double to increase its length to the given width
//--------------------------------------------------------------------------------------------------------

  public static String rightPad(double inNumber, int inNDecimals, int inWidth) {
    return rightPad(FormatUtils.formatDouble(inNumber,inNDecimals),inWidth); }

  public static String rightPad(double inNumber, int inWidth) {
    return rightPad(FormatUtils.formatDouble(inNumber),inWidth); }

//--------------------------------------------------------------------------------------------------------
// cut
//
// Truncates a string and includes elipses "..." if necessary
//--------------------------------------------------------------------------------------------------------

  public static String cut(String inString, int inWidth) {
    if (inString==null)
      return inString;
    if (inString.length()<=inWidth)
      return inString;
    else
      return inString.substring(0,Math.max(0,inWidth-3))+"...";
  }

//--------------------------------------------------------------------------------------------------------
// formatDouble
//
// Fixed point - inNDecimals decimal places
//--------------------------------------------------------------------------------------------------------
  
  public static final double[]  kPositivePowersOf10=new double[] {
          1.0,                 // 0
          10.0,                // 1
          100.0,               // 2
          1000.0,              // 3
          10000.0,             // 4
          100000.0,            // 5
          1000000.0,           // 6
          10000000.0,          // 7
          100000000.0,         // 8
          1000000000.0,        // 9
          10000000000.0,       // 10
  };

  public static final double[]  kNegativePowersOf10=new double[] {
          1.0,                 // 0
          0.1,                 // -1
          0.01,                // -2
          0.001,               // -3
          0.0001,              // -4
          0.00001,             // -5
          0.000001,            // -6
          0.0000001,           // -7
          0.00000001,          // -8
          0.000000001,         // -9
          0.0000000001,        // -10
  };

  public static String formatDouble(double inNumber, int inNDecimals) {
    if (inNumber==Double.POSITIVE_INFINITY)
      return "infinite";
    else if (inNumber==Double.NEGATIVE_INFINITY)
      return "-infinite";
    else if (Double.isNaN(inNumber))
      return "NaN";
    else {
      if (inNDecimals==0)
        return String.valueOf(Math.round(inNumber));
      double theTens=1;
      if (inNDecimals>0) 
        theTens=kPositivePowersOf10[Math.min(inNDecimals,kPositivePowersOf10.length-1)];
      else if (inNDecimals<0) 
        theTens=kNegativePowersOf10[Math.min(inNDecimals,kNegativePowersOf10.length-1)];
      long theRoundedNumber=Math.round(Math.abs(inNumber)*theTens);
      if (theRoundedNumber==0)
        return "0."+FormatUtils.zeros(inNDecimals);
      String theString=String.valueOf(theRoundedNumber);
      if (inNDecimals<0) {
        if (theRoundedNumber!=0.0)
          theString+=FormatUtils.zeros(-inNDecimals);
      } else {
        int theLeadingDigits=theString.length()-inNDecimals;
        if (theLeadingDigits>0)
          theString=theString.substring(0,theLeadingDigits)+"."+theString.substring(theLeadingDigits);
        else if (theLeadingDigits==0)
          theString="0."+theString;
        else
          theString="0."+FormatUtils.zeros(-theLeadingDigits)+theString;
      }
      if (inNumber>0.0)
        return theString;
      else
        return "-"+theString;
    }
  }

  public static String formatDouble(double inNumber) { return formatDouble(inNumber,2); }

//--------------------------------------------------------------------------------------------------------
// formatFloat
//--------------------------------------------------------------------------------------------------------

  public static String formatFloat(float inNumber, int inNDecimals) { 
    return formatDouble(inNumber,inNDecimals); }

  public static String formatFloat(float inNumber) { return formatFloat(inNumber,2); }

//--------------------------------------------------------------------------------------------------------
// formatTime
//--------------------------------------------------------------------------------------------------------

  public static String formatTime(long inTime) {
    if (inTime==kNotFound)
      return "";
    synchronized(kStdTimeFormat) {
      return kStdTimeFormat.format(new Date(inTime));
    }
  }

//--------------------------------------------------------------------------------------------------------
// breakOnChars
//
// Breaks a string on inChar
// Pieces do not contain inChars - they are all dropped
// Pieces can be zero length (unlike StringTokenizer)
//--------------------------------------------------------------------------------------------------------

  public static String[] breakOnChars(String inString, char inChar) {

    if ((inString==null)||(inString.length()==0))
      return kNoStrings;

    int theNTokens=1;
    int thePos=inString.indexOf(inChar);
    while (thePos>=0) {
      theNTokens++;
      thePos=inString.indexOf(inChar,thePos+1);
    }

    String[] theTokens=new String[theNTokens];

    theNTokens=0;
    int theLastPos=0;
    thePos=inString.indexOf(inChar);
    while (thePos>=0) {
      theTokens[theNTokens]=inString.substring(theLastPos,thePos);
      theNTokens++;
      theLastPos=thePos+1;
      thePos=inString.indexOf(inChar,theLastPos);
    }
    theTokens[theNTokens]=inString.substring(theLastPos);

    return theTokens;
  }

//--------------------------------------------------------------------------------------------------------
// join
//--------------------------------------------------------------------------------------------------------

  public static String join(String[] inStrings, int inStringDelta, int inNStrings, String inSeparator) {
    if ((inStrings==null)||(inNStrings<=0))
      return "";
    if (inNStrings==1)
      return inStrings[inStringDelta];

    StringBuffer theBuffer=new StringBuffer();
    theBuffer.append(inStrings[inStringDelta]);
    for (int i=1; i<inNStrings; i++) {
      theBuffer.append(inSeparator);
      theBuffer.append(inStrings[inStringDelta+i]);
    }
    return theBuffer.toString();
  }

  public static String join(String[] inStrings, String inSeparator) {
    if (inStrings==null)
      return "";
    else
      return join(inStrings,0,inStrings.length,inSeparator); 
  }

//--------------------------------------------------------------------------------------------------------
// formatMemory
//--------------------------------------------------------------------------------------------------------

  public static String formatMemory(long inMemory) {
    if (inMemory==kNotFound)
      return "";
    String theMemoryStr=null;
    long theMemory=Math.abs(inMemory);
    if (theMemory<5000)
      theMemoryStr=theMemory+"B ";
    else {
      double theRealMemory=theMemory/1024.0;
      if (theRealMemory<999.995)
        theMemoryStr=formatDouble(theRealMemory)+"KB";
      else {
        theRealMemory/=1024.0;
        if (theRealMemory<999.995)
          theMemoryStr=formatDouble(theRealMemory)+"MB";
        else {
          theRealMemory/=1024.0;
          if (theRealMemory<999.995)
            theMemoryStr=formatDouble(theRealMemory)+"GB";
          else {
            theRealMemory/=1024.0;
            if (theRealMemory<999.995)
              theMemoryStr=formatDouble(theRealMemory)+"TB";
            else {
              theRealMemory/=1024.0;
              if (theRealMemory<999.995)
                theMemoryStr=formatDouble(theRealMemory)+"PB";
              else {
                theRealMemory/=1024.0;
                theMemoryStr=formatDouble(theRealMemory)+"XB";
              }
            }
          }
        }
      }
    }
    if (inMemory>=0)
      return theMemoryStr;
    else
      return '-'+theMemoryStr;
  }

//--------------------------------------------------------------------------------------------------------
// formatDuration
//
// Converts a duration in millis to an english string
//--------------------------------------------------------------------------------------------------------

  public static String formatDuration(double inDuration) {
    if (inDuration==kNotFound)
      return "";
    String theTimeStr=null;
    double theDuration=Math.abs(inDuration);
    if ((theDuration<100)&&(theDuration!=Math.round(theDuration)))
      theTimeStr=formatDouble(theDuration)+"ms";
    else if (theDuration<9999.5)
      theTimeStr=Math.round(theDuration)+"ms";
    else {
      double theTime=theDuration/1000.0;
      if (theTime<99.995)
        theTimeStr=formatDouble(theTime)+"secs";
      else {
        theTime/=60.0;
        if (theTime<99.995)
          theTimeStr=formatDouble(theTime)+"mins";
        else {
          theTime/=60.0;
          if (theTime<99.995)
            theTimeStr=formatDouble(theTime)+"hrs";
          else {
            theTime/=24.0;
            if (theTime<99.995)
              theTimeStr=formatDouble(theTime)+"days";
            else {
              theTime/=7.0;
              if (theTime<99.995)
                theTimeStr=formatDouble(theTime)+"weeks";
              else {
                theTime*=7.0/365.0;
                theTimeStr=formatDouble(theTime)+"years";
              }
            }
          }
        }
      }
    }
    if (inDuration<0.0)
      theTimeStr="-"+theTimeStr;
    return theTimeStr;
  }

//--------------------------------------------------------------------------------------------------------
// formatPercent
//--------------------------------------------------------------------------------------------------------

  public static String formatPercent(double inFraction, int inNDecimals) {
    if (inFraction==kNotFound)
      return "";
    else
      return formatDouble(inFraction*100.0,inNDecimals)+"%"; 
  }

  public static String formatPercent(double inFraction) { 
    return formatPercent(inFraction,2); }

  public static String formatPercent(long inTop, long inBot) { 
    if (inBot==0)
      return "0.00%";
    else
      return formatPercent(inTop/(double) inBot,2); 
  }

  public static String formatPercent(int inTop, int inBot) { 
    return formatPercent((long) inTop, (long) inBot); }

//--------------------------------------------------------------------------------------------------------
// formatException
//--------------------------------------------------------------------------------------------------------

  public static String formatException(String inMessage, Throwable inThrowable) {
    StringWriter theBuffer=new StringWriter();
    PrintWriter theWriter=new PrintWriter(theBuffer);
    if (inMessage!=null)
      theWriter.println(inMessage);
    inThrowable.printStackTrace(theWriter);
    return theBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// reportBanner
//--------------------------------------------------------------------------------------------------------

  public static String reportBanner(String[] inTitles) {
    StringBuffer theBuffer=new StringBuffer(4096);
    theBuffer.append("\n"+kDivider+"\n");
    for (int i=0; i<inTitles.length; i++)
      theBuffer.append("// "+inTitles[i]+"\n");
    theBuffer.append(kDivider+"\n");
    return theBuffer.toString();
  }

  public static String reportBanner(String inTitle) {
    return reportBanner(new String[] {inTitle}); }
  
//--------------------------------------------------------------------------------------------------------
// reportHeader
//--------------------------------------------------------------------------------------------------------

  public static String reportHeader(String inTitle, long inStartTime) {
    return reportBanner(new String[] {inTitle,"Starting at "+formatTime(inStartTime)})+"\n\n"; }
  
//--------------------------------------------------------------------------------------------------------
// reportFooter
//--------------------------------------------------------------------------------------------------------

  public static String reportFooter(long inStartTime) {
    long theEndTime=System.currentTimeMillis();
    return reportBanner(new String[] {"Done at "+formatTime(theEndTime)+
        "      "+formatDuration(theEndTime-inStartTime)+" elapsed"});
  }
  
}
