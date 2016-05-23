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
class BaiwuHttpVisitService implements IVisitService {

	private static Log log = LogFactory.getLog(BaiwuHttpVisitService.class);

	// 配置信息，以后全部放在配置文件中
	private String urlBundle;// 这个url，已经带有需要的参数，除了电话号码和发送内容为参数
	private String paramBundle;
	private String name;// 系统的标示，用来区分不同的信息

	private String configPath;

	public BaiwuHttpVisitService(String configPath) {
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
			p.load(BaiwuHttpVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("Lanz sms config file error:" + e.getMessage(), e);
			}
		}
		urlBundle = p.getProperty("baiwuHttp.url");
		paramBundle = p.getProperty("baiwuHttp.param");
		name = p.getProperty("name");

		if (checkConfig()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]baiwuHttp sms config:\n");
				sb.append("baiwuHttp.url:" + urlBundle + "\n");
				sb.append("baiwuHttp.param:" + paramBundle + "\n");
				log.info(sb.toString());
			}
		} else {
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]baiwuHttp sms config error:缺少配置信息\n");
				sb.append("baiwuHttp.url:" + urlBundle + "\n");
				sb.append("baiwuHttp.param:" + paramBundle + "\n");
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
	public SMSResult sms(String phoneRece, String notes) throws Exception {

		if (!checkConfig()) {
			throw new Exception("短信发送失败:参数不全，未配置baiwuHttp.url");
		}

		if (phoneRece == null || "".equals(phoneRece)) {
			return SMSResult.createFailtrueResult("电话号码 为空");
		}
		if (notes == null || "".equals(notes)) {
			return SMSResult.createFailtrueResult("短信 为空");
		}

		String visitResultXML = null;//
		String resultCode;

		String visitUrl = urlBundle;

		String paramUrl = paramBundle.replaceAll("\\{phoneRece\\}", phoneRece);
		paramUrl = paramUrl.replaceAll("\\{notes\\}", notes);

		if (log.isInfoEnabled()) {
			log.info("请求URL:" + visitUrl + "?" + paramUrl);
		}

		URL url = new URL(visitUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");

		conn.getOutputStream().write(paramUrl.getBytes("GBK"));

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

		if (resultCode != null && resultCode.startsWith("0")) {
			return SMSResult.createSuccessResult();
		} else {
			// 出错
			if ("1000".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：当前用户已经登录");
			} else if ("100".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：余额不足");
			} else if ("101".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：账号关闭");
			} else if ("102".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：短信内容超过500字或为空或内容编码格式不正确");
			} else if ("103".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：手机号码超过50个或合法的手机号码为空");
			} else if ("104".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：用户访问时间间隔低于50毫秒");
			} else if ("105".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：用户访问方式不是post方式");
			} else if ("106".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：用户名不存在");
			} else if ("107".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：密码错误");
			} else if ("108".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：指定访问ip错误	");
			} else if ("109".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：业务不存在");
			} else if ("110".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：小号不合法");
			} else if ("111".equalsIgnoreCase(resultCode)) {
				return SMSResult.createFailtrueResult(resultCode + "：短信内容内有敏感词");
			} else {
				return SMSResult.createFailtrueResult(resultCode + "：未知错误");
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
