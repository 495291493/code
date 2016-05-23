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
 * 访问统一接口的访问接口进行处理
 * 
 * @author Wu Xiao Fei
 * 
 */
class ClschinaHttpVisitService implements IVisitService {

	private static Log log = LogFactory.getLog(ClschinaHttpVisitService.class);

	// 配置信息，以后全部放在配置文件中
	private String urlBundle;// 这个url，已经带有需要的参数，除了电话号码和发送内容为参数
	private String paramBundle;
	private String name;// 系统的标示，用来区分不同的信息

	private String configPath;
	private String configPropertyPre;//

	public ClschinaHttpVisitService(String configPath, String configPropertyPre) {
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
			p.load(ClschinaHttpVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error(this.configPropertyPre + " sms config file error:" + e.getMessage(), e);
			}
		}
		urlBundle = p.getProperty(this.configPropertyPre + ".url");
		paramBundle = p.getProperty(this.configPropertyPre + ".param");
		name = p.getProperty("name");

		if (checkConfig()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]" + this.configPropertyPre + " sms config:\n");
				sb.append(this.configPropertyPre + ".url:" + urlBundle + "\n");
				sb.append(this.configPropertyPre + ".param:" + paramBundle + "\n");
				log.info(sb.toString());
			}
		} else {
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]" + this.configPropertyPre + " sms config error:缺少配置信息\n");
				sb.append(this.configPropertyPre + ".url:" + urlBundle + "\n");
				sb.append(this.configPropertyPre + ".param:" + paramBundle + "\n");
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
			throw new Exception("短信发送失败:参数不全，未配置" + this.configPropertyPre + ".url");
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

		String paramUrl = paramBundle.replaceAll("\\{phoneRece\\}", URLEncoder.encode(phoneRece, "UTF-8"));
		paramUrl = paramUrl.replaceAll("\\{notes\\}", URLEncoder.encode(notes, "UTF-8"));
		paramUrl = paramUrl.replaceAll("\\{clientName\\}", URLEncoder.encode(name, "UTF-8"));

		if (log.isInfoEnabled()) {
			log.info("请求URL:" + visitUrl + "?" + paramUrl);
		}

		URL url = new URL(visitUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(3 * 1000);// 超时
		conn.setReadTimeout(5 * 1000);// 超时

		conn.getOutputStream().write(paramUrl.getBytes());

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

		if (resultCode != null && resultCode.startsWith("00")) {
			return SMSResult.createSuccessResult();
		} else {
			// 出错
			return SMSResult.createFailtrueResult(resultCode);
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
