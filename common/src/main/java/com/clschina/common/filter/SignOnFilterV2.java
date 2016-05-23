package com.clschina.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.component.LoginBase;
import com.clschina.common.component.LoginPageFace;
import com.clschina.common.component.ThreadLocalManager;


public class SignOnFilterV2 implements Filter {
	private static Log log = LogFactory.getLog(SignOnFilterV2.class);
	private String signOnUrl = "/login.xhtml";
	private String accessdenyUrl = "/accessdeny.xhtml";
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		LoginBase user = (LoginBase)ThreadLocalManager.getLogin();
		
		if(user == null){
			req.getRequestDispatcher(signOnUrl).forward(req, response);
			return;
		}

		String uri = req.getRequestURI();
		if (log.isTraceEnabled()) {
			log.trace("request PathInfo is '" + req.getPathInfo() + "'");
			log.trace("request getServletPath is '" + req.getServletPath() + "'");
			log.trace("request getRequestURI is '" + uri + "'");
		}

		//查询受访问页面所需要的权限
		List<String> priviList = new ArrayList<String>();
		for(LoginPageFace p : user.getAllLoginPage()){
			if(p.getPageUrl() != null && p.getPageUrl().startsWith(uri)){
				priviList.add(p.getPrivilege());
			}
		}
		
		if(priviList.isEmpty()){
			//受访问页面未设置权限,直接访问
			chain.doFilter(req, response);
		}else{
			boolean allowed = false;
			for(String pv : user.getPrivileges()){
				if(priviList.contains(pv)){
					allowed = true;
					break;
				}
			}
			if(allowed){
				//有权限
				chain.doFilter(req, response);
			}else{
				//没有权限
				req.getRequestDispatcher(accessdenyUrl).forward(req, response);	
			}
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
