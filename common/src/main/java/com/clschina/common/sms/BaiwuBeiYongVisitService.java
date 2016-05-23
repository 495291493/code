/**
 * 
 */
package com.clschina.common.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.sms.cmpp.CmppApi;
import com.clschina.common.sms.cmpp.CmppDeliver;
import com.clschina.common.sms.cmpp.CmppResult;

/**
 * 访问百悟（）的访问接口进行处理（备份与非备份已经合并，本类不再使用）
 * 
 * @author Wu Xiao Fei
 * 
 */
@Deprecated
class BaiwuBeiYongVisitService implements IVisitService {

	private static Log log = LogFactory.getLog(BaiwuBeiYongVisitService.class);

	public static final String KEY_START_STR = "startStr";
	public static final String KEY_SIGN_TXT = "signTxt";
	public static final String KEY_SIGN_LENGTH = "signLength";

	// 配置信息，以后全部放在配置文件中
	private String host;// 远程地址
	private int port;// 远程端口
	private String corpId;// 企业id（用户名）
	private String passwd;// 密码
	private String srcPhoneNo;// 源设备id，即发送短信的电话

	private String name;// 系统的标示，用来区分不同的信息

	private int signLength;// 签名的长度

	private String deliverClass;// class of deliver

	private String configPath;

	private boolean isSignDynamic = false;// 是否启用动态签名
	private List<Map<String, Object>> signDynamicList = new ArrayList<Map<String, Object>>();// 动态签名list

	private CmppApi cmppApi = new CmppApi();

	public BaiwuBeiYongVisitService(String configPath) {
		this.configPath = configPath;
		init();
	}

