package com.clschina.common.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadLocalManager {
	private static Log log = LogFactory.getLog(ThreadLocalManager.class);
	private static Properties prop;
	private static Properties ldProp;
	private static Properties erpProp;
	public static final String HIBERNATE_SESSION = "hibernate_session";
	public static final String MYBATIS_SESSION = "mybatis_session";
	public static final String REQUEST = "request";
	public static final String RESPONSE = "response";
	public static final String LOGIN = "login";
	public static String contextFolder;
	
	protected ThreadBindings m_bindings = new ThreadBindings();

	private static ThreadLocalManager instance = new ThreadLocalManager();
	
	protected class ThreadBindings extends ThreadLocal<Map<String, Object>> {
	    private String instanceTime = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date());
		public Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}

		public Map<String, Object> getBindings() {
			return (Map<String, Object>) get();
		}
		
		@Override
		public String toString(){
		     return super.toString() + " instanced at " + instanceTime + "; keys=" + getBindings().keySet().toString();
		}
	}
	
	private ThreadLocalManager(){
		
	}
	public static ThreadLocalManager getInstance(){
		return instance;
	}
	public static void setValue(String name, Object value){
		instance.set(name, value);
	}
	public static Object getValue(String name){
		return instance.get(name);
	}
	
	/**
	 * 返回当前会话的登录的用户，如果当前无用户登录，返回NULL
	 * @return
	 */
	public static Login getLogin(){
	    Login u = null;
		try{
			u = (Login) getRequest().getSession().getAttribute(LOGIN);
		}catch(Exception e){
		}
//		if(u == null){
//		    u = new Guest();
//		}
		return u;
//		return (Login)FacesContext.getCurrentInstance()
//		.getExternalContext().getSessionMap().get("login");
	}
	
	/**
	 * 设置会话登录的用户
	 * @param user
	 */
	public static void setLogin(Login user){
		if(user != null){
			getRequest().getSession().setAttribute(LOGIN, user);
		}else{
			getRequest().getSession().removeAttribute(LOGIN);
		}
		
	}
	/**
	 * 返回当前线程会话的HttpServletRequest类
	 * @return
	 */
	public static HttpServletRequest getRequest(){
		return (HttpServletRequest) instance.get(REQUEST);
	}
	
	/**
	 * 返回当前线程会话的HttpServletResponse类
	 * @return
	 */
	public static HttpServletResponse getResponse(){
		return (HttpServletResponse) instance.get(RESPONSE);
	}

	
	/**
	 * Bind this object under this name with the current thread, or remove if the value is null.
	 * 
	 * @param name The binding name.
	 * @param value The object to bind, or null to unbind this name.
	 */
	public void set(String name, Object value) {
		// find the map that might already exist
		Map<String, Object> bindings = m_bindings.getBindings();
		if (bindings == null) {
			log.info("setInThread: no bindings!");
			return;
		}

		if (value == null) {
			bindings.remove(name);
		}else {
			bindings.put(name, value);
		}
	}

	/**
	 * Remove all objects bound to the current thread.
	 */
	public void clear(){
		Map<?, ?> bindings = m_bindings.getBindings();
		if (bindings == null) {
			if(log.isInfoEnabled()){
				log.info("clear: no bindings!");
			}
			return;
		}
		bindings.clear();	
		m_bindings.remove();		
	}

	/**
	 * Find the named object bound to the current thread.
	 * 
	 * @param name The binding name.
	 * @return The object bound by this name, or null if not found.
	 */
	public Object get(String name) {
		Map<?, ?> bindings = m_bindings.getBindings();
		if (bindings == null) {
			log.info("get: no bindings!");
			return null;
		}

		return bindings.get(name);
	}

	/**
	 * 取得当前Context的绝对路径
	 * @return
	 */
	public static String getContextFolder(){
		return contextFolder;
	}
	/**
	 * 设置当前Context的绝对路径，由Filter启动时调用设置。
	 * @param f
	 */
	public static void setContextFolder(String f){
		contextFolder = f;
	}
	
	public File getLogFileFolder() {
		Properties p = getProperties();
		String folder = p.getProperty("log_folder");
		if(folder != null){
			File file = new File(folder);
			if(!file.exists()){
				file.mkdirs();
			}
			return file;
		}
		return null;
	} 
	
	/**
	 * 取得liduo.properties
	 * @deprecated 推荐使用 getConfigProperties() ,把设置信息放到WEB-INF/classes/config.properties内
	 * @return
	 */
	public Properties getProperties(){
		if(ldProp == null){
			ldProp = new Properties();
			try {
				InputStream in = this.getClass().getResourceAsStream("/liduonet.properties");
				ldProp.load(in);
			} catch (IOException e) {
				if(log.isErrorEnabled()){
					log.error("liduonet.properties is not exist");
				}
			}
		}
		return ldProp;
	}
	
	/**
	 * WEB-INF/classes目录下的config.properties文件
	 * @return
	 */
	public Properties getConfigProperties(){
		/*if(prop == null){
			prop = new Properties();
			try {
				InputStream in = this.getClass().getResourceAsStream("/config.properties");
				prop.load(in);
			} catch (IOException e) {
				if(log.isErrorEnabled()){
					log.error("liduonet.properties is not exist");
				}
			}
		}
		return prop;*/
	    String fixProperFileNameUrl = "/config.properties";
	    return this.getConfigPropertiesByFileName(fixProperFileNameUrl);
	}
	
	/**
	 *  WEB-INF/classes目录下的传入的properFileUrl文件
	 * @param properFileUrl
	 *             要读取的文件的url
	 * @return
	 */
	public Properties getConfigProperties(String properFileUrl){
	    return this.getConfigPropertiesByFileName(properFileUrl);
	}
	
	private Properties getConfigPropertiesByFileName(String properFileUrl){
	    if(prop == null){
            prop = new Properties();
            try {
                InputStream in = this.getClass().getResourceAsStream(properFileUrl);
                prop.load(in);
            } catch (IOException e) {
                if(log.isErrorEnabled()){
                    log.error("["+properFileUrl + "]该属性文件没有找到，不能进行载入：", e);
                }
            }
        }
        return prop;
	}
	
	/**
	 *   @deprecated 推荐使用 getConfProperties() ,把设置信息放到WEB-INF/classes/config.properties内
	 * @return
	 */
	public Properties getERPProperties(){
		if(erpProp == null){
			erpProp = new Properties();
			try {
				InputStream in = this.getClass().getResourceAsStream("/erp.properties");
				erpProp.load(in);
			} catch (IOException e) {
				if(log.isErrorEnabled()){
					log.error("erp.properties is not exist");
				}
			}
		}
		return erpProp;
	}

	/**
	 * 取webapp下的files目录
	 * @return
	 * @deprecated 请使用 getFilesFolder()
	 */
	public File getERPFileFolder() {
		return getFilesFolder();
	}
	/**
	 * 取webapp下的files目录，从conf.properties中找 files_folder的配置。
	 * （不推荐：如果找不到，会查找erp.properties中的files_folder，这仅仅是为了旧程序兼容）
	 * @return
	 */
	public File getFilesFolder() {
		String folder = getConfigProperties().getProperty("files_folder");

		if(folder == null){
			folder = getERPProperties().getProperty("files_folder");
		}
		if(folder != null){
			File file = new File(folder);
			if(!file.exists()){
				file.mkdirs();
			}
			return file;
		}else{
			
		}
		return null;
	}
	
	

	
	/**
	 * 获得ERP当前帐套
	 * @deprecated 已经取消
	 * @return
	 */
	public String getZangTao() {
		String zt = null;
		try{
			zt = getProperties().getProperty("zangtao");
		} catch (Exception e) {}
		return zt;
	}
	
	/**
	 *   @deprecated 已经取消
	 * @return
	 */
	public String getErpZangTao() {
		String zt = null;
		try{
			zt = getERPProperties().getProperty("zangtao");
		} catch (Exception e) {}
		return zt;
	}
}
