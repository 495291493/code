package com.clschina.common.db.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 方法返回结果对象
 * @author Administrator
 *
 */
public class ResultInfo {
	/**
	 * 是否成功，默认false
	 */
	private boolean success = false;
	private int code = 0;
	/**
	 * 返回信息
	 */
	private String msg = "";
	/**
	 * 返回参数,供上级方法使用
	 */
	private Map<String,Object> returnParamMap = new HashMap<String,Object>();
	public ResultInfo(){
		
	}
	public ResultInfo(boolean success,String msg){
		this.success = success;
		this.msg = msg;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Map<String, Object> getReturnParamMap() {
		return returnParamMap;
	}
	public void setReturnParamMap(Map<String, Object> returnParamMap) {
		this.returnParamMap = returnParamMap;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
}
