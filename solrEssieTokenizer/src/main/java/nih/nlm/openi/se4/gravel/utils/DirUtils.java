//--------------------------------------------------------------------------------------------------------
// DirUtils.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.stream.*;

import gravel.sort.*;

//--------------------------------------------------------------------------------------------------------
// DirUtils
//--------------------------------------------------------------------------------------------------------

public abstract class DirUtils extends FileUtils {
  
//--------------------------------------------------------------------------------------------------------
// DirUtils consts
//--------------------------------------------------------------------------------------------------------

  public static final String            kSystemTempDir;
  
  public static final Set<PosixFilePermission>   kDefaultPosixDirPermissions=EnumSet.of(
          PosixFilePermission.OWNER_READ, 
          PosixFilePermission.OWNER_WRITE, 
          PosixFilePermission.OWNER_EXECUTE, 
          PosixFilePermission.GROUP_READ, 
          PosixFilePermission.GROUP_EXECUTE);
  
  public static final FileAttribute     kDefaultPosixDirAttrs=
      PosixFilePermissions.asFileAttribute(kDefaultPosixDirPermissions);

//--------------------------------------------------------------------------------------------------------
// DirUtils class init
//--------------------------------------------------------------------------------------------------------

  static {
    String theSystemTmpDir=null;
    try {
      theSystemTmpDir=getRealFilename(System.getProperty("java.io.tmpdir"));
      if (theSystemTmpDir.endsWith("/"))
        theSystemTmpDir=theSystemTmpDir.substring(0,theSystemTmpDir.length()-1);
    } catch (Throwable e) {
      System.err.println(FormatUtils.formatException("Cannot init DirUtils",e));      
      System.exit(1);
    }
    kSystemTempDir=theSystemTmpDir;
  }  
  
//--------------------------------------------------------------------------------------------------------
// getSystemTempDir
//--------------------------------------------------------------------------------------------------------

  public static String getSystemTempDir() { return kSystemTempDir; }
  
//--------------------------------------------------------------------------------------------------------
// getCurrentWorkingDir
//--------------------------------------------------------------------------------------------------------

  public static String getCurrentWorkingDir() throws IOException {
    return getRealFilename("."); }

//--------------------------------------------------------------------------------------------------------
// doesDirExist
//--------------------------------------------------------------------------------------------------------

  public static boolean doesDirExist(Path inDirPath) throws IOException {
    if (!doesFileExist(inDirPath))
      return false;
    else
      return (isDirectory(getRealPath(inDirPath)));    // Resolves symlinks
  }

  public static boolean doesDirExist(String inDirname) throws IOException {
    return doesDirExist(getPath(inDirname)); }

//--------------------------------------------------------------------------------------------------------
// isDirEmpty
//--------------------------------------------------------------------------------------------------------

  public static boolean isDirEmpty(Path inPath) throws IOException {
    if (!doesDirExist(inPath))
      return true;
    boolean theDirEmpty;
    Stream<Path> theChildPathStream=null;
    try {
      theChildPathStream=Files.list(inPath);
      theDirEmpty=(!theChildPathStream.iterator().hasNext());
    } finally {
      theChildPathStream.close();
      theChildPathStream=null;
    }
    return theDirEmpty;
  }

  public static boolean isDirEmpty(String inDirname) throws IOException {
    return isDirEmpty(getPath(inDirname)); }
  
//--------------------------------------------------------------------------------------------------------
// isDirAlmostEmpty
//--------------------------------------------------------------------------------------------------------

  public static boolean isDirAlmostEmpty(Path inDirPath, String[] inIgnorableFiles) throws IOException {
    if (!doesDirExist(inDirPath))
      return true;
    String[] theDirnames=null;
    try {
      theDirnames=listDirs(inDirPath,null);
      if (theDirnames.length>0)
        return false;
    } catch (Exception e) { }
    if ((inIgnorableFiles==null)||(inIgnorableFiles.length==0))
      return isDirEmpty(inDirPath);
    ArrayList<Path> theFilePathList=new ArrayList<Path>();
    try {
      listFiles(inDirPath,theFilePathList,false,null);
      if (theFilePathList.size()==0)
        return true;
    } catch (Exception e) { 
      return true;
    }
    for (int i=0; i<theFilePathList.size(); i++) {
      String theFilename=theFilePathList.get(i).toString();
      boolean theFound=false;
      for (int j=0; j<inIgnorableFiles.length; j++) 
        if (theFilename.equals(inIgnorableFiles[j])) {
          theFound=true;
          break;
        }
      if (!theFound)
        return false;
    }
    return true;
  }

