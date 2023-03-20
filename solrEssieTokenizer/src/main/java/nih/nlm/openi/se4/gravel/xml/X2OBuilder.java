//--------------------------------------------------------------------------------------------------------
// X2OBuilder.java
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// X2OBuilder.java
//
// Builds a java object from data extracted from XML
// Usually only one routine to implement
//
// Objects are built by buildObject() when end tag parsed
//   This allows objects for parent tags to access objects from child tags
//   Usually want parent object to contain children
//   Must implement this routine!  Default throws exception.
//
// Local state info can be updated in a startObject() method, which is 
//   called when a start tag parsed
//   Can usually ignore this routine.  Default does nothing.
//
// A different X2OBuilder can be used for child tags.  The method,
//   createChildBuilder(), is called with the child tag name when a start  
//   tag parsed.  This method is called before the startObject() call, so 
//   that the correct builder gets the startObject().
//   Can usually ignore this routine.  Default does nothing.
//
//--------------------------------------------------------------------------------------------------------

public abstract class X2OBuilder implements Constants {
 
//========================================================================================================
//
// Important builder routines
//
//========================================================================================================

//--------------------------------------------------------------------------------------------------------
// buildObject
//
// Extracts XML Attrs, texts, and child Elmts from inX2OData, and builds an 
// object with them.  
//
// Some useful routines on inX2OData include:
//   String getTagname()
//   String getFullPath()
//   X2OData getParentX2OData()
//
//   boolean getHasText()
//   String getText()
//
//   boolean getHasAttrs()
//   int getNAttrs()
//   String getAttrName(int inIndex)
//   String[] getAttrNames()
//   String getAttrValue(String inName)
//   String getAttrValue(int inIndex)
//   String[] getAttrValues()
//   HashMap getAttrMap()
//
//   boolean getHasElmts()
//   int getNElmtNames()
//   String getElmtName(int inIndex)
//   String[] getElmtNames()
//   int getNElmts()
//   int getNElmts(String inName)
//   Object getElmt(int inIndex)
//   Object getElmt(String inName, int inIndex)
//   Object getFirstElmt()
//   Object getFirstElmt(String inName)
//   Object[] getElmts()
//   ArrayList getElmts(String inName)
//
// I'm calling the characters outside of inner tags "contents".
// For our stuff, these characters are usually whitespace, but they
// may be meaningful if tags are used as mark-up.  In any case, these
// chars must be preserved to recreate the original XML.  Note, if a
// tag has text, one of its contents will include that text plus
// any leading and trailing whitespace.
//
//   boolean getHasContents()
//   int getNContents()
//   String getContent(int inIndex)
//   String[] getContents()
//
// By default, this routine throws an exception.  Child classes are expected
// to override this method and do something useful.
//--------------------------------------------------------------------------------------------------------

  public Object buildObject(X2OData inX2OData) throws Exception {
    throw new Exception("Unknown tag, <"+inX2OData.getTagname()+"> at "+inX2OData.getFullPath());
  }

//--------------------------------------------------------------------------------------------------------
// startObject
//
// Called when a start tag is parsed, which allows you to adjust any local state info
// you may be carrying along.  For example, you could carry along a counter so that 
// newly built objects could be given a unique id in the order that they were seen.
//
// All the known data about the tag is in inX2OData
//
// Some useful routines on inX2OData include:
//   String getTagname()
//   String getFullPath()
//   X2OData getParentX2OData()
//
//   boolean getHasAttrs()
//   int getNAttrs()
//   String getAttrName(int inIndex)
//   String[] getAttrNames()
//   String getAttrValue(String inName)
//   String getAttrValue(int inIndex)
//   String[] getAttrValues()
//   HashMap getAttrMap()
//
// You can also walk up the chain to get grandparent Attrs as in:
//   getParentX2OData().getAttrNames()
//   getParentX2OData().getAttrValues()
//   getParentX2OData().getParentX2OData().getAttrValue("SomeAttrName")
//
// Since this routine is called when the start tag is parsed, this is
//   no data available about tag text or child Elmts.  The following
//   routines are present, but not useful.  They only become useful
//   when the end tag is parsed and buildObject() is called.
//
//   boolean getHasText()
//   String getText()
//
//   boolean getHasElmts()
//   int getNElmtNames()
//   String getElmtName(int inIndex)
//   String[] getElmtNames()
//   int getNElmts()
//   int getNElmts(String inName)
//   Object getElmt(int inIndex)
//   Object getElmt(String inName, int inIndex)
//   Object getFirstElmt()
//   Object getFirstElmt(String inName)
//   Object[] getElmts()
//   ArrayList getElmts(String inName)
//
//   boolean getHasContents()
//   int getNContents()
//   String getContent(int inIndex)
//   String[] getContents()
//
// By default, nothing is done.  If you are not keeping local tag dependent state,
// ignore this routine
//--------------------------------------------------------------------------------------------------------

  public void startObject(X2OData inX2OData) throws Exception { }

//--------------------------------------------------------------------------------------------------------
// createChildBuilder
//
// Typically a builder knows how to create a few closely related objects.
// If the xml contains a variety of complicated objects, it may make sense to have 
// multiple builders.  This routine lets you choose a builder for a child tag, based 
// on parent tag state and child tag name.
//
// By default, this builder is returned.  If you only need one builder, ignore this routine
//
// All the known data about the parent tag is in inParentX2OData.
//   Since this routine is called just before a child tag is parsed, the parent tag is
//   only partially parsed.  There is some incomplete data available, including attributes
//   and preceding child elements.
//--------------------------------------------------------------------------------------------------------

  public X2OBuilder createChildBuilder(X2OData inParentX2OData, String inChildTagname) throws Exception {
    return this; }

}
