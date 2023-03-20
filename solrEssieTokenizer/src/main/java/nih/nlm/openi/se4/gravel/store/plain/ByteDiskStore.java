//--------------------------------------------------------------------------------------------------------
// ByteDiskStore.java
//--------------------------------------------------------------------------------------------------------

package gravel.store.plain;

import java.io.*;

import gravel.store.*;
import gravel.utils.*;

//--------------------------------------------------------------------------------------------------------
// ByteDiskStore
//--------------------------------------------------------------------------------------------------------

public class ByteDiskStore extends ByteStore implements DiskStoreInterface {

//--------------------------------------------------------------------------------------------------------
// ByteDiskStore consts
//--------------------------------------------------------------------------------------------------------

  //  Bytes   Size    Content
  //   0-3      4     magic cookie and code version
  //    4       1     avail
  //    5       1     number of params
  //   6-7      2     avail
  //   8-15     8     store size (store capacity is file length minus header length)
  //  16-31    16     avail - could use as offset to next store in file - would determine capacity
  //  32-63    32     4 long params 
  
  public static final int      kFileHeaderSize=64;  // in bytes
  public static final int      kNParams=4;

  public static final String   kMagicCookieVersion="STR"+((char) 3);
    
//--------------------------------------------------------------------------------------------------------
// ByteDiskStore member vars
//--------------------------------------------------------------------------------------------------------

  private boolean       mReadOnly;
  private String        mFilename;
  private BasePool      mFilePool;

//--------------------------------------------------------------------------------------------------------
// ByteDiskStore - create
//--------------------------------------------------------------------------------------------------------

  public ByteDiskStore(String inFilename, long inCapacity, long[] inParams) {
    super(0,inCapacity,inParams);
    try {

      int thePos=Math.max(inFilename.lastIndexOf('/'),inFilename.lastIndexOf('\\'));
      if (thePos>0) {
        String theDirname=inFilename.substring(0,thePos);
        DirUtils.makeDir(theDirname,false);
      }
      FileUtils.deleteFile(inFilename);

      StoreFile theStoreFile=new StoreFile(inFilename,false);
      theStoreFile.open();
      theStoreFile.setLength(getFileSize());
      
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theHeaderBytes=Allocate.newBytes(kFileHeaderSize);
      UTF8Utils.stringToUTF8Bytes(kMagicCookieVersion,theHeaderBytes);
      if (inParams!=null)
        theHeaderBytes[5]=(byte) inParams.length;
      Conversions.longToBytes(getSize(),theHeaderBytes,8);
      if (inParams!=null) 
        Conversions.longsToBytes(inParams,0,inParams.length,theHeaderBytes,32);
      
      theStoreFile.seek(0);
      theStoreFile.writeBytes(theHeaderBytes,0,kFileHeaderSize);
      
      theSliceStore.putByteSlice(theHeaderBytes);

      mReadOnly=false;
      mFilename=inFilename;
      mFilePool=new BasePool(1) {      
        protected PoolObjectInterface newPoolObject() { 
          return new StoreFile(mFilename,mReadOnly); }};
      mFilePool.putPoolObject(theStoreFile);

    } catch (Exception e) {
      throw new StoreException("Cannot create "+inFilename,e);
    }
  }

  public ByteDiskStore(String inFilename, long inCapacity) { 
    this(inFilename,inCapacity,null); }

  public ByteDiskStore(String inFilename) { this(inFilename,16); }

//--------------------------------------------------------------------------------------------------------
// ByteDiskStore - load
//--------------------------------------------------------------------------------------------------------

