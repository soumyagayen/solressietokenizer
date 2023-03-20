//--------------------------------------------------------------------------------------------------------
// X2OParser.java
//
// Implements XML to object transforms
//--------------------------------------------------------------------------------------------------------

package gravel.xml;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// X2OParser.java
//
// No user serviceable parts
//--------------------------------------------------------------------------------------------------------

public class X2OParser extends DefaultHandler implements PoolObjectInterface {

//--------------------------------------------------------------------------------------------------------
// X2OParser consts
//--------------------------------------------------------------------------------------------------------

  private static final boolean    kIgnorePI=true;

  private static final int        kPreferredNInPool=1;
  private static final BasePool   kX2OParserPool;
  
//--------------------------------------------------------------------------------------------------------
// X2OParser class init
//--------------------------------------------------------------------------------------------------------

  static {
    try {
      kX2OParserPool=new BasePool(kPreferredNInPool) {
        protected PoolObjectInterface newPoolObject() { return new X2OParser(); } };
    } catch (Throwable e) {
      System.err.println(FormatUtils.formatException("Cannot init X2OParser",e));
      throw e;
    }
  }

//--------------------------------------------------------------------------------------------------------
// getX2OParser 
//--------------------------------------------------------------------------------------------------------

  static X2OParser getX2OParser() throws Exception { return (X2OParser) kX2OParserPool.getPoolObject(); }

//--------------------------------------------------------------------------------------------------------
// putX2OParser
//--------------------------------------------------------------------------------------------------------

  static void putX2OParser(X2OParser inX2OParser) { kX2OParserPool.putPoolObject(inX2OParser); }

//--------------------------------------------------------------------------------------------------------
// X2OParser member vars
//--------------------------------------------------------------------------------------------------------

  private Schema         mSchema;
  private X2OBuilder     mX2OBuilder;

  private boolean        mInitialized;
  private char[]         mContentBuffer;
  private SAXParser      mSAXParser;
  private Locator        mLocator;
  private int            mNElmts;

  private X2OConverter   mX2OConverter;
  private X2OData[]      mX2ODatas;
  private int            mNX2ODatas;
  private Object         mObject;

//--------------------------------------------------------------------------------------------------------
// X2OParser
//--------------------------------------------------------------------------------------------------------

  private X2OParser(Schema inSchema) {
    mSchema=inSchema;
  }

  private X2OParser() { this(null); }

//--------------------------------------------------------------------------------------------------------
// gets
//--------------------------------------------------------------------------------------------------------

  public Schema getSchema() { return mSchema; }

//--------------------------------------------------------------------------------------------------------
// canRecycle
//--------------------------------------------------------------------------------------------------------

  public boolean canRecycle() { return true; }

//--------------------------------------------------------------------------------------------------------
// open
//--------------------------------------------------------------------------------------------------------

  public void open() {

    if (!mInitialized) {
      mContentBuffer=Allocate.newChars(256);

      // Construct a SAX parser
      try {
        SAXParserFactory theSAXParserFactory=SAXParserFactory.newInstance();
        theSAXParserFactory.setNamespaceAware(true);
        theSAXParserFactory.setValidating(true);
        if (mSchema!=null)
          theSAXParserFactory.setSchema(mSchema);
        mSAXParser=theSAXParserFactory.newSAXParser();
      } catch (Exception e) {
        throw new RuntimeException("Could not build SAX Parser",e);
      }

      mX2ODatas=new X2OData[16];
      mNX2ODatas=0;

      mInitialized=true;
    }
  }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    mX2OBuilder=null;

    mInitialized=false;
    mContentBuffer=null;
    mSAXParser=null;
    mLocator=null;

    mX2OConverter=null;
    mX2ODatas=null;
    mObject=null;
  }

//--------------------------------------------------------------------------------------------------------
// check
//--------------------------------------------------------------------------------------------------------

  public boolean check() { return true; }

//--------------------------------------------------------------------------------------------------------
// getLocator
//--------------------------------------------------------------------------------------------------------

  Locator getLocator() { return mLocator; }

