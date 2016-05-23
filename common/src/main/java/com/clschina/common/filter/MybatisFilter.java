package com.clschina.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.MybatisSessionFactory;

/**
 * 用于保持Mybatis的Session，在同一个request内，共用同一个Mybatis session
 */
public class MybatisFilter implements Filter {

	private final static Log log = LogFactory.getLog(MybatisFilter.class);

	/**
	 * 初始化过程，会创建Mybatis session factory
	 */
	public void init(FilterConfig config) throws ServletException {
		MybatisSessionFactory.init();
	}

	/**
	 * 销毁
	 */
	public void destroy() {
	    if(log.isTraceEnabled()){
	        log.trace("destroy MybatisFilter....");
	    }
	}

	/**
	 * 处理以下：
	 * 1、把request response mybatis_session等放入ThreadLocalManager
	 * 2、等结束后，检查是否有mybatis session，如果有关闭。
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(log.isTraceEnabled()){
			log.trace("enter Mybatis filter...");
		}

		ThreadLocalManager tlm = ThreadLocalManager.getInstance();
		try {
			tlm.set(ThreadLocalManager.MYBATIS_SESSION, MybatisSessionFactory.getSqlSessionFactory().openSession());
			chain.doFilter(request, response);
		} finally {
			if(log.isTraceEnabled()){
				log.trace("try to close mybatis session if exists.");
			}
			SqlSession s = null;
			Object o = tlm.get(ThreadLocalManager.MYBATIS_SESSION);
			if (o != null) {
				s = (SqlSession) o;
				s.close();
			}
			tlm.set(ThreadLocalManager.MYBATIS_SESSION, null);
			tlm.clear();
		}
		

	}


}
