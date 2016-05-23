package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;

public class SMGPActivceTestRespMessage extends SMGPMessage {

	public SMGPActivceTestRespMessage(SMGPActivceTestMessage test) {
		header = test.getCloneMsgHeader();
		header.setPacket_length(12);
		header.setRequest_id(ID_SMGP_ACTIVE_TEST_RESP, false);
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
