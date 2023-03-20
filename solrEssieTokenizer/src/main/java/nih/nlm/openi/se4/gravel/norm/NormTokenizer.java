//--------------------------------------------------------------------------------------------------------
// NormTokenizer.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// NormTokenizer
//--------------------------------------------------------------------------------------------------------

public class NormTokenizer implements Constants {

//--------------------------------------------------------------------------------------------------------
// isWhitespace
//--------------------------------------------------------------------------------------------------------

  public boolean isWhitespace(char inChar) { return CharNorm.isWhitespace(inChar); } 

//--------------------------------------------------------------------------------------------------------
// isLetter
//--------------------------------------------------------------------------------------------------------

  public boolean isLetter(char inChar) { return CharNorm.isLetter(inChar); }

//--------------------------------------------------------------------------------------------------------
// isUpperCaseLetter
//--------------------------------------------------------------------------------------------------------

  public boolean isUpperCaseLetter(char inChar) { 
    return (CharNorm.isLetter(inChar)&&CharNorm.isUpperCaseLetter(inChar)); }

//--------------------------------------------------------------------------------------------------------
// isLowerCaseLetter
//--------------------------------------------------------------------------------------------------------

  public boolean isLowerCaseLetter(char inChar) { 
    return (CharNorm.isLetter(inChar)&&CharNorm.isLowerCaseLetter(inChar)); }

//--------------------------------------------------------------------------------------------------------
// isDigit
//--------------------------------------------------------------------------------------------------------

  public boolean isDigit(char inChar) { return CharNorm.isDigit(inChar); }

//--------------------------------------------------------------------------------------------------------
// isPunctuation
//--------------------------------------------------------------------------------------------------------

  public boolean isPunctuation(char inChar) {
    return ((!isWhitespace(inChar))&&(!isLetter(inChar))&&(!isDigit(inChar))); }

//--------------------------------------------------------------------------------------------------------
// isQuote
//--------------------------------------------------------------------------------------------------------

  public boolean isQuote(char inChar) { return CharNorm.isQuote(inChar); }

//--------------------------------------------------------------------------------------------------------
// isBracket
//--------------------------------------------------------------------------------------------------------

  public boolean isBracket(char inChar) { return CharNorm.isBracket(inChar); }

//--------------------------------------------------------------------------------------------------------
// isBullet
//--------------------------------------------------------------------------------------------------------

  public boolean isBullet(char inChar) { return CharNorm.isBullet(inChar); }

//--------------------------------------------------------------------------------------------------------
// findEndOfWhitespace
//
// Can be overridden by subclass, but should be OK as is.
//
// We are assuming that all non-whitespace characters are part of tokens (that is the standard
//   definition of white space).
// Also assuming that whitespace always separates tokens, but not vice versa (i.e. given "ab", a and b
//   may be separate tokens, but given "x y", x and y must be separate tokens.
//--------------------------------------------------------------------------------------------------------

  public int findEndOfWhitespace(char[] inChars, int inCharN, int inEndCharN) {
    int theCharN=inCharN;
    while ((theCharN<inEndCharN)&&(isWhitespace(inChars[theCharN]))) 
      theCharN++;
    return theCharN;
  }

//-------------------------------------------------------------------------------------------------------- 
// skipHyphens
//
// Hyphens are so optional that they are almost whitespace - usually want to see them, but ignore them
//--------------------------------------------------------------------------------------------------------

  public int skipHyphens(char[] inChars, int inCharN, int inEndCharN) {
    int theCharN=inCharN;
    while (theCharN<inEndCharN) {
      char theChar=inChars[theCharN];
      if (theChar!='-')
        break;
      theCharN++;
    }
    return theCharN;
  }

//-------------------------------------------------------------------------------------------------------- 
// findEndOfWord
//--------------------------------------------------------------------------------------------------------

  public int findEndOfWord(char[] inChars, int inCharN, int inEndCharN) {
    int theCharN=inCharN+1;
    while (theCharN<inEndCharN) {
      char theChar=inChars[theCharN];
      if (!(isLetter(theChar)||(theChar=='\'')))
        break;
      theCharN++;
    }
    return theCharN;
  }
  
//-------------------------------------------------------------------------------------------------------- 
// findEndOfNumber
//--------------------------------------------------------------------------------------------------------

