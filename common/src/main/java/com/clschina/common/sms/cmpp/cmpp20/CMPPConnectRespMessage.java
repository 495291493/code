
package com.clschina.common.sms.cmpp.cmpp20;




public class CMPPConnectRespMessage extends CMPPMessage {
    
    public static final int LEN_CMPP_CONNECTRESP = 18;
    private byte[] body = null;
    
    
    public CMPPConnectRespMessage(CMPPHeader header, byte[] body) throws IllegalArgumentException {
        this.header = header;
        this.body = body;
    	
        if(body.length != LEN_CMPP_CONNECTRESP) {
            throw new IllegalArgumentException("connect response message body: invalid size");
        }
    }
//    public CMPPConnectRespMessage(CMPPConnectMessage bind) {
//        this.header = bind.getCloneMsgHeader();
//        header.setTotal_length(CMPPHeader.LEN_CMPP_HEADER + LEN_CMPP_CONNECTRESP);
//        header.setCommand(CMPPMessage.ID_CMPP_CONNECT_RESP, header.getSequence_id());
//        body = new byte[LEN_CMPP_CONNECTRESP];
//        body[0] = 0;	// status
//    }
    
    
    public int getConnect_status() {
    	
    	return body[0];
    }
    
    
    public int getVersion() {
        return body[17];
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
    
}