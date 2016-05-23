package com.clschina.common.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.clschina.common.component.Login;
import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.util.MD5;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Project;


/**
 * 把ERROR自动增加到redmine作为一个issue；归并相同的错误到同一个issue
 * [http://192.168.1.165/redmine/issues/405]
 * log4j中设置如下配置：
 * 
 * log4j.appender.REDMINE = com.clschina.common.log.RedmineIssueLogAppender
 * log4j.appender.REDMINE.Threshold=ERROR
 * log4j.appender.REDMINE.Uri = http://zb.clschina.com/redmine/
 * log4j.appender.REDMINE.ApiAccessKey = 84b55e2434516e2909fbf0db6fada056ad7ec5da
 * log4j.appender.REDMINE.ProjectKey = sandbox
 * log4j.appender.REDMINE.Subject=项目名称:
 * log4j.appender.REDMINE.IssueMd5FieldId = 1
 * log4j.appender.REDMINE.IssueMd5FieldName = issuemd5
 * 
 * 
 * @author gexiangdong
 *
 */
public class RedmineIssueLogAppender extends AppenderSkeleton {
	private static Log log = LogFactory.getLog(RedmineIssueLogAppender.class);

	private String redmineUri;
    private String redmineApiAccessKey;
    private String redmineProjectKey;
    private int redmineIssueMD5CustomerFieldId = 1;
    private String redmineIssueMD5CustomerFieldName = "issuemd5";
    private String redmineSubjectPrefix = "";
    
    
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	public void setSubject(String subject){
		redmineSubjectPrefix = subject;
	}
	public String getSubject(){
		return redmineSubjectPrefix;
	}
	
	public void setUri(String uri){
		redmineUri = uri;
	}
	public String getUri(){
		return redmineUri;
	}
	
	public void setApiAccessKey(String apiAccessKey){
		this.redmineApiAccessKey = apiAccessKey;
	}
	public String getApiAccessKey(){
		return this.redmineApiAccessKey;
	}
	
	public void setProjectKey(String projectKey){
		this.redmineProjectKey = projectKey;
	}
	public String getProjectKey(){
		return this.redmineProjectKey;
	}
	
	public void setIssueMd5FiledId(int fieldId){
		this.redmineIssueMD5CustomerFieldId = fieldId;
	}
	public int getIssueMd5FieldId(){
		return this.redmineIssueMD5CustomerFieldId;
	}
	
	public void setIssueMd5FiledName(String fieldName){
		this.redmineIssueMD5CustomerFieldName = fieldName;
	}
	public String getIssueMd5FieldName(){
		return this.redmineIssueMD5CustomerFieldName;
	}