  public static boolean isDirAlmostEmpty(String inDirname, String[] inIgnorableFiles) throws IOException {
    return isDirAlmostEmpty(getPath(inDirname),inIgnorableFiles); }

  public static boolean isDirAlmostEmpty(String inDirname, String inIgnorableFile) throws IOException {
    return isDirAlmostEmpty(inDirname,new String[] {inIgnorableFile}); }

//--------------------------------------------------------------------------------------------------------
// listFiles
//--------------------------------------------------------------------------------------------------------

  private static void listFiles(
      Path            inDirPath,
      ArrayList       outFilePathList, 
      boolean         inRecursive, 
      FilenameFilter  inFilter) throws IOException {

    // If recursive, keep list of child dirs
    // Want to list all files in current dir before files in sub dirs
    ArrayList<Path> theChildDirList=inRecursive?new ArrayList<Path>():null;

    // List all paths in current dir
    Stream<Path> theChildPathStream=null;
    try {
      theChildPathStream=Files.list(inDirPath);
      Iterator<Path> theChildPathIterator=theChildPathStream.iterator();
      
      // Loop over paths
      while (theChildPathIterator.hasNext()) {
        Path theChildPath=theChildPathIterator.next();
        
        // Keep files that match filter (if present)
        if (Files.isRegularFile(theChildPath)) {
          if ((inFilter==null)||(inFilter.accept(null,theChildPath.getFileName().toString())))
            outFilePathList.add(theChildPath);
          
        // If recursive, keep list of child dirs
        } else if ((inRecursive)&&(Files.isDirectory(theChildPath))) 
          theChildDirList.add(theChildPath);
  
        // Ignore symlinks?  Try to avoid using symlinks 
      }
    } finally {
      theChildPathStream.close();
      theChildPathStream=null;
    }
    
    // If recursive, listFiles for child dirs 
    if (inRecursive) 
      for (int i=0; i<theChildDirList.size(); i++)
        listFiles(theChildDirList.get(i),outFilePathList,inRecursive,inFilter);
  }

  public static String[] listFiles(
      String          inDirname, 
      boolean         inRecursive,
      FilenameFilter  inFilter) throws IOException {
    
    Path theDirPath=getPath(inDirname);
    if (!doesDirExist(theDirPath))
      throw new IOException(inDirname+" is not a dir");

    ArrayList<Path> theFilePathList=new ArrayList<Path>();
    listFiles(theDirPath,theFilePathList,inRecursive,inFilter);
    
    String[] theFilenames=new String[theFilePathList.size()];
    for (int i=0; i<theFilenames.length; i++)
      theFilenames[i]=theDirPath.relativize(theFilePathList.get(i)).toString();
    SortUtils.sort(theFilenames);
    
    return theFilenames;
  }

  public static String[] listFiles(String inDirname, FilenameFilter inFilter) throws IOException {
    return listFiles(inDirname,false,inFilter); }

  public static String[] listFiles(String inDirname, boolean inRecursive) throws IOException {
    return listFiles(inDirname,inRecursive,null); }

  public static String[] listFiles(String inDirname) throws IOException {
    return listFiles(inDirname,false); }

//--------------------------------------------------------------------------------------------------------
// listDirs
//--------------------------------------------------------------------------------------------------------

