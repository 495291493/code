/**
 * 
 */
package com.clschina.common.sms.cmpp.mina;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.clschina.common.sms.cmpp.cmpp20.CMPPMessage;

/**
 * 将对象转成二级制
 * 
 * @author Wu Xiao Fei
 * 
 */
public class CmppMessageEncoder implements ProtocolEncoder {
	private static Log log = LogFactory.getLog(CmppMessageEncoder.class);

	/**
	 * 这里释放资源
	 */
	@Override
	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 将对象转成字节
	 */
	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		CMPPMessage cmppMessage = (CMPPMessage) message;
		IoBuffer buf = IoBuffer.wrap(cmppMessage.getWriteBytes());
		out.write(buf);
	}
}
