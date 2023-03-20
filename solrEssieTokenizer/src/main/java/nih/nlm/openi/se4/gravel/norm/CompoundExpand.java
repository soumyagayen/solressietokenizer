//--------------------------------------------------------------------------------------------------------
// CompoundExpand.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import java.util.*;

import gravel.store.data.*;
import gravel.store.hash.*;
import gravel.store.var.*;
import gravel.utils.*;
import gravel.xml.*;

//--------------------------------------------------------------------------------------------------------
// CompoundExpand
//--------------------------------------------------------------------------------------------------------

public class CompoundExpand implements Constants {

//--------------------------------------------------------------------------------------------------------
// CompoundExpand consts
//--------------------------------------------------------------------------------------------------------

  public static final int        kMaxNCompoundVariants=72;
  
  public static final String     kLogName="CompoundExpand";

  private static final Object    kLoadLock=new Object();    // Used as sync lock, so keep private

//--------------------------------------------------------------------------------------------------------
// CompoundExpand class vars
//--------------------------------------------------------------------------------------------------------
  
  public static ByteDataHashRAMStore  gCompoundSpaced;       // compound words with spaces
  public static ByteDataHashRAMStore  gCompoundCompressed;   // compound words without spaces
  public static VarRAMStore           gCompoundMap;          // index of compressed for each spaced
  public static VarDataRAMStore       gCompoundInverseMap;   // indexes of all spaceds for each compressed

//--------------------------------------------------------------------------------------------------------
// gets
//--------------------------------------------------------------------------------------------------------
  
  public static ByteDataHashRAMStore getCompoundSpaced() { return gCompoundSpaced; }
  public static ByteDataHashRAMStore getCompoundCompressed() { return gCompoundCompressed; }
  public static VarRAMStore getCompoundMap() { return gCompoundMap; }
  public static VarDataRAMStore getCompoundInverseMap() { return gCompoundInverseMap; }

//--------------------------------------------------------------------------------------------------------
// isLoaded
//--------------------------------------------------------------------------------------------------------

  public static boolean isLoaded() {
    return (gCompoundSpaced!=null); }

//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static void load(String inNormDir) throws Exception {   
    TermNorm.load(inNormDir);

