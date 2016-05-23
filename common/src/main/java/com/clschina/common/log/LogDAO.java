package com.clschina.common.log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.clschina.common.component.Login;
import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.DAOBase;
import com.clschina.common.db.QueryParameter;
import com.clschina.common.util.DateUtil;

public class LogDAO extends DAOBase{
	private static LogDAO autoDAO = new LogDAO();
	public static final String AUTORUNLOG_DATA_ENTRY = AutoRunLog.class.getName();
	public static final String OPERATELOG_DATA_ENTRY = OperateLog.class.getName();
	
	public static LogDAO instance () {
		return autoDAO;
	}
	
	/**
	 * 保存日志
	 * @param autolog
	 */
	public void save(AutoRunLog autolog) {
		super.save(AUTORUNLOG_DATA_ENTRY, autolog);
	}
	
	/**
	 * 查询最近的运行成功的日志
	 * @param type
	 * @return
	 */
	public AutoRunLog findLastAutorunLogByType(String type){
		Session s = getSession();
		String hql = "from " + AUTORUNLOG_DATA_ENTRY 
		+ " where type=:type and successful=true order by time desc";
		Query q = s.createQuery(hql);
		q.setParameter("type", type);
		Object o = q.setMaxResults(1).uniqueResult();
		return o == null ? null : (AutoRunLog)o;
	}
	
	/**
	 * 查询最近的运行成功的日志
	 * @param type
	 * @return
	 */
	public AutoRunLog findLastAutorunLogByType(String type, Calendar start){
		DateUtil.dayTimeBegin(start);
		Session s = getSession();
		String hql = "from " + AUTORUNLOG_DATA_ENTRY 
		+ " where type=:type and successful=true and time>=:start  order by time desc";
		Query q = s.createQuery(hql);
		q.setParameter("type", type);
		q.setParameter("start", start);
		Object o = q.setMaxResults(1).uniqueResult();
		return o == null ? null : (AutoRunLog)o;
	}
	 
	
	/**
	 * 在同一事务中保存操作日志
	 * @param msg
	 * @param pro
	 */
	public void logInCurrentTransaction(String msg, String pro) {
		getSession().save(OPERATELOG_DATA_ENTRY, createOperateLog(msg, pro));
	}
	
	/**
	 * 保存操作日志
	 * @param msg
	 * @param pro
	 */
	public void log(String msg, String pro) {
		super.save(OPERATELOG_DATA_ENTRY, createOperateLog(msg, pro));
	}
	/**
	 * 保存操作日志
	 * @param msg
	 * @param pro
	 */
	public void log(String msg, String pro,Session session) {
		super.save(session,OPERATELOG_DATA_ENTRY, createOperateLog(msg, pro));
	}
	public OperateLog createOperateLog(){
		return new OperateLog();
	}
	
	/**
	 * 根据ID在数据库表operate_log中查找对象
	 * @param id
	 * @return
	 */
	public OperateLog findOperateLogById(String id) {
		OperateLog op = null;
		Object o = super.findById(OPERATELOG_DATA_ENTRY, id);
		if (o != null) {
			op = (OperateLog) o;
		}
		return op;
	}
	
	/**
	 * 统计数据库中有多少条OperateLog记录。
	 * 
	 * @return
	 */
	public int getOperateLogCount() {
		ArrayList<QueryParameter> params = new ArrayList<QueryParameter>();
		return super.countRecords(OPERATELOG_DATA_ENTRY, params);
	}
	
	/**
	 * 查询JobMain列表，
	 * 
	 * @param startRow
	 *            开始记录
	 * @param maxRows
	 *            最多查询多少条记录
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OperateLog> listOperateLog(int startRow, int maxRows) {
		ArrayList<QueryParameter> params = new ArrayList<QueryParameter>();
		List<?> list = super.queryTable(OPERATELOG_DATA_ENTRY, params, "operatorDate desc", startRow, maxRows);
		return (List<OperateLog>) list;
	}
	
	private OperateLog createOperateLog(String msg, String pro){
		Login login = ThreadLocalManager.getLogin();
		String operator = login == null ? "system" : login.getId();
		OperateLog log = new OperateLog();
		log.setMessage(msg);
		log.setProgram(pro);
		log.setOperator(operator);
		return log;
	}
	
	/**
	 * 日志记录的程序（可用于选择的下拉列表）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> listProgram() {	
		Session s = getSession();
		String hql = "select program from " + OPERATELOG_DATA_ENTRY
				+ " group by program ";
		Query q = s.createQuery(hql);
		List<String> result = q.list();
		return result;
	}
	/**
	 * 统计数据库中有多少条OperateLog记录。
	 * 
	 * @return
	 */
	public int getOperateLogCount(String searchTitle,String searchMessage) {						
			Query q = getOperateLogQuery(searchTitle, searchMessage, "select count(*) ", null);
			return ((Number) q.uniqueResult()).intValue();			
	}
	
	/**
	 * 查询OperateLog列表，
	 * @param startRow
	 * @param maxRows
	 * @param searchTitle
	 * @param searchMessage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OperateLog> listOperateLog(int startRow, int maxRows,String searchTitle,String searchMessage) {			
		Query q = getOperateLogQuery(searchTitle, searchMessage, "", " operatorDate desc ");
			if (startRow > 0) {
				q.setFirstResult(startRow);
			}
			if (maxRows > 0) {
				q.setMaxResults(maxRows);
			}
			List<OperateLog> result = (List<OperateLog>) q.list();
			return result;		
	}	
	
	private Query getOperateLogQuery(String searchTitle,String searchMessage,String hqlPrefix, String orderBy){
		Session s = getSession();
		StringBuffer hql = new StringBuffer();
		hql.append(hqlPrefix);
		hql.append(" from ");
		hql.append(OPERATELOG_DATA_ENTRY);
		hql.append(" where 0=0 ");
		if (searchTitle != null && !"".equals(searchTitle)) {
			hql.append(" and program like :program ");
		}
		if (searchMessage != null && searchMessage.trim().length() > 0) {
			hql.append(" and message like :message ");
		}
		if (orderBy != null && orderBy.length() > 0) {
			hql.append(" order by ");
			hql.append(orderBy);
		}
		Query q = s.createQuery(hql.toString());
		if (searchTitle != null && !"".equals(searchTitle)) {
	    	q.setParameter("program" , "%"+searchTitle.trim() + "%");
		}
		if (searchMessage != null && searchMessage.trim().length() > 0) {
			q.setParameter("message" , "%"+searchMessage.trim() + "%");
		}
		return q;
	}
	
}
