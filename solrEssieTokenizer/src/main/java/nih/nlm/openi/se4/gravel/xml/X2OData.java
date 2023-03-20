//--------------------------------------------------------------------------------------------------------
// X2OData.java
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import java.util.*;

import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// X2OData.java
//--------------------------------------------------------------------------------------------------------

public class X2OData extends X2OEasy implements PoolObjectInterface {

//--------------------------------------------------------------------------------------------------------
// X2OData consts
//--------------------------------------------------------------------------------------------------------

  private static final int        kPreferredNInPool=3;
  private static final BasePool   kX2ODataPool;
  
//--------------------------------------------------------------------------------------------------------
// X2OData class init
//--------------------------------------------------------------------------------------------------------

  static {
    try {
      kX2ODataPool=new BasePool(kPreferredNInPool) {
        protected PoolObjectInterface newPoolObject() { return new X2OData(); } };
    } catch (Throwable e) {
      System.err.println(FormatUtils.formatException("Cannot init X2OData",e));
      throw e;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getX2OData 
//--------------------------------------------------------------------------------------------------------

  static X2OData getX2OData() { 
    try { 
      return (X2OData) kX2ODataPool.getPoolObject(); 
    } catch (Exception e) { 
      return null; 
    } 
  }

//--------------------------------------------------------------------------------------------------------
// putX2OData 
//--------------------------------------------------------------------------------------------------------

  static void putX2OData(X2OData inX2OData) { kX2ODataPool.putPoolObject(inX2OData); }

//--------------------------------------------------------------------------------------------------------
// X2OData member vars
//--------------------------------------------------------------------------------------------------------
 
  String        mTagname;
  String        mFullPath;
  X2OData       mParentElmt;
  int           mElmtN;
  int           mDepth;
  int           mSiblingN;

  ArrayList     mAttrNames;
  ArrayList     mAttrs;
  ArrayList     mContents;
  ArrayList     mChildTagnames;
  ArrayList     mChildElmts;

// When mIsMultiValued, 
//   1) there are more ChildElmts than ChildTagnames, 
//   2) there is a ChildTagnameForElmts which points from elmt back to tag name 
//   3) there is a NElmtsForChildTagname, FirstChildIndex, and LastChildIndex for every ChildTagname, 
//      which characterizes a linked list for each tagname
//   4) there is a NextChildIndex for every elmt, which is all the links in all the lists

  boolean       mIsMultiValued;
  VarRAMStore   mChildTagnameForElmts; // index of ChildTagname for each ChildElmt
  VarRAMStore   mNElmtsForChildTagname;// number of mChildElmts for given ChildTagname - length of linked list
  VarRAMStore   mFirstChildIndex;      // index in mChildElmts of first occr of given ChildTagname - start of linked list
  VarRAMStore   mLastChildIndex;       // index in mChildElmts of last occr of given ChildTagname - end of linked list
  VarRAMStore   mNextChildIndexes;     // index in mChildElmts of next occr of given ChildTagname - linked list
 
//--------------------------------------------------------------------------------------------------------
// canRecycle 
//--------------------------------------------------------------------------------------------------------

  public boolean canRecycle() { return true; }

//--------------------------------------------------------------------------------------------------------
// open 
//--------------------------------------------------------------------------------------------------------

  public void open() throws Exception {}

//--------------------------------------------------------------------------------------------------------
// close 
//--------------------------------------------------------------------------------------------------------

  public void close() {
    mTagname=null;
    mFullPath=null;
    mParentElmt=null;
    mAttrNames=null;
    mAttrs=null;
    mContents=null;
    mChildTagnames=null;
    mChildElmts=null;
    if (mChildTagnameForElmts!=null)
      mChildTagnameForElmts.close();
    if (mNElmtsForChildTagname!=null)
      mNElmtsForChildTagname.close();          
    if (mFirstChildIndex!=null)
      mFirstChildIndex.close();  
    if (mLastChildIndex!=null)
      mLastChildIndex.close(); 
    if (mNextChildIndexes!=null)
      mNextChildIndexes.close();
    mChildTagnameForElmts=null;
    mNElmtsForChildTagname=null;          
    mFirstChildIndex=null;  
    mLastChildIndex=null;   
    mNextChildIndexes=null; 
  }

//--------------------------------------------------------------------------------------------------------
// check 
//--------------------------------------------------------------------------------------------------------

  public boolean check() { return true; }

//--------------------------------------------------------------------------------------------------------
// prepare
//
// X2ODatas are reused.  This routine is called to initialize an instance for reuse.
// Holds code that would usually be in a constructor
//--------------------------------------------------------------------------------------------------------

  void prepare(String inTagname, X2OData inParentX2OData, int inElmtN) {
    mTagname=inTagname;
    mFullPath=null;
    mParentElmt=inParentX2OData;
    mElmtN=inElmtN;
    mDepth=0;
    mSiblingN=0;
    if (inParentX2OData!=null) {
      mDepth=inParentX2OData.getDepth()+1;
      mSiblingN=inParentX2OData.getNChildElmts();
    }
    if (mAttrNames!=null) {
      mAttrNames.clear();
      mAttrs.clear();
    }
    if (mContents!=null)
      mContents.clear();
    if (mChildTagnames!=null) {
      mChildTagnames.clear();
      mChildElmts.clear();
    }
    mIsMultiValued=false;
    if (mChildTagnameForElmts!=null) 
      mChildTagnameForElmts.clear();
    if (mNElmtsForChildTagname!=null) 
      mNElmtsForChildTagname.clear();
    if (mFirstChildIndex!=null) 
      mFirstChildIndex.clear();
    if (mLastChildIndex!=null) 
      mLastChildIndex.clear();
    if (mNextChildIndexes!=null) 
      mNextChildIndexes.clear();
  }

//--------------------------------------------------------------------------------------------------------
// get routines
//--------------------------------------------------------------------------------------------------------

  public String getTagname() { return mTagname; }
  public X2OData getParentElmt() { return mParentElmt; }
  public boolean getHasParent() { return (mParentElmt!=null); }
  public boolean getIsRoot() { return (mParentElmt==null); }
  public int getElmtN() { return mElmtN; }
  public int getDepth() { return mDepth; }
  public int getSiblingN() { return mSiblingN; }

//--------------------------------------------------------------------------------------------------------
// getFullPath
//--------------------------------------------------------------------------------------------------------

  private void getFullPath(StringBuffer inBuffer) {
    if (mFullPath!=null) 
      inBuffer.append(mFullPath);
    else {
      X2OData theParentX2OData=getParentElmt();
      if (theParentX2OData!=null) 
        theParentX2OData.getFullPath(inBuffer);
      inBuffer.append('/');
      inBuffer.append(mTagname);
    }
  }

  public String getFullPath() {
    if (mFullPath==null) {
      StringBuffer theBuffer=new StringBuffer(64);
      getFullPath(theBuffer);
      mFullPath=theBuffer.toString();
    }
    return mFullPath;
  }

//--------------------------------------------------------------------------------------------------------
// getNAttrs
//--------------------------------------------------------------------------------------------------------

  public int getNAttrs() {
    if (mAttrs==null)
      return 0;
    return mAttrNames.size();
  }

//--------------------------------------------------------------------------------------------------------
// getAttrName
//--------------------------------------------------------------------------------------------------------

  public String getAttrName(int inIndex) {
    if (mAttrs==null)
      return null;
    return (String) mAttrNames.get(inIndex);
  }

//--------------------------------------------------------------------------------------------------------
// getAttrNames
//--------------------------------------------------------------------------------------------------------

  public String[] getAttrNames() {
    if (mAttrs==null)
      return kNoStrings;
    return (String[]) mAttrNames.toArray(new String[mAttrNames.size()]);
  }

//--------------------------------------------------------------------------------------------------------
// getAttr
//--------------------------------------------------------------------------------------------------------

  public String getAttr(int inIndex) {
    if (mAttrs==null)
      return null;
    return (String) mAttrs.get(inIndex);
  }

//--------------------------------------------------------------------------------------------------------
// getAttrs
//--------------------------------------------------------------------------------------------------------

  public String[] getAttrs() {
    if (mAttrs==null)
      return kNoStrings;
    return (String[]) mAttrs.toArray(new String[mAttrs.size()]);
  }

//--------------------------------------------------------------------------------------------------------
// getNContents
//--------------------------------------------------------------------------------------------------------

  public int getNContents() {
    if (mContents==null)
      return 0;
    return mContents.size();
  }

//--------------------------------------------------------------------------------------------------------
// getContent
//--------------------------------------------------------------------------------------------------------

  public String getContent(int inIndex) {
    if (mContents==null)
      return null;
    return (String) mContents.get(inIndex);
  }

//--------------------------------------------------------------------------------------------------------
// getContents
//--------------------------------------------------------------------------------------------------------

  public String[] getContents() {
    if (mContents==null)
      return kNoStrings;
    return (String[]) mContents.toArray(new String[mContents.size()]);
  }

//--------------------------------------------------------------------------------------------------------
// getNChildElmts
//--------------------------------------------------------------------------------------------------------

  public int getNChildElmts() {
    if (mChildElmts==null)
      return 0;
    return mChildElmts.size();
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getChildElmt(int inIndex) {
    if (mChildElmts==null)
      return null;
    return mChildElmts.get(inIndex);
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//--------------------------------------------------------------------------------------------------------

  public Object[] getChildElmts() {
    if (mChildElmts==null)
      return kNoObjects;
    return mChildElmts.toArray(new Object[mChildElmts.size()]);
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmtList
//--------------------------------------------------------------------------------------------------------

  public ArrayList getChildElmtList() { 
    if (mChildElmts==null)
      return new ArrayList();
    return (ArrayList) mChildElmts.clone(); 
  }

//--------------------------------------------------------------------------------------------------------
// getNChildTagnames
//--------------------------------------------------------------------------------------------------------

  public int getNChildTagnames() {
    if (mChildTagnames==null)
      return 0;
    return mChildTagnames.size();
  }

//--------------------------------------------------------------------------------------------------------
// getChildTagname
//--------------------------------------------------------------------------------------------------------

  public String getChildTagname(int inIndex) {   // Note this is Tagname index not Elmt index
    if (mChildTagnames==null)
      return null;
    return (String) mChildTagnames.get(inIndex);
  }

//--------------------------------------------------------------------------------------------------------
// getChildTagnames
//--------------------------------------------------------------------------------------------------------

  public String[] getChildTagnames() {
    if (mChildTagnames==null)
      return kNoStrings;
    return (String[]) mChildTagnames.toArray(new String[mChildTagnames.size()]);
  }

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
      return getChildTagname(mChildTagnameForElmts.getInt(inIndex));
  }

//--------------------------------------------------------------------------------------------------------
// getNChildElmts
//--------------------------------------------------------------------------------------------------------

  public int getNChildElmts(String inTagname) {
    if (mChildElmts==null)
      return 0;
    for (int i=0; i<mChildTagnames.size(); i++)
      if (inTagname.equals(mChildTagnames.get(i))) 
        if (!mIsMultiValued)
          return 1;
        else
          return mNElmtsForChildTagname.getInt(i);
    return 0;
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmt
//
// Alert !!!  This routine does a linear search and is too slow to be used in a big loop !
//--------------------------------------------------------------------------------------------------------

  public Object getChildElmt(String inTagname, int inIndex) {
    if (mChildElmts==null)
      return null;
    for (int i=0; i<mChildTagnames.size(); i++)
      if (inTagname.equals(mChildTagnames.get(i))) 
        if (!mIsMultiValued) {
          if (inIndex==0)
            return mChildElmts.get(i);
          else
            return null;
        } else {
          int n=0;
          int theValueIndex=mFirstChildIndex.getInt(i);
          while (n<inIndex) {
            n++;
            theValueIndex=mNextChildIndexes.getInt(theValueIndex);
            if (theValueIndex<0)
              return null;
          }
          return mChildElmts.get(theValueIndex);
        }
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getFirstChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getFirstChildElmt(String inTagname) {
    if (mChildElmts==null)
      return null;
    for (int i=0; i<mChildTagnames.size(); i++)
      if (inTagname.equals(mChildTagnames.get(i))) 
        if (!mIsMultiValued) 
          return mChildElmts.get(i);
        else
          return mChildElmts.get(mFirstChildIndex.getInt(i));
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getLastChildElmt
//--------------------------------------------------------------------------------------------------------

  public Object getLastChildElmt(String inTagname) {
    if (mChildElmts==null)
      return null;
    for (int i=0; i<mChildTagnames.size(); i++)
      if (inTagname.equals(mChildTagnames.get(i))) 
        if (!mIsMultiValued) 
          return mChildElmts.get(i);
        else
          return mChildElmts.get(mLastChildIndex.getInt(i));
    return null;
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmts
//
// If expecting only one value, use getFirstElmt().  This routine creates an array
// (expensive) to hold the multiple values
//--------------------------------------------------------------------------------------------------------

  public Object[] getChildElmts(String inTagname) {
    if (mChildElmts==null)
      return kNoObjects;
    for (int i=0; i<mChildTagnames.size(); i++)
      if (inTagname.equals(mChildTagnames.get(i))) 
        if (!mIsMultiValued) 
          return new Object[] {mChildElmts.get(i)};
        else {
          Object[] theChildElmts=new Object[mNElmtsForChildTagname.getInt(i)];
          int n=0;
          int theValueIndex=mFirstChildIndex.getInt(i);
          while (theValueIndex>=0) {
            theChildElmts[n]=mChildElmts.get(theValueIndex);
            theValueIndex=mNextChildIndexes.getInt(theValueIndex);
            n++;
          }
          return theChildElmts;
        }
    return kNoObjects;
  }

//--------------------------------------------------------------------------------------------------------
// getChildElmtList
//--------------------------------------------------------------------------------------------------------

  public ArrayList getChildElmtList(String inTagname) { 
    ArrayList theChildElmtList=new ArrayList();
    if (mChildElmts==null)
      return theChildElmtList;
    for (int i=0; i<mChildTagnames.size(); i++)
      if (inTagname.equals(mChildTagnames.get(i))) 
        if (!mIsMultiValued) 
          theChildElmtList.add(mChildElmts.get(i));
        else {
          int theValueIndex=mFirstChildIndex.getInt(i);
          while (theValueIndex>=0) {
            theChildElmtList.add(mChildElmts.get(theValueIndex));
            theValueIndex=mNextChildIndexes.getInt(theValueIndex);
          }
        }
    return theChildElmtList;
  }

//--------------------------------------------------------------------------------------------------------
// getX2OElmt
//--------------------------------------------------------------------------------------------------------

  public X2OElmt getX2OElmt() { 
    X2OElmt[] theElmts=X2OElmt.kNoX2OElmts;
    if (mChildElmts!=null) 
      theElmts=(X2OElmt[]) mChildElmts.toArray(new X2OElmt[mChildElmts.size()]);
    int[] theChildTagnameForElmts=null;
    int[] theNElmtsForChildTagnames=null;
    int[] theFirstChildIndexs=null;
    int[] theLastChildIndexs=null;
    int[] theNextChildIndexes=null;
    if (mIsMultiValued) {
      theChildTagnameForElmts=mChildTagnameForElmts.getAllInts();
      theNElmtsForChildTagnames=mNElmtsForChildTagname.getAllInts();
      theFirstChildIndexs=mFirstChildIndex.getAllInts();
      theLastChildIndexs=mLastChildIndex.getAllInts();
      theNextChildIndexes=mNextChildIndexes.getAllInts();
    }
    return new X2OElmt(
        mTagname,
        mElmtN,
        mDepth,
        mSiblingN,
        getAttrNames(),
        getAttrs(),
        getContents(),
        getChildTagnames(),
        theElmts,
        mIsMultiValued,
        theChildTagnameForElmts,
        theNElmtsForChildTagnames, 
        theFirstChildIndexs,
        theLastChildIndexs,
        theNextChildIndexes);
  }

}
