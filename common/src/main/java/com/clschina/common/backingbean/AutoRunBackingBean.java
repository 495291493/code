package com.clschina.common.backingbean;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.autorun.AutoRun;
import com.clschina.common.autorun.AutoRunThreadManager;
import com.clschina.common.util.ContextUtil;
/**
 * 自动运行任务管理
 * @author DuChaoTing
 *
 */
public class AutoRunBackingBean {
	private static final Log log = LogFactory.getLog(AutoRunBackingBean.class);
	private List<Map<String, String>> autoRunTask;
	
	public AutoRunBackingBean(){
		
	}
	
	public List<AutoRun> getAllAutoRunTask(){
		return AutoRunThreadManager.list;
	}

	public List<Map<String, String>> getAutoRunTask() {
		if(autoRunTask == null){
			autoRunTask = AutoRunThreadManager.getTaskStatus();
		}
		return autoRunTask;
	}
	
	/**
	 * 手动运行指定的任务
	 * @return
	 */
	public String runTask(){
		String clazz = ContextUtil.findParameter("class");
		AutoRunThreadManager.startAutoRunThread(clazz);
		autoRunTask = AutoRunThreadManager.getTaskStatus();
		return null;
	}

	/**
	 * 手动运行任务
	 * @return
	 */
	public String run(){
		String autoid = ContextUtil.findParameter("id");
		if(log.isTraceEnabled()){
			log.trace("get autoid = ["+ autoid +"] ");
		}
		if(autoid != null && autoid.trim().length() > 0){
			List<AutoRun> l = AutoRunThreadManager.list;
			for (AutoRun autoRun : l) {
				String id = autoRun.getId();
				if(autoid.equals(id)){
					autoRun.setRunning(true);
					autoRun.setProviousStartTime(Calendar.getInstance());
					autoRun.setFirst(true);
					Thread t = new Thread(autoRun);
					t.start();
				}
			}
		}
		return null;
	}
}
