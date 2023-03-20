//--------------------------------------------------------------------------------------------------------
// UTF8Utils
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import gravel.sort.*;

//--------------------------------------------------------------------------------------------------------
// UTF8Utils
//
// First bit of byte (aka the sign bit)
//   0 = single byte ascii 7 char
//   1 = first byte of multi-byte UTF-8 char
// Second bit when first bit set
//   10 = continuation byte in multi-byte UTF-8 char
//   11 = leading byte in multi-byte UTF-8 char
//
// char    UTF-8     byte
// bits    bytes     structure
//   7       1       0xxxxxxx
//           1       10xxxxxx                invalid char - started in middle
//  11       2       110xxxxx 10xxxxxx
//  16       3       1110xxxx 10xxxxxx 10xxxxxx
//  22       4       11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
//  28       5       111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
//  32       6       1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxx??
//           7+      1111111x                invalid char - 7+ bytes
//--------------------------------------------------------------------------------------------------------

public class UTF8Utils implements Constants {

//--------------------------------------------------------------------------------------------------------
// countUTF8Bytes
//--------------------------------------------------------------------------------------------------------

  public static int countUTF8Bytes(char inChar) {
    if (inChar<0x0080)
      return 1;
    else if (inChar<0x0800)
      return 2;
    else
      return 3;
    // Not dealing with multi char chars
  }

  public static int countUTF8Bytes(char[] inChars, int inCharDelta, int inNChars) {
    int theNBytes=0;
    int theEndChar=inCharDelta+inNChars;
    for (int i=inCharDelta; i<theEndChar; i++) 
      theNBytes+=countUTF8Bytes(inChars[i]);
    return theNBytes;
  }

  public static int countUTF8Bytes(char[] inChars) { return countUTF8Bytes(inChars,0,inChars.length); }

  public static int countUTF8Bytes(String inString) {
    int theNChars=inString.length();
    long theNFullSlices=SliceStore.getNFullCharSlices(theNChars);
    int theRemainder=SliceStore.getCharRemainder(theNChars);
    SliceStore theSliceStore=SliceStore.getSliceStore();
    char[] theCharSlice=theSliceStore.getCharSlice();
    int theNBytes=0;
    int theOffset=0;
    for (long i=0; i<theNFullSlices; i++) {
      inString.getChars(theOffset,theOffset+SliceStore.kCharSliceSize,theCharSlice,0);
      theNBytes+=countUTF8Bytes(theCharSlice);
      theOffset+=SliceStore.kCharSliceSize;
    }
    if (theRemainder>0) {
      inString.getChars(theOffset,theOffset+theRemainder,theCharSlice,0);
      theNBytes+=countUTF8Bytes(theCharSlice,0,theRemainder);
    }
    theSliceStore.putCharSlice(theCharSlice);
    return theNBytes;
  }

//--------------------------------------------------------------------------------------------------------
// isByteContinuation
//--------------------------------------------------------------------------------------------------------

  public static boolean isByteContinuation(byte inByte) { return ((inByte&0x00c0)==0x0080); }

//--------------------------------------------------------------------------------------------------------
// isByteCharStart
//--------------------------------------------------------------------------------------------------------

  public static boolean isByteCharStart(byte inByte) { return ((inByte&0x00c0)!=0x0080); }

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
// calcByteShifts
//
// ioByteShifts holds offset to byte pos, for each char pos (should be >= zero)
// byte pos for char n is:  n+ioByteShifts[n] 
//--------------------------------------------------------------------------------------------------------

  public static void calcByteShifts(char[] inChars, int inCharDelta, int inNChars, byte[] ioByteShifts) {
    byte theByteShift=0;
    for (int i=0; i<inNChars; i++) {
      ioByteShifts[i]=theByteShift;
      theByteShift+=countUTF8Bytes(inChars[inCharDelta+i])-1;
    }
  }

  public static void calcByteShifts(char[] inChars, byte[] ioByteShifts) {
    calcByteShifts(inChars,0,inChars.length,ioByteShifts); }

  public static byte[] calcByteShifts(char[] inChars, int inCharDelta, int inNChars) {
    if (inChars==null)
      return null;
    byte[] theByteShifts=Allocate.newBytes(inNChars);
    calcByteShifts(inChars,inCharDelta,inNChars,theByteShifts);
    return theByteShifts;
  }

  public static byte[] calcByteShifts(char[] inChars) {
    return calcByteShifts(inChars,0,inChars.length); }

//--------------------------------------------------------------------------------------------------------
// calcCharShifts
//
// ioCharShifts holds offset to char pos, for each byte pos (should be <= zero)
// char pos for byte n is:  n+ioCharShifts[n] 
//--------------------------------------------------------------------------------------------------------

