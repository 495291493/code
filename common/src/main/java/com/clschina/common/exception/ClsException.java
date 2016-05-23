package com.clschina.common.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 异常 <br>
 * 遇到此类异常，界面上将显示出来message和code
 *
 */
public class ClsException extends Exception {
    private static final long serialVersionUID = 4163834308392461859L;

    private static final Log log = LogFactory.getLog(ClsException.class);
    
    private String code;
    private String message;
    
    public ClsException(String code, String message, Throwable t){
        super(t);
        this.code = code;
        this.message = message;
        if(log.isWarnEnabled()){
            log.warn("遇到异常(" + code + "; " + message + "; ", t);
        }
    }
    
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    
    
}
