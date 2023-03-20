//--------------------------------------------------------------------------------------------------------
// Preferences.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.util.*;

//--------------------------------------------------------------------------------------------------------
// Preferences
//--------------------------------------------------------------------------------------------------------

public class Preferences implements Constants {
  
//--------------------------------------------------------------------------------------------------------
// Preferences consts
//--------------------------------------------------------------------------------------------------------
   
  public static final HashMap     kPrefsMap;
  
//--------------------------------------------------------------------------------------------------------
// Preferences class init
//--------------------------------------------------------------------------------------------------------

  static {
    kPrefsMap=load();
  }
  
//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static HashMap load() {    
    HashMap thePrefsMap=new HashMap(); 

    // First, load filename specified on command line 
    loadFile(System.getProperty("pfile",null),thePrefsMap);  
    
    // Override with local prefs, if they exist
    boolean theLoaded=loadFile("Preferences.properties",thePrefsMap);
    
    // Try alternate spelling of local prefs filename (not on Windows which is not case sensitive)
    if (!theLoaded)
      loadFile("preferences.properties",thePrefsMap);
    
    return thePrefsMap;
  }
  
//--------------------------------------------------------------------------------------------------------
// loadFile
//--------------------------------------------------------------------------------------------------------

  public static boolean loadFile(String inFilename, HashMap inPrefsMap) {
    try {
      if ((inFilename!=null)&&(FileUtils.doesFileExist(inFilename))) {
        String theFullText=TextFileUtils.loadTextFile(inFilename);
        String[] theLines=FormatUtils.breakOnChars(theFullText,'\n');
        for (int i=0; i<theLines.length; i++) {
          String theLine=theLines[i].trim();
          if ((FormatUtils.hasRealContent(theLine))&&       // Skip blank lines
              (!theLine.startsWith("#"))) {                 // Skip contents
            int thePos=theLine.indexOf("=");
            if (thePos!=kNotFound) {
              String theKey=theLine.substring(0,thePos).trim();
              String theValue=theLine.substring(thePos+1).trim();
              if (FormatUtils.hasRealContent(theValue)) 
                if (!FormatUtils.hasRealContent(theKey))
                  throw new RuntimeException("Invalid preference in "+inFilename+", line: "+theLine);
                else
                  inPrefsMap.put(theKey,theValue);
            }
          }
        }
        return true;
      }
    } catch (Exception e) { 
      e.printStackTrace(System.err);
      try { Thread.sleep(k1Sec); } catch (Exception e2) {}
      System.exit(1);
    }
    return false;
  }

//--------------------------------------------------------------------------------------------------------
// getKeys
//--------------------------------------------------------------------------------------------------------

  public static String[] getKeys() {
    return (String[]) kPrefsMap.keySet().toArray(new String[kPrefsMap.size()]); }

//--------------------------------------------------------------------------------------------------------
// getString
//--------------------------------------------------------------------------------------------------------

  public static String getString(String inKey, String inDefaultValue) {
    String theValue=(String) kPrefsMap.get(inKey);
    return (theValue==null)?inDefaultValue:theValue;
  }

  public static String getString(String inKey) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      throw new RuntimeException("Missing preference: "+inKey);
    return theValue;
  }

//--------------------------------------------------------------------------------------------------------
// getBoolean
//--------------------------------------------------------------------------------------------------------

  public static boolean getBoolean(String inKey, boolean inDefaultValue) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      return inDefaultValue;
    try {
      return Boolean.parseBoolean(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid boolean preference: "+inKey+" = "+theValue);
    }
  }

  public static boolean getBoolean(String inKey) {
    String theValue=getString(inKey,null);
    try {
      return Boolean.parseBoolean(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid boolean preference: "+inKey+" = "+theValue);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getShort
//--------------------------------------------------------------------------------------------------------

  public static short getShort(String inKey, short inDefaultValue) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      return inDefaultValue;
    try {
      return Short.parseShort(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid short preference: "+inKey+" = "+theValue);
    }
  }

  public static short getShort(String inKey) {
    String theValue=getString(inKey,null);
    try {
      return Short.parseShort(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid short preference: "+inKey+" = "+theValue);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getInt
//--------------------------------------------------------------------------------------------------------

  public static int getInt(String inKey, int inDefaultValue) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      return inDefaultValue;
    try {
      return Integer.parseInt(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid int preference: "+inKey+" = "+theValue);
    }
  }

  public static int getInt(String inKey) {
    String theValue=getString(inKey,null);
    try {
      return Integer.parseInt(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid int preference: "+inKey+" = "+theValue);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getLong
//--------------------------------------------------------------------------------------------------------

  public static long getLong(String inKey, long inDefaultValue) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      return inDefaultValue;
    try {
      return Long.parseLong(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid long preference: "+inKey+" = "+theValue);
    }
  }

  public static long getLong(String inKey) {
    String theValue=getString(inKey,null);
    try {
      return Long.parseLong(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid long preference: "+inKey+" = "+theValue);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getFloat
//--------------------------------------------------------------------------------------------------------

  public static float getFloat(String inKey, float inDefaultValue) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      return inDefaultValue;
    try {
      return Float.parseFloat(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid float preference: "+inKey+" = "+theValue);
    }
  }

  public static float getFloat(String inKey) {
    String theValue=getString(inKey,null);
    try {
      return Float.parseFloat(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid float preference: "+inKey+" = "+theValue);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getDouble
//--------------------------------------------------------------------------------------------------------

  public static double getDouble(String inKey, double inDefaultValue) {
    String theValue=getString(inKey,null);
    if (theValue==null)
      return inDefaultValue;
    try {
      return Double.parseDouble(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid double preference: "+inKey+" = "+theValue);
    }
  }

  public static double getDouble(String inKey) {
    String theValue=getString(inKey,null);
    try {
      return Double.parseDouble(theValue);
    } catch (Exception e) {
      throw new RuntimeException("Invalid double preference: "+inKey+" = "+theValue);
    }
  }


}
