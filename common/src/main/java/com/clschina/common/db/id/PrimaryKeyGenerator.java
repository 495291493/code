package com.clschina.common.db.id;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.type.Type;

import com.clschina.common.db.HibernateSessionFactory;
import com.clschina.common.db.bean.IdAssignableDataEntry;
import com.clschina.common.util.CommonUtil;



/**
 * 自动增长的主键的生成。
 *
 */
public class PrimaryKeyGenerator implements Configurable, IdentifierGenerator {
	private static final Log log = LogFactory.getLog(PrimaryKeyGenerator.class);

	private String prefix;

	private boolean useServerId;

	private String key;

	private int length;

	private static String tableName, idColumn, catalogueColumn;

	private static String _siteId = null;
	private String query, update, insert;
	static {
		Properties ps = new Properties();
		try {
			ps.load(PrimaryKeyGenerator.class.getResourceAsStream("UniqueId.properties"));
			tableName = ps.getProperty("table.name");
			idColumn = ps.getProperty("table.idcolumn");
			catalogueColumn = ps.getProperty("table.cataloguecolumn");
		} catch (Exception e) {
			log.fatal("Error while init the PrimaryKeyGenerator.", e);
		}
	}
	
	private String getServerId(){
		return _siteId;
	}
	public void configure(Type type, Properties params, Dialect d) {
		prefix = params.getProperty("prefix");
		if (params.getProperty("usesiteid") != null){
			useServerId = CommonUtil.object2boolean(params.getProperty("usesiteid"));
		}
		if (params.getProperty("useserverid") != null){
			useServerId = CommonUtil.object2boolean(params.getProperty("useserverid"));
		}
		key = params.getProperty("key");
		prefix = prefix == null ? "" : prefix.trim();
		key = key == null ? "" : key.trim();
		if (key.length() == 0) {
			log.warn("Invalid parameter key to PrimaryKeyGenerator," + 
					" maybe somthing wrong in hbm file." + 
					"(Using the default key instead.");
		}
		length = CommonUtil.object2int(params.getProperty("length"));
		query = "select " + idColumn + " from " + tableName + " with (updlock) "  + " where "
				+ catalogueColumn + "=:key  " ;
		update = "update " + tableName + " set " + idColumn + "=:newvalue  where "
				+ idColumn + "=:oldvalue   and  " + catalogueColumn + "=:key";

	}

	protected int doWork() throws SQLException {
		if(log.isTraceEnabled()){
			log.trace(" dow wordk run.... ");
		}
		Session s = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction tran = null;
		int id = 1;

		try{
			tran = s.beginTransaction();
			Object o = s.createSQLQuery(query).setParameter("key", key).uniqueResult();
			if(o != null){
				//exists
				id = ((Number)o).intValue();
			}else{
				//not exists
				if(log.isTraceEnabled()){
					log.trace("excute sql["+ insert +"],key["+ key +"], id["+ id +"]");
				}
				insert = "insert into " + tableName + "(" + idColumn + ", "
						+ catalogueColumn + ") values(" + id + ", :key)";
				int cnt = s.createSQLQuery(insert).setParameter("key", key).executeUpdate();
				if(cnt == 0){
					throw new SQLException("excute sql:["+insert+"],result["+ cnt +"]");
				}
			}
			
			//最大值加1
			SQLQuery q = s.createSQLQuery(update);
			q.setParameter("key", key);
			q.setParameter("oldvalue", id);
			q.setParameter("newvalue", id+1);
			int cnt = q.executeUpdate();
			if(cnt != 1){
				throw new SQLException("excute sql:["+update+"] error, key[" + key +
						"],oldvalue["+ id +"],newvalue["+ (id + 1) +"],result["+ cnt +"]");
			}
			if(log.isTraceEnabled()){
				log.trace("excute sql["+ update +"],key["+ key +"], oldvalue["+ id +"]," +
						"newvalue["+ (id+1) +"]");
			}
			tran.commit();
		}catch(Exception ex){
			log.error("generator id error ", ex);
			if(tran != null){
				try {
					tran.rollback();
				} catch (Exception e) {
					log.error("error while rollback hibernate session.", e);
				}
			}
			throw new SQLException("Generator id error at doWork method ," + ex.getMessage(), ex);
		}finally{
			//关闭session
			try {
				if(s.isOpen()){
					s.close();
				}
			} catch (Exception e) {
				log.error("error while close hibernate session.", e);
			}
			try {
				if (s.isConnected()) {
					s.disconnect();
				}
			} catch (Exception e) {
				log.error("error while disconnect hibernate session", e);
			}
		}
		return id;
	}

	public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
		return new String[] {
				"create table " + tableName + " ( " + 
				catalogueColumn + " char(50) not null primary key, " + //dialect.getTypeName(Types.CHAR, 50, 50, 0) + ", " +
				idColumn + " " + dialect.getTypeName(Types.INTEGER) + " default 0" +
				" )",
			};
	}

	public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
		StringBuffer sqlDropString = new StringBuffer().append("drop table ");
		if ( dialect.supportsIfExistsBeforeTableName() ) sqlDropString.append("if exists ");
		sqlDropString.append(tableName).append( dialect.getCascadeConstraintsString() );
		if ( dialect.supportsIfExistsAfterTableName() ) sqlDropString.append(" if exists");
		return new String[] { sqlDropString.toString() };
	}

	public Object generatorKey() {
		return tableName;
	}



	public synchronized Serializable generate(SessionImplementor session, Object object)
		throws HibernateException {
		if(log.isTraceEnabled()){
			log.trace("generate(session, " + object.getClass().getName() + " " + object + ")");
		}
		if(object instanceof IdAssignableDataEntry){
			IdAssignableDataEntry iade = (IdAssignableDataEntry) object;
			Serializable s = iade.getAssignedId();
			if(s != null){
				return s;
			}
		}
		int len;
		if (useServerId) {
			len = length - getServerId().length() - prefix.length();
			if (len <= 0) {
				throw new IdentifierGenerationException(
						"the length of the generated is too small, must big than "
								+ (getServerId().length() + prefix.length()) + 
								" prefix=" + getServerId() + prefix);
			}
		} else {
			len = length - prefix.length();
			if (len <= 0) {
				throw new IdentifierGenerationException(
						"the length of the generated is too small, must big than "
								+ (prefix.length()) + 
								" prefix=" + getServerId());
			}
		}
		// TransactionManager transactionManager =
		// session.getFactory().getTransactionManager();
		int id = 0;
		try {
			id = doWork();
		} catch (Exception e) {
			log.error("error while generate next id.", e);
			throw new HibernateException(e);
		}

		if (id >= Math.pow(10, len)) {
			log.error("the identify primay key has reached the max value. key="
					+ key + "; length=" + length + "; now value=" + id);
			throw new IdentifierGenerationException(
					"the identify primay key has reached the max value. key="
							+ key + "; length=" + length + "; now value=" + id);
		}
		StringBuffer ret = new StringBuffer();
		if (useServerId) {
			ret.append(getServerId());
		}
		ret.append(prefix);
		String tmpId = String.valueOf(id);
		for (int i = 0; i < (len - tmpId.length()); i++) {
			ret.append("0");
		}
		ret.append(tmpId);
		if(log.isTraceEnabled()){
			log.trace("获得主键：[" + ret.toString() + "]");
		}
		return ret.toString();		
	}


}
