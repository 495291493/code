package com.clschina.common.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 处理包含上传文件的HttpRequest。
 *
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {
	private static Log log = LogFactory.getLog(MultipartRequestWrapper.class);
	private Map<String, List<FileItem>> uploads;
	private Map<String, List<String>> fields;
	private HttpServletRequest request;
	//不允许上传的文件格式的列表
	final String[] denyExts = new String[] { ".jsp", ".php", ".cgi", ".asp", ".exe",
			".bin", ".rb", ".pl", ".py" };
	
	/**
	 * 构造函数
	 * @param request
	 */
	public MultipartRequestWrapper(HttpServletRequest request, String rootFolder) throws ServletException{
		super(request);
		this.request = request;
		
		if(log.isTraceEnabled()){
			log.trace("MultipartRequestWrapper()");
		}
		uploads = new HashMap<String, List<FileItem>>();
		fields = new HashMap<String, List<String>>();
		if(ServletFileUpload.isMultipartContent(request)){
			// 实例化一个硬盘文件工厂,用来配置上传组件ServletFileUpload
			DiskFileItemFactory dfif = new DiskFileItemFactory();
			// 设置上传文件时用于临时存放文件的内存大小,这里是2M.多于的部分将临时存在硬盘
			dfif.setSizeThreshold(2 * 1024 * 1024);
			File tmp = new File(rootFolder, "files/temp/tmp");
			if(!tmp.exists()){
				tmp.mkdirs();
			}
			dfif.setRepository(tmp);	//临时文件存放位置

			// 用以上工厂实例化上传组件
			ServletFileUpload sfu = new ServletFileUpload(dfif);
			sfu.setHeaderEncoding("UTF-8");
			// 设置最大上传尺寸
			sfu.setSizeMax(100 * 1024 * 1024);
			List<?> fileList = null;
			try {
				fileList = sfu.parseRequest(request);
			} catch (FileUploadException e) {// 处理文件尺寸过大异常
				if (e instanceof SizeLimitExceededException) {
					if(log.isWarnEnabled()){
						log.warn("too larget. max is " + sfu.getSizeMax() + ".", e);
					}
					throw new ServletException("file too large");
				}
			}
			// 没有文件上传
			if (fileList == null || fileList.size() == 0) {
				if(log.isTraceEnabled()){
					log.trace("no file found.");
				}
				return;
			}
			// 得到所有上传的文件
			Iterator<?> fileItr = fileList.iterator();
			
			while (fileItr.hasNext()) {
				FileItem fileItem = null;
				String path = null;
				long size = 0;
				// 得到当前文件
				fileItem = (FileItem) fileItr.next();
				
				// 忽略简单form字段而不是上传域的文件域(<input type="text" />等)
				if (fileItem == null){
					continue;
				}
				if(fileItem.isFormField()){
					//表单字段
					String v = null;
					try {
						v = fileItem.getString("UTF-8");
					} catch (UnsupportedEncodingException e) {
						v = fileItem.getString();
					}
					List<String> list = fields.get(fileItem.getFieldName());
					if(list == null){
						list = new ArrayList<String>();
					}
					list.add(v);
					fields.put(fileItem.getFieldName(), list);

					if(log.isTraceEnabled()){
						log.trace("[" + fileItem.getFieldName() + "],[" + fileItem.getString() + "] is a form field.");
					}
				}else{
					//文件上传
					// 得到文件的完整路径
					path = fileItem.getName();
					if(log.isTraceEnabled()){
						log.trace("文件上传 [" + fileItem.getFieldName() + "],[" + fileItem.getName() + "]");
					}
					// 得到文件的大小
					size = fileItem.getSize();
					if (path == null || path.length() == 0 || size == 0) {
						log.warn("no file found in " + fileItem.getName());
						continue;
					}
					
					for(int i=0; i<denyExts.length; i++){
						String lowerPath = path.toLowerCase();
						if(lowerPath.endsWith(denyExts[i])){
							//禁止上传的类型
							if(log.isInfoEnabled()){
								log.info(fileItem.getFieldName() + " was refused.");
							}
							continue;
						}
					}
					List<FileItem> fileItemList = uploads.get(fileItem.getFieldName());
					if(fileItemList == null){
						fileItemList = new ArrayList<FileItem>();
					}
					fileItemList.add(fileItem);
					uploads.put(fileItem.getFieldName(), fileItemList);
				}
			}			
			
		}
	}
	
	/**
	 * 取得一个上传的文件。
	 * @param key
	 * @return
	 */
	public FileItem getFileItem(String key){
		List<FileItem> list = uploads.get(key);
		if(list == null || list.size() == 0){
			return null;
		}else{
			return list.get(0);
		}
	}
	public FileItem[] getFileItems(String key){
		List<FileItem> list = uploads.get(key);
		if(list == null || list.size() == 0){
			return null;
		}
		return (FileItem[]) list.toArray(new FileItem[0]);
	}
	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		return request.getAttribute(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration<?> getAttributeNames() {
		return request.getAttributeNames();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return request.getAuthType();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		return request.getContentLength();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		return request.getContentType();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		return request.getContextPath();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		return request.getCookies();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		return request.getDateHeader(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		return request.getHeader(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration<?> getHeaderNames() {
		return request.getHeaderNames();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration<?> getHeaders(String arg0) {
		return request.getHeaders(arg0);
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) {
		return request.getIntHeader(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		return request.getLocalAddr();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		return request.getLocale();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration<?> getLocales() {
		return request.getLocales();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		return request.getLocalName();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		return request.getLocalPort();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return request.getMethod();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String param) {
		if(log.isTraceEnabled()){
			log.trace("request.getParameter('" + param + "')=" + request.getParameter(param));
			log.trace("fields.get('" + param + "')=" + fields.get(param));
		}
		List<String> list = fields.get(param);
		return (list == null ? null : list.get(0));
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map<?, ?> getParameterMap() {
		if(log.isTraceEnabled()){
			log.trace("getParameterMap()=" + request.getParameterMap());
			log.trace("fields=" + fields);
		}
		return fields;
		//return request.getParameterMap();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration<?> getParameterNames() {
		return request.getParameterNames();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String parm) {
		List<String> list = fields.get(parm);
		return (list == null ? null : (String[]) list.toArray(new String[0]));
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		return request.getPathInfo();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		return request.getPathTranslated();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return request.getProtocol();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		return request.getQueryString();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		return request.getReader();
	}

	/**
	 * @param arg0
	 * @return
	 * @deprecated
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		return request.getRealPath(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		return request.getRemotePort();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return request.getRequestDispatcher(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		return request.getRequestURI();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		return request.getRequestURL();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return request.getScheme();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		return request.getServerName();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return request.getServerPort();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return request.getServletPath();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return request.getSession();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		return request.getSession(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return request.isRequestedSessionIdFromCookie();
	}

	/**
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return request.isRequestedSessionIdFromUrl();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		return request.isRequestedSessionIdFromURL();
	}

	/**
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return request.isSecure();
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		return request.isUserInRole(arg0);
	}

	/**
	 * @param arg0
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		request.removeAttribute(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		request.setAttribute(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @throws UnsupportedEncodingException
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding(arg0);
	}
}
