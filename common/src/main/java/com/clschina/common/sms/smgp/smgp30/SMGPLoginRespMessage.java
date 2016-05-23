package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;



public class SMGPLoginRespMessage extends SMGPMessage {
	private int status;
	private String pwd;
	private int version;

	public static final int LEN_SMGP_LOGINRESP = 21;
	private byte[] body = null;


	public SMGPLoginRespMessage(SMGPHeader header, byte[] body) throws IllegalArgumentException {
		this.header = header;
		this.body = body;
		status = byte4ToInteger(body, 0);
		pwd = new String(body,4,16);
		version = body[20];
		if(body.length != LEN_SMGP_LOGINRESP) {
			throw new IllegalArgumentException("login response message body: invalid size");
		}
	}


	public SMGPLoginRespMessage(SMGPLoginMessage bind) {
		this.header = bind.getCloneMsgHeader();
		header.setPacket_length(SMGPHeader.LEN_SMGP_HEADER + LEN_SMGP_LOGINRESP);
		header.setRequest_id(LEN_SMGP_LOGINRESP, false);
		body = new byte[LEN_SMGP_LOGINRESP];
		body[0] = 0;	// status
	}


	public int getResult() {
		return status;
	}



	@Override
	protected byte[] getMsgBody() {
		
		return body;
	}

	@Override
	public void resp(OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}


	public String getPwd() {
		return pwd;
	}
}
