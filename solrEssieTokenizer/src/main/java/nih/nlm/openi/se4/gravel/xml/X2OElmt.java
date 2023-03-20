//--------------------------------------------------------------------------------------------------------
// X2OElmt.java
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import gravel.sort.*;
import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// X2OElmt
//
// A generic container to hold data parsed from XML
// Similar to DOM
// Can be used to hold and access data from child tags while building object for current tag
// Also useful for reformatting XML
//--------------------------------------------------------------------------------------------------------

public class X2OElmt extends X2OEasy {

//--------------------------------------------------------------------------------------------------------
// X2OElmt consts
//--------------------------------------------------------------------------------------------------------

  public static final X2OElmt[]   kNoX2OElmts=new X2OElmt[0];
  
//--------------------------------------------------------------------------------------------------------
// X2OElmt member vars
//--------------------------------------------------------------------------------------------------------
 
  private String      mTagname;
  private X2OElmt     mParentElmt;
  private int         mElmtN;
  private int         mDepth;
  private int         mSiblingN;
  private String[]    mAttrNames;
  private String[]    mAttrs;
  private String[]    mContents;
  private String[]    mChildTagnames;
  private X2OElmt[]   mChildElmts;
  private boolean     mIsMultiValued;
  private int[]       mChildTagnameForElmts;
  private int[]       mNElmtsForChildTagname;
  private int[]       mFirstChildIndex;
  private int[]       mLastChildIndex;
  private int[]       mNextChildIndexes;

//--------------------------------------------------------------------------------------------------------
// X2OElmt 
//--------------------------------------------------------------------------------------------------------

  public X2OElmt(
      String      inTagname, 
      int         inElmtN, 
      int         inDepth, 
      int         inChildN, 
      String[]    inAttrNames, 
      String[]    inAttrs, 
      String[]    inContents, 
      String[]    inChildTagnames, 
      X2OElmt[]   inChildElmts, 
      boolean     inIsMultiValued,
      int[]       inChildTagnameForElmts,
      int[]       inNElmtsForChildTagname,
      int[]       inFirstChildIndex,
      int[]       inLastChildIndex,
      int[]       inNextChildIndexes) { 
    mTagname=inTagname;
    if (inChildElmts!=null)
      for (int i=0; i<inChildElmts.length; i++)
        inChildElmts[i].mParentElmt=this;
    mElmtN=inElmtN;
    mDepth=inDepth;
    mSiblingN=inChildN;
    mAttrNames=inAttrNames;
    mAttrs=inAttrs;
    mContents=inContents;
    mChildTagnames=inChildTagnames;
    mChildElmts=inChildElmts;
    mIsMultiValued=inIsMultiValued;
    mChildTagnameForElmts=inChildTagnameForElmts;
    mNElmtsForChildTagname=inNElmtsForChildTagname;
    mFirstChildIndex=inFirstChildIndex;
    mLastChildIndex=inLastChildIndex;
    mNextChildIndexes=inNextChildIndexes;
  }

//--------------------------------------------------------------------------------------------------------
// getTagname
//--------------------------------------------------------------------------------------------------------

  public String getTagname() { return mTagname; }

//--------------------------------------------------------------------------------------------------------
// getParentElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getParentElmt() { return mParentElmt; }

//--------------------------------------------------------------------------------------------------------
// getElmtN
//--------------------------------------------------------------------------------------------------------

  public int getElmtN() { return mElmtN; }

//--------------------------------------------------------------------------------------------------------
// getDepth
//--------------------------------------------------------------------------------------------------------

  public int getDepth() { return mDepth; }

//--------------------------------------------------------------------------------------------------------
// getSiblingN
//--------------------------------------------------------------------------------------------------------

  public int getSiblingN() { return mSiblingN; }

//--------------------------------------------------------------------------------------------------------
// getNAttrs
//--------------------------------------------------------------------------------------------------------

  public int getNAttrs() { return (mAttrs==null)?0:mAttrs.length; }

//--------------------------------------------------------------------------------------------------------
// getAttrName
//--------------------------------------------------------------------------------------------------------

  public String getAttrName(int inIndex) { return mAttrNames[inIndex]; }

//--------------------------------------------------------------------------------------------------------
// getAttr
//--------------------------------------------------------------------------------------------------------

