
package com.clschina.common.sms.cmpp.cmpp20;


public class CMPPSubmitRespMessage extends CMPPMessage {
    
    public static final int LEN_CMPP_SUBMITRESP = 9;
    private byte[] body = null;
    
    
    public CMPPSubmitRespMessage(CMPPHeader header, byte[] body) throws IllegalArgumentException {
        this.header = header;
        this.body = body;
        if(body.length != LEN_CMPP_SUBMITRESP) {
            throw new IllegalArgumentException("submit response message body: invalid size");
        }
    }
    
    
    public long getSeq_no1() {
    	byte[] tmp = new byte[8];
    	System.arraycopy(body, 0, tmp, 0, 8);
    	/*long result = CMPPMessage.byte8ToLong(tmp, 0);
    	StringBuffer sb = new StringBuffer("seq_no1(");
    	sb.append(result).append("): ");
    	for(int i = 0; i < 8; i++) {
    		sb.append(body[i]).append(", ");
    	}
    	System.out.println(sb.toString());
        return result;*/
    	return CMPPMessage.byte8ToLong(tmp, 0);
    }
    
    
    public int getResult() {
    	return body[8] & 0xff;
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
    
}