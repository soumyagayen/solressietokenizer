//--------------------------------------------------------------------------------------------------------
// CharNorm.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import gravel.store.data.*;
import gravel.store.plain.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// CharNorm
//--------------------------------------------------------------------------------------------------------

public class CharNorm implements Constants {

//--------------------------------------------------------------------------------------------------------
// CharNorm constants
//--------------------------------------------------------------------------------------------------------

  public static final int        kEmptyStringFlag=1;
  public static final int        kSingleCharFlag=2;
  public static final int        kMultiCharFlag=4;
  public static final int        kOverrideFlag=8;
  public static final int        kWhitespaceFlag=16;
  public static final int        kLetterFlag=32;
  public static final int        kDigitFlag=64;
  public static final int        kUpperCaseFlag=128;

  private static final Object    kLoadLock=new Object();  // Used as sync lock, so keep private

//--------------------------------------------------------------------------------------------------------
// CharNorm class member vars
//--------------------------------------------------------------------------------------------------------

  public static String            gNormDir;
  
  public static VarRAMStore       gMapChars;
  public static ByteRAMStore      gCharFlags;
  public static ByteDataRAMStore  gMultiChars;

//--------------------------------------------------------------------------------------------------------
// getNormDir
//--------------------------------------------------------------------------------------------------------

  public static String getNormDir() { return gNormDir; }

//--------------------------------------------------------------------------------------------------------
// isLoaded
//--------------------------------------------------------------------------------------------------------

