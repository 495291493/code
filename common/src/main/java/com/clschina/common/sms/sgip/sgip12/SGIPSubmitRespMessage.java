
package com.clschina.common.sms.sgip.sgip12;

import java.io.IOException;
import java.io.OutputStream;





public class SGIPSubmitRespMessage extends SGIPMessage {
    
    public static final int LEN_SGIP_SUBMITRESP = 9;
    private byte[] body = null;
    
    
    public SGIPSubmitRespMessage(SGIPHeader header, byte[] body) throws IllegalArgumentException {
        this.header = header;
        this.body = body;
        if(body.length != LEN_SGIP_SUBMITRESP) {
            throw new IllegalArgumentException("submit response message body: invalid size");
        }
    }
    
    
    public int getResult() {
        return body[0];
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
}