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

/**
 * 访问朗驰（www.lanz.net.cn）的访问接口进行处理
 * 
 * @author Wu Xiao Fei
 * 
 */
class LanzVisitService implements IVisitService {

	private static Log log = LogFactory.getLog(LanzVisitService.class);

	// 配置信息，以后全部放在配置文件中
	private String urlBundle;// 这个url，已经带有需要的参数，除了电话号码和发送内容为参数
	private String name;// 系统的标示，用来区分不同的信息

	private String configPath;

	public LanzVisitService(String configPath) {
		this.configPath = configPath;
		init();
	}

	/**
	 * 初始化，从配置文件中获取配置信息
	 */
	protected void init() {
		String filepath = getConfigPath();
		if (log.isInfoEnabled()) {
			log.info("Lanz sms config file:" + filepath);
		}
		Properties p = new Properties();
		try {
			p.load(LanzVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("Lanz sms config file error:" + e.getMessage(), e);
			}
		}
		urlBundle = p.getProperty("lanz.url");
		name = p.getProperty("name");

		if (checkConfig()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]Lanz sms config:\n");
				sb.append("lanz.url:" + urlBundle + "\n");
				log.info(sb.toString());
			}
		} else {
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]Lanz sms config error:缺少配置信息\n");
				sb.append("lanz.url:" + urlBundle + "\n");
				log.error(sb.toString());
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
	public SMSResult sms(String phoneRece, String notes) {

		if (!checkConfig()) {
			if (log.isErrorEnabled()) {
				log.error("短信发送失败:参数不全，未配置lanz.url");
			}
			return SMSResult.createFailtrueResult("短信发送失败:参数不全，未配置etonene.url", new RuntimeException());
		}

		if (phoneRece == null || "".equals(phoneRece)) {
			return SMSResult.createFailtrueResult("电话号码 为空", new RuntimeException());
		}
		if (notes == null || "".equals(notes)) {
			return SMSResult.createFailtrueResult("短信 为空", new RuntimeException());
		}

		String visitResultXML = null;//
		String resultCode;

		try {

			String visitUrl = urlBundle
					.replaceAll("\\{phoneRece\\}", phoneRece);
			visitUrl = visitUrl.replaceAll("\\{notes\\}",
					URLEncoder.encode(notes, "GB2312"));

			if (log.isInfoEnabled()) {
				log.info("请求URL:" + visitUrl);
			}

			URL url = new URL(visitUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");

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

			resultCode = visitResultXML.substring(visitResultXML
					.indexOf("ErrorNum") + 9);
			resultCode = resultCode.substring(0, resultCode.indexOf("<"));

		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("短信发送失败", e);
			}
			return SMSResult.createFailtrueResult("短信发送失败" + e.getMessage(), e);
		}
		if ("0".equalsIgnoreCase(resultCode)) {
			return SMSResult.createSuccessResult();
		} else {
			// 出错
			if ("1000".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：当前用户已经登录", new RuntimeException());
			} else if ("1001".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：当前用户没有登录", new RuntimeException());
			} else if ("1002".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode
						+ "：登录被拒绝（一般是账号和密码错误了）", new RuntimeException());
			} else if ("2001".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：短信发送失败", new RuntimeException());
			} else if ("2002".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：短信库存不足", new RuntimeException());
			} else if ("2003".equalsIgnoreCase(resultCode)) {
				return SMSResult
						.createFailtrueResult(resultCode + "：存在无效的手机号码", new RuntimeException());
			} else if ("2004".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode
						+ "：短信内容包含禁用词语", new RuntimeException());
			} else if ("3001".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：没有要接收的短信", new RuntimeException());
			} else if ("3002".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode
						+ "：没有要接收的回复状态", new RuntimeException());
			} else if ("9001".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode
						+ "：JobID参数不符合要求", new RuntimeException());
			} else if ("9002".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode
						+ "：SendDate或SendTime参数不是有效日期", new RuntimeException());
			} else if ("9003".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode
						+ "：短信内容长度超过300(短信内容为空也会报这个错误", new RuntimeException());
			} else if ("9004".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：参数不符合要求", new RuntimeException());
			} else if ("9099".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：其它系统错误", new RuntimeException());
			} else {
				return SMSResult.createFailtrueResult(resultCode + "：未知错误", new RuntimeException());
			}

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

}
