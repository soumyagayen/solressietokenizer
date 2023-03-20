//--------------------------------------------------------------------------------------------------------
// O2XBuilder.java
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import java.io.*;
import java.util.*;

import gravel.store.plain.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// O2XBuilder.java
//--------------------------------------------------------------------------------------------------------

public abstract class O2XBuilder implements O2XConsts {

//--------------------------------------------------------------------------------------------------------
// O2XBuilder consts
//--------------------------------------------------------------------------------------------------------

  public static final String   kBar=
      "==========================================================================================";

//--------------------------------------------------------------------------------------------------------
// inner class TagInfo
//--------------------------------------------------------------------------------------------------------

  public static class TagInfo {
    private String     mTagname;
    private boolean    mHasAttrs;
    private long       mBeforeTagPos;
    private long       mAfterTagPos;
    
    public TagInfo(String inTagname, boolean inHasAttrs, long inBeforeTagPos, long inAfterTagPos) {
      mTagname=inTagname;
      mHasAttrs=inHasAttrs;
      mBeforeTagPos=inBeforeTagPos;
      mAfterTagPos=inAfterTagPos;
    }
    
    public String getTagname() { return mTagname; }
    public boolean getHasAttrs() { return mHasAttrs; }
    public long getBeforeTagPos() { return mBeforeTagPos; }
    public long getAfterTagPos() { return mAfterTagPos; }
  }

//--------------------------------------------------------------------------------------------------------
// O2XBuilder member vars
//--------------------------------------------------------------------------------------------------------

  private int              mStartIndent;
  private boolean          mShowEmpties;
  private boolean          mWrapLongTexts;
  private boolean          mUsePCEOL;
  private int              mIndent;
  private long             mLength;
  private String           mTagPrefix;
  private StringBuffer     mAttrBuffer;
  private StringBuffer     mBuffer;
  private ArrayList        mTagInfoStack;
  private ByteRAMStore     mUTF8Store;
  private BufferedWriter   mWriter;

//--------------------------------------------------------------------------------------------------------
// O2XBuilder
//--------------------------------------------------------------------------------------------------------

  public O2XBuilder(int inStartIndent, boolean inShowEmpties, boolean inWrapLongTexts, boolean inUsePCEOL) {
    mStartIndent=inStartIndent;
    mShowEmpties=inShowEmpties;
    mWrapLongTexts=inWrapLongTexts;
    mUsePCEOL=inUsePCEOL;
    mIndent=inStartIndent;
  }

  public O2XBuilder(int inStartIndent) { this(inStartIndent,false,false,false); }
  public O2XBuilder() { this(0); }

//--------------------------------------------------------------------------------------------------------
// gets
//--------------------------------------------------------------------------------------------------------

  public int getStartIndent() { return mStartIndent; }
  public boolean getShowEmpties() { return mShowEmpties; }
  public boolean getWrapLongTexts() { return mWrapLongTexts; }
  public boolean getUseCarriageReturns() { return mUsePCEOL; }
  public boolean getHasIndent() { return (mIndent>=0); }
  public int getIndent() { return mIndent; }

//--------------------------------------------------------------------------------------------------------
// getBuffer
//
// Used when buildXML() in O2XBuilder 1 calls O2XBuilder 2, and wants 2 to write directly into 1's buffer
//--------------------------------------------------------------------------------------------------------

  public StringBuffer getBuffer() { return mBuffer; }

//--------------------------------------------------------------------------------------------------------
// buildXML
//--------------------------------------------------------------------------------------------------------

  public abstract void buildXML();

//--------------------------------------------------------------------------------------------------------
// checkDone
//--------------------------------------------------------------------------------------------------------