    // Should always be called in single threaded section, but protect just in case
    synchronized(kLoadLock) {
      if (!isLoaded()) {
        gCompoundSpaced=new ByteDataHashRAMStore();
        gCompoundCompressed=new ByteDataHashRAMStore();
        gCompoundMap=new VarRAMStore();
        gCompoundInverseMap=null;
        
        X2OParser.xmlFileToObject(new X2OBuilder() {
          public Object buildObject(X2OData inX2OData) throws Exception {
            String theTagname=inX2OData.getTagname();
            if ((theTagname.equals("compressed"))||(theTagname.equals("spaced")))
              return inX2OData.getText();
            else if (theTagname.equals("compound")) {
              String theCompressed=(String) inX2OData.getFirstChildElmt("compressed");
              gCompoundCompressed.appendUTF8(theCompressed);
              long theCompressedDx=gCompoundCompressed.getSize()-1;
              Object[] theSpaceds=inX2OData.getChildElmts("spaced");
              for (int i=0; i<theSpaceds.length; i++) {
                gCompoundSpaced.appendUTF8((String) theSpaceds[i]);
                gCompoundMap.appendVar(theCompressedDx);
              }
            }
            return null;
          }
        },inNormDir+"/Compounds.xml");
        
        gCompoundSpaced.compact();
        gCompoundCompressed.compact();
        gCompoundMap.compact();
        gCompoundInverseMap=MapUtils.invertNTo1Map(gCompoundMap,gCompoundCompressed.getSize(),true);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// setCompounds
//--------------------------------------------------------------------------------------------------------

  static void setCompounds(
      ByteDataHashRAMStore  inCompoundSpaced,
      ByteDataHashRAMStore  inCompoundCompressed,
      VarRAMStore           inCompoundMap, 
      VarDataRAMStore       inCompoundInverseMap) {
    synchronized(kLoadLock) {
      if (gCompoundSpaced!=null) {
        gCompoundSpaced.close();
        gCompoundCompressed.close();
        gCompoundMap.close();
        gCompoundInverseMap.close();
      }
      gCompoundSpaced=inCompoundSpaced;
      gCompoundCompressed=inCompoundCompressed;
      gCompoundMap=inCompoundMap;
      gCompoundInverseMap=inCompoundInverseMap;
    }
  }

//--------------------------------------------------------------------------------------------------------
// expandNormTokens
//
// We are looking for cases where 2 or 3 space separated tokens can be merged into a compound word
// In general, compound words need not overlap cleanly
//
//         +----cde----+
//         |           |
//  -a-+-b-+-c---d-+-e-+-f-
//     |           |
//     +----bcd----+
//
// We are going to assume that this case is rare enough to ignore.
// Furthermore, we assume that a found compound word data has a complete set of spaced variants.
// If bcd is found and expanded, we assume "b cd" and "bc d" exist too (almost true)
//
// So, any run 1, 2, or 3 tokens can become several several spaced variants
// Treating an array of tokens as a term, the original term will be expanded into a set of 
//   alternative terms.  
// 
//  -a---b---c---d---e---f-    becomes    -a---b---c---d---e---f-
//                                        -a---bc---d---e---f-
//                                        -a---b---cd---e---f-
//                                        -a---bcd---e---f-
//
//  If multiple compound words occur, there can be many alternatives.
//--------------------------------------------------------------------------------------------------------
  
  public static String[][] expandNormTokens(String[] inNormTokens) {
    if (gCompoundSpaced==null) 
      return new String[][] {inNormTokens};
    
    ArrayList theTermList=new ArrayList();
    theTermList.add(new ArrayList());

    // loop over all tokens looking for compound words
    for (int i=0; i<inNormTokens.length; i++) {
      boolean theFound=false;

      // Keep track of current number of alternative terms.
      int theOldNTerms=theTermList.size();
      if (theOldNTerms>kMaxNCompoundVariants)
        return new String[][] {inNormTokens};

      // Look for 3 cases: a 3 token spaced variant, a 2 token spaced variant, a 1 token compressed variant 
      for (int j=Math.min(i+3,inNormTokens.length); j>i; j--) {
        
        // Spaced and compressed variants handled differently
        //  1) Compressed variant expanded to all spaced variants
        //  2) Spaced variant mapped to compressed variant first and then expanded
        boolean theCompressedCase=(j==i+1);
        
        // If we can find the index of the compressed form, we have a compound word
        // For compressed variant case, index is a simple lookup 
        long theCompressedIndex=kNotFound;
        if (theCompressedCase) 
          theCompressedIndex=gCompoundCompressed.getIndex(inNormTokens[i]);
          
        // Spaced variant requires several steps:
        //   1) create spaced variant,
        //   2) lookup spaced form,
        //   3) if found, map to compressed index
        else {
          
          // Create spaced out form of compound word by concatenating tokens
          String theSpacedTokens=inNormTokens[i];
          for (int k=i+1; k<j; k++) 
            theSpacedTokens+=' '+inNormTokens[k];

          // Lookup spaced form
          long theSpacedIndex=gCompoundSpaced.getIndex(theSpacedTokens);

          if (theSpacedIndex!=kNotFound) 
            // Map to compressed index
            theCompressedIndex=gCompoundMap.getLong(theSpacedIndex);
        }

        // If compressed form exists, compound word
        if (theCompressedIndex!=kNotFound) {
          theFound=true;
          
          // Map (i.e. expand) compressed variant to all spaced variants
          long[] theSpacedIndexes=gCompoundInverseMap.getLongs(theCompressedIndex);

          // Make a duplicate set of alternatives for each spaced variant
          // Orignial set will be used for compressed variant
          for (int k=0; k<theSpacedIndexes.length; k++) 
            for (int l=0; l<theOldNTerms; l++) {        
              ArrayList theTokenList=((ArrayList) theTermList.get(l));
              theTermList.add(theTokenList.clone());
            }
          
          // Add compressed form of compound word to original set of alternatives
          int n=0;
          String theCompressedToken=gCompoundCompressed.getUTF8(theCompressedIndex);
          for (int k=0; k<theOldNTerms; k++) {
            ArrayList theTokenList=((ArrayList) theTermList.get(n));
            theTokenList.add(theCompressedToken);
            n++;
          }

          // Add spaced variants of compound word to duplicate sets of alternatives
          for (int k=0; k<theSpacedIndexes.length; k++) {
            String theSpacedNormWord=gCompoundSpaced.getUTF8(theSpacedIndexes[k]);
            String[] theSpacedNormTokens=FormatUtils.breakOnChars(theSpacedNormWord,' ');
            for (int l=0; l<theOldNTerms; l++) {
              ArrayList theTokenList=((ArrayList) theTermList.get(n));
              for (int m=0; m<theSpacedNormTokens.length; m++)
                theTokenList.add(theSpacedNormTokens[m]);
              n++;
            }
          }

          // Advance past this compound word
          i=j-1;  // about to get a +1 at start of next loop
          
          break;
        }
      }
      
      // If no compound word
      if (!theFound) {
        
        // Add token from original term to all alternative terms
        String theNextToken=inNormTokens[i];
        for (int k=0; k<theOldNTerms; k++) {
          ArrayList theTokenList=((ArrayList) theTermList.get(k));
          theTokenList.add(theNextToken);
        }
      }
    }
    
    // Bail if no compound words
    if (theTermList.size()==1)
      return new String[][] {inNormTokens};
    
    // Flatten list of list of tokens to 2D array of tokens
    for (int i=0; i<theTermList.size(); i++) {
      ArrayList theTokenList=(ArrayList) theTermList.get(i);
      String[] theTokens=(String[]) theTokenList.toArray(new String[theTokenList.size()]);
      theTermList.set(i,theTokens);
    }
    String[][] theTerms=(String[][]) theTermList.toArray(new String[theTermList.size()][]);
    
    return theTerms;
  }

//--------------------------------------------------------------------------------------------------------
// expandNormTerm
//--------------------------------------------------------------------------------------------------------
  
  public static String[] expandNormTerm(String inRawTerm) {
    String[][] theNormTokens=expandNormTokens(TermNorm.getNormTokens(inRawTerm));
    String[] theNormTerms=new String[theNormTokens.length];
    for (int i=0; i<theNormTerms.length; i++)
      theNormTerms[i]=FormatUtils.join(theNormTokens[i]," ");
    return theNormTerms;
  }

//--------------------------------------------------------------------------------------------------------
// compressNormTokens
//
// Similar to above, but only produce the compressed compound word version
//--------------------------------------------------------------------------------------------------------
  
  public static String[] compressNormTokens(String[] inNormTokens) {
    if (gCompoundSpaced==null) 
      return inNormTokens;

    // Accumulate tokens for the norm term with compressed compound words
    ArrayList theTokenList=new ArrayList();

    // loop over all tokens looking for compound words
    for (int i=0; i<inNormTokens.length; i++) {
      boolean theFound=false;

      // Look for 2 cases: a 3 token spaced variant and a 2 token spaced variant
      for (int j=Math.min(i+3,inNormTokens.length); j>i+1; j--) {
        
        // Create spaced out form of compound word by concatenating tokens
        String theSpacedTokens=inNormTokens[i];
        for (int k=i+1; k<j; k++) 
          theSpacedTokens+=' '+inNormTokens[k];

        // Lookup spaced form
        long theSpacedIndex=gCompoundSpaced.getIndex(theSpacedTokens);
        if (theSpacedIndex!=kNotFound) {
          
          // Found compound word 
          theFound=true;
          
          // Get compressed form
          long theCompressedIndex=gCompoundMap.getLong(theSpacedIndex);
          String theCompressedToken=gCompoundCompressed.getUTF8(theCompressedIndex);
          
          // Add compressed form to token list
          theTokenList.add(theCompressedToken);
          i=j-1; 
          break;
        }
      }
      
      // If no compound word found
      if (!theFound) 
        theTokenList.add(inNormTokens[i]);
    }

    // Return tokens 
    String[] theTokens=(String[]) theTokenList.toArray(new String[theTokenList.size()]);
    return theTokens;
  }

//--------------------------------------------------------------------------------------------------------
// compressNormTerm
//--------------------------------------------------------------------------------------------------------
  
  public static String compressNormTerm(String inRawTerm) {
    return FormatUtils.join(compressNormTokens(TermNorm.getNormTokens(inRawTerm))," "); }
  
}

