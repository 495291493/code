package com.clschina.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GZIPFilter implements Filter {
	private static Log log = LogFactory.getLog(GZIPFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			String ae = request.getHeader("accept-encoding");
			String url = request.getRequestURL().toString().toLowerCase();
			if(url.contains(".gif") || url.contains(".jpg") || url.contains(".jpeg") || 
					url.contains(".png") || url.contains(".zip") || url.contains(".rar") || 
					url.contains(".gz")){
				//已经是压缩格式的文件
				ae = null;
			}
			if (ae != null && ae.indexOf("gzip") != -1) {
				if(log.isTraceEnabled()){
					log.trace("客户端支持GZIP, 启用压缩。");
				}
				GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(
						response);
				chain.doFilter(req, wrappedResponse);
				wrappedResponse.finishResponse();
				return;
			}
			chain.doFilter(req, res);
		}
	}

	public void init(FilterConfig filterConfig) {
		// noop
	}

	public void destroy() {
		// noop
	}
}
