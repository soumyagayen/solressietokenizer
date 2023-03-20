//--------------------------------------------------------------------------------------------------------
// X2OConverter.java
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import java.util.*;

import org.xml.sax.*;

import gravel.store.var.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// X2OConverter.java
//
// No user serviceable parts
//--------------------------------------------------------------------------------------------------------

final class X2OConverter implements Constants {

//--------------------------------------------------------------------------------------------------------
// X2OConverter member vars
//--------------------------------------------------------------------------------------------------------

  private X2OBuilder     mX2OBuilder; 
  private X2OData        mX2OData;
  private char[]         mContentBuffer;
  private int            mContentLength;
  private X2OConverter   mParentX2OConverter;
  private X2OParser      mX2OParser;

//--------------------------------------------------------------------------------------------------------
// X2OConverter member vars
//--------------------------------------------------------------------------------------------------------

  X2OConverter(X2OBuilder inX2OBuilder, X2OData inX2OData, char[] inContentBuffer, 
      X2OConverter inParentX2OConverter, X2OParser inX2OParser) {
    mX2OBuilder=inX2OBuilder;
    mX2OData=inX2OData;
    mContentBuffer=inContentBuffer;
    if (mContentBuffer!=null) 
      mContentLength=0;
    mParentX2OConverter=inParentX2OConverter;
    mX2OParser=inX2OParser;
  }

//--------------------------------------------------------------------------------------------------------
// startElement
//--------------------------------------------------------------------------------------------------------

  void startElement(String inChildTagname, Attributes inAttributes) throws SAXException {

    // Save contents between tags, even if X2OBuilder returned null for ChildElmt
    if (mX2OData.mContents==null)
      mX2OData.mContents=new ArrayList(10);
    String theContent=kEmptyString;
    if (mContentLength>0) 
      theContent=new String(mContentBuffer,0,mContentLength);    
    mX2OData.mContents.add(theContent);
    mContentLength=0;

    X2OBuilder theX20Builder=null;
    try {
      if (mX2OBuilder!=null)
        theX20Builder=mX2OBuilder.createChildBuilder(mX2OData,inChildTagname);
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      throw new SAXParseException("Unexpected error at "+mX2OData.getFullPath()+kEOL+e.getMessage(),
          mX2OParser.getLocator(),e);
    }
    
    // Get next child converter in chain
    X2OConverter theChildX2OConverter=mX2OParser.nextX2OConverter(inChildTagname,theX20Builder,mX2OData);

    // Have child converter begin this element
    theChildX2OConverter.beginElement(inAttributes);
  }

//--------------------------------------------------------------------------------------------------------
// beginElement
//--------------------------------------------------------------------------------------------------------

