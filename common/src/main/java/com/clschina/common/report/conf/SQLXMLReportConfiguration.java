package com.clschina.common.report.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clschina.common.util.DomUtil;

/**
 * 从XML读取配置信息；XML例子参照/com/clschina/erp/caigoudaopiao/backingbean/sample.
 * reportconfig.xml
 * 
 */
public class SQLXMLReportConfiguration extends XMLReportConfiguration {
	private static Log log = LogFactory.getLog(SQLXMLReportConfiguration.class);
	private List<String> sql;//SQL语句

	public SQLXMLReportConfiguration(File xmlFile) throws IOException {
		super(xmlFile);
	}

	public SQLXMLReportConfiguration(InputStream is) throws IOException {
		super(is);
		if(log.isTraceEnabled()){
			log.trace("sql["+this.sql+"]");
		}
	}
		
	/**
	 * 解析配置文件
	 * 
	 * @param root
	 * @return
	 */
	protected void init(Element root) {
		super.init(root);
		if(log.isTraceEnabled()){
			log.trace("init called.");
		}
		NodeList list = root.getElementsByTagName("sql");
		if(list.getLength() == 0){
			return;
		}
		
		if(log.isTraceEnabled()){
			log.trace("found " + list.getLength() + " nodes named 'sql'");
		}
		
		this.sql = new ArrayList<String>();
		for(int i=0;i<list.getLength(); i++){
			String v = DomUtil.getNodeValue(list.item(i)).trim();
			this.sql.add(v);
			if(log.isTraceEnabled()){
				log.trace("hql=" + v);
			}
		}
	}
	

	public String getSql() {
		return sql == null || sql.isEmpty() ? null : sql.get(0);
	}
	
	public List<String> getAllSql() {
		return this.sql;
	}
}
