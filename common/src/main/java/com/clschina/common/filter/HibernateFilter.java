package com.clschina.common.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;

import com.clschina.common.autorun.AutoRunThreadManager;
import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.HibernateSessionFactory;

/**
 * 用于保持Hibernate的Session，在同一个request内，共用同一个hibernate session
 */
public class HibernateFilter implements Filter {
    private final static Log log = LogFactory.getLog(HibernateFilter.class);
    private FilterConfig filterConfig;
    private AutoRunThreadManager autoMgr;
    private boolean debug = false;

    /**
     * 初始化过程，会创建hibernate session factory
     */
    public void init(FilterConfig config) throws ServletException {
        filterConfig = config;
        ThreadLocalManager.setContextFolder(filterConfig.getServletContext().getRealPath("/"));
        HibernateSessionFactory.init(config.getServletContext().getRealPath("/WEB-INF/classes"));
        String mirror = System.getProperty("mirror");// mirror环境变量在tomcat启动配置中设置
        if (mirror != null && mirror.trim().equalsIgnoreCase("true")) {
            // 如果是镜像服务器则返回，不初始化自动运行任务
            if (log.isTraceEnabled()) {
                log.trace("当前服务为镜像服务。");
            }
            return;
        }
        if ("true".equalsIgnoreCase(config.getInitParameter("debug"))) {
            debug = true;
        }
        if (log.isTraceEnabled()) {
            log.trace("当前服务不是镜像服务，开始启动自动运行任务线程。");
        }
        // 不是镜像服务器，启动自动运行任务
        InputStream is = null;
        try {
            String f = config.getServletContext().getRealPath("/WEB-INF/autostart.xml");
            is = new FileInputStream(f);
        } catch (Exception e) {
            log.error("Error while find autostart.xml.", e);
            return;
        }
        // 启动自动运行管理线程
        autoMgr = new AutoRunThreadManager(is);
        autoMgr.setName("AutoRunThreadManager-initfromHibernateFilter");
        autoMgr.start();
        try {
            is.close();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("close inputstream error ", e);
            }
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (log.isTraceEnabled()) {
            log.trace("destroy HibernateFilter....");
        }
        filterConfig = null;
        HibernateSessionFactory.getSessionFactory().close();
        if (autoMgr != null) {
            autoMgr.interrupt();
        }
    }

