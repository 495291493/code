
package com.clschina.common.sms.cmpp.cmpp20;


public class CMPPActiveRespMessage extends CMPPMessage {
	public static final int LEN_CMPP_ACTIVERESP = 1;
    private byte[] body = null;
	
    
    public CMPPActiveRespMessage(CMPPHeader header, byte[] body) {
        this.header = header;
        this.body = body;
        
    }
    
    
    public CMPPActiveRespMessage(CMPPActiveMessage active) {
        this.header = active.getCloneMsgHeader();
        header.setTotal_length(CMPPHeader.LEN_CMPP_HEADER + LEN_CMPP_ACTIVERESP);
        header.setCommand_id(CMPPMessage.ID_CMPP_ACTIVE_RESP);
        body = new byte[LEN_CMPP_ACTIVERESP];
        body[0] = 0;
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
    
}