
package com.clschina.common.sms.sgip.sgip12;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;



public class SGIPHeader implements Cloneable {
    
    public final static int LEN_SGIP_HEADER = 20;
    
    private int total_length;
    private int command_id;
    private int seq_no1, seq_no2, seq_no3;
    
    
    public void setTotalLength(int len) {
        total_length = len;
    }
    
    
    public int getTotalLength() {
        return total_length;
    }
   
    
    public void setCommandID(int id, boolean createSequence) {
        command_id = id;
        if(createSequence) {
            buildSequence();
        }
    }
    
    
    public int getCommanID() {
        return command_id;
    }
    
    
    public void buildSequence() {
        SGIPSequence seq = SGIPSequence.createSequence();
        seq_no1 = seq.getSeq_no1();
        seq_no2 = seq.getSeq_no2();
        seq_no3 = seq.getSeq_no3();
    }
    
    
    public int getSeq_no1() {
        return seq_no1;
    }
    
    
    public int getSeq_no2() {
        return seq_no2;
    }
    
    
    public int getSeq_no3() {
        return seq_no3;
    }
    
    
    public byte[] getMsgHeader() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(LEN_SGIP_HEADER);
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(total_length);
        dos.writeInt(command_id);
        dos.writeInt((int)seq_no1);
        dos.writeInt(seq_no2);
        dos.writeInt(seq_no3);
        return out.toByteArray();
    }
    
    
    public void readMsgHeader(InputStream in) throws IOException {
        byte[] buffer = new byte[LEN_SGIP_HEADER];
        //int actual_length = in.read(buffer);
        int actual_length = 0, read_bytes;
        //while(read_bytes > 0 && actual_length < LEN_SGIP_HEADER) {
        do {
        	read_bytes = in.read(buffer, actual_length, LEN_SGIP_HEADER-actual_length);
        	actual_length += read_bytes;
        }while(actual_length < LEN_SGIP_HEADER && read_bytes > 0);
        if(LEN_SGIP_HEADER != actual_length) {
        	if(actual_length == -1) {
        		//throw new SocketException("get the end of the inputStream, maybe the connection is broken");
        	}else {
        		System.out.println("head: actual_length=" + actual_length + ", LEN_SGIP_HEADER=" + LEN_SGIP_HEADER);
    	        StringBuffer sb = new StringBuffer();
    	        for(int i = 0; i < LEN_SGIP_HEADER; i++) {
    	            sb.append("head[").append(i).append("] = ").append(buffer[i] + ", ");
    	    	}
    	        System.out.println(sb.toString());
        		throw new IOException("can't get actual length of message header from the inputstream:" + actual_length);
        	}
        }
        
        /*if(SGIPMessage.log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < LEN_SGIP_HEADER; i++) {
                sb.append("\nhead[").append(i).append("] = ").append(buffer[i]);
        	}
            SGIPMessage.log.debug(sb.toString());
        }*/
        total_length = SGIPMessage.byte4ToInteger(buffer, 0);
        command_id = SGIPMessage.byte4ToInteger(buffer, 4);
        seq_no1 = SGIPMessage.byte4ToInteger(buffer, 8);
        seq_no2 = SGIPMessage.byte4ToInteger(buffer, 12);
        seq_no3 = SGIPMessage.byte4ToInteger(buffer, 16);
    }
    
    
    protected Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}