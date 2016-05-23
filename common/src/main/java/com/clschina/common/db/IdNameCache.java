package com.clschina.common.db;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;



public abstract class IdNameCache extends DataCache {
	private static Log log = LogFactory.getLog(IdNameCache.class);

	protected String idColumnName = "id";
	protected String nameColumnName = "name";
	protected String statusColumnName = "status";
	protected String tableName = null;
	protected int deletedStatus = -1;
	private boolean showIdAfterName = false;	//是否在名字后面显示编号
	
	public IdNameCache(String tableName, String idColumnName, 
			String nameColumnName, String statusColumnName, int deletedStatus){
		this(tableName, idColumnName, nameColumnName, statusColumnName, deletedStatus, true);
	}
	public IdNameCache(String tableName, String idColumnName, 
			String nameColumnName, String statusColumnName, int deletedStatus, boolean showIdAfterName){
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.nameColumnName = nameColumnName;
		this.statusColumnName = statusColumnName;
		this.deletedStatus = deletedStatus;
		this.showIdAfterName = showIdAfterName;
	}	
	@Override
	protected Entry<String, String> element2Entry(Object t) {
		Object[] os = (Object[]) t;
		final String id = ((String) os[0]).trim();
		String name = (String) os[1];
		int status = ((Number) os[2]).intValue();
		final String value = (name.trim() + (status == deletedStatus ? "{已删除}" : "") + (showIdAfterName ? " <" + id + ">" : ""));
		return new Entry<String, String>(){

			public String getKey() {
				return id;
			}

			public String getValue() {
				return value;
			}

			public String setValue(String value) {
				return null;
			}
			
		};
	}

	@Override
	protected List<?> loadData() {
		Session session = getHibernateSession();
		Transaction trans = null;
		trans = session.beginTransaction();
		Query q;
		String sql = "select " + idColumnName + ", " + nameColumnName + ", " + statusColumnName + "  FROM " + tableName;
		if(log.isTraceEnabled()){
			log.trace("sql=" + sql);
		}
		q = session.createQuery(sql);
		List<?> result = q.list();
		trans.commit();
		return result;
	}
	
	protected abstract Session getHibernateSession();
}
