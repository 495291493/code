package com.clschina.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;

import com.clschina.common.db.HibernateSessionFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {
	private final static Log log = LogFactory.getLog(SendMail.class);
	private static int PRIORITY_COMMON = 2; // 普通优先级

	private String sender = "";

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	private String senderName = null;
	private String smtpHost = "";
	private String user = "";
	private int sendNum = 0;
	private String password = "";
	private String receiver, content;
	private String contentType = "text/plain";
	private String replyTo = null;

	private String subject = "";

	public SendMail() {
		this(null);
	}

	/**
	 * 使用Properties构建SendMail class. Properties可以包含以下内容：<br/>
	 * smtphost: 发信用smtp server<br/>
	 * user: 发信用smtphost上的用户，如果不需要smtp验证，则null<br/>
	 * password: 对应用户的密码<br/>
	 * sender: 发信人email<br/>
	 * subject: email主题.<br/>
	 * 
	 * @param prop
	 * <br/>
	 */
	public SendMail(Properties prop) {
		if (prop != null) {
			setPropertiesAttributes(prop);
		} else {
			this.getDefaultProperties();
		}
	}

	private void setPropertiesAttributes(Properties prop) {
		if (prop != null) {
			// this.properties = prop;
			this.setSmtpHost(prop.get("smtphost").toString());
			this.setUser(prop.get("user").toString());
			this.setPassword(prop.get("password").toString());
			this.setSender(prop.get("sender").toString());
			if (prop.get("sendername") != null) {
				this.setSenderName(prop.get("sendername").toString());
			}
			if (prop.get("subject") != null) {
				this.setSubject(prop.get("subject").toString());
			}
			try {
				if (prop.get("emailnum") != null) {
					this.sendNum = Integer.parseInt(prop.get("emailnum")
							.toString());
				}
			} catch (Exception e) {
			}
		}
	}

	private void getDefaultProperties() {
		try {
			InputStream is = getClass().getResourceAsStream(
					"MailServer.properties");
			Properties prop = new Properties();
			prop.load(is);
			setPropertiesAttributes(prop);
		} catch (Exception ex) {
			log.error("ex1 in sendmail.java:" + ex.toString(), ex);
		}
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return sender;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 发送email，异步发送，纯文本邮件
	 * 
	 * @param receiver
	 *            收信人email，如果同一封信发给多个人，用分号";"分隔
	 * @param content
	 *            email内容
	 * @throws MessagingException
	 */
	public boolean smtp(String receiver, String content) {
		return smtp(receiver, content, true);
	}

	public boolean smtp(String receiver, String content, boolean asy) {
		return smtp(receiver, content, asy, PRIORITY_COMMON);
	}

	/**
	 * 发送email
	 * 
	 * @param receiver
	 *            收信人email，如果同一封信发给多个人，用分号";"分隔
	 * @param content
	 *            email内容
	 * @param asy
	 *            是否异步发送。 false表示同步
	 * @return true/false
	 *         发送成功失败的标志，异步发送邮件，此返回值永远为true，异步发送请使用SendMailCallback接口接受发送成功失败标志
	 * @throws MessagingException
	 */
	public boolean smtp(String receiver, String content, boolean asy,
			int priority) {
		if (smtpHost == null){
			throw new NullPointerException("smtpHost not found");
		}
		if(receiver.contains(",")){
			receiver = CommonUtil.replace(receiver, ", ", ";");
		}
		this.receiver = receiver;
		this.content = content;
		if (asy) {
			// 保存入队列
			SessionFactory factory = HibernateSessionFactory
					.getSessionFactory();
			if (log.isTraceEnabled()) {
				log
						.trace("get session from HibernateSessionFactory "
								+ factory);
			}
			org.hibernate.Session s = factory.openSession();
			StringBuffer buf = new StringBuffer();
			buf.append("insert into email_queue");
			buf
					.append("(sender_name, subject, content, content_type, receiver, ");
			buf
					.append("reply_to, create_date, times, sent_date, priority, status)");
			buf
					.append("values(:sendername, :subject, :content, :contenttype, :receiver, ");
			buf.append(":replyto, :today, 0, null, " + priority + ", 0)");
			SQLQuery q = s.createSQLQuery(buf.toString());
			q.setParameter("sendername", senderName);
			q.setParameter("subject", subject);
			q.setParameter("content", content);
			q.setParameter("contenttype", contentType);
			q.setParameter("receiver", receiver);
			q.setParameter("replyto", replyTo == null ? "" : replyTo);
			q.setParameter("today", Calendar.getInstance());
			org.hibernate.Transaction trans = s.beginTransaction();
			q.executeUpdate();
			trans.commit();

			// 调用发件进程
			try {
				SMTPThread t = SMTPThread.getInstance();
				t.wakeUp(); // 如果在间歇，唤醒
				if (priority > PRIORITY_COMMON) {
					// 高优先级出现。
					t.restartLoop();
				}
				if (t.getState() == Thread.State.NEW) {
					t.start();
				}
			} catch (Exception e) {
				log.error("处理邮件队列线程调用失败。", e);
			}
			return true;
		} else {
			// 同步发送，暂不进入队列
			boolean result = false;
			int i = 0;
			while (i < 5 && result == false) {
				result = send();
				i++;
			}
			return result;
		}
	}

	private synchronized boolean send() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", smtpHost);
		Session session = null;
		int n = (int) (Math.random() * sendNum) + 1;
		if (log.isTraceEnabled()) {
			log.trace("sendNum=" + sendNum + "; randound n=" + n);
		}
		final String smtpUser;
		if (this.user != null && this.user.trim().length() != 0) {
			smtpUser = MessageFormat.format(this.user, new Object[] { n });
		} else {
			smtpUser = null;
		}
		String senderMail = null;
		if (this.sender != null) {
			senderMail = MessageFormat.format(sender, new Object[] { n });
		}
		try {
			if (smtpUser != null) {
				properties.put("mail.smtp.auth", "true");
				session = Session.getDefaultInstance(properties,
						new Authenticator() {
							public PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(smtpUser,
										password);
							}
						});
			} else {
				log.trace("user is null, try to get connnection without auth");
				session = Session.getDefaultInstance(properties, null);
			}
			MimeMessage mimeMsg = new MimeMessage(session);
			if (senderMail != null) {
				if (senderName != null) {
					try {
						mimeMsg.setFrom(new InternetAddress(senderMail,
								senderName, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						mimeMsg.setFrom(new InternetAddress(senderMail));
					}
				} else {
					mimeMsg.setFrom(new InternetAddress(senderMail));
				}
			}
			if (receiver != null) {
				mimeMsg
						.setRecipients(Message.RecipientType.TO,
								parse(receiver));
			}
			if (replyTo != null) {
				InternetAddress[] replyToAddress = new InternetAddress[1];
				replyToAddress[0] = new InternetAddress(replyTo);
				mimeMsg.setReplyTo(replyToAddress);
			}

			if (subject != null) {
				mimeMsg.setSubject(subject, "UTF-8");
			}
			MimeBodyPart part = new MimeBodyPart();
			part.setText(content == null ? "" : content, "UTF-8");

			if (contentType == null) {
				part.setContent(content, "text/plain;charset=UTF-8");
			} else {
				part.setContent(content, contentType);
			}
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(part);
			mimeMsg.setContent(multipart);
			mimeMsg.setSentDate(new Date());
			if (log.isDebugEnabled()) {
				log.debug("SMTP INFO: HOST=" + smtpHost + "; USER=" + user
						+ "; PASS=" + password + "; SENDER=" + sender + "; TO="
						+ receiver);
			}
			Transport.send(mimeMsg);
			if (log.isTraceEnabled()) {
				log.trace("mail sent to " + receiver);
			}
			return true;
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("发送邮件失败，SMTP INFO: N=" + n + "; SMTPUSER=" + smtpUser
						+ "; senderMail=" + senderMail + "; HOST=" + smtpHost
						+ "; USER=" + user + "; PASS=" + password + "; SENDER="
						+ sender + "; TO=" + receiver, e);
			}
			return false;
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

	public boolean sendMails(String mail, String content) {
		if (mail == null || content == null) {
			return false;
		}

		try {
			this.smtp(mail, content, true);
		} catch (Exception ex) {
			if (log.isErrorEnabled()) {
				log.error("ex2 in sendmail.java:" + ex.toString(), ex);
			}
			return false;
		}

		return true;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the replyTo
	 */
	public String getReplyTo() {
		return replyTo;
	}

	/**
	 * @param replyTo
	 *            the replyTo to set
	 */
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

}
