package com.clschina.common.component;

import javax.servlet.http.Cookie;

/**
 * 此接口会被SignOnFilter使用，用来提供基于cookie的验证方式。
 * @author gexiangdong
 *
 */
public interface LoginManager {
	
	/**
	 * 根据Cookie，返回登录用户。如果cookie中没有保存登录信息，返回null
	 * @param cookies
	 * @return
	 */
	public Login createLoginFromCookies(Cookie[] cookies);
}
