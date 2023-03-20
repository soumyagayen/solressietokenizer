//--------------------------------------------------------------------------------------------------------
// X2OEasy.java
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import java.util.*;

import gravel.sort.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// X2OEasy.java
//--------------------------------------------------------------------------------------------------------

public abstract class X2OEasy implements Constants {

//--------------------------------------------------------------------------------------------------------
// X2OEasy member vars
//--------------------------------------------------------------------------------------------------------

  private int      mRenderOptions;
  private String   mRenderedXML;
  
//--------------------------------------------------------------------------------------------------------
// getTagname
//--------------------------------------------------------------------------------------------------------

  public abstract String getTagname();

//--------------------------------------------------------------------------------------------------------
// getParent
//--------------------------------------------------------------------------------------------------------

  public abstract X2OEasy getParentElmt();

//--------------------------------------------------------------------------------------------------------
// getElmtN
//--------------------------------------------------------------------------------------------------------

  public abstract int getElmtN();

//--------------------------------------------------------------------------------------------------------
// getDepth
//--------------------------------------------------------------------------------------------------------

  public abstract int getDepth();

//--------------------------------------------------------------------------------------------------------
// getSiblingN
//--------------------------------------------------------------------------------------------------------

  public abstract int getSiblingN();

//--------------------------------------------------------------------------------------------------------
// getNAttrs
//--------------------------------------------------------------------------------------------------------

  public abstract int getNAttrs();

//--------------------------------------------------------------------------------------------------------
// getHasAttrs
//--------------------------------------------------------------------------------------------------------

  public boolean getHasAttrs() { return (getNAttrs()>0); }

//--------------------------------------------------------------------------------------------------------
// getAttrName
//--------------------------------------------------------------------------------------------------------

  public abstract String getAttrName(int inIndex);

//--------------------------------------------------------------------------------------------------------
// getAttrNames
//--------------------------------------------------------------------------------------------------------

  public abstract String[] getAttrNames();

//--------------------------------------------------------------------------------------------------------
// getAttr
//--------------------------------------------------------------------------------------------------------

  public abstract String getAttr(int inIndex);

//--------------------------------------------------------------------------------------------------------
// getAttrs
//--------------------------------------------------------------------------------------------------------

  public abstract String[] getAttrs();

//--------------------------------------------------------------------------------------------------------
// getAttr
//--------------------------------------------------------------------------------------------------------

