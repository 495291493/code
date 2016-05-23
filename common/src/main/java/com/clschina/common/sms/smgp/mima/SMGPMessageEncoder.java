/**
 * 
 */
package com.clschina.common.sms.smgp.mima;

import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.clschina.common.sms.smgp.smgp30.SMGPMessage;

/**
 * 将对象转成二级制
 * 
 * @author Wu Xiao Fei
 * 
 */
public class SMGPMessageEncoder implements ProtocolEncoder {
	private static Log log = LogFactory.getLog(SMGPMessageEncoder.class);

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
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		SMGPMessage smgpMessage = (SMGPMessage) message;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		smgpMessage.write(bout);
		IoBuffer buf = IoBuffer.wrap(bout.toByteArray());
		out.write(buf);
	}
}
