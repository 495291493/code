package com.clschina.common.sms.sgip.mima;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author Wu Xiao Fei
 * 
 */
public class SGIPProtocolCodecFactory implements ProtocolCodecFactory {

	private ProtocolDecoder decoder = new SGIPMessageDecoder();
	private ProtocolEncoder encoder = new SGIPMessageEncoder();

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return encoder;
	}

}
