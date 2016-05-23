package com.clschina.common.db;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.clschina.common.component.Login;
import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.bean.DataEntryInterface;
import com.clschina.common.db.bean.DataEntryInterfaceWithStatus;

/**
 * 包含了一些基本的数据库操作。所有DAOHome类可以从此处继承，使用其中方法。
 *
 */
public class DAOBase {
	/**
	 * 数据库表锁
	 */
	public static final String TABLOCKX = "TABLOCKX";
	/**
	 * 数据库行锁
	 */
	public static final String UPDLOCK = "UPDLOCK";
	/**
	 * 不添加任何锁
	 */
	public static final String NOLOCK = "NOLOCK";
	
	private final static Log log = LogFactory.getLog(DAOBase.class);

	/**
	 * 取得一个hibernate的session，所有取得session的操作都应该从这里取得。
	 * @return
	 */
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
	
	/**
	 * 保存某个类到数据库。
	 * @param o 要保存的类
	 * @param entityName hbm配置文件中的entiry-name
	 * @return 保存成功后，如果是新增，返回自动生成的ID，如果没有ID生成，则返回null
	 * @throws RuntimeException 保存失败
	 */
	protected String save(String entityName, Object o){
		if(log.isTraceEnabled()){
			log.trace("save('" + entityName + "', " + o.getClass().getName() + ")");
		}
		String id = null;
		Session session = getSession();
		Transaction trans = null;
		try {
			trans = session.beginTransaction();
			if(o instanceof DataEntryInterface){
				if(log.isTraceEnabled()){
					log.trace("save('" + entityName + "', " + o.getClass().getName() + " extends DataEntiryInterface) updating ...");
				}
				DataEntryInterface dei = (DataEntryInterface) o;
				dei.setModifyDate(Calendar.getInstance());
				Login login = ThreadLocalManager.getLogin();
				String modificator = null;
				if(login != null){
					modificator = login.getId();
				}
				HttpServletRequest request = ThreadLocalManager.getRequest();
				String ip = null;
				if(request != null){
					ip = request.getRemoteAddr();
				}
				if(modificator != null){
					dei.setModificator(modificator);
				}
				if(ip != null){
					dei.setModifyIP(ip);
				}
			}
			session.saveOrUpdate(entityName, o);
			session.flush();
			try {
				id = (String) session.getIdentifier(o);
			} catch (Exception e) {
				log.debug("cannot get the primary key of the object." + o.getClass().getName() + ", maybe doesnot has an id.");
			}
			trans.commit();
			log.trace("object " + o.getClass().getName() + " save successful");
		} catch (RuntimeException re) {
			if (trans != null) {
				trans.rollback();
			}
			log.error("object " + o.getClass().getName() + " save failed", re);
			throw re;
		} finally {

		}
		return id;
	}
	
