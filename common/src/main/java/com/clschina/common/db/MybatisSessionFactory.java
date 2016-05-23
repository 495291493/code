package com.clschina.common.db;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


public class MybatisSessionFactory {
	private static Log log = LogFactory.getLog(MybatisSessionFactory.class);
	private static MybatisSessionFactory instance = null;
	private SqlSessionFactory sqlSessionFactory;

	private MybatisSessionFactory(){}

	
	public static SqlSessionFactory getSqlSessionFactory(){
		if(instance == null){
			init();
		}
		return instance.sqlSessionFactory;
	}
	
	public synchronized static void init(){
		if(instance == null){
			instance = new MybatisSessionFactory();
			instance.initConfiguration();
		}
	}
	
	public void initConfiguration(){
		String resource = "mybatis.config.xml";
		InputStream inputStream = null;
		try {
			inputStream = Resources.getResourceAsStream(resource);
			this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			this.sqlSessionFactory.getConfiguration().addMappers("com.clschina.erp.mappers");
			this.sqlSessionFactory.getConfiguration().addMappers("com.clschina.saojifen.mappers");
			this.sqlSessionFactory.getConfiguration().addMappers("com.clschina.interfaces.mappers");
			this.sqlSessionFactory.getConfiguration().addMappers("com.clschina.yuetu.mappers");
			//扫钱宝
			this.sqlSessionFactory.getConfiguration().addMappers("com.saofenbao.mappers");
		} catch (IOException e) {
			log.error("Init Mybatis SqlSessionFactory Error.", e);
		}
	}
}
