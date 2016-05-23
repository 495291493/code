package com.clschina.common.util.mail;


public class FileEntry{
	private String fileName;
	private String filePath;
	
	public FileEntry(String fname, String fpath){
		this.fileName = fname;
		this.filePath = fpath;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}
}