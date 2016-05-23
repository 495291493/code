package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;

public class SMGPDeliverRespMessage extends SMGPMessage {
	public static final int LEN_SMGP_DELIVER_RESP = 14;
	private byte[] body = null;
	
	
	public SMGPDeliverRespMessage(SMGPDeliverMessage bind) {
		this.header = bind.getCloneMsgHeader();
		header.setPacket_length(SMGPHeader.LEN_SMGP_HEADER + LEN_SMGP_DELIVER_RESP);
		header.setRequest_id(ID_SMGP_DELIVER_RESP, false);
		body = new byte[LEN_SMGP_DELIVER_RESP];
		byte [] tmp = bind.getMsg_id().getBytes();
		System.arraycopy(tmp, 0, body, 0, tmp.length);
		System.arraycopy(integerToByte(0), 0, body, 10, 4);
	}
	@Override
	protected byte[] getMsgBody() {
		// TODO Auto-generated method stub
		return body;
	}

	@Override
	public void resp(OutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

}
