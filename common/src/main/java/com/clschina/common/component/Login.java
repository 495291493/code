package com.clschina.common.component;

import java.util.List;

/**
 * 用户信息接口，会被SignOnFilter, LoginManager, AbstractDataEntry等诸多类使用，用来
 * 设置或者取得当前的用户。<br/>
 * 凡是需要登录的用户类，都应该实现此接口。
 * @author gexiangdong
 *
 */
public interface Login {
	/**
	 * 用户ID
	 * @return
	 */
	public String getId();
	
	/**
	 * 用户姓名，用于显示在界面上的名字
	 * @return
	 */
	public String getName();
	
	/**
	 * 用户具有的权限
	 * @return
	 */
	public List<String> getPrivileges();
	
}
