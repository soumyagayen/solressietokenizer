//--------------------------------------------------------------------------------------------------------
// TermNorm.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import java.util.*;

import gravel.store.data.*;
import gravel.store.hash.*;
import gravel.store.var.*;
import gravel.utils.*;
import gravel.xml.*;

//--------------------------------------------------------------------------------------------------------
// TermNorm
//--------------------------------------------------------------------------------------------------------

public class TermNorm implements Constants {

//--------------------------------------------------------------------------------------------------------
// TermNorm consts
//--------------------------------------------------------------------------------------------------------

  public static final int        kNormHyphensFlag=1;
  public static final int        kNormNumbersFlag=2;
  public static final int        kNormRunsOfPunctFlag=4;
  public static final int        kNormPossessivesFlag=8;
  public static final int        kNormInflectionsFlag=16;  
  public static final int        kFullNorm=kNormHyphensFlag|kNormNumbersFlag|kNormRunsOfPunctFlag|
                                     kNormPossessivesFlag|kNormInflectionsFlag;  

  private static final Object    kLoadLock=new Object();    // Used as sync lock, so keep private

//--------------------------------------------------------------------------------------------------------
// TermNorm class vars
//--------------------------------------------------------------------------------------------------------

  private static ByteDataHashRAMStore  gInflectionNorms;      // preferred form for inflection variants
  private static ByteDataHashRAMStore  gInflectionVariants;   // all inflection variants (including norm form)
  private static VarRAMStore           gInflectionMap;        // index of norm for each variant
  private static VarDataRAMStore       gInflectionInverseMap; // indexes of all variants for each norm

//--------------------------------------------------------------------------------------------------------
// gets
//--------------------------------------------------------------------------------------------------------

  public static ByteDataHashRAMStore getInflectionNorms() { return gInflectionNorms; }
  public static ByteDataHashRAMStore getInflectionVariants() { return gInflectionVariants; }
  public static VarRAMStore getInflectionMap() { return gInflectionMap; }
  public static VarDataRAMStore getInflectionInverseMap() { return gInflectionInverseMap; }

//--------------------------------------------------------------------------------------------------------
// clear
//--------------------------------------------------------------------------------------------------------