  public static boolean isLoaded() { return (gMapChars!=null); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static void load(String inNormDir) throws Exception {
    // Should always be called in single threaded section, but protect just in case
    synchronized(kLoadLock) {
      if (!isLoaded()) {
        gNormDir=inNormDir;
        gMapChars=VarRAMStore.load(inNormDir+"/MapChars.dat");
        gCharFlags=ByteRAMStore.load(inNormDir+"/CharFlags.dat");
        gMultiChars=ByteDataRAMStore.load(inNormDir+"/MultiChars.dat");
      }
    }
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
    if (gCharFlags==null)
      return ((inChar==' ')||isControlChar(inChar)||isNonBreakingSpace(inChar)); 
    else
      return ((gCharFlags.getByte(inChar)&kWhitespaceFlag)!=0); 
  }
 
//--------------------------------------------------------------------------------------------------------
// isLetter
//--------------------------------------------------------------------------------------------------------

  public static boolean isLetter(char inChar) { 
    if (gCharFlags==null)
      return Character.isLetter(inChar);
    else
      return ((gCharFlags.getByte(inChar)&kLetterFlag)!=0); 
  }

//--------------------------------------------------------------------------------------------------------
// isUpperCaseLetter
//--------------------------------------------------------------------------------------------------------

  public static boolean isUpperCaseLetter(char inChar) { 
    if (gCharFlags==null)
      return ((Character.isLetter(inChar))&&(Character.isUpperCase(inChar)));
    else
      return ((isLetter(inChar))&&((gCharFlags.getByte(inChar)&kUpperCaseFlag)!=0)); 
  }

//--------------------------------------------------------------------------------------------------------
// isLowerCaseLetter
//--------------------------------------------------------------------------------------------------------

  public static boolean isLowerCaseLetter(char inChar) { 
    if (gCharFlags==null)
      return ((Character.isLetter(inChar))&&(Character.isLowerCase(inChar)));
    else
      return ((isLetter(inChar))&&((gCharFlags.getByte(inChar)&kUpperCaseFlag)==0)); 
  }

//--------------------------------------------------------------------------------------------------------
// isDigit
//--------------------------------------------------------------------------------------------------------

  public static boolean isDigit(char inChar) { 
    if (gCharFlags==null)
      return (Character.isDigit(inChar));
    else
      return ((gCharFlags.getByte(inChar)&kDigitFlag)!=0); 
  }

//--------------------------------------------------------------------------------------------------------
// isPunctuation
//
// Includes everything that is not whitespace, letters, or digits
//--------------------------------------------------------------------------------------------------------

  public static boolean isPunctuation(char inChar) {
    return ((!isWhitespace(inChar))&&(!isLetter(inChar))&&(!isDigit(inChar))); }

//--------------------------------------------------------------------------------------------------------
// isUnprintableChar (chars that norm to nothing)
//   including trademark, copyright, registered, zero width space, left to right mark, right to left mark
//--------------------------------------------------------------------------------------------------------

  public static boolean isUnprintableChar(char inChar) {
    return ((inChar=='\u0099')||(inChar=='\u00a9')||
            (inChar=='\u00ae')||
            ((inChar>'\u00ff')&&(
                  (inChar=='\u200d')||(inChar=='\u200e')||
                  (inChar=='\u200f')||(inChar=='\u2122')|| 
                  (inChar=='\ufeff'))));
  }

//--------------------------------------------------------------------------------------------------------
// isQuote
//--------------------------------------------------------------------------------------------------------

  public static boolean isQuote(char inChar) {
    return ((inChar=='"')||(inChar=='\u0084')||
        (inChar=='\u0093')||(inChar=='\u0094')||  // Smart quotes in wrong encoding
        (inChar=='\u00ab')||(inChar=='\u00bb')||  
        (inChar=='\u02ba')||
        (inChar>'\u2000')&&(
          (inChar=='\u201c')||(inChar=='\u201d')||
          (inChar=='\u201e')||(inChar=='\u201f')||
          (inChar=='\u2033')||(inChar=='\u2036')||
          (inChar=='\uff02'))); }

//--------------------------------------------------------------------------------------------------------
// isApostrophe
//--------------------------------------------------------------------------------------------------------

  public static boolean isApostrophe(char inChar) {
    return ((inChar=='\'')||(inChar=='`')||
        (inChar=='\u0082')||(inChar=='\u0091')||
        (inChar=='\u0092')||(inChar=='\u02b9')||
        (inChar=='\u02bb')||(inChar=='\u02bc')||
        (inChar=='\u02bd')||
        (inChar>'\u2000')&&(
          (inChar=='\u2018')||(inChar=='\u2019')||
          (inChar=='\u201a')||(inChar=='\u201b')||
          (inChar=='\u2032')||(inChar=='\u2035')||
          (inChar=='\uff07')||(inChar=='\uff40'))); }

//--------------------------------------------------------------------------------------------------------
// isHyphen
//--------------------------------------------------------------------------------------------------------

  public static boolean isHyphen(char inChar) {
    return ((inChar=='-')||
        (inChar=='\u0096')||(inChar=='\u0097')||
        (inChar=='\u00ad')||(inChar=='\u058a')||
        (inChar=='\u05be')||(inChar=='\u1400')||
        (inChar=='\u1806')||
        (inChar>'\u2000')&&(
          (inChar=='\u2010')||(inChar=='\u2011')||
          (inChar=='\u2012')||(inChar=='\u2013')||
          (inChar=='\u2014')||(inChar=='\u2015')||
          (inChar=='\u203e')||(inChar=='\u2053')||
          (inChar=='\u207b')||(inChar=='\u208b')||
          (inChar=='\u2212')||(inChar=='\u2500')||
          (inChar=='\u2501')||(inChar=='\u2e17')||
          (inChar=='\u2e1a')||(inChar=='\u2e3a')||
          (inChar=='\u2e3b')||(inChar=='\u2e40')||
          (inChar=='\uff0d'))); }

//--------------------------------------------------------------------------------------------------------
// isBullet
//--------------------------------------------------------------------------------------------------------

  public static boolean isBullet(char inChar) {
    return ((inChar=='*')||
        (inChar=='\u0095')||(inChar=='\u00b7')||
        (inChar>'\u2000')&&(
            (inChar=='\u2022')||(inChar=='\u2027')||
            (inChar=='\u2218')||(inChar=='\u2219')||
            (inChar=='\u22c5')||(inChar=='\u24cf')||
            (inChar=='\u25cb')||(inChar=='\u25e6'))); }

//--------------------------------------------------------------------------------------------------------
// isBracket
//--------------------------------------------------------------------------------------------------------

  public static boolean isBracket(char inChar) { 
    return ((inChar=='(')||(inChar==')')||
        (inChar=='[')||(inChar==']')||
        (inChar=='{')||(inChar=='}')||
        (inChar>'\u2000')&&(
            (inChar=='\u2308')||(inChar=='\u2309')||
            (inChar=='\u230a')||(inChar=='\u230b')||
            (inChar>'\uff00')&&(
                (inChar=='\uff08')||(inChar=='\uff09')||
                (inChar=='\uff3b')||(inChar=='\uff3d')||
                (inChar=='\uff5b')||(inChar=='\uff5d')))); }

//--------------------------------------------------------------------------------------------------------
// isLessThan
//--------------------------------------------------------------------------------------------------------

  public static boolean isLessThan(char inChar) {
    return ((inChar=='<')||
        (inChar>'\u2000')&&(
            (inChar=='\u2039')||(inChar=='\u3008')||
            (inChar=='\uff1c'))); }

//--------------------------------------------------------------------------------------------------------
// isGreaterThan
//--------------------------------------------------------------------------------------------------------

  public static boolean isGreaterThan(char inChar) {
    return ((inChar=='>')||
        (inChar>'\u2000')&&(
            (inChar=='\u203a')||(inChar=='\u27a2')||
            (inChar=='\u3009')||(inChar=='\uff1e'))); }
 
//--------------------------------------------------------------------------------------------------------
// toUpperCase
//--------------------------------------------------------------------------------------------------------

  public static char toUpperCase(char inChar) { 
    if (!isLowerCaseLetter(inChar))
      return inChar; 
    else
      return Character.toUpperCase(inChar);
  }

//--------------------------------------------------------------------------------------------------------
// isLowerCase
//--------------------------------------------------------------------------------------------------------

  public static char toLowerCase(char inChar) { 
    if (!isUpperCaseLetter(inChar))
      return inChar; 
    else
      return Character.toLowerCase(inChar);
  }

//--------------------------------------------------------------------------------------------------------
// countUTF8BytesInNextChar
//--------------------------------------------------------------------------------------------------------

  public static int countUTF8BytesInNextChar(byte[] inBytes, int inByteDelta) {
    int theByteDelta=inByteDelta;
    byte theByte=inBytes[theByteDelta++];
    if ((theByte&0x0080)==0)
      return 1;
    else if ((theByte&0x0040)==0) {
      // Started in middle of UTF8 char
    } else if ((theByte&0x0020)==0)
      return 2;
    else if ((theByte&0x0010)==0)
      return 3;
    else {
      // Invalid UTF8 or char that doesn't fit in a java char
    }
    
    // Bad char - return count of bad bytes
    while (inByteDelta<inBytes.length) {
      theByte=inBytes[theByteDelta++];
      if (((theByte&0x00f0)!=0x00f0)&&   // Start of a bad char
          ((theByte&0x00c0)!=0x00c0))    // Continuation char
        break;
    }
    return theByteDelta-inByteDelta;
  }

//--------------------------------------------------------------------------------------------------------
// countUTF8Chars
//--------------------------------------------------------------------------------------------------------

  public static int countUTF8Chars(byte[] inBytes, int inByteDelta, int inNBytes) {
    int theNChars=0;
    int n=inByteDelta;
    int theEndByte=inByteDelta+inNBytes;
    while (n<theEndByte) {
      n+=countUTF8BytesInNextChar(inBytes,n);
      theNChars++;
    }
    // Last multi-byte char may be truncated.  If so, don't count it.
    if (n>theEndByte)
      theNChars--;
    return theNChars;
  }

  public static int countUTF8Chars(byte[] inBytes) {
    return countUTF8Chars(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// bytesToUTF8Chars
//--------------------------------------------------------------------------------------------------------

  public static int bytesToUTF8Chars(byte[] inBytes, int inByteDelta, int inNBytes, 
      char[] ioChars, int inCharDelta) {
    int n=inCharDelta;
    int m=inByteDelta;
    int theEndByte=inByteDelta+inNBytes;
    while (m<theEndByte) {
      byte theByte=inBytes[m++];
      if ((theByte&0x0080)==0) 
        ioChars[n++]=(char) theByte;
      else if ((theByte&0x0040)==0) { 
        // Started in middle of UTF8 char
        if ((n==inCharDelta)||(ioChars[n-1]!=kBadChar))
          ioChars[n++]=kBadChar;     // Add bad char marker if doesn't already exist
      } else if ((theByte&0x0020)==0) 
        ioChars[n++]=(char) (((theByte&0x001f)<<6)|
                              (inBytes[m++]&0x003f));
      else if ((theByte&0x0010)==0) 
        ioChars[n++]=(char) (((theByte&0x000f)<<12)|
                             ((inBytes[m++]&0x003f)<<6)|
                              (inBytes[m++]&0x003f));
      else {
        // Invalid UTF8 or char that doesn't fit in a java char
        if ((n==inCharDelta)||(ioChars[n-1]!=kBadChar))
          ioChars[n++]=kBadChar;     // Add bad char marker if doesn't already exist
      }
    }
    return n-inCharDelta;
  }

  public static int bytesToUTF8Chars(byte[] inBytes, char[] ioChars, int inCharDelta) {
    return bytesToUTF8Chars(inBytes,0,inBytes.length,ioChars,inCharDelta); }

  public static char[] bytesToUTF8Chars(byte[] inBytes, int inByteDelta, int inNBytes) {
    if (inBytes==null)
      return null;
    int theNChars=countUTF8Chars(inBytes,inByteDelta,inNBytes);
    char[] theChars=new char[theNChars];
    bytesToUTF8Chars(inBytes,inByteDelta,inNBytes,theChars,0);
    return theChars;
  }

  public static char[] bytesToUTF8Chars(byte[] inBytes) {
    return bytesToUTF8Chars(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// normChar
//
// returns pos in ToChar array after norm chars = inToCharN + theNToChars
//
// Should:
//   Lowercase
//   Strip Accents - reduces string length
//   Strip Trademark, copyright, registered, etc - reduces string length
//   Expand ligatures - increases string length
//   Blank control chars
//   Collapse redundant symbols into one (similar to lowercasing)
//--------------------------------------------------------------------------------------------------------

  public static int normChar(char inFromChar, char[] inToChars, int inToCharN) {
    if (gCharFlags==null)
      throw new RuntimeException("Norm not loaded");
    byte theCharFlags=gCharFlags.getByte(inFromChar);
    if ((theCharFlags&kEmptyStringFlag)!=0)
      return inToCharN; 
    else {
      char theMapChar=(char) gMapChars.getInt(inFromChar);
      if ((theCharFlags&kSingleCharFlag)!=0) {
        inToChars[inToCharN]=theMapChar;
        return inToCharN+1;
      } else {
        byte[] theBytes=gMultiChars.getBytes(theMapChar);
        return inToCharN+bytesToUTF8Chars(theBytes,inToChars,inToCharN);
      }
    }
  }

  // returns String holding 0-4 ToChars for FromChar   (roman numeral viii takes 4 ToChars!)
  public static String normChar(char inFromChar) {
    byte theCharFlags=gCharFlags.getByte(inFromChar);
    if ((theCharFlags&kEmptyStringFlag)!=0)
      return ""; 
    else {
      char theMapChar=(char) gMapChars.getInt(inFromChar);
      if ((theCharFlags&kSingleCharFlag)!=0)
        return String.valueOf(theMapChar);
      else 
        return gMultiChars.getUTF8(theMapChar);
    }
  }

//--------------------------------------------------------------------------------------------------------
// normChars
//
// returns pos in ToChar array after norm chars = inToCharN + theNToChars
//--------------------------------------------------------------------------------------------------------

  public static int normChars(char[] inFromChars, int inFromCharN, int inFromNChars, 
      char[] ioToChars, int inToCharN, int[] ioFromForToCharNs) {
    int theFromCharEnd=inFromCharN+inFromNChars;
    int theToCharN=inToCharN;
    for (int theFromCharN=inFromCharN; theFromCharN<theFromCharEnd; theFromCharN++) {
      int theToCharEnd=normChar(inFromChars[theFromCharN],ioToChars,theToCharN);
      if (ioFromForToCharNs==null)
        theToCharN=theToCharEnd;
      else 
        while (theToCharN<theToCharEnd) 
          ioFromForToCharNs[theToCharN++]=theFromCharN;
    }
    if (ioFromForToCharNs!=null)
      ioFromForToCharNs[theToCharN]=theFromCharEnd;
    return theToCharN;
  }

  // returns String holding ToChars for FromChars
  public static String normChars(char[] inFromChars, int inFromCharN, int inFromNChars, 
      int[] ioFromForToCharNs) {
    char[] theCharSlice=new char[64+(int) (inFromNChars*1.1)];
    int theNToChars=normChars(inFromChars,inFromCharN,inFromNChars,theCharSlice,0,ioFromForToCharNs);
    return new String(theCharSlice,0,theNToChars);
  }

  public static String normChars(char[] inFromChars, int[] ioFromForToCharNs) { 
    return normChars(inFromChars,0,inFromChars.length,ioFromForToCharNs); }

  public static String normChars(String inText, int[] ioFromForToCharNs) {
    char[] theCharSlice=new char[inText.length()];
    inText.getChars(0,inText.length(),theCharSlice,0);
    String theString=normChars(theCharSlice,0,inText.length(),ioFromForToCharNs);      
    return theString;
  }

  public static String normChars(char[] inFromChars, int inFromCharN, int inFromNChars) { 
    return normChars(inFromChars,inFromCharN,inFromNChars,null); }

  public static String normChars(char[] inFromChars) { return normChars(inFromChars,null); }

  public static String normChars(String inText) { return normChars(inText,null); }

}
