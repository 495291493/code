
package com.clschina.common.sms.cmpp.cmpp20;


public class CMPPDeliverRespMessage extends CMPPMessage {
    
    public static final int LEN_CMPP_DELIVERRESP = 9;
    private byte[] body = null;
    
    public CMPPDeliverRespMessage(CMPPDeliverMessage deliver) throws IllegalArgumentException {
        this.header = deliver.getCloneMsgHeader();
        header.setTotal_length(CMPPHeader.LEN_CMPP_HEADER + LEN_CMPP_DELIVERRESP);
        header.setCommand_id(CMPPMessage.ID_CMPP_DELIVER_RESP);
        body = new byte[LEN_CMPP_DELIVERRESP];
        deliver.copyMsgID(body);
        body[8] = 0;	// Congestion State
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
    
}