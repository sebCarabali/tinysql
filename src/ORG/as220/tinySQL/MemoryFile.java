package ORG.as220.tinySQL;

import ORG.as220.tinySQL.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: bharath
 * Date: 2/23/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class MemoryFile {
  // A mem file is simply a string stored in memory
  // TODO(bharath): Is this sufficient or do we need a ByteString?
  private static HashMap<String, Vector<Byte>> memFiles = new HashMap<String, Vector<Byte>>();
  private static HashMap<String, String> filePermissions = new HashMap<String, String>();
  private int location = 0;

  private String myFilename = new String("");
  private Vector<Byte> myFileContents = new Vector<Byte>();
  private String myPermissions = "";

  public MemoryFile(String filename, String permissions) throws FileNotFoundException {

    this.myFilename = filename;
    this.location = 0;

    if (!memFiles.containsKey(myFilename)) {
      if (permissions.equals("r")) {
        throw new FileNotFoundException();
      }

      if (permissions.contains("rw")) {
       // System.out.println("Creating non existent file" + myFilename);
        memFiles.put(myFilename, new Vector<Byte>());
      }

    } else {
      myFileContents = memFiles.get(myFilename);
      myPermissions = filePermissions.get(myFilename);
    }

  }

  public void close() throws IOException {
    checkFileOpen();
    memFiles.put(myFilename, myFileContents);
    filePermissions.put(myFilename, myPermissions);
  }

  public void seek(int location) throws IOException {
    checkFileOpen();

    if (location < 0) {
      throw new IOException("Seek position less than 0");
    }

    if (location >= myFileContents.size()) {
      this.location = myFileContents.size();
    } else {
      this.location = location;
    }
  }

  private void checkFileOpen() throws IOException {
    if (myFilename.isEmpty()) throw new IOException("File not open");
  }

  public void write(byte[] ovalue) throws IOException {
    checkFileOpen();
    int i = 0;

    while (location < myFileContents.size() && i < ovalue.length) {
      myFileContents.set(location, ovalue[i]);
      ++location;
      ++i;
    }

    if (i < ovalue.length) {
      // Still have more to write
      while (i < ovalue.length) {
        myFileContents.add(ovalue[i]);
        location++;
        i++;
      }
    }
  }

  public void readFully(byte[] line) throws IOException {
    checkFileOpen();

    if (location + line.length > myFileContents.size()) {
      Byte[] bytes = new Byte[line.length];
      myFileContents.subList(location, myFileContents.size()).toArray(bytes);
      for (int i = 0; i < bytes.length; ++i) {
        line[i]  = bytes[i];
      }

     throw new EOFException();
    }

    Byte[] bytes = new Byte[line.length];
    myFileContents.subList(location, location + line.length).toArray(bytes);
    for (int i = 0; i < line.length; ++i) {
      line[i] = bytes[i];
    }
  }

  public int length() throws IOException {
    checkFileOpen();

    return memFiles.get(myFilename).size();
  }

  public static InputStream getInputStream(String filename) throws FileNotFoundException {
    if (memFiles.containsKey(filename)) {
      Byte[] bytes = new Byte[memFiles.get(filename).size()];
      memFiles.get(filename).toArray(bytes);

      byte[] primitive = new byte[bytes.length];
      for (int i = 0 ; i < bytes.length; ++i) {
        primitive[i] = bytes[i];
      }
      return new ByteArrayInputStream(primitive);
    } else {
      throw new FileNotFoundException(filename);
    }
  }

  public static void delFile(String myFilename) {
    if (memFiles.containsKey(myFilename)) {
      memFiles.remove(myFilename);
    } else {
      Log.debug("File: " + myFilename + " does not exist. No action taken on delete.");
    }
  }

  public static boolean renameFile(String filename, String newFileName) {
    if (!memFiles.containsKey(filename)) {
      Log.warn("File " + filename + " does not exist");
      return false;
    }

    if (memFiles.containsKey(newFileName)) {
      Log.warn("File " + newFileName + " exists");
      return false;
    }

    memFiles.put(newFileName, memFiles.get(filename));
    memFiles.remove(filename);
    return true;
  }
}
