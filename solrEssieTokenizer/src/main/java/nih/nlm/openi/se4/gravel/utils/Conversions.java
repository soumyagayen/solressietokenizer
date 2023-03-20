//--------------------------------------------------------------------------------------------------------
// Conversions.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// Conversions
/**
* A set of static routines for converting bytes to other native types, such as ints, floats, longs,
* doubles, chars, and strings
* <P>
* IMPORTANT CONCEPTS  --  LEARN EM, LOVE EM, LIVE EM
* <P>
* 1) Allocating arrays is expensive!!! <BR>
* 2) Synchronization on multi-cpu and multi-core computers is expensive!!!
* <P>
*/
//--------------------------------------------------------------------------------------------------------

public class Conversions implements Constants {

//--------------------------------------------------------------------------------------------------------
// Conversions constants
//--------------------------------------------------------------------------------------------------------

  public static final byte[]       kBooleanFlags=new byte[] {1,2,4,8,16,32,64,-128};
  public static final byte[]       kBooleanMasks=new byte[] {-2,-3,-5,-9,-17,-33,-65,127};


//========================================================================================================
//
// Conversions from bytes at offset to native type
//
// Input params are:
//   byte[] inBytes   source bytes
//   int inByteDelta  offset into inBytes where conversion should start
//
// Outputs the native type converted to
//
// If inByteDelta is too small a RuntimeException will occur
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// bytesToBoolean
/**
* Converts 1 byte in inBytes at inByteDelta into a boolean
* <p>
* Throws an ArrayOutOfBounds runtime exception if the required byte falls outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-1
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the boolean value of the byte in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static boolean bytesToBoolean(byte[] inBytes, int inByteDelta) {
    return getBitInByte(inBytes[inByteDelta],0); }

//--------------------------------------------------------------------------------------------------------
// bytesToShort
/**
* Converts 2 bytes in inBytes starting at inByteDelta into a short
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required 2 bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-2
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the short value of the 2 bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static short bytesToShort(byte[] inBytes, int inByteDelta) {
    return (short) (((inBytes[inByteDelta]&0x00ff)<<8)|(inBytes[inByteDelta+1]&0x00ff)); }

//--------------------------------------------------------------------------------------------------------
// bytesToChar
/**
* Converts 2 bytes in inBytes starting at inByteDelta into a char
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required 2 bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-2
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the char value of the 2 bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static char bytesToChar(byte[] inBytes, int inByteDelta) {
    return (char) (((inBytes[inByteDelta]&0x00ff)<<8)|(inBytes[inByteDelta+1]&0x00ff)); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarChar
/**
* Converts inCharSize bytes in inBytes starting at inByteDelta into a char
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required inCharSize bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inCharSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inCharSize    number of source bytes to use
*
* @return  the char value of the inCharSize bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static char bytesToVarChar(byte[] inBytes, int inByteDelta, int inCharSize) {
    if ((inCharSize<1)||(inCharSize>2))
      throw new RuntimeException("Only 1-2 bytes fit in a char");
    char theChar=(char) (inBytes[inByteDelta]&0x00ff);
    if (inCharSize==2) {
      theChar<<=8;
      theChar|=(inBytes[inByteDelta+1]&0x00ff);
    }
    return theChar;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToInt
/**
* Converts 4 bytes in inBytes starting at inByteDelta into an int
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required 4 bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-4
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the int value of the 4 bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static int bytesToInt(byte[] inBytes, int inByteDelta) {
    int theInt=inBytes[inByteDelta]; // Sign transfer ocurrs here
    int theByteEnd=inByteDelta+4;
    for (int i=inByteDelta+1; i<theByteEnd; i++) {
      theInt<<=8;
      theInt|=(inBytes[i]&0x00ff);
    }
    return theInt;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarInt
/**
* Converts inIntSize bytes in inBytes starting at inByteDelta into an int
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required inIntSize bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inIntSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inIntSize     number of source bytes to use
*
* @return  the int value of the inIntSize bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static int bytesToVarInt(byte[] inBytes, int inByteDelta, int inIntSize) {
    if ((inIntSize<1)||(inIntSize>4))
      throw new RuntimeException("Only 1-4 bytes fit in an int");
    int theInt=inBytes[inByteDelta]; // Sign transfer ocurrs here
    int theByteEnd=inByteDelta+inIntSize;
    for (int i=inByteDelta+1; i<theByteEnd; i++) {
      theInt<<=8;
      theInt|=(inBytes[i]&0x00ff);
    }
    return theInt;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToLong
/**
* Converts 8 bytes in inBytes starting at inByteDelta into a long
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required 8 bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-8
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the long value of the 8 bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static long bytesToLong(byte[] inBytes, int inByteDelta) {
    long theLong=inBytes[inByteDelta]; // Sign transfer ocurrs here
    int theByteEnd=inByteDelta+8;
    for (int i=inByteDelta+1; i<theByteEnd; i++) {
      theLong<<=8;
      theLong|=(inBytes[i]&0x00ff);
    }
    return theLong;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarLong
/**
* Converts inLongSize bytes in inBytes starting at inByteDelta into a long
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required inLongSize bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inLongSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inLongSize    number of source bytes to use
*
* @return  the long value of the inLongSize bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static long bytesToVarLong(byte[] inBytes, int inByteDelta, int inLongSize) {
    if ((inLongSize<1)||(inLongSize>8))
      throw new RuntimeException("Only 1-8 bytes fit in a long");
    long theLong=inBytes[inByteDelta]; // Sign transfer ocurrs here
    int theByteEnd=inByteDelta+inLongSize;
    for (int i=inByteDelta+1; i<theByteEnd; i++) {
      theLong<<=8;
      theLong|=(inBytes[i]&0x00ff);
    }
    return theLong;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToFloat
/**
* Converts 4 bytes in inBytes starting at inByteDelta into a float
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required 4 bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-4
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the float value of the 4 bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static float bytesToFloat(byte[] inBytes, int inByteDelta) {
    return intToFloat(bytesToInt(inBytes,inByteDelta)); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarFloat
/**
* Converts inFloatSize bytes in inBytes starting at inByteDelta into a float
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required inFloatSize bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inFloatSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inFloatSize   number of source bytes to use
*
* @return  the float value of the inFloatSize bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static float bytesToVarFloat(byte[] inBytes, int inByteDelta, int inFloatSize) {
    return (float) bytesToVarDouble(inBytes,inByteDelta,inFloatSize); }

//--------------------------------------------------------------------------------------------------------
// bytesToDouble
/**
* Converts 8 bytes in inBytes starting at inByteDelta into a double
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required 8 bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-8
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
*
* @return  the double value of the 8 bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static double bytesToDouble(byte[] inBytes, int inByteDelta) {
    return longToDouble(bytesToLong(inBytes,inByteDelta)); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarDouble
/**
* Converts inDoubleSize bytes in inBytes starting at inByteDelta into a double
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the required inDoubleSize bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inDoubleSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inDoubleSize  number of source bytes to use
*
* @return  the double value of the inDoubleSize bytes in inBytes starting at inByteDelta
*/
//--------------------------------------------------------------------------------------------------------

