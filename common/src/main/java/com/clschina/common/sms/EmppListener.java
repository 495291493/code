/**
 * 
 */
package com.clschina.common.sms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wondertek.esmp.esms.empp.EMPPDeliver;
import com.wondertek.esmp.esms.empp.EMPPDeliverReport;
import com.wondertek.esmp.esms.empp.EMPPObject;
import com.wondertek.esmp.esms.empp.EMPPRecvListener;
import com.wondertek.esmp.esms.empp.EMPPReqNoticeResp;
import com.wondertek.esmp.esms.empp.EMPPSubmitSM;
import com.wondertek.esmp.esms.empp.EMPPSubmitSMResp;
import com.wondertek.esmp.esms.empp.EMPPSyncAddrBookResp;
import com.wondertek.esmp.esms.empp.EMPPTerminate;
import com.wondertek.esmp.esms.empp.EMPPUnAuthorization;
import com.wondertek.esmp.esms.empp.EmppApi;

/**
 * Empp连接的监听器
 * 
 * @author Wu Xiao Fei
 * 
 */
public class EmppListener implements EMPPRecvListener {
	private static Log log = LogFactory.getLog(EmppVisitService.class);

	public static final long RECONNECT_WAIT = 5 * 1000;

	private EmppApi emppApi = null;
	private int closedCount = 0;
	private String name;

	public EmppListener(EmppApi emppApi) {
		super();
		this.emppApi = emppApi;
	}

	public void onMessage(EMPPObject message) {
		if (message instanceof EMPPUnAuthorization) {
			EMPPUnAuthorization unAuth = (EMPPUnAuthorization) message;
			if (log.isInfoEnabled()) {
				log.info("[" + name + "]empp:" + "客户端无权执行此操作 commandId="
						+ unAuth.getUnAuthCommandId());
			}
			return;
		}
		if (message instanceof EMPPSubmitSMResp) {
			EMPPSubmitSMResp resp = (EMPPSubmitSMResp) message;

			if (log.isInfoEnabled()) {
				log.info("[" + name + "]empp:" + "收到sumbitResp:" + "msgId="
						+ resp.getMsgId() + "result=" + resp.getResult());
			}
			return;
		}
		if (message instanceof EMPPDeliver) {
			EMPPDeliver deliver = (EMPPDeliver) message;
			if (deliver.getRegister() == EMPPSubmitSM.EMPP_STATUSREPORT_TRUE) {
				// 收到状态报告
				EMPPDeliverReport report = deliver.getDeliverReport();
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]empp:" + "收到状态报告:" + "msgId="
							+ report.getMsgId() + "result=" + report.getStat());
				}

			} else {
				// 收到手机回复
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]empp:" + "收到"
							+ deliver.getSrcTermId() + "发送的短信。" + "短信内容为："
							+ deliver.getMsgContent());
				}
			}
			return;
		}
		if (message instanceof EMPPSyncAddrBookResp) {
			EMPPSyncAddrBookResp resp = (EMPPSyncAddrBookResp) message;
			if (resp.getResult() != EMPPSyncAddrBookResp.RESULT_OK) {
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]empp:" + "同步通讯录失败");
				}
			} else {
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]empp:" + "收到服务器发送的通讯录信息。"
							+ "通讯录类型为：" + resp.getAddrBookType() + "."
							+ resp.getAddrBook());
				}
			}
		}
		if (message instanceof EMPPReqNoticeResp) {
			EMPPReqNoticeResp response = (EMPPReqNoticeResp) message;
			if (response.getResult() != EMPPReqNoticeResp.RESULT_OK) {
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]empp:" + "查询运营商发布信息失败");
				}

			} else {
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]empp:" + "收到运营商发布的信息："
							+ response.getNotice());
				}
			}
			return;
		}
		if (log.isInfoEnabled()) {
			log.info("[" + name + "]empp:" + message);
		}

	}

	public void OnError(Exception e) {
		if (log.isInfoEnabled()) {
			log.error("[" + name + "]empp error:" + e.getMessage(), e);
		}
	}

	public void OnClosed(Object object) {

		if (object instanceof EMPPTerminate) {
			if (log.isInfoEnabled()) {
				log.info("[" + name + "]empp close:"
						+ "收到服务器发送的Terminate消息，连接终止");
			}
			return;
		}

		if (log.isInfoEnabled()) {
			log.info("[" + name + "]empp close:" + object);
		}

		// 这里注意要将emppApi做为参数传入构造函数
		if (log.isInfoEnabled()) {
			log.info("连接断掉次数：" + (++closedCount));
		}
		for (int i = 1; !emppApi.isConnected(); i++) {
			try {
				if (log.isInfoEnabled()) {
					log.info("[" + name + "]重连次数:" + i);
				}
				Thread.sleep(RECONNECT_WAIT);
				emppApi.reConnect(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (log.isInfoEnabled()) {
			log.info("[" + name + "]重连成功");
		}
		// 连接断掉次数清零
		closedCount = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
