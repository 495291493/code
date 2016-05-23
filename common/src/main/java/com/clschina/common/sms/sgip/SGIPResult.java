/**
 * 
 */
package com.clschina.common.sms.sgip;


/**
 * @author Wu Xiao Fei
 * 
 */
public class SGIPResult {
	public static final int CODE_SUCCESS = 0;// 成功
	public static final int CODE_FAILTRUE = 1;// 失败
	public static final int CODE_NO_RESULT = -1;// 表示没有结果

	public static SGIPResult createSuccessResult() {
		SGIPResult result = new SGIPResult();
		result.setCode(CODE_SUCCESS);
		return result;
	}

	public static SGIPResult createFailtrueResult(String msg) {
		SGIPResult result = new SGIPResult();
		result.setCode(CODE_FAILTRUE);
		result.setMessage(msg);
		return result;
	}

	private int code = CODE_NO_RESULT;// 返回代码,必须要有返回值
	private String message;// 中文说明

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return CODE_SUCCESS == code;
	}

	public boolean isNotSuccess() {
		// 不成功，不一定是失败
		return CODE_SUCCESS != code;
	}

}