  public int findEndOfNumber(char[] inChars, int inCharN, int inEndCharN) {
    int theCharN=inCharN+1;
    while (theCharN<inEndCharN) {
      char theChar=inChars[theCharN];
      if (!isDigit(theChar))
        break;
      theCharN++;
    }
   
    // Optionally include comma and more digits
    if ((theCharN<inEndCharN)&&(inChars[theCharN]==',')) {
      int theLastGoodCharN=theCharN;
      
      // Look for occrs of comma followed by 3 digits
      boolean theOK=true;        
      while ((theOK)&&(theCharN<inEndCharN)&&(inChars[theCharN]==',')) {
        theCharN++; // Found comma
        for (int i=0; i<3; i++) {
          if ((theCharN==inEndCharN)||(!isDigit(inChars[theCharN]))) {
            theOK=false;  // Bad digit
            break;
          }
          theCharN++; // Found 1 of 3 digits
        }
        // A 4th digit breaks format
        if ((theOK)&&(theCharN<inEndCharN)&&(isDigit(inChars[theCharN])))
          theOK=false;
        if (theOK)
          theLastGoodCharN=theCharN;
      }
      
      // if poorly formatted, probably not a number, may be a list, so backup to start
      if (!theOK)
        theCharN=theLastGoodCharN;
    }

    // Optionally include decimal point and more digits
    if ((theCharN<inEndCharN)&&(inChars[theCharN]=='.')) {
      theCharN++;
      if ((theCharN<inEndCharN)&&(isDigit(inChars[theCharN]))) {
        theCharN++;
        while (theCharN<inEndCharN) {
          char theChar=inChars[theCharN];
          if (!isDigit(theChar))
            break;
          theCharN++;
        }
      }
    }
    return theCharN;
  }
  
//-------------------------------------------------------------------------------------------------------- 
// findRunOfPunct
//--------------------------------------------------------------------------------------------------------

  public int findRunOfPunct(char[] inChars, int inCharN, int inEndCharN) {
    char thePunctChar=inChars[inCharN];
    int theCharN=inCharN+1;
    while (theCharN<inEndCharN) {           
      char theChar=inChars[theCharN];
      if (theChar!=thePunctChar)
        break;
      theCharN++;
    }
    return theCharN;
  }
    
//-------------------------------------------------------------------------------------------------------- 
// findEndOfToken
//
// Assuming that chars already run through char norm, so only need deal with simple ascii here
// Hyphens are so optional that they are almost whitespace - usually want to see them, but ignore them
//
// It hurts to tokenize too finely when:
//   you issolate a partial token that you shouldn't
//     4.50 --> 4 . 50    A search for 50 will find something
//   you issolate a  partial token does not contain enough info to correctly normalize
//     4.50 --> 4 . 50    Would like to norm 4.50 and 4.5 to same thing, but 50 != 5
//--------------------------------------------------------------------------------------------------------

  public int findEndOfToken(char[] inChars, int inCharN, int inEndCharN, boolean inFirstToken) {
    
    // Skip over leading hyphens
    int theCharN=skipHyphens(inChars,inCharN,inEndCharN);
    
    // If issolated hyphens - keep as hyphen token
    if ((theCharN==inEndCharN)||(isWhitespace(inChars[theCharN])))
      return theCharN;

    char theChar=inChars[theCharN];
        
    // Word = run of letters or apos (optional leading and/or trailing hyphens)
    if (isLetter(theChar)||(theChar=='\'')) {        
      theCharN=findEndOfWord(inChars,theCharN,inEndCharN);
      // Skip over trailing hyphens
      theCharN=skipHyphens(inChars,theCharN,inEndCharN);
      return theCharN;
      
    // Number = runs of digits plus some complexity (optional leading and/or trailing hyphens)
    } else if (isDigit(theChar)) {
      theCharN=findEndOfNumber(inChars,theCharN,inEndCharN);
      // Skip over trailing hyphens
      theCharN=skipHyphens(inChars,theCharN,inEndCharN);
      return theCharN;
    
    // Hyphens then punct - keep hyphens as token
    } else if (theCharN>inCharN)
      return theCharN;

    // Parens are always single char tokens (no hyphens)
    else if (isBracket(theChar)) 
      return theCharN+1;
    
    // Runs of identical punct (no hyphens)
    else 
      return findRunOfPunct(inChars,theCharN,inEndCharN);
  }

//--------------------------------------------------------------------------------------------------------
// findTokens
//--------------------------------------------------------------------------------------------------------

