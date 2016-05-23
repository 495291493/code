package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;

public class SMGPExitMessage extends SMGPMessage {
	public static final int LEN_SGMGP_SUBMIT = 12;
	private byte[] body = null;

	public SMGPExitMessage() {
		header = new SMGPHeader();
		header.setPacket_length(12);
		header.setRequest_id(ID_SMGP_EXIT, true);
	}

	public SMGPExitMessage(SMGPHeader header, byte[] body) throws IllegalArgumentException {
		this.header = header;

		header = getCloneMsgHeader();
		header.setPacket_length(12);
		header.setRequest_id(ID_SMGP_EXIT, true);

	}

	@Override
	protected byte[] getMsgBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resp(OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

}
