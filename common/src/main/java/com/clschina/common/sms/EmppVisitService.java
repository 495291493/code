/**
 * 
 */
package com.clschina.common.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wondertek.esmp.esms.empp.EMPPConnectResp;
import com.wondertek.esmp.esms.empp.EMPPData;
import com.wondertek.esmp.esms.empp.EMPPDeliver;
import com.wondertek.esmp.esms.empp.EMPPDeliverReport;
import com.wondertek.esmp.esms.empp.EMPPObject;
import com.wondertek.esmp.esms.empp.EMPPRecvListener;
import com.wondertek.esmp.esms.empp.EMPPReqNoticeResp;
import com.wondertek.esmp.esms.empp.EMPPShortMsg;
import com.wondertek.esmp.esms.empp.EMPPSubmitSM;
import com.wondertek.esmp.esms.empp.EMPPSubmitSMResp;
import com.wondertek.esmp.esms.empp.EMPPSyncAddrBookResp;
import com.wondertek.esmp.esms.empp.EMPPTerminate;
import com.wondertek.esmp.esms.empp.EMPPUnAuthorization;
import com.wondertek.esmp.esms.empp.EmppApi;

/**
 * 
 * 访问移动的短信接口
 * 
 * @author Wu Xiao Fei
 * 
 */
class EmppVisitService implements IVisitService {

	private static Log log = LogFactory.getLog(EmppVisitService.class);

	// 配置信息，以后全部放在配置文件中
	private String host;
	private int port;
	private String accountId;
	private String password;
	private String serviceId;
	private String name;// 系统的标示，用来区分不同的信息

	private String configPath;

	private EmppApi emppApi = new EmppApi();
	private EmppListener emppListener = new EmppListener(emppApi);

	public EmppVisitService(String configPath) {
		this.configPath = configPath;
		init();
	}

	/**
	 * 初始化，从配置文件中获取配置信息
	 */
	protected void init() {
		String filepath = getConfigPath();
		if (log.isInfoEnabled()) {
			log.info("sms config file:" + filepath);
		}
		Properties p = new Properties();
		try {
			p.load(EmppVisitService.class.getResourceAsStream(filepath));
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("sms config file error:" + e.getMessage(), e);
			}
		}
		host = p.getProperty("host");
		port = Integer.parseInt(p.getProperty("port"));
		accountId = p.getProperty("accountId");
		password = p.getProperty("password");
		// serviceId = p.getProperty("serviceId");暂时不用
		serviceId = "";
		name = p.getProperty("name");
		this.emppListener.setName(name);

