/**
 * 
 */
package com.clschina.common.util;



/**
 * 此接口用于设置SendMail类异步发送邮件时的回调函数
 */
public interface SendMailCallback {
	/**
	 * 异步发送的每封邮件，发送完毕会调用的接口
	 * @param successful	是否成功发送
	 * @param recption		接收者
	 * @param subject		邮件主题
	 * @param errMessage	发送失败的错误信息（如果发送成功，则此参数为NULL）
	 */
	public void complete(boolean successful, String recption, String subject, String errMessage);
}
