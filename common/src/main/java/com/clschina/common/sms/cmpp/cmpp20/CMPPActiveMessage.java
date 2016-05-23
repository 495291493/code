
package com.clschina.common.sms.cmpp.cmpp20;


public class CMPPActiveMessage extends CMPPMessage {
    
    
    /**
     * construct a new Active test message
     */
    public CMPPActiveMessage() {
        header = new CMPPHeader();
        header.setTotal_length(CMPPHeader.LEN_CMPP_HEADER);
        header.setCommand(CMPPMessage.ID_CMPP_ACTIVE, CMPPSequence.createSequence());
    }
    
    
    /**
     * get the active message from server(SMG)
     */
    public CMPPActiveMessage(CMPPHeader header) {
        this.header = header;
    }
    
    
    protected byte[] getMsgBody() {
        return null;
    }
    
}