//--------------------------------------------------------------------------------------------------------
// nextX2OConverter
//
// Adds new X2OConverter to chain.  
// Keeps reference to new X2OConverter so it can pass all the SAX callbacks to it.
//--------------------------------------------------------------------------------------------------------

  X2OConverter nextX2OConverter(String inTagname, X2OBuilder inX2OBuilder, X2OData inParentX2OData) {

    // Get X2OData from pool or local cache
    X2OData theX2OData;
    if (mNX2ODatas==0)
      theX2OData=X2OData.getX2OData();
    else {
      mNX2ODatas--;
      theX2OData=mX2ODatas[mNX2ODatas];
      mX2ODatas[mNX2ODatas]=null;
    }

    // Prepare X2OData with what is already known
    theX2OData.prepare(inTagname,inParentX2OData,mNElmts);
    mNElmts++;

    // Make new X2OConverter the current X2OConverter to pass SAX callbacks to
    mX2OConverter=new X2OConverter(inX2OBuilder,theX2OData,mContentBuffer,mX2OConverter,this);

    return mX2OConverter;
  }

//--------------------------------------------------------------------------------------------------------
// prevX2OConverter
//
// Removes X2OConverter from chain.  
// Keeps reference to parent of removed X2OConverter
//--------------------------------------------------------------------------------------------------------

  void prevX2OConverter(X2OData inX2OData, X2OConverter inParentX2OConverter) {

    // Empty X2OData, but don't release its memory
    inX2OData.prepare(null,null,kNotFound);

    // Return X2OData to local cache
    if (mNX2ODatas==mX2ODatas.length) {
      X2OData[] theX2ODatas=mX2ODatas;
      mX2ODatas=new X2OData[2*mNX2ODatas+1];
      System.arraycopy(theX2ODatas,0,mX2ODatas,0,mNX2ODatas);
    }
    mX2ODatas[mNX2ODatas]=inX2OData;
    mNX2ODatas++;

    // Make parent X2OConverter the current X2OConverter to pass SAX callbacks to
    mX2OConverter=inParentX2OConverter;
  }

//--------------------------------------------------------------------------------------------------------
// saveObject
//--------------------------------------------------------------------------------------------------------

  void saveObject(Object inObject) { 
    mObject=inObject; 
  }

//--------------------------------------------------------------------------------------------------------
// ContentHandler methods
//--------------------------------------------------------------------------------------------------------

  //------------------------------------------------------------------------------------------------------
  // setDocumentLocator
  //------------------------------------------------------------------------------------------------------

  public void setDocumentLocator(Locator inLocator) {
    mLocator=inLocator;
  }

  //------------------------------------------------------------------------------------------------------
  // startElement
  //------------------------------------------------------------------------------------------------------

  public void startElement(String inUri, String inLocalPart, String inTagname,
      Attributes inAttributes) throws SAXException {
    if (mX2OConverter!=null)
      mX2OConverter.startElement(inTagname,inAttributes);
    else {
      mX2OConverter=nextX2OConverter(inTagname,mX2OBuilder,null);
      mX2OConverter.beginElement(inAttributes); 
    }
  }

  //------------------------------------------------------------------------------------------------------
  // characters
  //------------------------------------------------------------------------------------------------------

  public void characters(char[] inChars, int inOffset, int inLength) throws SAXException {
    mX2OConverter.characters(inChars,inOffset,inLength);
  }

  //------------------------------------------------------------------------------------------------------
  // ignorableWhitespace
  //-----------------------------------------------------------------------------------------------------

  public void ignorableWhitespace(char[] inChars, int inOffset, int inLength) throws SAXException {
    characters(inChars,inOffset,inLength);
  }

  //------------------------------------------------------------------------------------------------------
  // processingInstruction
  //------------------------------------------------------------------------------------------------------

  public void processingInstruction(String inTarget, String inData) throws SAXException {
    if (!kIgnorePI)
      throw new SAXException("Processing instructions unsupported"+
          " - to silently ignore them, use pref: xml.parser.ignore_pi=true");
  }

  //------------------------------------------------------------------------------------------------------
  // endElement
  //------------------------------------------------------------------------------------------------------

  public void endElement(String inUri, String inLocalPart, String inTagname) throws SAXException {
    mX2OConverter.endElement(inTagname);
  }

  //------------------------------------------------------------------------------------------------------
  // warning
  //------------------------------------------------------------------------------------------------------

  public void warning(SAXParseException inException) throws SAXException {
    // defaults to do nothing
  }

  //------------------------------------------------------------------------------------------------------
  // error
  //------------------------------------------------------------------------------------------------------

  public void error(SAXParseException inException) throws SAXException {
    // defaults to do nothing
  }

  //------------------------------------------------------------------------------------------------------
  // fatalError
  //------------------------------------------------------------------------------------------------------

  public void fatalError(SAXParseException inException) throws SAXException {
    throw(inException); // defaults to kill parse
  }

