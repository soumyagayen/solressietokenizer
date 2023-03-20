//--------------------------------------------------------------------------------------------------------
// EscapeUtils
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// EscapeUtils
//--------------------------------------------------------------------------------------------------------

public class EscapeUtils implements Constants {

//------------------------------------------------------------------------------------------------------
// escapeOps
//------------------------------------------------------------------------------------------------------

  public static String escapeOps(String inText, char inEscapeChar, String inCharOperators) {
    if (inText==null)
      return null;
    int theLength=inText.length();
    StringBuffer theBuffer=new StringBuffer(theLength+16);
    for (int i=0; i<theLength; i++) {
      char theChar=inText.charAt(i);
      if ((theChar==inEscapeChar)||(inCharOperators.indexOf(theChar)>=0))
        theBuffer.append(inEscapeChar);
      theBuffer.append(theChar);
    }
    return theBuffer.toString();
  }

//------------------------------------------------------------------------------------------------------
// isEscaped
//
// For case of escaping | with \
//   1)  xx|xx      Not escaped
//   2)  |xxxx      Not escaped
//   3)  x\|xx      Escaped
//   4)  x\\|xx     Not escaped
//   5)  \\\|xx     Escaped
//------------------------------------------------------------------------------------------------------

  public static boolean isEscaped(int inCharN, String inText, char inEscapeChar) {
    int theCharN=inCharN;
    int theNEscapes=0;
    while ((theCharN>0)&&((inText.charAt(theCharN-1)==inEscapeChar))) {
      theCharN--;
      theNEscapes++;
    }
    return (theNEscapes%2==1);  // FindBugs complains about this, but is wrong 
  }

//------------------------------------------------------------------------------------------------------
// stripEscapes
//------------------------------------------------------------------------------------------------------

  public static String stripEscapes(String inText, char inEscapeChar) {
    if (inText==null)
      return null;
    int theLength=inText.length();
    StringBuffer theBuffer=new StringBuffer(theLength);
    char theLastChar=' ';
    for (int i=0; i<theLength; i++) {
      char theChar=inText.charAt(i);
      if ((theChar!=inEscapeChar)||(theLastChar==inEscapeChar))
        theBuffer.append(theChar);
      if ((theChar==inEscapeChar)&&(theLastChar==inEscapeChar))
        theLastChar=' ';
      else
        theLastChar=theChar;
    }
    return theBuffer.toString();
  }

//------------------------------------------------------------------------------------------------------
// escapeHTML
//------------------------------------------------------------------------------------------------------

  public static void escapeHTML(String inString, StringBuffer inBuffer) {
    if ((inString==null)||(inString.length()==0))
      return;
    for (int i=0; i<inString.length(); i++) {
      char theChar=inString.charAt(i);
      switch (theChar) {
        case '<': { inBuffer.append("&lt;"); break; }
        case '>': { inBuffer.append("&gt;"); break; }
        case '&': { inBuffer.append("&amp;"); break; }
        case '"': { inBuffer.append("&quot;"); break; }
        case '\'':{ inBuffer.append("&apos;"); break; }
        case '\\':{ inBuffer.append("&#92;"); break; }
        case '\t':
        case '\n':
        case '\r': { inBuffer.append(theChar); break; }
        default:
          if (theChar>0x7f)
            inBuffer.append("&#").append((int) theChar).append(';');
          else if (theChar>=' ')       // Note: escape chars dropped
            inBuffer.append(theChar);
      }
    }
  }

