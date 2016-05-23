/**
 * 
 */
package com.clschina.common.sms;

/**
 * 访问第三方短信服务的接口
 * 
 * @author Wu Xiao Fei
 * 
 */
interface IVisitService {

	/**
	 * 发送短信
	 * 
	 * @param phoneRece
	 * @param notes
	 * @return
	 */
	public SMSResult sms(String phoneRece, String notes) throws Exception;

}