//--------------------------------------------------------------------------------------------------------
// parseXML
//--------------------------------------------------------------------------------------------------------

  private Object parseXML(X2OBuilder inX2OBuilder, InputSource inInputSource) throws Exception {

    mX2OBuilder=inX2OBuilder;
    mNElmts=0;
    mNX2ODatas=0;

    open();

    // Parse XML
    // Callbacks to startElement(), characters(), and endElement(), occur during parsing
    Object theObject=null;
    try {
      mSAXParser.parse(inInputSource,this);
      theObject=mObject;
    } finally {
      // Clean up
      mObject=null;
      mX2OConverter=null;
      mX2OBuilder=null;
      for (int i=0; i<mNX2ODatas; i++) {
        X2OData.putX2OData(mX2ODatas[i]);
        mX2ODatas[i]=null;
      }
      mNX2ODatas=0;
    }

    return theObject;
  }

//--------------------------------------------------------------------------------------------------------
// xsdToSchema
//--------------------------------------------------------------------------------------------------------

  public static Schema xsdToSchema(String inXSD) throws Exception {
    SchemaFactory theSchemaFactory=SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    return theSchemaFactory.newSchema(new StreamSource(new StringReader(inXSD)));
  }

//--------------------------------------------------------------------------------------------------------
// xsdFileToSchema
//--------------------------------------------------------------------------------------------------------

  public static Schema xsdFileToSchema(String inFilename) throws Exception {
    return xsdToSchema(TextFileUtils.loadTextFile(inFilename)); }
  
//--------------------------------------------------------------------------------------------------------
// findSkip
//--------------------------------------------------------------------------------------------------------

  public static int findSkip(String inXML, boolean inSkipDTD) throws Exception {

    // Determine how many chars to skip
    int theNSkipped=0;
    if (inSkipDTD) {
      int thePos=inXML.indexOf("<?xml ");
      if (thePos>=0)
        theNSkipped=inXML.indexOf("?>",thePos)+2;
      // This is not robust - will be defeated by complicated DOCTYPE defs containing '>' chars
      thePos=inXML.indexOf("<!DOCTYPE ",theNSkipped);
      if (thePos>=0)
        theNSkipped=inXML.indexOf(">",thePos)+1;
    }
    theNSkipped=inXML.indexOf("<",theNSkipped);
    return theNSkipped;
  }

