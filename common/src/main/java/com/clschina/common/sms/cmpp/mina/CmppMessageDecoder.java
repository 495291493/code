/**
 * 
 */
package com.clschina.common.sms.cmpp.mina;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.clschina.common.sms.cmpp.cmpp20.CMPPMessage;

/**
 * 对将字节流转成高级对象
 * 
 * @author Wu Xiao Fei
 * 
 */
public class CmppMessageDecoder implements ProtocolDecoder {
	private static Log log = LogFactory.getLog(CmppMessageDecoder.class);

	/**
	 * 将字节流转成对象
	 */
	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {
		try {
			CMPPMessage message = CMPPMessage.read(in.asInputStream());
			out.write(message);
		} catch (Exception e) {
			if (log.isInfoEnabled()) {
				log.info("decode message error:" + e.getMessage());
			}
		}
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("CmppMessageDecoder.dispose");
		}

	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
		if (log.isInfoEnabled()) {
			log.info("CmppMessageDecoder.finishDecode");
		}

	}

}
