package com.clschina.common.db;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;



public abstract class IdNameSQLCache<T>  extends DataCache<Object[]>{
	private static final long serialVersionUID = -5412417069166542403L;

	private static Log log = LogFactory.getLog(IdNameCache.class);

	protected String sql;

	/**
	 * 返回的第一列是key，第2列是value；
	 * @param sql
	 */
	public IdNameSQLCache(String sql){
		this.sql = sql;
	}

	@Override
	protected Entry<String, String> element2Entry(Object[] t) {
		final String k = ((String) t[0]).trim();
		final String v = (String) t[1];
		
		return new Entry<String, String>(){

			public String getKey() {
				return k;
			}

			public String getValue() {
				return v;
			}

			public String setValue(String value) {
				return null;
			}
			
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Object[]> loadData() {
		Session session = getHibernateSession();
		Transaction trans = null;
		trans = session.beginTransaction();
		Query q;
			if(log.isTraceEnabled()){
			log.trace("sql=" + sql);
		}
		q = session.createSQLQuery(sql);
		List<Object[]> result = (List<Object[]>) q.list();
		trans.commit();
		return result;
	}
	
	protected abstract Session getHibernateSession();
}
