package com.clschina.common.db.bean;

import java.io.Serializable;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import com.clschina.common.component.Login;
import com.clschina.common.component.ThreadLocalManager;

/**
 * 抽象的数据bean，包含了创建日期，创建者，修改日，修改者，修改者IP等属性。 需要这些属性的bean可以继承此类，省去了写这些代码。
 * 此类还能够自动根据hibernate生命周期同步最后修改者，修改日等属性，应为它实现了Lifecycle接口
 * 
 */
public abstract class AbstractDataEntry implements DataEntryInterface,
		Lifecycle {
	/**
	 * 创建人
	 * */
	protected String creator;
	/**
	 * 创建日期
	 * */
	protected Calendar createDate;
	/**
	 * 创建者Ip
	 * */
	protected String createIp;
	/**
	 * 修改人
	 * */
	protected String modificator;
	/**
	 * 修改日期
	 * */
	protected Calendar modifyDate;
	/**
	 * 修改者Ip
	 * */
	protected String modifyIp;
	private static final Log log = LogFactory.getLog(AbstractDataEntry.class);

	public AbstractDataEntry() {
	}

	public void addModifyFlag() {
		if (log.isTraceEnabled()) {
			log.trace("addModifyFlag() was called. on "
					+ this.getClass().getName() + " ");
		}
		modifyDate = Calendar.getInstance();
		Login login = ThreadLocalManager.getLogin();
		HttpServletRequest req = ThreadLocalManager.getRequest();
		if (req != null) {
			modifyIp = req.getRemoteAddr();
		} else {
			modifyIp = "local(no request)";
		}
		if (login != null) {
			modificator = login.getId();
		} else {
			if (log.isTraceEnabled()) {
				log.trace("No login found.");
			}
			modificator = "unknow";
		}
		if (creator == null || creator.trim().length() == 0) {
			creator = modificator;
			createDate = modifyDate;
			createIp=modifyIp;
		}
		if (createDate == null) {
			createDate = modifyDate;
		}
	}

	public void audit() {
		addModifyFlag();
	}

	public Calendar getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getModificator() {
		return modificator;
	}

	public void setModificator(String modificator) {
		this.modificator = modificator;
	}

	public Calendar getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Calendar modifyDate) {
		this.modifyDate = modifyDate;
	}

	/**
	 * @deprecated 大小写错误,请用getModifyIp
	 */
	public String getModifyIP() {
		return getModifyIp();
	}

	/**
	 * @deprecated 大小写错误,请用setModifyIp
	 */
	public void setModifyIP(String modifyIp) {
		setModifyIp(modifyIp);
	}
	
	
	public String getModifyIp() {
		return (modifyIp == null ? "" : modifyIp);
	}

	public void setModifyIp(String modifyIp) {
		this.modifyIp = modifyIp;
	}
	public String getCreateIp() {
		return (createIp == null ? "" : createIp);
	}

	public void setCreateIp(String createIp) {
		this.createIp = createIp;
	}

	public boolean onSave(Session session) throws CallbackException {
		addModifyFlag();
		return false;
	}

	public boolean onUpdate(Session session) throws CallbackException {
		addModifyFlag();
		return false;
	}

	public boolean onDelete(Session session) throws CallbackException {
		return false;
	}

	public void onLoad(Session session, Serializable arg1) {
	}

}