		if (checkConfig()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]sms config:\n");
				sb.append("host:" + host + "\n");
				sb.append("port:" + port + "\n");
				sb.append("accountId:" + accountId + "\n");
				sb.append("password:" + password + "\n");
				sb.append("serviceId:" + serviceId + "\n");
				log.info(sb.toString());
			}
		} else {
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]sms config error:缺少配置信息\n");
				sb.append("host:" + host + "\n");
				sb.append("port:" + port + "\n");
				sb.append("accountId:" + accountId + "\n");
				sb.append("password:" + password + "\n");
				sb.append("serviceId:" + serviceId + "\n");
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
		if (host == null || host.trim().length() == 0 || port == 0
				|| accountId == null || accountId.trim().length() == 0
				|| password == null || password.trim().length() == 0) {
			return false;
		} else {
			return true;
		}

	}

	protected SMSResult checkConnect() {
		if (emppApi.isConnected()) {
			// 如果没有，则建立连接
			return SMSResult.createSuccessResult();
		} else {
			return connect();
		}

	}

	protected synchronized SMSResult connect() {
		if (emppApi.isConnected()) {

		} else {
			if (log.isInfoEnabled()) {
				log.info("[" + name + "]connect...");
			}
			// 建立
			try {

				// 建立同服务器的连接
				EMPPConnectResp response = emppApi.connect(host, port,
						accountId, password, emppListener);

				if (response == null) {
					String msg = "连接超时失败";
					if (log.isWarnEnabled()) {
						log.warn("[" + name + "]connect error:" + msg);
					}

					return SMSResult.createFailtrueResult(msg, new RuntimeException());
				}
				if (!emppApi.isConnected()) {
					int status = response.getStatus();
					String msg = "连接失败:响应包状态位=" + status;
					if (status == EMPPConnectResp.EMPP_AUTH_ERROR) {
						msg += "(连接建立状态:认证错)";
					} else if (status == EMPPConnectResp.EMPP_EXCEPRMAX_ERROR) {
						msg += "(连接建立状态:已经达到允许企业帐号的最大登录数)";
					} else if (status == EMPPConnectResp.EMPP_ROK) {
						msg += "(连接建立状态:连接成功建立)";
					} else if (status == EMPPConnectResp.EMPP_SUC_ANOTHER) {
						msg += "(连接建立状态:成功登录，但有一个用相同帐号在线登录的连接被服务器注销)";
					} else if (status == EMPPConnectResp.EMPP_VERSION_ERROR) {
						msg += "(连接建立状态:协议版本错)";
					}

					if (log.isWarnEnabled()) {
						log.warn("[" + name + "]connect error:" + msg);
					}
					// 重连3次，间隔5秒
					SMSResult result = reConnect(3, 5);
					result.setMessage(msg + result.getMessage());
					return result;
				}
			} catch (Exception e) {
				String msg = "连接失败:发生异常，导致连接失败:" + e.getMessage();
				if (log.isWarnEnabled()) {
					log.warn("[" + name + "]connect error:" + msg, e);
				}
				return SMSResult.createFailtrueResult(msg, e);
			}

		}
		return SMSResult.createSuccessResult();
	}

	/**
	 * 发送短信
	 * 
	 * @param phoneRece
	 * @param notes
	 * @return
	 */
	public SMSResult sms(String phoneRece, String notes) {
		if (!checkConfig()) {
			return SMSResult.createFailtrueResult("缺少配置信息", new RuntimeException());
		}
		SMSResult result = null;
		if ((result = checkConnect()).isNotSuccess()) {
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]send not success:\n");
				sb.append("code(0-success;1-failure):" + result.getCode()
						+ "\n");
				sb.append("msg:" + result.getMessage() + "\n");
				sb.append("phoneRece:" + phoneRece + "\n");
				sb.append("notes:" + notes + "\n");
				log.info(sb.toString());
			}
			// 测试不成功时，将结果返回
			return result;
		}

		// 发送
		EMPPSubmitSMResp resp;
		try {
			if (notes.length() > 70) {
				EMPPSubmitSMResp[] respArray = emppApi.submitMsg(notes,
						new String[] { phoneRece }, serviceId);
				resp = respArray[0];
			} else {
				EMPPSubmitSM msg = (EMPPSubmitSM) EMPPObject
						.createEMPP(EMPPData.EMPP_SUBMIT);
				List dstId = new ArrayList();
				dstId.add(phoneRece);
				msg.setDstTermId(dstId);
				msg.setSrcTermId(accountId);
				msg.setServiceId(serviceId);

				EMPPShortMsg msgContent = new EMPPShortMsg(
						EMPPShortMsg.EMPP_MSG_CONTENT_MAXLEN);
				msgContent.setMessage(notes.getBytes("GBK"));
				msg.setShortMessage(msgContent);
				msg.assignSequenceNumber();
				resp = emppApi.submitMsg(msg);
			}

			if (resp.getResult() == EMPPSubmitSMResp.EMPP_SEND_MSG_OK) {
				// 成功
				result = SMSResult.createSuccessResult();
				result.setMessage("发送成功");
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_BLACKCODE_STATUS) {
				result = SMSResult.createFailtrueResult("短信发送状态:接收方手机为黑名单用户", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_DESTID_ERROR) {
				result = SMSResult
						.createFailtrueResult("短信发送状态:Dest_terminal_Id错误", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_DOUBLECHECK_ERROR) {
				result = SMSResult.createFailtrueResult("", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_FREQ_LIMIT) {
				result = SMSResult.createFailtrueResult(" 短信发送状态:达到该账号的最大发信频率", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_MATCH_KEYWORD) {
				result = SMSResult.createFailtrueResult("短信发送状态:发送的短信含有敏感词", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_SEND_ERROR) {
				result = SMSResult
						.createFailtrueResult("短信发送状态:短信网关连接异常发送该短信出错", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_SERV_BUSY) {
				result = SMSResult.createFailtrueResult("短信发送状态:服务忙", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_SERVICEID_ERROR) {
				result = SMSResult.createFailtrueResult("短信发送状态:ServiceID错", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_SRCID_ERROR) {
				result = SMSResult.createFailtrueResult("短信发送状态:源号码SrcID错", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.EMPP_TESTEPR_MAXCOUNT) {
				result = SMSResult.createFailtrueResult("短信发送状态:达到该测试账号的最大发信量", new RuntimeException());
			} else if (resp.getResult() == EMPPSubmitSMResp.FORBID_MULTI_SEND) {
				result = SMSResult.createFailtrueResult("短信发送状态:该账号没有群发权限", new RuntimeException());
			} else {
				result = SMSResult.createFailtrueResult("", new RuntimeException());
			}
			if (log.isInfoEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]send result:\n");
				sb.append("code(0-success;1-failure):" + result.getCode()
						+ "\n");
				sb.append("msg:" + result.getMessage() + "\n");
				sb.append("phoneRece:" + phoneRece + "\n");
				sb.append("notes:" + notes + "\n");
				log.info(sb.toString());
			}
		} catch (Exception e) {
			String msg = "发送失败:发生异常，导致连接失败:" + e.getMessage();
			if (log.isErrorEnabled()) {
				StringBuffer sb = new StringBuffer();
				sb.append("[" + name + "]send error:\n");
				sb.append("code(0-success;1-failure):" + result.getCode()
						+ "\n");
				sb.append("msg:" + result.getMessage() + "\n");
				sb.append("phoneRece:" + phoneRece + "\n");
				sb.append("notes:" + notes + "\n");
				log.warn(sb.toString(), e);
			}
			result = SMSResult.createFailtrueResult(msg, e);
		}

		return result;
	}

	/**
	 * 重连
	 * 
	 * @param reConnTime
	 *            ,重连次数,当reConnTime<=0时,无限次重连
	 * @param reConnWait
	 *            ,重连等待秒数,当reConnWait<=0时,使用默认等待时间
	 */
	public SMSResult reConnect(int reConnTime, int reConnWait) {
		// reConnTime重连
		SMSResult result = SMSResult.createSuccessResult();
		long wait = EmppListener.RECONNECT_WAIT;
		if (reConnWait > 0) {
			wait = reConnWait * 1000L;
		}

		for (int i = 1; !emppApi.isConnected(); i++) {
			if (reConnTime > 0 && reConnTime > i) {
				String msg = "重连" + reConnTime + "次失败";
				result = SMSResult.createFailtrueResult(msg);
				break;
			}
			try {
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]重连次数:" + i);
				}
				Thread.sleep(wait);
				emppApi.reConnect(this.emppListener);
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error(
							"[" + name + "]reconnect error:" + e.getMessage(),
							e);
				}

			}
		}
		return result;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	String getConfigPath() {
		return configPath;
	}

	void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

}
