package com.clschina.common.test;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.SendMail;
import com.clschina.common.util.mail.FileEntry;
import com.clschina.common.util.mail.Mail;



public class SendMailTestCase extends TestCase {
	private static Log log = LogFactory.getLog(SendMailTestCase.class);

	public void testSendMail() throws Exception{
		
		log.trace("test send mail");
		/*
		for(int i=0; i<0; i++){
			SendMail sendMail = new SendMail();
			sendMail.setSenderName("Mail");
			//sendMail.setReplyTo("duchaoting@clschina.com");
			sendMail.setContentType("text/plain;charset=utf-8");
			sendMail.setSubject("Hello.2");
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress(); //IP
			String address = addr.getHostName(); //Name
			boolean returnValue = sendMail.smtp("gexiangdong@clschina.com", "Hello sir " + i + ",\n\nIt's the body.\n\n" + address + "(" + ip + ")\r\n\n" + new Date());
			assertTrue(returnValue);
//			Thread.currentThread().sleep(10000);
		}
*/	}
	
	public void testERPMail(){
		
	
	}
}
