package com.clschina.common.log;

import java.util.Calendar;

public class AutoRunLog {
	private int id;
	private Calendar time;
	private String type;
	private boolean successful;
	private String result;
	
	public AutoRunLog(){
		result = "成功";
		time = Calendar.getInstance();
		successful = true;
		type = "";
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Calendar getTime() {
		return time;
	}
	public void setTime(Calendar time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isSuccessful() {
		return successful;
	}
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	
}
