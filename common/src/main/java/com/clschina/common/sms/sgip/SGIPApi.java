/**
 * 
 */
package com.clschina.common.sms.sgip;

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

import com.clschina.common.sms.sgip.mima.SGIPProtocolCodecFactory;
import com.clschina.common.sms.sgip.sgip12.SGIPBindMessage;
import com.clschina.common.sms.sgip.sgip12.SGIPBindRespMessage;
import com.clschina.common.sms.sgip.sgip12.SGIPMessage;
import com.clschina.common.sms.sgip.sgip12.SGIPSubmitMessage;
import com.clschina.common.sms.sgip.sgip12.SGIPSubmitRespMessage;
import com.clschina.common.sms.sgip.sgip12.SGIPUnbindMessage;

/**
 * 2个重要逻辑:出现锁超时，则说明出现问题，断连接（子线程）。如果完成，则如果还有其他锁，则不处理，如果没有了，则断连接（子线程）
 * 
 * @author Wu Xiao Fei
 * 
 */
public class SGIPApi implements IoHandler {
	private static Log log = LogFactory.getLog(SGIPApi.class);

	// 等待时间
	private static final long SGIP_CONNECT_WAIT_TIME = 15 * 1000;// 连接等待15秒
	private static final long SGIP_SUBMIT_WAIT_TIME = 15 * 1000;// 提交短信反馈等待15秒

	private static final long SGIP_TERMINATE_WAIT_TIME = 5 * 1000;// 连接等待5秒

	// 短信信息
	private static int SMS_SIGN_LENGTH = 6;// 签名6字符
	private static final int SMS_NORMAL_LENGTH = 70;// 短信正常70字符
	private static final int SMS_EACH_SPLIT_LENGTH = 65;// 长短信拆分后，每条短信为67字符

	// 基本的参数
	private String host;// 远程地址
	private int port;// 远程端口
	private String corpId;// 企业id（用户名）
	private String passwd;// 密码
	private String srcPhoneNo;// 源设备id，即发送短信的电话

	// 内部参数
	private boolean sgipConnected;// sgip是否已经连接
	private boolean inited = false;// 参数已经初始化。

	// mina
	private boolean connectorInited = false;
	private IoConnector ioConnector = null;
	private IoSession ioSession = null;

	// 锁
	private Map<String, Object> lockMap = new ConcurrentHashMap<String, Object>();

	// 返回值
	private Map<String, SGIPMessage> respMap = new ConcurrentHashMap<String, SGIPMessage>();

	// 关闭线程
	private Thread terminateTcpThread;
	private boolean terminateTcpThreadRunning = false;

	private void initIoConnector() {
		if (!connectorInited) {

			ioConnector = new NioSocketConnector();
			ioConnector.setConnectTimeoutMillis(30 * 1000);
			ioConnector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new SGIPProtocolCodecFactory()));
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

	public SGIPResult submitMsg(String destPhoneNo, String msgContent) throws Exception {
		return submitMsg(destPhoneNo, msgContent, SMS_SIGN_LENGTH);
	}

