package com.clschina.common.jsf.renderer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.Renderer;
import javax.faces.view.facelets.FaceletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.filter.MultipartRequestWrapper;

public class FileUploadRenderer extends Renderer {
	private static Log log = LogFactory.getLog(FileUploadRenderer.class);

	public FileUploadRenderer() {
		if (log.isTraceEnabled()) {
			log.trace("creating FileUploadRenderer.");
		}
	}

	/**
	 * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent)
	 */
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("encodeBegin with " + component.getClientId());
		}
		String id = component.getClientId(context);
		ResponseWriter writer = context.getResponseWriter();
		String styleClass = (String) get(context, component, "styleClass");
		if (styleClass == null) {
			styleClass = "fileupload";
		}
		writer.startElement("input", component);
		writer.writeAttribute("class", styleClass, null);
		writer.writeAttribute("type", "file", null);
		writer.writeAttribute("id", id, null);
		writer.writeAttribute("name", id, null);
		if(get(context, component, "size") != null){
			writer.writeAttribute("size", get(context, component, "size"), null);
		}
		if(get(context, component, "style") != null){
			writer.writeAttribute("style", get(context, component, "style"), null);
		}
		writer.endElement("input");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext,
	 * javax.faces.component.UIComponent)
	 */
	@Override
	public void decode(FacesContext context, UIComponent component) {
		if (log.isTraceEnabled()) {
			log.trace("enter FileUploadRenderer.decode()");
		}
		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			if (log.isInfoEnabled()) {
				log
						.info("fileUpload component must inside a form with enctype=\"multipart/form-data\" attribute.");
			}
			throw new FaceletException(
					"fileUpload component must inside a form with enctype=\"multipart/form-data\" attribute.");
		}
		MultipartRequestWrapper mr = null;
		if (request instanceof MultipartRequestWrapper && request != null) {
			mr = (MultipartRequestWrapper) request;
		} else {
			if (log.isWarnEnabled()) {
				log.warn("request isnot MultipartRequestWrapper");
			}
			throw new FaceletException(
					"configuration error in web.xml, no MultipartRequestWrapper found.");
		}
		String clientId = component.getClientId(context);
		FileItem fileItem = mr.getFileItem(clientId);
		if (log.isTraceEnabled()) {
			log.trace("clientId=" + clientId + "; file="
					+ (fileItem == null ? "NULL" : fileItem.getName() + " size=" + fileItem.getSize()));
		}
		if(fileItem != null){
			if(fileItem.getSize() == 0){
				fileItem = null;
			}
		}
		if (log.isTraceEnabled()) {
			if(fileItem == null){
				log.trace("fileItem is null for " + clientId);
			}else{
				log.trace("fileItem is " + fileItem.getName() + 
						" contenttype=" + fileItem.getContentType() + 
						" size=" + fileItem.getSize() + 
						" string=" + fileItem.getString() + " for " + clientId);
				
			}
		}
		UIInput input = (UIInput) component;
		input.setSubmittedValue(fileItem == null ? new BlankFileItem() : fileItem);

		if(log.isTraceEnabled()){
			log.trace("valid=" + input.isValid() + "; value=" + input.getValue() + "; required=" + input.isRequired());
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.render.Renderer#getConvertedValue(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
	 */
	@Override
	public Object getConvertedValue(FacesContext context,
			UIComponent component, Object submittedValue)
			throws ConverterException {
		if(submittedValue instanceof BlankFileItem){
			return null;
		}else{
			return (FileItem) submittedValue;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext,
	 * javax.faces.component.UIComponent)
	 */
	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

	}

	private static Object get(FacesContext context, UIComponent component,
			String name) {
		ValueExpression binding = component.getValueExpression(name);
		if (binding != null) {
			return binding.getValue(context.getELContext());
		} else {
			return component.getAttributes().get(name);
		}
	}


	/**
	 * 仅仅用做标记是空文件条目，任何方法都无需实现。
	 * @author GeXiangDong
	 *
	 */
	class BlankFileItem implements FileItem{
		private static final long serialVersionUID = -1917299469947113786L;

		public void delete() {
			
		}

		public byte[] get() {
			return null;
		}

		public String getContentType() {
			return null;
		}

		public String getFieldName() {
			return null;
		}

		public InputStream getInputStream() throws IOException {
			return null;
		}

		public String getName() {
			return null;
		}

		public OutputStream getOutputStream() throws IOException {
			return null;
		}

		public long getSize() {
			return 0;
		}

		public String getString() {
			return null;
		}

		public String getString(String arg0)
				throws UnsupportedEncodingException {
			return null;
		}

		public boolean isFormField() {
			return false;
		}

		public boolean isInMemory() {
			return false;
		}

		public void setFieldName(String arg0) {
			
		}

		public void setFormField(boolean arg0) {
			
		}

		public void write(File arg0) throws Exception {
			
		}
		
	}

}
