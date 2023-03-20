//--------------------------------------------------------------------------------------------------------
// TextFileUtils.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.io.*;

//--------------------------------------------------------------------------------------------------------
// TextFileUtils
//--------------------------------------------------------------------------------------------------------

public abstract class TextFileUtils extends FileUtils {

//--------------------------------------------------------------------------------------------------------
// loadTextFile
//--------------------------------------------------------------------------------------------------------

  public static String loadTextFile(String inFilename, String inEncoding) throws IOException {
    return new String(loadBinaryFile(inFilename),inEncoding); }

  public static String loadTextFile(String inFilename) throws IOException {
    return loadTextFile(inFilename,"UTF-8"); }

//--------------------------------------------------------------------------------------------------------
// saveTextFile
//--------------------------------------------------------------------------------------------------------

  public static void saveTextFile(String inFilename, String inText, String inEncoding) throws IOException {
    saveBinaryFile(inText.getBytes(inEncoding),inFilename); }

  public static void saveTextFile(String inFilename, String inText) throws IOException {
    saveTextFile(inFilename,inText,"UTF-8"); }

//--------------------------------------------------------------------------------------------------------
// appendTextFile
//--------------------------------------------------------------------------------------------------------

  public static void appendTextFile(String inFilename, String inText, String inEncoding) throws IOException {
    appendBinaryFile(inText.getBytes(inEncoding),inFilename); }

  public static void appendTextFile(String inFilename, String inText) throws IOException {
    appendTextFile(inFilename,inText,"UTF-8"); }

//--------------------------------------------------------------------------------------------------------
// openUTF8Reader
//--------------------------------------------------------------------------------------------------------

  public static BufferedReader openUTF8Reader(String inFilename, long inOffset) throws IOException {
    BufferedInputStream theStream;
    if (inOffset==0)
      theStream=openBufferedInputStream(inFilename);
    else {
      // Open one byte before desired offset and read that byte
      theStream=openBufferedInputStream(inFilename,inOffset-1);
      int theByte=theStream.read();
      // If at end of file, offset was too big
      if (theByte==kNotFound) {
        theStream.close();
        throw new RuntimeException("Invalid Offset: "+inOffset);
      }
      // If byte is not the start of char or end of file, continue to read
      while (true) {        
        if (((theByte&0x00c0)!=0x0080)||(theByte==kNotFound))
          break;
        theByte=theStream.read();
      }
    }
    return new BufferedReader(new InputStreamReader(theStream,"UTF-8")); 
  }

  public static BufferedReader openUTF8Reader(String inFilename) throws IOException {
    return openUTF8Reader(inFilename,0); }

//--------------------------------------------------------------------------------------------------------
// openUTF8Writer
//--------------------------------------------------------------------------------------------------------

  public static BufferedWriter openUTF8Writer(String inFilename, boolean inAppend) throws IOException {
    return new BufferedWriter(
        new OutputStreamWriter(
            openBufferedOutputStream(inFilename,inAppend),"UTF-8")); 
  }

  public static BufferedWriter openUTF8Writer(String inFilename) throws IOException {
    return openUTF8Writer(inFilename,false); }

  public static PrintWriter openUTF8PrintWriter(String inFilename) throws IOException {
    return new PrintWriter(openUTF8Writer(inFilename,false)); }

//--------------------------------------------------------------------------------------------------------
// textHasPCEOL
//--------------------------------------------------------------------------------------------------------

  public static boolean textHasPCEOL(String inText) { return (inText.indexOf("\r\n")!=kNotFound); }

//--------------------------------------------------------------------------------------------------------
// textPC2UnixEOLs
//--------------------------------------------------------------------------------------------------------

  public static String textPC2UnixEOLs(String inText) {
    int theStartPos=0;
    int theEndPos=inText.indexOf("\r\n",theStartPos);
    if (theEndPos==kNotFound)
      return inText;
    StringBuffer theBuffer=new StringBuffer(); 
    while (theEndPos!=kNotFound) {
      theBuffer.append(inText.substring(theStartPos,theEndPos));
      theStartPos=theEndPos+1;  // Skip \r but not \n
      theEndPos=inText.indexOf("\r\n",theStartPos);
    }
    theBuffer.append(inText.substring(theStartPos));
    return theBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// filePC2UnixEOLs
//--------------------------------------------------------------------------------------------------------

  public static void filePC2UnixEOLs(String inFilename) throws IOException {
    String theText=loadTextFile(inFilename,"UTF-8");
    theText=textPC2UnixEOLs(theText);
    saveTextFile(inFilename,theText);
  }

//--------------------------------------------------------------------------------------------------------
// textUnix2PCEOLs
//--------------------------------------------------------------------------------------------------------

  public static String textUnix2PCEOLs(String inText) {
    int theStartPos=0;
    int theEndPos=inText.indexOf("\n",theStartPos);
    if (theEndPos==kNotFound)
      return inText;
    StringBuffer theBuffer=new StringBuffer(); 
    while (theEndPos!=kNotFound) {
      theBuffer.append(inText.substring(theStartPos,theEndPos));
      theBuffer.append('\r');  // Insert \r before \n
      theStartPos=theEndPos+1;
      theEndPos=inText.indexOf("\n",theStartPos);
    }
    theBuffer.append(inText.substring(theStartPos));
    return theBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// fileUnix2PCEOLs
//--------------------------------------------------------------------------------------------------------

  public static void fileUnix2PCEOLs(String inFilename) throws IOException {
    String theText=loadTextFile(inFilename,"UTF-8");
    theText=textUnix2PCEOLs(theText);
    saveTextFile(inFilename,theText);
  }

}