  public String getAttr(String inAttrName) {
    if (!getHasAttrs())
      return null;
    for (int i=getNAttrs()-1; i>=0; i--)
      if (getAttrName(i).equals(inAttrName))
        return getAttr(i);
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getAttrMap
//--------------------------------------------------------------------------------------------------------

  public HashMap getAttrMap() { 
    HashMap theMap=new HashMap();
    if (getHasAttrs()) 
      for (int i=getNAttrs()-1; i>=0; i--)
        theMap.put(getAttrName(i),getAttr(i));
    return theMap;
  }

//--------------------------------------------------------------------------------------------------------
// Contents are chars between child tags 
// For markup, these are significant content data
// For structured data with child elmts, these are just tag indents
// For structured data without child elmts, there is only one content and it is significant 
// In this last case, the content is called the tag text, and can be retrieved with getText() below
//--------------------------------------------------------------------------------------------------------

//--------------------------------------------------------------------------------------------------------
// getIsEmpty   ( tests if tag empty and therefore gets trailing /> )
//--------------------------------------------------------------------------------------------------------

  public boolean getIsEmpty() { return ((!getHasChildElmts())&&(!getHasText())); }
  
//--------------------------------------------------------------------------------------------------------
// getNContents
//--------------------------------------------------------------------------------------------------------

  public abstract int getNContents();
  
//--------------------------------------------------------------------------------------------------------
// getHasText
//--------------------------------------------------------------------------------------------------------

  public boolean getHasText() { 
    return ((getNContents()==1)&&(FormatUtils.hasRealContent(getContent(0)))); } 

//--------------------------------------------------------------------------------------------------------
// getContent
//--------------------------------------------------------------------------------------------------------

  public abstract String getContent(int inIndex);

//--------------------------------------------------------------------------------------------------------
// getText
//--------------------------------------------------------------------------------------------------------

  public String getText() { return (getHasText())?getContent(0):null; }

//--------------------------------------------------------------------------------------------------------
// getFirstContent
//--------------------------------------------------------------------------------------------------------

  public String getFirstContent() { return ((getNContents()==0)?null:getContent(0)); }

//--------------------------------------------------------------------------------------------------------
// getLastContent
//--------------------------------------------------------------------------------------------------------

  public String getLastContent() { return ((getNContents()==0)?null:getContent(getNContents()-1)); }

//--------------------------------------------------------------------------------------------------------
// getContents
//--------------------------------------------------------------------------------------------------------

  public abstract String[] getContents();

//--------------------------------------------------------------------------------------------------------
// getNChildElmts
//--------------------------------------------------------------------------------------------------------

  public abstract int getNChildElmts();

//--------------------------------------------------------------------------------------------------------
// getHasChildElmts
//--------------------------------------------------------------------------------------------------------

  public boolean getHasChildElmts() { return (getNChildElmts()>0); }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public abstract Object getChildElmt(int inIndex);

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getFirstChildElmt() { return ((getNChildElmts()==0)?null:getChildElmt(0)); }

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getLastChildElmt() { return ((getNChildElmts()==0)?null:getChildElmt(getNChildElmts()-1)); }

//--------------------------------------------------------------------------------------------------------
// getNextSiblingElmt
//--------------------------------------------------------------------------------------------------------

  public Object getNextSiblingElmt() {
    X2OEasy theParent=getParentElmt();
    if (theParent==null)
      return null;
    int theChildN=getSiblingN();
    if (theChildN>=theParent.getNChildElmts()-1)
      return null;
    return theParent.getChildElmt(theChildN+1);
  }

//--------------------------------------------------------------------------------------------------------
// getPrevSiblingElmt
//--------------------------------------------------------------------------------------------------------

  public Object getPrevSiblingElmt() {
    X2OEasy theParent=getParentElmt();
    if (theParent==null)
      return null;
    int theChildN=getSiblingN();
    if (theChildN<1)
      return null;
    return theParent.getChildElmt(theChildN-1);
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public abstract Object[] getChildElmts();

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getChildElmts(Object[] ioElmts) {
    Object[] theElmts=getChildElmts();
    for (int i=0; i<theElmts.length; i++)
      ioElmts[i]=theElmts[i];
    return ioElmts;
  }

//--------------------------------------------------------------------------------------------------------
// getNChildTagnames
//--------------------------------------------------------------------------------------------------------

  public abstract int getNChildTagnames();

//--------------------------------------------------------------------------------------------------------
// getChildTagname
//--------------------------------------------------------------------------------------------------------

  public abstract String getChildTagname(int inIndex);

//--------------------------------------------------------------------------------------------------------
// getChildTagnames
//--------------------------------------------------------------------------------------------------------

  public abstract String[] getChildTagnames();

//--------------------------------------------------------------------------------------------------------
// getNChildElmts
//--------------------------------------------------------------------------------------------------------

  public abstract int getNChildElmts(String inTagname);

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public abstract Object getChildElmt(String inTagname, int inIndex);

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public abstract Object getFirstChildElmt(String inTagname);

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public abstract Object getLastChildElmt(String inTagname);

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public abstract Object[] getChildElmts(String inTagname);

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getChildElmts(String inTagname, Object[] ioElmts) {
    Object[] theElmts=getChildElmts(inTagname);
    for (int i=0; i<theElmts.length; i++)
      ioElmts[i]=theElmts[i];
    return ioElmts;
  }

//--------------------------------------------------------------------------------------------------------
// getXML
//--------------------------------------------------------------------------------------------------------

  public String getXML(
      int       inIndent,
      boolean   inSqueezeWhitespace, 
      boolean   inTrimTexts, 
      boolean   inWrapLongTexts, 
      boolean   inSortChildTags,
      int       inRenderOptions) {
    
    // Keep copy of rendered XML so parent can sort children
    // Reduces rendering time from N^2 to N, but increase RAM usage from N to N ln N.
    // Should be transient, as in X2OParser.reformatXML()
    
    if ((mRenderedXML==null)||(mRenderOptions!=inRenderOptions)) {
      mRenderOptions=inRenderOptions;
      
      mRenderedXML=new O2XBuilder(inIndent,false,inWrapLongTexts,false) {
        public void buildXML() {
          int theNAttrs=getNAttrs();
          for (int i=0; i<theNAttrs; i++) {
            String theAttr=getAttr(i);
            if (inSqueezeWhitespace)
              theAttr=FormatUtils.squeezeWhitespace(theAttr);  // includes trim()
            if (inTrimTexts)
              theAttr=theAttr.trim();
            addAttr(getAttrName(i),theAttr);        
          }
          String theTagname=getTagname();
          if (getIsEmpty()) 
            addEmptyTag(theTagname);
          else if (getHasText()) {
            String theText=getText();
            if (inSqueezeWhitespace)
              theText=FormatUtils.squeezeWhitespace(theText);  // includes trim()
            if (inTrimTexts)
              theText=theText.trim();
            addTagWithContent(theTagname,theText);    // Escapes text  
          } else {
            addStartTag(theTagname);
            int theNElmts=getNChildElmts();  
            int[] theSortMap=null;
            if (inSortChildTags) {
              String[] theChildXMLs=new String[theNElmts];
              for (int i=0; i<theNElmts; i++) {
                X2OEasy theChildElmt=((X2OEasy) getChildElmt(i));
                theChildXMLs[i]=theChildElmt.getXML(getIndent(),inSqueezeWhitespace,
                    inTrimTexts,inWrapLongTexts,inSortChildTags)+" |"+FormatUtils.leftPad(i,4);
              }
              theSortMap=SortMapUtils.sortMap(theChildXMLs);
            }
            for (int i=0; i<theNElmts; i++) {
              if (getIndent()==O2XBuilder.kDontChangeIndent) 
                addContent(getContent(i));              // Escapes text 
              Object theElmt;
              if (inSortChildTags)
                theElmt=getChildElmt(theSortMap[i]);
              else
                theElmt=getChildElmt(i);
              if (theElmt instanceof String)
                addXML((String) theElmt);               // Hope child elmts are xml
              else if (theElmt instanceof X2OEasy)
                ((X2OEasy) theElmt).getXML(getIndent(),inSqueezeWhitespace,
                    inTrimTexts,inWrapLongTexts,inSortChildTags,getBuffer());
              else
                throw new RuntimeException("Unrecognized object cannot build XML");
            }
            if (getIndent()==O2XBuilder.kDontChangeIndent) 
              addContent(getContent(theNElmts));        // Escapes text 
            addEndTag();
          }
        }
      }.getXML();
    }
    return mRenderedXML;
  }

  public String getXML(int inIndent, boolean inSqueezeWhitespace, 
      boolean inTrimTexts, boolean inWrapLongTexts, boolean inSortChildTags) {    
    int theRenderOptions=
        (inSqueezeWhitespace?2:0)+
        (inTrimTexts?4:0)+
        (inWrapLongTexts?8:0)+
        (inSortChildTags?16:0)+
        (inIndent<<5);
    return getXML(inIndent,inSqueezeWhitespace,inTrimTexts,
        inWrapLongTexts,inSortChildTags,theRenderOptions);
  }

  public void getXML(int inIndent, boolean inSqueezeWhitespace, 
      boolean inTrimTexts, boolean inWrapLongTexts, boolean inSortChildTags, StringBuffer inBuffer) {
    inBuffer.append(getXML(inIndent,inSqueezeWhitespace,inTrimTexts,
        inWrapLongTexts,inSortChildTags));
  }

  public void getXML(int inIndent, StringBuffer inBuffer) { 
    getXML(inIndent,false,false,false,false,inBuffer); }
  
  public String getXML(int inIndent) { return getXML(inIndent,false,false,false,false); }
  
  public String getXML() { return getXML(0); }
  
  
  
  

//========================================================================================================
// Convenience routines
//
// Used to get typed Attrs with default values
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// getAttr
//--------------------------------------------------------------------------------------------------------

  public String getAttr(String inAttrName, String inDefaultValue) {
    String theValueString=getAttr(inAttrName);
    if (!FormatUtils.hasRealContent(theValueString))
      return inDefaultValue;
    else
      return theValueString;
  }

//--------------------------------------------------------------------------------------------------------
// getBooleanAttr
//--------------------------------------------------------------------------------------------------------

  public boolean getBooleanAttr(String inAttrName, boolean inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (theValueString==null)
      return inDefaultValue;
    try {
      return Boolean.parseBoolean(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public boolean getBooleanAttr(String inAttrName) throws Exception { 
    return getBooleanAttr(inAttrName,false); }

//--------------------------------------------------------------------------------------------------------
// getByteAttr
//--------------------------------------------------------------------------------------------------------

  public byte getByteAttr(String inAttrName, byte inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (theValueString==null)
      return inDefaultValue;
    try {
      return Byte.parseByte(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public byte getByteAttr(String inAttrName) throws Exception { 
    return getByteAttr(inAttrName,(byte) kNotFound); }

//--------------------------------------------------------------------------------------------------------
// getCharAttr
//--------------------------------------------------------------------------------------------------------

  public char getCharAttr(String inAttrName, char inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if ((theValueString==null)||(theValueString.length()<1))
      return inDefaultValue;
    try {
      return theValueString.charAt(0);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public char getCharAttr(String inAttrName) throws Exception { return getCharAttr(inAttrName,(char) 0); }

//--------------------------------------------------------------------------------------------------------
// getIntAttr
//--------------------------------------------------------------------------------------------------------

  public int getIntAttr(String inAttrName, int inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (theValueString==null)
      return inDefaultValue;
    try {
      return Integer.parseInt(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public int getIntAttr(String inAttrName) throws Exception { return getIntAttr(inAttrName,kNotFound); }

//--------------------------------------------------------------------------------------------------------
// getLongAttr
//--------------------------------------------------------------------------------------------------------

  public long getLongAttr(String inAttrName, long inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (theValueString==null)
      return inDefaultValue;
    try {
      return Long.parseLong(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public long getLongAttr(String inAttrName) throws Exception { return getLongAttr(inAttrName,kNotFound); }

//--------------------------------------------------------------------------------------------------------
// getDoubleAttr
//--------------------------------------------------------------------------------------------------------

  public double getDoubleAttr(String inAttrName, double inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (theValueString==null)
      return inDefaultValue;
    try {
      return Double.parseDouble(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public double getDoubleAttr(String inAttrName) throws Exception { return getDoubleAttr(inAttrName,kNotFound); }

//--------------------------------------------------------------------------------------------------------
// getFloatAttr
//--------------------------------------------------------------------------------------------------------

  public float getFloatAttr(String inAttrName, float inDefaultValue) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (theValueString==null)
      return inDefaultValue;
    try {
      return Float.parseFloat(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

  public float getFloatAttr(String inAttrName) throws Exception { return getFloatAttr(inAttrName,kNotFound); }

//--------------------------------------------------------------------------------------------------------
// getText
//--------------------------------------------------------------------------------------------------------

  public String getText(String inDefaultValue) {
    String theValueString=getText();
    // An empty or whitespace string is considered a missing value, and replaced by default
    if (!FormatUtils.hasRealContent(theValueString))
      return inDefaultValue;
    else
      return theValueString;
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getChildElmt(int inIndex, Object inDefaultValue) {
    Object theObject=getChildElmt(inIndex);
    if (theObject==null)
      return inDefaultValue;
    else
      return theObject;
  }

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getFirstChildElmt(Object inDefaultValue) { 
    return getChildElmt(0,inDefaultValue); }

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getLastChildElmt(Object inDefaultValue) { 
    return getChildElmt(getNChildElmts()-1,inDefaultValue); }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getChildElmts(Object inDefaultValues) {
    int theNElmts=getNChildElmts();
    if (theNElmts==0)
      return (Object[]) inDefaultValues;
    else
      return getChildElmts();
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getChildElmt(String inTagname, int inIndex, Object inDefaultValues) {
    Object theObject=getChildElmt(inTagname,inIndex);
    if (theObject==null)
      return inDefaultValues;
    else
      return theObject;
  }

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getFirstChildElmt(String inTagname, Object inDefaultValues) {
    return getChildElmt(inTagname,0,inDefaultValues); }

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getLastChildElmt(String inTagname, Object inDefaultValues) {
    return getChildElmt(inTagname,getNChildElmts(inTagname)-1,inDefaultValues); }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getChildElmts(String inTagname, Object inDefaultValues) {
    int theNElmts=getNChildElmts(inTagname);
    if (theNElmts==0)
      return (Object[]) inDefaultValues;
    else
      return getChildElmts(inTagname);
  }

  

  

//========================================================================================================
// More convenience routines
//
// Used to require typed Attrs - will throw exception if missing
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// getRequiredAttr
//--------------------------------------------------------------------------------------------------------

  public String getRequiredAttr(String inAttrName) throws Exception {
    String theValueString=getAttr(inAttrName);
    if (!FormatUtils.hasRealContent(theValueString))
      throw new Exception("Missing Attr, "+inAttrName+", in <"+getTagname()+">");
    else
      return theValueString;
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredBooleanAttr
//--------------------------------------------------------------------------------------------------------

  public boolean getRequiredBooleanAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return Boolean.parseBoolean(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredByteAttr
//--------------------------------------------------------------------------------------------------------

  public byte getRequiredByteAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return Byte.parseByte(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredCharAttr
//--------------------------------------------------------------------------------------------------------

  public char getRequiredCharAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return theValueString.charAt(0);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// getRequiredIntAttr
//--------------------------------------------------------------------------------------------------------

  public int getRequiredIntAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return Integer.parseInt(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }
   
//--------------------------------------------------------------------------------------------------------
// getRequiredLongAttr
//--------------------------------------------------------------------------------------------------------

  public long getRequiredLongAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return Long.parseLong(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredDoubleAttr
//--------------------------------------------------------------------------------------------------------

  public double getRequiredDoubleAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return Double.parseDouble(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// getRequiredFloatAttr
//--------------------------------------------------------------------------------------------------------

  public float getRequiredFloatAttr(String inAttrName) throws Exception {
    String theValueString=getRequiredAttr(inAttrName);
    try {
      return Float.parseFloat(theValueString);
    } catch (Exception e) {
      throw new Exception("Invalid Attr, "+inAttrName+"=\""+theValueString+"\", in <"+getTagname()+">",e);
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// getRequiredText
//--------------------------------------------------------------------------------------------------------

  public String getRequiredText() throws Exception {
    String theValueString=getText();
    // An empty or whitespace string is considered a missing value, and replaced by default
    if (!FormatUtils.hasRealContent(theValueString))
      throw new Exception("Missing text in <"+getTagname()+">");
    else
      return theValueString;
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getRequiredChildElmts(String inTagname) throws Exception {
    int theNElmts=getNChildElmts(inTagname);
    if (theNElmts==0)
      throw new Exception("Missing <"+inTagname+"> Elmts in <"+getTagname()+">");
    else
      return getChildElmts(inTagname);
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredSingleChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getRequiredSingleChildElmt(String inTagname) throws Exception {
    int theNElmts=getNChildElmts(inTagname);
    if (theNElmts==0)
      throw new Exception("Missing <"+inTagname+"> Elmts in <"+getTagname()+">");
    else if (theNElmts>1)
      throw new Exception("Multiple <"+inTagname+"> Elmts in <"+getTagname()+">");
    else
      return getFirstChildElmt(inTagname);
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getRequiredChildElmts() throws Exception {
    int theNElmts=getNChildElmts();
    if (theNElmts==0)
      throw new Exception("Missing Elmts in <"+getTagname()+">");
    else
      return getChildElmts();
  }

//--------------------------------------------------------------------------------------------------------
// getRequiredSingleChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getRequiredSingleChildElmt() throws Exception {
    int theNElmts=getNChildElmts();
    if (theNElmts==0)
      throw new Exception("Missing Elmts in <"+getTagname()+">");
    else if (theNElmts>1)
      throw new Exception("Multiple Elmts in <"+getTagname()+">");
    else
      return getFirstChildElmt();
  }

}
