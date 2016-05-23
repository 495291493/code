package com.clschina.common.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.clschina.common.util.ClassPath;
import com.clschina.common.util.FileNameComparator;


public class HibernateSessionFactory {
	private static Log log = LogFactory.getLog(HibernateSessionFactory.class);
	private static HibernateSessionFactory instance = null;
	private SessionFactory factory;
	private Configuration cfg = null;
	
	private HibernateSessionFactory(){}

	
	public static SessionFactory getSessionFactory(){
 		if(log.isTraceEnabled()){
			log.trace("getSessionFactory()");
		}
		if(instance == null || instance.factory == null){
			init(null);
		}
		
		return instance.factory;
	}
	
	public static void init(String hbmPath){
		if(log.isTraceEnabled()){
			log.trace("init(" + hbmPath + ")");
		}
		if(instance == null){
			instance = new HibernateSessionFactory();
		}
		instance.initConfiguration(hbmPath);
	}
	
	public void initConfiguration(String hbmPath){
		URL cfgUrl = this.getClass().getResource("/hibernate.cfg.xml");
		if(log.isTraceEnabled()){
			log.trace("initConfiguration(" + hbmPath + ") cfgUrl=" + cfgUrl);
		}
		try{
			cfg = new Configuration();
		}catch(Exception e){
			log.error("Error while loading configuration file from " + cfgUrl);
			return;
		}
		if(log.isTraceEnabled()){
			log.trace("initConfiguration(" + hbmPath + ") cfg=" + cfg);
		}
		cfg.configure(cfgUrl);
		cfg.buildSettings();

		String dialectName = cfg.getProperty("hibernate.dialect");
		
		//1. 取得classes目录下所有配置文件
		try {
			if(hbmPath != null){
				File f = new File(hbmPath);
				File[] files = f.listFiles();
				ArrayList<File> hbmList = new ArrayList<File>();
				for(int i=0; i<files.length; i++){
					if(files[i].getName().endsWith(".hbm.xml")){
						hbmList.add(files[i]);
					}
				}
				
				// 排序
				Collections.sort(hbmList, new FileNameComparator());
	
				// 登记Hibernate配置文件
				for (Iterator<File> it = hbmList.iterator(); it.hasNext();) {
					File u = it.next();
					if(log.isTraceEnabled()){
						log.trace("adding configuration file " + u.getPath());
					}
					try {
						addHBMXML(cfg, new FileInputStream(u));
						//factory = cfg.buildSessionFactory();
					} catch (Exception e) {
						if(log.isErrorEnabled()){
							log.error("Error occured while register file "
								+ u.getPath() + ", omit it.", e);
						}
					}
				}
			}
		} catch (Exception e) {
			if(log.isFatalEnabled()){
				log.fatal("Cannot found the hibernate.cfg.xml file, "
					+ "is it in classpath?", e);
			}
		}
		
		//2. 取得jar中所有配置文件
		try {
			URL[] hbms;
			hbms = ClassPath.search(Thread.currentThread()
					.getContextClassLoader(), "META-INF/config/hbm/"
					+ dialectName + "/", ".hbm.xml");
			if(log.isTraceEnabled()){
				log.trace("found " + hbms.length + " resource(s) with prefix '"
					+ "META-INF/config/hbm/" + dialectName + "/"
					+ "' and suffix '.hbm.xml'");
			}
			ArrayList<URL> hbmList = new ArrayList<URL>();
			for (int i = 0; i < hbms.length; i++) {
				hbmList.add(hbms[i]);
			}
			// 排序
			Collections.sort(hbmList, new FileNameComparator());

			// 登记Hibernate配置文件
			for (Iterator<URL> it = hbmList.iterator(); it.hasNext();) {
				URL u = it.next();
				if(log.isTraceEnabled()){
					log.trace("adding configuration file " + u.getFile());
				}
				try {
					addHBMXML(cfg, u.openStream());
					//factory = cfg.buildSessionFactory();
				} catch (Exception e) {
					log.error("Error occured while register file "
							+ u.getFile() + ", omit it.", e);
				}

			}
		} catch (IOException e) {
			if(log.isFatalEnabled()){
				log.fatal("Cannot found the hibernate.cfg.xml file, "
					+ "is it in classpath?", e);
			}
		}

		factory = cfg.buildSessionFactory();
		
	}
	
	/**
	 * 把一个hbm文件，加入到sessionfactory中
	 * @param hbmIs
	 */
	private void addHBMXML(Configuration cfg, InputStream hbmIs) {
		cfg.addInputStream(hbmIs);
		//factory = cfg.buildSessionFactory();
	}	

	public static Configuration getConfiguration(){
		return instance == null ? null : instance.cfg;
	}
}