	@Override
	protected void append(LoggingEvent event) {
		Throwable t = null;
		if(event.getThrowableInformation() != null){
			t = event.getThrowableInformation().getThrowable();
		}
		
		if(t != null && (t instanceof IOException || t instanceof SocketException)){
			if(t.getClass().getName().contains("ClientAbortException") ||
					(t.getMessage() != null && t.getMessage().contains("ClientAbortException"))){
				if(log.isInfoEnabled()){
					log.info("忽略ClientAbortException异常");
				}
				return;
			}
		}
		if(t == null){
			t = new RuntimeException("无异常关联的错误。【请同时改正此处log.error未关联异常的BUG】");
		}
		String issueMd5 = null;
		StringBuffer buf = new StringBuffer();
		buf.append(redmineProjectKey);
		StackTraceElement[]	stes = t.getStackTrace();
		int stackCount = 0;
		for(StackTraceElement ste : stes){
			buf.append(ste.getClassName());
			buf.append(ste.getFileName());
			buf.append(ste.getMethodName());
			buf.append(ste.getLineNumber());
			if(stackCount ++ > 30){
				//超过30个的堆栈不再继续追溯
				if(log.isInfoEnabled()){
					log.info("堆栈过深" + stes.length + ";放弃后面部分");
					break;
				}
			}
		}
		issueMd5 = new MD5().md5String(buf.toString());
		
		
		StringBuffer error = new StringBuffer();
		error.append(getRequestInformation());
		
		error.append("\r\n\r\nERROR: (" + event.getLevel()+ ") ");
		error.append(event.getMessage() + "; ");
		error.append(event.getRenderedMessage() + "; " + t.getMessage());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		t.printStackTrace(pw);
		pw.flush();
		pw.close();
		error.append("\r\n\r\n\r\n");
		//下面留几个空格是为了在redmine中显示成白底引用状态
		error.append("    " + baos.toString());
		error.append("    \r\n    \r\n");
		try {
			baos.close();
		} catch (IOException e) {
		}
		
		final String redmineIssueMd5 = issueMd5;
		final String redmineIssueSubject = redmineSubjectPrefix + "-" + event.getMessage();
		final String redmineIssueNote = error.toString();
		//创建一个新线程来保存到redmine，以免网络连接速度慢导致干扰主线程的用户响应。
		Runnable runnable = new Runnable(){

			@Override
			public void run() {
				RedmineManager mgr = RedmineManagerFactory.createWithApiKey(redmineUri, redmineApiAccessKey);
			    IssueManager issueManager = mgr.getIssueManager();
			    HashMap<String, String> map = new HashMap<String, String>();
			    //map.put("created_on", "2015-05-28");
			    map.put("status_id", "o");
			    map.put("cf_1", redmineIssueMd5);
			    map.put("author_id", "me");
			    
			    try{
				    List<Issue> issues = issueManager.getIssues(map);
				    if(issues.size() > 0){
				    	//已经创建过，在此基础上补充
				    	for(Issue issue : issues){
				    		issue.setNotes(redmineIssueNote);
				    		issueManager.update(issue);
				    	}
				    }else{
				    	//创建新issue
					    Project project = mgr.getProjectManager().getProjectByKey(redmineProjectKey);
					    Issue issueToCreate = IssueFactory.create(project.getId(), redmineIssueSubject);
					    CustomField cf = CustomFieldFactory.create(redmineIssueMD5CustomerFieldId, redmineIssueMD5CustomerFieldName, redmineIssueMd5);
					    issueToCreate.addCustomField(cf);
					    issueToCreate.setDescription(redmineIssueNote); 
					    issueManager.createIssue(issueToCreate);
					    
				    }
			    }catch(RedmineException re){
			    	//为防止进入死循环，此处不能用log4j记录日志
			    	re.printStackTrace();
			    }
			}
			
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	protected String getRequestInformation() {
		StringBuffer buf = new StringBuffer();
		
		try {
			HttpServletRequest request = ThreadLocalManager.getRequest();
			Login login = ThreadLocalManager.getLogin();
			buf.append("Date:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S").format(new Date()));
			buf.append("\r\n");
			buf.append("User: "
					+ (login == null ? "NULL" : login.getId() + ", "
							+ login.getName()));

			if (request != null) {
				buf.append("\r\nIP: " + request.getRemoteAddr());
				buf.append("\r\nBrowser: " + request.getHeader("User-Agent"));
				buf.append("\r\nURI: " + request.getRequestURI());
				buf.append("\r\nServletPath: " + request.getServletPath());
				buf.append("\r\nMethod: " + request.getMethod());

				buf.append("\r\n\r\nRequest Headers:");
				for (Enumeration<?> enum0 = request.getHeaderNames(); enum0
						.hasMoreElements();) {
					String key = (String) enum0.nextElement();
					buf.append("\r\n  ");
					buf.append(key);
					buf.append(": ");
					buf.append(request.getHeader(key));
				}
				buf.append("\r\n\r\n");

				buf.append("\r\nRequest Parameter Values:");
				for (Enumeration<?> enum1 = request.getParameterNames(); enum1
						.hasMoreElements();) {
					String key = (String) enum1.nextElement();
					buf.append("\r\n  ");
					buf.append(key);
					buf.append(": ");
					buf.append(request.getParameter(key));
				}
				buf.append("\r\n\r\n");

				buf.append("\r\nRequest Attribute Values:");
				for (Enumeration<?> enum1 = request.getAttributeNames(); enum1
						.hasMoreElements();) {
					String key = (String) enum1.nextElement();
					buf.append("\r\n  ");
					buf.append(key);
					buf.append(": ");
					buf.append(request.getAttribute(key));
				}
				buf.append("\r\n\r\n");

				HttpSession session = request.getSession();
				buf.append("\r\nSession Id:" + session.getId());
				for (Enumeration<?> enum1 = session.getAttributeNames(); enum1
						.hasMoreElements();) {
					String key = (String) enum1.nextElement();
					buf.append("\r\n  ");
					buf.append(key);
					buf.append(": ");
					buf.append(session.getAttribute(key));
				}
				buf.append("\r\n\r\n");
				
				Cookie[] cookies = request.getCookies();
				if(cookies != null){
                    buf.append("Cookies:\r\n");
				    for(int i=0; i<cookies.length; i++){
				        if(cookies[i] == null){
				            buf.append("\t#" + i  + " is null");
				            continue;
				        }
				        buf.append("\t#" + i + "  name=" + cookies[i].getName() + 
				                "; value=" + cookies[i].getValue() + "; maxAge=" + cookies[i].getMaxAge() +
				                "; domain=" + cookies[i].getDomain() + "; path=" + cookies[i].getPath() +
				                "; comment=" + cookies[i].getComment() + "; \r\n\t\t" + cookies[i].toString());
				        buf.append("\r\n");
				    }
	                buf.append("\r\n\r\n");
				}
			}

		} catch (Exception e) {
			//为了防止error触发发邮件，进入死循环，此处不写log.error，改用打印堆栈
			e.printStackTrace();
			log.warn("ERROR:", e);
		}
		
		return buf.toString();
	}

}