	public SGIPResult submitMsg(String destPhoneNo, String msgContent, int signLength) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run submitMsg('" + destPhoneNo + "','" + msgContent + "'," + signLength + ")...");
		}

		SGIPResult result = null;
		if (destPhoneNo == null || destPhoneNo.trim().length() == 0 || msgContent == null
				|| msgContent.trim().length() == 0) {
			return result = SGIPResult.createFailtrueResult("sgip submit error:参数不能为空 destPhoneNo[" + destPhoneNo
					+ "] msgContent[" + msgContent + "]");
		}

		if (!isTCPConnected() || !isSgipConnected()) {
			connectTcp();
		}

		if (isTCPConnected() && isSgipConnected()) {
			// 发送连接请求
			if (log.isInfoEnabled()) {
				log.info("sgip submit start...");
			}
			// 如果是长短信的话，将进行拆分，最后一条短信将上锁
			SGIPMessage respMessage = null;
			if (msgContent.length() > (SMS_NORMAL_LENGTH - SMS_SIGN_LENGTH)) {
				if (log.isInfoEnabled()) {
					log.info("长短信拆分[" + msgContent + "]...");
				}

				List<String> splitMsg = splitMsgContent(msgContent, signLength);
				int total = splitMsg.size();
				int i = 1;
				for (String tmp_content : splitMsg) {
					SGIPSubmitMessage submit = new SGIPSubmitMessage(this.srcPhoneNo, destPhoneNo, tmp_content,
							this.corpId, "", 0, total, i);
					if (log.isInfoEnabled()) {
						log.info("长短信结果[" + i + "/" + total + "]:" + tmp_content + "");
					}

					if (i == total) {
						// 最后一条，上锁
						respMessage = sendSGIPMessageWithLock(submit, SGIP_SUBMIT_WAIT_TIME);
					} else {
						// 不上锁提交
						this.ioSession.write(submit);

					}
					i++;
				}
			} else {

				respMessage = sendSGIPMessageWithLock(new SGIPSubmitMessage(this.srcPhoneNo, destPhoneNo, msgContent,
						this.corpId, ""), SGIP_SUBMIT_WAIT_TIME);

			}
			if (respMessage == null) {
				// 失败，超时
				result = SGIPResult.createFailtrueResult("sgip submit error:超时");

			} else if (respMessage.getCommandID() != SGIPMessage.ID_SGIP_SUBMIT_RESP) {
				result = SGIPResult.createFailtrueResult("sgip submit error:返回commandId 不匹配");
			} else {
				SGIPSubmitRespMessage submitRespMessage = (SGIPSubmitRespMessage) respMessage;
				switch (submitRespMessage.getResult()) {
				case 0:
					result = SGIPResult.createSuccessResult();
					break;
				default:
					result = SGIPResult.createFailtrueResult("sgip submit error:代码：" + submitRespMessage.getResult());
					break;
				}
			}
		} else {
			result = SGIPResult.createFailtrueResult("sgip submit error:tcp 已断开");

		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("sgip submit success phone[" + destPhoneNo + "],[" + msgContent + "] ");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {
			log.error("sgip submit error [" + destPhoneNo + "],[" + msgContent + "] :" + result.getMessage());
		}

		return result;
	}

	public static List<String> splitMsgContent(String msgContent, int signLength) {
		// 直接按66个字符截取
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

	/**
	 * 断开连接
	 * 
	 * @return
	 * @throws Exception
	 */
	public void terminateTcp() throws Exception {
		// 打开线程
		if (!this.terminateTcpThreadRunning) {
			terminateTcpThread = new TerminateTcpThread();
			terminateTcpThread.start();
		}
	}

	public SGIPResult connectTcp() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run connect()...");
		}
		return connect();
	}

	public SGIPResult reconnectTcp() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run reconnect()...");
		}
		terminate();
		return connect();
	}

	/**
	 * 断开连接
	 * 
	 * @return
	 * @throws Exception
	 */
	private void terminate() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run terminate()...");
		}
		if (isTCPConnected() && isSgipConnected()) {
			if (log.isInfoEnabled()) {
				log.info("sgip terminate start...");
			}
			setSgipConnected(false);
			SGIPMessage respMessage = sendSGIPMessageWithLock(new SGIPUnbindMessage(), SGIP_TERMINATE_WAIT_TIME);
			// 记录是否超时
			if (respMessage == null) {
				if (log.isInfoEnabled()) {
					log.info("sgip terminate error:超时");
				}
			}

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

	private SGIPResult connect() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("run connect()...");
		}
		if (!isTCPConnected()) {
			// 连接
			setSgipConnected(false);
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
				return SGIPResult.createFailtrueResult("tcp connect fail:连不上tcp，可能是网络问题。" + e.getMessage());

			}
			if (log.isInfoEnabled()) {
				log.info("tcp connect success to host[" + this.host + "] port[" + this.port + "]");
			}

		}
		// 发送连接请求
		if (log.isInfoEnabled()) {
			log.info("sgip connect start...");
		}
		SGIPBindMessage connectMessage = new SGIPBindMessage(1, this.corpId, this.passwd);// XXX,检查参数的对于不对

		// 等待返回
		String key = getMassageKey(connectMessage);
		Object lock = new Object();
		lockMap.put(key, lock);
		synchronized (lock) {
			this.ioSession.write(connectMessage);
			lock.wait(SGIP_CONNECT_WAIT_TIME);
		}
		lockMap.remove(key);
		SGIPMessage respMessage = respMap.remove(key);
		SGIPResult result = null;
		if (respMessage == null) {
			// 失败，超时
			result = SGIPResult.createFailtrueResult("sgip connect error:超时");

		} else if (respMessage.getCommandID() != SGIPMessage.ID_SGIP_BIND_RESP) {
			result = SGIPResult.createFailtrueResult("sgip connect error:返回commandId 不匹配");
		} else {
			SGIPBindRespMessage connectRespMessage = (SGIPBindRespMessage) respMessage;
			switch (connectRespMessage.getResult()) {
			case 0:
				result = SGIPResult.createSuccessResult();
				break;
			case 1:
				result = SGIPResult.createFailtrueResult("sgip connect error:代码：" + connectRespMessage.getResult()
						+ ":用户名和密码错误");
				break;
			default:
				result = SGIPResult.createFailtrueResult("sgip connect error:代码：" + connectRespMessage.getResult());
				break;
			}
		}

		if (log.isInfoEnabled() && result.isSuccess()) {
			log.info("sgip connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] success");
		}
		if (log.isErrorEnabled() && result.isNotSuccess()) {
			log.error("sgip connect corpId[" + this.corpId + "] passwd[" + this.passwd + "] error:"
					+ result.getMessage());
		}

		if (result.isSuccess()) {
			setSgipConnected(true);
		}

		return result;
	}

	/**
	 * 同步发送SGIP Message
	 * 
	 * @param reqMessage
	 * @param waitTime
	 * @return
	 * @throws Exception
	 */
	private SGIPMessage sendSGIPMessageWithLock(SGIPMessage reqMessage, long waitTime) throws Exception {
		// 等待返回
		String key = getMassageKey(reqMessage);
		Object lock = new Object();
		lockMap.put(key, lock);
		synchronized (lock) {
			this.ioSession.write(reqMessage);
			lock.wait(waitTime);
		}
		lockMap.remove(key);
		SGIPMessage respMessage = respMap.remove(key);

		if (respMessage == null) {
			// 超时，则断连接
			if (log.isInfoEnabled()) {
				log.info("出现超时，现在关闭连接...");
			}
			terminateTcp();
		} else {
			// 非超时，判断还有没有数据等待，有则不处理，无则关闭
			if (lockMap.size() == 0) {
				if (log.isInfoEnabled()) {
					log.info("无其他等待回应，现在关闭连接...");
				}
				terminateTcp();
			} else {
				if (log.isInfoEnabled()) {
					log.info("有其他等待回应，保持连接...");
				}
			}
		}

		return respMessage;
	}

	//
	private String getMassageKey(SGIPMessage reqMessage) {
		return Integer.toString(reqMessage.getSeq_no1()) + Integer.toString(reqMessage.getSeq_no2())
				+ Integer.toString(reqMessage.getSeq_no3());// 序列号为12字节
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

	public boolean isSgipConnected() {
		return sgipConnected;
	}

	void setSgipConnected(boolean sgipConnected) {
		this.sgipConnected = sgipConnected;
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
		SGIPMessage sgipMessage = (SGIPMessage) message;
		if (log.isDebugEnabled()) {
			log.debug("get message from tcp command_id[" + sgipMessage.getCommandID() + "]");
		}
		if (sgipMessage.getCommandID() == SGIPMessage.ID_SGIP_BIND_RESP
				|| sgipMessage.getCommandID() == SGIPMessage.ID_SGIP_SUBMIT_RESP
				|| sgipMessage.getCommandID() == SGIPMessage.ID_SGIP_UNBIND_RESP) {
			String key = getMassageKey(sgipMessage);

			if (log.isInfoEnabled() && sgipMessage.getCommandID() == SGIPMessage.ID_SGIP_SUBMIT_RESP) {
				SGIPSubmitRespMessage submitRespMessage = (SGIPSubmitRespMessage) sgipMessage;
				log.info("submitRespMessage[" + key + "] result:" + submitRespMessage.getResult());
			}

			Object lock = lockMap.remove(key);
			if (lock != null) {
				if (log.isDebugEnabled()) {
					log.debug("Sequence_id[" + key + "] has lock");
				}
				respMap.put(key, sgipMessage);
				synchronized (lock) {
					lock.notifyAll();
				}
			} else {
				// 如果没有lock，表示已经没有线程等待了，所以消息丢弃
				if (log.isDebugEnabled()) {
					log.debug("Sequence_id[" + key + "] has not lock");
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

	/**
	 * 关闭tcp的子线程
	 * 
	 * @author Wu Xiao Fei
	 * 
	 */
	private class TerminateTcpThread extends Thread {

		@Override
		public void run() {
			if (log.isInfoEnabled()) {
				log.info("terminateTcpThread run...");
			}

			if (!interrupted() && !terminateTcpThreadRunning) {
				// 开始测试
				terminateTcpThreadRunning = true;
				try {
					terminate();
				} catch (Exception e) {
					if (log.isInfoEnabled()) {
						log.info("terminate error:" + e.getMessage(), e);
					}
				}
				terminateTcpThreadRunning = false;
			}
			if (log.isInfoEnabled()) {
				log.info("terminateTcpThread stop...");
			}
		}

		private TerminateTcpThread() {
		}
	}

}
