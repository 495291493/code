package com.clschina.common.sms.cmpp.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author Wu Xiao Fei
 * 
 */
public class CmppProtocolCodecFactory implements ProtocolCodecFactory {

	private ProtocolDecoder decoder = new CmppMessageDecoder();
	private ProtocolEncoder encoder = new CmppMessageEncoder();

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return encoder;
	}

}
