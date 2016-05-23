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

public class CacheFilter implements Filter {
	private static Log log = LogFactory.getLog(CacheFilter.class);

	private Integer cacheTime = 3600 * 24;

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(req, resp);

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		if (log.isTraceEnabled()) {
			log.trace("doFilter for " + request.getRequestURI());
		}
		response.setHeader("Cache-Control", "public, max-age=" + cacheTime);
		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("set cache control for uri=").append(
					request.getRequestURI());
			sb.append(" and the cache time is ").append(cacheTime).append(
					"second");
			log.debug(sb.toString());
		}

	}

	public void init(FilterConfig config) throws ServletException {
		if (config != null) {
			String ct = config.getInitParameter("cache-time");
			if (!"".equals(ct) && null != ct) {
				cacheTime = new Integer(ct);
				if (log.isInfoEnabled()) {
					log.info(">>>>>>>>>> the cache time is " + cacheTime);
				}
			}
		}
	}


}