  public String getAttr(int inIndex) { return mAttrs[inIndex]; }

//--------------------------------------------------------------------------------------------------------
// getAttrNames
//--------------------------------------------------------------------------------------------------------

  public String[] getAttrNames() { return mAttrNames; }

//--------------------------------------------------------------------------------------------------------
// getAttrs
//--------------------------------------------------------------------------------------------------------

  public String[] getAttrs() { return mAttrs; }

//--------------------------------------------------------------------------------------------------------
// getNContents
//--------------------------------------------------------------------------------------------------------

  public int getNContents() { return (mContents==null)?0:mContents.length; }

//--------------------------------------------------------------------------------------------------------
// getContent
//--------------------------------------------------------------------------------------------------------

  public String getContent(int inIndex) { return mContents[inIndex]; }

//--------------------------------------------------------------------------------------------------------
// getContents
//--------------------------------------------------------------------------------------------------------

  public String[] getContents() { return mContents; }

//--------------------------------------------------------------------------------------------------------
// getNChildElmts
//--------------------------------------------------------------------------------------------------------

  public int getNChildElmts() { return (mChildElmts==null)?0:mChildElmts.length; }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getChildElmt(int inIndex) { return mChildElmts[inIndex]; }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public X2OElmt[] getChildElmts() { return mChildElmts; }

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getFirstChildElmt() { return (X2OElmt) super.getFirstChildElmt(); }

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getLastChildElmt() { return (X2OElmt) super.getLastChildElmt(); }

//--------------------------------------------------------------------------------------------------------
// getNextSiblingElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getNextSiblingElmt() { return (X2OElmt) super.getNextSiblingElmt(); }

//--------------------------------------------------------------------------------------------------------
// getPrevSiblingElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getPrevSiblingElmt() { return (X2OElmt) super.getPrevSiblingElmt(); }

//--------------------------------------------------------------------------------------------------------
// getFirstDescendantElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getFirstDescendantElmt() { 
    if (getNChildElmts()==0)
      return this;
    return getFirstChildElmt().getFirstDescendantElmt();
  }

//--------------------------------------------------------------------------------------------------------
// getLastDescendantElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getLastDescendantElmt() { 
    if (getNChildElmts()==0)
      return this;
    return getLastChildElmt().getLastDescendantElmt();
  }

//--------------------------------------------------------------------------------------------------------
// getNextStartElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getNextStartElmt() {
    if (getNChildElmts()>0)
      return getFirstChildElmt();
    int theSiblingN=mSiblingN;
    X2OElmt theParentElmt=mParentElmt;
    while (theParentElmt!=null) {
      if (theSiblingN<theParentElmt.getNChildElmts()-1)
        return theParentElmt.getChildElmt(theSiblingN+1);
      theSiblingN=theParentElmt.getSiblingN();
      theParentElmt=theParentElmt.getParentElmt();
    }
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getPrevStartElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getPrevStartElmt() {
    if (mSiblingN==0)
      return mParentElmt;
    return getPrevSiblingElmt().getLastDescendantElmt();
  }

//--------------------------------------------------------------------------------------------------------
// getPrevEndElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getPrevEndElmt() {
    if (getNChildElmts()>0)
      return getLastChildElmt();
    int theSiblingN=mSiblingN;
    X2OElmt theParentElmt=mParentElmt;
    while (theParentElmt!=null) {
      if (theSiblingN>0)
        return theParentElmt.getChildElmt(theSiblingN-1);
      theSiblingN=theParentElmt.getSiblingN();
      theParentElmt=theParentElmt.getParentElmt();
    }
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getNextEndElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getNextEndElmt() {
    if (mParentElmt==null)
      return null;
    if (mSiblingN==mParentElmt.getNChildElmts()-1)
      return mParentElmt;
    return getNextSiblingElmt().getFirstDescendantElmt();
  }

//--------------------------------------------------------------------------------------------------------
// getNChildTagnames
//--------------------------------------------------------------------------------------------------------

  public int getNChildTagnames() { return (mChildTagnames==null)?0:mChildTagnames.length; }

//--------------------------------------------------------------------------------------------------------
// getChildTagname
//--------------------------------------------------------------------------------------------------------

  public String getChildTagname(int inIndex) { return getChildTagnames()[inIndex]; }

