package com.clschina.common.autorun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.util.DateUtil;
import com.clschina.common.util.DomUtil;
import com.clschina.common.util.SMTPThread;
/**
 * 自动运行线程管理类
 * @author du
 *
 */
public class AutoRunThreadManager extends Thread{
    SMTPThread smtpThread;
    public static final String STATUS_RUNNING = "运行中";
    public static final String STATUS_STOPED = "已停止";
	public static Object lock = new Object();
	private static Log log = LogFactory.getLog(AutoRunThreadManager.class); 
	private boolean first = true;
	public static List<AutoRun> list = new ArrayList<AutoRun>();
	/**
	 * 多少分钟之内可以运行线程(30*1000)
	 */
	public static long TIME = 30*1000;
	/**
	 * 多少毫秒之后可再次运行5*60*1000
	 */
	public static long TIMEOUT = 5*60*1000;
	/**
	 * 线程等待时间2*60*1000
	 */
	public static long WAIT_TIME = 15*1000; 
	
	public AutoRunThreadManager(InputStream is){ 
		Element root = DomUtil.loadDocument(is);		
		NodeList nodes = root.getElementsByTagName("task");
		for (int loop = 0; loop < nodes.getLength(); loop++) {
			Node node = nodes.item(loop);
			if (node != null) {
				String cls = DomUtil.getNodeAttribute(node, "class");
				if(cls != null){
					try{
						String starts = DomUtil.getNodeAttribute(node, "start");
						String[] startAry = starts.split(",");
						String id = DomUtil.getNodeAttribute(node, "id");
						String description = DomUtil.getNodeAttribute(node, "description");
						String startRun = DomUtil.getNodeAttribute(node, "startrun");

						Object o = Class.forName(cls).newInstance();
						if (o == null || !(o instanceof AutoRun)) {
							continue;
						}
						AutoRun run = (AutoRun) o;
						for(int i=0; i<startAry.length; i++) {
							if(startAry[i].trim().length() > 0){
								Calendar startTime = Calendar.getInstance();
								DateUtil.dayTimeBegin(startTime);
								String[] times = startAry[i].split(":");
								startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0]));
								if(times.length > 1){
									startTime.set(Calendar.MINUTE, Integer.parseInt(times[1]));
								}
								run.getStartTimes().add(startTime);
							}
						}
						
						if(id != null){
							run.setId(id);
						} else {
							run.setId("" + loop);
						}
						
						if(description != null){
							run.setDescription(description);
						}
						if(startRun != null && startRun.trim().equals("true")){
							run.setSrartRun(true);
						}
						run.setStarts(starts);
						run.setType(id==null ? run.getClass().getSimpleName() : id);
						list.add((AutoRun)o);
					}catch(Exception e){
						if(log.isErrorEnabled()){
							log.error("Error " + cls + "'", e);
						}
					}
				}
			}
		}
		this.setName("AutoRunThreadManager");
	}

	
	@Override
	public void run() {
		//启动发邮件的队列线程
		try{
		    smtpThread = SMTPThread.getInstance();
			if(smtpThread.getState() == Thread.State.NEW){
			    smtpThread.start();
        	}
		}catch(Exception e){
			log.error("启动邮件队列出错", e);
		}
		
		if(list.size() == 0){
			if(log.isWarnEnabled()){
				log.warn("auto run task is null, manager thread is stop .");
			}
			return;
		}
		
		while (!this.isInterrupted()) {
			synchronized (AutoRunThreadManager.lock) {
				for (int i = 0; i < list.size(); i++) {
					AutoRun run = list.get(i);
					boolean isRunning = run.isRunning();
					if(isRunning) {
						//线程运行中
						continue; 
					}
					if(first && run.isSrartRun()){
						run.setProviousStartTime(Calendar.getInstance());
						run.setRunning(true);
						Thread thread = new Thread(run);
						thread.setName("AutoRun[" + run.getId() +", " + run.getDescription() + "]");
						thread.start();
						continue;
					}
					//********************
					Calendar current = Calendar.getInstance();
					Calendar pStartTime = run.getProviousStartTime();

					if(pStartTime == null 
							|| current.getTimeInMillis() - pStartTime.getTimeInMillis() >= TIMEOUT){
						//已经超过规定时间，可再次运行
						for(Calendar qd : run.getStartTimes()){
							qd.set(Calendar.YEAR, current.get(Calendar.YEAR));
							qd.set(Calendar.MONTH, current.get(Calendar.MONTH));
							qd.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
							//当前时间与启动时间的差值
							long cz = current.getTimeInMillis() - qd.getTimeInMillis();
							
							if(cz >=0 && cz < TIME){
								//达到运行启动条件
								run.setProviousStartTime(current);
								run.setRunning(true);
								Thread thread = new Thread(run);
								thread.start();
								break;
							}
						}
					}
				}
				
				first = false;
			}
				
			try{
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
                if(log.isInfoEnabled()){
                    log.info("InterruptedException, will exit", e);
                }
                this.interrupt();
                break;
            }
		}
		if(log.isDebugEnabled()){
		    log.debug("AutoRunThreadManager exits.");
		}
	}

	/**
	 * 单独启动一个自动运行任务
	 */
	public static void startAutoRunThread(String className) {
		String httpurl = ThreadLocalManager.getInstance().getConfigProperties().getProperty("autorunUrl");
		URL url = null;
		HttpURLConnection con = null;
		try {
			url = new URL(httpurl + "?act=start&class=" + className);
			con = (HttpURLConnection) url.openConnection();
			con.getInputStream();
		} catch (Exception ex) {
			log.error("启动自动运行任务出错", ex);
		}finally{
			con.disconnect();
		}
	}
	
	/**
	 * 查询指定任务的运行状态
	 * @param className
	 * @return
	 */
	public static String getTaskStatusByClassName(String className){
		String status = null;
		List<Map<String, String>> statusList = getTaskStatus();
		for(Map<String, String> m : statusList){
			if(m.get("class").equalsIgnoreCase(className.trim())){
				status = m.get("status");
			}
		}
		
		if(status == null){
			throw new RuntimeException("未找到自动运行任务["+ className +"]");
		}else{
			return status;
		}
	}
	
	
	/**
	 * 查询自动运行任务
	 * @throws IOException 
	 */
	public static String queryAutoRunTaskStatus() throws IOException{
		String httpurl = ThreadLocalManager.getInstance().getConfigProperties().getProperty("autorunUrl");
		StringBuffer buf = new StringBuffer();
		URL url = new URL(httpurl + "?act=query");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
		String temp;
		while ((temp = br.readLine()) != null) {
			buf.append(temp);
		}
		return buf.toString();
	}
	
	/**
	 * 返回所有自动运行任务的状态
	 * @return
	 */
	public static List<Map<String, String>> getTaskStatus(){
		List<Map<String, String>> autoRunTask = new ArrayList<Map<String,String>>();
		String str = null;
		try{
			str = AutoRunThreadManager.queryAutoRunTaskStatus();
			JSONObject json = new JSONObject(str);
			JSONArray array = json.getJSONArray("list");
			for(int i=0; i<array.length(); i++){
				JSONObject obj = array.getJSONObject(i);
				Map<String, String> m = new HashMap<String, String>();
				m.put("description", obj.getString("description"));
				m.put("class", obj.getString("class"));
				m.put("classname", obj.getString("class"));
				m.put("starttimes", obj.getString("starttimes"));
				m.put("status", obj.getString("status"));
				m.put("pstarttime", obj.getString("pstarttime"));
				autoRunTask.add(m);
			}
		}catch(Exception ex){
			log.error("查询自动运行任务出错json["+ str +"]", ex);
		}
		return autoRunTask;
	}
	


	@Override
    public void interrupt(){
	    if(log.isTraceEnabled()){
	        log.trace("interrupt AutoRunThreadManager.........");
	    }
	    try{
	        smtpThread.interrupt();
	    }catch(Exception e){
	        log.warn("Error while interrupt SMTPThread.", e);
	    }
        super.interrupt();
    }

}
