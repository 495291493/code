package com.clschina.common.db.bean;

import java.util.Calendar;


public interface DataEntryInterface extends Auditable{
	
	public Calendar getCreateDate();
	public void setCreateDate(Calendar createDate);
	public String getCreator() ;
	public void setCreator(String creator);
	public String getModificator() ;
	public void setModificator(String modificator) ;
	public Calendar getModifyDate() ;
	public void setModifyDate(Calendar modifyDate) ;
	public String getModifyIP() ;
	public void setModifyIP(String createIp) ;
	public String getCreateIp() ;
	public void setCreateIp(String createIp) ;

}
