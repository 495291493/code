package com.clschina.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipException;
//import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 用于解压缩zip文件
 * @author acer
 *
 */
public class ZipUtil {
	File zipFile;
	ZipFile archive;
	private static Log log = LogFactory.getLog(ZipUtil.class);
	
	public ZipUtil(File file) throws  IOException {
		this.zipFile = file;
		archive = new ZipFile(zipFile);
	}

	public ZipUtil(String fileName) throws IOException {
		this(new File(fileName));
	}

	public void unzipTo(File folder) throws IOException {
		unzipTo(folder.getPath());
	}

	public void unzipTo(String folder) throws IOException {
		// do our own buffering; reuse the same buffer.
		String toFolder;
		if (folder.endsWith(File.separator)) {
			toFolder = folder;
		} else {
			toFolder = folder + File.separator;
		}

		// Loop through each Zip file entry
		for (Enumeration<?> e = archive.entries(); e.hasMoreElements();) {
			// get the next entry in the archive
			ZipEntry entry = (ZipEntry) e.nextElement();

			if (!entry.isDirectory()) {
				saveOneFile(entry, toFolder);
			}
		}
	}
	private void saveOneFile(ZipEntry entry, String toFolder ) throws  IOException {
		byte[] buffer = new byte[16384];
		// Set up the name and location of where the file will be
		// extracted to
		String filename = entry.getName();
		filename = filename.replace('/', File.separatorChar);
		filename = toFolder + filename;
		File destFile = new File(filename);

		String parent = destFile.getParent();
		if (parent != null) {
			File parentFile = new File(parent);
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		}

		// get a stream of the archive entry's bytes
		InputStream in = archive.getInputStream(entry);

		// open a stream to the destination file
		OutputStream outStream = new FileOutputStream(filename);

		// repeat reading into buffer and writing buffer to file,
		// until done. count will always be # bytes read, until
		// EOF when it is -1.
		int count;
		if(in != null){
			while ((count = in.read(buffer)) != -1) {
				outStream.write(buffer, 0, count);
			}
		}else{
			log.warn("error occure while readding entry " + entry);
			throw new NullPointerException();
		}
		in.close();
		outStream.close();
		
	}

	public void extractFile(String fileName, String folder) throws IOException{
		ZipEntry entry = archive.getEntry(fileName);
		saveOneFile(entry, folder);
	}
	
	public static void unzip(File zipFile, File destFolder)
		throws  IOException {
		ZipUtil zuu = new ZipUtil(zipFile);
		zuu.unzipTo(destFolder);
	}
	public static void unzip(String zipFile, String destFolder)
			throws IOException {
		ZipUtil zuu = new ZipUtil(zipFile);
		zuu.unzipTo(destFolder);
	}

}