  public static String[] listDirs(Path inDirPath, FilenameFilter inFilter) throws IOException {
    
    if (!doesDirExist(inDirPath))
      throw new IOException(inDirPath+" is not a dir");

    ArrayList<Path> theChildDirList=new ArrayList<Path>();
    Stream<Path> theChildPathStream=null;
    try {
      theChildPathStream=Files.list(inDirPath);
      Iterator<Path> theChildPathIterator=theChildPathStream.iterator();
      while (theChildPathIterator.hasNext()) {
        Path theChildPath=theChildPathIterator.next();
        if ((Files.isDirectory(theChildPath))&&
            ((inFilter==null)||(inFilter.accept(null,theChildPath.getFileName().toString()))))
          theChildDirList.add(theChildPath);
      }
    } finally {
      theChildPathStream.close();
      theChildPathStream=null;
    }
    
    String[] theDirnames=new String[theChildDirList.size()];
    for (int i=0; i<theDirnames.length; i++)
      theDirnames[i]=inDirPath.relativize(theChildDirList.get(i)).toString();
    SortUtils.sort(theDirnames);
    
    return theDirnames;
  }

  public static String[] listDirs(String inDirname, FilenameFilter inFilter) throws IOException {
    return listDirs(getPath(inDirname),inFilter); }

  public static String[] listDirs(String inDirname) throws IOException {
    return listDirs(inDirname,null); }

//--------------------------------------------------------------------------------------------------------
// listRootDirs
//--------------------------------------------------------------------------------------------------------

  public static String[] listRootDirs() throws IOException {
    return listDirs("/"); }

//--------------------------------------------------------------------------------------------------------
// dirSize
//--------------------------------------------------------------------------------------------------------

  public static long dirSize(Path inDirPath) throws IOException {
    
    if (!doesDirExist(inDirPath))
      return 0;

    long theDirSize=0;
    Stream<Path> theChildPathStream=null;
    try {
      theChildPathStream=Files.list(inDirPath);
      Iterator<Path> theChildPathIterator=theChildPathStream.iterator();
      while (theChildPathIterator.hasNext()) {
        Path theChildPath=theChildPathIterator.next();
        if (Files.isDirectory(theChildPath))
          theDirSize+=dirSize(theChildPath);
        else if (Files.isRegularFile(theChildPath))
          theDirSize+=getFileLength(theChildPath);
      }
    } finally {
      theChildPathStream.close();
      theChildPathStream=null;
    }
    return theDirSize;
  }

  public static long dirSize(String inDirname) throws IOException {
    return dirSize(getPath(inDirname)); }
/*
//--------------------------------------------------------------------------------------------------------
// setPosixDirPermissions
//--------------------------------------------------------------------------------------------------------

  public static void setPosixDirPermissions(Path inDirPath, Set<PosixFilePermission> inPermissions) throws IOException {
    if (kUsePosix)
      Files.setPosixFilePermissions(inDirPath,inPermissions); 
  }

  public static void setPosixFilePermissions(Path inDirPath) throws IOException {
    setPosixDirPermissions(inDirPath,kDefaultPosixDirPermissions); }

  public static void setPosixFilePermissions(String inDirname) throws IOException {
    setPosixFilePermissions(getPath(inDirname)); }
*/
//--------------------------------------------------------------------------------------------------------
// deleteDir
//--------------------------------------------------------------------------------------------------------

  public static void deleteDir(Path inDirPath) throws IOException {

    if (!doesDirExist(inDirPath))
      return;
    
    if (!doesDirExist(inDirPath))
      throw new IOException(inDirPath+" is not a dir");
    
    // Could move dir to temp dir the delete in background, but exception reporting would be obscure

    // Weird dependency alert!
    // This stream keeps the dir alive as long as it is open.
    // Cannot rely on stream finalizer closing it soon enough.
    // Must explicitly close stream before deleting dir, or delete will succeed, but not actually happen.
    // Then parent dir will attempt a delete before being empty and throw an exception.
    Stream<Path> theChildPathStream=null;
    try {
      theChildPathStream=Files.list(inDirPath);
      Iterator<Path> theChildPathIterator=theChildPathStream.iterator();
      while (theChildPathIterator.hasNext()) {
        Path theChildPath=theChildPathIterator.next();
        if (Files.isDirectory(theChildPath))
          deleteDir(theChildPath);
        else 
          deleteFile(theChildPath);
      }
      theChildPathIterator=null;
    } finally {
      theChildPathStream.close();
      theChildPathStream=null;
    }
    
    deleteFile(inDirPath);
  }

