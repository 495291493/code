package com.clschina.common.db.bean;

import java.io.Serializable;

/**
 * 如果要手工指定一个自动设置Id（通过com.extendradius.db.hibernate.id.PrimaryKeyGenerator）
 * 的ID，则Bean需要实现此方法，并且在getAssignedId()方法中返回一个非NULL值。
 * 
 */
public interface IdAssignableDataEntry {
	/**
	 * 返回值将被做为主键。
	 * @return
	 */
	public Serializable getAssignedId();
}
