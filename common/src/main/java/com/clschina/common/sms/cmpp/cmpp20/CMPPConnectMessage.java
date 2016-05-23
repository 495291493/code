
package com.clschina.common.sms.cmpp.cmpp20;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CMPPConnectMessage extends CMPPMessage {
    
    public final static int LEN_CMPP_CONNECT = 27;
    private String enter_code = null;//Դ��ַ��
    private String passwd = null;//
    private int version;//˫��Э�̵İ汾��(��4bit��ʾ���屾�ţ���4bit��ʾ�ΰ汾��)
    
    
    public CMPPConnectMessage(String enter_code, String passwd, int version) {
    	this.enter_code = enter_code;
        this.passwd = passwd;
        this.version = version;
        header = new CMPPHeader();
        header.setTotal_length(CMPPHeader.LEN_CMPP_HEADER + LEN_CMPP_CONNECT);
        header.setCommand(CMPPMessage.ID_CMPP_CONNECT, CMPPSequence.createSequence());
    }
    
    
    protected byte[] getMsgBody() {
    	byte[] buffer = new byte[LEN_CMPP_CONNECT];
    	// calculate authenticate string
    	byte[] spid = enter_code.getBytes();
    	byte[] pass = passwd.getBytes();
    	String timestamp = new SimpleDateFormat("MMddHHmmss").format(new Date());//ʱ��������ɿͻ��˲���
    	int length = spid.length + pass.length + 19;//19 = 9�ֽڵ�0 ��timestamp(10λ)
    	byte[] auth = new byte[length];
    	int position = spid.length;
    	System.arraycopy(spid, 0, auth, 0, position);
    	position += 9;
    	System.arraycopy(pass, 0, auth, position, pass.length);
    	position += pass.length;
    	System.arraycopy(timestamp.getBytes(), 0, auth, position, 10);
    	try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(auth);//���Դ��ַ
            md5.digest(buffer, 6, 16);//�ӵ���λ��ʼ�洢>=6  <=21
        }catch(Exception e) {}
    	
        // write enterprise code
        System.arraycopy(spid, 0, buffer, 0, spid.length);// >=0  <=5
        //copy��˳��ɱ仹�ǲ��ɱ䡣��
        
        // write version
        buffer[22] = (byte)version;  
        
        // write timestamp 
        System.arraycopy(integerToByte(Integer.parseInt(timestamp)), 0, buffer, 23, 4);
        //>=23  <=26
        return buffer;//connectMessage��������Ϣ
    }
    
}