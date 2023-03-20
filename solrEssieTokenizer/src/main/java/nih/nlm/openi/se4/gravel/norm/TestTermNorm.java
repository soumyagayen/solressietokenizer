//--------------------------------------------------------------------------------------------------------
// TestTermNorm.java
//--------------------------------------------------------------------------------------------------------

package gravel.norm;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// TestTermNorm
//--------------------------------------------------------------------------------------------------------

public class TestTermNorm extends TermNorm {

//--------------------------------------------------------------------------------------------------------
// TestTermNorm consts
//--------------------------------------------------------------------------------------------------------

  public static final String   kNormDir="D:/DataWorkspace/SE5/Refs/Norm/derived";

//--------------------------------------------------------------------------------------------------------
// specificNorms
//--------------------------------------------------------------------------------------------------------

  public static void specificNorms() throws Exception {

    long theStartTime=System.currentTimeMillis();
    System.out.println(FormatUtils.reportHeader("Some specific norms",theStartTime));

    String[] kTestTerms=new String[] {
        "pot",
        "Waldenstrom**** Macroglobulinemia",
        "Waldenstrom’s macroglobulinemia",
        "between 8%--26% have",
        "http://www.nida.nih.gov/",
        "\"ALS\"",
        "'ALS'",
        "\"ALS",
        "'ALS",
        "ALS\"",
        "ALS'",
        "≥38.0˚C",
        "100.5˚F",
        "360˚", 
        "-1,000.00mg^2/l",
        "24hour",
        "24-hour",
        "mg/m2/day",  
        "(6.1-7",
        "-1",
        "--1",
        "1-2",
        "-1-2",
        "--1-2",
        "-alpha",
        "HIV-",
        "Therapy--",
        "--Patient",
        "A-1",
        "5-FU",
        "II-IV",
        "≥18",
        "3,000<=3,000.1, 3,000,000,",
        "Title:",
        "non-hodgkin's lymphoma",
        "albinism, ocular",
        "USA.",
        "U.S.A.",
        "(U.S.A.)", 
        "(U.S.A.).",
        "(4.5 hours)",
        "ca(+2).",
        "ca+2",
        "ca(+2.",
        "prpl@mail.cc.nih.gov",
        "1-800-411-1222",
        "Methotrexate [Mass/volume] in Serum or Plasma --1-2 weeks post dose",
        "x ray",
        "x-ray",
        "xray",
        "retin a",
        "retin-a",
        "retina",
        "b ring",
        "b-ring",
        "bring",
        "S000049Z",
        "(Lymphoma, Non-Hodjkin's?)",
        "\u00a9 Avg. \u00bc patients's x-ray in U.S.A.",
        "Cl-- --i x-ray x--ray x---ray x-ray-vision - -- --- ---- ----------- -1 --1 ---1 ----1 HIV- HIV-- HIV--- HIV----",
        "hyper's expand Jak2 P2P",
        "A Phase III trial of Erlotinib (TarcevaÃÂ®)",
        "cancers cancers- cancers)",
        "1-2 1-a 1-) 1-", 
        "= == === ==== ===== ====================================", 
    };
    
    int[] theCharNs=SliceStore.getSliceStore().getIntSlice();
    int[] theNChars=SliceStore.getSliceStore().getIntSlice();
    
    for (int i=0; i<kTestTerms.length; i++) {
      System.out.println("");
      
      String theTerm=kTestTerms[i];
      String theNormTerm=TermNorm.normTerm(theTerm);
      System.out.println(theTerm+"  -->  "+theNormTerm);
      
      String[] theNormTokens=TermNorm.getNormTokens(theTerm,theCharNs,theNChars);
      for (int j=0; j<theNormTokens.length; j++)
        System.out.println("    "+theTerm.substring(theCharNs[j],theCharNs[j]+theNChars[j])+"  -->  "+theNormTokens[j]);
    }

    SliceStore.getSliceStore().putIntSlice(theCharNs);
    SliceStore.getSliceStore().putIntSlice(theNChars);

    System.out.println(FormatUtils.reportFooter(theStartTime));
  }
 
//--------------------------------------------------------------------------------------------------------
// specificExpansions
//--------------------------------------------------------------------------------------------------------

  public static void specificExpansions() throws Exception {

    long theStartTime=System.currentTimeMillis();
    System.out.println(FormatUtils.reportHeader("Some specific expansions",theStartTime));

    String[] kTestTerms=new String[] {
        "pot",
        "POTS",
        "Pot's",
        "heart attack",
    };
    
    int[] theCharNs=SliceStore.getSliceStore().getIntSlice();
    int[] theNChars=SliceStore.getSliceStore().getIntSlice();
    
    for (int i=0; i<kTestTerms.length; i++) {
      System.out.println("");
      
      String theTerm=kTestTerms[i];
      String[] theNormTokens=TermNorm.getNormTokens(theTerm);
      System.out.println(theTerm+"  -->  "+normTerm(theNormTokens));
      
      String[][] theExpansionTokens=TermNorm.expandInflections(theNormTokens);
      for (int j=0; j<theExpansionTokens.length; j++)
        System.out.println("    "+theNormTokens[j]+"  -->  "+normTerm(theExpansionTokens[j]));
    }

    SliceStore.getSliceStore().putIntSlice(theCharNs);
    SliceStore.getSliceStore().putIntSlice(theNChars);

    System.out.println(FormatUtils.reportFooter(theStartTime));
  }

//--------------------------------------------------------------------------------------------------------
// run
//--------------------------------------------------------------------------------------------------------

  private static void run() throws Exception {

    long theStartTime=System.currentTimeMillis();
    System.out.println(FormatUtils.reportHeader("Test Term Norm",theStartTime));

    load(kNormDir);
    specificNorms();
    specificExpansions();

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
