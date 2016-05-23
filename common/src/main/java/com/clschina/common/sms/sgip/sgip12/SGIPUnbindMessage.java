package com.clschina.common.sms.sgip.sgip12;


public class SGIPUnbindMessage extends SGIPMessage {

	public SGIPUnbindMessage() {
		header = new SGIPHeader();
		header.setTotalLength(SGIPHeader.LEN_SGIP_HEADER);
		header.setCommandID(SGIPMessage.ID_SGIP_UNBIND, true);
	}

	public SGIPUnbindMessage(SGIPHeader header) {
		this.header = header;
	}

	protected byte[] getMsgBody() {
		return null;
	}
}