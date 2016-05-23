package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;

public class SMGPExitRespMessage extends SMGPMessage {
	public static final int LEN_SGMGP_SUBMIT = 12;
	private byte[] body = null;
	public SMGPExitRespMessage(SMGPExitMessage exit) throws IllegalArgumentException {
		this.header = exit.getCloneMsgHeader();


		header = getCloneMsgHeader();
		header.setPacket_length(12);
		header.setRequest_id(ID_SMGP_EXIT_RESP, false);
		
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
