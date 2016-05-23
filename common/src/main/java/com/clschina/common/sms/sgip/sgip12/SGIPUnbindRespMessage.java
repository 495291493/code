package com.clschina.common.sms.sgip.sgip12;


public class SGIPUnbindRespMessage extends SGIPMessage {

	public SGIPUnbindRespMessage(SGIPHeader header) {
		this.header = header;
	}

	public SGIPUnbindRespMessage(SGIPUnbindMessage unbind) {
		this.header = unbind.getCloneMsgHeader();
		header.setTotalLength(SGIPHeader.LEN_SGIP_HEADER);
		header.setCommandID(SGIPMessage.ID_SGIP_UNBIND_RESP, false);
	}

	protected byte[] getMsgBody() {
		return null;
	}

}