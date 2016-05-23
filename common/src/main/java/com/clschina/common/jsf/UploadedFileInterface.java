package com.clschina.common.jsf;

import java.io.InputStream;
import java.io.Serializable;



public interface UploadedFileInterface extends Serializable {
	
	/**
	 * 文件名，不包含文件名中路径部分
	 * @return
	 */
	public String getName();
	
	/**
	 * 取得文件的输入流
	 * @return
	 */
	public InputStream getInputStream();
	
	/**
	 * 到此文件的链接URL
	 * @return
	 */
	public String getUrl();
}
