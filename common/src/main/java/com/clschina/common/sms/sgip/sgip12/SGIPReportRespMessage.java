package com.clschina.common.sms.sgip.sgip12;


public class SGIPReportRespMessage extends SGIPMessage {

	public static final int LEN_SGIP_REPORTRESP = 9;
	private byte[] body = null;

	public SGIPReportRespMessage(SGIPReportMessage report) throws IllegalArgumentException {
		this.header = report.getCloneMsgHeader();
		header.setTotalLength(SGIPHeader.LEN_SGIP_HEADER + LEN_SGIP_REPORTRESP);
		header.setCommandID(SGIPMessage.ID_SGIP_REPORT_RESP, false);
		body = new byte[LEN_SGIP_REPORTRESP];
		body[0] = 0; // status
	}

	protected byte[] getMsgBody() {
		return body;
	}

}