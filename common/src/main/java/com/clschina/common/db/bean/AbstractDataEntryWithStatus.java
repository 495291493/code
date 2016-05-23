package com.clschina.common.db.bean;

import java.io.Serializable;

/**
 * 
 * 增加了一个state属性，用于表示状态，当DELETED_STATUS时，表示是被删除了的数据。
 *
 */
public abstract class AbstractDataEntryWithStatus extends AbstractDataEntry 
	implements DataEntryInterfaceWithStatus,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3203326617573888981L;
	private int status;
	
	public AbstractDataEntryWithStatus() {
		super();
		status = DataEntryInterfaceWithStatus.NORMAL_STATUS;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
