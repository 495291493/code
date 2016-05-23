package com.clschina.common.backingbean;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.component.ThreadLocalManager;


public class ConfigurationBackingBean {
	private static Log log = LogFactory.getLog(ConfigurationBackingBean.class);

	private Properties conf;
	
	public ConfigurationBackingBean() throws Exception{
		InputStream is = this.getClass().getResourceAsStream("/config.properties");
		conf = new Properties();
		conf.load(is);
	}
	
	public Properties getProperties(){
		if(log.isTraceEnabled()){
			log.trace("conf = " + conf);
		}
		return conf;
	}
	
	public String getContextPath(){
		return ThreadLocalManager.getContextFolder();
	}
	
	public boolean isTestServer(){
		String ip = ThreadLocalManager.getRequest().getLocalAddr();
		if(ip != null && (ip.startsWith("192.168.") || ip.equals("127.0.0.1") || ip.equals("0000:0000:0000:0000:0000:0000:0000:0001") || ip.equals("::1") || ip.equals("0.0.0.0"))){
			return true;
		}
		return false;
	}
}
