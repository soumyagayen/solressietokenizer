//--------------------------------------------------------------------------------------------------------
// TestCharNorm.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// TestCharNorm
//--------------------------------------------------------------------------------------------------------

public class TestCharNorm extends CharNorm {

//--------------------------------------------------------------------------------------------------------
// TestCharNorm constants
//--------------------------------------------------------------------------------------------------------

  public static final String   kNormDir="D:/DataWorkspace/SE5/Refs/Norm/derived";

//--------------------------------------------------------------------------------------------------------
// specificTests
//--------------------------------------------------------------------------------------------------------

  private static void specificTests() throws Exception {
    
    System.out.println("");
    System.out.println(FormatUtils.kDivider);
    System.out.println("// Some specific tests");
    System.out.println(FormatUtils.kDivider);
    System.out.println("");

    String[] kTestTexts=new String[] {
        "mg^2/l",
        "Aa\u2113",          // \u2113 maps to l (lowercase L)
        "Aa\u2116",          // \u2116 maps to no 
        "123ABC",
        "123ABC\u00a9123",   // \u00a9 maps to empty string
        "ABCabc\u00bdabc",   // \u00bd maps to 1/2 (multi-char string)
        "123ABC\u00a9",
        "ABCabc\u00bd",
        "\u00a9123",
        "\u00bdabc", 
        "123ABC\u00a9\u00a9\u00a9123",
        "ABCabc\u00bc\u00bd\u00beabc",
        "ABCabc\u00bc\u00a9\u00beabc",
        "123ABC\u00a9\u00bd\u00a9123",
        "Asperger’s Asperger's",
    };

    int[] theFromForToCharNs=Allocate.newInts(1024);
    for (int i=0; i<kTestTexts.length; i++) {
      String theText=kTestTexts[i];
      String theNormText=normChars(theText,theFromForToCharNs);
      System.out.println(theText+"  --->  "+theNormText);
    }
  }
   
//--------------------------------------------------------------------------------------------------------
// run
//--------------------------------------------------------------------------------------------------------

  private static void run() throws Exception {

    long theStartTime=System.currentTimeMillis();
    System.out.println(FormatUtils.reportHeader("Test Char Norm",theStartTime));

    load(kNormDir);
    specificTests();

    System.out.println(FormatUtils.reportFooter(theStartTime));
  }

//--------------------------------------------------------------------------------------------------------
// main
//--------------------------------------------------------------------------------------------------------

  public static void main(String[] args) {
    try {
      run();
    } catch (Throwable e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
  
}
