package com.clschina.common.log;

import java.util.Calendar;

/**
 * 记录日志
 * @author hebinsen
 *
 */
public class OperateLog {
	/**
	 * 编号
	 */
	private Integer id;
	/**
	 * 信息
	 */
	private String message;
	/**
	 * 数据
	 */
	private String program;
	/**
	 * 操作人员
	 */
	private String operator;
	/**
	 * 操作时间
	 */
	private Calendar operatorDate;
	
	public OperateLog(){
		message = "";
		program = "";
		operator = "";
		operatorDate = Calendar.getInstance();
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public Calendar getOperatorDate() {
		return operatorDate;
	}
	public void setOperatorDate(Calendar operatorDate) {
		this.operatorDate = operatorDate;
	}

}
