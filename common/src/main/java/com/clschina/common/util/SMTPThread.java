package com.clschina.common.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.JDBCException;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;

import com.clschina.common.db.HibernateSessionFactory;


public class SMTPThread extends Thread {
	private static Log log = LogFactory.getLog(SMTPThread.class);

	private static SMTPThread thread = new SMTPThread();
	private boolean shouldRestart = false;
	private boolean wakeUpMe = false;
	
	private String sender = "";
    private String smtpHost = "";
    private String user = "";
    private int sendNum = 0;
    private String password = "";
    private final int MAXRETRY = 5;


	private SMTPThread(){
        try {
            InputStream is = this.getClass().getResourceAsStream("/smtp.properties");
            if(is == null){
                is = getClass().getResourceAsStream("MailServer.properties");
            }
            Properties prop = new Properties();
            prop.load(is);

            sender = prop.getProperty("sender");
            smtpHost = prop.getProperty("smtphost");
            user = prop.getProperty("user");
            try{
            	sendNum = Integer.parseInt(prop.getProperty("emailnum"));
            }catch(Exception e){}
            password = prop.getProperty("password");
        } catch (Exception ex) {
            log.error("ex1 in sendmail.java:" + ex.toString(), ex);
        }
        this.setName("SMTPThread for sending email in queue.");
	}
	
	
	
	public static SMTPThread getInstance(){
		return thread;
	}



