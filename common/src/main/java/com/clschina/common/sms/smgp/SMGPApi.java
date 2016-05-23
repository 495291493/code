/**
 * 
 */
package com.clschina.common.sms.smgp;

import java.net.InetSocketAddress;
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

import com.clschina.common.sms.smgp.mima.SMGPProtocolCodecFactory;
import com.clschina.common.sms.smgp.smgp30.SMGPActivceTestMessage;
import com.clschina.common.sms.smgp.smgp30.SMGPActivceTestRespMessage;
import com.clschina.common.sms.smgp.smgp30.SMGPExitMessage;
import com.clschina.common.sms.smgp.smgp30.SMGPLoginMessage;
import com.clschina.common.sms.smgp.smgp30.SMGPLoginRespMessage;
import com.clschina.common.sms.smgp.smgp30.SMGPMessage;
import com.clschina.common.sms.smgp.smgp30.SMGPSubmitMessageLongTlv;
import com.clschina.common.sms.smgp.smgp30.SMGPSubmitRespMessage;

/**
 * @author Wu Xiao Fei
 * 
 */
public class SMGPApi implements IoHandler {
	private static Log log = LogFactory.getLog(SMGPApi.class);

	// 等待时间
	private static final long SMGP_CONNECT_WAIT_TIME = 15 * 1000;// 连接等待15秒
	private static final long SMGP_SUBMIT_WAIT_TIME = 15 * 1000;// 提交短信反馈等待15秒
	private static final long SMGP_ACTIVE_TEST_SLEEP_TIME = 2 * 60 * 1000;// 2分钟
																			// 链路测试等待时间
	private static final long SMGP_ACTIVE_TEST_WAIT_TIME = 25 * 1000;// 25秒
																		// 链路测试超时时间
	private static final long SMGP_TERMINATE_WAIT_TIME = 5 * 1000;// 连接等待5秒

	private static final long SEND_THREAD_SLEEP_TIME = 5 * 1000;// 线程等待时间5秒
	// 短信信息
	private static int SMS_SIGN_LENGTH = 6;// 签名6字符
	private static final int SMS_NORMAL_LENGTH = 70;// 短信正常70字符
	private static final int SMS_EACH_SPLIT_LENGTH = 67;// 长短信拆分后，每条短信为67字符

	// 基本的参数
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
	private Map<String, SMGPMessage> respMap = new ConcurrentHashMap<String, SMGPMessage>();

	// 链路检测
	private Thread activeThread;
	private boolean activeTestThreadStopped;

	private void initIoConnector() {
		if (!connectorInited) {

			ioConnector = new NioSocketConnector();
			ioConnector.setConnectTimeoutMillis(30 * 1000);
			ioConnector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new SMGPProtocolCodecFactory()));
			ioConnector.setHandler(this);

