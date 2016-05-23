package com.clschina.common.db;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;



@SuppressWarnings("unchecked")
public abstract class IdGiftCache extends GiftDataCache {
	private static Log log = LogFactory.getLog(IdGiftCache.class);

	protected String hql;
	
	public IdGiftCache(String hql){
		this.hql=hql;
	}
		
	@Override
	protected Entry<String, Object> element2Entry(final Object t) {
		return new Entry<String, Object>(){
			Object[] os = (Object[]) t;
			final String id = (String) os[0];
			
			public String getKey() {
				return id;
			}
			public Object getValue() {
				return os[1];
			}

			public Object setValue(Object arg0) {
				// TODO Auto-generated method stub
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
		String sql = hql;
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
