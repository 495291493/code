/**
 * 
 */
package com.clschina.common.sms.cmpp;

/**
 * 
 * 百悟的接受接口
 * 
 * @author Wu Xiao Fei
 * 
 */
public interface CmppDeliver {

	/**
	 * 电话接受方法
	 * @param destPhone(申请的短信账号手机号)
	 * @param srcPhone(客户手机号)
	 * @param msg
	 */
	public void deliver(String destPhone, String srcPhone, String msg);

}
