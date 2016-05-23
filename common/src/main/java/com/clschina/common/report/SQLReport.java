package com.clschina.common.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.HibernateSessionFactory;
import com.clschina.common.report.conf.SQLXMLReportConfiguration;


public class SQLReport extends BaseReport {
	private static Log log = LogFactory.getLog(SQLReport.class);
	private SQLXMLReportConfiguration conf;
	
	public SQLReport(InputStream xmlIS) throws IOException{
		initConfiguration(xmlIS);
	}
	
	/**
	 * 配置文件必须放置在 /WEB-INF/report-config/xml/ 目录下
	 * @param xmlConfigFile
	 * @throws IOException
	 */
	public SQLReport(String xmlConfigFile) throws IOException{
		String f = ThreadLocalManager.getContextFolder() + "/WEB-INF/report-config/xml/" + xmlConfigFile;
		InputStream fis = new FileInputStream(f);
		initConfiguration(fis);
		fis.close();
	}
	
	private void initConfiguration(InputStream xmlIS) throws IOException{
		conf = new SQLXMLReportConfiguration(xmlIS);
		this.setConfiguration(conf);
		if(log.isTraceEnabled()){
			log.trace("SQL in XML:\r\n" + conf.getSql());
		}
	}
	/**
	 * 单SQL查询调用getHTMLTable/genExcel前，请调用这个方法，把SQL需要的参数传递进来。
	 * @param params
	 */
	public void query(Object[] params){
		String orignalSQL = conf.getSql();
		if(log.isTraceEnabled()){
			log.trace("SQL in XML:\r\n" + orignalSQL);
		}
		String sql = MessageFormat.format(orignalSQL, params);
		if(log.isTraceEnabled()){
			log.trace("SQL:\r\n" + sql);
		}
		query(sql);
	}
	
	/**
	 * 注：此方法是多SQL查询，如果只有一个SQL请调用query(Object[] params)方法
	 */
	public void queryForManySql(Object[] params){
		for(int i=0; i<conf.getAllSql().size(); i++){
			String orignalSQL = conf.getAllSql().get(i);
			if(log.isTraceEnabled()){
				log.trace("SQL in XML:\r\n" + orignalSQL);
			}
			
			Object[] param = (Object[])params[i];
			String sql = MessageFormat.format(orignalSQL, param);
			if(log.isTraceEnabled()){
				log.trace("SQL:\r\n" + sql);
			}
			List<Object[]> data = executeQuery(sql);
			super.addData(data);		
		}
	}
	
	/**
	 * 如果SQL不在XML中配置，可直接执行这个方法。然后调用getHTMLTable/genExcel
	 * @param sql
	 */
	public void query(String sql){
		List<Object[]> data = executeQuery(sql);
		super.setData(data);		
	}
	
	public void query(){
		query(conf.getSql());
	}
	
	/**
	 * 如果需要多个sql执行结果，或单个sql，然后根据结果再计算出列来。
	 * 可以通过 executeQuery，取得结果（多个调用多次）。
	 * 然后组成一个数据，并调用setData(List<Object[]>)保存，之后调用getHTMLTable/genExcel
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> executeQuery(String sql){
		try{
			Session s = getSession();
			List<Object[]> data = (List<Object[]>) s.createSQLQuery(sql).list();
			if(log.isTraceEnabled()){
				log.trace("query found " + data.size() + " rows.");
			}
			return data;	
		}catch(Exception e){
			log.error("创建查询失败: " + sql, e);
			throw new RuntimeException(e);
		}
	}
	
	protected Session getSession() {
		Session s = (Session) ThreadLocalManager.getValue(ThreadLocalManager.HIBERNATE_SESSION);
		if(s != null && (!s.isOpen() || !s.isConnected())){
			if(log.isInfoEnabled()){
				log.info("SESSION isOpen=" + s.isOpen() + "; isConnected=" + s.isConnected());
			}
			s = null;
		}
		if(s == null){
			if(log.isInfoEnabled()){
				log.info("cannot found hibernate session from ThreadLocalManager.");
			}
			SessionFactory factory = HibernateSessionFactory.getSessionFactory();
			if(log.isTraceEnabled()){
				log.trace("get session from HibernateSessionFactory " + factory);
			}
			s = factory.openSession();
			ThreadLocalManager.setValue(ThreadLocalManager.HIBERNATE_SESSION, s);
		}
		return s;
	}
	

}
