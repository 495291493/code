package com.clschina.common.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.autorun.AutoRun;
import com.clschina.common.autorun.AutoRunThreadManager;
/**
 * 启动自动运行任务接口
 * @author user
 *
 */
public class AutoRunServlet extends HttpServlet {
	private static final Log log = LogFactory.getLog(AutoRunServlet.class);
	private static final long serialVersionUID = 73898829177949940L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String act = req.getParameter("act");
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/plain;charset=utf-8");
		if("start".equalsIgnoreCase(act)){
			//启动自动运行任务
//			String hostIp = req.getRemoteAddr();
			String clazz = req.getParameter("class");
			String respResult = "";
			if(clazz == null){
				log.error("单独启动自动运行程序出错，clazz is null, clazz["+ clazz +"]");
				return;
			}
			synchronized (AutoRunThreadManager.lock) {
				boolean flag = false;
				for(AutoRun run : AutoRunThreadManager.list){
					if(!run.getClass().getName().equals(clazz.trim())){
						continue;
					}
					
					if(run.isRunning()){
						respResult = "任务运行中";
					}else{
						//启动匹配的自动运行程序
						new Thread(run).start();
						run.setRunning(true);
						run.setProviousStartTime(Calendar.getInstance());
						respResult = "任务启动成功";
					}
					flag = true;
					break;
				}
				
				if(!flag){
					respResult = "未找到匹配的任务";
				}
			}
			
			resp.getWriter().print(respResult);
			
		}else if("query".equalsIgnoreCase(act)){
			//查询
			StringBuffer buf = new StringBuffer();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			buf.append("{\"list\": [");
			for(int i=0; i<AutoRunThreadManager.list.size(); i++){
				AutoRun run = AutoRunThreadManager.list.get(i);
				buf.append("{\"description\":\""+run.getDescription() +
						"\",\"class\":\"" +run.getClass().getName() + 
						"\",\"starttimes\":\"" +run.getStarts() + 
						"\",\"status\":\"" + (run.isRunning() ? AutoRunThreadManager.STATUS_RUNNING : AutoRunThreadManager.STATUS_STOPED) + 
						"\",\"pstarttime\":\""
						+ (run.getProviousStartTime() == null ? "从未运行" 
								: sdf.format(run.getProviousStartTime().getTime())) +"\"}");
				if(i+1 < AutoRunThreadManager.list.size()){
					buf.append(",");
				}
			}
			buf.append("]}");
			resp.getWriter().print(buf.toString());
		}
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
