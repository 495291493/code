package com.clschina.common.util.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class Mail {  
	private static final Log log = LogFactory.getLog(Mail.class);
    //定义发件人、收件人、SMTP服务器、用户名、密码、主题、内容等  
    private String displayName;  
    private String to;  
    private String from;  
    private String smtpServer;  
    private String username;  
    private String password;  
    private String subject;  
    private String content;  
    private boolean ifAuth; //服务器是否要身份认证  
    private List<FileEntry> file = new ArrayList<FileEntry>(); //用于保存发送附件的文件名的集合  
      
    /** 
     * 设置SMTP服务器地址 
     */  
    public void setSmtpServer(String smtpServer){  
        this.smtpServer=smtpServer;  
    }  
     
    /** 
     * 设置发件人的地址 
     */  
    public void setFrom(String from){  
        this.from=from;  
    }  
    /** 
     * 设置显示的名称 
     */  
    public void setDisplayName(String displayName){  
        this.displayName=displayName;  
    }  
     
    /** 
     * 设置服务器是否需要身份认证 
     */  
    public void setIfAuth(boolean ifAuth){  
        this.ifAuth=ifAuth;  
    }  
     
    /** 
     * 设置E-mail用户名 
     */  
    public void setUserName(String username){  
        this.username=username;  
    }  
     
    /** 
     * 设置E-mail密码 
     */  
    public void setPassword(String password){  
        this.password=password;  
    }  
     
    /** 
     * 设置接收者 
     */  
    public void setTo(String to){  
        this.to=to;  
    }  
     
    /** 
     * 设置主题 
     */  
    public void setSubject(String subject){  
        this.subject=subject;  
    }  
     
    /** 
     * 设置主体内容 
     */  
    public void setContent(String content){  
        this.content=content;  
    }  
     
    /** 
     * 该方法用于收集附件名 
     */  
    public void addAttachfile(FileEntry fentry){  
        file.add(fentry);  
    }  
     
    public Mail(){  
         
    }  
    
    /**
     * 
     * @param from 		发送者E-mail地址
     * @param displayName 显示名称
     * @param username	用户名
     * @param password	密码
     * @param to		接收人地址
     * @param subject 	标题
     * @param content	内容
     */
    public Mail(String from,String displayName,String username,String password,String to,String subject,String content){  
        this("smtp.ym.163.com",from,displayName,username,password,to,subject,content);
    }  
 
    /**
     * 
     * @param smtpServer	SMTP服务器地址
     * @param from			发送者E-mail地址
     * @param displayName	显示名称
     * @param username		用户名
     * @param password		密码
     * @param to			接收人地址
     * @param subject 		标题
     * @param content		内容
     */
    public Mail(String smtpServer,String from,String displayName,String username,String password,String to,String subject,String content){  
        this.smtpServer=smtpServer;  
        this.from=from;  
        this.displayName=displayName;  
        this.ifAuth=true;  
        this.username=username;  
        this.password=password;  
        this.to=to;  
        this.subject=subject;  
        this.content=content;  
    }  
     
    /** 
     * 初始化SMTP服务器地址、发送者E-mail地址、接收者、主题、内容 
     */  
    public Mail(String smtpServer,String from,String displayName,String to,String subject,String content){  
        this.smtpServer=smtpServer;  
        this.from=from;  
        this.displayName=displayName;  
        this.ifAuth=false;  
        this.to=to;  
        this.subject=subject;  
        this.content=content;  
    }  
    
    private InternetAddress[] parseAddress(String addressSet) throws AddressException {
		ArrayList<InternetAddress> list = new ArrayList<InternetAddress>();
		StringTokenizer tokens = new StringTokenizer(addressSet, ";");
		while (tokens.hasMoreTokens()) {
			list.add(new InternetAddress(tokens.nextToken().trim()));
		}
		InternetAddress[] addressArray = new InternetAddress[list.size()];
		list.toArray(addressArray);
		return addressArray;
	}
  
    /**
     * 发送邮件 
     * @return 
     * state:success, message:邮件发送成功！
     * state:failed, message:！ 邮件发送失败！错误原因：xxx
     */
    public Map<String, String> send(){  
        HashMap<String, String> map=new HashMap<String, String>();  
        map.put("state", "success");  
        String message="邮件发送成功！";  
        Session session=null;  
        Properties props = System.getProperties();  
        props.put("mail.smtp.host", smtpServer);  
        if(ifAuth){ //服务器需要身份认证  
            props.put("mail.smtp.auth","true");     
            SmtpAuth smtpAuth=new SmtpAuth(username,password);  
            session=Session.getDefaultInstance(props, smtpAuth);   
        }else{  
            props.put("mail.smtp.auth","false");  
            session=Session.getDefaultInstance(props, null);  
        }  
//      session.setDebug(true);  
        Transport trans = null;    
        try {  
            Message msg = new MimeMessage(session);   
            Address from_address;
			from_address = new InternetAddress(from, displayName);
			msg.setFrom(from_address);
            msg.setRecipients(Message.RecipientType.TO, parseAddress(to));  
            msg.setSubject(subject);  
            Multipart mp = new MimeMultipart();  
            MimeBodyPart mbp = new MimeBodyPart();  
            mbp.setContent(content.toString(), "text/html;charset=gb2312");  
            mp.addBodyPart(mbp);    
            for(int i=0; i<file.size(); i++){//有附件   
                mbp=new MimeBodyPart();  
                FileDataSource fds=new FileDataSource(file.get(i).getFilePath()); //得到数据源  
                mbp.setDataHandler(new DataHandler(fds)); //得到附件本身并至入BodyPart  
				mbp.setFileName(new String(
						file.get(i).getFileName().getBytes("gb2312"), "iso8859-1"));//得到文件名同样至入BodyPart  
                mp.addBodyPart(mbp);  
            }    
            file.clear();      

            msg.setContent(mp); //Multipart加入到信件  
            msg.setSentDate(new Date());     //设置信件头的发送日期  
            //发送信件  
            msg.saveChanges();   
            trans = session.getTransport("smtp");  
            trans.connect(smtpServer, username, password);  
            trans.sendMessage(msg, msg.getAllRecipients());  
            trans.close();  
             
        }catch(AuthenticationFailedException e){   
            map.put("state", "failed");  
            message="邮件发送失败！错误原因：\n"+"用户名密码错误!";  
        }catch (Exception e) {
        	log.error("发送邮件出错", e);
            message="邮件发送失败！错误原因：\n"+e.getMessage();  
            map.put("state", "failed");  
        }
        map.put("message", message);  
        return map;  
    }    
}  

class SmtpAuth extends javax.mail.Authenticator {   
    private String username,password;   
  
    public SmtpAuth(String username,String password){   
        this.username = username;    
        this.password = password;    
    }   
    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {   
        return new javax.mail.PasswordAuthentication(username,password);   
    }   
}