  void beginElement(Attributes inAttributes) throws SAXException {

    // Parse attributes
    parseAttributes(inAttributes);

    // Create local state from parent state - attributes parsed and available
    try {
      if (mX2OBuilder!=null)
        mX2OBuilder.startObject(mX2OData);
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      throw new SAXParseException("Unexpected error "+mX2OData.getFullPath()+kEOL+e.getMessage(),
          mX2OParser.getLocator(),e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// parseAttributes
//--------------------------------------------------------------------------------------------------------

  private void parseAttributes(Attributes inAttributes) throws SAXException {

    int theNAttributes=inAttributes.getLength();

    // Create attribute name and value stores if necessary
    if ((theNAttributes>0)&&(mX2OData.mAttrNames==null)) {
      mX2OData.mAttrNames=new ArrayList(10);
      mX2OData.mAttrs=new ArrayList(10);
    }

    for (int i=0; i<theNAttributes; i++) {
      String theAttrName=inAttributes.getQName(i);
      String theAttr=inAttributes.getValue(i);
      if (FormatUtils.hasRealContent(theAttr)) {
        boolean theFound=false;
        for (int j=mX2OData.mAttrNames.size()-1; j>=0; j--)
          if (theAttrName.equals(mX2OData.mAttrNames.get(j))) {
            theFound=true;
            mX2OData.mAttrs.set(j,theAttr);
            break;
          }
        if (!theFound) {
          mX2OData.mAttrNames.add(theAttrName);
          mX2OData.mAttrs.add(theAttr);
        }
      }
    }
  }

//--------------------------------------------------------------------------------------------------------
// characters
//--------------------------------------------------------------------------------------------------------

  void characters(char[] inChars, int inOffset, int inLength) throws SAXException {

    int theNewContentLength=mContentLength+inLength;
    if (theNewContentLength>mContentBuffer.length) {
      char[] theOldContentBuffer=mContentBuffer;
      mContentBuffer=Allocate.newChars(theNewContentLength*3/2);
      System.arraycopy(theOldContentBuffer,0,mContentBuffer,0,mContentLength);
    }
    System.arraycopy(inChars,inOffset,mContentBuffer,mContentLength,inLength);
    mContentLength=theNewContentLength;
  }

//--------------------------------------------------------------------------------------------------------
// endElement
//--------------------------------------------------------------------------------------------------------

  void endElement(String inTagname) throws SAXException {

    // Save last content, except when empty string and only content 
    if ((mContentLength>0)||(mX2OData.getNContents()>0)) {
      if (mX2OData.mContents==null)
        mX2OData.mContents=new ArrayList(10);
      String theContent=kEmptyString;
      if (mContentLength>0) 
        theContent=new String(mContentBuffer,0,mContentLength);    
      mX2OData.mContents.add(theContent);
      mContentLength=0;
    }
    
    // Build object
    Object theObject;
    try {
      if (mX2OBuilder==null)
        theObject=mX2OData.getX2OElmt();
      else
        theObject=mX2OBuilder.buildObject(mX2OData);
    } catch (ArrayStoreException e) {
      throw new SAXParseException("Unexpected object built in <"+mX2OData.getTagname()+"> at "+
          mX2OData.getFullPath()+kEOL+e.getMessage(),mX2OParser.getLocator(),e);
    } catch (SAXException e) {
      throw e;
    } catch (Exception e) {
      throw new SAXParseException("Unexpected object at "+mX2OData.getFullPath()+kEOL+e.getMessage(),
          mX2OParser.getLocator(),e);
    }

    // Save object, either in parent X2OConverter or X2OParser
    if (mParentX2OConverter!=null)
      mParentX2OConverter.saveObject(inTagname,theObject);
    else
      mX2OParser.saveObject(theObject);

    // Back up to previous X2OConverter in chain
    mX2OParser.prevX2OConverter(mX2OData,mParentX2OConverter);
  }

//--------------------------------------------------------------------------------------------------------
// saveObject
//--------------------------------------------------------------------------------------------------------

  private void saveObject(String inChildTagname, Object inObject) throws SAXException {

    // Keep known child objects - empty/unknown tags will return null objects and are dropped
    if (inObject!=null) {

      // Create element name and value stores if necessary
      if (mX2OData.mChildTagnames==null) {
        mX2OData.mChildTagnames=new ArrayList(10);
        mX2OData.mChildElmts=new ArrayList(10);
      }

      // Check if this child occurred before
      int theNameIndex=kNotFound;
      for (int i=mX2OData.mChildTagnames.size()-1; i>=0; i--)
        if (inChildTagname.equals(mX2OData.mChildTagnames.get(i))) {
          theNameIndex=i;
          break;
        }

      // First instance of child tag
      if (theNameIndex==kNotFound) {
        theNameIndex=mX2OData.mChildTagnames.size();
        mX2OData.mChildTagnames.add(inChildTagname);

        // If one of the child elements was multi-valued, need to keep track of indexes
        if (mX2OData.mIsMultiValued) {
          int theNewValueIndex=mX2OData.mChildElmts.size();
          mX2OData.mChildTagnameForElmts.appendVar(theNameIndex);
          mX2OData.mFirstChildIndex.appendVar(theNewValueIndex);
          mX2OData.mLastChildIndex.appendVar(theNewValueIndex);
          mX2OData.mNextChildIndexes.appendVar(-1);
          mX2OData.mNElmtsForChildTagname.appendVar(1);
        }

      // Multiple instances of child tag
      } else {
        mX2OData.mIsMultiValued=true;

        // Create extra indexs if needed
        if (mX2OData.mChildTagnameForElmts==null) {
          mX2OData.mChildTagnameForElmts=new VarRAMStore(10);
          mX2OData.mFirstChildIndex=new VarRAMStore(10);
          mX2OData.mLastChildIndex=new VarRAMStore(10);
          mX2OData.mNextChildIndexes=new VarRAMStore(10);
          mX2OData.mNElmtsForChildTagname=new VarRAMStore(10);
        }

        // If empty, populate indexs with single valued elmts
        if (mX2OData.mChildTagnameForElmts.getSize()==0) {
          int theNValues=mX2OData.mChildElmts.size();
          for (int i=0; i<theNValues; i++) {
            mX2OData.mChildTagnameForElmts.appendVar(i);
            mX2OData.mFirstChildIndex.appendVar(i);
            mX2OData.mLastChildIndex.appendVar(i);
            mX2OData.mNextChildIndexes.appendVar(-1);
            mX2OData.mNElmtsForChildTagname.appendVar(1);
          }
        }

        // Add this instance
        int theNewValueIndex=mX2OData.mChildElmts.size();
        mX2OData.mChildTagnameForElmts.appendVar(theNameIndex);
        mX2OData.mNextChildIndexes.setVar(mX2OData.mLastChildIndex.getInt(theNameIndex),theNewValueIndex);
        mX2OData.mLastChildIndex.setVar(theNameIndex,theNewValueIndex);
        mX2OData.mNextChildIndexes.appendVar(-1);
        mX2OData.mNElmtsForChildTagname.setVar(theNameIndex,
            mX2OData.mNElmtsForChildTagname.getInt(theNameIndex)+1);
      }

      mX2OData.mChildElmts.add(inObject);
    }
  }

}
