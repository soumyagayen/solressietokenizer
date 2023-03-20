//--------------------------------------------------------------------------------------------------------
// StoreFile
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.io.*;

//--------------------------------------------------------------------------------------------------------
// StoreFile
//--------------------------------------------------------------------------------------------------------

public class StoreFile implements PoolObjectInterface { 

//--------------------------------------------------------------------------------------------------------
// StoreFile member vars
//--------------------------------------------------------------------------------------------------------

  private String            mFilename;
  private boolean           mReadOnly;
  private RandomAccessFile  mRandomAccessFile;      // ### Replace with java.nio.channels.FileChannel
  private boolean           mCanRecycle;

//--------------------------------------------------------------------------------------------------------
// StoreFile
//--------------------------------------------------------------------------------------------------------

  public StoreFile(String inFilename, boolean inReadOnly) {
    mFilename=inFilename;
    mReadOnly=inReadOnly;
    mCanRecycle=false;
  }

//--------------------------------------------------------------------------------------------------------
// open
//--------------------------------------------------------------------------------------------------------

  public void open() throws IOException {
    // Compensating for java io crap
    // Retry with delays to give file system a chance to catch up
    for (int i=0; i<100; i++) 
      try {
        mRandomAccessFile=new RandomAccessFile(mFilename,(mReadOnly?"r":"rws"));
        break; 
      } catch (IOException e) {
        if (i==99)
          throw e;
        try { Thread.sleep(10); } catch (Exception e2) {}
      }
    mCanRecycle=true;
  }

//--------------------------------------------------------------------------------------------------------
// close
//--------------------------------------------------------------------------------------------------------

  public void close() {
    mFilename=null;
    RandomAccessFile theRandomAccessFile=mRandomAccessFile;
    mRandomAccessFile=null;
    mCanRecycle=false;
    
    if (theRandomAccessFile!=null) {
      try { theRandomAccessFile.close(); } catch (Exception e) { }
      theRandomAccessFile=null;
    }
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
// getReadOnly
//--------------------------------------------------------------------------------------------------------

  public boolean getReadOnly() { return mReadOnly; }

//--------------------------------------------------------------------------------------------------------
// canRecycle
//--------------------------------------------------------------------------------------------------------

  public boolean canRecycle() { return mCanRecycle; }

//--------------------------------------------------------------------------------------------------------
// check
//--------------------------------------------------------------------------------------------------------

  public boolean check() { return mCanRecycle; }

//--------------------------------------------------------------------------------------------------------
// getLength
//--------------------------------------------------------------------------------------------------------

  public long getLength() { 
    try {
      return mRandomAccessFile.length(); 
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot get length of "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// setLength
//--------------------------------------------------------------------------------------------------------

  public void setLength(long inLength) { 
    try {
      long theOldLength=getLength();
      long theAmtGrown=Math.max(0,inLength-theOldLength);
      mRandomAccessFile.setLength(inLength); 
      if (theAmtGrown>0) { // If increasing size, write last byte to ensure space allocated
        mRandomAccessFile.seek(inLength-1);
        mRandomAccessFile.write((byte) -1);
      }
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot set length of "+mFilename,e);
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// seek
//--------------------------------------------------------------------------------------------------------

  public void seek(long inOffset) { 
    try {
      mRandomAccessFile.seek(inOffset); 
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot seek in "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// readByte
//
// Assumes seek already performed so file at correct position
//--------------------------------------------------------------------------------------------------------

  public byte readByte() { 
    try {
      int theValue=mRandomAccessFile.read(); 
      if (theValue==-1)
        throw new StoreException("Read past end of "+mFilename);
      return (byte) theValue;
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot read "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// readBytes
//
// Assumes seek already performed so file at correct position
//--------------------------------------------------------------------------------------------------------
  
  public void readBytes(byte[] outBytes, int inByteDelta, int inNBytes) { 
    try {
      int theValue=mRandomAccessFile.read(outBytes,inByteDelta,inNBytes);
      if (theValue<inNBytes)
        throw new StoreException("Read past end of "+mFilename);
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot read "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// readBytes
//--------------------------------------------------------------------------------------------------------

  public void readBytes(byte[] outBytes) { readBytes(outBytes,0,outBytes.length); }

//--------------------------------------------------------------------------------------------------------
// writeByte
//
// Assumes seek already performed so file at correct position
//--------------------------------------------------------------------------------------------------------

  public void writeByte(byte inByte) { 
    try {
      mRandomAccessFile.write(inByte); 
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot write "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// writeBytes
//
// Assumes seek already performed so file at correct position
//--------------------------------------------------------------------------------------------------------

  public void writeBytes(byte[] outBytes, int inByteDelta, int inNBytes) { 
    try {
      mRandomAccessFile.write(outBytes,inByteDelta,inNBytes);
    } catch (IOException e) {
      mCanRecycle=false;
      throw new StoreException("Cannot write "+mFilename,e);
    }
  }

//--------------------------------------------------------------------------------------------------------
// writeBytes
//--------------------------------------------------------------------------------------------------------

  public void writeBytes(byte[] outBytes) { writeBytes(outBytes,0,outBytes.length); }

}

