
package com.clschina.common.sms.cmpp.cmpp20;


public class CMPPTerminateMessage extends CMPPMessage {
    
    
    public CMPPTerminateMessage() {
        header = new CMPPHeader();
        header.setTotal_length(CMPPHeader.LEN_CMPP_HEADER);
        header.setCommand(CMPPMessage.ID_CMPP_TERMINATE, CMPPSequence.createSequence());
    }
    
    
    protected byte[] getMsgBody() {
        return null;
    }
    
}