			if (log.isInfoEnabled()) {
				log.info("ioConnector inited!");
			}

		}

	}

	public void init(String host, int port, String corpId, String passwd, String srcPhoneNo, int signLength) {
		if (log.isInfoEnabled()) {
			log.info("init connect(?,?,?,?)...");
		}
		if (!inited) {
			this.host = host;
			this.port = port;
			this.corpId = corpId;
			this.passwd = passwd;
			this.srcPhoneNo = srcPhoneNo;
			SMS_SIGN_LENGTH = signLength;
			inited = true;
		}
	}

	public SMGPResult submitMsg(String destPhoneNo, String msgContent) throws Exception {
		return submitMsg(destPhoneNo, msgContent, SMS_SIGN_LENGTH);
	}

	public SMGPResult submitMsg(String destPhoneNo, String msgContent, int signLength) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run submitMsg('" + destPhoneNo + "','" + msgContent + "')...");
		}

		SMGPResult result = null;
		if (destPhoneNo == null || destPhoneNo.trim().length() == 0 || msgContent == null
				|| msgContent.trim().length() == 0) {
			result = SMGPResult.createFailtrueResult("cmpp submit error:参数不能为空 destPhoneNo[" + destPhoneNo
					+ "] msgContent[" + msgContent + "]");
		} else if (isTCPConnected() && isCmppConnected()) {
			// 发送连接请求
			if (log.isInfoEnabled()) {
				log.info("cmpp submit start...");
			}
			// 如果是长短信的话，将进行拆分，最后一条短信将上锁
			SMGPMessage respMessage = null;
			if (msgContent.length() > (SMS_NORMAL_LENGTH - SMS_SIGN_LENGTH)) {
				if (log.isInfoEnabled()) {
					log.info("长短信拆分[" + msgContent + "]...");
				}

				int total = (msgContent.length() - 1) / (SMS_EACH_SPLIT_LENGTH - SMS_SIGN_LENGTH) + 1;
				int i = 1;
				for (; i <= total; i++) {
					String tmp_content = msgContent.substring((i - 1) * (SMS_EACH_SPLIT_LENGTH - SMS_SIGN_LENGTH), i
							* (SMS_EACH_SPLIT_LENGTH - SMS_SIGN_LENGTH) > msgContent.length() ? msgContent.length() : i
							* (SMS_EACH_SPLIT_LENGTH - SMS_SIGN_LENGTH));

					if (log.isInfoEnabled()) {
						log.info("长短信结果[" + i + "/" + total + "]:" + tmp_content + "");
					}

					SMGPSubmitMessageLongTlv submit = new SMGPSubmitMessageLongTlv(this.srcPhoneNo, destPhoneNo,
							tmp_content, "", "1", total, i);

					if (i == total) {
						// 最后一条，上锁
						respMessage = sendSMGPMessageWithLock(submit, SMGP_SUBMIT_WAIT_TIME);
					} else {
						// 不上锁提交
						this.ioSession.write(submit);

					}
				}

			} else {

				respMessage = sendSMGPMessageWithLock(new SMGPSubmitMessageLongTlv(this.srcPhoneNo, destPhoneNo,
						msgContent, "", ""), SMGP_SUBMIT_WAIT_TIME);

			}
			if (respMessage == null) {
				// 失败，超时
				result = SMGPResult.createFailtrueResult("cmpp submit error:超时");
				setAppearedTimeout(true);// 意味着子线程检查

			} else if (respMessage.getRequestID() != SMGPMessage.ID_SMGP_SUBMIT_RESP) {
				result = SMGPResult.createFailtrueResult("cmpp submit error:返回commandId 不匹配");
			} else {
				SMGPSubmitRespMessage submitRespMessage = (SMGPSubmitRespMessage) respMessage;
				switch (submitRespMessage.getStatus()) {
				case 0:
					result = SMGPResult.createSuccessResult();
					break;
				default:
					result = SMGPResult.createFailtrueResult("cmpp submit error:代码：" + submitRespMessage.getStatus());
					break;
				}
			}
		} else {
			result = SMGPResult.createFailtrueResult("cmpp submit error:tcp 已断开");

		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("cmpp submit success phone[" + destPhoneNo + "],[" + msgContent + "] ");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {
			log.error("cmpp submit error [" + destPhoneNo + "],[" + msgContent + "] :" + result.getMessage());
		}

		return result;
	}

	public SMGPResult activeTest() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run activeTest()...");
		}
		SMGPResult result = null;
		if (isTCPConnected() && isCmppConnected()) {

			SMGPMessage respMessage = sendSMGPMessageWithLock(new SMGPActivceTestMessage(), SMGP_ACTIVE_TEST_WAIT_TIME);
			if (respMessage == null) {
				// 失败，超时
				result = SMGPResult.createFailtrueResult("cmpp active test error:超时");
			} else {
				result = SMGPResult.createSuccessResult();
			}
		} else {
			result = SMGPResult.createFailtrueResult("cmpp active test error:tcp 已断开");
		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("cmpp active test success. ");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {
			log.error("cmpp active test error:" + result.getMessage());
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

	public SMGPResult connectTcp() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run connect()...");
		}
		return connect(true);
	}

	public SMGPResult reconnectTcp() throws Exception {
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
	private SMGPResult reconnect(boolean stopThread) throws Exception {
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
			SMGPMessage respMessage = sendSMGPMessageWithLock(new SMGPExitMessage(), SMGP_TERMINATE_WAIT_TIME);
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

	private SMGPResult connect(boolean stopThread) throws Exception {
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
				return SMGPResult.createFailtrueResult("tcp connect fail:连不上tcp，可能是网络问题。" + e.getMessage());

			}
			if (log.isInfoEnabled()) {
				log.info("tcp connect success to host[" + this.host + "] port[" + this.port + "]");
			}

		}
		// 发送连接请求
		if (log.isInfoEnabled()) {
			log.info("cmpp connect start...");
		}
		SMGPLoginMessage connectMessage = new SMGPLoginMessage(2, this.corpId, this.passwd, "");

		// 等待返回
		String key = Integer.toString(connectMessage.getSequence_id());
		Object lock = new Object();
		lockMap.put(key, lock);
		synchronized (lock) {
			this.ioSession.write(connectMessage);
			lock.wait(SMGP_CONNECT_WAIT_TIME);
		}
		lockMap.remove(key);
		SMGPMessage respMessage = respMap.remove(key);
		SMGPResult result = null;
		if (respMessage == null) {
			// 失败，超时
			result = SMGPResult.createFailtrueResult("cmpp connect error:超时");

		} else if (respMessage.getRequestID() != SMGPMessage.ID_SMGP_LOGIN_RESP) {
			result = SMGPResult.createFailtrueResult("cmpp connect error:返回commandId 不匹配");
		} else {
			SMGPLoginRespMessage connectRespMessage = (SMGPLoginRespMessage) respMessage;
			switch (connectRespMessage.getResult()) {
			case 0:
				result = SMGPResult.createSuccessResult();
				break;
			default:
				result = SMGPResult.createFailtrueResult("cmpp connect error:代码：" + connectRespMessage.getResult());
				break;
			}
		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("cmpp connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] success");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {
			log.error("cmpp connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] error:"
					+ result.getMessage());
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
	 * 同步发送SMGP Message
	 * 
	 * @param reqMessage
	 * @param waitTime
	 * @return
	 * @throws Exception
	 */
	private SMGPMessage sendSMGPMessageWithLock(SMGPMessage reqMessage, long waitTime) throws Exception {
		// 等待返回
		String key = Integer.toString(reqMessage.getSequence_id());
		Object lock = new Object();
		lockMap.put(key, lock);
		synchronized (lock) {
			this.ioSession.write(reqMessage);
			lock.wait(waitTime);
		}
		lockMap.remove(key);
		SMGPMessage respMessage = respMap.remove(key);
		return respMessage;
	}

	/**
	 * 直接向对方发送一个SMGP Message，本方法目前只用于发送active的返回值
	 * 
	 * @param reqMessage
	 * @throws Exception
	 */
	private void sendSMGPMessageWithNoLock(SMGPMessage reqMessage) throws Exception {
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
		SMGPMessage cmppMessage = (SMGPMessage) message;
		if (log.isDebugEnabled()) {
			log.debug("get message from tcp command_id[" + cmppMessage.getRequestID() + "]");
		}
		if (cmppMessage.getRequestID() == SMGPMessage.ID_SMGP_LOGIN_RESP
				|| cmppMessage.getRequestID() == SMGPMessage.ID_SMGP_SUBMIT_RESP
				|| cmppMessage.getRequestID() == SMGPMessage.ID_SMGP_ACTIVE_TEST_RESP
				|| cmppMessage.getRequestID() == SMGPMessage.ID_SMGP_EXIT_RESP) {
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

		} else if (cmppMessage.getRequestID() == SMGPMessage.ID_SMGP_ACTIVE_TEST) {
			// 如果是对方发送的active，则我这里要返回一个响应
			SMGPActivceTestMessage activeMsg = (SMGPActivceTestMessage) cmppMessage;
			SMGPActivceTestRespMessage activeRespMsg = new SMGPActivceTestRespMessage(activeMsg);
			sendSMGPMessageWithNoLock(activeRespMsg);
			if (log.isDebugEnabled()) {
				log.debug("send active response message to remote:Sequence_id[" + activeRespMsg.getSequence_id() + "]");
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

						reconnect(false);

						waitTimeSinceLastActiveTest = 0;
						continue;
					}

					if (waitTimeSinceLastActiveTest < SMGP_ACTIVE_TEST_SLEEP_TIME && !isAppearedTimeout()) {
						// 不进行测试，
						waitTimeSinceLastActiveTest += SEND_THREAD_SLEEP_TIME;
						continue;
					}

					// 发送
					SMGPResult result = activeTest();
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
					// 如果是线程错误，则暂定本线程
					interrupt();
					activeTestThreadStopped = true;
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