	@Override
	public void run() {
		while(!this.isInterrupted()){
			try{
				SessionFactory factory = HibernateSessionFactory.getSessionFactory();
				if(log.isTraceEnabled()){
					log.trace("get session from HibernateSessionFactory " + factory);
				}
				org.hibernate.Session s = factory.openSession();
				org.hibernate.Transaction trans = s.beginTransaction();
				SQLQuery q = s.createSQLQuery("select id, sender_name, subject, content, content_type, receiver, reply_to, times from email_queue with(nolock) where (status=2 or status=0) and times < 12 and create_date >= :yesterday order by status asc, priority desc ");
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				CommonUtil.clearTime(cal.getTime());
				q.setParameter("yesterday", cal);
				List<?> list = q.list();
				trans.commit();
				if(log.isTraceEnabled()){
					log.trace("found " + list.size() + " email in queue.");
				}
				for(int index=0; index<list.size(); index++){
					Object[] row = (Object[]) list.get(index);
					long id = ((Number) row[0]).longValue();
					String senderName = (String) row[1];
					String subject = (String) row[2];
					String content = null;
					org.hibernate.lob.SerializableClob desc = (org.hibernate.lob.SerializableClob) row[3];
					try {
						content = desc.getSubString(1l, (int) desc.length());
					} catch (Exception e) {
						if (log.isErrorEnabled()) {
							log.error("Error occured.", e);
						}
						continue;
					}

					String contentType = (String) row[4];
					String receiver = (String) row[5];
					String replyTo = (String) row[6];
					int times = ((Number) row[7]).intValue();
					//发送单条邮件
					int retry = 0;
					boolean shouldExit = false;
					int sendStatus = 0;
					boolean continueFor = false;
			        while(!shouldExit){
			            retry++;
			            org.hibernate.Transaction trans2 = s.beginTransaction();
						SQLQuery q2 = s.createSQLQuery("update email_queue set times=times+1 where id=:id and times=:times");
						q2.setParameter("id", id);
						q2.setParameter("times", times);
						int updateRows = q2.executeUpdate();
						trans2.commit();
						if(updateRows == 0){
							continueFor = true;
							break;
						}
			            int result = sendMail(senderName, receiver, replyTo, subject, content, contentType);
			            if(result == 0){
			            	//发送成功
			                shouldExit = true;
			                sendStatus = 1;
			            }else if(result == 1){
			            	//失败，但不会再次发送
			            	shouldExit = true;
			            	sendStatus = -1;
			            }else{
			            	//失败，会再次发送
			            	sendStatus = 2;
							if (retry > MAXRETRY) {
								shouldExit = true;
							} else {
								Thread.sleep(1000 + (long) ((Math.random() * 4000)));
							}
						}
			        }
			        if(continueFor){
			        	continue;
			        }
			        StringBuffer buf = new StringBuffer();
			        buf.append("update email_queue set ");
			        buf.append(" sent_date=getdate(), status=" + sendStatus);
			        
			        buf.append(" where id=" + id);
			        org.hibernate.Transaction trans2 = s.beginTransaction();
			        SQLQuery q2 = s.createSQLQuery(buf.toString());
			        q2.executeUpdate();
			        trans2.commit();
					if(shouldRestart){
						break;
					}
					long t = 1000 + (long) ((Math.random() * 5000));
					Thread.sleep(t);
				}
				
				s.close();
				//factory.close();
			}catch(JDBCException jdbce){
				Calendar c = Calendar.getInstance();
				if(c.get(Calendar.HOUR_OF_DAY) <= 7){
					//早上7点前的数据库错误被记录成WARN；因为可能是数据库服务器重启造成的
					if(log.isWarnEnabled()){
						log.warn("数据库错误", jdbce);
					}
				}else{
				    //基本都是重启tomcat造成发生此异常，不再记录error，改成warn
					log.warn("邮件队列，连接数据库错误", jdbce);
				}
			}catch (InterruptedException e) {
                if(log.isInfoEnabled()){
                    log.info("Error occured. InterruptedException, will exit", e);
                }
                this.interrupt();
                break;
            }catch(Exception e){
				log.error("邮件队列出错", e);
			}
			try {
				for(int i=0; i<600; i++){
					if(wakeUpMe || shouldRestart){
						break;
					}
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				if(log.isInfoEnabled()){
				    log.info("Error occured. InterruptedException, will exit", e);
				}
				this.interrupt();
				break;
			}
			wakeUpMe = false;
			shouldRestart = false;
		}
		if(log.isTraceEnabled()){
		    log.trace("will exit SMTPThread....");
		}
	}

	@Override
	public void interrupt(){
	    if(log.isTraceEnabled()){
            log.trace("interrupt SMTPThread.........");
        }	    
	    this.wakeUpMe = true;
	    super.interrupt();
	}
	public void wakeUp(){
		wakeUpMe = true;
	}
	public void restartLoop(){
		shouldRestart = true;
	}

	/**
	 * 
	 * @param senderName
	 * @param receiver
	 * @param replyTo
	 * @param subject
	 * @param content
	 * @param contentType
	 * @return 0：成功；1：失败，但是不再继续尝试；2：失败，稍后会继续尝试发送
	 */
	private synchronized int sendMail(String senderName, String receiver, String replyTo, String subject, String content, String contentType){
        Properties properties = new Properties();
        properties.setProperty ("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", smtpHost);
        //properties.setProperty("mail.debug", "true");
        Session session = null;
        int n = (int) (Math.random() * sendNum) + 1;
        if(log.isTraceEnabled()){
        	log.trace("sendNum=" + sendNum + "; randound n=" + n);
        }
        final String smtpUser;
        if(this.user != null && this.user.trim().length() != 0){
        	smtpUser = MessageFormat.format(this.user, new Object[]{n});
        }else{
        	smtpUser = null;
        }
    	String senderMail = null;
    	if(this.sender != null){
	        senderMail = MessageFormat.format(sender, new Object[]{n});
    	}
		try{
	        if(smtpUser != null){
		        properties.setProperty("mail.smtp.auth", "true");
		        session = Session.getDefaultInstance(properties,
		                new Authenticator() {
		            public PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication(smtpUser, password);
		            }
		        });
	        }else{
	        	session = Session.getDefaultInstance(properties, null);
	        }

	        MimeMessage mimeMsg = new MimeMessage(session);
	        if (senderMail != null) {
	        	if(senderName != null){
	        		try {
						mimeMsg.setFrom(new InternetAddress(senderMail, senderName, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						mimeMsg.setFrom(new InternetAddress(senderMail));
					}
	        	}else{
	        		mimeMsg.setFrom(new InternetAddress(senderMail));
	        	}
	        }
	        if (receiver != null) {
	            mimeMsg.setRecipients(Message.RecipientType.TO, parse(receiver));
	        }
	        if(replyTo != null && replyTo.trim().length() > 5){
	        	InternetAddress[] replyToAddress = new InternetAddress[1];
	        	replyToAddress[0] = new InternetAddress(replyTo);
	        	mimeMsg.setReplyTo(replyToAddress);
	        }
	
	        if (subject != null) {
	            mimeMsg.setSubject(subject, "UTF-8");
	        }
	        MimeBodyPart part = new MimeBodyPart();
	        part.setText(content == null ? "" : content);
	
	        if(contentType == null){
	        	part.setContent(content, "text/plain;charset=UTF-8");
	        }else{
	        	part.setContent(content, contentType);
	        }
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(part);
	        mimeMsg.setContent(multipart);
	        mimeMsg.setSentDate(new Date());
			if(log.isDebugEnabled()){
				log.debug("SMTP INFO: HOST=" + smtpHost + "; USER=" + user + "; PASS=" + password + "; SENDER=" + sender + "; TO=" + receiver);
			}
			Transport t = session.getTransport("smtp"); 
			t.connect(smtpHost, smtpUser, password); 
			t.sendMessage(mimeMsg, mimeMsg.getAllRecipients());
			t.close();
	        if(log.isTraceEnabled()){
	        	log.trace("mail sent to " + receiver);
	        }
	        return 0;
		}catch(AddressException ae){
			if(log.isTraceEnabled()){
				log.trace("地址错误:" + receiver, ae);
			}
			return 1;
		}catch(SendFailedException sfe){
			String message = sfe.getMessage();
			if(log.isTraceEnabled()){
				log.trace("SendFailedException:" + receiver + " invalidaddress=" + message.contains("Invalid Addresses"), sfe);
			}
			if(message.contains("Invalid Addresses")){
				return 1;
			}else{
				return 2;
			}
		}catch(Exception e){
        	if(log.isErrorEnabled()){
        		log.error("发送邮件失败，SMTP INFO: N=" + n + "; SMTPUSER=" + smtpUser + "; senderMail=" + senderMail + "; HOST=" + smtpHost + "; USER=" + user + "; PASS=" + password + "; SENDER=" + sender + "; TO=" + receiver + "; properties=" + properties.toString(), e);
        	}
        	//发送失败，但是会重新尝试发送
			return 2;
		}
   }
   
   private InternetAddress[] parse(String addressSet) throws AddressException {
       ArrayList<InternetAddress> list = new ArrayList<InternetAddress>();
       StringTokenizer tokens = new StringTokenizer(addressSet, ";");
       while (tokens.hasMoreTokens()) {
           list.add(new InternetAddress(tokens.nextToken().trim()));
       }
       InternetAddress[] addressArray = new InternetAddress[list.size()];
       list.toArray(addressArray);
       return addressArray;
   }
   
   
}