  private void checkDone() {
    store(true); 
    if (mAttrBuffer!=null)
      throw new RuntimeException("Pending Attrs: "+mAttrBuffer.toString());
    if (mTagPrefix!=null)
      throw new RuntimeException("Pending tag prefix: "+mTagPrefix);
    if (mTagInfoStack.size()!=0)
      throw new RuntimeException("Mismatched start and end tags: "+
          ((TagInfo) mTagInfoStack.get(mTagInfoStack.size()-1)).getTagname());
    if (mIndent!=mStartIndent)
      throw new RuntimeException("Mismatched start and end indent: "+mIndent+"!="+mStartIndent);
  }

//--------------------------------------------------------------------------------------------------------
// getXML
//--------------------------------------------------------------------------------------------------------

  public void getXML(StringBuffer inBuffer) {
    mBuffer=inBuffer;
    mTagInfoStack=new ArrayList();
    buildXML();
    checkDone();
    mBuffer=null;
    mTagInfoStack=null;
  }

  public String getXML() {
    StringBuffer theXMLBuffer=new StringBuffer();
    getXML(theXMLBuffer);
    return theXMLBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// getUTF8XML
//--------------------------------------------------------------------------------------------------------

  public void getUTF8XML(ByteRAMStore inUTF8Store) {
    mUTF8Store=inUTF8Store;
    getXML(new StringBuffer());
    mUTF8Store=null;
  }

  public byte[] getUTF8XML() {
    ByteRAMStore theUTF8Store=new ByteRAMStore(1024);
    getUTF8XML(theUTF8Store);
    byte[] theBytes=theUTF8Store.getAllBytes();
    theUTF8Store.close();
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// saveXML
//--------------------------------------------------------------------------------------------------------

  public void saveXML(BufferedWriter inWriter) throws Exception {
    mWriter=inWriter;
    getXML(new StringBuffer());
    mWriter=null;
  }

  public void saveXML(String inFilename) throws Exception {
    BufferedWriter theWriter=null;
    try {
      theWriter=TextFileUtils.openUTF8Writer(inFilename);
      saveXML(theWriter);
      theWriter.flush();
    } finally { 
      if (theWriter!=null)
        theWriter.close();
    }
  }

//--------------------------------------------------------------------------------------------------------
// store
//--------------------------------------------------------------------------------------------------------

  public void store(boolean inForce) {
    if ((inForce)||(mBuffer.length()>k1K))
      if (mUTF8Store!=null) {
        mUTF8Store.appendUTF8(mBuffer.toString());
        mLength+=mBuffer.length();
        mBuffer.setLength(0);
      } else if (mWriter!=null) {
        try { mWriter.write(mBuffer.toString()); } catch (Exception e) { throw new RuntimeException(e); }
        mLength+=mBuffer.length();
        mBuffer.setLength(0);
      }
  }

  public void store() { store(false); }

//--------------------------------------------------------------------------------------------------------
// addAttrs
//--------------------------------------------------------------------------------------------------------

  public void addAttrs(String inAttrs) {
    if (!mShowEmpties)
      if (!FormatUtils.hasRealContent(inAttrs))
        return;
    if (mAttrBuffer==null)
      mAttrBuffer=new StringBuffer();
    mAttrBuffer.append(inAttrs);
  }

//--------------------------------------------------------------------------------------------------------
// makeAttr
//--------------------------------------------------------------------------------------------------------

  public String makeAttr(String inAttrName, String inAttrValue) {
    if (!FormatUtils.hasRealContent(inAttrValue))
      return "";
    else {
      StringBuffer theAttrBuffer=new StringBuffer(20+inAttrName.length()+inAttrValue.length());
      theAttrBuffer.append(' ');
      theAttrBuffer.append(inAttrName);
      theAttrBuffer.append("=\"");
      EscapeUtils.escapeXML(theAttrBuffer,inAttrValue);
      theAttrBuffer.append('\"');
      return theAttrBuffer.toString();
    }
  }

//--------------------------------------------------------------------------------------------------------
// addAttr
//--------------------------------------------------------------------------------------------------------

  public void addAttr(String inAttrName, String inAttrValue) {
    addAttrs(makeAttr(inAttrName,inAttrValue)); }

  public void addAttr(String inAttrName, boolean inAttrValue) {
    addAttr(inAttrName,Boolean.toString(inAttrValue)); }

  public void addAttr(String inAttrName, byte inAttrValue) {
    addAttr(inAttrName,Byte.toString(inAttrValue)); }

  public void addAttr(String inAttrName, char inAttrValue) {
    addAttr(inAttrName,Character.toString(inAttrValue)); }

  public void addAttr(String inAttrName, long inAttrValue) {
    addAttr(inAttrName,Long.toString(inAttrValue)); }

  public void addAttr(String inAttrName, double inAttrValue) {
    addAttr(inAttrName,Double.toString(inAttrValue)); }

//--------------------------------------------------------------------------------------------------------
// addAttr
//--------------------------------------------------------------------------------------------------------

  public void addAttr(String inAttrName, String inAttrValue, String inDefault) {
    if (inDefault==null) 
      addAttr(inAttrName,inAttrValue);
    else 
      if (!inDefault.equals(inAttrValue))
        addAttr(inAttrName,inAttrValue);
  }

  public void addAttr(String inAttrName, boolean inAttrValue, boolean inDefault) {
    if (inAttrValue!=inDefault)
      addAttr(inAttrName,inAttrValue);
  }

  public void addAttr(String inAttrName, byte inAttrValue, byte inDefault) {
    if (inAttrValue!=inDefault)
      addAttr(inAttrName,inAttrValue);
  }

  public void addAttr(String inAttrName, char inAttrValue, char inDefault) {
    if (inAttrValue!=inDefault)
      addAttr(inAttrName,inAttrValue);
  }

  public void addAttr(String inAttrName, long inAttrValue, long inDefault) {
    if (inAttrValue!=inDefault)
      addAttr(inAttrName,inAttrValue);
  }

  public void addAttr(String inAttrName, double inAttrValue, double inDefault) {
    if (inAttrValue!=inDefault)
      addAttr(inAttrName,inAttrValue);
  }

//--------------------------------------------------------------------------------------------------------
// addTagPrefix
//--------------------------------------------------------------------------------------------------------

  public void addTagPrefix(String inTagPrefix) {
    if (mTagPrefix!=null)
      throw new RuntimeException("Tag prefix already exists: "+mTagPrefix);
    mTagPrefix=inTagPrefix;
  }

//--------------------------------------------------------------------------------------------------------
// addIndent
//--------------------------------------------------------------------------------------------------------

  private void addIndent() {
    if (getHasIndent())
      mBuffer.append(FormatUtils.blanks(mIndent));
  }

//--------------------------------------------------------------------------------------------------------
// addEOL
//--------------------------------------------------------------------------------------------------------

  private void addEOL() {
    if (getHasIndent())
      if (mUsePCEOL)
        mBuffer.append(kPCEOL);
      else
        mBuffer.append(kEOL);
  }

//--------------------------------------------------------------------------------------------------------
// beginStartTag
//--------------------------------------------------------------------------------------------------------

  private void beginStartTag(String inTagname) {
    addIndent();
    mBuffer.append('<');
    mBuffer.append(inTagname);
    if (mAttrBuffer!=null) {
      mBuffer.append(mAttrBuffer);
      mAttrBuffer=null;
    }
  }

//--------------------------------------------------------------------------------------------------------
// addEmptyTag
//--------------------------------------------------------------------------------------------------------

  public void addEmptyTag(String inTagname, boolean inRemoveEmptyTags) {
    boolean theHasAttrs=(mAttrBuffer!=null);
    if ((theHasAttrs)||(!inRemoveEmptyTags)) {
      String theTagname=inTagname;
      if (mTagPrefix!=null) {
        theTagname=mTagPrefix+theTagname;
        mTagPrefix=null;
      }
      beginStartTag(theTagname);
      mBuffer.append("/>");
      addEOL();
      store(false);
    }
  }

  public void addEmptyTag(String inTagname) { addEmptyTag(inTagname,false); }
  
//--------------------------------------------------------------------------------------------------------
// addStartTag
//--------------------------------------------------------------------------------------------------------

  public void addStartTag(String inTagname) {
    boolean theHasAttrs=(mAttrBuffer!=null);
    String theTagname=inTagname;
    if (mTagPrefix!=null) {
      theTagname=mTagPrefix+theTagname;
      mTagPrefix=null;
    }
    long theBeforeTagPos=mLength+mBuffer.length();
    beginStartTag(theTagname);
    mBuffer.append('>');
    long theAfterTagPos=mLength+mBuffer.length();
    mTagInfoStack.add(new TagInfo(theTagname,theHasAttrs,theBeforeTagPos,theAfterTagPos));
    if (getHasIndent())
      mIndent+=kIndentStepSize;
  }

//--------------------------------------------------------------------------------------------------------
// addContent
//--------------------------------------------------------------------------------------------------------

  public void addContent(String inContent) {
    if (inContent!=null) {
      EscapeUtils.escapeXML(mBuffer,inContent); 
      // Tried normalizing EOLs, but backed out - interferes with chinzy markup
      // 
      //    String theContent=inContent;
      //    if (inContent.indexOf('\n')!=kNotFound)
      //      if (mUsePCEOL)
      //        theContent=FormatUtils.normalizePCEOLs(theContent);
      //      else
      //        theContent=FormatUtils.normalizeEOLs(theContent);
      //    EscapeUtils.escapeXML(mBuffer,theContent); 
    }
  }

//--------------------------------------------------------------------------------------------------------
// addO2XObject
//--------------------------------------------------------------------------------------------------------

  public void addO2XObject(O2XIntf inO2XObject) { 
    if (inO2XObject!=null) {
      inO2XObject.toXML(mIndent,mBuffer); 
      store(false);
    }
  }

//--------------------------------------------------------------------------------------------------------
// addXML
//--------------------------------------------------------------------------------------------------------

  public void addXML(String inXML) {
    if (inXML!=null) {
      mBuffer.append(inXML);
      store(false);
    }
  }

//--------------------------------------------------------------------------------------------------------
// addEndTag
//--------------------------------------------------------------------------------------------------------

  public void addEndTag(boolean inRemoveEmptyTags) {
    if (mAttrBuffer!=null)
      throw new RuntimeException("End tag with pending Attrs: "+mAttrBuffer.toString());
    if (mTagPrefix!=null)
      throw new RuntimeException("End tag with pending tag prefix: "+mTagPrefix);
    if (getHasIndent())
      mIndent=Math.max(0,mIndent-kIndentStepSize);
    if (mTagInfoStack.size()==0)
      throw new RuntimeException("End tag with no corresponding start tag");
    TagInfo theTagInfo=(TagInfo) mTagInfoStack.remove(mTagInfoStack.size()-1);
    int theBufferLength=(int) (theTagInfo.getAfterTagPos()-mLength);
    boolean theHasContent=((theBufferLength<0)||(theBufferLength>mBuffer.length())||
        (FormatUtils.hasRealContent(mBuffer.substring(theBufferLength))));
    if (!theHasContent) {
      if ((!theTagInfo.getHasAttrs())&&(inRemoveEmptyTags)) {
        theBufferLength-=theTagInfo.getTagname().length()+2;
        while ((theBufferLength>0)&&(mBuffer.charAt(theBufferLength-1)==' '))
          theBufferLength--;
        mBuffer.setLength(theBufferLength);
      } else {
        mBuffer.setLength(theBufferLength-1);
        mBuffer.append("/>");
        addEOL();
        store(false);
      }
    } else {
      mBuffer.append("</");
      mBuffer.append(theTagInfo.getTagname());
      mBuffer.append('>');
      addEOL();
      store(false);
    }
  }

  public void addEndTag() { addEndTag(false); }

//--------------------------------------------------------------------------------------------------------
// addTagWithContent
//--------------------------------------------------------------------------------------------------------

  public void addTagWithContent(String inTagname, String inContent) {
    if ((inContent==null)||(inContent.length()==0)) {
      if ((mShowEmpties)||(mAttrBuffer!=null))
        addEmptyTag(inTagname);
      else
        mAttrBuffer=null;
    } else {
      addStartTag(inTagname);
      addContent(inContent);
      addEndTag();
    }
  }

//--------------------------------------------------------------------------------------------------------
// addTagsWithContents
//--------------------------------------------------------------------------------------------------------

  public void addTagsWithContents(String inWrapperTagname, String inTagname, String[] inContents) {
    if (inContents!=null) {
      boolean theFoundContent=false;
      for (int i=0; i<inContents.length; i++)
        if (inContents[i]!=null) {
          theFoundContent=true;
          break;
        }
      if (theFoundContent) {
        if (inWrapperTagname!=null)
          addStartTag(inWrapperTagname);
        for (int i=0; i<inContents.length; i++)
          if (inContents[i]!=null)
            addTagWithContent(inTagname,inContents[i]);
        if (inWrapperTagname!=null)
          addEndTag();
      }
    }
  }

  public void addTagsWithContents(String inTagname, String[] inContents) {
    addTagsWithContents(null,inTagname,inContents); }

//--------------------------------------------------------------------------------------------------------
// addO2XObjects
//--------------------------------------------------------------------------------------------------------

  public void addO2XObjects(String inWrapperTagname, O2XIntf[] inO2XObjects) {
    if (inO2XObjects!=null) {
      boolean theFoundContent=false;
      for (int i=0; i<inO2XObjects.length; i++)
        if (inO2XObjects[i]!=null) {
          theFoundContent=true;
          break;
        }
      if (theFoundContent) {
        if (inWrapperTagname!=null)
          addStartTag(inWrapperTagname);
        for (int i=0; i<inO2XObjects.length; i++)
          if (inO2XObjects[i]!=null)
            addO2XObject(inO2XObjects[i]);
        if (inWrapperTagname!=null)
          addEndTag();
      }
    }
  }

  public void addO2XObjects(O2XIntf[] inO2XObject) {
    addO2XObjects(null,inO2XObject); }

//--------------------------------------------------------------------------------------------------------
// addBlankLine
//--------------------------------------------------------------------------------------------------------

  public void addBlankLine() { addEOL(); }

//--------------------------------------------------------------------------------------------------------
// addComment
//--------------------------------------------------------------------------------------------------------

  public void addComment(String inText) {
    if (inText!=null) {
      if (getHasIndent()) {
        addIndent();
        mBuffer.append("<!-- ");
        mBuffer.append(inText);
        mBuffer.append(FormatUtils.blanks(kBar.length()-mIndent-inText.length()));
        mBuffer.append(" -->");
        addEOL();
        store(false);
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// addDividerBar
//--------------------------------------------------------------------------------------------------------

  public void addDividerBar(String inTitle) {
    if (getHasIndent())
      if ((inTitle==null)||(inTitle.length()==0))
        addComment(kBar.substring(mIndent));
      else
        addComment(inTitle+' '+kBar.substring(mIndent+inTitle.length()+1));
  }

  public void addDividerBar() { addDividerBar(null); }

//--------------------------------------------------------------------------------------------------------
// addBanner
//--------------------------------------------------------------------------------------------------------

  public void addBanner(String[] inTexts) {
    addBlankLine();
    addDividerBar();
    for (int i=0; i<inTexts.length; i++)
      addComment(inTexts[i]);
    addDividerBar();
    addBlankLine();
  }

  public void addBanner(String inTitle) { addBanner(new String[] {inTitle}); }

}


