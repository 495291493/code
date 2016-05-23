/**
 * 
 */
package com.clschina.common.sms;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.StringUtil;

/**
 * 短信发送的对外的模块
 * 
 * @author Wu Xiao Fei
 * 
 */
public class SMSService {

	private static Log log = LogFactory.getLog(SMSService.class);

	// 分发器，目前以数字表示
	public final static int DISPATCHER_EMPP = 1;// 移动供应商
	public final static int DISPATCHER_LANZ = 2;// 浪驰供应商
	public final static int DISPATCHER_BAIWU_SGIP_YI_DONG = 3;// 百悟科技SGIP协议(移动)
	public final static int DISPATCHER_BAIWU_SGIP_LIAN_TONG = 4;// 百悟科技SGIP协议(联通)
	public final static int DISPATCHER_BAIWU_CMPP = 5;// 百悟科技CMPP协议
	public final static int DISPATCHER_BAIWU_BEIYONG = 6;// 百悟科技CMPP协议（备用）
	public final static int DISPATCHER_BAIWU_HTTP = 7;// 百悟科技HTTP协议（电信用）
	public final static int DISPATCHER_CLSCHINA_HTTP = 8;// clschina统一接口
	public final static int DISPATCHER_CLSCHINA_GUANGGAO_HTTP = 9;// clschina统一接口(广告用)
	public final static int DISPATCHER_BAIWU_SMGP = 10;// 百悟科技SMGP协议
	public final static int DISPATCHER_JIANZHOU_HTTP = 11;// 建周HTTP
	public final static int DISPATCHER_JIANZHOU_YIDONG_HTTP = 110;// 建周HTTP移动
	public final static int DISPATCHER_JIANZHOU_LIANTONG_HTTP = 111;// 建周HTTP联通
	public final static int DISPATCHER_JIANZHOU_DIANXIN_HTTP = 112;// 建周HTTP电信
	public final static int DISPATCHER_AUTO = 99;// 智能选择

	public static final String CONFIG_PATH = "/sms.properties";// 单例使用固定的配置文件

	private static SMSService instance = new SMSService();

	public static SMSService getInstance() {
		return instance;
	}

	private EmppVisitService emppVisitService = null;

	private LanzVisitService lanzVisitService = null;

	private BaiwuSgipVisitService baiwuSgipYiDongVisitService = null;
	private BaiwuSgipVisitService baiwuSgipLianTongVisitService = null;

	private BaiwuCmppVisitService baiwuCmppVisitService = null;

	private BaiwuCmppVisitService baiwuBeiYongVisitService = null;// 备份账号

	private BaiwuHttpVisitService baiwuHttpVisitService = null;

	private ClschinaHttpVisitService clschinaHttpVisitService = null;

	private ClschinaHttpVisitService clschinaGuangGaoHttpVisitService = null;

	private BaiwuSmgpVisitService baiwuSmgpVisitService = null;

	private JianzhouHttpVisitService jianzhouHttpVisitService = null;
	private JianzhouHttpVisitService jianzhouHttpYidongVisitService = null;
	private JianzhouHttpVisitService jianzhouHttpLiantongVisitService = null;
	private JianzhouHttpVisitService jianzhouHttpDianxinVisitService = null;

	// 智能选择的相关参数
	private int defaultDispatcher = DISPATCHER_AUTO;// 默认
	private Set<String> prefix4TargetBaiwuCmpp;
	private Set<String> prefix4TargetBaiwuBeiYong;
	private Set<String> prefix4TargetBaiwuHttp;
	private Set<String> prefix4TargetBaiwuSgipYiDong;
	private Set<String> prefix4TargetBaiwuSgipLianTong;
	private Set<String> prefix4TargetBaiwuSmgp;
	private Set<String> prefix4TargetJianzhouHttp;
	
	private Set<String> prefix4TargetJianzhouHttpYidong;
	private Set<String> prefix4TargetJianzhouHttpLiantong;
	private Set<String> prefix4TargetJianzhouHttpDianxin;

	private String name;

	private String gudingMsg;// 固定的短信后缀（使用所有接口都有）

	public SMSService() {
		try {
			init();
		} catch (Exception e) {
			log.error("初始化短信服务错误", e);
		}
	}

