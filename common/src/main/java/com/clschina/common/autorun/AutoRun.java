package com.clschina.common.autorun;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.hibernate.Session;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.log.AutoRunLog;
import com.clschina.common.log.LogDAO;
/**
 * 自动运行线程父类
 * @author du
 *
 */
public abstract class AutoRun implements Runnable{
	private static final Log log = LogFactory.getLog(AutoRun.class);
	private String id;
	private boolean first = true;//是否第一次运行
	private Calendar proviousStartTime;//上一次运行时间
	private String description;
	private boolean srartRun = false;
	private boolean running = false;
	private List<Calendar> startTimes = new ArrayList<Calendar>();
	private String starts;
	/**
	 * 自动运行程序分类，可以从配置文件中读取；ID
	 */
	private String type;
	/**
	 * 运行结果
	 */
	private String runResult = "成功";
	
	
	/**
	 * 此方法执行线程需要处理的任务
	 */
	public abstract void excuteTask() throws Exception;
	
	public final void run(){
		if(this.getType() == null){
			this.setType(this.getClass().getSimpleName());
		}
		setRunning(true);
		AutoRunLog autolog = new AutoRunLog();
		autolog.setType(this.getType());
		try{
			excuteTask();
			autolog.setResult(this.runResult);
		} catch (Exception ex) {
			log.error("自动运行程序异常 name ["+ description +"],type["+ this.type +"]", ex);
			Writer result = new StringWriter();
			PrintWriter printWriter = new PrintWriter(result);
			ex.printStackTrace(printWriter);
			autolog.setSuccessful(false);
			autolog.setResult(result.toString());
		} finally{
			setRunning(false);
			LogDAO.instance().save(autolog);
			Session s = null;
			Object o = ThreadLocalManager.getInstance().get(ThreadLocalManager.HIBERNATE_SESSION);
			Object mybatisObj = ThreadLocalManager.getValue(ThreadLocalManager.MYBATIS_SESSION);
			if (o != null && o instanceof Session) {
				s = (Session) o;
			} 
			if (s != null) {
				if(s.isDirty()){
					log.error("发现有未提交/回滚的事务。", new RuntimeException("dirty session."));
				}
				try {
					if (s.isConnected()) {
						s.disconnect();
					}
				} catch (Exception e) {
					log.error("error while disconnect hibernate session", e);
				}
				try {
					if(s.isOpen()){
						s.close();
					}
				} catch (Exception e) {
					log.error("error while close hibernate session.", e);
				}

			}
			if(mybatisObj != null){
				try{
					((SqlSession)mybatisObj).close();
				}catch(Exception e){
					log.error("自动行动任务：关闭Mybatis SqlSession出错", e);
				}
			}
			ThreadLocalManager.getInstance().clear();
		}
	}
	/**
	 * 线程是否运行中
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * 是否第一次运行
	 * @return
	 */
	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}
	/**
	 * 上一次运行时间点
	 * @return
	 */
	public Calendar getProviousStartTime() {
		return proviousStartTime;
	}

	public void setProviousStartTime(Calendar proviousStartTime) {
		this.proviousStartTime = proviousStartTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 说明
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSrartRun() {
		return srartRun;
	}

	public void setSrartRun(boolean srartRun) {
		this.srartRun = srartRun;
	}

	public List<Calendar> getStartTimes() {
		return this.startTimes;
	}

	public void setStartTimes(List<Calendar> startTimes) {
		this.startTimes = startTimes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRunResult() {
		return runResult;
	}

	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}

	public String getStarts() {
		return starts;
	}

	public void setStarts(String starts) {
		this.starts = starts;
	}
}
