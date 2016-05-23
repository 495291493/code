
package com.clschina.common.sms.cmpp.cmpp20;


public class CMPPTerminateRespMessage extends CMPPMessage {
    
    public CMPPTerminateRespMessage(CMPPHeader header) {
        this.header = header;
    }
    
    protected byte[] getMsgBody() {
        return null;
    }
    
}