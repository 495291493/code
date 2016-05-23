/**
 * 
 */
package com.clschina.common.sms.cmpp;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.clschina.common.sms.cmpp.cmpp20.CMPPActiveMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPActiveRespMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPConnectMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPConnectRespMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPDeliverMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPDeliverRespMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPSubmitMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPSubmitRespMessage;
import com.clschina.common.sms.cmpp.cmpp20.CMPPTerminateMessage;
import com.clschina.common.sms.cmpp.mina.CmppProtocolCodecFactory;

/**
 * @author Wu Xiao Fei
 * 
 */
public class CmppApi implements IoHandler {
	private static Log log = LogFactory.getLog(CmppApi.class);
	private static int SEQ = 0;

	// 等待时间
	private static final long CMPP_CONNECT_WAIT_TIME = 15 * 1000;// 连接等待15秒
	private static final long CMPP_SUBMIT_WAIT_TIME = 15 * 1000;// 提交短信反馈等待15秒
	private static final long CMPP_ACTIVE_TEST_SLEEP_TIME = 2 * 60 * 1000;// 2分钟
	// 链路测试等待时间
	private static final long CMPP_ACTIVE_TEST_WAIT_TIME = 25 * 1000;// 25秒
	// 链路测试超时时间
	private static final long CMPP_TERMINATE_WAIT_TIME = 5 * 1000;// 连接等待5秒

	private static final long SEND_THREAD_SLEEP_TIME = 5 * 1000;// 线程等待时间5秒
	// 短信信息
	
	private static final int SMS_NORMAL_LENGTH = 70;// 短信正常70字符
	private static final int SMS_EACH_SPLIT_LENGTH = 67;// 长短信拆分后，每条短信为67字符

	// 基本的参数
	private int smsSignLength = 6;// 签名6字符
	private String host;// 远程地址
	private int port;// 远程端口
	private String corpId;// 企业id（用户名）
	private String passwd;// 密码
	private String srcPhoneNo;// 源设备id，即发送短信的电话

	// 内部参数
	private boolean cmppConnected;// cmpp是否已经连接
	private boolean inited = false;// 参数已经初始化。
	private boolean appearedTimeout = false;// 出现超时。将通知马上进行链路检测

	// mina
	private boolean connectorInited = false;
	private IoConnector ioConnector = null;
	private IoSession ioSession = null;

	// 锁
	private Map<String, Object> lockMap = new ConcurrentHashMap<String, Object>();

	// 返回值
	private Map<String, CMPPMessage> respMap = new ConcurrentHashMap<String, CMPPMessage>();

	// 链路检测
	private Thread activeThread;
	private boolean activeTestThreadStopped;

	// Deliver
	private CmppDeliver deliverInstance = null;

	// 错误处理
	private long connectErrorFirstTime = 0;
	private long activeErrorFirstTime = 0;

	private void initIoConnector() {
		if (!connectorInited) {

			ioConnector = new NioSocketConnector();
			ioConnector.setConnectTimeoutMillis(30 * 1000);
			ioConnector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CmppProtocolCodecFactory()));
			ioConnector.setHandler(this);