//--------------------------------------------------------------------------------------------------------
// findSkip
//--------------------------------------------------------------------------------------------------------

  public static int findSkip(byte[] inLeadingBytes, int inByteDelta, int inNBytes, 
      String inEncoding, boolean inSkipDTD) throws Exception {

    // Determine how many bytes to skip
    int theNSkipped=0;
    if (inSkipDTD) {

      // Look for char encoding header
      String theStartString="<?xml ";
      byte[] theStartBytes=theStartString.getBytes(inEncoding);
      int theLimit=inByteDelta+inNBytes-(theStartBytes.length-1);
      int theStartPos=kNotFound;
      boolean theFoundStart=false;
      for (int i=inByteDelta; i<theLimit; i++)
        if (inLeadingBytes[i]==theStartBytes[0]) {
          theFoundStart=true;
          for (int j=1; j<theStartBytes.length; j++)
            if (inLeadingBytes[i+j]!=theStartBytes[j]) {
              theFoundStart=false;
              break;
            }
          if (theFoundStart) {
            theStartPos=i;
            break;
          }
        }
  
      // Found header tag start - now find end
      // This is not robust - will be defeated by complicated headers containing '?>' chars
      if (theFoundStart) {
        String theEndString="?>";
        byte[] theEndBytes=theEndString.getBytes(inEncoding);
        theLimit=inByteDelta+inNBytes-(theEndBytes.length-1);
        for (int i=theStartPos+theStartBytes.length; i<theLimit; i++)
          if (inLeadingBytes[i]==theEndBytes[0]) {
            boolean theFoundEnd=true;
            for (int j=1; j<theEndBytes.length; j++)
              if (inLeadingBytes[i+j]!=theEndBytes[j]) {
                theFoundEnd=false;
                break;
              }
            if (theFoundEnd) {
              theNSkipped=i-inByteDelta+theEndBytes.length;
              break;
            }
          }
      }

      // Look for DOCTYPE tag in leading bytes
      theStartString="<!DOCTYPE ";
      theStartBytes=theStartString.getBytes(inEncoding);
      theLimit=inByteDelta+inNBytes-(theStartBytes.length-1)-theNSkipped;
      theFoundStart=false;
      for (int i=inByteDelta+theNSkipped; i<theLimit; i++)
        if (inLeadingBytes[i]==theStartBytes[0]) {
          theFoundStart=true;
          for (int j=1; j<theStartBytes.length; j++)
            if (inLeadingBytes[i+j]!=theStartBytes[j]) {
              theFoundStart=false;
              break;
            }
          if (theFoundStart) {
            theStartPos=i;
            break;
          }
        }
  
      // Found DOCTYPE tag start - now find end
      // This is not robust - will be defeated by complicated DOCTYPE defs containing '>' chars
      if (theFoundStart) {
        String theEndString=">";
        byte[] theEndBytes=theEndString.getBytes(inEncoding);
        theLimit=inByteDelta+inNBytes-(theEndBytes.length-1);
        for (int i=theStartPos+theStartBytes.length; i<theLimit; i++)
          if (inLeadingBytes[i]==theEndBytes[0]) {
            boolean theFoundEnd=true;
            for (int j=1; j<theEndBytes.length; j++)
              if (inLeadingBytes[i+j]!=theEndBytes[j]) {
                theFoundEnd=false;
                break;
              }
            if (theFoundEnd) {
              theNSkipped=i-inByteDelta+theEndBytes.length;
              break;
            }
          }
      }
    }    
    
    // Skip to first <
    // Many XML sources (especially those from files) have leading magic cookies which choke the parser
    if (theNSkipped==0) {
      byte theStartByte=(byte) '<';
      int theLimit=inByteDelta+inNBytes;
      for (int i=inByteDelta; i<theLimit; i++)
        if (inLeadingBytes[i]==theStartByte) {
          theNSkipped=i-inByteDelta;
          break;
        }
    }    
    
    return theNSkipped;
  }
  
//--------------------------------------------------------------------------------------------------------
// makeInputSource
//--------------------------------------------------------------------------------------------------------

  public static InputSource makeInputSource(String inXML, boolean inSkipDTD) throws Exception {
    int theNSkipped=findSkip(inXML,inSkipDTD);
    StringReader theReader=new StringReader(inXML);
    theReader.skip(theNSkipped);
    return new InputSource(theReader); 
  }

//--------------------------------------------------------------------------------------------------------
// makeInputSource
//--------------------------------------------------------------------------------------------------------

  public static InputSource makeInputSource(byte[] inBytes, int inByteDelta, int inNBytes, 
      boolean inSkipDTD) throws Exception {
    int theNSkipped=findSkip(inBytes,0,Math.min(inNBytes,1024),"UTF-8",inSkipDTD);
    InputSource theInputSource=new InputSource(
        new ByteArrayInputStream(inBytes,inByteDelta+theNSkipped,inNBytes-theNSkipped));
    theInputSource.setEncoding("UTF-8");
    return theInputSource;
  }

//--------------------------------------------------------------------------------------------------------
// makeInputSource
//--------------------------------------------------------------------------------------------------------

  public static InputSource makeInputSource(String inFilename, String inEncoding, 
      boolean inSkipDTD) throws Exception {
    byte[] theLeadingBytes=Allocate.newBytes(1024);
    BufferedInputStream theStream=FileUtils.openBufferedInputStream(inFilename);
    int theNRead=theStream.read(theLeadingBytes);
    int theNSkipped=findSkip(theLeadingBytes,0,theNRead,inEncoding,inSkipDTD);
    theStream.close();
    theStream=FileUtils.openBufferedInputStream(inFilename);
    while (theNSkipped>0) 
      theNSkipped-=theStream.skip(theNSkipped);
    InputSource theInputSource=new InputSource(theStream);
    theInputSource.setEncoding(inEncoding);
    return theInputSource;
  }

//--------------------------------------------------------------------------------------------------------
// xmlToObject
//--------------------------------------------------------------------------------------------------------

  public static Object xmlToObject(X2OBuilder inX2OBuilder, Schema inSchema,
      InputSource inInputSource) throws Exception { 
    X2OParser theX2OParser=null;
    try {
      if (inSchema==null)
        theX2OParser=getX2OParser(); 
      else 
        theX2OParser=new X2OParser(inSchema); 
      Object theObject=theX2OParser.parseXML(inX2OBuilder,inInputSource);      
      if (inSchema==null)
        putX2OParser(theX2OParser);
      else
        theX2OParser.close();
      return theObject;
    } catch (Exception e) { 
      if (theX2OParser!=null)
        theX2OParser.close();
      throw e; 
    }
  }