  public static void calcCharShifts(byte[] inBytes, int inByteDelta, int inNBytes, byte[] ioCharShifts) {
    int n=0;
    byte theCharShift=0;
    while (n<inNBytes) {
      int theNBytes=countUTF8BytesInNextChar(inBytes,n);
      if ((theNBytes<=0)||(theNBytes>6))
        throw new RuntimeException("Corrupted UTF-8:  "+theNBytes+" byte char");
      ioCharShifts[n++]=theCharShift;
      for (int i=1; i<theNBytes; i++)
        ioCharShifts[n++]=theCharShift--;
    }
  }

  public static void calcCharShifts(byte[] inBytes, byte[] ioCharShifts) {
    calcCharShifts(inBytes,0,inBytes.length,ioCharShifts); }

  public static byte[] calcCharShifts(byte[] inBytes, int inByteDelta, int inNBytes) {
    if (inBytes==null)
      return null;
    byte[] theCharShifts=Allocate.newBytes(inNBytes);
    calcCharShifts(inBytes,inByteDelta,inNBytes,theCharShifts);
    return theCharShifts;
  }

  public static byte[] calcCharShifts(byte[] inBytes) {
    return calcCharShifts(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// advanceToUTF8Start
//
// Returns inByteDelta if at the start of a UTF-8 char
// Otherwise, advances until start found, and returns that ByteDelta
// If advances past end, returns inNBytes  (This is an issue - check for it!)
//--------------------------------------------------------------------------------------------------------

  public static int advanceToUTF8Start(byte[] inBytes, int inByteDelta, int inNBytes) {
    int n=inByteDelta;
    while ((n<inNBytes)&&(!isByteCharStart(inBytes[n])))  
      n++;
    return n;
  }

  public static int advanceToUTF8Start(byte[] inBytes, int inByteDelta) {
    return advanceToUTF8Start(inBytes,inByteDelta,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// backToUTF8Start
//
// Returns inByteDelta if at the start of a UTF-8 char
// Otherwise, backs up until start found, and returns that ByteDelta
// If advances past start, returns -1, which only happens if inBytes starts in the middle of a char
//--------------------------------------------------------------------------------------------------------

  public static int backToUTF8Start(byte[] inBytes, int inByteDelta) {
    int n=inByteDelta;
    while ((n>=0)&&(!isByteCharStart(inBytes[n])))
      n--;
    return n;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToUTF8Char
//--------------------------------------------------------------------------------------------------------

  public static char bytesToUTF8Char(byte[] inBytes, int inByteDelta) {
    int theByteDelta=inByteDelta;
    byte theByte1=inBytes[theByteDelta++];
    if ((theByte1&0x0080)==0)
      return (char) theByte1;
    else if ((theByte1&0x0040)==0)
      // Started in middle of UTF8 char - return bad char marker
      return kBadChar;
    else {
      byte theByte2=inBytes[theByteDelta++];
      if ((theByte2&0x00c0)!=0x0080)
        throw new RuntimeException("Bad continuation byte");
      else if ((theByte1&0x0020)==0) 
        return (char) (((theByte1&0x001f)<<6)|(theByte2&0x003f));
      else {
        byte theByte3=inBytes[theByteDelta++];
        if ((theByte3&0x00c0)!=0x0080)
          throw new RuntimeException("Bad continuation byte");
        if ((theByte1&0x0010)==0) 
          return (char) (((theByte1&0x000f)<<12)|((theByte2&0x003f)<<6)|(theByte3&0x003f));
        else 
          // Invalid UTF8 or char that doesn't fit in a java char - return bad char marker
          return kBadChar;
      }
    }
  }

  public static char bytesToUTF8Char(byte[] inBytes) { return bytesToUTF8Char(inBytes,0); }

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
    char[] theChars=Allocate.newChars(theNChars);
    bytesToUTF8Chars(inBytes,inByteDelta,inNBytes,theChars,0);
    return theChars;
  }

  public static char[] bytesToUTF8Chars(byte[] inBytes) {
    return bytesToUTF8Chars(inBytes,0,inBytes.length); }

//--------------------------------------------------------------------------------------------------------
// bytesToUTF8String
//--------------------------------------------------------------------------------------------------------

  public static String bytesToUTF8String(byte[] inBytes, int inByteDelta, int inNBytes) {   
    if (inBytes==null)
      return null;
    SliceStore theSliceStore=SliceStore.getSliceStore();
    char[] theCharSlice=theSliceStore.getCharSlice();
    String theString;
    if (inNBytes<=SliceStore.kCharSliceSize) {
      int theNChars=bytesToUTF8Chars(inBytes,inByteDelta,inNBytes,theCharSlice,0);
      theString=new String(theCharSlice,0,theNChars);
    } else {
      StringBuffer theStringBuffer=new StringBuffer(inNBytes);
      int theOffset=inByteDelta;
      int theLimit=inByteDelta+inNBytes-SliceStore.kCharSliceSize;
      while (theOffset<=theLimit) {
        int theNBytes=SliceStore.kCharSliceSize-1;
        while (!isByteCharStart(inBytes[theOffset+theNBytes]))
          theNBytes--;
        int theNChars=bytesToUTF8Chars(inBytes,theOffset,theNBytes,theCharSlice,0);
        theStringBuffer.append(theCharSlice,0,theNChars);
        theOffset+=theNBytes;
      }
      int theRemainder=inByteDelta+inNBytes-theOffset;
      if (theRemainder>0) {
        int theNChars=bytesToUTF8Chars(inBytes,theOffset,theRemainder,theCharSlice,0);
        theStringBuffer.append(theCharSlice,0,theNChars);
      }
      theString=theStringBuffer.toString();
    }
    theSliceStore.putCharSlice(theCharSlice);
    return theString;
  }

  public static String bytesToUTF8String(byte[] inBytes) { 
    if (inBytes==null)
      return null;
    return bytesToUTF8String(inBytes,0,inBytes.length); 
  }

//--------------------------------------------------------------------------------------------------------
// charToUTF8Bytes
//--------------------------------------------------------------------------------------------------------

  public static int charToUTF8Bytes(char inChar, byte[] ioBytes, int inByteDelta) {
    if (inChar<0x0080) {                    // 7 bits
      ioBytes[inByteDelta]=(byte) inChar;
      return 1;
    } else if (inChar<0x0800) {             // 11 bits
      ioBytes[inByteDelta]=(byte) (0x00c0|(inChar>>>6));
      ioBytes[inByteDelta+1]=(byte) (0x0080|(inChar&0x003f));
      return 2;
    } else {                                // 16 bits
      ioBytes[inByteDelta]=(byte) (0x00e0|(inChar>>>12));
      ioBytes[inByteDelta+1]=(byte) (0x0080|((inChar>>>6)&0x003f));
      ioBytes[inByteDelta+2]=(byte) (0x0080|(inChar&0x003f));
      return 3;
    }
    // Not dealing with multi char chars
  }

  public static byte[] charToUTF8Bytes(char inChar) {
    int theNBytes=countUTF8Bytes(inChar);
    byte[] theBytes=Allocate.newBytes(theNBytes);
    charToUTF8Bytes(inChar,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// charsToUTF8Bytes
//--------------------------------------------------------------------------------------------------------

  public static int charsToUTF8Bytes(char[] inChars, int inCharDelta, int inNChars, byte[] ioBytes,
          int inByteDelta) {
    int n=inByteDelta;
    int theEndChar=inCharDelta+inNChars;
    for (int i=inCharDelta; i<theEndChar; i++) {
      char theChar=inChars[i];
      if (theChar<0x0080)                       //  7 bits
        ioBytes[n++]=(byte) theChar;
      else if (theChar<0x0800) {                // 11 bits
        ioBytes[n++]=(byte) (0x00c0|(theChar>>>6));
        ioBytes[n++]=(byte) (0x0080|(theChar&0x003f));
      } else {                                  // 16 bits
        ioBytes[n++]=(byte) (0x00e0|(theChar>>>12));
        ioBytes[n++]=(byte) (0x0080|((theChar>>>6)&0x003f));
        ioBytes[n++]=(byte) (0x0080|(theChar&0x003f));
      }
      // Not dealing with multi char chars
    }
    return n-inByteDelta;
  }

  public static int charsToUTF8Bytes(char[] inChars, byte[] ioBytes, int inByteDelta) {
    return charsToUTF8Bytes(inChars,0,inChars.length,ioBytes,inByteDelta); }

  public static byte[] charsToUTF8Bytes(char[] inChars, int inCharDelta, int inNChars) {
    if (inChars==null)
      return null;
    int theNBytes=countUTF8Bytes(inChars,inCharDelta,inNChars);
    byte[] theBytes=Allocate.newBytes(theNBytes);
    charsToUTF8Bytes(inChars,inCharDelta,inNChars,theBytes,0);
    return theBytes;
  }

  public static byte[] charsToUTF8Bytes(char[] inChars) {
    return charsToUTF8Bytes(inChars,0,inChars.length); }
 
//--------------------------------------------------------------------------------------------------------
// stringToUTF8Bytes
//--------------------------------------------------------------------------------------------------------

  public static int stringToUTF8Bytes(String inString, byte[] ioBytes, int inByteDelta) {
    int theNChars=inString.length();
    int theNBytes=0;
    SliceStore theSliceStore=SliceStore.getSliceStore();
    char[] theCharSlice=theSliceStore.getCharSlice();
    if (theNChars<=SliceStore.kCharSliceSize) {
      inString.getChars(0,theNChars,theCharSlice,0);
      theNBytes+=charsToUTF8Bytes(theCharSlice,0,theNChars,ioBytes,inByteDelta);
    } else {
      long theNFullSlices=SliceStore.getNFullCharSlices(theNChars);
      int theRemainder=SliceStore.getCharRemainder(theNChars);
      int theSrcOffset=0;
      for (long i=0; i<theNFullSlices; i++) {
        inString.getChars(theSrcOffset,theSrcOffset+SliceStore.kCharSliceSize,theCharSlice,0);
        theNBytes+=charsToUTF8Bytes(theCharSlice,0,SliceStore.kCharSliceSize,ioBytes,inByteDelta+theNBytes);
        theSrcOffset+=SliceStore.kCharSliceSize;
      }
      if (theRemainder>0) {
        inString.getChars(theSrcOffset,theSrcOffset+theRemainder,theCharSlice,0);
        theNBytes+=charsToUTF8Bytes(theCharSlice,0,theRemainder,ioBytes,inByteDelta+theNBytes);
      }
    }
    theSliceStore.putCharSlice(theCharSlice);
    return theNBytes;
  }

  public static int stringToUTF8Bytes(String inString, byte[] ioBytes) {
    return stringToUTF8Bytes(inString,ioBytes,0); }

  public static byte[] stringToUTF8Bytes(String inString) {
    if (inString==null)
      return null;
    int theNBytes=countUTF8Bytes(inString);
    byte[] theBytes=Allocate.newBytes(theNBytes);
    stringToUTF8Bytes(inString,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// compareUTF8Bytes
//
// returns 0 if equal, positive if 1>2, and negative if 2>1
//--------------------------------------------------------------------------------------------------------

  public static int compareUTF8Bytes(byte[] inBytes1, int inByteDelta1, int inNBytes1,
      byte[] inBytes2, int inByteDelta2, int inNBytes2, byte inHandleCase) {

    // If case is significant (binary comparison), can just compare bytes    
    if (inHandleCase==Comparisons.kBinary)
      return Comparisons.compareBytes(inBytes1,inByteDelta1,inNBytes1,
          inBytes2,inByteDelta2,inNBytes2,inHandleCase);
    
    // If don't need to perform CharNorm, can check chars one by one
    boolean theNeedNorm=((inHandleCase==Comparisons.kNormChars)||(inHandleCase==Comparisons.kRawBreaksTies));
    if (!theNeedNorm) {
      
      // Extract chars and compare 
      int theOffset1=inByteDelta1;
      int theOffset2=inByteDelta2;
      while ((theOffset1<inNBytes1)&&(theOffset2<inNBytes2)) {
        char theChar1=UTF8Utils.bytesToUTF8Char(inBytes1,theOffset1);
        char theChar2=UTF8Utils.bytesToUTF8Char(inBytes2,theOffset2);
        theOffset1+=UTF8Utils.countUTF8Bytes(theChar1);
        theOffset2+=UTF8Utils.countUTF8Bytes(theChar2);
        int theDiff=Comparisons.compareChars(theChar1,theChar2,inHandleCase);
        if (theDiff<0) 
          return Comparisons.kLessThan;
        else if (theDiff>0) 
          return Comparisons.kGreaterThan;
      }
      int theDiff=inNBytes1-inNBytes2;
      if (theDiff<0) 
        return Comparisons.kLessThan;
      else if (theDiff>0) 
        return Comparisons.kGreaterThan;
      else if (inHandleCase!=Comparisons.kCaseBreaksTies)
        return Comparisons.kEquals;
      else
        return Comparisons.compareBytes(inBytes1,inByteDelta1,inNBytes1,
            inBytes2,inByteDelta2,inNBytes2,Comparisons.kBinary);
      
    } else {
      
      // Norm does not preserve number of chars
      // Need to convert all UTF-8 bytes to chars before norm
      SliceStore theSliceStore=SliceStore.getSliceStore();
      char[] theChars1=theSliceStore.getCharSlice();
      char[] theChars2=theSliceStore.getCharSlice();
      int theLength1=bytesToUTF8Chars(inBytes1,inByteDelta1,inNBytes1,theChars1,0);
      int theLength2=bytesToUTF8Chars(inBytes2,inByteDelta2,inNBytes2,theChars2,0);
      int theDiff=Comparisons.compareChars(theChars1,0,theLength1,theChars2,0,theLength2,inHandleCase);
      theSliceStore.putCharSlice(theChars2);
      theSliceStore.putCharSlice(theChars1);
      return theDiff;
    }
  }

  public static int compareUTF8Bytes(byte[] inBytes1, byte[] inBytes2, byte inHandleCase) {
      return compareUTF8Bytes(inBytes1,0,inBytes1.length,inBytes2,0,inBytes2.length,inHandleCase); }

}
