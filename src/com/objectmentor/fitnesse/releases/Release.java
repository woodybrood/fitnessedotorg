package com.objectmentor.fitnesse.releases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.FileUtil;

public class Release {
  public static final String ENDL = System.getProperty("line.separator");
  private static final File releaseHome = new File("releases");

  private final File releaseDir;
  private final Map<String, ReleaseFile> releaseFiles;
  private final File infoFile;
  private final String name;

  public Release(String name) throws IOException {
    this.name = name;
    releaseDir = new File(releaseHome, name);
    infoFile = new File(releaseDir, ".releaseInfo");
    releaseFiles = new HashMap<String, ReleaseFile>(4);
    if (exists())
      load();
  }

  public String getName() {
    return name;
  }

  public boolean exists() {
    return releaseDir.exists();
  }

  private void load() throws IOException {
    loadRecordedFiles();
    loadLocalFiles();
  }

  private void loadLocalFiles() {
    File[] files = releaseDir.listFiles();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      String filename = file.getName();
      if (!releaseFiles.containsKey(filename) && !filename.startsWith(".") &&
        !file.isDirectory()) {
        releaseFiles.put(filename, new ReleaseFile(file.getAbsolutePath()));
      }
    }
  }

  private void loadRecordedFiles() throws IOException {
    if (infoFile.exists()) {
      String info = FileUtil.getFileContent(infoFile);
      String[] rows = info.split("\n");
      for (int i = 0; i < rows.length; i++) {
        ReleaseFile releaseFile = ReleaseFile.parse(
          releaseDir.getAbsolutePath(), rows[i]
        );
        if (releaseFile.exists())
          releaseFiles.put(releaseFile.getFilename(), releaseFile);
      }
    }
  }

  public int fileCount() {
    return releaseFiles.size();
  }

  public void saveInfo()  {
    FileWriter writer = null;
	try {
		writer = new FileWriter(infoFile);
		for (Iterator iterator = getFiles().iterator(); iterator.hasNext();)
		  writer.write(iterator.next().toString() + "\n");
		writer.flush();
		writer.close();
		writer = null;
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	finally{
		// Handle the case where something failed that you *didn't* catch
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e2) {
            }
        }
	}
  }

  public List<ReleaseFile> getFiles() {
    LinkedList<ReleaseFile> files = new LinkedList<ReleaseFile>(releaseFiles.values());
    Collections.sort(files);
    return files;
  }

  public ReleaseFile getFile(String filename) {
    return releaseFiles.get(filename);
  }

  public boolean isCorrupted()  {
    try {
		if (infoFile == null)
		  return true;
		else if (!infoFile.exists())
		  return false;

        String fileContent = FileUtil.getFileContent(infoFile);
        if (fileContent.equals("") || (fileContent).equals(ENDL))
		  return true;
	} catch (IOException e) {
		return false;
	}

    return false;
  }
}
