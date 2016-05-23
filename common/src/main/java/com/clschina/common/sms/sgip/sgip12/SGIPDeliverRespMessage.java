package com.clschina.common.sms.sgip.sgip12;


public class SGIPDeliverRespMessage extends SGIPMessage {

	public static final int LEN_SGIP_DELIVERRESP = 9;
	private byte[] body = null;

	public SGIPDeliverRespMessage(SGIPDeliverMessage deliver) throws IllegalArgumentException {
		this.header = deliver.getCloneMsgHeader();
		header.setTotalLength(SGIPHeader.LEN_SGIP_HEADER + LEN_SGIP_DELIVERRESP);
		header.setCommandID(SGIPMessage.ID_SGIP_DELIVER_RESP, false);
		body = new byte[LEN_SGIP_DELIVERRESP];
		body[0] = 0; // status
	}

	protected byte[] getMsgBody() {
		return body;
	}

}