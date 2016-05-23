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

import com.clschina.common.sms.smgp.SMGPApi;
import com.clschina.common.sms.smgp.SMGPResult;


/**
 * 访问百悟（）的访问接口进行处理
 * 
 * @author Wu Xiao Fei
 * 
 */
class BaiwuSmgpVisitService implements IVisitService {

	private static Log log = LogFactory.getLog(BaiwuSmgpVisitService.class);

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

	private String configPropertyPre;//

	private SMGPApi smgpApi = new SMGPApi();

	public BaiwuSmgpVisitService(String configPath, String configPropertyPre) {
		this.configPath = configPath;
		this.configPropertyPre = configPropertyPre;
		init();
	}

	/**
	 * 初始化，从配置文件中获取配置信息
	 */
	protected void init() {
		String filepath = getConfigPath();
		if (log.isInfoEnabled()) {
			log.info(this.configPropertyPre + " sms config file:" + filepath);
		}
		Properties p = new Properties();
		try {
			p.load(BaiwuSmgpVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error(this.configPropertyPre + " sms config file error:" + e.getMessage(), e);
			}
		}
		host = p.getProperty(this.configPropertyPre + ".host");
		port = Integer.parseInt(p.getProperty(this.configPropertyPre + ".port"));
		corpId = p.getProperty(this.configPropertyPre + ".corpId");
		passwd = p.getProperty(this.configPropertyPre + ".passwd");
		srcPhoneNo = p.getProperty(this.configPropertyPre + ".srcPhoneNo");
		signLength = Integer.parseInt(p.getProperty(this.configPropertyPre + ".signLength", "6"));// 默认值为6
		deliverClass = p.getProperty(this.configPropertyPre + ".deliverClass");

		name = p.getProperty("name");

		String signDynamic = p.getProperty(this.configPropertyPre + ".signDynamic");
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
				sb.append("[" + name + "]" + this.configPropertyPre + " sms config:\n");
				sb.append(".host:" + host + "\n");
				sb.append(".port:" + port + "\n");
				sb.append(".corpId:" + corpId + "\n");
				sb.append(".passwd:" + passwd + "\n");
				sb.append(".srcPhoneNo:" + srcPhoneNo + "\n");
				sb.append(".signLength:" + signLength + "\n");
				sb.append(".deliverClass:" + deliverClass + "\n");
				sb.append(".signDynamic:" + signDynamic + "\n");
				sb.append(".isSignDynamic:" + isSignDynamic + "\n");
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
				sb.append("[" + name + "]" + this.configPropertyPre + " sms config error:缺少配置信息\n");
				sb.append(".host:" + host + "\n");
				sb.append(".port:" + port + "\n");
				sb.append(".corpId:" + corpId + "\n");
				sb.append(".passwd:" + passwd + "\n");
				sb.append(".srcPhoneNo:" + srcPhoneNo + "\n");
				sb.append(".signLength:" + signLength + "\n");
				sb.append(".deliverClass:" + deliverClass + "\n");
				sb.append(".signDynamic:" + signDynamic + "\n");
				sb.append(".isSignDynamic:" + isSignDynamic + "\n");
				log.error(sb.toString());
			}
			return;
		}

		smgpApi.init(host, port, corpId, passwd, srcPhoneNo, signLength);

		try {
			smgpApi.connectTcp();
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("sgipApi connectTcp error:" + e.getMessage(), e);
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

		SMGPResult result = null;

		if (isSignDynamic) {
			String signTxt = (String) signDynamicMap.get(KEY_SIGN_TXT);
			if (signTxt == null) {
				signTxt = "";
			}
			Integer signLength = (Integer) signDynamicMap.get(KEY_SIGN_LENGTH);
			if (signLength == null) {
				return SMSResult.createFailtrueResult("动态签名长度不存在");
			}
			result = this.smgpApi.submitMsg(phoneRece, notes + signTxt, signLength);
		} else {
			result = this.smgpApi.submitMsg(phoneRece, notes);
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