  private ByteDiskStore(String inFilename, boolean inLoad) { 
    super(0,0,null);
    try {
      // Open existing file
      File theFile=new File(inFilename);
      if (!theFile.exists())        
        throw new IOException("File not found:  "+inFilename);        
      
      if (!theFile.canRead())
        throw new IOException("File cannot be read:  "+inFilename);
     
      
      mReadOnly=true;
      try { mReadOnly=(!theFile.canWrite()); } catch (SecurityException e) { }

      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theHeaderBytes=theSliceStore.getByteSlice();

      StoreFile theStoreFile=new StoreFile(inFilename,mReadOnly);
      theStoreFile.open();
      theStoreFile.readBytes(theHeaderBytes,0,kFileHeaderSize);
      long theLength=theStoreFile.getLength();

      if (!UTF8Utils.bytesToUTF8String(theHeaderBytes,0,4).equals(kMagicCookieVersion))
        throw new IOException("Store file corrupt:  "+inFilename);

      long theCapacity=theLength-kFileHeaderSize;
      if (theCapacity<0)
        throw new IOException("Store file corrupt:  "+inFilename);
      super.setCapacity(theCapacity);
       
      long theSize=Conversions.bytesToLong(theHeaderBytes,8);
      if ((theSize<0)||(theSize>theCapacity))
        throw new IOException("Store file corrupt:  "+inFilename);
      super.setSize(theSize);

      int theNParams=theHeaderBytes[5];
      if ((theNParams<0)||(theNParams>4))
        throw new IOException("Store file corrupt:  "+inFilename+", "+theNParams);
      super.setNParams(theNParams);
      for (int i=0; i<theNParams; i++)
        super.setParam(i,Conversions.bytesToLong(theHeaderBytes,32+i*8));

      theSliceStore.putByteSlice(theHeaderBytes);
      
      mFilename=inFilename;
      mFilePool=new BasePool(1) {      
        protected PoolObjectInterface newPoolObject() { 
          return new StoreFile(mFilename,mReadOnly); }};
      mFilePool.putPoolObject(theStoreFile);

    } catch (Exception e) {
      throw new StoreException("Cannot open "+inFilename,e);
    }
  }
   
//--------------------------------------------------------------------------------------------------------
// load
//--------------------------------------------------------------------------------------------------------

