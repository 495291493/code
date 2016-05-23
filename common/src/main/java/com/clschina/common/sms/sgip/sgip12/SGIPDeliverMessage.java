

package com.clschina.common.sms.sgip.sgip12;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;





public class SGIPDeliverMessage extends SGIPMessage {
    
    public static final int LEN_SGIP_DELIVER = 57;
    private byte[] body = null;
    private int msg_length;
    private int msg_encode;
    
    
    public SGIPDeliverMessage(SGIPHeader header, byte[] body) throws IllegalArgumentException {
        this.header = header;
        this.body = body;
        msg_length = SGIPMessage.byte4ToInteger(body, 45);
        msg_encode = body[44];
        if(body.length != LEN_SGIP_DELIVER + msg_length) {
        	System.out.println("body.length=" + body.length + ", LEN_SGIP_DELIVER=" + LEN_SGIP_DELIVER + ", msg_length=" + msg_length);
            //throw new IllegalArgumentException("deliver message body: invalid size");
        }
    }
    
    
    /** get the src-terminal-id */
    public String getSrcTermid() {
        return new String(body, 0, 21).trim();
    }
    
    
    /** get the dest-terminal-id */
    public String getDestTermid() {
        return new String(body, 21, 21).trim();
    }
    
    
    /** get the message content */
    public String getMsgContent() {
        String content = null;
        try {
        	if(msg_encode == 15) {			
        		content = new String(body, 49, msg_length, "GBK");
        	}else if(msg_encode == 8) {		
        		content = new String(body, 49, msg_length, "UnicodeBigUnmarked");
        	}else {
        		content = new String(body, 49, msg_length, "ISO8859_1");
        	}
        }catch(UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        return content;
    }
    
    
    /** get the link id */
    public String getLinkID() {
        return new String(body, 49 + msg_length, 8).trim();
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
}