	/**
	 * 初始化，从配置文件中获取配置信息
	 */
	protected void init() {
		String filepath = getConfigPath();
		if (log.isInfoEnabled()) {
			log.info("Baiwu sms config file:" + filepath);
		}
		Properties p = new Properties();
		try {
			p.load(BaiwuBeiYongVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("Baiwu sms config file error:" + e.getMessage(), e);
			}
		}
		host = p.getProperty("baiwuBeiYong.host");
		port = Integer.parseInt(p.getProperty("baiwuBeiYong.port"));
		corpId = p.getProperty("baiwuBeiYong.corpId");
		passwd = p.getProperty("baiwuBeiYong.passwd");
		srcPhoneNo = p.getProperty("baiwuBeiYong.srcPhoneNo");
		signLength = Integer.parseInt(p.getProperty("baiwuBeiYong.signLength", "6"));// 默认值为6
		deliverClass = p.getProperty("baiwuCmpp.deliverClass");

		name = p.getProperty("name");

		String signDynamic = p.getProperty("baiwuBeiYong.signDynamic");
		if (signDynamic == null || signDynamic.trim().length() == 0) {
			isSignDynamic = false;
		} else {
			isSignDynamic = true;
			// init
			String[] signDynamicItems = signDynamic.split("\\|");
			for (int i = 0; i < signDynamicItems.length; i++) {
				String signDynamicItem = signDynamicItems[i];
				String[] itemArray = signDynamicItem.split(",");
				if (itemArray.length != 3) {
					if (log.isInfoEnabled()) {
						log.info("signDynamicItem is not right:" + signDynamicItem);
					}
					continue;
				}
				if (itemArray[0] == null || itemArray[0].trim().length() == 0) {
					if (log.isInfoEnabled()) {
						log.info("signDynamicItem is not right:" + signDynamicItem);
					}
					continue;
				}
				try {
					int length = Integer.parseInt(itemArray[2]);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(KEY_START_STR, itemArray[0]);
					map.put(KEY_SIGN_TXT, itemArray[1] == null ? "" : itemArray[1]);
					map.put(KEY_SIGN_LENGTH, length);
					this.signDynamicList.add(map);
				} catch (Exception e) {
					log.error("signDynamicItem is not right:" + signDynamicItem, e);
				}
			}

		}

		if (checkConfig()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]baiwuCmpp sms config:\n");
				sb.append("baiwuBeiYong.host:" + host + "\n");
				sb.append("baiwuBeiYong.port:" + port + "\n");
				sb.append("baiwuBeiYong.corpId:" + corpId + "\n");
				sb.append("baiwuBeiYong.passwd:" + passwd + "\n");
				sb.append("baiwuBeiYong.srcPhoneNo:" + srcPhoneNo + "\n");
				sb.append("baiwuBeiYong.signLength:" + signLength + "\n");
				sb.append("baiwuBeiYong.deliverClass:" + deliverClass + "\n");
				sb.append("baiwuBeiYong.signDynamic:" + signDynamic + "\n");
				sb.append("baiwuBeiYong.isSignDynamic:" + isSignDynamic + "\n");
				log.info(sb.toString());
				if (this.isSignDynamic) {
					sb = new StringBuffer("signDynamic is:\n");
					for (Map<String, Object> map : this.signDynamicList) {
						sb.append(map.get(KEY_START_STR) + "," + map.get(KEY_SIGN_TXT) + "," + map.get(KEY_SIGN_LENGTH)
								+ "\n");
					}

					log.info(sb.toString());
				}

			}
		} else {
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]baiwuCmpp sms config error:缺少配置信息\n");
				sb.append("baiwuBeiYong.host:" + host + "\n");
				sb.append("baiwuBeiYong.port:" + port + "\n");
				sb.append("baiwuBeiYong.corpId:" + corpId + "\n");
				sb.append("baiwuBeiYong.passwd:" + passwd + "\n");
				sb.append("baiwuBeiYong.srcPhoneNo:" + srcPhoneNo + "\n");
				sb.append("baiwuBeiYong.signLength:" + signLength + "\n");
				sb.append("baiwuCmpp.deliverClass:" + deliverClass + "\n");
				sb.append("baiwuBeiYong.signDynamic:" + signDynamic + "\n");
				sb.append("baiwuBeiYong.isSignDynamic:" + isSignDynamic + "\n");
				log.error(sb.toString());
			}
			return;
		}

		// init deliver
		CmppDeliver deliverInstance = null;
		if (deliverClass != null && deliverClass.length() > 0 && !"none".equalsIgnoreCase(deliverClass)) {
			try {
				deliverInstance = (CmppDeliver) Class.forName(deliverClass).newInstance();
				log.info("get instance[" + deliverClass + "] success!");
			} catch (Exception e) {
				log.error("deliverClass[" + deliverClass + "] can't be instanced!");
				deliverInstance = null;
			}
		}

		cmppApi.init(host, port, corpId, passwd, srcPhoneNo, signLength, deliverInstance);

		try {
			cmppApi.connectTcp();
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("cmppApi connectTcp error:" + e.getMessage(), e);
			}
		}

	}

	/**
	 * 检查配置信息
	 * 
	 * @return
	 */
	protected boolean checkConfig() {
		if (host == null || host.trim().length() == 0 || port == 0 || corpId == null || corpId.trim().length() == 0
				|| passwd == null || passwd.trim().length() == 0 || srcPhoneNo == null
				|| srcPhoneNo.trim().length() == 0 || signLength == 0) {
			return false;
		} else {
			return true;
		}

	}

	@Override
	public SMSResult sms(String phoneRece, String notes) throws Exception {

		if (phoneRece == null || "".equals(phoneRece)) {
			return SMSResult.createFailtrueResult("电话号码 为空");
		}
		if (notes == null || "".equals(notes)) {
			return SMSResult.createFailtrueResult("短信 为空");
		}
		if (!checkConfig()) {
			return SMSResult.createFailtrueResult("缺少配置信息");
		}
		Map<String, Object> signDynamicMap = null;
		if (isSignDynamic) {
			for (Map<String, Object> map : this.signDynamicList) {
				String startStr = (String) map.get(KEY_START_STR);
				if (phoneRece.startsWith(startStr)) {
					signDynamicMap = map;
					if (log.isInfoEnabled()) {
						log.info("get sign Dynamic:" + startStr);
					}
					break;
				}
			}
			if (signDynamicMap == null) {
				return SMSResult.createFailtrueResult("找不到动态签名设置");
			}
		}

		CmppResult result = null;

		if (isSignDynamic) {
			String signTxt = (String) signDynamicMap.get(KEY_SIGN_TXT);
			if (signTxt == null) {
				signTxt = "";
			}
			Integer signLength = (Integer) signDynamicMap.get(KEY_SIGN_LENGTH);
			if (signLength == null) {
				return SMSResult.createFailtrueResult("动态签名长度不存在");
			}
			result = this.cmppApi.submitMsg(phoneRece, notes + signTxt, signLength);
		} else {
			result = this.cmppApi.submitMsg(phoneRece, notes);
		}

		if (result == null) {
			return SMSResult.createFailtrueResult("未知错误");
		} else if (result.isSuccess()) {
			return SMSResult.createSuccessResult();
		} else {
			// 出错
			return SMSResult.createFailtrueResult(result.getMessage());

		}

	}

	String getConfigPath() {
		return configPath;
	}

	void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCorpId() {
		return corpId;
	}

	public void setCorpId(String corpId) {
		this.corpId = corpId;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getSrcPhoneNo() {
		return srcPhoneNo;
	}

	public void setSrcPhoneNo(String srcPhoneNo) {
		this.srcPhoneNo = srcPhoneNo;
	}

	public void startThread() {
		// nothing should do...
	}

}