	protected void init() {
		String filepath = CONFIG_PATH;
		if (log.isInfoEnabled()) {
			log.info("service config file:" + filepath);
		}
		Properties p = new Properties();
		try {
			p.load(LanzVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("service config file error:" + e.getMessage(), e);
			}
		}

		name = p.getProperty("name");
		gudingMsg = p.getProperty("gudingMsg", "");
		if (gudingMsg == null) {
			gudingMsg = "";
		}

		defaultDispatcher = Integer.parseInt(p.getProperty("service.defaultDispatcher"));
		String prefix4TargetBaiwuCmppStr = p.getProperty("dispatcher.99.target.baiwuCmpp");
		String prefix4TargetbaiwuBeiYongStr = p.getProperty("dispatcher.99.target.baiwuBeiYong");
		String prefix4TargetbaiwuHttpStr = p.getProperty("dispatcher.99.target.baiwuHttp");
		String prefix4TargetbaiwuSgipYiDongStr = p.getProperty("dispatcher.99.target.baiwuSgipYiDong");
		String prefix4TargetbaiwuSgipLianTongStr = p.getProperty("dispatcher.99.target.baiwuSgipLianTong");
		String prefix4TargetbaiwuSmgpStr = p.getProperty("dispatcher.99.target.baiwuSmgp");
		String prefix4TargetJianzhouHttp = p.getProperty("dispatcher.99.target.jianzhouHttp");
		
		String prefix4TargetJianzhouHttpYidong = p.getProperty("dispatcher.99.target.jianzhouHttpYidong");
		String prefix4TargetJianzhouHttpLiantong = p.getProperty("dispatcher.99.target.jianzhouHttpLiantong");
		String prefix4TargetJianzhouHttpDianxin = p.getProperty("dispatcher.99.target.jianzhouHttpDianxin");

		if (log.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("service config:\n");
			sb.append("service.defaultDispatcher:" + defaultDispatcher + "\n");
			sb.append("dispatcher.99.target.baiwuCmpp:" + prefix4TargetBaiwuCmppStr + "\n");
			sb.append("dispatcher.99.target.baiwuBeiYong:" + prefix4TargetbaiwuBeiYongStr + "\n");
			sb.append("dispatcher.99.target.baiwuHttp:" + prefix4TargetbaiwuHttpStr + "\n");
			sb.append("dispatcher.99.target.baiwuSgipYiDong:" + prefix4TargetbaiwuSgipYiDongStr + "\n");
			sb.append("dispatcher.99.target.baiwuSgipLianTong:" + prefix4TargetbaiwuSgipLianTongStr + "\n");
			sb.append("dispatcher.99.target.baiwuSmgp:" + prefix4TargetbaiwuSmgpStr + "\n");
			sb.append("dispatcher.99.target.jianzhouHttp:" + prefix4TargetJianzhouHttp + "\n");
			sb.append("gudingMsg:" + gudingMsg + "\n");
			log.info(sb.toString());
		}

		this.prefix4TargetBaiwuCmpp = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetBaiwuCmppStr)) {
			String[] prefix4TargetEmppArray = prefix4TargetBaiwuCmppStr.split("\\,");
			for (String string : prefix4TargetEmppArray) {
				this.prefix4TargetBaiwuCmpp.add(string.trim());
			}
		}

		this.prefix4TargetBaiwuBeiYong = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetbaiwuBeiYongStr)) {
			String[] prefix4TargetLanzArray = prefix4TargetbaiwuBeiYongStr.split("\\,");
			for (String string : prefix4TargetLanzArray) {
				this.prefix4TargetBaiwuBeiYong.add(string.trim());
			}
		}

		this.prefix4TargetBaiwuHttp = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetbaiwuHttpStr)) {
			String[] prefix4TargetHttpArray = prefix4TargetbaiwuHttpStr.split("\\,");
			for (String string : prefix4TargetHttpArray) {
				this.prefix4TargetBaiwuHttp.add(string.trim());
			}
		}
		this.prefix4TargetBaiwuSgipYiDong = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetbaiwuSgipYiDongStr)) {
			String[] prefix4TargetSgipArray = prefix4TargetbaiwuSgipYiDongStr.split("\\,");
			for (String string : prefix4TargetSgipArray) {
				this.prefix4TargetBaiwuSgipYiDong.add(string.trim());
			}
		}
		this.prefix4TargetBaiwuSgipLianTong = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetbaiwuSgipLianTongStr)) {
			String[] prefix4TargetSgipArray = prefix4TargetbaiwuSgipLianTongStr.split("\\,");
			for (String string : prefix4TargetSgipArray) {
				this.prefix4TargetBaiwuSgipLianTong.add(string.trim());
			}
		}
		this.prefix4TargetBaiwuSmgp = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetbaiwuSmgpStr)) {
			String[] prefix4TargetSmgpArray = prefix4TargetbaiwuSmgpStr.split("\\,");
			for (String string : prefix4TargetSmgpArray) {
				this.prefix4TargetBaiwuSmgp.add(string.trim());
			}
		}

		this.prefix4TargetJianzhouHttp = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetJianzhouHttp)) {
			String[] prefix4TargetSmgpArray = prefix4TargetJianzhouHttp.split("\\,");
			for (String string : prefix4TargetSmgpArray) {
				this.prefix4TargetJianzhouHttp.add(string.trim());
			}
		}
		
		this.prefix4TargetJianzhouHttpYidong = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetJianzhouHttpYidong)) {
			String[] prefix4TargetSmgpArray = prefix4TargetJianzhouHttpYidong.split("\\,");
			for (String string : prefix4TargetSmgpArray) {
				this.prefix4TargetJianzhouHttpYidong.add(string.trim());
			}
		}
		
		this.prefix4TargetJianzhouHttpLiantong = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetJianzhouHttpLiantong)) {
			String[] prefix4TargetSmgpArray = prefix4TargetJianzhouHttpLiantong.split("\\,");
			for (String string : prefix4TargetSmgpArray) {
				this.prefix4TargetJianzhouHttpLiantong.add(string.trim());
			}
		}
		
		this.prefix4TargetJianzhouHttpDianxin = new HashSet<String>();
		if (!StringUtil.isNullOrEmpty(prefix4TargetJianzhouHttpDianxin)) {
			String[] prefix4TargetSmgpArray = prefix4TargetJianzhouHttpDianxin.split("\\,");
			for (String string : prefix4TargetSmgpArray) {
				this.prefix4TargetJianzhouHttpDianxin.add(string.trim());
			}
		}
	}

	/**
	 * 发送短信,使用默认分发
	 * 
	 * @param phoneRece
	 * @param notes
	 * @return
	 */
	public SMSResult sms(String phoneRece, String notes) {
		return sms(phoneRece, notes, this.defaultDispatcher);
	}

	public SMSResult sms(String phoneRece, String notes, int dispatcher) {
		return sms(phoneRece, notes, dispatcher, name);
	}

	/**
	 * 发送短信
	 * 
	 * @param phoneRece
	 *            ，接受人电话
	 * @param notes
	 *            短信内容
	 * @return
	 */
	public SMSResult sms(String phoneRece, String notes, int dispatcher, String clientName) {

		if (gudingMsg != null && gudingMsg.trim().length() > 0) {
			if (!notes.endsWith(gudingMsg) 
					|| !notes.startsWith(gudingMsg)) {// 不重复添加
				String lastChar = notes.substring(notes.length() - 1);
				if ("。".equals(lastChar) || "！".equals(lastChar) || "？".equals(lastChar) || ".".equals(lastChar)
						|| "!".equals(lastChar) || "?".equals(lastChar)) {
					notes += gudingMsg;
				} else {
					notes += "。" + gudingMsg;
				}
			}
		}

		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("[" + clientName + "]send:\n");
			sb.append("phoneRece:" + phoneRece + "\n");
			sb.append("notes:" + notes + "\n");
			sb.append("dispatcher:" + dispatcher + "\n");
			log.debug(sb.toString());
		}

		// 10、11、12开头的手机号码，直接返回成功，不发短信；这些号码不存在，都是测试用的
		if (phoneRece.startsWith("10") || phoneRece.startsWith("11") || phoneRece.startsWith("12")) {
			if (log.isInfoEnabled()) {
				log.info("测试用号码" + phoneRece + "， 不实际发送短信。");
			}
			return SMSResult.createSuccessResult();
		}

		int sendDispatcher = dispatcher;
		switch (sendDispatcher) {
		case DISPATCHER_EMPP:
			break;
		case DISPATCHER_LANZ:
			break;
		case DISPATCHER_BAIWU_SGIP_YI_DONG:
			break;
		case DISPATCHER_BAIWU_CMPP:
			break;
		case DISPATCHER_BAIWU_BEIYONG:
			break;
		case DISPATCHER_BAIWU_HTTP:
			break;
		case DISPATCHER_CLSCHINA_HTTP:
			break;
		case DISPATCHER_CLSCHINA_GUANGGAO_HTTP:
			break;
		case DISPATCHER_BAIWU_SMGP:
			break;
		case DISPATCHER_JIANZHOU_HTTP:
			break;
		case DISPATCHER_AUTO:
			sendDispatcher = autoDispatcherResult(phoneRece);

			break;

		default:
			// 如果是非法的，使用EMPP
			sendDispatcher = defaultDispatcher;
			if (sendDispatcher == DISPATCHER_AUTO) {
				sendDispatcher = autoDispatcherResult(phoneRece);
			}

			break;
		}
		SMSResult result = null;
		try {
			if (DISPATCHER_EMPP == sendDispatcher) {
				result = getEmppVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_LANZ == sendDispatcher) {
				result = getLanzVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_BAIWU_SGIP_YI_DONG == sendDispatcher) {
				result = getBaiwuSgipYiDongVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_BAIWU_SGIP_LIAN_TONG == sendDispatcher) {
				result = getBaiwuSgipLianTongVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_BAIWU_CMPP == sendDispatcher) {
				result = getBaiwuCmppVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_BAIWU_BEIYONG == sendDispatcher) {
				result = getBaiwuBeiYongVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_BAIWU_HTTP == sendDispatcher) {
				result = getBaiwuHttpVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_CLSCHINA_HTTP == sendDispatcher) {
				result = getClschinaHttpVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_CLSCHINA_GUANGGAO_HTTP == sendDispatcher) {
				result = getClschinaGuangGaoHttpVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_BAIWU_SMGP == sendDispatcher) {
				result = getBaiwuSmgpVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_JIANZHOU_HTTP == sendDispatcher) {
				result = getJianzhouHttpVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_JIANZHOU_YIDONG_HTTP == sendDispatcher) {
				result = getJianzhouHttpYidongVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_JIANZHOU_LIANTONG_HTTP == sendDispatcher) {
				result = getJianzhouHttpLiantongVisitService().sms(phoneRece, notes);
			} else if (DISPATCHER_JIANZHOU_DIANXIN_HTTP == sendDispatcher) {
				result = getJianzhouHttpDianxinVisitService().sms(phoneRece, notes);
			}

		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("[" + clientName + "]发送短信出错:" + e.getMessage());
			}
			result = SMSResult.createFailtrueResult("发送短信出错:" + e.getMessage(), e);

		}
		if (result.isNotSuccess()) {
			if (log.isWarnEnabled()) {
				// 记录为发送成功的错误
				log.warn("发送短信出错:sendDispatcher=" + sendDispatcher + ", client=" + clientName + ", resultCode="
						+ result.getCode() + ", resultMessage=" + result.getMessage() + "(phone=" + phoneRece
						+ "; content=" + notes + ")", new RuntimeException());
			}
		}
		if (log.isDebugEnabled()) {// 改日志进入单独日志文件
			StringBuffer sb = new StringBuffer();
			sb.append("[" + clientName + "]send result:\n");
			sb.append("code(0-success;1-failure):" + result.getCode() + "\n");
			sb.append("msg:" + result.getMessage() + "\n");
			sb.append("phoneRece:" + phoneRece + "\n");
			sb.append("notes:" + notes + "\n");
			sb.append("sendDispatcher:" + sendDispatcher + "\n");
			log.debug(sb.toString());

		}

		return result;
	}

	/**
	 * 发送短信,使用默认分发，如果不成功，可以重试
	 * 
	 * @param phoneRece
	 * @param notes
	 * @return
	 */
	public SMSResult smsWithRetry(String phoneRece, String notes) {
		return smsWithRetry(phoneRece, notes, 3);// 这里的3次数，不是分发器
	}

	/**
	 * 发送短信,使用默认分发，如果不成功，可以重试
	 * 
	 * @param phoneRece
	 * @param notes
	 * @param retryNum
	 *            重试次数
	 * @return
	 */
	public SMSResult smsWithRetry(String phoneRece, String notes, int retryNum) {
		SMSResult result = null;
		
		for(int i=0; i<= retryNum; i++){
			if (i > 0 && log.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("第" + (i + 1) + "次重发：\n");
				sb.append("phoneRece:" + phoneRece + "\n");
				sb.append("notes:" + notes + "\n");
				log.debug(sb.toString());

			}
			
			result = sms(phoneRece, notes);
			if (result.isSuccess()) {
				return result;
			}
			
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				//一般都是tomcat shutdown 时触发此事件，退出程序。
				if (log.isDebugEnabled()) {
					log.debug("Thread.sleep error" + e.getMessage(), e);
				}
				break;
			}
			
		}
		
		if(result == null || result.isNotSuccess()){
			//未发送成功 记录错误日志；
			if(log.isErrorEnabled()){
				log.error("发送短信失败。" + phoneRece + "; " + notes + "; 尝试" + retryNum + "次；" + result.toString(),
							result.getException() == null ? new RuntimeException("发送短信失败。") : result.getException());
			}
		}
		
		return result;

	}

	public int autoDispatcherResult(String phoneRece) {
		int resultDispatcher = DISPATCHER_BAIWU_CMPP;// 默认使用
		String prefix = phoneRece.substring(0, 3);
		if (this.prefix4TargetBaiwuCmpp.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_CMPP;
		} else if (this.prefix4TargetBaiwuBeiYong.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_BEIYONG;
		} else if (this.prefix4TargetBaiwuHttp.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_HTTP;
		} else if (this.prefix4TargetBaiwuSgipYiDong.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_SGIP_YI_DONG;
		} else if (this.prefix4TargetBaiwuSgipLianTong.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_SGIP_LIAN_TONG;
		} else if (this.prefix4TargetBaiwuSmgp.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_SMGP;
		} else if (this.prefix4TargetJianzhouHttp.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_HTTP;
		} else if (this.prefix4TargetJianzhouHttpYidong.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_YIDONG_HTTP;
		} else if (this.prefix4TargetJianzhouHttpLiantong.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_LIANTONG_HTTP;
		} else if (this.prefix4TargetJianzhouHttpDianxin.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_DIANXIN_HTTP;
		}
		// 四位判断（目前就是）
		prefix = phoneRece.substring(0, 4);
		if (this.prefix4TargetBaiwuCmpp.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_CMPP;
		} else if (this.prefix4TargetBaiwuBeiYong.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_BEIYONG;
		} else if (this.prefix4TargetBaiwuHttp.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_HTTP;
		} else if (this.prefix4TargetBaiwuSgipYiDong.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_SGIP_YI_DONG;
		} else if (this.prefix4TargetBaiwuSgipLianTong.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_SGIP_LIAN_TONG;
		} else if (this.prefix4TargetBaiwuSmgp.contains(prefix)) {
			resultDispatcher = DISPATCHER_BAIWU_SMGP;
		} else if (this.prefix4TargetJianzhouHttp.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_HTTP;
		} else if (this.prefix4TargetJianzhouHttpYidong.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_YIDONG_HTTP;
		} else if (this.prefix4TargetJianzhouHttpLiantong.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_LIANTONG_HTTP;
		} else if (this.prefix4TargetJianzhouHttpDianxin.contains(prefix)) {
			resultDispatcher = DISPATCHER_JIANZHOU_DIANXIN_HTTP;
		}

		return resultDispatcher;
	}

	// --service的单例方法-----------------
	protected synchronized EmppVisitService getEmppVisitService() {
		if (emppVisitService == null) {
			emppVisitService = new EmppVisitService(CONFIG_PATH);
		}
		return emppVisitService;
	}

	protected synchronized LanzVisitService getLanzVisitService() {
		if (lanzVisitService == null) {
			lanzVisitService = new LanzVisitService(CONFIG_PATH);
		}
		return lanzVisitService;
	}

	protected synchronized BaiwuCmppVisitService getBaiwuCmppVisitService() {
		if (baiwuCmppVisitService == null) {
			baiwuCmppVisitService = new BaiwuCmppVisitService(CONFIG_PATH, "baiwuCmpp");
		}
		return baiwuCmppVisitService;
	}

	protected synchronized BaiwuCmppVisitService getBaiwuBeiYongVisitService() {
		if (baiwuBeiYongVisitService == null) {
			baiwuBeiYongVisitService = new BaiwuCmppVisitService(CONFIG_PATH, "baiwuBeiYong");
		}
		return baiwuBeiYongVisitService;
	}

	public void startJieshouThread() {
		getBaiwuCmppVisitService().startThread();
		getBaiwuBeiYongVisitService().startThread();
	}

	public BaiwuHttpVisitService getBaiwuHttpVisitService() {
		if (baiwuHttpVisitService == null) {
			baiwuHttpVisitService = new BaiwuHttpVisitService(CONFIG_PATH);
		}
		return baiwuHttpVisitService;
	}

	public ClschinaHttpVisitService getClschinaHttpVisitService() {
		if (clschinaHttpVisitService == null) {
			clschinaHttpVisitService = new ClschinaHttpVisitService(CONFIG_PATH, "clschinaHttp");
		}
		return clschinaHttpVisitService;
	}

	public int getDefaultDispatcher() {
		return defaultDispatcher;
	}

	public BaiwuSgipVisitService getBaiwuSgipYiDongVisitService() {
		if (baiwuSgipYiDongVisitService == null) {
			baiwuSgipYiDongVisitService = new BaiwuSgipVisitService(CONFIG_PATH, "baiwuSgipYiDong");
		}
		return baiwuSgipYiDongVisitService;
	}

	public BaiwuSgipVisitService getBaiwuSgipLianTongVisitService() {
		if (baiwuSgipLianTongVisitService == null) {
			baiwuSgipLianTongVisitService = new BaiwuSgipVisitService(CONFIG_PATH, "baiwuSgipLianTong");
		}
		return baiwuSgipLianTongVisitService;
	}

	public ClschinaHttpVisitService getClschinaGuangGaoHttpVisitService() {
		if (clschinaGuangGaoHttpVisitService == null) {
			clschinaGuangGaoHttpVisitService = new ClschinaHttpVisitService(CONFIG_PATH, "clschinaGuangGaoHttp");
		}
		return clschinaGuangGaoHttpVisitService;
	}

	public BaiwuSmgpVisitService getBaiwuSmgpVisitService() {
		if (baiwuSmgpVisitService == null) {
			baiwuSmgpVisitService = new BaiwuSmgpVisitService(CONFIG_PATH, "baiwuSmgp");
		}
		return baiwuSmgpVisitService;
	}

	public JianzhouHttpVisitService getJianzhouHttpVisitService() {
		if (jianzhouHttpVisitService == null) {
			jianzhouHttpVisitService = new JianzhouHttpVisitService(CONFIG_PATH, "jianzhouHttp");
		}
		return jianzhouHttpVisitService;
	}
	
	public JianzhouHttpVisitService getJianzhouHttpYidongVisitService() {
		if (jianzhouHttpYidongVisitService == null) {
			jianzhouHttpYidongVisitService = new JianzhouHttpVisitService(CONFIG_PATH, "jianzhouHttpYidong");
		}
		return jianzhouHttpYidongVisitService;
	}
	
	public JianzhouHttpVisitService getJianzhouHttpDianxinVisitService() {
		if (jianzhouHttpDianxinVisitService == null) {
			jianzhouHttpDianxinVisitService = new JianzhouHttpVisitService(CONFIG_PATH, "jianzhouHttpDianxin");
		}
		return jianzhouHttpDianxinVisitService;
	}
	
	public JianzhouHttpVisitService getJianzhouHttpLiantongVisitService() {
		if (jianzhouHttpLiantongVisitService == null) {
			jianzhouHttpLiantongVisitService = new JianzhouHttpVisitService(CONFIG_PATH, "jianzhouHttpLiantong");
		}
		return jianzhouHttpLiantongVisitService;
	}

}
