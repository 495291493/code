
package com.clschina.common.sms.sgip.sgip12;





public class SGIPBindRespMessage extends SGIPMessage {
    
    public static final int LEN_SGIP_BINDRESP = 9;
    private byte[] body = null;
    
    
    public SGIPBindRespMessage(SGIPHeader header, byte[] body) throws IllegalArgumentException {
        this.header = header;
        this.body = body;
        if(body.length != LEN_SGIP_BINDRESP) {
            throw new IllegalArgumentException("bind response message body: invalid size");
        }
    }
    
    
    public SGIPBindRespMessage(SGIPBindMessage bind) {
        this.header = bind.getCloneMsgHeader();
        header.setTotalLength(SGIPHeader.LEN_SGIP_HEADER + LEN_SGIP_BINDRESP);
        header.setCommandID(SGIPMessage.ID_SGIP_BIND_RESP, false);
        body = new byte[LEN_SGIP_BINDRESP];
        body[0] = 0;	// status
    }
    
    
    public int getResult() {
        return body[0];
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
 
}