package com.clschina.common.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.BeanELResolver;
import javax.el.ELResolver;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 一些常用的和JSF相关的函数，例如取得参数，取backingbean等
 */

public class ContextUtil {

	private static Log log = LogFactory.getLog(ContextUtil.class);

	private static ServletContext M_servletContext = null;

	/**
	 * Determine if we have been passed a parameter ending in the param string,
	 * else null. We are doing an endsWith test, since the default JSF renderer
	 * embeds the parent identity in the HTML id string; we look for the id that
	 * was specified in the JSF.
	 * 
	 * 
	 * @param lookup
	 *            JSF id String
	 * @return String the full parameter
	 */
	public static String lookupParam(String lookup) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<?,?> requestParams = context.getExternalContext()
				.getRequestParameterMap();

		Iterator<?> iter = requestParams.keySet().iterator();
		while (iter.hasNext()) {
			String currKey = (String) iter.next();
			if (currKey.endsWith(lookup)) {
				return (String) requestParams.get(currKey);
			}
		}
		return null;
	}

	/**
	 * 查找某个参数的值。如果有多个，返回数组，没有返回零长度数组。
	 * 
	 * @param name
	 * @return
	 */
	public static String findParameter(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		if(context != null){
			Map<?, ?> requestParams = context.getExternalContext()
				.getRequestParameterMap();		
			String v = (String) requestParams.get(name);
			return v;
		}else{
			return "";
		}
	}

	/**
	 * 查找某个参数的值。
	 * 
	 * @param name
	 * @return
	 */
	public static String[] findParameters(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		return request.getParameterValues(name);
	}

	/**
	 * Determine if we have been passed a parameter that contains a given
	 * string, else null. Typically this would be where you want to check for
	 * one of a set of similar commandLinks or commandButtons, such as the
	 * sortBy headings in evaluation.
	 * 
	 * @param paramPart
	 *            String to look for
	 * @return String last part of full parameter, corresponding to JSF id
	 */
	public static String paramLike(String paramPart) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<?, ?> requestParams = context.getExternalContext()
				.getRequestParameterMap();

		Iterator<?> iter = requestParams.keySet().iterator();
		while (iter.hasNext()) {
			String currKey = (String) iter.next();

			int location = currKey.indexOf(paramPart);
			if (location > -1) {
				return currKey.substring(location);
			}
		}
		return null;
	}

	/**
	 * Determine if we have been passed a parameter that contains a given
	 * string, return ArrayList of these Strings, else return empty list.
	 * 
	 * Typically this would be where you want to check for one of a set of
	 * similar radio buttons commandLinks or commandButtons.
	 * 
	 * @param paramPart
	 *            String to look for
	 * @return ArrayList of last part Strings of full parameter, corresponding
	 *         to JSF id
	 */
	public static ArrayList<String> paramArrayLike(String paramPart) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<?, ?> requestParams = context.getExternalContext()
				.getRequestParameterMap();
		ArrayList<String> list = new ArrayList<String>();

		Iterator<?> iter = requestParams.keySet().iterator();
		while (iter.hasNext()) {
			String currKey = (String) iter.next();

			int location = currKey.indexOf(paramPart);
			if (location > -1) {
				list.add(currKey.substring(location));
			}
		}
		return list;

	}

	/**
	 * Determine if we have been passed a parameter that contains a given
	 * string, else null. Typically this would be where you want to check for
	 * one of a set of similar commandLinks or commandButtons, such as the
	 * sortBy headings in evaluation.
	 * 
	 * @param paramPart
	 *            String to look for
	 * @return String the value of the first hit
	 */
	public static String paramValueLike(String paramPart) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<?, ?> requestParams = context.getExternalContext()
				.getRequestParameterMap();

		Iterator<?> iter = requestParams.keySet().iterator();
		while (iter.hasNext()) {
			String currKey = (String) iter.next();

			int location = currKey.indexOf(paramPart);
			if (location > -1) {
				return (String) requestParams.get(currKey);
			}
		}
		return null;
	}

	/**
	 * Determine if we have been passed a parameter that contains a given
	 * string, return ArrayList of the corresponding values, else return empty
	 * list.
	 * 
	 * Typically this would be where you want to check for one of a set of
	 * similar radio buttons commandLinks or commandButtons.
	 * 
	 * @param paramPart
	 *            String to look for
	 * @return ArrayList of corresponding values
	 */
	public static ArrayList<String> paramArrayValueLike(String paramPart) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<?, ?> requestParams = context.getExternalContext()
				.getRequestParameterMap();
		ArrayList<String> list = new ArrayList<String>();

		Iterator<?> iter = requestParams.keySet().iterator();
		while (iter.hasNext()) {
			String currKey = (String) iter.next();

			int location = currKey.indexOf(paramPart);
			if (location > -1) {
				list.add((String) requestParams.get(currKey));
			}
		}
		return list;

	}
	
	/**
	 * 添加错误信息到context
	 */
	public static void addErrorMessageToContext(String error, String formId) {
		FacesContext context = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage();
		message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(error);
		context.addMessage(formId, message);
	}

	/**
	 * 查找某个backingbean,根据backingbean的名字，<string>不</string>需要#{} Helper method
	 * to look up backing bean. Don't forget to cast! e.g. (TemplateBean)
	 * ContextUtil.lookupBean("template")
	 * 
	 * @param context
	 *            the faces context
	 * @return the backing bean
	 * @throws FacesException
	 */
	public static Object lookupBean(String beanName) {
		if(log.isTraceEnabled()){
			log.trace("looupBean('" + beanName + "')");
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ApplicationFactory factory = (ApplicationFactory) FactoryFinder
				.getFactory(FactoryFinder.APPLICATION_FACTORY);
		Application application = factory.getApplication();
		Object bean = application.getELResolver().getValue(facesContext.getELContext(), null, beanName);
		return bean;
	}

	/**
	 * 查找某个backingbean,根据backingbean的名字，<string>不</string>需要#{}。
	 * 当没有JSF环境时，例如一个单独的servlet使用这个。 Helper method to look up backing bean, when
	 * OUTSIDE faces in a servlet. Don't forget to cast! e.g. (TemplateBean)
	 * ContextUtil.lookupBean("template")
	 * 
	 * @param beanName
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @return the backing bean
	 */
	public static Object lookupBeanFromExternalServlet(String beanName,
			HttpServletRequest request, HttpServletResponse response) {
		// prepare lifecycle
		LifecycleFactory lFactory = (LifecycleFactory) FactoryFinder
				.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		Lifecycle lifecycle = lFactory
				.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

		FacesContextFactory fcFactory = (FacesContextFactory) FactoryFinder
				.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);

		// in the integrated environment, we can't get the ServletContext from
		// the
		// HttpSession of the request - because the HttpSession is
		// webcontainer-wide,
		// its not tied to a particular servlet.
		ServletContext servletContext = M_servletContext;
		if (servletContext == null) {
			servletContext = request.getSession().getServletContext();
		}

		FacesContext facesContext = fcFactory.getFacesContext(servletContext,
				request, response, lifecycle);

		ApplicationFactory factory = (ApplicationFactory) FactoryFinder
				.getFactory(FactoryFinder.APPLICATION_FACTORY);
		Application application = factory.getApplication();
		Object bean = application.getELResolver().getValue(facesContext.getELContext(), null, beanName);
		return bean;
	}

	/**
	 * Called by LoginServlet
	 */
	public static void setServletContext(ServletContext context) {
		M_servletContext = context;
	}

	/**
	 * Gets a localized message string based on the locale determined by the
	 * FacesContext.
	 * 
	 * @param key
	 *            The key to look up the localized string
	 */
	public static String getLocalizedString(String bundleName, String key) {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot()
				.getLocale();
		ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
		return rb.getString(key);
	}

	public static String getLocalizedString(HttpServletRequest request,
			String bundleName, String key) {
		Locale locale = request.getLocale();
		ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
		return rb.getString(key);
	}

	public static String getStringInUnicode(String string) {
		String s = "";
		char[] charArray = string.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char ch = charArray[i];
			s += toUnicode(ch);
		}
		log.debug("***unicode=" + s);
		return s;
	}

	private static char hexdigit(int v) {
		String symbs = "0123456789ABCDEF";
		return symbs.charAt(v & 0x0f);
	}

	private static String hexval(int v) {
		return String.valueOf(hexdigit(v >>> 12))
				+ String.valueOf(hexdigit(v >>> 8))
				+ String.valueOf(hexdigit(v >>> 4))
				+ String.valueOf(hexdigit(v));
	}

	private static String toUnicode(char ch) {
		int val = (int) ch;
		if (val == 10)
			return "\\n";
		else if (val == 13)
			return "\\r";
		else if (val == 92)
			return "\\\\";
		else if (val == 34)
			return "\\\"";
		else if (val == 39)
			return "\\\'";
		else if (val < 32 || val > 126)
			return "\\u" + hexval(val);
		else
			return String.valueOf(ch);
	}

	public static String getRoundedValue(String orig, int maxdigit) {
		Float origfloat = new Float(orig);
		return getRoundedValue(origfloat, maxdigit);
	}

	public static String getRoundedValue(Float orig, int maxdigit) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(maxdigit);
		String newscore = nf.format(orig);
		return newscore;
	}

	public static String escapeApostrophe(String input) {
		// this is needed to escape the ' in some firstname and lastname, that
		// caused javascript error , SAK-4121
		// no longer needed because we don't pass firstname and lastname in
		// f:param. but we'll keep this method here
		String regex = "'";
		String replacement = "\\\\'";
		String output = input.replaceAll(regex, replacement);
		return output;
	}

	public static String getProtocol() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext extContext = context.getExternalContext();
		String server = ((javax.servlet.http.HttpServletRequest) extContext
				.getRequest()).getRequestURL().toString();
		int index = server.indexOf(extContext.getRequestContextPath() + "/");
		String protocol = server.substring(0, index);
		return protocol;
	}

	public static String stringWYSIWYG(String s) {// this is to detect an
		// empty in WYSIWYG FF1.5

		if ((s != null) && ("&nbsp;".equals(s.trim())))
			s = "";
		return s;
	}

	public static String getRelativePath(String url) {
		// replace whitespace with %20
		String protocol = getProtocol();
		url = replaceSpace(url);
		String location = url;
		int index = url.lastIndexOf(protocol);
		if (index == 0) {
			location = url.substring(protocol.length());
		}
		return location;
	}

	private static String replaceSpace(String tempString) {
		String newString = "";
		char[] oneChar = new char[1];
		for (int i = 0; i < tempString.length(); i++) {
			if (tempString.charAt(i) != ' ') {
				oneChar[0] = tempString.charAt(i);
				String concatString = new String(oneChar);
				newString = newString.concat(concatString);
			} else {
				newString = newString.concat("%20");
			}
		}
		return newString;
	}
	/**
	 * beanName不要包含#{}等内容，例如getBackingBean("course")而不是用getBackingBean("#{course}")
	 * @param beanName
	 * @return
	 */
	public static Object getBackingBean(String beanName) {
		Application app = getApplication();
		FacesContext context = FacesContext.getCurrentInstance();
		return app.getELResolver().getValue(context.getELContext(), null, beanName);
	}
	public static void setBackingBean(String beanName, Object bean){
		Application app = getApplication();
		FacesContext context = FacesContext.getCurrentInstance();
		ELResolver el = new BeanELResolver();
		el.setValue(context.getELContext(), null, beanName, bean);
		app.addELResolver(el);
	}
	public static Application getApplication() {
		return ((ApplicationFactory) FactoryFinder
				.getFactory(FactoryFinder.APPLICATION_FACTORY))
				.getApplication();
	}

	public static String getValueBindingBeanName(String beanName) {
		return "#{" + beanName + "}";
	}

	public static String getRequestParameter(String paramName) {
		return (String) FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get(paramName);
	}

	public static Object getRequestAttribute(String attrName) {
		return ((ServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest()).getAttribute(attrName);
	}

	public static ServletRequest getServletRequest() {
		return (ServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
	}

	public static ServletResponse getServletResponse() {
		return (ServletResponse) FacesContext.getCurrentInstance()
				.getExternalContext().getResponse();
	}

	static public ServletContext getServletContext() {
		return (ServletContext) FacesContext.getCurrentInstance()
				.getExternalContext().getContext();
	}

}