  public static ByteDiskStore load(String inFilename) { 
    return new ByteDiskStore(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    mFilename=null;
    if (mFilePool!=null) {
      mFilePool.close();
      mFilePool=null;
    }
    super.close();
  }

//--------------------------------------------------------------------------------------------------------
// getIsClosed
//--------------------------------------------------------------------------------------------------------

  public boolean getIsClosed() { return (mFilename==null); }

//--------------------------------------------------------------------------------------------------------
// getFilename
//--------------------------------------------------------------------------------------------------------

  public String getFilename() { return mFilename; }

//--------------------------------------------------------------------------------------------------------
// calcFileOffset
//--------------------------------------------------------------------------------------------------------

  private long calcFileOffset(long inOffset) { return inOffset+kFileHeaderSize; }

//--------------------------------------------------------------------------------------------------------
// getFileSize
//--------------------------------------------------------------------------------------------------------

  public long getFileSize() { return calcFileOffset(getCapacity()); }

//--------------------------------------------------------------------------------------------------------
// getReadOnly
//--------------------------------------------------------------------------------------------------------

  public boolean getReadOnly() { return mReadOnly; }

//--------------------------------------------------------------------------------------------------------
// getMemory
//--------------------------------------------------------------------------------------------------------

  public long getMemory() {
    if (kRangeChecking) 
      if (getIsClosed())
        throw new StoreException("Store closed");
    long theMemory=super.getMemory()+
        3*kReferenceMemory+kBooleanMemory+kLongMemory+
        Allocate.getStringMemory(mFilename)+
        2*k1K;  // Extra 2K is approx for FilePool - Don't know what to do about cache
    return theMemory;
  }

//--------------------------------------------------------------------------------------------------------
// getContentMemory
//--------------------------------------------------------------------------------------------------------

  public long getContentMemory() { return 0; }

//--------------------------------------------------------------------------------------------------------
// getStoreFile
//--------------------------------------------------------------------------------------------------------

  private StoreFile getStoreFile() { 
    if (kRangeChecking) 
      if (getIsClosed())
        throw new StoreException("Store closed");
    try {
      return (StoreFile) mFilePool.getPoolObject(); 
    } catch (StoreException e) {
      throw e;
    } catch (Exception e) {
      throw new StoreException("Cannot get store file "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// putStoreFile
//--------------------------------------------------------------------------------------------------------

  private void putStoreFile(StoreFile inStoreFile) { mFilePool.putPoolObject(inStoreFile); }

//--------------------------------------------------------------------------------------------------------
// setSize
//--------------------------------------------------------------------------------------------------------

  public void setSize(long inSize) {
    long theOldSize=getSize();
    super.setSize(inSize);
    long theNewSize=getSize();
    if (theOldSize!=theNewSize) {
      byte[] theBytes=new byte[8];
      Conversions.longToBytes(theNewSize,theBytes,0);
      StoreFile theStoreFile=getStoreFile();
      theStoreFile.seek(8);      
      theStoreFile.writeBytes(theBytes); 
      putStoreFile(theStoreFile);
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// setCapacity
//--------------------------------------------------------------------------------------------------------

  public void setCapacity(long inCapacity) {
    long theOldCapacity=getCapacity();
    super.setCapacity(inCapacity);
    long theNewCapacity=getCapacity();

    if (kRangeChecking) 
      if (getIsClosed())
        throw new StoreException("Store closed");

    if (theNewCapacity!=theOldCapacity) {
      StoreFile theStoreFile=getStoreFile();
      theStoreFile.setLength(calcFileOffset(theNewCapacity));
      putStoreFile(theStoreFile);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setNParams
//--------------------------------------------------------------------------------------------------------

  public void setNParams(int inNParams) {
    int theOldNParams=getNParams();
    super.setNParams(inNParams);
    int theNewNParams=getNParams();
    if (theOldNParams!=theNewNParams) {
      StoreFile theStoreFile=getStoreFile();
      theStoreFile.seek(5);
      theStoreFile.writeByte((byte) inNParams); 
      putStoreFile(theStoreFile);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setParam
//--------------------------------------------------------------------------------------------------------

  public void setParam(int inIndex, long inParam) {
    if ((inIndex<0)||(inIndex>4))
      throw new StoreException("Invalid param");
    long theOldParam=getParam(inIndex);
    super.setParam(inIndex,inParam);
    long theNewParam=getParam(inIndex);
    if (theOldParam!=theNewParam) {
      byte[] theBytes=new byte[8];
      Conversions.longToBytes(theNewParam,theBytes,0);
      StoreFile theStoreFile=getStoreFile();
      theStoreFile.seek(32+inIndex*8);
      theStoreFile.writeBytes(theBytes,0,8); 
      putStoreFile(theStoreFile);
    }
  }
 
//--------------------------------------------------------------------------------------------------------
// copy
//
// Not thread safe
//--------------------------------------------------------------------------------------------------------

  public void copy(String inFilename, boolean inCompact) {
    if (!mFilename.equals(inFilename)) {
      try {
        mFilePool.drain();
        FileUtils.copyBinaryFile(mFilename,inFilename);
        if (inCompact)
          FileUtils.setFileLength(inFilename,calcFileOffset(getSize()));
        mFilePool.stopDrain();
      } catch (Exception e) {
        throw new StoreException("Cannot copy "+mFilename+" --> "+inFilename,e);
      }
    }
  }
  
  public void copy(String inFilename) { copy(inFilename,true); }

//--------------------------------------------------------------------------------------------------------
// closeAndDelete
//--------------------------------------------------------------------------------------------------------

  public void closeAndDelete() {
    String theFilename=mFilename;
    try {
      close();
      FileUtils.deleteFile(theFilename);
    } catch (Exception e) {
      throw new StoreException("Cannot close and delete "+theFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getByte
//--------------------------------------------------------------------------------------------------------

  public byte getByte(long inOffset) { 
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }
      
    StoreFile theStoreFile=getStoreFile();
    theStoreFile.seek(calcFileOffset(inOffset));
    byte theByte=theStoreFile.readByte(); 
    putStoreFile(theStoreFile);
    return theByte;
  }

//--------------------------------------------------------------------------------------------------------
// getVarLong
//--------------------------------------------------------------------------------------------------------

  public long getVarLong(long inOffset, int inLongSize) {
    return Conversions.bytesToVarLong(getBytes(inOffset,inLongSize),inLongSize); }

//--------------------------------------------------------------------------------------------------------
// getVarDouble
//--------------------------------------------------------------------------------------------------------

  public double getVarDouble(long inOffset, int inDoubleSize) {
    return Conversions.bytesToVarDouble(getBytes(inOffset,inDoubleSize),inDoubleSize); }

//--------------------------------------------------------------------------------------------------------
// getBytes
//--------------------------------------------------------------------------------------------------------

  public void getBytes(long inOffset, byte[] ioBytes, int inByteDelta, int inNBytes) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inByteDelta<0)
        throw new StoreException("Negative ByteDelta: "+inByteDelta);
      if (inOffset+inNBytes>getSize())
        throw new StoreException("Offset+NBytes past end: "+inOffset+"+"+inNBytes+">"+getSize());
      if (inByteDelta+inNBytes>ioBytes.length)
        throw new StoreException("ByteDelta+NBytes past end: "+inByteDelta+"+"+inNBytes+">"+ioBytes.length);
    }
    
    if (inNBytes>0) {
      StoreFile theStoreFile=getStoreFile();
      theStoreFile.seek(calcFileOffset(inOffset));
      theStoreFile.readBytes(ioBytes,inByteDelta,inNBytes); 
      putStoreFile(theStoreFile);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setByte
//--------------------------------------------------------------------------------------------------------

  public void setByte(long inOffset, byte inByte) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inOffset>=getSize())
        throw new StoreException("Offset past end: "+inOffset+">="+getSize());
    }
      
    StoreFile theStoreFile=getStoreFile();
    theStoreFile.seek(calcFileOffset(inOffset));
    theStoreFile.writeByte(inByte); 
    putStoreFile(theStoreFile);
  }

//--------------------------------------------------------------------------------------------------------
// setVarLong
//--------------------------------------------------------------------------------------------------------

  public void setVarLong(long inOffset, long inLong, int inLongSize) {
    setBytes(inOffset,Conversions.longToVarBytes(inLong,inLongSize)); }

//--------------------------------------------------------------------------------------------------------
// setVarDouble
//--------------------------------------------------------------------------------------------------------

  public void setVarDouble(long inOffset, double inDouble, int inDoubleSize) {
    setBytes(inOffset,Conversions.doubleToVarBytes(inDouble,inDoubleSize)); }

//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public void setBytes(long inOffset, byte inValue, long inNCopies) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNCopies<0)
        throw new StoreException("Negative NCopies: "+inNCopies);
      if (inOffset+inNCopies>getSize())
        throw new StoreException("Offset+NCopies past end: "+inOffset+"+"+inNCopies+">"+getSize());
    }

    if (inNCopies>0) {
      SliceStore theSliceStore=SliceStore.getSliceStore();
      byte[] theByteSlice=theSliceStore.getByteSlice();
      int theNCopies=(int) Math.min(kByteSliceSize,inNCopies);
      for (int i=0; i<theNCopies; i++)
        theByteSlice[i]=inValue;
      if (inNCopies<kByteSliceSize) 
        setBytes(inOffset,theByteSlice,0,theNCopies);
      else {
        long theOffset=inOffset;
        long theLimit=inOffset+inNCopies-kByteSliceSize;
        while (theOffset<=theLimit) {
          setBytes(theOffset,theByteSlice,0,kByteSliceSize);
          theOffset+=kByteSliceSize;
        }
        int theRemainder=(int) (inOffset+inNCopies-theOffset);
        if (theRemainder>0)
          setBytes(theOffset,theByteSlice,0,theRemainder);
      }
      theSliceStore.putByteSlice(theByteSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setBytes
//--------------------------------------------------------------------------------------------------------

  public void setBytes(long inOffset, byte[] inBytes, int inByteDelta, int inNBytes) {
    if (kRangeChecking) {
      if (getIsClosed())
        throw new StoreException("Store closed");
      if (inOffset<0)
        throw new StoreException("Negative offset: "+inOffset);
      if (inNBytes<0)
        throw new StoreException("Negative NBytes: "+inNBytes);
      if (inByteDelta<0)
        throw new StoreException("Negative ByteDelta: "+inByteDelta);
      if (inOffset+inNBytes>getSize())
        throw new StoreException("Offset+NBytes past end: "+inOffset+"+"+inNBytes+">"+getSize());
      if (inByteDelta+inNBytes>inBytes.length)
        throw new StoreException("ByteDelta+NBytes past end: "+inByteDelta+"+"+inNBytes+">"+inBytes.length);
    }

    if (inNBytes>0) {
      StoreFile theStoreFile=getStoreFile();
      theStoreFile.seek(calcFileOffset(inOffset));
      theStoreFile.writeBytes(inBytes,inByteDelta,inNBytes); 
      putStoreFile(theStoreFile);
    }
  }
 
}

