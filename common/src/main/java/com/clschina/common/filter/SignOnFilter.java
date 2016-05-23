package com.clschina.common.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.component.Login;
import com.clschina.common.component.LoginManager;
import com.clschina.common.component.ThreadLocalManager;


public class SignOnFilter implements Filter {
	private static Log log = LogFactory.getLog(SignOnFilter.class);
	private static final String HTACCESSFILE = "signon.properties";
	private LoginManager loginManager;
	private HashMap<String, Properties> cache = new HashMap<String, Properties>();
	private String rootFolder;
	
	public void destroy() {
		loginManager = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		Login user = ThreadLocalManager.getLogin();
		if(user == null){
			if(loginManager != null){
				Cookie[] cookies = req.getCookies();
				if(cookies != null){
					user = loginManager.createLoginFromCookies(cookies);
				}
				if(user != null){
					ThreadLocalManager.setLogin(user);
				}
			}
		}
		
		String servletPath = req.getServletPath();
		if(log.isTraceEnabled()){
			log.trace("request PathInfo is '" + req.getPathInfo() + "'");
			log.trace("request getServletPath is '" + req.getServletPath() + "'");
		}

		String requiredRoles = null;
		String signOnURL = null;
		File f = new File(rootFolder, servletPath).getAbsoluteFile();
		while(requiredRoles == null || signOnURL == null){
			String fileName = null;
			if(f.isFile()){
				//访问的是文件时，从文件所在目录开始计算。
				fileName = f.getName();
				f = f.getParentFile().getAbsoluteFile();
			}
			Properties prop  = getPropertiesByURI(f.getPath());
			if(log.isTraceEnabled()){
				log.trace(servletPath + " finding config file in " + f.getPath());
			}
			if(prop != null){
				if(fileName != null){
					//文件权限优先
					if(requiredRoles == null){
						String v = prop.getProperty(fileName + ".roles");
						if(v != null){
							requiredRoles = v;
						}
					}
					if(signOnURL == null){
						String v =  prop.getProperty(fileName + ".signonurl");
						if(v != null){
							signOnURL = v;
						}
					}
				}
				//目录权限
				if(requiredRoles == null){
					String v = prop.getProperty("all.roles");
					if(v != null){
						requiredRoles = v;
					}
				}
				if(signOnURL == null){
					String v =  prop.getProperty("all.signonurl");
					if(v != null){
						signOnURL = v;
					}
				}
			}
			f = f.getParentFile().getAbsoluteFile();
			if(f == null){
				break;
			}
			if((f.getPath().length() + 1) < rootFolder.length()){
				//加1，是因为少最后一个斜杠
				if(log.isTraceEnabled()){
					log.trace(" check signon.properties break on (" + f.getPath() + " vs " + rootFolder);
				}
				break;
			}
		}
		
		if(log.isTraceEnabled()){
			log.trace("URI: " + req.getRequestURI() + "; roles=" + requiredRoles + ";  signOnUrl=" + signOnURL);
			log.trace("user [" + (user == null ? "null" : user.getId()) + "] privileges " + (user == null ? "" : user.getPrivileges().toString()));
		}
		boolean allowed = false;
		if(requiredRoles != null){
			String[] roles = requiredRoles.split(",");
			List<String> userRoles;
			if(user != null){
				userRoles = user.getPrivileges();
			}else{
				userRoles = new ArrayList<String>();
			}
			userRoles.add("guest");
			userRoles.add("visitor");
			for(int i=0; i<userRoles.size(); i++){
				for(int j=0; j<roles.length; j++){
					if(roles[j].trim().equalsIgnoreCase(userRoles.get(i))){
						allowed = true;
						break;
					}
				}
				if(allowed){
					break;
				}
			}
		}else{
			allowed = true;
		}
		if(log.isTraceEnabled()){
			log.trace("check privilege " + allowed);
		}
		if(allowed){
			chain.doFilter(req, response);
		}else{
			if(signOnURL != null){
				String url = req.getServletPath().toString();
				if(!url.endsWith(signOnURL)){
					String qs = req.getQueryString();
					if(qs != null){
						url += "?" + qs;
					}
					req.setAttribute("url", url);
				}
				req.getRequestDispatcher(signOnURL).forward(req, response);
			}else{
				if(log.isInfoEnabled()){
					log.info("no signon url for " + req.getRequestURI());
				}
				HttpServletResponse resp = (HttpServletResponse) response;
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				resp.setContentType("text/plain;charset=UTF-8");
				PrintWriter out = resp.getWriter();
				out.write("access deny");
			}
		}
	}


	private Properties getPropertiesByURI(String path){
		if(cache.containsKey(path)){
			return cache.get(path);
		}
		File f = new File(path);
		Properties prop = null;
		if(f.exists() && f.isDirectory()){
			File configFile = new File(f, HTACCESSFILE);
			if(configFile.exists() && configFile.isFile()){
				try{
					FileInputStream fis = new FileInputStream(configFile);
					prop = new Properties();
					prop.load(fis);
					fis.close();
				}catch(Exception e){
					if(log.isErrorEnabled()){
						log.error("ERROR while loading properties from " + configFile.getPath(), e);
					}
				}
			}
		}
		if(log.isTraceEnabled()){
			log.trace("SignOn config " + path + " {" + prop + "}");
		}
		cache.put(path, prop);
		return prop;
	}

	public void init(FilterConfig config) throws ServletException {
		String cls = config.getInitParameter("loginmanager");
		rootFolder = config.getServletContext().getRealPath("/");
		if(cls != null){
			cls = cls.trim();
			if(log.isTraceEnabled()){
				log.trace("parameter loginmanager is '" + cls + "'");
			}
			try{
				Object o = Class.forName(cls).newInstance();
				if (o == null || !(o instanceof LoginManager)) {
					log.warn("cannot instance " + cls + " for it is NOT implements the LoginManager interface.");
				}else{
					loginManager = (LoginManager) o;
				}
			}catch(Exception e){
				if(log.isErrorEnabled()){
					log.error("Error instance '" + cls + "'", e);
				}
			}
		}
	}

}
