//--------------------------------------------------------------------------------------------------------
// TestCompoundExpand.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// TestCompoundExpand
//--------------------------------------------------------------------------------------------------------

public class TestCompoundExpand extends CompoundExpand {

//--------------------------------------------------------------------------------------------------------
// TestCompoundExpand consts
//--------------------------------------------------------------------------------------------------------
  
  public static final String   kNormDir="D:/DataWorkspace/SE5/Refs/Norm/derived";
  
//--------------------------------------------------------------------------------------------------------
// specificTests
//--------------------------------------------------------------------------------------------------------

  public static void specificTests() throws Exception {

    long theStartTime=System.currentTimeMillis();
    System.out.println(FormatUtils.reportHeader("Some specific compounds",theStartTime));

    String[] kTestTerms=new String[] {
        "xray",
        "x-ray",
        "x ray",
        "intraabdominal",
        "bring",
        "b-ring",
        "retina",
        "retin-a", 
        "nonhodgkin's lymphomae"
    };
    
    for (int i=0; i<kTestTerms.length; i++) {
      String theTerm=kTestTerms[i];
      String[] theNormTerms=expandNormTerm(TermNorm.normTerm(kTestTerms[i]));
      System.out.println("");
      System.out.println(theTerm);
      for (int j=0; j<theNormTerms.length; j++)
        System.out.println("    "+theNormTerms[j]);
    }

    System.out.println(FormatUtils.reportFooter(theStartTime));
  }

//--------------------------------------------------------------------------------------------------------
// run
//--------------------------------------------------------------------------------------------------------

  private static void run() throws Exception {

    long theStartTime=System.currentTimeMillis();
    System.out.println(FormatUtils.reportHeader("Test Compound Expand",theStartTime));

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