  public static double bytesToVarDouble(byte[] inBytes, int inByteDelta, int inDoubleSize) {
    if ((inDoubleSize<3)||(inDoubleSize>8))
      throw new RuntimeException("Only 3-8 bytes fit in a double");
    long theLong=inBytes[inByteDelta];
    int theByteEnd=inByteDelta+inDoubleSize;
    for (int i=inByteDelta+1; i<theByteEnd; i++) {
      theLong<<=8;
      theLong|=(inBytes[i]&0x00ff);
    }
    theLong<<=(8*(8-inDoubleSize));
    return longToDouble(theLong);
  }




//========================================================================================================
//
// Conversions from bytes with offset and length to array of native type at offset
//
// These routines are fast because no arrays are allocated
//
// Input params are:
//   byte[] inBytes   source bytes
//   int inByteDelta  offset into inBytes where conversion should start
//   int inNBytes     number of bytes at inByteDelta to use in conversion
//   XXX[] ioXXXs     destination array of native type, XXX
//   int inXXXDelta   offset into ioXXXs where conversion should start
//
// No Output - part of ioXXXs is overwritten
//
// If inByteDelta is too big a RuntimeException will occur
// If inNBytes is too big a RuntimeException will occur
// If inNBytes is not an exact multiple of the size of the native type, the last few bytes
//   will not be used
// If ioXXXs is too short a RuntimeException will occur
// If inXXXDelta is too big a RuntimeException will occur
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// bytesToShorts
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/2 shorts in ioShorts
* starting at inShortDelta.  Contents of ioShorts are overwritten.
* <p>
* If inNBytes is not an exact multiple of 2, the last byte will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioShorts too small.  Must have
* ioShorts.length >= inNBytes/2
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/2 destination shorts fall outside
* the ioShorts array.  Therefore, inShortDelta must be between 0 and ioShorts.length-inNBytes/2
*
* @param   inBytes        source bytes
* @param   inByteDelta    offset into inBytes where conversion should start
* @param   inNBytes       number of bytes in inBytes to use in conversion
* @param   ioShorts       destination short array
* @param   inShortDelta   offset into ioShorts where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToShorts(byte[] inBytes, int inByteDelta, int inNBytes, short[] ioShorts,
          int inShortDelta) {
    int theByteDelta=inByteDelta;
    int theEndShort=inShortDelta+inNBytes/kShortMemory;
    for (int i=inShortDelta; i<theEndShort; i++) {
      ioShorts[i]=bytesToShort(inBytes,theByteDelta);
      theByteDelta+=kShortMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToChars
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/2 chars in ioChars
* starting at inCharDelta.  Contents of ioChars are overwritten.
* <p>
* If inNBytes is not an exact multiple of 2, the last byte will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioChars too small.  Must have
* ioChars.length >= inNBytes/2
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/2 destination chars fall outside
* the ioChars array.  Therefore, inCharDelta must be between 0 and ioChars.length-inNBytes/2
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inNBytes      number of bytes in inBytes to use in conversion
* @param   ioChars       destination char array
* @param   inCharDelta   offset into ioChars where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToChars(byte[] inBytes, int inByteDelta, int inNBytes, char[] ioChars,
          int inCharDelta) {
    int theByteDelta=inByteDelta;
    int theEndChar=inCharDelta+inNBytes/kCharMemory;
    for (int i=inCharDelta; i<theEndChar; i++) {
      ioChars[i]=bytesToChar(inBytes,theByteDelta);
      theByteDelta+=kCharMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarChars
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/inCharSize chars in ioChars
* starting at inCharDelta.  Contents of ioChars are overwritten.
* <p>
* If inNBytes is not an exact multiple of inCharSize, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioChars too small.  Must have
* ioChars.length >= inNBytes/inCharSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/inCharSize destination
* chars fall outside the ioChars array.  Therefore, inCharDelta must be between 0 and
* ioChars.length-inNBytes/inCharSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inNBytes      number of bytes in inBytes to use in conversion
* @param   ioChars       destination char array
* @param   inCharDelta   offset into ioChars where conversion should start
* @param   inCharSize    number of source bytes to use for each char
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToVarChars(byte[] inBytes, int inByteDelta, int inNBytes, char[] ioChars,
          int inCharDelta, int inCharSize) {
    int theByteDelta=inByteDelta;
    int theEndChar=inCharDelta+inNBytes/inCharSize;
    for (int i=inCharDelta; i<theEndChar; i++) {
      ioChars[i]=bytesToVarChar(inBytes,theByteDelta,inCharSize);
      theByteDelta+=inCharSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToInts
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/4 ints in ioInts
* starting at inIntDelta.  Contents of ioInts are overwritten.
* <p>
* If inNBytes is not an exact multiple of 4, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioInts too small.  Must have
* ioInts.length >= inNBytes/4
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/4 destination ints fall outside
* the ioInts array.  Therefore, inIntDelta must be between 0 and ioInts.length-inNBytes/4
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inNBytes      number of bytes in inBytes to use in conversion
* @param   ioInts        destination int array
* @param   inIntDelta    offset into ioInts where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToInts(byte[] inBytes, int inByteDelta, int inNBytes, int[] ioInts,
          int inIntDelta) {
    int theByteDelta=inByteDelta;
    int theEndInt=inIntDelta+inNBytes/kIntMemory;
    for (int i=inIntDelta; i<theEndInt; i++) {
      ioInts[i]=bytesToInt(inBytes,theByteDelta);
      theByteDelta+=kIntMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarInts
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/inIntSize ints in ioInts
* starting at inIntDelta.  Contents of ioInts are overwritten.
* <p>
* If inNBytes is not an exact multiple of inIntSize, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioInts too small.  Must have
* ioInts.length >= inNBytes/inIntSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/inIntSize destination
* ints fall outside the ioInts array.  Therefore, inIntDelta must be between 0 and
* ioInts.length-inNBytes/inIntSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inNBytes      number of bytes in inBytes to use in conversion
* @param   ioInts        destination int array
* @param   inIntDelta    offset into ioInts where conversion should start
* @param   inIntSize     number of source bytes to use for each int
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToVarInts(byte[] inBytes, int inByteDelta, int inNBytes, int[] ioInts,
          int inIntDelta, int inIntSize) {
    int theByteDelta=inByteDelta;
    int theEndInt=inIntDelta+inNBytes/inIntSize;
    for (int i=inIntDelta; i<theEndInt; i++) {
      ioInts[i]=bytesToVarInt(inBytes,theByteDelta,inIntSize);
      theByteDelta+=inIntSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToLongs
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/8 longs in ioLongs
* starting at inLongDelta.  Contents of ioLongs are overwritten.
* <p>
* If inNBytes is not an exact multiple of 8, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioLongs too small.  Must have
* ioLongs.length >= inNBytes/8
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/8 destination longs fall outside
* the ioLongs array.  Therefore, inLongDelta must be between 0 and ioLongs.length-inNBytes/8
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inNBytes      number of bytes in inBytes to use in conversion
* @param   ioLongs       destination long array
* @param   inLongDelta   offset into ioLongs where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToLongs(byte[] inBytes, int inByteDelta, int inNBytes, long[] ioLongs,
          int inLongDelta) {
    int theByteDelta=inByteDelta;
    int theEndLong=inLongDelta+inNBytes/kLongMemory;
    for (int i=inLongDelta; i<theEndLong; i++) {
      ioLongs[i]=bytesToLong(inBytes,theByteDelta);
      theByteDelta+=kLongMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarLongs
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/inLongSize longs in ioLongs
* starting at inLongDelta.  Contents of ioLongs are overwritten.
* <p>
* If inNBytes is not an exact multiple of inLongSize, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioLongs too small.  Must have
* ioLongs.length >= inNBytes/inLongSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/inLongSize destination
* longs fall outside the ioLongs array.  Therefore, inLongDelta must be between 0 and
* ioLongs.length-inNBytes/inLongSize
*
* @param   inBytes       source bytes
* @param   inByteDelta   offset into inBytes where conversion should start
* @param   inNBytes      number of bytes in inBytes to use in conversion
* @param   ioLongs       destination long array
* @param   inLongDelta   offset into ioLongs where conversion should start
* @param   inLongSize    number of source bytes to use for each long
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToVarLongs(byte[] inBytes, int inByteDelta, int inNBytes, long[] ioLongs,
          int inLongDelta, int inLongSize) {
    int theByteDelta=inByteDelta;
    int theEndLong=inLongDelta+inNBytes/inLongSize;
    for (int i=inLongDelta; i<theEndLong; i++) {
      ioLongs[i]=bytesToVarLong(inBytes,theByteDelta,inLongSize);
      theByteDelta+=inLongSize;
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// bytesToFloats
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/4 floats in ioFloats
* starting at inFloatDelta.  Contents of ioFloats are overwritten.
* <p>
* If inNBytes is not an exact multiple of 4, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioFloats too small.  Must have
* ioFloats.length >= inNBytes/4
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/4 destination floats fall outside
* the ioFloats array.  Therefore, inFloatDelta must be between 0 and ioFloats.length-inNBytes/4
*
* @param   inBytes         source bytes
* @param   inByteDelta     offset into inBytes where conversion should start
* @param   inNBytes        number of bytes in inBytes to use in conversion
* @param   ioFloats       destination float array
* @param   inFloatDelta   offset into ioFloats where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToFloats(byte[] inBytes, int inByteDelta, int inNBytes, float[] ioFloats,
          int inFloatDelta) {
    int theByteDelta=inByteDelta;
    int theEndFloat=inFloatDelta+inNBytes/kFloatMemory;
    for (int i=inFloatDelta; i<theEndFloat; i++) {
      ioFloats[i]=bytesToFloat(inBytes,theByteDelta);
      theByteDelta+=kFloatMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarFloats
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/inFloatSize floats in ioFloats
* starting at inFloatDelta.  Contents of ioFloats are overwritten.
* <p>
* If inNBytes is not an exact multiple of inFloatSize, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioFloats too small.  Must have
* ioFloats.length >= inNBytes/inFloatSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/inFloatSize destination
* floats fall outside the ioFloats array.  Therefore, inFloatDelta must be between 0 and
* ioFloats.length-inNBytes/inFloatSize
*
* @param   inBytes         source bytes
* @param   inByteDelta     offset into inBytes where conversion should start
* @param   inNBytes        number of bytes in inBytes to use in conversion
* @param   ioFloats       destination float array
* @param   inFloatDelta   offset into ioFloats where conversion should start
* @param   inFloatSize    number of source bytes to use for each float
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToVarFloats(byte[] inBytes, int inByteDelta, int inNBytes, float[] ioFloats,
          int inFloatDelta, int inFloatSize) {
    int theByteDelta=inByteDelta;
    int theEndFloat=inFloatDelta+inNBytes/inFloatSize;
    for (int i=inFloatDelta; i<theEndFloat; i++) {
      ioFloats[i]=bytesToVarFloat(inBytes,theByteDelta,inFloatSize);
      theByteDelta+=inFloatSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToDoubles
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/8 doubles in ioDoubles
* starting at inDoubleDelta.  Contents of ioDoubles are overwritten.
* <p>
* If inNBytes is not an exact multiple of 8, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioDoubles too small.  Must have
* ioDoubles.length >= inNBytes/8
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/8 destination doubles fall outside
* the ioDoubles array.  Therefore, inDoubleDelta must be between 0 and ioDoubles.length-inNBytes/8
*
* @param   inBytes         source bytes
* @param   inByteDelta     offset into inBytes where conversion should start
* @param   inNBytes        number of bytes in inBytes to use in conversion
* @param   ioDoubles       destination double array
* @param   inDoubleDelta   offset into ioDoubles where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToDoubles(byte[] inBytes, int inByteDelta, int inNBytes, double[] ioDoubles,
          int inDoubleDelta) {
    int theByteDelta=inByteDelta;
    int theEndDouble=inDoubleDelta+inNBytes/kDoubleMemory;
    for (int i=inDoubleDelta; i<theEndDouble; i++) {
      ioDoubles[i]=bytesToDouble(inBytes,theByteDelta);
      theByteDelta+=kDoubleMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarDoubles
/**
* Converts inNBytes bytes in inBytes starting at inByteDelta to inNBytes/inDoubleSize doubles in ioDoubles
* starting at inDoubleDelta.  Contents of ioDoubles are overwritten.
* <p>
* If inNBytes is not an exact multiple of inDoubleSize, the last few bytes will not be used.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioDoubles too small.  Must have
* ioDoubles.length >= inNBytes/inDoubleSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes source bytes fall outside
* the inBytes array.  Therefore, inByteDelta must be between 0 and inBytes.length-inNBytes
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNBytes/inDoubleSize destination
* doubles fall outside the ioDoubles array.  Therefore, inDoubleDelta must be between 0 and
* ioDoubles.length-inNBytes/inDoubleSize
*
* @param   inBytes         source bytes
* @param   inByteDelta     offset into inBytes where conversion should start
* @param   inNBytes        number of bytes in inBytes to use in conversion
* @param   ioDoubles       destination double array
* @param   inDoubleDelta   offset into ioDoubles where conversion should start
* @param   inDoubleSize    number of source bytes to use for each double
*/
//--------------------------------------------------------------------------------------------------------

  public static void bytesToVarDoubles(byte[] inBytes, int inByteDelta, int inNBytes, double[] ioDoubles,
          int inDoubleDelta, int inDoubleSize) {
    int theByteDelta=inByteDelta;
    int theEndDouble=inDoubleDelta+inNBytes/inDoubleSize;
    for (int i=inDoubleDelta; i<theEndDouble; i++) {
      ioDoubles[i]=bytesToVarDouble(inBytes,theByteDelta,inDoubleSize);
      theByteDelta+=inDoubleSize;
    }
  }
 



//========================================================================================================
//
// Conversions from bytes to native type
//
// Input params are:
//   byte[] inBytes   source bytes
//
// Outputs the native type converted to
//
// If inBytes is too short a RuntimeException will occur
// If inBytes is too long only the beginning of it will be used
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// bytesToBoolean
/**
* Converts the leading 1 byte in inBytes into a boolean
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 1 byte long
*
* @param   inBytes       source bytes
*
* @return  the boolean value of the leading 1 byte in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static boolean bytesToBoolean(byte[] inBytes) { return bytesToBoolean(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToShort
/**
* Converts the leading 2 bytes in inBytes into a short
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 2 bytes long
*
* @param   inBytes       source bytes
*
* @return  the short value of the leading 2 bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static short bytesToShort(byte[] inBytes) { return bytesToShort(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToChar
/**
* Converts the leading 2 bytes in inBytes into a char
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 2 bytes long
*
* @param   inBytes       source bytes
*
* @return  the char value of the leading 2 bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static char bytesToChar(byte[] inBytes) { return bytesToChar(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarChar
/**
* Converts the leading inCharSize bytes in inBytes into a char
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than inCharSize bytes long
*
* @param   inBytes       source bytes
* @param   inCharSize    number of source bytes to use
*
* @return  the char value of the leading inCharSizebytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static char bytesToVarChar(byte[] inBytes, int inCharSize) {
    return bytesToVarChar(inBytes,0,inCharSize); }

//--------------------------------------------------------------------------------------------------------
// bytesToInt
/**
* Converts the leading 4 bytes in inBytes into an int
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 4 bytes long
*
* @param   inBytes       source bytes
*
* @return  the int value of the leading 4 bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static int bytesToInt(byte[] inBytes) { return bytesToInt(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarInt
/**
* Converts the leading inIntSize bytes in inBytes into an int
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than inIntSize bytes long
*
* @param   inBytes       source bytes
* @param   inIntSize     number of source bytes to use
*
* @return  the int value of the leading inIntSizebytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static int bytesToVarInt(byte[] inBytes, int inIntSize) { 
    return bytesToVarInt(inBytes,0,inIntSize); }

//--------------------------------------------------------------------------------------------------------
// bytesToLong
/**
* Converts the leading 8 bytes in inBytes into a long
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 8 bytes long
*
* @param   inBytes       source bytes
*
* @return  the long value of the leading 8 bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static long bytesToLong(byte[] inBytes) { return bytesToLong(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarLong
/**
* Converts the leading inLongSize bytes in inBytes into a long
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than inLongSize bytes long
*
* @param   inBytes       source bytes
* @param   inLongSize    number of source bytes to use
*
* @return  the long value of the leading inLongSize bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static long bytesToVarLong(byte[] inBytes, int inLongSize) {
    return bytesToVarLong(inBytes,0,inLongSize); }

//--------------------------------------------------------------------------------------------------------
// bytesToFloat
/**
* Converts the leading 4 bytes in inBytes into a float
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 4 bytes long
*
* @param   inBytes       source bytes
*
* @return  the float value of the leading 4 bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static float bytesToFloat(byte[] inBytes) { return bytesToFloat(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarFloat
/**
* Converts the leading inFloatSize bytes in inBytes into a float
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than inFloatSize bytes float
*
* @param   inBytes       source bytes
* @param   inFloatSize    number of source bytes to use
*
* @return  the float value of the leading inFloatSize bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static float bytesToVarFloat(byte[] inBytes, int inFloatSize) {
    return bytesToVarFloat(inBytes,0,inFloatSize); }

//--------------------------------------------------------------------------------------------------------
// bytesToDouble
/**
* Converts the leading 8 bytes in inBytes into a double
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than 8 bytes long
*
* @param   inBytes       source bytes
*
* @return  the double value of the leading 8 bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static double bytesToDouble(byte[] inBytes) { return bytesToDouble(inBytes,0); }

//--------------------------------------------------------------------------------------------------------
// bytesToVarDouble
/**
* Converts the leading inDoubleSize bytes in inBytes into a double
* <p>
* Throws an ArrayOutOfBounds runtime exception if inBytes is less than inDoubleSize bytes double
*
* @param   inBytes       source bytes
* @param   inDoubleSize    number of source bytes to use
*
* @return  the double value of the leading inDoubleSize bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static double bytesToVarDouble(byte[] inBytes, int inDoubleSize) {
    return bytesToVarDouble(inBytes,0,inDoubleSize); }




//========================================================================================================
//
// Conversions from bytes to array of native type
//
// These routines are slower because arrays are allocated
//
// Input params are:
//   byte[] inBytes   source bytes
//
// Outputs an array of the native type converted to
//
// If inBytes is not an exact multiple of the size of the native type, the last few bytes
//   will not be used
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// bytesToShorts
/**
* Converts inBytes into a short[] of length inNBytes.length/2
* <p>
* If inNBytes.length is not an exact multiple of 2, the last byte will not be used.
* <p>
*
* @param   inBytes       source bytes
*
* @return  a short[] of length inNBytes.length/2 with shorts specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static short[] bytesToShorts(byte[] inBytes) {
    short[] theShorts=Allocate.newShorts(inBytes.length/kShortMemory);
    bytesToShorts(inBytes,0,inBytes.length,theShorts,0);
    return theShorts;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToChars
/**
* Converts inBytes into a char[] of length inNBytes.length/2
* <p>
* If inNBytes.length is not an exact multiple of 2, the last byte will not be used.
* <p>
*
* @param   inBytes       source bytes
*
* @return  a char[] of length inNBytes.length/2 with chars specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static char[] bytesToChars(byte[] inBytes) {
    char[] theChars=Allocate.newChars(inBytes.length/kCharMemory);
    bytesToChars(inBytes,0,inBytes.length,theChars,0);
    return theChars;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarChars
/**
* Converts inBytes into an char[] of length inNBytes.length/inCharSize
* <p>
* If inNBytes.length is not an exact multiple of inCharSize, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
* @param   inCharSize    number of source bytes to use for each char
*
* @return  an char[] of length inNBytes.length/inCharSize with chars specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static char[] bytesToVarChars(byte[] inBytes, int inCharSize) {
    char[] theChars=Allocate.newChars(inBytes.length/inCharSize);
    bytesToVarChars(inBytes,0,inBytes.length,theChars,0,inCharSize);
    return theChars;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToInts
/**
* Converts inBytes into an int[] of length inNBytes.length/4
* <p>
* If inNBytes.length is not an exact multiple of 4, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
*
* @return  an int[] of length inNBytes.length/4 with ints specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static int[] bytesToInts(byte[] inBytes) {
    int[] theInts=Allocate.newInts(inBytes.length/kIntMemory);
    bytesToInts(inBytes,0,inBytes.length,theInts,0);
    return theInts;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarInts
/**
* Converts inBytes into an int[] of length inNBytes.length/inIntSize
* <p>
* If inNBytes.length is not an exact multiple of inIntSize, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
* @param   inIntSize     number of source bytes to use for each int
*
* @return  an int[] of length inNBytes.length/inIntSize with ints specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static int[] bytesToVarInts(byte[] inBytes, int inIntSize) {
    int[] theInts=Allocate.newInts(inBytes.length/inIntSize);
    bytesToVarInts(inBytes,0,inBytes.length,theInts,0,inIntSize);
    return theInts;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToLongs
/**
* Converts inBytes into a long[] of length inNBytes.length/8
* <p>
* If inNBytes.length is not an exact multiple of 8, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
*
* @return  a long[] of length inNBytes.length/8 with longs specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static long[] bytesToLongs(byte[] inBytes) {
    long[] theLongs=Allocate.newLongs(inBytes.length/kLongMemory);
    bytesToLongs(inBytes,0,inBytes.length,theLongs,0);
    return theLongs;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarLongs
/**
* Converts inBytes into a long[] of length inNBytes.length/inLongSize
* <p>
* If inNBytes.length is not an exact multiple of inLongSize, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
* @param   inLongSize    number of source bytes to use for each long
*
* @return  a long[] of length inNBytes.length/inLongSize with longs specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static long[] bytesToVarLongs(byte[] inBytes, int inLongSize) {
    long[] theLongs=Allocate.newLongs(inBytes.length/inLongSize);
    bytesToVarLongs(inBytes,0,inBytes.length,theLongs,0,inLongSize);
    return theLongs;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToFloats
/**
* Converts inBytes into a float[] of length inNBytes.length/4
* <p>
* If inNBytes.length is not an exact multiple of 4, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
*
* @return  a float[] of length inNBytes.length/4 with floats specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static float[] bytesToFloats(byte[] inBytes) {
    float[] theFloats=Allocate.newFloats(inBytes.length/kFloatMemory);
    bytesToFloats(inBytes,0,inBytes.length,theFloats,0);
    return theFloats;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarFloats
/**
* Converts inBytes into a float[] of length inNBytes.length/inFloatSize
* <p>
* If inNBytes.length is not an exact multiple of inFloatSize, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
* @param   inFloatSize    number of source bytes to use for each float
*
* @return  a float[] of length inNBytes.length/inFloatSize with floats specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static float[] bytesToVarFloats(byte[] inBytes, int inFloatSize) {
    float[] theFloats=Allocate.newFloats(inBytes.length/inFloatSize);
    bytesToVarFloats(inBytes,0,inBytes.length,theFloats,0,inFloatSize);
    return theFloats;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToDoubles
/**
* Converts inBytes into a double[] of length inNBytes.length/8
* <p>
* If inNBytes.length is not an exact multiple of 8, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
*
* @return  a double[] of length inNBytes.length/8 with doubles specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static double[] bytesToDoubles(byte[] inBytes) {
    double[] theDoubles=Allocate.newDoubles(inBytes.length/kDoubleMemory);
    bytesToDoubles(inBytes,0,inBytes.length,theDoubles,0);
    return theDoubles;
  }

//--------------------------------------------------------------------------------------------------------
// bytesToVarDoubles
/**
* Converts inBytes into a double[] of length inNBytes.length/inDoubleSize
* <p>
* If inNBytes.length is not an exact multiple of inDoubleSize, the last few bytes will not be used.
* <p>
*
* @param   inBytes       source bytes
* @param   inDoubleSize    number of source bytes to use for each double
*
* @return  a double[] of length inNBytes.length/inDoubleSize with doubles specified by bytes in inBytes
*/
//--------------------------------------------------------------------------------------------------------

  public static double[] bytesToVarDoubles(byte[] inBytes, int inDoubleSize) {
    double[] theDoubles=Allocate.newDoubles(inBytes.length/inDoubleSize);
    bytesToVarDoubles(inBytes,0,inBytes.length,theDoubles,0,inDoubleSize);
    return theDoubles;
  }




//========================================================================================================
//
// Conversions from native type to bytes at offset
//
// These routines are fast because no arrays are allocated
//
// Input params are:
//   XXX inXXX        source native type converted from
//   byte[] ioBytes   destination bytes
//   int inByteDelta  offset into ioBytes where conversion should start
//
// No Output - part of ioBytes is overwritten
//
// If ioBytes is too short a RuntimeException will occur
// If inByteDelta is too big a RuntimeException will occur
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// booleanToBytes
/**
* Converts the inBoolean to 1 byte in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if the destination byte falls outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-1
*
* @param   inBoolean     source boolean
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void booleanToBytes(boolean inBoolean, byte[] ioBytes, int inByteDelta) {
    if (inBoolean)
      ioBytes[inByteDelta]=(byte) -1;
    else
      ioBytes[inByteDelta]=0;
  }

//--------------------------------------------------------------------------------------------------------
// shortToBytes
/**
* Converts the inShort to 2 bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the 2 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-2
*
* @param   inShort        source short
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void shortToBytes(short inShort, byte[] ioBytes, int inByteDelta) {
    ioBytes[inByteDelta]=(byte) (inShort>>>8);
    ioBytes[inByteDelta+1]=(byte) inShort;
  }

//--------------------------------------------------------------------------------------------------------
// charToBytes
/**
* Converts the inChar to 2 bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the 2 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-2
*
* @param   inChar        source char
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void charToBytes(char inChar, byte[] ioBytes, int inByteDelta) {
    ioBytes[inByteDelta]=(byte) (inChar>>>8);
    ioBytes[inByteDelta+1]=(byte) inChar;
  }

//--------------------------------------------------------------------------------------------------------
// charToVarBytes
/**
* Converts the inChar to inCharSize bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inCharSize destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inCharSize
*
* @param   inChar        source char
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inCharSize    number of destination bytes to use
*/
//--------------------------------------------------------------------------------------------------------

  public static void charToVarBytes(char inChar, byte[] ioBytes, int inByteDelta, int inCharSize) {
    if ((inCharSize<1)||(inCharSize>2))
      throw new RuntimeException("Only 1-2 bytes fit in an int");
    if (inCharSize==1) {
      ioBytes[inByteDelta]=(byte) inChar;
    } else {
      ioBytes[inByteDelta]=(byte) (inChar>>>8);
      ioBytes[inByteDelta+1]=(byte) inChar;
    }
  }

//--------------------------------------------------------------------------------------------------------
// intToBytes
/**
* Converts the inInt to 4 bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the 4 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-4
*
* @param   inInt         source int
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void intToBytes(int inInt, byte[] ioBytes, int inByteDelta) {
    int theInt=inInt;
    int theByteEnd=inByteDelta+kIntMemory;
    for (int i=theByteEnd-1; i>=inByteDelta; i--) {
      ioBytes[i]=(byte) theInt;
      theInt>>>=8;
    }
  }

//--------------------------------------------------------------------------------------------------------
// intToVarBytes
/**
* Converts the inInt to inIntSize bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inIntSize destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inIntSize
*
* @param   inInt         source int
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inIntSize     number of destination bytes to use
*/
//--------------------------------------------------------------------------------------------------------

  public static void intToVarBytes(int inInt, byte[] ioBytes, int inByteDelta, int inIntSize) {
    if ((inIntSize<1)||(inIntSize>4))
      throw new RuntimeException("Only 1-4 bytes fit in an int");
    int theInt=inInt;
    int theByteEnd=inByteDelta+inIntSize;
    for (int i=theByteEnd-1; i>=inByteDelta; i--) {
      ioBytes[i]=(byte) theInt;
      theInt>>>=8;
    }
  }

//--------------------------------------------------------------------------------------------------------
// longToBytes
/**
* Converts the inLong to 8 bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the 8 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-8
*
* @param   inLong        source long
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void longToBytes(long inLong, byte[] ioBytes, int inByteDelta) {
    long theLong=inLong;
    int theByteEnd=inByteDelta+kLongMemory;
    for (int i=theByteEnd-1; i>=inByteDelta; i--) {
      ioBytes[i]=(byte) theLong;
      theLong>>=8;
    }
  }

//--------------------------------------------------------------------------------------------------------
// longToVarBytes
/**
* Converts the inLong to inLongSize bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inLongSize destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inLongSize
*
* @param   inLong        source long
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inLongSize    number of destination bytes to use
*/
//--------------------------------------------------------------------------------------------------------

  public static void longToVarBytes(long inLong, byte[] ioBytes, int inByteDelta, int inLongSize) {
    if ((inLongSize<1)||(inLongSize>8))
      throw new RuntimeException("Only 1-8 bytes fit in a long");
    long theLong=inLong;
    for (int i=inByteDelta+inLongSize-1; i>=inByteDelta; i--) {
      ioBytes[i]=(byte) theLong;
      theLong>>=8;
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// floatToBytes
/**
* Converts the inFloat to 4 bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the 4 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-4
*
* @param   inFloat       source double
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
// Starts at inByteDelta.  Sets next 4 bytes of ioBytes
//--------------------------------------------------------------------------------------------------------

  public static void floatToBytes(float inFloat, byte[] ioBytes, int inByteDelta) {
    intToBytes(floatToInt(inFloat),ioBytes,inByteDelta); }

//--------------------------------------------------------------------------------------------------------
// floatToVarBytes
/**
* Converts the inFloat to inFloatSize bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inFloatSize destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inFloatSize
*
* @param   inFloat      source float
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inFloatSize  number of destination bytes to use
*/
//--------------------------------------------------------------------------------------------------------

  public static void floatToVarBytes(float inFloat, byte[] ioBytes, int inByteDelta, int inFloatSize) {
    doubleToVarBytes(inFloat,ioBytes,inByteDelta,inFloatSize); }

//--------------------------------------------------------------------------------------------------------
// doubleToBytes
/**
* Converts the inDouble to 8 bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the 8 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-8
*
* @param   inDouble      source double
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
// Starts at inByteDelta.  Sets next 8 bytes of ioBytes
//--------------------------------------------------------------------------------------------------------

  public static void doubleToBytes(double inDouble, byte[] ioBytes, int inByteDelta) {
    longToBytes(doubleToLong(inDouble),ioBytes,inByteDelta); }

//--------------------------------------------------------------------------------------------------------
// doubleToVarBytes
/**
* Converts the inDouble to inDoubleSize bytes in ioBytes starting at inByteDelta.  Contents of ioBytes
* are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inDoubleSize destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inDoubleSize
*
* @param   inDouble      source double
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inDoubleSize  number of destination bytes to use
*/
//--------------------------------------------------------------------------------------------------------

  public static void doubleToVarBytes(double inDouble, byte[] ioBytes, int inByteDelta, int inDoubleSize) {
    if ((inDoubleSize<3)||(inDoubleSize>8))
      throw new RuntimeException("Only 3-8 bytes fit in a double");
    long theLong=doubleToLong(inDouble);
    theLong>>>=(8*(8-inDoubleSize));
    int theByteEnd=inByteDelta+inDoubleSize;
    for (int i=theByteEnd-1; i>=inByteDelta; i--) {
      ioBytes[i]=(byte) theLong;
      theLong>>=8;
    }
  }




//========================================================================================================
//
// Conversions from array of native type with offset and length to bytes at offset
//
// These routines are fast because no arrays are allocated
//
// Input params are:
//   XXX[] inXXXs     source array of native type converted from
//   int inXXXDelta   offset into inXXXs where conversion should start
//   int inNXXXs      number of native types at inXXXDelta to use in conversion
//   byte[] ioBytes   destination bytes
//   int inByteDelta  offset into ioBytes where conversion should start
//
// No Output - part of ioBytes is overwritten
//
// If inXXXDelta is too big a RuntimeException will occur
// If inNXXXs is too big a RuntimeException will occur
// If ioBytes is too short a RuntimeException will occur
// If inByteDelta is too big a RuntimeException will occur
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// shortsToBytes
/**
* Converts inNShorts shorts in inShorts starting at inShortDelta to inNShorts*2 bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNShorts*2
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNShorts source shorts fall outside
* the inShorts array.  Therefore, inShortDelta must be between 0 and inShorts.length-inNShorts
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNShorts*2 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inNShorts*2
*
* @param   inShorts       source shorts
* @param   inShortDelta   offset into inShorts where conversion should start
* @param   inNShorts      number of shorts in inShorts to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void shortsToBytes(short[] inShorts, int inShortDelta, int inNShorts, byte[] ioBytes,
          int inByteDelta) {
    int theByteDelta=inByteDelta;
    int theEndShort=inShortDelta+inNShorts;
    for (int i=inShortDelta; i<theEndShort; i++) {
      shortToBytes(inShorts[i],ioBytes,theByteDelta);
      theByteDelta+=kShortMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// charsToBytes
/**
* Converts inNChars chars in inChars starting at inCharDelta to inNChars*2 bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNChars*2
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNChars source chars fall outside
* the inChars array.  Therefore, inCharDelta must be between 0 and inChars.length-inNChars
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNChars*2 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inNChars*2
*
* @param   inChars       source chars
* @param   inCharDelta   offset into inChars where conversion should start
* @param   inNChars      number of chars in inChars to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void charsToBytes(char[] inChars, int inCharDelta, int inNChars, byte[] ioBytes,
          int inByteDelta) {
    int theByteDelta=inByteDelta;
    int theEndChar=inCharDelta+inNChars;
    for (int i=inCharDelta; i<theEndChar; i++) {
      charToBytes(inChars[i],ioBytes,theByteDelta);
      theByteDelta+=kCharMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// charsToVarBytes
/**
* Converts inNChars chars in inChars starting at inCharDelta to inNChars*inCharSize bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNChars*inCharSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNChars source chars fall outside
* the inChars array.  Therefore, inCharDelta must be between 0 and inChars.length-inNChars
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNChars*inCharSize destination
* bytes fall outside the ioBytes array.  Therefore, inByteDelta must be between 0 and
* ioBytes.length-inNChars*inCharSize
*
* @param   inChars        source chars
* @param   inCharDelta    offset into inChars where conversion should start
* @param   inNChars       number of chars in inChars to use in conversion
* @param   ioBytes        destination byte array
* @param   inByteDelta    offset into ioBytes where conversion should start
* @param   inCharSize     number of destination bytes to use for each int
*/
//--------------------------------------------------------------------------------------------------------

  public static void charsToVarBytes(char[] inChars, int inCharDelta, int inNChars, byte[] ioBytes,
          int inByteDelta, int inCharSize) {
    int theByteDelta=inByteDelta;
    int theEndChar=inCharDelta+inNChars;
    for (int i=inCharDelta; i<theEndChar; i++) {
      charToVarBytes(inChars[i],ioBytes,theByteDelta,inCharSize);
      theByteDelta+=inCharSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// intsToBytes
/**
* Converts inNInts ints in inInts starting at inIntDelta to inNInts*4 bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNInts*4
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNInts source ints fall outside
* the inInts array.  Therefore, inIntDelta must be between 0 and inInts.length-inNInts
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNInts*4 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inNInts*4
*
* @param   inInts        source ints
* @param   inIntDelta    offset into inInts where conversion should start
* @param   inNInts       number of ints in inInts to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void intsToBytes(int[] inInts, int inIntDelta, int inNInts, byte[] ioBytes,
          int inByteDelta) {
    int theByteDelta=inByteDelta;
    int theEndInt=inIntDelta+inNInts;
    for (int i=inIntDelta; i<theEndInt; i++) {
      intToBytes(inInts[i],ioBytes,theByteDelta);
      theByteDelta+=kIntMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// intsToVarBytes
/**
* Converts inNInts ints in inInts starting at inIntDelta to inNInts*inIntSize bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNInts*inIntSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNInts source ints fall outside
* the inInts array.  Therefore, inIntDelta must be between 0 and inInts.length-inNInts
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNInts*inIntSize destination
* bytes fall outside the ioBytes array.  Therefore, inByteDelta must be between 0 and
* ioBytes.length-inNInts*inIntSize
*
* @param   inInts        source ints
* @param   inIntDelta    offset into inInts where conversion should start
* @param   inNInts       number of ints in inInts to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inIntSize     number of destination bytes to use for each int
*/
//--------------------------------------------------------------------------------------------------------

  public static void intsToVarBytes(int[] inInts, int inIntDelta, int inNInts, byte[] ioBytes,
          int inByteDelta, int inIntSize) {
    int theByteDelta=inByteDelta;
    int theEndInt=inIntDelta+inNInts;
    for (int i=inIntDelta; i<theEndInt; i++) {
      intToVarBytes(inInts[i],ioBytes,theByteDelta,inIntSize);
      theByteDelta+=inIntSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// longsToBytes
/**
* Converts inNLongs longs in inLongs starting at inLongDelta to inNLongs*8 bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNLongs*8
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNLongs source longs fall outside
* the inLongs array.  Therefore, inLongDelta must be between 0 and inLongs.length-inNLongs
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNLongs*8 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inNLongs*8
*
* @param   inLongs       source longs
* @param   inLongDelta   offset into inLongs where conversion should start
* @param   inNLongs      number of longs in inLongs to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void longsToBytes(long[] inLongs, int inLongDelta, int inNLongs, byte[] ioBytes,
          int inByteDelta) {
    int n=inByteDelta;
    int theEndLong=inLongDelta+inNLongs;
    for (int i=inLongDelta; i<theEndLong; i++) {
      long theLong=inLongs[i];
      ioBytes[n++]=(byte) (theLong>>>56);
      ioBytes[n++]=(byte) (theLong>>>48);
      ioBytes[n++]=(byte) (theLong>>>40);
      ioBytes[n++]=(byte) (theLong>>>32);
      ioBytes[n++]=(byte) (theLong>>>24);
      ioBytes[n++]=(byte) (theLong>>>16);
      ioBytes[n++]=(byte) (theLong>>>8);
      ioBytes[n++]=(byte)  theLong;
    }
  }

//--------------------------------------------------------------------------------------------------------
// longsToVarBytes
/**
* Converts inNLongs longs in inLongs starting at inLongDelta to inNLongs*inLongSize bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNLongs*inLongSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNLongs source longs fall outside
* the inLongs array.  Therefore, inLongDelta must be between 0 and inLongs.length-inNLongs
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNLongs*inLongSize destination
* bytes fall outside the ioBytes array.  Therefore, inByteDelta must be between 0 and
* ioBytes.length-inNLongs*inLongSize
*
* @param   inLongs       source longs
* @param   inLongDelta   offset into inLongs where conversion should start
* @param   inNLongs      number of longs in inLongs to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inLongSize    number of destination bytes to use for each long
*/
//--------------------------------------------------------------------------------------------------------

  public static void longsToVarBytes(long[] inLongs, int inLongDelta, int inNLongs, byte[] ioBytes,
          int inByteDelta, int inLongSize) {
    int theByteDelta=inByteDelta;
    int theEndLong=inLongDelta+inNLongs;
    for (int i=inLongDelta; i<theEndLong; i++) {
      longToVarBytes(inLongs[i],ioBytes,theByteDelta,inLongSize);
      theByteDelta+=inLongSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// floatsToBytes
/**
* Converts inNFloats floats in inFloats starting at inFloatDelta to inNFloats*4 bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNFloats*4
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNFloats source floats fall outside
* the inFloats array.  Therefore, inFloatDelta must be between 0 and inFloats.length-inNFloats
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNFloats*4 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inNFloats*4
*
* @param   inFloats       source floats
* @param   inFloatDelta   offset into inFloats where conversion should start
* @param   inNFloats      number of floats in inFloats to use in conversion
* @param   ioBytes        destination byte array
* @param   inByteDelta    offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void floatsToBytes(float[] inFloats, int inFloatDelta, int inNFloats, byte[] ioBytes,
          int inByteDelta) {
    int theByteDelta=inByteDelta;
    int theEndFloat=inFloatDelta+inNFloats;
    for (int i=inFloatDelta; i<theEndFloat; i++) {
      floatToBytes(inFloats[i],ioBytes,theByteDelta);
      theByteDelta+=kFloatMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// floatsToVarBytes
/**
* Converts inNFloats floats in inFloats starting at inFloatDelta to inNFloats*inFloatSize bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNFloats*inFloatSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNFloats source floats fall outside
* the inFloats array.  Therefore, inFloatDelta must be between 0 and inFloats.length-inNFloats
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNFloats*inFloatSize destination
* bytes fall outside the ioBytes array.  Therefore, inByteDelta must be between 0 and
* ioBytes.length-inNFloats*inFloatSize
*
* @param   inFloats       source floats
* @param   inFloatDelta   offset into inFloats where conversion should start
* @param   inNFloats      number of floats in inFloats to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inFloatSize    number of destination bytes to use for each float
*/
//--------------------------------------------------------------------------------------------------------

  public static void floatsToVarBytes(float[] inFloats, int inFloatDelta, int inNFloats, byte[] ioBytes,
          int inByteDelta, int inFloatSize) {
    int theByteDelta=inByteDelta;
    int theEndFloat=inFloatDelta+inNFloats;
    for (int i=inFloatDelta; i<theEndFloat; i++) {
      floatToVarBytes(inFloats[i],ioBytes,theByteDelta,inFloatSize);
      theByteDelta+=inFloatSize;
    }
  }

//--------------------------------------------------------------------------------------------------------
// doublesToBytes
/**
* Converts inNDoubles doubles in inDoubles starting at inDoubleDelta to inNDoubles*8 bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNDoubles*8
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNDoubles source doubles fall outside
* the inDoubles array.  Therefore, inDoubleDelta must be between 0 and inDoubles.length-inNDoubles
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNDoubles*8 destination bytes fall outside
* the ioBytes array.  Therefore, inByteDelta must be between 0 and ioBytes.length-inNDoubles*8
*
* @param   inDoubles       source doubles
* @param   inDoubleDelta   offset into inDoubles where conversion should start
* @param   inNDoubles      number of doubles in inDoubles to use in conversion
* @param   ioBytes         destination byte array
* @param   inByteDelta     offset into ioBytes where conversion should start
*/
//--------------------------------------------------------------------------------------------------------

  public static void doublesToBytes(double[] inDoubles, int inDoubleDelta, int inNDoubles, byte[] ioBytes,
          int inByteDelta) {
    int theByteDelta=inByteDelta;
    int theEndDouble=inDoubleDelta+inNDoubles;
    for (int i=inDoubleDelta; i<theEndDouble; i++) {
      doubleToBytes(inDoubles[i],ioBytes,theByteDelta);
      theByteDelta+=kDoubleMemory;
    }
  }

//--------------------------------------------------------------------------------------------------------
// doublesToVarBytes
/**
* Converts inNDoubles doubles in inDoubles starting at inDoubleDelta to inNDoubles*inDoubleSize bytes in
* ioBytes starting at inByteDelta.  Contents of ioBytes are overwritten.
* <p>
* Throws an ArrayOutOfBounds runtime exception if ioBytes too small.  Must have
* ioBytes.length >= inNDoubles*inDoubleSize
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNDoubles source doubles fall outside
* the inDoubles array.  Therefore, inDoubleDelta must be between 0 and inDoubles.length-inNDoubles
* <p>
* Throws an ArrayOutOfBounds runtime exception if any of the inNDoubles*inDoubleSize destination
* bytes fall outside the ioBytes array.  Therefore, inByteDelta must be between 0 and
* ioBytes.length-inNDoubles*inDoubleSize
*
* @param   inDoubles       source doubles
* @param   inDoubleDelta   offset into inDoubles where conversion should start
* @param   inNDoubles      number of doubles in inDoubles to use in conversion
* @param   ioBytes       destination byte array
* @param   inByteDelta   offset into ioBytes where conversion should start
* @param   inDoubleSize    number of destination bytes to use for each double
*/
//--------------------------------------------------------------------------------------------------------

  public static void doublesToVarBytes(double[] inDoubles, int inDoubleDelta, int inNDoubles, byte[] ioBytes,
          int inByteDelta, int inDoubleSize) {
    int theByteDelta=inByteDelta;
    int theEndDouble=inDoubleDelta+inNDoubles;
    for (int i=inDoubleDelta; i<theEndDouble; i++) {
      doubleToVarBytes(inDoubles[i],ioBytes,theByteDelta,inDoubleSize);
      theByteDelta+=inDoubleSize;
    }
  }
  



//========================================================================================================
//
// Conversions from native type to bytes
//
// These routines are slower because arrays are allocated
//
// Input params are:
//   XXX inXXX        source native type converted from
//
// Outputs array of bytes containing conversion
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// booleanToBytes
/**
* Converts the inBoolean into an array of 1 byte
*
* @param   inBoolean  source boolean to be converted
*
* @return  array of 1 byte containing the boolean value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] booleanToBytes(boolean inBoolean) {
    byte[] theBytes=new byte[kBooleanMemory];
    booleanToBytes(inBoolean,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// shortToBytes
/**
* Converts the inShort into an array of 2 bytes
*
* @param   inShort  source short to be converted
*
* @return  array of 2 bytes containing the short value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] shortToBytes(short inShort) {
    byte[] theBytes=new byte[kShortMemory];
    shortToBytes(inShort,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// charToBytes
/**
* Converts the inChar into an array of 2 bytes
*
* @param   inChar  source char to be converted
*
* @return  array of 2 bytes containing the char value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] charToBytes(char inChar) {
    byte[] theBytes=new byte[kCharMemory];
    charToBytes(inChar,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// charToVarBytes
/**
* Converts the inChar into an array of inCharSize bytes
*
* @param   inChar       source char to be converted
* @param   inCharSize   number of destination bytes to use
*
* @return  array of inCharSize  bytes containing the char value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] charToVarBytes(char inChar, int inCharSize) {
    byte[] theBytes=new byte[inCharSize];
    charToVarBytes(inChar,theBytes,0,inCharSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// intToBytes
/**
* Converts the inInt into an array of 4 bytes
*
* @param   inInt   source int to be converted
*
* @return  array of 4 bytes containing the int value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] intToBytes(int inInt) {
    byte[] theBytes=new byte[kIntMemory];
    intToBytes(inInt,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// intToVarBytes
/**
* Converts the inInt into an array of inIntSize bytes
*
* @param   inInt       source int to be converted
* @param   inIntSize   number of destination bytes to use
*
* @return  array of inIntSize  bytes containing the int value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] intToVarBytes(int inInt, int inIntSize) {
    byte[] theBytes=new byte[inIntSize];
    intToVarBytes(inInt,theBytes,0,inIntSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// longToBytes
/**
* Converts the inLong into an array of 8 bytes
*
* @param   inLong  source long to be converted
*
* @return  array of 8 bytes containing the long value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] longToBytes(long inLong) {
    byte[] theBytes=new byte[kLongMemory];
    longToBytes(inLong,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// longToVarBytes
/**
* Converts the inLong into an array of inLongSize bytes
*
* @param   inLong       source long to be converted
* @param   inLongSize   number of destination bytes to use
*
* @return  array of inLongSize bytes containing the long value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] longToVarBytes(long inLong, int inLongSize) {
    byte[] theBytes=new byte[inLongSize];
    longToVarBytes(inLong,theBytes,0,inLongSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// floatToBytes
/**
* Converts the inFloat into an array of 4 bytes
*
* @param   inFloat  source float to be converted
*
* @return  array of 4 bytes containing the float value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] floatToBytes(float inFloat) {
    byte[] theBytes=new byte[kFloatMemory];
    floatToBytes(inFloat,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// floatToVarBytes
/**
* Converts the inFloat into an array of inFloatSize bytes
*
* @param   inFloat       source float to be converted
* @param   inFloatSize   number of destination bytes to use
*
* @return  array of inFloatSize bytes containing the float value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] floatToVarBytes(float inFloat, int inFloatSize) {
    byte[] theBytes=new byte[inFloatSize];
    floatToVarBytes(inFloat,theBytes,0,inFloatSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// doubleToBytes
/**
* Converts the inDouble into an array of 8 bytes
*
* @param   inDouble  source double to be converted
*
* @return  array of 8 bytes containing the double value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] doubleToBytes(double inDouble) {
    byte[] theBytes=new byte[kDoubleMemory];
    doubleToBytes(inDouble,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// doubleToVarBytes
/**
* Converts the inDouble into an array of inDoubleSize bytes
*
* @param   inDouble       source double to be converted
* @param   inDoubleSize   number of destination bytes to use
*
* @return  array of inDoubleSize bytes containing the double value
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] doubleToVarBytes(double inDouble, int inDoubleSize) {
    byte[] theBytes=new byte[inDoubleSize];
    doubleToVarBytes(inDouble,theBytes,0,inDoubleSize);
    return theBytes;
  }




//========================================================================================================
//
// Conversions from array of native type to bytes
//
// These routines are slower because arrays are allocated
//
// Input params are:
//   XXX[] inXXXs     source array of native type converted from
//
// Outputs array of bytes containing conversion
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// shortsToBytes
/**
* Converts the inShorts array into an array of 2*inShorts.length bytes
*
* @param   inShorts  source shorts to be converted
*
* @return  array of 2*inShorts.length bytes containing the short values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] shortsToBytes(short[] inShorts) {
    byte[] theBytes=Allocate.newBytes(kShortMemory*inShorts.length);
    shortsToBytes(inShorts,0,inShorts.length,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// charsToBytes
/**
* Converts the inChars array into an array of 2*inChars.length bytes
*
* @param   inChars  source chars to be converted
*
* @return  array of 2*inChars.length bytes containing the char values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] charsToBytes(char[] inChars) {
    byte[] theBytes=Allocate.newBytes(kCharMemory*inChars.length);
    charsToBytes(inChars,0,inChars.length,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// charsToVarBytes
/**
* Converts the inChars array into an array of inCharSize*inChars.length bytes
*
* @param   inChars      source chars to be converted
* @param   inCharSize   number of destination bytes to use for each int
*
* @return  array of inCharSize*inChars.length bytes containing the int values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] charsToVarBytes(char[] inChars, int inCharSize) {
    byte[] theBytes=Allocate.newBytes(inCharSize*inChars.length);
    charsToVarBytes(inChars,0,inChars.length,theBytes,0,inCharSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// intsToBytes
/**
* Converts the inInts array into an array of 4*inInts.length bytes
*
* @param   inInts  source ints to be converted
*
* @return  array of 4*inInts.length bytes containing the int values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] intsToBytes(int[] inInts) {
    byte[] theBytes=Allocate.newBytes(kIntMemory*inInts.length);
    intsToBytes(inInts,0,inInts.length,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// intsToVarBytes
/**
* Converts the inInts array into an array of inIntSize*inInts.length bytes
*
* @param   inInts      source ints to be converted
* @param   inIntSize   number of destination bytes to use for each int
*
* @return  array of inIntSize*inInts.length bytes containing the int values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] intsToVarBytes(int[] inInts, int inIntSize) {
    byte[] theBytes=Allocate.newBytes(inIntSize*inInts.length);
    intsToVarBytes(inInts,0,inInts.length,theBytes,0,inIntSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// longsToBytes
/**
* Converts the inLongs array into an array of 8*inLongs.length bytes
*
* @param   inLongs  source longs to be converted
*
* @return  array of 8*inLongs.length bytes containing the long values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] longsToBytes(long[] inLongs) {
    byte[] theBytes=Allocate.newBytes(kLongMemory*inLongs.length);
    longsToBytes(inLongs,0,inLongs.length,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// longsToVarBytes
/**
* Converts the inLongs array into an array of inLongSize*inLongs.length bytes
*
* @param   inLongs      source longs to be converted
* @param   inLongSize   number of destination bytes to use for each long
*
* @return  array of inLongSize*inLongs.length bytes containing the long values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] longsToVarBytes(long[] inLongs, int inLongSize) {
    byte[] theBytes=Allocate.newBytes(inLongSize*inLongs.length);
    longsToVarBytes(inLongs,0,inLongs.length,theBytes,0,inLongSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// floatsToBytes
/**
* Converts the inFloats array into an array of 4*inFloats.length bytes
*
* @param   inFloats  source floats to be converted
*
* @return  array of 4*inFloats.length bytes containing the float values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] floatsToBytes(float[] inFloats) {
    byte[] theBytes=Allocate.newBytes(kFloatMemory*inFloats.length);
    floatsToBytes(inFloats,0,inFloats.length,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// floatsToVarBytes
/**
* Converts the inFloats array into an array of inFloatSize*inFloats.length bytes
*
* @param   inFloats      source floats to be converted
* @param   inFloatSize   number of destination bytes to use for each float
*
* @return  array of inFloatSize*inFloats.length bytes containing the float values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] floatsToVarBytes(float[] inFloats, int inFloatSize) {
    byte[] theBytes=Allocate.newBytes(inFloatSize*inFloats.length);
    floatsToVarBytes(inFloats,0,inFloats.length,theBytes,0,inFloatSize);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// doublesToBytes
/**
* Converts the inDoubles array into an array of 8*inDoubles.length bytes
*
* @param   inDoubles  source doubles to be converted
*
* @return  array of 8*inDoubles.length bytes containing the double values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] doublesToBytes(double[] inDoubles) {
    byte[] theBytes=Allocate.newBytes(kDoubleMemory*inDoubles.length);
    doublesToBytes(inDoubles,0,inDoubles.length,theBytes,0);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// doublesToVarBytes
/**
* Converts the inDoubles array into an array of inDoubleSize*inDoubles.length bytes
*
* @param   inDoubles      source doubles to be converted
* @param   inDoubleSize   number of destination bytes to use for each double
*
* @return  array of inDoubleSize*inDoubles.length bytes containing the double values
*/
//--------------------------------------------------------------------------------------------------------

  public static byte[] doublesToVarBytes(double[] inDoubles, int inDoubleSize) {
    byte[] theBytes=Allocate.newBytes(inDoubleSize*inDoubles.length);
    doublesToVarBytes(inDoubles,0,inDoubles.length,theBytes,0,inDoubleSize);
    return theBytes;
  }




//========================================================================================================
//
// Conversions of longs to/from doubles
//
// Bit  NBits  Mask                 Purpose
//  63     1   0x8000000000000000L  sign 
// 62-52  11   0x7ff0000000000000L  exponent (stored with a bias of +1023, subtract 1023 to get value)
// 51-0   52   0x000fffffffffffffL  mantissa
// 
// Positive infinity:  0x7ff0000000000000L = {+,2047,0)
// Negative infinity:  0xfff0000000000000L = {-,2047,0)
// All other NaNs*:    0x7ff8000000000000L = {+,2047,1)
//
// * NaNs differ in mantissa and are collapsed by doubleToLong to canonical NaN with zero mantissa
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// longToDouble
/**
* Converts long into a double
*
* @see Conversions#doubleToLong
*
* @param   inLong    source long
*
* @return  double made from the bits in the source long
*/
//--------------------------------------------------------------------------------------------------------

  public static double longToDouble(long inLong) {
    return Double.longBitsToDouble(inLong); }

//--------------------------------------------------------------------------------------------------------
// doubleToLong
/**
* Converts double into a long
*
* @see Conversions#longToDouble
*
* @param   inDouble    source double
*
* @return  long made from the bits in the source double
*/
//--------------------------------------------------------------------------------------------------------

  public static long doubleToLong(double inDouble) {
    return Double.doubleToLongBits(inDouble); }




//========================================================================================================
//
// Conversions of ints to/from floats
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// intToFloat
/**
* Converts int into a float
*
* @see Conversions#floatToInt
*
* @param   inInt    source int
*
* @return  float made from the bits in the source int
*/
//--------------------------------------------------------------------------------------------------------

  public static float intToFloat(int inInt) {
    return Float.intBitsToFloat(inInt); }

//--------------------------------------------------------------------------------------------------------
// floatToInt
/**
* Converts float into a int
*
* @see Conversions#intToFloat
*
* @param   inFloat    source float
*
* @return  int made from the bits in the source float
*/
//--------------------------------------------------------------------------------------------------------

  public static int floatToInt(float inFloat) {
    return Float.floatToIntBits(inFloat); }




//========================================================================================================
//
// Bit access routines
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// getBitInByte
/**
* Extracts the inIndexth of 8 boolean bits that make up inByte
*
* @see Conversions#setBitInByte
*
* @param   inByte    the source byte
* @param   inIndex   which bit to get
*
* @return  the inIndex bit in inByte
*/
//--------------------------------------------------------------------------------------------------------

  public static boolean getBitInByte(byte inByte, int inIndex) {
    return (inByte&kBooleanFlags[inIndex%8])!=0; }

//--------------------------------------------------------------------------------------------------------
// setBitInByte
/**
* Sets the inIndexth of 8 bits in inByte to inBoolean
*
* @see Conversions#getBitInByte
*
* @param   inByte      the destination byte
* @param   inIndex     which bit to set
* @param   inBoolean   what to set the bit to
*
* @return  inByte with the inIndexth bit set to inBoolean
*/
//--------------------------------------------------------------------------------------------------------

  public static byte setBitInByte(byte inByte, int inIndex, boolean inBoolean) {
    if (inBoolean)
      return (byte) (inByte|kBooleanFlags[inIndex%8]);
    else
      return (byte) (inByte&kBooleanMasks[inIndex%8]);
  }

  
  
  
//========================================================================================================
//
// VarChar size routines
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// calcVarCharSize
/**
* Counts number of bytes required to hold inChar in VarChar encoding.
*
* @param   inChar       source char
*
* @return  the number, N, of bytes required to hold the VarChar encoding of inChar.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarCharSize(char inChar) {
    if (inChar>0x000000ff) 
      return 2;
    else
      return 1;
  }

//--------------------------------------------------------------------------------------------------------
// calcVarCharSize
/**
* Counts number of bytes required to hold inNChars in inChar starting at inCharDelta in VarChar encoding.
*
* @param   inChars       source chars
* @param   inCharDelta   offset into inChars where conversion should start
* @param   inNChars      number of chars in inChars to use in conversion
*
* @return  the number, N, of bytes required to hold the VarChar encoding of inNChars in inChar
* starting at inCharDelta.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarCharSize(char[] inChars, int inCharDelta, int inNChars) {
    char theMaxChar=0;
    int theCharEnd=inCharDelta+inNChars;
    for (int i=inCharDelta; i<theCharEnd; i++) {
      int theChar=inChars[i];
      theMaxChar=(char) Math.max(theMaxChar,theChar);
    }
    return calcVarCharSize(theMaxChar);
  }

//--------------------------------------------------------------------------------------------------------
// calcVarCharSize
/**
* Counts number of bytes required to hold inChars in VarChar encoding.
*
* @param   inChars       source chars
*
* @return  the number, N, of bytes required to hold the VarChar encoding of inChars.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarCharSize(char[] inChars) {
    return calcVarCharSize(inChars,0,inChars.length); }




//========================================================================================================
//
// VarInt size routines
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// calcVarIntSize
/**
* Counts number of bytes required to hold inInt in VarInt encoding.
*
* @param   inInt       source int
*
* @return  the number, N, of bytes required to hold the VarInt encoding of inInt.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarIntSize(int inInt) {
    if ((inInt>0x00007fff)||(inInt<0xffff8000)) {
      if ((inInt>0x007fffff)||(inInt<0xff800000))
        return 4;
      else
        return 3;
    } else {
      if ((inInt>0x0000007f)||(inInt<0xffffff80))
        return 2;
      else
        return 1;
    }
  }

//--------------------------------------------------------------------------------------------------------
// calcVarIntSize
/**
* Counts number of bytes required to hold inNInts in inInt starting at inIntDelta in VarInt encoding.
*
* @param   inInts       source ints
* @param   inIntDelta   offset into inInts where conversion should start
* @param   inNInts      number of ints in inInts to use in conversion
*
* @return  the number, N, of bytes required to hold the VarInt encoding of inNInts in inInt
* starting at inIntDelta.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarIntSize(int[] inInts, int inIntDelta, int inNInts) {
    int theMinInt=0;
    int theMaxInt=0;
    int theIntEnd=inIntDelta+inNInts;
    for (int i=inIntDelta; i<theIntEnd; i++) {
      int theInt=inInts[i];
      theMinInt=Math.min(theMinInt,theInt);
      theMaxInt=Math.max(theMaxInt,theInt);
    }
    if (theMinInt<-theMaxInt)
      return calcVarIntSize(theMinInt);
    else
      return calcVarIntSize(theMaxInt);
  }

//--------------------------------------------------------------------------------------------------------
// calcVarIntSize
/**
* Counts number of bytes required to hold inInts in VarInt encoding.
*
* @param   inInts       source ints
*
* @return  the number, N, of bytes required to hold the VarInt encoding of inInts.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarIntSize(int[] inInts) {
    return calcVarIntSize(inInts,0,inInts.length); }




//========================================================================================================
//
// VarLong size routines
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// calcVarLongSize
/**
* Counts number of bytes required to hold inLong in VarLong encoding.
*
* @param   inLong       source long
*
* @return  the number, N, of bytes required to hold the VarLong encoding of inLong.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarLongSize(long inLong) {
    if ((inLong>0x000000007fffffffL)||(inLong<0xffffffff80000000L)) {
      if ((inLong>0x00007fffffffffffL)||(inLong<0xffff800000000000L)) {
        if ((inLong>0x007fffffffffffffL)||(inLong<0xff80000000000000L))
          return 8;
        else
          return 7;
      } else {
        if ((inLong>0x0000007fffffffffL)||(inLong<0xffffff8000000000L))
          return 6;
        else
          return 5;
      }
    } else {
      if ((inLong>0x0000000000007fffL)||(inLong<0xffffffffffff8000L)) {
        if ((inLong>0x00000000007fffffL)||(inLong<0xffffffffff800000L))
          return 4;
        else
          return 3;
      } else {
        if ((inLong>0x000000000000007fL)||(inLong<0xffffffffffffff80L))
          return 2;
        else
          return 1;
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// calcVarLongSize
/**
* Counts number of bytes required to hold inNLongs in inLong starting at inLongDelta in VarLong encoding.
*
* @param   inLongs       source longs
* @param   inLongDelta   offset into inLongs where conversion should start
* @param   inNLongs      number of longs in inLongs to use in conversion
*
* @return  the number, N, of bytes required to hold the VarLong encoding of inNLongs in inLong
* starting at inLongDelta.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarLongSize(long[] inLongs, int inLongDelta, int inNLongs) {
    long theMinLong=0;
    long theMaxLong=0;
    int theLongEnd=inLongDelta+inNLongs;
    for (int i=inLongDelta; i<theLongEnd; i++) {
      long theLong=inLongs[i];
      theMinLong=Math.min(theMinLong,theLong);
      theMaxLong=Math.max(theMaxLong,theLong);
    }
    if (theMinLong<-theMaxLong)
      return calcVarLongSize(theMinLong);
    else
      return calcVarLongSize(theMaxLong);
  }

//--------------------------------------------------------------------------------------------------------
// calcVarLongSize
/**
* Counts number of bytes required to hold inLongs in VarLong encoding.
*
* @param   inLongs       source longs
*
* @return  the number, N, of bytes required to hold the VarLong encoding of inLongs.
**/
//--------------------------------------------------------------------------------------------------------

  public static int calcVarLongSize(long[] inLongs) {
    return calcVarLongSize(inLongs,0,inLongs.length); }

  
  
  
  
//--------------------------------------------------------------------------------------------------------
// calcVarDoubleSize
//
// Leading 12 bits are sign and exponent.  Trailing 52 bits are mantissa.  Shorten long reduces precision
/**
 *     Size   Mantissa   Digits
 *   3 bytes   12 bits     3
 *   4 bytes   20 bits     6
 *   5 bytes   28 bits     8
 *   6 bytes   36 bits     10
 *   7 bytes   44 bits     13
 *   8 bytes   52 bits     15
**/
//--------------------------------------------------------------------------------------------------------

}