  public static void clear() {
    synchronized(kLoadLock) {
      if (gInflectionNorms!=null) {
        gInflectionNorms.close();
        gInflectionVariants.close();
        gInflectionMap.close();
        gInflectionInverseMap.close();
        gInflectionNorms=null;
        gInflectionVariants=null;
        gInflectionMap=null;
        gInflectionInverseMap=null;
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// isLoaded
//--------------------------------------------------------------------------------------------------------

  public static boolean isLoaded() {
    return (gInflectionNorms!=null); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static void load(String inNormDir) throws Exception {   
    CharNorm.load(inNormDir);

    // Should always be called in single threaded section, but protect just in case
    synchronized(kLoadLock) {
      if (!isLoaded())  {
        gInflectionNorms=new ByteDataHashRAMStore();
        gInflectionVariants=new ByteDataHashRAMStore();
        gInflectionMap=new VarRAMStore();
        gInflectionInverseMap=null;
        
        X2OParser.xmlFileToObject(new X2OBuilder() {
          public Object buildObject(X2OData inX2OData) throws Exception {
            String theTagname=inX2OData.getTagname();
            if ((theTagname.equals("norm"))||(theTagname.equals("variant")))
              return inX2OData.getText();
            else if (theTagname.equals("inflection")) {
              String theNorm=(String) inX2OData.getFirstChildElmt("norm");
              gInflectionNorms.appendUTF8(theNorm);
              long theNormDx=gInflectionNorms.getSize()-1;
              gInflectionVariants.appendUTF8(theNorm);
              gInflectionMap.appendVar(theNormDx);
              Object[] theVariants=inX2OData.getChildElmts("variant");
              for (int i=0; i<theVariants.length; i++) {
                gInflectionVariants.appendUTF8((String) theVariants[i]);
                gInflectionMap.appendVar(theNormDx);
              }
            }
            return null;
          }
        },inNormDir+"/Inflections.xml");
    
        gInflectionNorms.compact();
        gInflectionVariants.compact();
        gInflectionMap.compact();
        gInflectionInverseMap=MapUtils.invertNTo1Map(gInflectionMap,gInflectionNorms.getSize(),true);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// setInflections
//--------------------------------------------------------------------------------------------------------

  static void setInflections(
      ByteDataHashRAMStore  inInflectionNorms, 
      ByteDataHashRAMStore  inInflectionVariants, 
      VarRAMStore           inInflectionMap, 
      VarDataRAMStore       inInflectionInverseMap) {
    synchronized(kLoadLock) {
      if (gInflectionNorms!=null) {
        gInflectionNorms.close();
        gInflectionVariants.close();
        gInflectionMap.close();
        gInflectionInverseMap.close();
      }
      gInflectionNorms=inInflectionNorms;
      gInflectionVariants=inInflectionVariants;
      gInflectionMap=inInflectionMap;
      gInflectionInverseMap=inInflectionInverseMap;
    }
  }

//--------------------------------------------------------------------------------------------------------
// Inner class Info
//--------------------------------------------------------------------------------------------------------

  static class Info {
    
    char[]   mTokenChars;
    int      mCharN;
    int      mEndCharN;
    int[]    mFromForToCharNs;
    
    Info(char[] inTokenChars, int inCharN, int inEndCharN, int[] inFromForToCharNs) {
      mTokenChars=inTokenChars;
      mCharN=inCharN;
      mEndCharN=inEndCharN;
      mFromForToCharNs=inFromForToCharNs;
    }
    
    char getFirstChar() { return mTokenChars[mCharN]; }
    char getLastChar() { return mTokenChars[mEndCharN-1]; }
    char getChar(int inCharN) { 
      if ((inCharN<mCharN)||(inCharN>=mEndCharN))
        throw new RuntimeException("CharN, "+inCharN+", outside range, "+mCharN+"-"+mEndCharN);
      return mTokenChars[inCharN]; 
    }

    String getPartialToken(int inCharN, int inEndCharN) { 
      if ((inCharN<mCharN)||(inCharN>=mEndCharN))
        throw new RuntimeException("CharN, "+inCharN+", outside range, "+mCharN+"-"+mEndCharN);
      if ((inEndCharN<=mCharN)||(inEndCharN>mEndCharN))
        throw new RuntimeException("EndCharN, "+inEndCharN+", outside range, "+mCharN+"-"+mEndCharN);
      return new String(mTokenChars,inCharN,inEndCharN-inCharN); 
    }
    String getToken() { return getPartialToken(mCharN,mEndCharN); }
    
    int getCharN() { return mCharN; }
    int getEndCharN() { return mEndCharN; }
    int getNChars() { return getEndCharN()-getCharN(); }

    int getFromCharN(int inCharN) { return mFromForToCharNs[inCharN]; }
    int getFromCharN() { return mFromForToCharNs[mCharN]; }
    int getFromEndCharN() { return mFromForToCharNs[mEndCharN]; }
    int getFromNChars() { return getFromEndCharN()-getFromCharN(); }

    int indexOf(char inChar, int inCharN, int inEndCharN) {
      for (int i=inCharN; i<inEndCharN; i++)
        if (mTokenChars[i]==inChar)
          return i;
      return kNotFound;
    }
    int indexOf(char inChar, int inCharN) { return indexOf(inChar,inCharN,mEndCharN); }
    int indexOf(char inChar) { return indexOf(inChar,mCharN); }

    int lastIndexOf(char inChar, int inCharN, int inEndCharN) {
      for (int i=inEndCharN-1; i>=inCharN; i--)
        if (mTokenChars[i]==inChar)
          return i;
      return kNotFound;
    }
    int lastIndexOf(char inChar, int inEndCharN) { return lastIndexOf(inChar,mCharN,inEndCharN); }
    int lastIndexOf(char inChar) { return lastIndexOf(inChar,mEndCharN); }
    
    void dropChars(int inCharN, int inEndCharN) {
      if ((inCharN<mCharN)||(inCharN>=mEndCharN))
        throw new RuntimeException("Drop start out of range: "+inCharN+":"+mCharN+"-"+mEndCharN+"  "+getChar(inCharN)+":"+getToken());
      if ((inEndCharN<=mCharN)||(inCharN>mEndCharN))
        throw new RuntimeException("Drop end out of range: "+inEndCharN+":"+mCharN+"-"+mEndCharN+"  "+getChar(inEndCharN)+":"+getToken());
      if ((inCharN>=inEndCharN))
        throw new RuntimeException("Drop start greater or equal end: "+inCharN+">="+inEndCharN);
      int theNDropped=inEndCharN-inCharN;
      for (int i=inEndCharN; i<mEndCharN; i++) {
        mTokenChars[i-theNDropped]=mTokenChars[i];
        mFromForToCharNs[i-theNDropped]=mFromForToCharNs[i];
      }
      mFromForToCharNs[mEndCharN-theNDropped]=mFromForToCharNs[mEndCharN];
      mEndCharN-=theNDropped;
    }
    void dropChar(int inCharN) { dropChars(inCharN,inCharN+1); }
    
    Info split(int inCharN) {
      if ((inCharN<=mCharN)||(inCharN>=mEndCharN))
        throw new RuntimeException("Split out of range: "+inCharN+":"+mCharN+"-"+mEndCharN+"  "+getChar(inCharN)+":"+getToken());
      int theEndCharN=mEndCharN;
      mEndCharN=inCharN;
      return new Info(mTokenChars,inCharN,theEndCharN,mFromForToCharNs); 
    }
    
    void merge(Info inInfo) {
      int theNewEndpos=mEndCharN+inInfo.getNChars();
      if (mEndCharN<inInfo.getCharN()) {
        int theOffset=inInfo.getCharN()-mEndCharN;
        for (int i=mEndCharN; i<theNewEndpos; i++) {
          mTokenChars[i]=inInfo.getChar(theOffset+i);
          mFromForToCharNs[i]=inInfo.getFromCharN(theOffset+i);
        }
        mFromForToCharNs[theNewEndpos]=inInfo.getFromCharN(theOffset+theNewEndpos);
      }
      mEndCharN=theNewEndpos;
    }
   
    void replaceToken(String inToken) {
      if (inToken.length()>getNChars())
        throw new RuntimeException("Replace with larger token: "+getToken()+" --> "+inToken);
      for (int i=0; i<inToken.length(); i++) 
        mTokenChars[mCharN+i]=inToken.charAt(i);
      int theEndCharN=mCharN+inToken.length();
      if (theEndCharN<mEndCharN)
        dropChars(theEndCharN,mEndCharN);
    }
    
    void replacePartialToken(String inPartialToken, int inCharN, int inEndCharN) {
      if (inPartialToken.length()>(inEndCharN-inCharN))
        throw new RuntimeException("Replace with larger partial token: "+
            getPartialToken(inCharN,inEndCharN)+" --> "+inPartialToken);
      for (int i=0; i<inPartialToken.length(); i++) 
        mTokenChars[inCharN+i]=inPartialToken.charAt(i);
      int thePartEndCharN=inCharN+inPartialToken.length();
      if (thePartEndCharN<inEndCharN)
        dropChars(thePartEndCharN,inEndCharN);
    }
    
    void replaceChar(char inNewChar, int inCharN) {
      mTokenChars[inCharN]=inNewChar;
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// normalizeHyphens
//
// Three cases produced by NormTokenizer
//   1) Entire token is 1 or more hyphens
//   2) Token is word with 1 or more leading and/or trailing hyphens
//   3) Token is number with 1 or more leading and/or trailing hyphens
// 
// In case 1, collapse to single hyphen
// In case 2, remove all hyphens
// In case 3, collapse to single leading hyphen and remove all trailing hyphens
//--------------------------------------------------------------------------------------------------------

  private static void normalizeHyphens(ArrayList inInfoList, int inIndex, Info inInfo) {

    // If no hyphens, quick fail
    boolean theHasLeading=(inInfo.getFirstChar()=='-');
    boolean theHasTrailing=(inInfo.getLastChar()=='-');
    if ((!theHasLeading)&&(!theHasTrailing))
      return;

    // Reduce multiple leading hyphens to single leading hyphen
    int theCharN=inInfo.getCharN();
    while ((inInfo.getNChars()>1)&&(inInfo.getChar(theCharN+1)=='-'))
      inInfo.dropChar(theCharN+1);
    
    // If reduced token to single char, then it was case 1, all hyphens, and we're done
    if (inInfo.getNChars()==1)
      return;
    
    // Must be case 2 or 3, so remove all trailing hyphens
    while (inInfo.getLastChar()=='-')
      inInfo.dropChar(inInfo.getEndCharN()-1);

    // If no leading hyphens, we're done
    if (!theHasLeading)
      return;

    // If first non-hyphen char is a digit (NormTokenizer does not recognize numbers starting with decimal pt), 
    //   then it was case 3, number with hyphens, and we're done
    char theChar=inInfo.getChar(theCharN+1);
    if (CharNorm.isDigit(theChar))
      return;
    
    // Case 2, word with hyphens, drop final leading hyphen
    inInfo.replaceChar(inInfo.getChar(theCharN+1),theCharN);
    inInfo.dropChar(theCharN+1);
  }

  private static void normalizeHyphens(ArrayList inInfoList) {
    for (int i=inInfoList.size()-1; i>=0; i--) {
      Info theInfo=(Info) inInfoList.get(i);
      normalizeHyphens(inInfoList,i,theInfo);
    }
  }

//--------------------------------------------------------------------------------------------------------
// normalizeNumbers
//--------------------------------------------------------------------------------------------------------

  private static void normalizeNumbers(ArrayList inInfoList, int inIndex, Info inInfo) {
    
    // If not a number, bail
    // Numbers start with a digit with an optional leading hyphen 
    char theFirstChar=inInfo.getFirstChar();
    if (theFirstChar!='-') {
      if (!CharNorm.isDigit(theFirstChar))
        return;
    } else {
      if (inInfo.getNChars()==1)
        return;
      char theSecondChar=inInfo.getChar(inInfo.getCharN()+1);
      if (!CharNorm.isDigit(theSecondChar))
        return;
    }

    // Remove trailing 0 after decimal pt
    if (inInfo.lastIndexOf('.')>0)
      while (inInfo.getLastChar()=='0')
        inInfo.dropChar(inInfo.getEndCharN()-1);
       
    // Drop trailing decimal pt
    if (inInfo.getLastChar()=='.')  
      inInfo.dropChar(inInfo.getEndCharN()-1);
    
    // Remove all commas
    int theCharN=inInfo.lastIndexOf(',');
    while (theCharN!=kNotFound) {
      if (inInfo.getNChars()==1) {  
        inInfoList.remove(inIndex);
        return;
      } else if (theCharN==inInfo.getCharN()) {
        inInfo.replaceChar(inInfo.getChar(theCharN+1),theCharN);
        inInfo.dropChar(theCharN+1);
        return;
      } else {
        inInfo.dropChar(theCharN);
        theCharN=inInfo.lastIndexOf(',');
      }
    }
  }

  private static void normalizeNumbers(ArrayList inInfoList) {
    for (int i=inInfoList.size()-1; i>=0; i--) {
      Info theInfo=(Info) inInfoList.get(i);
      normalizeNumbers(inInfoList,i,theInfo);
    }
  }

//--------------------------------------------------------------------------------------------------------
// normalizePossessives
//--------------------------------------------------------------------------------------------------------

  private static void normalizePossessive(ArrayList inInfoList, int inIndex, Info inInfo) {
    
    // If not a word, bail
    // Words start with a letter or apos
    char theFirstChar=inInfo.getFirstChar();
    if ((!CharNorm.isLetter(theFirstChar))&&(theFirstChar!='\''))
      return;

    // Remove trailing 's
    int theEndCharN=inInfo.getEndCharN();
    if ((inInfo.getNChars()>2)&&(inInfo.getChar(theEndCharN-2)=='\'')&&(inInfo.getChar(theEndCharN-1)=='s')) 
      inInfo.dropChars(theEndCharN-2,theEndCharN);

    // Remove all other apos
    int theCharN=inInfo.lastIndexOf('\'');
    while (theCharN!=kNotFound) {
      if (inInfo.getNChars()==1) {  
        return; // word was all apos - norm to single apos
      } else if (theCharN==inInfo.getCharN()) {
        inInfo.replaceChar(inInfo.getChar(theCharN+1),theCharN);
        inInfo.dropChar(theCharN+1);
        return;
      } else {
        inInfo.dropChar(theCharN);
        theCharN=inInfo.lastIndexOf('\'');
      }
    }
  }

  private static void normalizePossessives(ArrayList inInfoList) {
    for (int i=inInfoList.size()-1; i>=0; i--) {
      Info theInfo=(Info) inInfoList.get(i);
      normalizePossessive(inInfoList,i,theInfo);
    }
  }

//--------------------------------------------------------------------------------------------------------
// normalizeRunsOfPunctuation
// Keep:
//   .
//   ..
//   ...
//   ....
// 5+ char runs norm to 4 char run
//--------------------------------------------------------------------------------------------------------

  private static void normalizeRunsOfPunctuation(ArrayList inInfoList, int inIndex, Info inInfo) {
    
    // If not punctuation, bail
    int theCharN=inInfo.getCharN();
    char theFirstChar=inInfo.getChar(theCharN);
    if (!CharNorm.isPunctuation(theFirstChar))
      return;
    
    // Will norm runs of 5+ punctuation marks into a run of 4
    int theNChars=inInfo.getNChars();
    if (theNChars<5)
      return;
    
    // Make sure this is a run
    for (int i=1; i<theNChars; i++)
      if (inInfo.getChar(theCharN+i)!=theFirstChar)
        return;

    // Remove trailing punctuation
    inInfo.dropChars(theCharN+4,inInfo.getEndCharN());
  }

  private static void normalizeRunsOfPunctuation(ArrayList inInfoList) {
    for (int i=inInfoList.size()-1; i>=0; i--) {
      Info theInfo=(Info) inInfoList.get(i);
      normalizeRunsOfPunctuation(inInfoList,i,theInfo);
    }
  }

//--------------------------------------------------------------------------------------------------------
// normalizeInflection
//--------------------------------------------------------------------------------------------------------

  private static String normalizeInflection(String inToken) {
    if (gInflectionMap!=null) {    
      long theIndex=gInflectionVariants.getIndex(inToken);
      if (theIndex!=kNotFound) {
        String theNorm=gInflectionNorms.getUTF8(gInflectionMap.getLong(theIndex));
        if (!theNorm.equals(inToken)) 
          return theNorm;
      }
    }
    return null;
  }

  private static void normalizeInflection(Info inInfo) {
    int theCharN=inInfo.getCharN();
    while (theCharN<inInfo.getEndCharN()) {
      while ((theCharN<inInfo.getEndCharN())&&(!CharNorm.isLetter(inInfo.getChar(theCharN))))
        theCharN++;
      if (theCharN<inInfo.getEndCharN()) {
        int theEndCharN=theCharN+1;
        while ((theEndCharN<inInfo.getEndCharN())&&(CharNorm.isLetter(inInfo.getChar(theEndCharN))))
          theEndCharN++;
        String theNorm=normalizeInflection(inInfo.getPartialToken(theCharN,theEndCharN));
        if (theNorm!=null) {
          inInfo.replacePartialToken(theNorm,theCharN,theEndCharN);             
          theEndCharN=theCharN+theNorm.length();
        }
        theCharN=theEndCharN+1;
      }
    }
  }

  private static void normalizeInflections(ArrayList inInfoList) {
    if (gInflectionMap==null)
      return;    
    for (int i=inInfoList.size()-1; i>=0; i--) {
      Info theInfo=(Info) inInfoList.get(i);
      normalizeInflection(theInfo);
    }
  }

//--------------------------------------------------------------------------------------------------------
// showInfos
//--------------------------------------------------------------------------------------------------------
/*
  private static void showInfos(ArrayList inInfoList) {
    for (int i=0; i<inInfoList.size(); i++) {
      Info theInfo=(Info) inInfoList.get(i);     
      System.out.print(theInfo.getToken()+' ');
    }
    System.out.println();
  }
*/

//--------------------------------------------------------------------------------------------------------
// getNormTokens
//
// This is a messy job, but after years of mucking about, the following (not too complex) algorithm
//   has been developed.  It is imperfect at best.  More could be done, at the cost of more complexity
//
// Note that there is a difference between this and other tokenizers.  Other tokenizers find tokens as  
//   substrings of the original raw text.  This tokenizer performs a char norm, which changes some chars 
//   into 0, 1, 2, or more chars.  Then, tokens are changed and shortened by inflection and stripping 
//   decoration.  The result is norm tokens that are not substrings of the original raw text.  Norm 
//   tokens are often different lengths than the range of the raw text they represent (which is returned
//   for use in highlighting).  So, ... messy.
//
// Norm uses datasets
//   1) CharNorm
//   2) Inflectional variants
// And sets of rules
//   1) Tokenization
//   2) Strip hyphens
//   3) Strip possessives
//   4) Truncate long runs of punctuation like: ____________________________
//   4) Recognize numbers
//--------------------------------------------------------------------------------------------------------
  
  public static String[] getNormTokens(char[] inFromChars, int inFromCharN, int inFromNChars,
      int[] ioTokenCharNs, int[] ioTokenNChars, int inNormStrategyFlags) {

    try {

      int theEstimatedNToChars=(int) (inFromNChars*1.1+64);
      char[] theToChars=new char[theEstimatedNToChars];
      int[]  theFromForToCharNs=new int[theEstimatedNToChars];

      // CharNorm includes
      //   Lowercase
      //   Strip Accents - reduces string length if accent by itself (not included in letter)
      //   Strip Trademark, copyright, registered, etc - reduces string length
      //   Expand ligatures - increases string length and possibly adds tokens
      //   Blank control chars
      //   Remove undefined chars - reduces string length
      //   Collapse redundant symbols into one, which is similar to lowercasing
      int theNToChars=CharNorm.normChars(inFromChars,inFromCharN,inFromNChars,
          theToChars,0,theFromForToCharNs);

      int theEstimatedNTokens=theNToChars/4+16;
      int[] theTokenCharNs=new int[theEstimatedNTokens];
      int[] theTokenNCharss=new int[theEstimatedNTokens];

      // Tokenize to words (aka thick tokens)
      // Is a little more clever than breaking on whitespace
      // Will usually separate a word from adjacent punctuation
      // Will usually not break up a word that contains punctuation
      //   (lymphoma, non-hodjkin's?)  -->  (  lymphoma  ,  non-hodjkin's  ?  )
      int theNTokens=new NormTokenizer().findTokens(theToChars,0,theNToChars,
          theTokenCharNs,theTokenNCharss);

      ArrayList theInfoList=new ArrayList((int) Math.round(theNTokens*1.1+4));
      for (int i=0; i<theNTokens; i++) 
        theInfoList.add(new Info(theToChars,theTokenCharNs[i],theTokenCharNs[i]+theTokenNCharss[i],
            theFromForToCharNs));    

      // Do the work
      if ((inNormStrategyFlags&kNormHyphensFlag)!=0)
        normalizeHyphens(theInfoList);
      if ((inNormStrategyFlags&kNormNumbersFlag)!=0)
        normalizeNumbers(theInfoList);
      if ((inNormStrategyFlags&kNormRunsOfPunctFlag)!=0)
        normalizeRunsOfPunctuation(theInfoList);
      if ((inNormStrategyFlags&kNormPossessivesFlag)!=0)
        normalizePossessives(theInfoList);
      if ((inNormStrategyFlags&kNormInflectionsFlag)!=0)
        normalizeInflections(theInfoList);  

      String[] theNormTokens=new String[theInfoList.size()];
      for (int i=0; i<theInfoList.size(); i++) {
        Info theInfo=(Info) theInfoList.get(i);     
        theNormTokens[i]=theInfo.getToken();
        if ((theNormTokens[i].length()==0)||(theInfo.getFromNChars()==0) )
          throw new RuntimeException();
        if (ioTokenCharNs!=null)
          ioTokenCharNs[i]=theInfo.getFromCharN();
        if (ioTokenNChars!=null)
          ioTokenNChars[i]=theInfo.getFromNChars();
      }

      return theNormTokens;
    } catch (Exception e) {
      throw new RuntimeException("Failed to norm: "+
          (new String(inFromChars,inFromCharN,inFromNChars)),e);
    }
  }

  public static String[] getNormTokens(char[] inChars, int inCharN, int inNChars) { 
    return getNormTokens(inChars,inCharN,inNChars,null,null,kFullNorm); }

  public static String[] getNormTokens(char[] inChars, int[] ioTokenCharNs, int[] ioTokenNCharss) {
    return getNormTokens(inChars,0,inChars.length,ioTokenCharNs,ioTokenNCharss,kFullNorm); }

  public static String[] getNormTokens(char[] inChars) { return getNormTokens(inChars,null,null); }

  public static String[] getNormTokens(String inString, 
      int[] ioTokenCharNs, int[] ioTokenNCharss, int inNormStrategyFlags) {
    int theNChars=inString.length();
    char[] theChars=new char[theNChars];
    inString.getChars(0,theNChars,theChars,0);
    String[] theTokens=getNormTokens(theChars,0,theNChars,ioTokenCharNs,ioTokenNCharss,inNormStrategyFlags); 
    return theTokens;
  }

  public static String[] getNormTokens(String inString, int[] ioTokenCharNs, int[] ioTokenNCharss) {
    return getNormTokens(inString,ioTokenCharNs,ioTokenNCharss,kFullNorm); }

  public static String[] getNormTokens(String inString) {
    return getNormTokens(inString,null,null); }

//--------------------------------------------------------------------------------------------------------
// getRawTokens
//--------------------------------------------------------------------------------------------------------

  public static String[] getRawTokens(String inRawText, int inNTokens,
      int[] inTokenCharNs, int[] inTokenNChars) {
        
    // This is weird - raw token boundaries set by normalized tokens
    // But, we want to be able to construct Raw NGrams by concatenating Raw Tokens
    // Therefore, raw tokens include an optional trailing blank 
    String[] theRawTokens=new String[inNTokens];
    int theEnd=inRawText.length();
    for (int i=inNTokens-1; i>=0; i--) {
      // Called for every single token in corpus - optimized for speed
      int theStart=inTokenCharNs[i];
      // Want to replace a trailing run of whitespace with single blank
      // Trim off 0 or more whitespace chars
      boolean theHasBlank=false;
      while (CharNorm.isWhitespace(inRawText.charAt(theEnd-1))) {
        theHasBlank=true;
        theEnd--;
      }
      // If next char is a blank, adjust substring to include it (fast)
      if ((theHasBlank)&&(inRawText.charAt(theEnd)==' ')) {
        theHasBlank=false;
        theEnd++;
      }
      // If there was whitespace, but not a blank, add the blank (slow)
      if (!theHasBlank)
        theRawTokens[i]=inRawText.substring(theStart,theEnd);
      else {
        int theNChars=theEnd-theStart+1;
        StringBuffer theBuffer=new StringBuffer(theNChars);
        theBuffer.append(inRawText.substring(theStart,theEnd));
        theBuffer.append(' ');
        theRawTokens[i]=theBuffer.toString();
      }
      theEnd=theStart;
    }
    return theRawTokens;
  }

  public static String[] getRawTokens(String inRawText) {
    int[] theCharNs=SliceStore.getSliceStore().getIntSlice();
    int[] theNChars=SliceStore.getSliceStore().getIntSlice();
    String[] theNormTokens=getNormTokens(inRawText,theCharNs,theNChars);
    String[] theRawTokens=getRawTokens(inRawText,theNormTokens.length,theCharNs,theNChars);
    SliceStore.getSliceStore().putIntSlice(theNChars);
    SliceStore.getSliceStore().putIntSlice(theCharNs);
    return theRawTokens;
  }
  
//--------------------------------------------------------------------------------------------------------
// normTerm
//--------------------------------------------------------------------------------------------------------

  public static String normTerm(String[] inNormTokens, int inNormTokenDelta, int inNNormTokens) { 
    return FormatUtils.join(inNormTokens,inNormTokenDelta,inNNormTokens," "); }

  public static String normTerm(String[] inNormTokens) { 
    return normTerm(inNormTokens,0,inNormTokens.length); }

  public static String normTerm(String inRawTerm) {
    return normTerm(getNormTokens(inRawTerm)); }
 
//--------------------------------------------------------------------------------------------------------
// expandInflections
//--------------------------------------------------------------------------------------------------------

  public static String[][] expandInflections(String[] inNormTokens) {
    int theNTokens=inNormTokens.length;
    String[][] theExpansion=new String[theNTokens][];
    for (int i=0; i<theNTokens; i++) {
      String theNormToken=inNormTokens[i];
      long theIndex=gInflectionNorms.getIndex(theNormToken);
      if (theIndex==kNotFound) 
        theExpansion[i]=new String[] {theNormToken};
      else {
        int[] theIndexes=gInflectionInverseMap.getInts(theIndex);
        theExpansion[i]=new String[theIndexes.length];
        for (int j=0; j<theIndexes.length; j++) 
          theExpansion[i][j]=gInflectionVariants.getUTF8(theIndexes[j]);
      }
    }
    return theExpansion;
  }

  public static String[][] expandInflections(String inString) {
    return expandInflections(getNormTokens(inString)); }

}

