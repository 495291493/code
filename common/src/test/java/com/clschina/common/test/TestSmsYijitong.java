package com.clschina.common.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.sms.SMSResult;
import com.clschina.common.sms.SMSService;

public class TestSmsYijitong extends TestCase {
    private static final Log log = LogFactory.getLog(TestSmsYijitong.class);
    
    
    public void testSend(){
//        SMSService serv = SMSService.getInstance();
//        SMSResult result = serv.smsWithRetry("13501714675", "葛香东您好，葛香东（13501714675）给您送了礼物，用手机或电脑打开看看吧\"g.gift5u.com/ruroxe\"。回复TD退订");
//        assertTrue (result.getCode() == SMSResult.CODE_SUCCESS) ;
//        log.trace("OK");
//        
//        result = serv.smsWithRetry("18964501838", "葛香东您好，葛香东（13501714675）给您送了礼物，用手机或电脑打开看看吧\"g.gift5u.com/ruroxe\"。回复TD退订");
//        assertTrue (result.getCode() == SMSResult.CODE_SUCCESS) ;
//        log.trace("OK");
//        
//        result = serv.smsWithRetry("13501714675", "葛香东您好，葛香东（13501714675）给您送了礼物，用手机或电脑打开看看吧。回复TD退订");
//        assertTrue (result.getCode() == SMSResult.CODE_SUCCESS) ;
//        log.trace("OK");   
    	try{
	    	SMSService serv = SMSService.getInstance();
//	    	SMSResult result = serv.sms("18964501838", "我司客服邮箱地址:service@clschina.com,照片请不要大于2兆，来邮请备注姓名和联电,谢谢!");
//	    	SMSResult result = serv.sms("13795488350", "尊敬的赵瑞，您在太平洋保险兑换的礼品已发出,顺丰单号:903982625764,请留意查收. 咨询投诉电话4008869986。太平洋保险积分换礼服务商-");
//	    	SMSResult result = serv.sms("18930067133", "我司客服邮箱地址:service@clschina.com,照片请不要大于2兆，来邮请备注姓名和联电,谢谢!");
//	    	log.trace("result = " + result.getCode());
    	}catch(Exception ex){
    		log.error("///ERROR:" + ex.getMessage(), ex);
    	}
//      
    }
}
