package com.clschina.common.sms.smgp.mima;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author Wu Xiao Fei
 * 
 */
public class SMGPProtocolCodecFactory implements ProtocolCodecFactory {

	private ProtocolDecoder decoder = new SMGPMessageDecoder();
	private ProtocolEncoder encoder = new SMGPMessageEncoder();

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return encoder;
	}

}
