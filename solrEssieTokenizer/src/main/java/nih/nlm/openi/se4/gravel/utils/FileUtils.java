//--------------------------------------------------------------------------------------------------------
// FileUtils.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.*;

import gravel.store.plain.*;

//--------------------------------------------------------------------------------------------------------
// FileUtils
//--------------------------------------------------------------------------------------------------------

public abstract class FileUtils implements Constants {
  
//--------------------------------------------------------------------------------------------------------
// FileUtils constants
//--------------------------------------------------------------------------------------------------------

  public static final int                 kByteSliceSize=SliceStore.kByteSliceSize;
  
  public static final SimpleDateFormat    kFileDateFormat=new SimpleDateFormat("yyyy_MM_dd");
  public static final SimpleDateFormat    kFileTimeFormat=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
  
//--------------------------------------------------------------------------------------------------------
// getPath
//--------------------------------------------------------------------------------------------------------

  public static Path getPath(String inFilename) {
    // Supposed to leave in symlinks
    return Paths.get(inFilename); }

//--------------------------------------------------------------------------------------------------------
// getRealPath
//--------------------------------------------------------------------------------------------------------

  public static Path getRealPath(Path inPath) throws IOException {
    // Supposed to replace symlinks
    return inPath.toRealPath(); }

  public static Path getRealPath(String inFilename) throws IOException {
    return getRealPath(getPath(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// getRealFilename
//--------------------------------------------------------------------------------------------------------

  public static String getRealFilename(Path inPath) throws IOException {
    // Supposed to
    //   1) Return an absolute path up to disk
    //   2) Remove an . or .. dir names
    //   3) Replace symlinks with actual dirs
    return getRealPath(inPath).toString(); }   

  public static String getRealFilename(String inFilename) throws IOException {
    return getRealFilename(getPath(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// doesFileExist
//--------------------------------------------------------------------------------------------------------
 
  // Can be any kind of file, including a directory or symlink
  public static boolean doesFileExist(Path inPath) { 
    return Files.exists(inPath); }   

  public static boolean doesFileExist(String inFilename) { 
    return doesFileExist(getPath(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// isDirectory
//--------------------------------------------------------------------------------------------------------

  public static boolean isDirectory(Path inPath) {
    return Files.isDirectory(inPath); }

  public static boolean isDirectory(String inFilename) {
    return isDirectory(getPath(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// getFileLength
//--------------------------------------------------------------------------------------------------------

  public static long getFileLength(Path inPath) throws IOException { 
    // Compensating for java nio crap
    // Retry with delays to give file system a chance to catch up
    for (int i=0; i<100; i++) 
      try {
        return Files.size(inPath); 
      } catch (NoSuchFileException e) {
        try { Thread.sleep(10); } catch (Exception e2) {}
      }
    throw new NoSuchFileException(inPath.toString());
  }

  public static long getFileLength(String inFilename) throws IOException {
    return getFileLength(getPath(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// setFileLength
//--------------------------------------------------------------------------------------------------------

  public static void setFileLength(String inFilename, long inLength) throws IOException {
    StoreFile theFile=new StoreFile(inFilename,false);
    theFile.open();
    try {
      theFile.setLength(inLength);
    } finally {
      theFile.close();
    }
  }

//--------------------------------------------------------------------------------------------------------
// getLastModified
//--------------------------------------------------------------------------------------------------------

  public static long getLastModified(Path inPath) throws IOException {
    if (!doesFileExist(inPath))
      throw new IOException("Cannot find file, "+inPath);
    // Compensating for java nio crap
    // Retry with delays to give file system a chance to catch up
    for (int i=0; i<100; i++) 
      try {
        return Files.getLastModifiedTime(inPath).toMillis(); 
      } catch (NoSuchFileException e) {
        try { Thread.sleep(10); } catch (Exception e2) {}
      }
    throw new NoSuchFileException(inPath.toString());
  }
 
  public static long getLastModified(String inFilename) throws IOException {
    return getLastModified(getPath(inFilename)); }

//--------------------------------------------------------------------------------------------------------
// setLastModified
//--------------------------------------------------------------------------------------------------------

  public static void setLastModified(Path inPath, long inTime) throws IOException {
    if (!doesFileExist(inPath))
      throw new IOException("Cannot find file, "+inPath);
    // Compensating for java nio crap
    // Retry with delays to give file system a chance to catch up
    for (int i=0; i<100; i++) 
      try {
        Files.setLastModifiedTime(inPath,FileTime.fromMillis(inTime)); 
        return;
      } catch (NoSuchFileException e) {
        try { Thread.sleep(10); } catch (Exception e2) {}
      }
    throw new NoSuchFileException(inPath.toString());
  }

  public static void setLastModified(String inFilename, long inTime) throws IOException {
    setLastModified(getPath(inFilename),inTime); }

//--------------------------------------------------------------------------------------------------------
// makeTempFile
//
// With dirname, aaa, prefix, bbb, and filetype, .cc, makes a filepath like
//   aaa/bbb######.ccc
// which is a unique temp file in the given dir
//
// Dirname is important - if you give the wrong disk, the file will need to be copied when renamed
//
// Note!  File is created and should be deleted later.
// Cannot ensure filename remains unique unless file is created to hold ownership of name.
//--------------------------------------------------------------------------------------------------------
 
  public static String makeTempFile(Path inDirPath, String inFilePrefix, String inFileType) throws IOException {
    if (!DirUtils.doesDirExist(inDirPath))
      throw new IOException(inDirPath+" is not a dir");
    return getRealFilename(Files.createTempFile(inDirPath,inFilePrefix,inFileType)); 
  }

  public static String makeTempFile(String inDirname, String inFilePrefix, String inFileType) throws IOException {
    return makeTempFile(getPath(inDirname),inFilePrefix,inFileType); }
    
  public static String makeTempFile(String inDirname) throws IOException {
    return makeTempFile(inDirname,".temp",".tmp"); }
  
//--------------------------------------------------------------------------------------------------------
// makeFile
//--------------------------------------------------------------------------------------------------------

  public static String makeFile(Path inPath) throws IOException {
    return getRealFilename(Files.createFile(inPath)); }

  public static String makeFile(String inDirname) throws IOException {
    return makeFile(getPath(inDirname)); }

//--------------------------------------------------------------------------------------------------------
// deleteFile
//--------------------------------------------------------------------------------------------------------

  public static void deleteFile(Path inPath) throws IOException {
    if (doesFileExist(inPath)) {
      
      // Compensating for java nio crap
      // Retry with delays to give file system a chance to catch up
      for (int i=0; i<100; i++) 
        try {
          Files.delete(inPath);
          break;
        } catch (DirectoryNotEmptyException e) {
          try { Thread.sleep(10); } catch (Exception e2) { }
        }
      
    } else {
      // A broken symlink does not exist, but still can be deleted
      // Catch and ignore NoSuchFileException
      // This is a waste of effort - should be a better way
      try { 
        Files.delete(inPath); 
      } catch (NoSuchFileException e) { 
        // Ignore this exception - can occur when multiple threads are deleting the same file
      }
    }      
  }

  public static void deleteFile(String inFilename) throws IOException {
    deleteFile(getPath(inFilename)); }
  
//--------------------------------------------------------------------------------------------------------
// moveFile
//--------------------------------------------------------------------------------------------------------

  public static void moveFile(Path inSrcPath, Path inDstPath) throws IOException {
    // Remove file
    deleteFile(inDstPath);
    // Move file - supposed to keep last modified time
    Files.move(inSrcPath,inDstPath);
  }

  public static void moveFile(String inSrcFilename, String inDstFilename) throws IOException {
    moveFile(getPath(inSrcFilename),getPath(inDstFilename)); }

//--------------------------------------------------------------------------------------------------------
// getFilePath
//
// Gets path aaa/bbb in aaa/bbb/XXX.ZZZ
// Also gets parent dir given dir path - e.g. gets path aaa in aaa/bbb
//--------------------------------------------------------------------------------------------------------

  public static String getFilePath(String inFilename) {
    int thePos=Math.max(inFilename.lastIndexOf('/'),inFilename.lastIndexOf('\\'));
    if (thePos<0)
      return "";
    else      
      return inFilename.substring(0,thePos);
  }

//--------------------------------------------------------------------------------------------------------
// stripFilePath
//
// Strips path aaa/bbb/ (including trailing /) in aaa/bbb/XXX.ZZZ to make XXX.ZZZ
//--------------------------------------------------------------------------------------------------------

  public static String stripFilePath(String inFilename) {
    int thePos=Math.max(inFilename.lastIndexOf('/'),inFilename.lastIndexOf('\\'));
    if (thePos<0)
      return inFilename;
    else      
      return inFilename.substring(thePos+1);
  }

//--------------------------------------------------------------------------------------------------------
// getFileType
//
// Gets type ZZZ in aaa/bbb/XXX.ZZZ
//
// Changed strategy on 2015_11_24:  Had been returning only last filetype.  Now returning all
//   So, now  XYZ.txt.zip --> .txt.zip  instead of  .zip
//--------------------------------------------------------------------------------------------------------

  public static String getFileType(String inFilename) {
    String theFilenameSansPath=stripFilePath(inFilename); // Strip path first - don't want to find . in path
    int thePos=theFilenameSansPath.indexOf(".");
    if (thePos<0) 
      return "";
    else 
      return theFilenameSansPath.substring(thePos);
  }

//--------------------------------------------------------------------------------------------------------
// stripFileType
//
// Strips type .ZZZ (including leading .) in aaa/bbb/XXX.ZZZ to make aaa/bbb/XXX
//
// Changed strategy on 2015_11_24:  Had been removing only last filetype.  Now removing all
//   So, now  XYZ.txt.zip --> XYZ  instead of  XYZ.txt
//--------------------------------------------------------------------------------------------------------

  public static String stripFileType(String inFilename) {
    String theFilenameSansPath=stripFilePath(inFilename);    
    int thePos=theFilenameSansPath.indexOf(".");
    if (thePos<0) 
      return inFilename;
    else {
      // Keeps path if present
      int thePathLength=inFilename.length()-theFilenameSansPath.length();
      return inFilename.substring(0,thePathLength+thePos);
    }
  }

//--------------------------------------------------------------------------------------------------------
// getFilenameExtension
//
// Gets extension ZZZ in aaa/bbb/XXX_ZZZ.YYY
//--------------------------------------------------------------------------------------------------------

  public static String getFilenameExtension(String inFilename) {
    String theFilenameSansPathAndType=stripFileType(stripFilePath(inFilename));
    int thePos=theFilenameSansPathAndType.lastIndexOf("_");
    if (thePos<0) 
      return "";
    else 
      return theFilenameSansPathAndType.substring(thePos+1);
  }

//--------------------------------------------------------------------------------------------------------
// stripFilenameExtension
//
// Strips extension ZZZ in aaa/bbb/XXX_ZZZ.YYY to make aaa/bbb/XXX.YYY
//--------------------------------------------------------------------------------------------------------

  public static String stripFilenameExtension(String inFilename) {
    String theFilenameSansPath=stripFilePath(inFilename);
    String theFilenameSansPathAndType=stripFileType(theFilenameSansPath);
    int thePos=theFilenameSansPathAndType.lastIndexOf("_");
    if (thePos<0) 
      return inFilename;
    else {
      // Keeps path and type if present
      int thePathLength=inFilename.length()-theFilenameSansPath.length();
      int theTypeLength=theFilenameSansPath.length()-theFilenameSansPathAndType.length();
      return inFilename.substring(0,thePathLength+thePos)+
             inFilename.substring(inFilename.length()-theTypeLength);
    }
  }

//--------------------------------------------------------------------------------------------------------
// extendFilename
//
// Sticks extension ZZZ in aaa/bbb/XXX.YYY to make aaa/bbb/XXX_ZZZ.YYY
//--------------------------------------------------------------------------------------------------------

  public static String extendFilename(String inFilename, String inExtension) {
    int thePos=stripFileType(inFilename).length();
    return inFilename.substring(0,thePos)+'_'+inExtension+inFilename.substring(thePos);
  }

//--------------------------------------------------------------------------------------------------------
// loadBinaryFile
//--------------------------------------------------------------------------------------------------------

  public static long loadBinaryFile(String inFilename, long inFileOffset, 
      ByteRAMStore ioByteStore, long inByteOffset, long inNBytes) throws IOException {
    int theNBytes=0;
    StoreFile theFile=new StoreFile(inFilename,true);
    theFile.open();
    try {
      long theFileLength=getFileLength(inFilename);
      theNBytes=(int) Math.min(inNBytes,Math.max(0,theFileLength-inFileOffset));
      if (theNBytes>0) {
        theFile.seek(inFileOffset);
        byte[] theByteSlice=SliceStore.getSliceStore().getByteSlice();    
        try {
          long theLimit=inByteOffset+theNBytes-kByteSliceSize;
          long theByteOffset=inByteOffset;
          while (theByteOffset<=theLimit) {
            theFile.readBytes(theByteSlice,0,kByteSliceSize);
            ioByteStore.setBytes(theByteOffset,theByteSlice,0,kByteSliceSize);
            theByteOffset+=kByteSliceSize;
          }  
          int theRemainder=(int) (theNBytes+inByteOffset-theByteOffset);
          if (theRemainder>0) {
            theFile.readBytes(theByteSlice,0,theRemainder);
            ioByteStore.setBytes(theByteOffset,theByteSlice,0,theRemainder);
          }
        } finally {
          SliceStore.getSliceStore().putByteSlice(theByteSlice);   
        }
      }
    } finally {
      theFile.close();
    }
    return ioByteStore.getSize();
  }

  public static int loadBinaryFile(String inFilename, long inFileOffset, 
      byte[] ioBytes, int inByteDelta, int inNBytes) throws IOException {
    int theNBytes=0;
    StoreFile theFile=new StoreFile(inFilename,true);
    theFile.open();
    try {
      long theFileLength=getFileLength(inFilename);
      theNBytes=(int) Math.min(inNBytes,Math.max(0,theFileLength-inFileOffset));
      if (theNBytes>0) {
        theFile.seek(inFileOffset);
        theFile.readBytes(ioBytes,inByteDelta,theNBytes);
      }
    } finally {
      theFile.close();
    }
    return theNBytes;
  }

  public static int loadBinaryFile(String inFilename, byte[] ioBytes, int inByteDelta) throws IOException {
    long theFileLength=getFileLength(inFilename);
    if (theFileLength>k1G)
      throw new RuntimeException("File too big:  "+inFilename);
    int theNBytes=(int) theFileLength;
    loadBinaryFile(inFilename,0,ioBytes,inByteDelta,theNBytes);
    return theNBytes;
  }

  public static long loadBinaryFile(String inFilename, ByteRAMStore ioByteStore) throws IOException {
    long theFileLength=getFileLength(inFilename);
    loadBinaryFile(inFilename,0,ioByteStore,0,theFileLength);
    return theFileLength;
  }

  public static int loadBinaryFile(String inFilename, byte[] ioBytes) throws IOException {
    return loadBinaryFile(inFilename,ioBytes,0); }

  public static byte[] loadBinaryFile(String inFilename) throws IOException {
    long theFileLength=getFileLength(inFilename);
    if (theFileLength>k1G)
      throw new RuntimeException("File too big:  "+inFilename);
    byte[] theBytes=Allocate.newBytes(theFileLength);
    loadBinaryFile(inFilename,0,theBytes,0,(int) theFileLength);
    return theBytes;
  }

//--------------------------------------------------------------------------------------------------------
// modifyBinaryFile
//--------------------------------------------------------------------------------------------------------

  public static void modifyBinaryFile(ByteRAMStore inByteStore, long inByteOffset, long inNBytes,
      String inFilename, long inFileOffset) throws IOException {
    StoreFile theFile=new StoreFile(inFilename,false);
    theFile.open();
    try {
      long theFileLength=kNotFound;
      if (doesFileExist(inFilename))
        theFileLength=getFileLength(inFilename);
      if (theFileLength<inFileOffset) 
        theFile.setLength(inFileOffset);
      theFile.seek(inFileOffset);
      byte[] theByteSlice=SliceStore.getSliceStore().getByteSlice();    
      try {
        long theByteOffset=inByteOffset;
        long theLimit=inByteOffset+inNBytes-kByteSliceSize;
        while (theByteOffset<=theLimit) {
          inByteStore.getBytes(theByteOffset,theByteSlice,0,kByteSliceSize);
          theFile.writeBytes(theByteSlice,0,kByteSliceSize);
          theByteOffset+=kByteSliceSize;
        }
        int theRemainder=(int) (inByteOffset+inNBytes-theByteOffset);
        if (theRemainder>0) {
          inByteStore.getBytes(theByteOffset,theByteSlice,0,theRemainder);
          theFile.writeBytes(theByteSlice,0,theRemainder);
        }
      } finally {
        SliceStore.getSliceStore().putByteSlice(theByteSlice);   
      }
    } finally {
      theFile.close();
    }
  }

  public static void modifyBinaryFile(byte[] inBytes, int inByteDelta, int inNBytes,
      String inFilename, long inFileOffset) throws IOException {
    StoreFile theFile=new StoreFile(inFilename,false);
    theFile.open();
    try {
      long theFileLength=kNotFound;
      if (doesFileExist(inFilename))
        theFileLength=getFileLength(inFilename);
       if (theFileLength<inFileOffset) 
        theFile.setLength(inFileOffset);
      theFile.seek(inFileOffset);
      theFile.writeBytes(inBytes,inByteDelta,inNBytes);
    } finally {
      theFile.close();
    }
  }

  public static void modifyBinaryFile(ByteRAMStore inByteStore, String inFilename, long inFileOffset) throws IOException {
    modifyBinaryFile(inByteStore,0,inByteStore.getSize(),inFilename,inFileOffset); }

  public static void modifyBinaryFile(byte[] inBytes, String inFilename, long inFileOffset) throws IOException {
    modifyBinaryFile(inBytes,0,inBytes.length,inFilename,inFileOffset); }

//--------------------------------------------------------------------------------------------------------
// saveBinaryFile
//--------------------------------------------------------------------------------------------------------

  public static void saveBinaryFile(ByteRAMStore inByteStore, long inByteOffset, long inNBytes,
      String inFilename) throws IOException {
    deleteFile(inFilename);
    modifyBinaryFile(inByteStore,inByteOffset,inNBytes,inFilename,0);
  }

  public static void saveBinaryFile(byte[] inBytes, int inByteDelta, int inNBytes,
      String inFilename) throws IOException {
    deleteFile(inFilename);
    modifyBinaryFile(inBytes,inByteDelta,inNBytes,inFilename,0);
  }

  public static void saveBinaryFile(ByteRAMStore inByteStore, String inFilename) throws IOException {
    saveBinaryFile(inByteStore,0,inByteStore.getSize(),inFilename); }

  public static void saveBinaryFile(byte[] inBytes, String inFilename) throws IOException {
    saveBinaryFile(inBytes,0,inBytes.length,inFilename); }

//--------------------------------------------------------------------------------------------------------
// appendBinaryFile
//--------------------------------------------------------------------------------------------------------

  public static void appendBinaryFile(ByteRAMStore inByteStore, long inByteOffset, long inNBytes,
      String inFilename) throws IOException {
    long theFileLength=0;
    if (doesFileExist(inFilename))
      theFileLength=getFileLength(inFilename);
    modifyBinaryFile(inByteStore,inByteOffset,inNBytes,inFilename,theFileLength); 
  }

  public static void appendBinaryFile(byte[] inBytes, int inByteDelta, int inNBytes,
      String inFilename) throws IOException {
    long theFileLength=0;
    if (doesFileExist(inFilename))
      theFileLength=getFileLength(inFilename);
    modifyBinaryFile(inBytes,inByteDelta,inNBytes,inFilename,theFileLength);
  }

  public static void appendBinaryFile(ByteRAMStore inByteStore, String inFilename) throws IOException {
    appendBinaryFile(inByteStore,0,inByteStore.getSize(),inFilename); }

  public static void appendBinaryFile(byte[] inBytes, String inFilename) throws IOException {
    appendBinaryFile(inBytes,0,inBytes.length,inFilename); }

//--------------------------------------------------------------------------------------------------------
// copyBinaryFile
//--------------------------------------------------------------------------------------------------------

  public static void copyBinaryFile(String inSrcFilename, String inDstFilename) throws IOException {
    String theDstDirPath=FileUtils.getFilePath(inDstFilename);
    String theTempFilename=FileUtils.makeTempFile(theDstDirPath,".copy",".tmp");
    StoreFile theOutputFile=new StoreFile(theTempFilename,false);    
    theOutputFile.open();
    try {
      StoreFile theInputFile=new StoreFile(inSrcFilename,true);
      theInputFile.open();
      try {
        byte[] theByteSlice=SliceStore.getSliceStore().getByteSlice();    
        try {
          long theFileSize=theInputFile.getLength();
          long theOffset=0;
          long theLimit=theFileSize-kByteSliceSize;
          while (theOffset<=theLimit) {
            theInputFile.readBytes(theByteSlice,0,kByteSliceSize);
            theOutputFile.writeBytes(theByteSlice,0,kByteSliceSize);
            theOffset+=kByteSliceSize;
          }
          int theRemainder=(int) (theFileSize-theOffset);
          if (theRemainder>0) {
            theInputFile.readBytes(theByteSlice,0,theRemainder);
            theOutputFile.writeBytes(theByteSlice,0,theRemainder);
          }
        } finally {
          SliceStore.getSliceStore().putByteSlice(theByteSlice);
        }
      } finally {
        theInputFile.close();
      }
    } finally {
      theOutputFile.close();
    }
    FileUtils.moveFile(theTempFilename,inDstFilename);
    setLastModified(inDstFilename,getLastModified(inSrcFilename));
  }

//--------------------------------------------------------------------------------------------------------
// mergeBinaryFiles
//--------------------------------------------------------------------------------------------------------

  public static void mergeBinaryFiles(String[] inSrcFilenames, String inDstFilename) throws IOException {
    byte[] theByteSlice=SliceStore.getSliceStore().getByteSlice();
    try {
      String theDstDirPath=FileUtils.getFilePath(inDstFilename);
      String theTempFilename=FileUtils.makeTempFile(theDstDirPath,".merge",".tmp");
      StoreFile theOutputFile=new StoreFile(theTempFilename,false);
      theOutputFile.open();
      try {
        for (int i=0; i<inSrcFilenames.length; i++) {
          StoreFile theInputFile=new StoreFile(inSrcFilenames[i],true);
          theInputFile.open();
          try {
            long theFileSize=getFileLength(inSrcFilenames[i]);
            long theOffset=0;
            long theLimit=theFileSize-kByteSliceSize;
            while (theOffset<=theLimit) {
              theInputFile.readBytes(theByteSlice,0,kByteSliceSize);
              theOutputFile.writeBytes(theByteSlice,0,kByteSliceSize);
              theOffset+=kByteSliceSize;
            }
            int theRemainder=(int) (theFileSize-theOffset);
            if (theRemainder>0) {
              theInputFile.readBytes(theByteSlice,0,theRemainder);
              theOutputFile.writeBytes(theByteSlice,0,theRemainder);
            }
          } finally {
            theInputFile.close();
          }
        }
      } finally {
        theOutputFile.close();
      }
      FileUtils.moveFile(theTempFilename,inDstFilename);
    } finally {
      SliceStore.getSliceStore().putByteSlice(theByteSlice);
    }
  }

//--------------------------------------------------------------------------------------------------------
// openInputStream
//--------------------------------------------------------------------------------------------------------

  public static FileInputStream openInputStream(String inFilename, long inOffset) throws IOException {
    if ((inOffset<0)||(inOffset>getFileLength(inFilename)))
      throw new RuntimeException("Invalid offset: "+inOffset);
    FileInputStream theStream=new FileInputStream(inFilename); 
    try {
      while (inOffset>0) 
        inOffset-=theStream.skip(inOffset);
    } catch (Exception e) {
      theStream.close();
      throw e;
    }
    return theStream;
 }

  public static FileInputStream openInputStream(String inFilename) throws IOException {
    return openInputStream(inFilename,0); }

//--------------------------------------------------------------------------------------------------------
// openOutputStream
//--------------------------------------------------------------------------------------------------------

  public static FileOutputStream openOutputStream(String inFilename, boolean inAppend) throws IOException {
    String theDstDirPath=FileUtils.getFilePath(inFilename);
    DirUtils.makeDir(theDstDirPath);
    if (!inAppend)
      deleteFile(inFilename);
    FileOutputStream theFileOutputStream=new FileOutputStream(inFilename,inAppend);
    return theFileOutputStream;
  }

  public static FileOutputStream openOutputStream(String inFilename) throws IOException {
    return openOutputStream(inFilename,false); }

//--------------------------------------------------------------------------------------------------------
// openBufferedInputStream
//--------------------------------------------------------------------------------------------------------

  public static BufferedInputStream openBufferedInputStream(String inFilename, long inOffset) throws IOException {
    return new BufferedInputStream(openInputStream(inFilename,inOffset),kByteSliceSize); }

  public static BufferedInputStream openBufferedInputStream(String inFilename) throws IOException {
    return openBufferedInputStream(inFilename,0); }

//--------------------------------------------------------------------------------------------------------
// openBufferedOutputStream
//--------------------------------------------------------------------------------------------------------

  public static BufferedOutputStream openBufferedOutputStream(String inFilename,
      boolean inAppend) throws IOException {
    return new BufferedOutputStream(openOutputStream(inFilename,inAppend),kByteSliceSize); }

  public static BufferedOutputStream openBufferedOutputStream(String inFilename) throws IOException {
    return openBufferedOutputStream(inFilename,false); }

}