  public int findTokens(char[] inChars, int inCharN, int inNChars, 
      int[] ioTokenCharNs, int[] ioTokenNCharss) {

    int theCharN=inCharN;
    int theEndCharN=inCharN+inNChars;
    while ((theEndCharN>inCharN)&&(isWhitespace(inChars[theEndCharN-1])))
      theEndCharN--;
    int theTokenN=0;
    boolean theFirstToken=true;

    while (theCharN<theEndCharN) {
        
      // Skip to start of next token
      theCharN=findEndOfWhitespace(inChars,theCharN,theEndCharN);
      if (theCharN>theEndCharN)
        throw new RuntimeException("Tokenizer lost: "+theCharN+">"+theEndCharN);

      // If reached end of buffer, there are no more tokens
      if (theCharN==theEndCharN)
        break;
  
      if (ioTokenCharNs!=null)
        ioTokenCharNs[theTokenN]=theCharN;
      
      // Extract token data - performed by findNextToken() in subclass
      int theNewCharN=findEndOfToken(inChars,theCharN,theEndCharN,theFirstToken);
      if (theNewCharN>theEndCharN)
        throw new RuntimeException("Tokenizer lost: "+theNewCharN+">"+theEndCharN);
      
      if (ioTokenNCharss!=null)
        ioTokenNCharss[theTokenN]=theNewCharN-theCharN;
  
      // Advance past current token
      theFirstToken=false;
      theCharN=theNewCharN;
      theTokenN++;
    }
    
    // If there is room, add an extra CharN
    // This gives NTokens+1 values for CharNs, which lets (CharNs[n+1]-CharNs[n]) work for all NTokens tokens
    if ((ioTokenCharNs!=null)&&(ioTokenCharNs.length>theTokenN))
      ioTokenCharNs[theTokenN]=theEndCharN;
    
    // Return number of tokens
    return theTokenN;
  }

  public int findTokens(char[] inChars, int[] outTokenCharNs, int[] outTokenNCharss) {
    return findTokens(inChars,0,inChars.length,outTokenCharNs,outTokenNCharss); }

  public int findTokens(String inString, int[] outTokenCharNs, int[] outTokenNCharss) {
    char[] theChars;
    int theNChars=inString.length();
    if (theNChars<=SliceStore.kCharSliceSize)
      theChars=SliceStore.getSliceStore().getCharSlice();
    else
      theChars=new char[theNChars];
    inString.getChars(0,theNChars,theChars,0);
    
    int theNTokens=findTokens(theChars,0,theNChars,outTokenCharNs,outTokenNCharss); 
    SliceStore.getSliceStore().putCharSlice(theChars);
    return theNTokens;
  }
  
//--------------------------------------------------------------------------------------------------------
// countTokens
//--------------------------------------------------------------------------------------------------------

  public int countTokens(char[] inChars, int inCharN, int inNChars) {
    return findTokens(inChars,0,inChars.length,null,null); }

  public int countTokens(char[] inChars) {
    return findTokens(inChars,0,inChars.length,null,null); }

  public int countTokens(String inString) {
    return findTokens(inString,null,null); }

//--------------------------------------------------------------------------------------------------------
// getTokens
//--------------------------------------------------------------------------------------------------------

  public String[] getTokens(char[] inChars, int inCharN, int inNChars, 
      int[] ioTokenCharNs, int[] ioTokenNCharss) {
    
    int[] theTokenCharNs=ioTokenCharNs;
    if (ioTokenCharNs==null)
      theTokenCharNs=SliceStore.getSliceStore().getIntSlice();
    int[] theTokenNCharss=ioTokenNCharss;
    if (ioTokenNCharss==null)
      theTokenNCharss=SliceStore.getSliceStore().getIntSlice();
    
    int theNTokens=findTokens(inChars,inCharN,inNChars,theTokenCharNs,theTokenNCharss); 
    
    String[] theTokens=new String[theNTokens];
    for (int i=0; i<theNTokens; i++)
      theTokens[i]=new String(inChars,theTokenCharNs[i],theTokenNCharss[i]);
    
    if (ioTokenNCharss==null)
      SliceStore.getSliceStore().putIntSlice(theTokenNCharss);
    if (ioTokenCharNs==null)
      SliceStore.getSliceStore().putIntSlice(theTokenCharNs);
    
    return theTokens;
  }

  public String[] getTokens(char[] inChars, int inCharN, int inNChars) { 
    return getTokens(inChars,inCharN,inNChars,null,null); }

  public String[] getTokens(char[] inChars, int[] ioTokenCharNs, int[] ioTokenNCharss) {
    return getTokens(inChars,0,inChars.length,ioTokenCharNs,ioTokenNCharss); }

  public String[] getTokens(char[] inChars) { return getTokens(inChars,null,null); }

  public String[] getTokens(String inString, int[] ioTokenCharNs, int[] ioTokenNCharss) {
    char[] theChars;
    int theNChars=inString.length();
    if (theNChars<=SliceStore.kCharSliceSize)
      theChars=SliceStore.getSliceStore().getCharSlice();
    else
      theChars=new char[theNChars];
    inString.getChars(0,theNChars,theChars,0);
    
    String[] theTokens=getTokens(theChars,0,theNChars,ioTokenCharNs,ioTokenNCharss);     
    SliceStore.getSliceStore().putCharSlice(theChars);    
    return theTokens;
  }

  public String[] getTokens(String inString) {
    return getTokens(inString,null,null); }

}
