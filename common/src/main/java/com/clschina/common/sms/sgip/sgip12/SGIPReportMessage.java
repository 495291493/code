
package com.clschina.common.sms.sgip.sgip12;

import java.io.IOException;
import java.io.OutputStream;





public class SGIPReportMessage extends SGIPMessage {
    
    public static final int LEN_SGIP_REPORT = 44;
    private byte[] body = null;
    
    
    public SGIPReportMessage(SGIPHeader header, byte[] body) throws IllegalArgumentException {
        this.header = header;
        this.body = body;
        if(body.length != LEN_SGIP_REPORT) {
            throw new IllegalArgumentException("report message body: invalid size");
        }
    }

    
    public int getReport_no1() {
        return SGIPMessage.byte4ToInteger(body, 0);
    }
    
    
    public int getReport_no2() {
        return SGIPMessage.byte4ToInteger(body, 4);
    }
    
    
    public int getReport_no3() {
        return SGIPMessage.byte4ToInteger(body, 8);
    }
    
    
    /**
     * get the type of the report
     * @return 0 if this is a report for previous submit message, otherwise 1 for previous forward deliver message
     */
    public int getReport_type() {
        return body[12];
    }
    
    
    public String getUserNumber() {
        return new String(body, 13, 21);
    }
    
    
    public int getReport_state() {
        return body[34];
    }
    
    
    public String getReport_Err() {
        return body[35] + "";
    }
    
    
    protected byte[] getMsgBody() {
        return body;
    }
}