//--------------------------------------------------------------------------------------------------------
// blankNonXMLControlChars
//--------------------------------------------------------------------------------------------------------

  public static String blankNonXMLControlChars(String inString) {
    StringBuffer theStringBuffer=new StringBuffer(inString.length());
    for (int i=0; i<inString.length(); i++) {
      char theChar=inString.charAt(i);
      // Non-control chars, tabs, and linefeeds unchanged
      if ((theChar>=' ')||(theChar=='\t')||(theChar=='\n'))
        theStringBuffer.append(theChar);
      // All other control chars except carriage returns replaced with blanks
      else if (theChar!='\r') 
        theStringBuffer.append(' ');
      // Issolated carriage returns replaced with linefeeds
      else if (((i==0)||(theStringBuffer.charAt(i-1)!='\n'))&&
               ((i==inString.length()-1)||(inString.charAt(i+1)!='\n')))
        theStringBuffer.append('\n');
      // Carriage returns adjacent to linefeeds replaced with blanks
      else
        theStringBuffer.append(' ');
    }
    return theStringBuffer.toString();
  }

//--------------------------------------------------------------------------------------------------------
// xmlToObject for String
//--------------------------------------------------------------------------------------------------------
  
  public static Object xmlToObject(X2OBuilder inX2OBuilder, String inXML, 
      Schema inSchema, boolean inSkipDTD) throws Exception {
    return xmlToObject(inX2OBuilder,inSchema,makeInputSource(inXML,inSkipDTD)); }
 
  public static Object xmlToObject(X2OBuilder inX2OBuilder, String inXML) throws Exception {
    return xmlToObject(inX2OBuilder,inXML,null,true); }

//--------------------------------------------------------------------------------------------------------
// xmlToObject for byte[]
//--------------------------------------------------------------------------------------------------------

  public static Object xmlToObject(X2OBuilder inX2OBuilder, byte[] inBytes, int inByteDelta, int inNBytes, 
      Schema inSchema, boolean inSkipDTD) throws Exception {
    return xmlToObject(inX2OBuilder,inSchema,makeInputSource(inBytes,inByteDelta,inNBytes,inSkipDTD));
  }

  public static Object xmlToObject(X2OBuilder inX2OBuilder, byte[] inBytes) throws Exception {
    return xmlToObject(inX2OBuilder,inBytes,0,inBytes.length,null,true); }

//--------------------------------------------------------------------------------------------------------
// xmlFileToObject
//--------------------------------------------------------------------------------------------------------

  public static Object xmlFileToObject(X2OBuilder inX2OBuilder, String inFilename, String inEncoding, 
      Schema inSchema, boolean inSkipDTD) throws Exception {
    try {
      return xmlToObject(inX2OBuilder,inSchema,makeInputSource(inFilename,inEncoding,inSkipDTD));
    } catch (Exception e) {
      throw new Exception("Failed to parse "+inFilename,e);
    }
  }

  public static Object xmlFileToObject(X2OBuilder inX2OBuilder, String inFilename) throws Exception {
    return xmlFileToObject(inX2OBuilder,inFilename,"UTF-8",null,true); }

//--------------------------------------------------------------------------------------------------------
// xmlToX2OElmt
//--------------------------------------------------------------------------------------------------------

  public static X2OElmt xmlToX2OElmt(String inXML, final boolean inDropEmptyTags) throws Exception {
    try {
      return (X2OElmt) X2OParser.xmlToObject(new X2OBuilder() {
        public Object buildObject(X2OData inX2OData) throws Exception {
          if ((inDropEmptyTags)&&(inX2OData.getIsEmpty())&&(!inX2OData.getHasAttrs()))
            return null;
          return inX2OData.getX2OElmt();
        }
      },inXML); 
    } catch (Exception e) {
      throw new Exception("Problem parsing xml: "+kEOL+FormatUtils.cut(inXML,1024),e);
    }
  }

  public static X2OElmt xmlToX2OElmt(String inXML) throws Exception {
    return xmlToX2OElmt(inXML,false); }
  
}