//--------------------------------------------------------------------------------------------------------
// getChildTagnames
//--------------------------------------------------------------------------------------------------------

  public String[] getChildTagnames() { return mChildTagnames; }

//--------------------------------------------------------------------------------------------------------
// getIsMultiValued
//--------------------------------------------------------------------------------------------------------

  public boolean getIsMultiValued() { return mIsMultiValued; }

//--------------------------------------------------------------------------------------------------------
// getChildTagnameForElmt
//--------------------------------------------------------------------------------------------------------

  public String getChildTagnameForElmt(int inIndex) {   // Note this is Elmt index
    if (!mIsMultiValued)
      return getChildTagname(inIndex);
    else
      return getChildTagname(mChildTagnameForElmts[inIndex]);
  }

//--------------------------------------------------------------------------------------------------------
// getNChildElmts
//--------------------------------------------------------------------------------------------------------

  public int getNChildElmts(String inTagname) { 
    if (mChildElmts==null)
      return 0;
    for (int i=0; i<mChildTagnames.length; i++)
      if (mChildTagnames[i].equals(inTagname))
        if (!mIsMultiValued) 
          return 1;
        else
          return mNElmtsForChildTagname[i];
    return 0;
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getChildElmt(String inTagname, int inIndex) { 
    if (mChildElmts==null)
      return null;
    for (int i=0; i<mChildTagnames.length; i++)
      if (inTagname.equals(mChildTagnames[i])) 
        if (!mIsMultiValued) {
          if (inIndex==0)
            return mChildElmts[i];
          else
            return null;
        } else {
          int n=0;
          int theValueIndex=mFirstChildIndex[i];
          while (n<inIndex) {
            n++;
            theValueIndex=mNextChildIndexes[theValueIndex];
            if (theValueIndex<0)
              return null;
          }
          return mChildElmts[theValueIndex];
        }
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getFirstChildElmt(String inTagname) {
    if (mChildElmts==null)
      return null;
    for (int i=0; i<mChildTagnames.length; i++)
      if (mChildTagnames[i].equals(inTagname))
        if (!mIsMultiValued) 
          return mChildElmts[i];
        else
          return mChildElmts[mFirstChildIndex[i]];
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getLastChildElmt(String inTagname) {
    if (mChildElmts==null)
      return null;
    for (int i=0; i<mChildTagnames.length; i++)
      if (mChildTagnames[i].equals(inTagname))
        if (!mIsMultiValued) 
          return mChildElmts[i];
        else
          return mChildElmts[mLastChildIndex[i]];
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public X2OElmt[] getChildElmts(String inTagname) { 
    if (mChildElmts==null)
      return kNoX2OElmts;
    for (int i=0; i<mChildTagnames.length; i++)
      if (inTagname.equals(mChildTagnames[i])) 
        if (!mIsMultiValued) 
          return new X2OElmt[] {mChildElmts[mFirstChildIndex[i]]};
        else {
          X2OElmt[] theChildElmts=new X2OElmt[mNElmtsForChildTagname[i]];
          int n=0;
          int theValueIndex=mFirstChildIndex[i];
          while (theValueIndex>=0) {
            theChildElmts[n]=mChildElmts[theValueIndex];
            theValueIndex=mNextChildIndexes[theValueIndex];
            n++;
          }
          return theChildElmts;
        }
    return kNoX2OElmts;
  }

//--------------------------------------------------------------------------------------------------------
// getAllDescendantElmts
//
// includes this
// returned in natural order of occurrence
//--------------------------------------------------------------------------------------------------------

  public X2OElmt[] getAllDescendantElmts() {
    int theElmtN=getElmtN();
    int theNElmts=getLastDescendantElmt().getElmtN()+1-theElmtN;
    X2OElmt[] theElmts=new X2OElmt[theNElmts];    
    X2OElmt theElmt=this;
    for (int i=0; i<theNElmts; i++) {
      theElmts[theElmt.getElmtN()-theElmtN]=theElmt;
      theElmt=theElmt.getNextStartElmt();
    }
    return theElmts;
  }

//--------------------------------------------------------------------------------------------------------
// findElmtStartCharNs
//
// Find start char pos of all Elmt start tags
//--------------------------------------------------------------------------------------------------------

  public static int[] findElmtStartCharNs(X2OElmt[] inAllElmts, String inXML) {
    int[] theElmtStartCharNs=new int[inAllElmts.length];
    int thePos=kNotFound;
    for (int i=0; i<inAllElmts.length; i++) {
      X2OElmt theElmt=inAllElmts[i];
      thePos=inXML.indexOf('<'+theElmt.getTagname(),thePos);
      if (thePos==kNotFound)
        throw new RuntimeException("Cannot find elmt start CharNs");
      theElmtStartCharNs[i]=thePos;
      thePos++;
    }
    return theElmtStartCharNs;
  }

//--------------------------------------------------------------------------------------------------------
// findElmtEndCharNs
//
// Find end char pos of all Elmt end tags (including empty tags)
//--------------------------------------------------------------------------------------------------------

  public static int[] findElmtEndCharNs(X2OElmt[] inAllElmts, String inXML) {
    int[] theElmtEndCharNs=new int[inAllElmts.length];
    int thePos=inXML.length();
    X2OElmt theElmt=inAllElmts[0];
    int theElmtN=theElmt.getElmtN();
    for (int j=0; j<inAllElmts.length; j++) {
      int i=theElmt.getElmtN()-theElmtN;
      String theTagname=theElmt.getTagname();
      if (!theElmt.getIsEmpty()) {
        thePos=inXML.lastIndexOf("</"+theTagname+">",thePos);
        theElmtEndCharNs[i]=thePos+theTagname.length()+3;
      } else {
        thePos=inXML.lastIndexOf("/>",thePos);  // Can't use tagname since may have attrs, should still be OK
        theElmtEndCharNs[i]=thePos+2;
        thePos=inXML.lastIndexOf('<'+theTagname,thePos);
      }
      if (thePos==kNotFound)
        throw new RuntimeException("Cannot find elmt end CharNs");
      theElmt=theElmt.getPrevEndElmt();
      thePos--;
    }
    return theElmtEndCharNs;
  }

//--------------------------------------------------------------------------------------------------------
// findContentCharRanges
//
// Find char ranges for contents between tags that are not just whitespace
//--------------------------------------------------------------------------------------------------------

  public static int[][] findContentCharRanges(X2OElmt[] inAllElmts, String inXML, 
      int[] inElmtStartCharNs, int[] inElmtEndCharNs) {
  
    VarRAMStore theContentCharNsStore=new VarRAMStore();
    VarRAMStore theContentNCharssStore=new VarRAMStore();
    for (int i=0; i<inAllElmts.length; i++) {
      X2OElmt theElmt=inAllElmts[i];
      int theNContents=theElmt.getNContents();
      if (theElmt.getNContents()>0) {
        String theContent=theElmt.getContent(0);
        if (FormatUtils.hasRealContent(theContent)) {
          int theContentCharN=inXML.indexOf('>',inElmtStartCharNs[i])+1;
          String theContentXML=EscapeUtils.escapeXML(theContent);
          if (!inXML.substring(theContentCharN).startsWith(theContentXML))
            throw new RuntimeException("Cannot find content char ranges");
          theContentCharNsStore.appendVar(theContentCharN);
          theContentNCharssStore.appendVar(theContentXML.length());
        }
      }
      for (int j=1; j<theNContents; j++) {
        String theContent=theElmt.getContent(j);
        if (FormatUtils.hasRealContent(theContent)) {
          int theContentCharN=inElmtEndCharNs[theElmt.getChildElmt(j-1).getElmtN()];
          String theContentXML=EscapeUtils.escapeXML(theContent);
          if (!inXML.substring(theContentCharN).startsWith(theContentXML))
            throw new RuntimeException("Cannot find content char ranges");
          theContentCharNsStore.appendVar(theContentCharN);
          theContentNCharssStore.appendVar(theContentXML.length());
        }
      }
    }
    
    int[] theContentCharNs=theContentCharNsStore.getAllInts();
    int[] theContentNCharss=theContentNCharssStore.getAllInts();

    // Sort content char ranges
    int[] theSortMap=SortMapUtils.sortMap(theContentCharNs);
    SortMapUtils.reorder(theContentCharNs,theSortMap);
    SortMapUtils.reorder(theContentNCharss,theSortMap);
    
    return new int[][] {theContentCharNs,theContentNCharss};
  }
  
}