	/**
	 * 传递session，使操作在同一个事务中
	 * @param session 传递的过来的session，已经开启一个事务
	 * @param entityName hbm配置文件中的entiry-name
	 * @param o 要保存的类
	 * @return 保存成功后，如果是新增，返回自动生成的ID，如果没有ID生成，则返回null
	 */
	protected String save(Session session,String entityName, Object o){
		if(log.isTraceEnabled()){
			log.trace("save('" + entityName + "', " + o.getClass().getName() + ")");
		}
		String id = null;	
		
		try {
			if(o instanceof DataEntryInterface){
				if(log.isTraceEnabled()){
					log.trace("save('" + entityName + "', " + o.getClass().getName() + " extends DataEntiryInterface) updating ...");
				}
				DataEntryInterface dei = (DataEntryInterface) o;
				dei.setModifyDate(Calendar.getInstance());
				Login login = ThreadLocalManager.getLogin();
				String modificator = null;
				if(login != null){
					modificator = login.getId();
				}
				HttpServletRequest request = ThreadLocalManager.getRequest();
				String ip = null;
				if(request != null){
					ip = request.getRemoteAddr();
				}
				if(modificator != null){
					dei.setModificator(modificator);
				}
				if(ip != null){
					dei.setModifyIP(ip);
				}
			}
			session.saveOrUpdate(entityName, o);
//			session.flush();//2012.3.5因为是传递session，不能在中间过程中刷新session
			try {
				id = (String) session.getIdentifier(o);
			} catch (Exception e) {
				log.debug("cannot get the primary key of the object." + o.getClass().getName() + ", maybe doesnot has an id.");
			}
			
			log.trace("object " + o.getClass().getName() + " save successful");
		} catch (RuntimeException re) {			
			log.error("object " + o.getClass().getName() + " save failed", re);
			throw re;
		} finally {

		}
		return id;
	}
	/**
	 * 根据主键ID查询某个类，注意返回的数据可能是已经被标记为删除的。如果返回的Object是
	 * DataEntryInterfaceWithStatus 类型，会不返回DataEntryInterfaceWithStatus.DELETED_STATUS
	 * 的类
	 * @param entityName hbm中的entity-name
	 * @param id 主键
	 * @return 查找到的对象
	 */
	public Object findById(String entityName, Serializable id){
		return findById(entityName, id, true);
	}
	/**
	 * 根据主键ID查询某个类，注意返回的数据可能是已经被标记为删除的。
	 * @param entityName hbm中的entity-name
	 * @param id 主键
	 * @param ignoreDeleteStatus 是否忽略删除状态，true则不返回被删除的记录
	 * @return 查找到的对象
	 */
	protected Object findById(String entityName, Serializable id, boolean ignoreDeleteStatus){
		log.trace("find " + entityName + " with id '" + id + "'");
		if(id == null){
			return null;
		}
		try {
			Object instance = getSession().get(entityName, id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
				if(!ignoreDeleteStatus && instance instanceof DataEntryInterfaceWithStatus){
					if(((DataEntryInterfaceWithStatus) instance).getStatus() == DataEntryInterfaceWithStatus.DELETED_STATUS){
						log.debug("the status is deleted.");
						instance = null;
					}
				}
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}		
	}
	/**
	 * 根据某些条件，从数据库中查询某些数据。可以指定数据的开始位置和最大返回的记录数。供分页时使用。
	 * 此方法只适合查询一个表的简单查询，并且所有条件都是and。
	 * @param entityName
	 * @param params HSQL的where子句中的条件。
	 * @return 返回list的结果集合
	 */
	protected List<?> queryTable(String entityName, List<QueryParameter> params){
		return queryTable(entityName, params, null, -1, -1);
	}
		
	/**
	 * 根据某些条件，从数据库中查询某些数据。可以指定数据的开始位置和最大返回的记录数。供分页时使用。
	 * 此方法只适合查询一个表的简单查询，并且所有条件都是and。
	 * @param entityName
	 * @param params HSQL的where子句中的条件。
	 * @param orderBy 排序的字段，不排序，传递null，如果多个字段参与才排序，传递 id asc, name asc, date desc 等等
	 * @param first 第一条记录的位置
	 * @param max  最大返回记录数。0表示返回说有数据。
	 * @return 返回list的结果集合
	 */
	protected List<?> queryTable(String entityName, List<QueryParameter> params, String orderBy, int first, int max){
		log.trace("enter queryTable. entity-name='" + entityName + "'");
		Session session = getSession();
		//Transaction trans = null;
		//trans = session.beginTransaction();
		Query q;
		String sql;
		sql = "from " + entityName + " ";
		if(params != null && params.size() > 0){
			boolean firstParameter = true;
			for(int i=0; i<params.size(); i++ ){
				QueryParameter qp =  params.get(i);
				if(firstParameter){
					sql += " where ";
					firstParameter = false;
				}else{
					sql += " and ";
				}
				sql += " " + qp.getProperty() + " " + qp.getSign() + (qp.getSign().trim().equalsIgnoreCase("in") ? "(" : "") + " :" + qp.getProperty() + "_" + i +" " + (qp.getSign().trim().equalsIgnoreCase("in") ? ")" : "") + " ";
			}
		}
		log.trace("queryTable, HQL=" + sql);
		if(orderBy != null){
			sql += " order by " + orderBy;
		}
		q = session.createQuery(sql);
		if(params != null && params.size() > 0){
			for(int i=0; i<params.size(); i++ ){
				QueryParameter qp =  params.get(i);
				String s = qp.getProperty() + "_" + i;
				if(log.isTraceEnabled()){
					log.trace("'" + s + "' => " + qp.getValue());
				}
				if(qp.getValue() instanceof Object[]){
					if(qp.getType() != null){
						q.setParameterList(s, (Object[]) qp.getValue(), qp.getType());
					}else{
						q.setParameterList(s, (Object[]) qp.getValue());
					}
				}else if(qp.getValue() instanceof Collection<?>){
					if(qp.getType() != null){
						q.setParameterList(s, (Collection<?>) qp.getValue(), qp.getType());
					}else{
						q.setParameterList(s, (Collection<?>) qp.getValue());
					}
				}else{
					if(qp.getType() != null){
						q.setParameter(s, qp.getValue(), qp.getType());
					}else{
						if(qp.getValue() instanceof Double){
							q.setDouble(s, (Double) qp.getValue());
						}else if(qp.getValue() instanceof Float){
							q.setFloat(s, (Float) qp.getValue());
						}else if(qp.getValue() instanceof Boolean){
							q.setBoolean(s, (Boolean) qp.getValue());
						}else{
							q.setParameter(s, qp.getValue());
						}
					}
				}
			}
		}
		if(first >= 0){
			q.setFirstResult(first);
		}
		if(max > 0){
			q.setMaxResults(max);
		}
		List<?> result = q.list();
		//trans.commit();
		log.trace("queryTable found " + result.size() + " records. entity-name='" + entityName + "'");
		return result;
	}
	/**
	 * 根据某些条件，统计记录的总数。可以和queryTable一起使用，用来分页时统计总数居数目。
	 * 此方法只适合查询一个表的简单查询，并且所有条件都是and。
	 * @param entityName
	 * @param params HSQL的where子句中的条件。
	 */
	protected int countRecords(String entityName, List<QueryParameter> params){
		log.trace("enter countRecords. entity-name='" + entityName + "'");
		Session session = getSession();
		Transaction trans = null;
		trans = session.beginTransaction();
		Query q;
		String sql;
		sql = "select count(*) from " + entityName + " ";
		if(params != null && params.size() > 0){
			boolean firstParameter = true;
			for(int i=0; i<params.size(); i++ ){
				QueryParameter qp =  params.get(i);
				if(firstParameter){
					sql += " where ";
					firstParameter = false;
				}else{
					sql += " and ";
				}
				sql += " " + qp.getProperty() + " " + qp.getSign() + (qp.getSign().trim().equalsIgnoreCase("in") ? "(" : "") + " :" + qp.getProperty() + "_" + i +" " + (qp.getSign().trim().equalsIgnoreCase("in") ? ")" : "") + " ";
			}
		}
		if(log.isTraceEnabled()){
			log.trace("countRecords, HQL=" + sql);
		}

		q = session.createQuery(sql);
		if(params != null && params.size() > 0){
			for(int i=0; i<params.size(); i++ ){
				QueryParameter qp =  params.get(i);
				String s = qp.getProperty() + "_" + i;
				if(log.isTraceEnabled()){
					log.trace("'" + s + "' => " + qp.getValue());
				}
				if(qp.getValue() instanceof Object[]){
					if(qp.getType() != null){
						q.setParameterList(s, (Object[]) qp.getValue(), qp.getType());
					}else{
						q.setParameterList(s, (Object[]) qp.getValue());
					}
				}else if(qp.getValue() instanceof Collection<?>){
					if(qp.getType() != null){
						q.setParameterList(s, (Collection<?>) qp.getValue(), qp.getType());
					}else{
						q.setParameterList(s, (Collection<?>) qp.getValue());
					}
				}else{
					if(qp.getType() != null){
						q.setParameter(s, qp.getValue(), qp.getType());
					}else{
						if(qp.getValue() instanceof Double){
							q.setDouble(s, (Double) qp.getValue());
						}else if(qp.getValue() instanceof Float){
							q.setFloat(s, (Float) qp.getValue());
						}else if(qp.getValue() instanceof Boolean){
							q.setBoolean(s, (Boolean) qp.getValue());
						}else{
							q.setParameter(s, qp.getValue());
						}
					}
				}
			}
		}

		List<?> result = q.list();
		trans.commit();
		int count = 0;
		if(result.size() < 1){
			count = 0;
		}else{
			try{
				count = ((Number) result.get(0)).intValue();
			}catch(Exception e){
				log.warn("error while countRecords," , e);
				count = 0;
			}
		}
		log.trace("countRecords found " + count + " records. '");
		return count;	
	}
	
	/**
	 * 执行一个hql语句，并返回结果
	 * @param hql
	 * @return
	 */
	public List<?> executeQuery(String hql){
		return executeQuery(hql, 0, 0);
	}
	public List<?> executeQuery(String hql, int start, int max){
		Session session = getSession();
		Transaction trans = null;
		trans = session.beginTransaction();
		Query q;
		q = session.createQuery(hql);
		if(start >= 0){
			q.setFirstResult(start);
		}
		if(max > 0){
			q.setMaxResults(max);
		}		
		List<?> result = q.list();
		trans.commit();
		return result;
	}
	/**
	 * 执行一个update/delete的hql语句 
	 * @param hql
	 * @return
	 */
	public int executeUpdate(String hql){
		Session session = getSession();
		Transaction trans = null;
		trans = session.beginTransaction();
		Query q;
		q = session.createQuery(hql);
		int ret = q.executeUpdate();
		trans.commit();
		return ret;
	}
	
	/**
	 * 捕获错误并显示打印日志和回滚事务
	 * @param tipInfo
	 *             错误提示信息
	 * @param ex
	 *             错误对象
	 * @param tran
	 *             事务对象
	 */
	protected void printEInfoRBack(String tipInfo, Exception ex, Transaction tran){
        if (log.isErrorEnabled()) {
            log.error(tipInfo, ex);
        }
        try {
            if (null != tran) {
                tran.rollback();
            }
        } catch (Exception e) {
            // ...
        }
    }
	
}
