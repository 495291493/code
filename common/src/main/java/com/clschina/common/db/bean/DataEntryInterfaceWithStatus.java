package com.clschina.common.db.bean;

public interface DataEntryInterfaceWithStatus extends DataEntryInterface {
	public final int DELETED_STATUS = -1;
	public final int NORMAL_STATUS = 0;
	
	public int getStatus();
	public void setStatus(int status) ;

}
