/**
 * 
 */
package com.clschina.common.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.StringUtil;

/**
 * 访问建周（www.shjianzhou.com）的访问接口进行处理
 * 
 * @author Wu Xiao Fei
 * 
 */
class JianzhouHttpVisitService implements IVisitService {
	public static String LOCATION_TOP = "top";
	public static String LOCATION_BOTTOM = "bottom";
	private static Log log = LogFactory.getLog(JianzhouHttpVisitService.class);

	// 配置信息，以后全部放在配置文件中
	private String urlBundle;// 这个url，已经带有需要的参数，除了电话号码和发送内容为参数
	private String paramBundle;
	private String name;// 系统的标示，用来区分不同的信息
	private String gudingMsg;// 固定签名
	private String gudingMsgLocation;//固定签名的位置
	private String serverName;

	private String configPath;

	public JianzhouHttpVisitService(String configPath, String serName) {
		this.configPath = configPath;
		this.serverName = serName;
		init();
	}

	/**
	 * 初始化，从配置文件中获取配置信息
	 */
	protected void init() {
		String filepath = getConfigPath();
		if (log.isInfoEnabled()) {
			log.info("Jianzhou sms config file:" + filepath);
		}
		Properties p = new Properties();
		try {
			p.load(JianzhouHttpVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("Jianzhou sms config file error:" + e.getMessage(), e);
			}
		}
		urlBundle = p.getProperty(serverName+".url");
		paramBundle = p.getProperty(serverName+".param");
		gudingMsg = p.getProperty(serverName+".gudingMsg");
		gudingMsgLocation = p.getProperty(serverName+".gudingMsgLocation");
		if (StringUtil.isNullOrEmpty(gudingMsg)) {
			gudingMsg = "";
		}
		name = p.getProperty("name");

		if (checkConfig()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]jianzhouHttp sms config:\n");
				sb.append(serverName+".url:" + urlBundle + "\n");
				sb.append(serverName+".param:" + paramBundle + "\n");
				sb.append(serverName+".gudingMsg:" + gudingMsg + "\n");
				sb.append(serverName+".gudingMsgLocation:" + gudingMsgLocation + "\n");
				log.info(sb.toString());
			}
		} else {
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]jianzhouHttp sms config error:缺少配置信息\n");
				sb.append(serverName+".url:" + urlBundle + "\n");
				sb.append(serverName+".param:" + paramBundle + "\n");
				sb.append(serverName+".gudingMsg:" + gudingMsg + "\n");
				sb.append(serverName+".gudingMsgLocation:" + gudingMsgLocation + "\n");
				log.error(sb.toString(), new RuntimeException());
			}
		}

	}

	/**
	 * 检查配置信息
	 * 
	 * @return
	 */
	protected boolean checkConfig() {
		if (urlBundle == null || urlBundle.trim().length() == 0) {
			return false;
		} else {
			return true;
		}

	}

	@Override
	public SMSResult sms(String phoneRece, String notes) throws Exception {

		if (!checkConfig()) {
			throw new Exception("短信发送失败:参数不全，未配置jianzhouHttp.url");
		}

		if (phoneRece == null || "".equals(phoneRece)) {
			return SMSResult.createFailtrueResult("电话号码 为空", new RuntimeException());
		}
		if (notes == null || "".equals(notes)) {
			return SMSResult.createFailtrueResult("短信 为空", new RuntimeException());
		}

		String visitResultXML = null;//
		String resultCode;

		String visitUrl = urlBundle;

		String paramUrl = paramBundle.replaceAll("\\{phoneRece\\}", phoneRece);
		if(gudingMsgLocation != null && gudingMsgLocation.trim().equalsIgnoreCase(LOCATION_TOP)){
			notes = gudingMsg + notes;
		}else{
			notes = notes + gudingMsg;
		}
		paramUrl = paramUrl.replaceAll("\\{notes\\}", notes);

		if (log.isInfoEnabled()) {
			log.info("请求URL:" + visitUrl + "?" + paramUrl);
		}

		URL url = new URL(visitUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");

		conn.getOutputStream().write(paramUrl.getBytes("utf-8"));

		conn.getOutputStream().flush();
		conn.getOutputStream().close();
		InputStream in = conn.getInputStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(in));

		if (rd.ready()) {
			visitResultXML = rd.readLine();
		}
		if (log.isInfoEnabled()) {
			log.info("请求URL:" + visitResultXML);
		}

		resultCode = visitResultXML;

		if (resultCode != null) {
			int iResultCode = Integer.parseInt(resultCode);
			if (iResultCode > 0) {
				return SMSResult.createSuccessResult();
			} else {
				switch (iResultCode) {
				case -1:
					return SMSResult.createFailtrueResult(resultCode + "：余额不足", new RuntimeException());
				case -2:
					return SMSResult.createFailtrueResult(resultCode + "：帐号或密码错误", new RuntimeException());
				case -3:
					return SMSResult.createFailtrueResult(resultCode + "：连接服务商失败", new RuntimeException());
				case -4:
					return SMSResult.createFailtrueResult(resultCode + "：超时", new RuntimeException());
				case -5:
					return SMSResult.createFailtrueResult(resultCode + "：其他错误，一般为网络问题，IP受限等", new RuntimeException());
				case -6:
					return SMSResult.createFailtrueResult(resultCode + "：短信内容为空", new RuntimeException());
				case -7:
					return SMSResult.createFailtrueResult(resultCode + "：目标号码为空", new RuntimeException());
				case -8:
					return SMSResult.createFailtrueResult(resultCode + "：用户通道设置不对，需要设置三个通道", new RuntimeException());
				case -9:
					return SMSResult.createFailtrueResult(resultCode + "：捕获未知异常", new RuntimeException());
				case -10:
					return SMSResult.createFailtrueResult(resultCode + "：超过最大定时时间限制", new RuntimeException());
				case -11:
					return SMSResult.createFailtrueResult(resultCode + "：目标号码在黑名单里", new RuntimeException());
				case -12:
					return SMSResult.createFailtrueResult(resultCode + "：消息内容包含禁用词语", new RuntimeException());
				case -13:
					return SMSResult.createFailtrueResult(resultCode + "：没有权限使用该网关", new RuntimeException());
				case -14:
					return SMSResult.createFailtrueResult(resultCode + "：找不到对应的Channel ID", new RuntimeException());
				case -17:
					return SMSResult.createFailtrueResult(resultCode + "：没有提交权限，客户端帐号无法使用接口提交", new RuntimeException());
				case -18:
					return SMSResult.createFailtrueResult(resultCode + "：提交参数名称不正确或确少参数", new RuntimeException());
				case -19:
					return SMSResult.createFailtrueResult(resultCode + "：必须为POST提交", new RuntimeException());
				case -20:
					return SMSResult.createFailtrueResult(resultCode + "：超速提交(一般为每秒一次提交)", new RuntimeException());

				default:
					return SMSResult.createFailtrueResult(resultCode + "：未知错误", new RuntimeException());
				}
			}
		} else {
			return SMSResult.createFailtrueResult(resultCode + "：未知错误", new RuntimeException());
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

	String getGudingMsg() {
		return gudingMsg;
	}

	void setGudingMsg(String gudingMsg) {
		this.gudingMsg = gudingMsg;
	}

}