  public static String escapeHTML(String inString) {
    if (inString==null)
      return null;
    StringBuffer theBuffer=new StringBuffer(inString.length()+128);
    escapeHTML(inString,theBuffer);
    return theBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// unescapeHTML
//--------------------------------------------------------------------------------------------------------

  public static void unescapeHTML(StringBuffer inBuffer, String inText) {
    if ((inText==null)||(inText.length()==0))
      return;

    int i=0;
    char[] theChars=inText.toCharArray();
    while (i<theChars.length) {
      char theChar=theChars[i];

      if (theChar!='&')
        inBuffer.append(theChar);

      else {
        if ((theChars[i+1]=='#')&&(theChars[i+2]>='0')&&(theChars[i+2]<='9')) {
          boolean theOK=false;
          int theN=(theChars[i+2]-'0');
          for (int j=3; j<10; j++) {
            char theChar2=theChars[i+j];
            if (theChar2==';') {
              inBuffer.append((char) theN);
              i+=j;
              theOK=true;
              break;
            } else if ((theChar2>='0')&&(theChar2<='9'))
              theN=theN*10+(theChar2-'0');
            else
              break;
          }
          if (!theOK) {
            inBuffer.append('&');
            i++;
          }
        } else if ((i<theChars.length-3)&&(theChars[i+2]=='t')&&(theChars[i+3]==';')) {
          if (theChars[i+1]=='l') {
            inBuffer.append('<');
            i+=3;
          } else if (theChars[i+1]=='g') {
            inBuffer.append('>');
            i+=3;
          } else {
            inBuffer.append('&');
            i++;
          }
        } else if ((i<theChars.length-4)&&(theChars[i+1]=='a')&&(theChars[i+2]=='m')&&
                (theChars[i+3]=='p')&&(theChars[i+4]==';')) {
          inBuffer.append('&');
          i+=4;
        } else if ((i<theChars.length-5)&&(theChars[i+3]=='o')) {
          if ((theChars[i+1]=='q')&&(theChars[i+2]=='u')&&(theChars[i+4]=='t')&&(theChars[i+5]==';')) {
            inBuffer.append('"');
            i+=5;
          } else if ((theChars[i+1]=='a')&&(theChars[i+2]=='p')&&(theChars[i+4]=='s')&&
                  (theChars[i+5]==';')) {
            inBuffer.append('\'');
            i+=5;
          } else {
            inBuffer.append('&');
            i++;
          }
        } else {
          inBuffer.append('&');
          i++;
        }
      }

      i++;
    }
  }

  public static String unescapeHTML(String inText) {
    if (inText==null)
      return null;
    StringBuffer theBuffer=new StringBuffer(inText.length());
    unescapeHTML(theBuffer,inText);
    return theBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// escapeXML
//--------------------------------------------------------------------------------------------------------

  public static void escapeXML(StringBuffer inBuffer, String inText) {
    if ((inText==null)||(inText.length()==0))
      return;
    int theLength=inText.length();
    inBuffer.ensureCapacity(inBuffer.length()+theLength+16);
    for (int i=0; i<theLength; i++) {
      char theChar=inText.charAt(i);
      switch (theChar) {
        case '<': { inBuffer.append("&lt;"); break; }
        case '>': { inBuffer.append("&gt;"); break; }
        case '&': { inBuffer.append("&amp;"); break; }
        case '"': { inBuffer.append("&quot;"); break; }
        
// ### Added 2020_10_23 - should be safe
        
        // XML Parser sometimes converts '\r' into '\n'
        // To preserve '\r', replace with char entity &#xD;
        case '\r': { inBuffer.append("&#xD;"); break; }

        case '\t':
        case '\n': { inBuffer.append(theChar); break; }
        default:
          if (theChar>=' ')       // Note: control chars dropped
            inBuffer.append(theChar);
      }
    }
  }
 
  public static String escapeXML(String inText) {
    if (inText==null)
      return null;
    StringBuffer theBuffer=new StringBuffer(inText.length()+128);
    escapeXML(theBuffer,inText);
    return theBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// unescapeXML
//--------------------------------------------------------------------------------------------------------

  public static void unescapeXML(StringBuffer inBuffer, String inText) {
    if ((inText==null)||(inText.length()==0))
      return;
    int i=0;
    int theLength=inText.length();
    while (i<theLength) {
      char theChar=inText.charAt(i);
      if (theChar!='&')
        inBuffer.append(theChar);
      else {
        if ((i<theLength-3)&&(inText.charAt(i+2)=='t')&&(inText.charAt(i+3)==';')) {
          if (inText.charAt(i+1)=='l') {
            inBuffer.append('<');
            i+=3;
          } else if (inText.charAt(i+1)=='g') {
            inBuffer.append('>');
            i+=3;
          } else {
            inBuffer.append('&');
            i++;
          }
        } else if ((i<theLength-4)&&(inText.charAt(i+1)=='a')&&(inText.charAt(i+2)=='m')&&
            (inText.charAt(i+3)=='p')&&(inText.charAt(i+4)==';')) {
          inBuffer.append('&');
          i+=4;
        } else if ((i<theLength-5)&&(inText.charAt(i+3)=='o')) {
          if ((inText.charAt(i+1)=='q')&&(inText.charAt(i+2)=='u')&&(inText.charAt(i+4)=='t')&&
              (inText.charAt(i+5)==';')) {
            inBuffer.append('"');
            i+=5;
          } else if ((inText.charAt(i+1)=='a')&&(inText.charAt(i+2)=='p')&&(inText.charAt(i+4)=='s')&&
              (inText.charAt(i+5)==';')) {
            inBuffer.append('\'');
            i+=5;
          } else {
            inBuffer.append('&');
            i++;
          }
        } else {
          inBuffer.append('&');
          i++;
        }
      }
      i++;
    }
  }

  public static String unescapeXML(String inText) {
    if (inText==null)
      return null;
    StringBuffer theBuffer=new StringBuffer(inText.length());
    unescapeXML(theBuffer,inText);
    return theBuffer.toString();
  }

}



































