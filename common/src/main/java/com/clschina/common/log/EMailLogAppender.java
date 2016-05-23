package com.clschina.common.log;

import java.io.IOException;
import java.net.SocketException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.hibernate.JDBCException;

import com.clschina.common.component.Login;
import com.clschina.common.component.ThreadLocalManager;

/**
 * TODO:注意：此类之对应 log4j 1.2.12版，在1.2.16版下会失效
 * @author GeXiangDong
 *
 */
public class EMailLogAppender extends SMTPAppender {
	private static Log log = LogFactory.getLog(EMailLogAppender.class);

	public EMailLogAppender(){
		if(log.isTraceEnabled()){
			log.trace("EMailLogAppender()");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.log4j.net.SMTPAppender#formatBody()
	 */
	@Override
	protected String formatBody() {
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
		buf.append("\r\n\r\n---cb.length()=" + cb.length() + "---(1)\r\n");
		try{
			for(int i=0; i<cb.length(); i++){
				LoggingEvent event = cb.get(i);
				if(event != null){
					Throwable t = null;
					if(event.getThrowableInformation() != null){
						t = event.getThrowableInformation().getThrowable();
					}

					if(t != null){
						if(t instanceof JDBCException){
							String sql = ((JDBCException) t).getSQL();
							if(sql != null && sql.length() > 0){
								buf.append("\r\n\r\n===================\r\n");
								buf.append(sql);
								buf.append("\r\n===================\r\n");
							}
						}else{
							buf.append("    Exception #" + i + ":" + t.getClass().getName() + "\r\n");
						}
					}
				}
			}
		}catch(Exception e){
			buf.append("ERROR:>>>>>>>>>>>>>>" + e);
			//为了防止error触发发邮件，进入死循环，此处不写log.error，改用打印堆栈
			e.printStackTrace();
			log.warn("", e);
		}
		
		buf.append("---cb.length()=" + cb.length() + "---(2)\r\n");
		String errorInfo = super.formatBody();
		
		buf.append("\r\n\r\nERROR Infomation:\r\n");
		buf.append(errorInfo);
		
		
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.log4j.net.SMTPAppender#createSession()
	 */
	@Override
	protected Session createSession() {
		final int SENDERNUM = 50;
		if (this.getSMTPPassword() != null) {
			int n = (int) (Math.random() * SENDERNUM) + 1;
			if (log.isTraceEnabled()) {
				log.trace("sendNum=" + SENDERNUM + "; randound n=" + n);
			}
			final String smtpUser;
			String user = this.getSMTPUsername();
			if (user != null && user.trim().length() != 0) {
				if (n == 1) {
					smtpUser = user;
				} else {
					smtpUser = MessageFormat.format(user, new Object[] { n });
				}
			} else {
				smtpUser = null;
			}
			String senderMail = this.getFrom();
			if (senderMail != null) {
				if (n == 1) {
					// senderMail = senderMail;
				} else {
					senderMail = MessageFormat.format(senderMail,
							new Object[] { n });
				}
			}
			this.setSMTPUsername(smtpUser);
			this.setFrom(senderMail);
		}
		return super.createSession();
	}


	/* (non-Javadoc)
	 * @see org.apache.log4j.net.SMTPAppender#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	public void append(LoggingEvent event) {
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
		Calendar c = Calendar.getInstance();
		if(c.get(Calendar.HOUR_OF_DAY) == 5 && c.get(Calendar.MINUTE) > 45){
		    //早上5:45～6：00之间不发错误提醒
		    return;
		}
		if(c.get(Calendar.HOUR_OF_DAY) == 6 && c.get(Calendar.MINUTE) < 15){
            //早上6：00～6：15之间不发错误提醒
		    return;
        }

		try {
			this.msg.setSubject(this.getSubject() + " " + event.getMessage());
		} catch (MessagingException e) {
			
		}

		super.append(event);
	}

}