			if (log.isInfoEnabled()) {
				log.info("ioConnector inited!");
			}

		}

	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param corpId
	 * @param passwd
	 * @param srcPhoneNo
	 * @throws Exception
	 */
	public void init(String host, int port, String corpId, String passwd, String srcPhoneNo, int signLength,
			CmppDeliver deliver) {
		if (log.isInfoEnabled()) {
			log.info("init connect(?,?,?,?)...");
		}
		if (!inited) {
			this.host = host;
			this.port = port;
			this.corpId = corpId;
			this.passwd = passwd;
			this.srcPhoneNo = srcPhoneNo;
			this.smsSignLength = signLength;
			this.deliverInstance = deliver;

			inited = true;
		}
	}

	public CmppResult submitMsg(String destPhoneNo, String msgContent) throws Exception {
		return submitMsg(destPhoneNo, msgContent, this.smsSignLength);
	}

	public CmppResult submitMsg(String destPhoneNo, String msgContent, int signLength) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run submitMsg('" + destPhoneNo + "','" + msgContent + "'," + signLength + ")...");
		}

		CmppResult result = null;
		if (destPhoneNo == null || destPhoneNo.trim().length() == 0 || msgContent == null
				|| msgContent.trim().length() == 0) {
			result = CmppResult.createFailtrueResult("cmpp submit error:参数不能为空 destPhoneNo[" + destPhoneNo
					+ "] msgContent[" + msgContent + "]");
		} else if (isTCPConnected() && isCmppConnected()) {
			// 发送连接请求
			if (log.isInfoEnabled()) {
				log.info("cmpp submit start...");
			}
			// 如果是长短信的话，将进行拆分，最后一条短信将上锁
			CMPPMessage respMessage = null;
			if (msgContent.length() > (SMS_NORMAL_LENGTH - signLength)) {
				if (log.isInfoEnabled()) {
					log.info("长短信拆分[" + msgContent + "]...");
				}

				List<String> splitMsg = splitMsgContent(msgContent, signLength);
				int total = splitMsg.size();
				int i = 1;
				int seq = ++SEQ;
				for (String tmp_content : splitMsg) {
					CMPPSubmitMessage submit = new CMPPSubmitMessage(this.srcPhoneNo, destPhoneNo, tmp_content,
							this.corpId, total, i, seq);
					if (log.isInfoEnabled()) {
						log.info("长短信结果[" + i + "/" + total + "][sequence=" + submit.getSequence_id() + "]content="
								+ tmp_content + "");
					}

					if (i == total) {
						// 最后一条，上锁
						respMessage = sendCMPPMessageWithLock(submit, CMPP_SUBMIT_WAIT_TIME);
					} else {
						// 不上锁提交
						this.ioSession.write(submit);

					}
					i++;
				}

			} else {
				CMPPSubmitMessage submit = new CMPPSubmitMessage(this.srcPhoneNo, destPhoneNo, msgContent, this.corpId);
				if (log.isInfoEnabled()) {
					log.info("短短信[" + msgContent + "][sequence=" + submit.getSequence_id() + "]");
				}
				respMessage = sendCMPPMessageWithLock(submit, CMPP_SUBMIT_WAIT_TIME);

			}
			if (respMessage == null) {
				// 失败，超时
				result = CmppResult.createFailtrueResult("cmpp submit error:超时");
				setAppearedTimeout(true);// 意味着子线程检查

			} else if (respMessage.getCommand_id() != CMPPMessage.ID_CMPP_SUBMIT_RESP) {
				result = CmppResult.createFailtrueResult("cmpp submit error:返回commandId 不匹配");
			} else {
				CMPPSubmitRespMessage submitRespMessage = (CMPPSubmitRespMessage) respMessage;
				switch (submitRespMessage.getResult()) {
				case 0:
					result = CmppResult.createSuccessResult();
					break;
				case 1:
					result = CmppResult.createFailtrueResult("cmpp submit error:消息结构错");
					break;
				case 2:
					result = CmppResult.createFailtrueResult("cmpp submit error:命令字错");
					break;
				case 3:
					result = CmppResult.createFailtrueResult("cmpp submit error:消息序号重复");
					break;
				case 4:
					result = CmppResult.createFailtrueResult("cmpp submit error:消息长度错");
					break;
				case 5:
					result = CmppResult.createFailtrueResult("cmpp submit error:资费代码错");
					break;
				case 6:
					result = CmppResult.createFailtrueResult("cmpp submit error:超过最大信息长");
					break;
				case 7:
					result = CmppResult.createFailtrueResult("cmpp submit error:业务代码错");
					break;
				case 8:
					result = CmppResult.createFailtrueResult("cmpp submit error:流量控制错");
					break;

				default:
					result = CmppResult.createFailtrueResult("cmpp submit error:其他错误");
					break;
				}
			}
		} else {
			result = CmppResult.createFailtrueResult("cmpp submit error:tcp 已断开");

			// 140307：未连接，且测试线程也未启动状态，则在这里重启
			if (activeTestThreadStopped) {
				if (log.isInfoEnabled()) {
					log.info("in submi msg,it finds tcp did not connected ,and activeTest did stopped,so reconnect...",
							new RuntimeException());
				}
				reconnect(true);// 重连，并重建测试线程
			}

		}

		if (result.isNotSuccess()) {
			if (result.getMessage().contains("超时")) {
				// 超时记录为警告，其他错误记录为error
				if (log.isWarnEnabled()) {
					log.warn("cmpp submit error [" + destPhoneNo + "],[" + msgContent + "] :" + result.getMessage(),
							new RuntimeException());
				}
			} else {
				if (log.isErrorEnabled()) {
					log.error(
							"cmpp submit error [" + destPhoneNo + "],[" + msgContent + "] :message="
									+ result.getMessage() + ";code=" + result.getCode(), new RuntimeException());
				}
			}
		} else {
			// 成功，
			if (log.isInfoEnabled()) {
				log.info("发送成功[" + destPhoneNo + "]" + msgContent);
			}
		}
		return result;
	}

	public static List<String> splitMsgContent(String msgContent, int signLength) {
		List<String> msgList = new ArrayList<String>();
		int total = msgContent.length() / SMS_EACH_SPLIT_LENGTH;
		if (msgContent.length() % SMS_EACH_SPLIT_LENGTH > 0) {// 余数
			total++;
		}
		for (int i = 1; i <= total; i++) {
			String splitMsg = msgContent.substring((i - 1) * SMS_EACH_SPLIT_LENGTH,
					i * SMS_EACH_SPLIT_LENGTH > msgContent.length() ? msgContent.length() : i * SMS_EACH_SPLIT_LENGTH);
			msgList.add(splitMsg);
		}
		// 最后一条按SMS_EACH_SPLIT_LENGTH-signLength拆分
		String last = msgList.get(msgList.size() - 1);
		if (last.length() > (SMS_EACH_SPLIT_LENGTH - signLength)) {
			String newLast = last.substring(0, (SMS_EACH_SPLIT_LENGTH - signLength));
			String newLast2 = last.substring(SMS_EACH_SPLIT_LENGTH - signLength);
			msgList.remove(msgList.size() - 1);
			msgList.add(newLast);
			msgList.add(newLast2);
		}
		return msgList;

	}

	public CmppResult activeTest() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run activeTest()...");
		}
		CmppResult result = null;
		if (isTCPConnected() && isCmppConnected()) {

			CMPPMessage respMessage = sendCMPPMessageWithLock(new CMPPActiveMessage(), CMPP_ACTIVE_TEST_WAIT_TIME);
			if (respMessage == null) {
				// 失败，超时
				result = CmppResult.createFailtrueResult("cmpp active test error:超时");
			} else {
				result = CmppResult.createSuccessResult();
			}
		} else {
			result = CmppResult.createFailtrueResult("cmpp active test error:tcp 已断开");
		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("cmpp active test success. ");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {

			if (activeErrorFirstTime == 0 || (System.currentTimeMillis() - activeErrorFirstTime) > 15 * 60 * 1000) {
				log.error("cmpp active test error:" + result.getMessage(), new RuntimeException());
				activeErrorFirstTime = System.currentTimeMillis();
			} else {
				log.info("cmpp active test error:" + result.getMessage(), new RuntimeException());
			}

		}

		return result;
	}

	/**
	 * 断开连接
	 * 
	 * @return
	 * @throws Exception
	 */
	public void terminateTcp() throws Exception {
		this.terminate(true);
	}

	public CmppResult connectTcp() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run connect()...");
		}
		return connect(true);
	}

	public CmppResult reconnectTcp() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run reconnect()...");
		}
		terminate(true);
		return connect(true);
	}

	/**
	 * 线程中重试
	 * 
	 * @return
	 * @throws Exception
	 */
	private CmppResult reconnect(boolean stopThread) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run reconnect(stopThread=" + stopThread + ")...");
		}
		terminate(stopThread);
		return connect(stopThread);
	}

	/**
	 * 断开连接
	 * 
	 * @return
	 * @throws Exception
	 */
	private void terminate(boolean stopThread) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run terminate(stopThread=" + stopThread + ")...");
		}
		if (isTCPConnected() && isCmppConnected()) {
			if (log.isInfoEnabled()) {
				log.info("cmpp terminate start...");
			}
			setCmppConnected(false);
			CMPPMessage respMessage = sendCMPPMessageWithLock(new CMPPTerminateMessage(), CMPP_TERMINATE_WAIT_TIME);
			// 记录是否超时
			if (respMessage == null) {
				if (log.isInfoEnabled()) {
					log.info("cmpp terminate error:超时");
				}
			}

		}
		// 暂停激活线程
		if (stopThread) {
			stopSendActiveThread();
		}
		// 关闭连接
		if (isTCPConnected()) {
			if (log.isInfoEnabled()) {
				log.info("tcp close start...");
			}
			CloseFuture cf = this.ioSession.close(true);// 立即结束
			cf.awaitUninterruptibly();
			this.ioConnector.dispose();
			if (log.isInfoEnabled()) {
				log.info("tcp close success!");
			}

		}
		// 清空所有的相关map中的缓存
		respMap.clear();
		lockMap.clear();// 不用唤醒，全部超时
	}

	private synchronized CmppResult connect(boolean stopThread) throws Exception {// 增加同步关键字
		if (log.isInfoEnabled()) {
			log.info("run connect(stopThread=" + stopThread + ")...");
		}
		if (!isTCPConnected()) {
			// 连接
			setCmppConnected(false);
			if (stopThread) {
				stopSendActiveThread();
			}

			initIoConnector();
			try {
				ConnectFuture connectFuture = this.ioConnector.connect(new InetSocketAddress(this.host, this.port));
				connectFuture.awaitUninterruptibly();
				this.ioSession = connectFuture.getSession();
			} catch (RuntimeException e) {
				if (log.isErrorEnabled()) {
					log.error("tcp connect fail to host[" + this.host + "] port[" + this.port + "]:" + e.getMessage(),
							e);
				}
				// 如果这时，检测线程未启动，应及时启动
				if (this.activeTestThreadStopped) {
					if (log.isInfoEnabled()) {
						log.info("启动startSendActiveThread，进行重连");
					}
					startSendActiveThread();
				}

				return CmppResult.createFailtrueResult("tcp connect fail:连不上tcp，可能是网络问题。" + e.getMessage());

			}
			if (log.isInfoEnabled()) {
				log.info("tcp connect success to host[" + this.host + "] port[" + this.port + "]");
			}

		}
		// 发送连接请求
		if (log.isInfoEnabled()) {
			log.info("cmpp connect start...");
		}
		CMPPConnectMessage connectMessage = new CMPPConnectMessage(this.corpId, this.passwd, 1);

		// 等待返回
		String key = Integer.toString(connectMessage.getSequence_id());
		Object lock = new Object();
		lockMap.put(key, lock);
		synchronized (lock) {
			this.ioSession.write(connectMessage);
			lock.wait(CMPP_CONNECT_WAIT_TIME);
		}
		lockMap.remove(key);
		CMPPMessage respMessage = respMap.remove(key);
		CmppResult result = null;
		if (respMessage == null) {
			// 失败，超时
			result = CmppResult.createFailtrueResult("cmpp connect error:超时");

		} else if (respMessage.getCommand_id() != CMPPMessage.ID_CMPP_CONNECT_RESP) {
			result = CmppResult.createFailtrueResult("cmpp connect error:返回commandId 不匹配");
		} else {
			CMPPConnectRespMessage connectRespMessage = (CMPPConnectRespMessage) respMessage;
			switch (connectRespMessage.getConnect_status()) {
			case 0:
				result = CmppResult.createSuccessResult();
				break;
			case 1:
				result = CmppResult.createFailtrueResult("cmpp connect error:消息结构错");
				break;
			case 2:
				result = CmppResult.createFailtrueResult("cmpp connect error:非法源地址");
				break;
			case 3:
				result = CmppResult.createFailtrueResult("cmpp connect error:认证错");
				break;
			case 4:
				result = CmppResult.createFailtrueResult("cmpp connect error:版本太高");
				break;

			default:
				result = CmppResult.createFailtrueResult("cmpp connect error:其他错误");
				break;
			}
		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("cmpp connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] success");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {

			if (connectErrorFirstTime == 0 || (System.currentTimeMillis() - connectErrorFirstTime) > 15 * 60 * 1000) {
				log.error(
						"cmpp connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] error:"
								+ result.getMessage(), new RuntimeException());
				connectErrorFirstTime = System.currentTimeMillis();
			} else {
				log.info("cmpp connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] error:"
						+ result.getMessage());
			}

		}

		if (result.isSuccess()) {
			setCmppConnected(true);
			if (stopThread) {
				startSendActiveThread();
			}
		}

		return result;
	}

	/**
	 * 同步发送CMPP Message
	 * 
	 * @param reqMessage
	 * @param waitTime
	 * @return
	 * @throws Exception
	 */
	private CMPPMessage sendCMPPMessageWithLock(CMPPMessage reqMessage, long waitTime) throws Exception {
		// 等待返回
		String key = Integer.toString(reqMessage.getSequence_id());
		Object lock = new Object();
		lockMap.put(key, lock);
		synchronized (lock) {
			this.ioSession.write(reqMessage);
			lock.wait(waitTime);
		}
		lockMap.remove(key);
		CMPPMessage respMessage = respMap.remove(key);
		return respMessage;
	}

	/**
	 * 直接向对方发送一个CMPP Message，本方法目前只用于发送active的返回值
	 * 
	 * @param reqMessage
	 * @throws Exception
	 */
	private void sendCMPPMessageWithNoLock(CMPPMessage reqMessage) throws Exception {
		this.ioSession.write(reqMessage);
	}

	public boolean isTCPConnected() {
		if (this.ioSession == null) {
			return false;
		}
		return this.ioSession.isConnected();
	}

	public boolean isTCPClosed() {
		if (this.ioSession == null) {
			return true;
		}
		return this.ioSession.isClosing();// 正在关闭，可能还没有正式关闭
	}

	public boolean isCmppConnected() {
		return cmppConnected;
	}

	void setCmppConnected(boolean cmppConnected) {
		this.cmppConnected = cmppConnected;
	}

	public boolean isAppearedTimeout() {
		return appearedTimeout;
	}

	void setAppearedTimeout(boolean appearedTimeout) {
		this.appearedTimeout = appearedTimeout;
	}

	// handle监听=====================================================
	@Override
	public void exceptionCaught(IoSession session, Throwable e) throws Exception {
		if (log.isErrorEnabled()) {
			log.error("tcp exception:" + e.getMessage(), e);
		}

	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		CMPPMessage cmppMessage = (CMPPMessage) message;
		if (log.isDebugEnabled()) {
			log.debug("get message from tcp command_id[" + cmppMessage.getCommand_id() + "]");
		}
		if (cmppMessage.getCommand_id() == CMPPMessage.ID_CMPP_CONNECT_RESP
				|| cmppMessage.getCommand_id() == CMPPMessage.ID_CMPP_SUBMIT_RESP
				|| cmppMessage.getCommand_id() == CMPPMessage.ID_CMPP_ACTIVE_RESP
				|| cmppMessage.getCommand_id() == CMPPMessage.ID_CMPP_TERMINATE_RESP) {
			String key = Integer.toString(cmppMessage.getSequence_id());
			Object lock = lockMap.remove(key);
			if (lock != null) {
				if (log.isDebugEnabled()) {
					log.debug("Sequence_id[" + key + "] has lock");
				}
				respMap.put(key, cmppMessage);
				synchronized (lock) {
					lock.notifyAll();
				}
			} else {
				// 如果没有lock，表示已经没有线程等待了，所以消息丢弃
				if (log.isDebugEnabled()) {
					log.debug("Sequence_id[" + key + "] has not lock");
				}
			}

		} else if (cmppMessage.getCommand_id() == CMPPMessage.ID_CMPP_ACTIVE) {
			// 如果是对方发送的active，则我这里要返回一个响应
			CMPPActiveMessage activeMsg = (CMPPActiveMessage) cmppMessage;
			CMPPActiveRespMessage activeRespMsg = new CMPPActiveRespMessage(activeMsg);
			sendCMPPMessageWithNoLock(activeRespMsg);
			if (log.isDebugEnabled()) {
				log.debug("send active response message to remote:Sequence_id[" + activeRespMsg.getSequence_id() + "]");
			}
		} else if (cmppMessage.getCommand_id() == CMPPMessage.ID_CMPP_DELIVER) {
			// 得到一个deliver msg
			CMPPDeliverMessage deliverMsg = (CMPPDeliverMessage) cmppMessage;
			CMPPDeliverRespMessage deliverRespMsg = new CMPPDeliverRespMessage(deliverMsg);
			sendCMPPMessageWithNoLock(deliverRespMsg);
			if (log.isDebugEnabled()) {
				log.debug("send deliver response message to remote:Sequence_id[" + deliverRespMsg.getSequence_id()
						+ "]");
			}

			if (this.deliverInstance != null) {
				if (log.isDebugEnabled()) {
					log.debug("get message to[" + deliverMsg.getDestTermid() + "],from[" + deliverMsg.getSrcTermid()
							+ "],msg[" + deliverMsg.getMsgContent() + "]");
				}
				try {
					this.deliverInstance.deliver(deliverMsg.getDestTermid(), deliverMsg.getSrcTermid(),
							deliverMsg.getMsgContent());
				} catch (Exception e) {
					log.error(e);
				}
			}

		}

	}

	@Override
	public void messageSent(IoSession arg0, Object arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionClosed(IoSession arg0) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("mina session closed");
		}

		// XXX
		if (log.isInfoEnabled()) {
			log.info("in the sessionClosed,connect again...");
		}
		connect(true);

	}

	@Override
	public void sessionCreated(IoSession arg0) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("mina session created");
		}

	}

	@Override
	public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("mina session idle 进入空闲状态");
		}

	}

	@Override
	public void sessionOpened(IoSession arg0) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("mina session opened 连接打开");
		}

	}

	// 连接检测=====================================================
	/**
	 * 暂停连接检测线程
	 */
	private void stopSendActiveThread() {
		if (log.isInfoEnabled()) {
			log.info("stopSendActiveThread");
		}
		if (activeThread != null) {
			if (log.isInfoEnabled()) {
				log.info("activeThread is not null.Interrupt...");
			}
			activeThread.interrupt();
		}
		activeThread = null;
		activeTestThreadStopped = true;
	}

	private void startSendActiveThread() {
		stopSendActiveThread();
		if (log.isInfoEnabled()) {
			log.info("startSendActiveThread");
		}
		this.activeThread = new SendActiveThread();
		this.activeThread.start();
		activeTestThreadStopped = false;
	}

	private class SendActiveThread extends Thread {

		private long waitTimeSinceLastActiveTest = 0;// 这个是据上一次测试的后已经等待的时间
		private int reconnectCount = 0;// 重连次数

		@Override
		public void run() {
			if (log.isInfoEnabled()) {
				log.info("SendActiveThread run...");
			}

			while (!interrupted() && !activeTestThreadStopped) {
				// 开始测试

				try {

					Thread.sleep(SEND_THREAD_SLEEP_TIME);

					if (!isTCPConnected()) {
						// 如果没有连上，重连
						if (log.isInfoEnabled()) {
							log.info("TCP connect closed.reconnect...");
						}
						if (reconnectCount > 2 && reconnectCount <= 5) {
							if (log.isInfoEnabled()) {
								log.info("睡眠1分钟后，开始第" + reconnectCount + "次cmpp tcp重连");
							}
							Thread.sleep(60 * 1000);

						} else if (reconnectCount > 5 && reconnectCount <= 8) {
							if (log.isErrorEnabled()) {
								log.error("睡眠10分钟后，开始第" + reconnectCount + "次cmpp tcp重连");
							}
							Thread.sleep(10 * 60 * 1000);
						} else if (reconnectCount > 8) {
							if (log.isErrorEnabled()) {
								log.error("睡眠30分钟后，开始第" + reconnectCount + "次cmpp tcp重连");
							}
							Thread.sleep(30 * 60 * 1000);
						}

						reconnect(false);

						reconnectCount++;
						waitTimeSinceLastActiveTest = 0;
						continue;
					} else {
						reconnectCount = 0;
					}

					if (waitTimeSinceLastActiveTest < CMPP_ACTIVE_TEST_SLEEP_TIME && !isAppearedTimeout()) {
						// 不进行测试，
						waitTimeSinceLastActiveTest += SEND_THREAD_SLEEP_TIME;
						continue;
					}

					// 发送
					CmppResult result = activeTest();
					if (result.isSuccess()) {
						if (log.isInfoEnabled()) {
							log.info("active test success lockMap[" + lockMap.size() + "] respMap[" + respMap.size()
									+ "]");
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info("active test error:" + result.getMessage());
						}
						reconnect(false);
					}
					waitTimeSinceLastActiveTest = 0;
					setAppearedTimeout(false);// 表示超时已经处理
					continue;
				} catch (InterruptedException e1) {
					if (log.isErrorEnabled()) {
						log.error("active test catch InterruptedException:" + e1.getMessage(), e1);
					}
					activeTestThreadStopped = true;
					// 如果是线程错误，则暂定本线程
					interrupt();
					break;

				} catch (Exception e2) {
					// 关闭连接
					if (log.isErrorEnabled()) {
						log.error("active test throw exception:" + e2.getMessage(), e2);
					}
				}
			}
			if (log.isInfoEnabled()) {
				log.info("SendActiveThread stop...");
			}
		}

		private SendActiveThread() {
		}
	}

}
