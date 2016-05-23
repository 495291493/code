package com.clschina.common.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.clschina.common.component.ThreadLocalManager;

public class MybatisBase {
	private static final Log log = LogFactory.getLog(MybatisBase.class);
	/**
	 * 取得一个Mytatis的 SqlSession
	 * @return
	 */
	protected SqlSession getSession() {
		Object obj = ThreadLocalManager.getValue(ThreadLocalManager.MYBATIS_SESSION);
		if( obj != null){
			return (SqlSession)obj;
		}else{
			if(log.isInfoEnabled()){
				log.info("cannot found Mybatis session from ThreadLocalManager, create new.");
			}
			SqlSession s = MybatisSessionFactory.getSqlSessionFactory().openSession();
			ThreadLocalManager.setValue(ThreadLocalManager.MYBATIS_SESSION, s);
			return s;
		}
	}
}
