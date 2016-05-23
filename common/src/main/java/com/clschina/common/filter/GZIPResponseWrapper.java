package com.clschina.common.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GZIPResponseWrapper extends HttpServletResponseWrapper {
	private static Log log = LogFactory.getLog(GZIPResponseWrapper.class);
	protected HttpServletResponse origResponse = null;
	protected ServletOutputStream stream = null;
	protected PrintWriter writer = null;

	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}

	public ServletOutputStream createOutputStream() throws IOException {
		String contentType = origResponse.getContentType();
		if(contentType != null && (contentType.toLowerCase().indexOf("zip") > 0 
				|| contentType.toLowerCase().indexOf("pdf") > 0)){
			//已经是zip了，不再压了
			return origResponse.getOutputStream();
		}else{
			return (new GZIPResponseStream(origResponse));
		}
	}

	public void finishResponse() {
		if (log.isTraceEnabled()) {
			log.trace("finishResponse()");
		}
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null) {
					if(stream instanceof GZIPResponseStream){
						GZIPResponseStream zStream = (GZIPResponseStream) stream;
						if (!zStream.closed()) {
							zStream.close();
						}
					}else{
						stream.close();
					}
				}
			}
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("error occured while finisheResponse.", e);
			}
		}
	}

	public void flushBuffer() throws IOException {
		if(stream != null){
			stream.flush();
		}
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("getOutputStream()");
		}
		if (writer != null) {
			throw new IllegalStateException(
					"getWriter() has already been called!");
		}

		if (stream == null) {
			stream = createOutputStream();
		}
		return (stream);
	}

	public PrintWriter getWriter() throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("getWriter()");
		}
		if (writer != null) {
			return (writer);
		}

		if (stream != null) {
			throw new IllegalStateException(
					"getOutputStream() has already been called!");
		}

		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
		return (writer);
	}

	public void setContentLength(int length) {
		if(stream instanceof GZIPResponseStream){
			
		}else{
			origResponse.setContentLength(length);
		}
	}
}