  public static void deleteDir(String inDirname) throws IOException {
    deleteDir(getPath(inDirname)); }

//--------------------------------------------------------------------------------------------------------
// makeTempDir
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
 
  public static String makeTempDir(Path inDirPath, String inFilePrefix) throws IOException {
    if (!DirUtils.doesDirExist(inDirPath))
      throw new IOException(inDirPath+" is not a dir");
    return getRealFilename(Files.createTempDirectory(inDirPath,inFilePrefix)); 
  }

  public static String makeTempDir(String inDirname, String inFilePrefix) throws IOException {
    return makeTempDir(getPath(inDirname),inFilePrefix); }
    
  public static String makeTempDir(String inDirname) throws IOException {
    return makeTempDir(inDirname,".temp"); }
  
//--------------------------------------------------------------------------------------------------------
// makeDir
//--------------------------------------------------------------------------------------------------------

  public static void makeDir(Path inDirPath, boolean inClean) throws IOException {
    
    if (doesDirExist(inDirPath)) 
      // Bail early if dir already exists and not cleaning
      if (!inClean) 
        return;
      else {
        // If cleaning, remove dir and all its content
        deleteDir(inDirPath);
      } 
    
    // Dir does not exist
    // Recurse up to create any missing parent dirs
    Path theParentPath=inDirPath.getParent();
    if ((theParentPath!=null)&&(!doesDirExist(theParentPath)))
      makeDir(theParentPath,false);  // Do not clean parents

    // Make the dir and set permissions
    try {
      Files.createDirectory(inDirPath);
    } catch (java.nio.file.FileAlreadyExistsException e) {
      // Ignore this exception - occurs when multiple threads are creating the same dir
    }
  }

  public static void makeDir(String inDirname, boolean inClean) throws IOException {
    makeDir(getPath(inDirname),inClean); }

  public static void makeDir(String inDirname) throws IOException {
    makeDir(inDirname,false); }

//--------------------------------------------------------------------------------------------------------
// moveDir
//--------------------------------------------------------------------------------------------------------

  public static void moveDir(Path inSrcDirPath, Path inDstDirPath) throws IOException {
    // Remove dst dir
    deleteDir(inDstDirPath);
    // Move src to dst dir
    Files.move(inSrcDirPath,inDstDirPath);
  }

  public static void moveDir(String inSrcDirname, String inDstDirname) throws IOException {
    moveDir(getPath(inSrcDirname),getPath(inDstDirname)); }

//--------------------------------------------------------------------------------------------------------
// copyDir
//--------------------------------------------------------------------------------------------------------

  public static void copyDir(Path inSrcDirPath, Path inDstDirPath, boolean inClean) throws IOException {
    
    if (!doesDirExist(inSrcDirPath))
      throw new IOException(inSrcDirPath+" is not a dir");

    makeDir(inDstDirPath,inClean);

    Stream<Path> theChildPathStream=null;
    try {
      theChildPathStream=Files.list(inSrcDirPath);
      Iterator<Path> theChildPathIterator=theChildPathStream.iterator();
      while (theChildPathIterator.hasNext()) {
        Path theSrcChildPath=theChildPathIterator.next();
        Path theDstChildPath=getPath(inDstDirPath+"/"+inSrcDirPath.relativize(theSrcChildPath).toString());
        
        if (Files.isDirectory(theSrcChildPath))
          copyDir(theSrcChildPath,theDstChildPath,false);
        else if (Files.isRegularFile(theSrcChildPath))
          copyBinaryFile(theSrcChildPath.toAbsolutePath().toString(),
                                   theDstChildPath.toAbsolutePath().toString());
      }
    } finally {
      theChildPathStream.close();
      theChildPathStream=null;
    }
  }

  public static void copyDir(String inSrcDirname, String inDstDirname, boolean inClean) throws IOException {
    copyDir(getPath(inSrcDirname),getPath(inDstDirname),inClean); }
  
  public static void copyDir(String inSrcDirname, String inDstDirname) throws IOException {
    copyDir(inSrcDirname,inDstDirname,false); }

}