    /**
     * 处理以下： 1、检查是否包含文件上传，如果包含文件上传，应用MultipartRequestWrapper转换request 2、把request
     * response hibernate_session等放入ThreadLocalManager 3、等结束后，检查是否有Hibernate
     * session，如果有关闭。
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (log.isTraceEnabled()) {
            log.trace("enter hibernate filter...");
        }
        long startTimeMillis = 0;
        if (log.isTraceEnabled()) {
            startTimeMillis = System.currentTimeMillis();
        }
        ThreadLocalManager tlm = ThreadLocalManager.getInstance();
        SessionFactory factory = HibernateSessionFactory.getSessionFactory();
        Statistics statistics = factory.getStatistics();
        statistics.clear();
        statistics.setStatisticsEnabled(true);
        long t = statistics.getStartTime();
        try {
            tlm.set(ThreadLocalManager.REQUEST, request);
            tlm.set(ThreadLocalManager.RESPONSE, response);
            ServletRequest wrapper = request;
            tlm.set(ThreadLocalManager.HIBERNATE_SESSION, HibernateSessionFactory.getSessionFactory().openSession());
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) request;
                ThreadLocalManager.setContextFolder(filterConfig.getServletContext().getRealPath("/"));
                if (ServletFileUpload.isMultipartContent(req)) {
                    wrapper = new MultipartRequestWrapper(req, ThreadLocalManager.getContextFolder());
                    tlm.set(ThreadLocalManager.REQUEST, wrapper);
                }
            }
            chain.doFilter(wrapper, response);
        } finally {
            if (log.isTraceEnabled()) {
                log.trace("finally statement in InitThreadLocalFilter, " + "try to close hibernate session if exists.");
            }
            Session s = null;
            Object o = tlm.get(ThreadLocalManager.HIBERNATE_SESSION);
            if (o instanceof Session && o != null) {
                s = (Session) o;
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("ThreadLocalManager.HIBERNATE_SESSION is not "
                            + "instanceof of hibernate.session. it is " + ((o == null) ? "null" : o.getClass().getName()));
                }
            }
            if (s != null) {
                if (s.isDirty()) {
                    try {
                        Transaction transaction = s.getTransaction();
                        
                        if (transaction.isActive() && !transaction.wasCommitted() && !transaction.wasRolledBack()) {
                        	if(log.isWarnEnabled()){
                        		log.warn("发现有未提交/回滚的事务。", new RuntimeException("dirty session."));
                        	}
                            s.getTransaction().rollback();
                        }
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error("error while rollback dirty session.", e);
                        }
                    }
                }
                try {
                    if (s.isOpen()) {
                        s.close();
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("error while close hibernate session.", e);
                    }
                }
                try {
                    if (s.isConnected()) {
                        s.disconnect();
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("error while disconnect hibernate session", e);
                    }
                }
            }
            tlm.set(ThreadLocalManager.HIBERNATE_SESSION, null);
            tlm.clear();
        }
        if (log.isTraceEnabled()) {
            /*String url = ((HttpServletRequest) request).getServletPath();
            long cost = System.currentTimeMillis() - startTimeMillis;
            String contentType = response.getContentType();
            log.trace("cost " + cost + "ms. " + url + " contentType=" + contentType);
            PrintWriter writer = response.getWriter();
//            || url.endsWith(".shtml")
            if (url.endsWith(".xhtml") && contentType != null && contentType.startsWith("text/html")) {
                String styleName = null;
                if (cost > 700) {
                    styleName = "haoshi-long";
                } else {
                    styleName = "haoshi-short";
                }
                writer.write("<p class='" + styleName + "'>耗时" + cost + "毫秒 " + (cost > 1000 ? "，程序需要继续优化。" : "")
                        + "</p>");
                if (debug) {
                    writer.write("<table  class='infotbl'>");
                    writer.write("<tr><td class='group' colspan='2'>Global Infomation</td></tr>");
                    writer.write("<tr><td>getCloseStatementCount()</td><td>" + statistics.getCloseStatementCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getSessionCloseCount() </td><td>" + statistics.getSessionCloseCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getSessionOpenCount()</td><td>" + statistics.getSessionOpenCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getStartTime() </td><td>t=" + new Date(t) + "</td></tr>");
                    writer.write("<tr><td>getSuccessfulTransactionCount() </td><td>"
                            + statistics.getSuccessfulTransactionCount() + "</td></tr>");
                    writer.write("<tr><td>getTransactionCount() </td><td>" + statistics.getTransactionCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getConnectCount()</td><td>" + statistics.getConnectCount() + "</td></tr>");
                    writer.write("<tr><td>getFlushCount() </td><td>" + statistics.getFlushCount() + "</td></tr>");
                    writer.write("<tr><td>getOptimisticFailureCount()</td><td>"
                            + statistics.getOptimisticFailureCount() + "</td></tr>");
                    writer.write("<tr><td>getPrepareStatementCount()</td><td>" + statistics.getPrepareStatementCount()
                            + "</td></tr>");
                    writer.write("<tr><td class='group' colspan='2'>Collection Infomation</td></tr>");
                    writer.write("<tr><td>getCollectionFetchCount() </td><td>" + statistics.getCollectionFetchCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getCollectionLoadCount() </td><td>" + statistics.getCollectionLoadCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getCollectionRecreateCount() </td><td>"
                            + statistics.getCollectionRecreateCount() + "</td></tr>");
                    writer.write("<tr>    <td>getCollectionRemoveCount() </td><td>"
                            + statistics.getCollectionRemoveCount() + "</td></tr>");
                    writer.write("<tr><td>getCollectionUpdateCount()  </td><td>"
                            + statistics.getCollectionUpdateCount() + "</td></tr>");
                    writer.write("<tr><td>getCollectionRoleNames()  </td><td><table class='childtbl'>");
                    writer.write("<thead><tr><th>Collection Name</th>");
                    writer.write("<th>Fetch Count</th><th>Load Count</th><th>Recreate Count</th>");
                    writer.write("<th>Remove Count</th><th>Update Count</th></tr>");
                    writer.write("</thead>");
                    writer.write("<tbody>");
                    String[] cnames = statistics.getCollectionRoleNames();
                    for (int i = 0; i < cnames.length; i++) {
                        CollectionStatistics cs = statistics.getCollectionStatistics(cnames[i]);
                        writer.write("<tr>");
                        writer.write("<td class='name'>" + cnames[i] + "</td>");
                        writer.write("<td>" + cs.getFetchCount() + "</td>");
                        writer.write("<td>" + cs.getLoadCount() + "</td>");
                        writer.write("<td>" + cs.getRecreateCount() + "</td>");
                        writer.write("<td>" + cs.getRemoveCount() + "</td>");
                        writer.write("<td>" + cs.getUpdateCount() + "</td>");
                        writer.write("</tr>");
                    }
                    writer.write("</tbody>");
                    writer.write("</table></td></tr>");
                    writer.write("<tr><td class='group' colspan='2'>Entity Infomation</td></tr>");
                    writer.write("<tr><td>getEntityDeleteCount() </td><td>" + statistics.getEntityDeleteCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getEntityFetchCount() </td><td>" + statistics.getEntityFetchCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getEntityInsertCount() </td><td>" + statistics.getEntityInsertCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getEntityLoadCount() </td><td>" + statistics.getEntityLoadCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getEntityUpdateCount() </td><td>" + statistics.getEntityUpdateCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getEntityNames() </td><td><table class='childtbl'>");
                    writer.write("<thead><tr><th>Entity Name</th>");
                    writer.write("<th>Delete Count</th><th>Fetch Count</th><th>Insert Count</th>");
                    writer.write("<th>Load Count</th><th>Optimistic Failure Count</th><th>Update Count</th></tr>");
                    writer.write("</thead>");
                    writer.write("<tbody>");
                    String[] names = statistics.getEntityNames();
                    for (int i = 0; i < names.length; i++) {
                        EntityStatistics es = statistics.getEntityStatistics(names[i]);
                        writer.write("<tr>");
                        writer.write("<td class='name'>" + names[i] + "</td>");
                        writer.write("<td>" + es.getDeleteCount() + "</td>");
                        writer.write("<td>" + es.getFetchCount() + "</td>");
                        writer.write("<td>" + es.getInsertCount() + "</td>");
                        writer.write("<td>" + es.getLoadCount() + "</td>");
                        writer.write("<td>" + es.getOptimisticFailureCount() + "</td>");
                        writer.write("<td>" + es.getUpdateCount() + "</td>");
                        writer.write("</tr>");
                    }
                    writer.write("</tbody>");
                    writer.write("</table></td></tr>");
                    writer.write("<tr><td class='group' colspan='2'>Querys Infomation</td></tr>");
                    writer.write("<tr><td>getQueries() </td><td><table class='childtbl'>");
                    writer.write("<thead><tr><th>Query</th>");
                    writer.write("<th>Cache Hit Count</th><th>Cache Miss Count</th><th>Cache Put Count</th>");
                    writer.write("<th>平均执行时长</th><th>执行次数</th><th>最大执行时长</th>");
                    writer.write("<th>最小执行时长</th><th>执行影响行数</th></tr>");
                    writer.write("</thead>");
                    writer.write("<tbody>");
                    String[] queries = statistics.getQueries();
                    for (int i = 0; i < queries.length; i++) {
                        QueryStatistics qs = statistics.getQueryStatistics(queries[i]);
                        writer.write("<tr>");
                        writer.write("<td class='name'>" + queries[i] + "</td>");
                        writer.write("<td>" + qs.getCacheHitCount() + "</td>");
                        writer.write("<td>" + qs.getCacheMissCount() + "</td>");
                        writer.write("<td>" + qs.getCachePutCount() + "</td>");
                        writer.write("<td>" + qs.getExecutionAvgTime() + "</td>");
                        writer.write("<td>" + qs.getExecutionCount() + "</td>");
                        writer.write("<td>" + qs.getExecutionMaxTime() + "</td>");
                        writer.write("<td>" + qs.getExecutionMinTime() + "</td>");
                        writer.write("<td>" + qs.getExecutionRowCount() + "</td>");
                        writer.write("</tr>");
                    }
                    writer.write("</tbody>");
                    writer.write(" </table>");
                    writer.write("</td></tr>");
                    writer.write("<tr><td>getQueryCacheHitCount() </td><td>" + statistics.getQueryCacheHitCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getQueryCacheMissCount() </td><td>" + statistics.getQueryCacheMissCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getQueryCachePutCount() </td><td>" + statistics.getQueryCachePutCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getQueryExecutionCount() </td><td>" + statistics.getQueryExecutionCount()
                            + "</td></tr>");
                    writer.write("<tr><td>getQueryExecutionMaxTime() </td><td>" + statistics.getQueryExecutionMaxTime()
                            + "ms</td></tr>");
                    writer.write("<tr><td>getQueryExecutionMaxTimeQueryString() </td><td>"
                            + statistics.getQueryExecutionMaxTimeQueryString() + "</td></tr>");
                    writer.write(" <tr><td>getSecondLevelCacheHitCount() </td><td>"
                            + statistics.getSecondLevelCacheHitCount() + "</td></tr>");
                    writer.write("<tr><td>getSecondLevelCacheMissCount() </td><td>"
                            + statistics.getSecondLevelCacheMissCount() + "</td></tr>");
                    writer.write("<tr><td>getSecondLevelCachePutCount() </td><td>"
                            + statistics.getSecondLevelCachePutCount() + "</td></tr>");
                    writer.write("<tr><td>getSecondLevelCacheRegionNames() </td><td>"
                            + Arrays.toString(statistics.getSecondLevelCacheRegionNames()) + "</td></tr>");
                    writer.write("</tbody>");
                    writer.write("</table>");
                }
            }*/
        }
    }
}
