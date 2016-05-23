package com.clschina.common.jsf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.ClassPath;



public class ExecuteActionPhaseListener implements PhaseListener {
	private Properties actions = null;
	private static final long serialVersionUID = 3815482146278626861L;
	private static Log log = LogFactory
			.getLog(ExecuteActionPhaseListener.class);
	private boolean webPropertiesAdded = false;
	
	public ExecuteActionPhaseListener() {
		if(log.isTraceEnabled()){
			log.trace("ExecuteActionPhaseListener constructor called.");
		}
		// 读出所有的PageController.xml
		URL[] configs = null;
		actions = new Properties();
		try {
			configs = ClassPath.search(Thread.currentThread()
					.getContextClassLoader(),
					"META-INF/config/page-action.properties", "");
			if (log.isTraceEnabled()) {
				log.trace("found " + configs.length
						+ " resource(s) with prefix '"
						+ "META-INF/config/page-action.properties");
			}
			for (int i = 0; i < configs.length; i++) {
				try {
					addConfigurationFromInputStream(configs[i].openStream());
				} catch (Exception e) {
					log.error("在处理配置文件" + configs[i] + "时遇到错误，将跳过此文件。", e);
				}
			}
			
		} catch (Exception e) {
			log.error("Error occured while searching "
					+ "page-controller files.", e);
			throw new NullPointerException(
					"Error occured while searching page-controller files.");
		}
	}

	private void addConfigurationFromInputStream(InputStream is) {
		try {
			Properties prop = new Properties();
			prop.load(is);
			actions.putAll(prop);
		} catch (IOException e) {
			if(log.isErrorEnabled()){
			    log.error("Error occured.", e);
			}
		}
	}

	public void beforePhase(PhaseEvent event) {
		if(log.isTraceEnabled()){
			log.trace("before " + event.getPhaseId().toString() + " phase.");
		}
	}

	public void afterPhase(PhaseEvent event) {
		if (log.isTraceEnabled()) {
			log.trace("afterPhaseEvent phaseId="
					+ event.getPhaseId().toString() + " ");
		}
		if (actions == null) {
			if (log.isTraceEnabled()) {
				log.debug("no actions was registered. ");
			}
			return;
		}
		if (event.getPhaseId() != PhaseId.RESTORE_VIEW) {
			if (log.isTraceEnabled()) {
				log.trace("not RESTORE_VIEW Phase, only trigger this "
						+ "event at the before restore_view phase");
			}
			return;
		}
		FacesContext facesContext = event.getFacesContext();
		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		if(!webPropertiesAdded){
			webPropertiesAdded = true;
			try{
				InputStream is = facesContext.getExternalContext().getResourceAsStream("/WEB-INF/page-action.properties");
				if(is != null){
					addConfigurationFromInputStream(is);
				}
			}catch(Exception e){
				if(log.isErrorEnabled()){
					log.error("Error while reading /WEB-INF/page-action.properties", e);
				}
			}
		}
		String servletPath = request.getServletPath();
		String action = actions.getProperty(servletPath);
		try{
			if(action != null){
				ELContext elContext = facesContext.getELContext();
		        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();      
		        MethodExpression me = expressionFactory.createMethodExpression(elContext, action, String.class, new Class[]{});
		        Object r = null;
		        try{
		        	r = me.invoke(elContext, new Object[]{});
		        }catch(Exception e){
		        	log.error("执行" + action + "时发生错误。" + e, e);
		        	
		        	HttpServletResponse resp = (HttpServletResponse)facesContext.getExternalContext().getResponse();
		        	resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        	resp.getWriter().write("出错了");
		        	facesContext.renderResponse();
		        	facesContext.responseComplete();
		        }
		        if(r != null && r instanceof String) {
					// 如果返回String,则当作action
					Application application = facesContext.getApplication();
					NavigationHandler navigationHandler = application
							.getNavigationHandler();
					navigationHandler.handleNavigation(facesContext, servletPath, (String) r);
					// Render Response if needed
					facesContext.renderResponse();
				}
			}
		} catch (Exception e) {
			if(log.isErrorEnabled()){
				log.error("afterPhase:: Error while search page method and invoke it.", e);
			}
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}


}
