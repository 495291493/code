package com.clschina.common.sms.sgip.sgip12;

import java.io.IOException;
import java.io.OutputStream;

public class SGIPBindMessage extends SGIPMessage {

	public final static int LEN_SGIP_BIND = 41;
	private int login_type;
	private String login_name, login_pwd;

	public SGIPBindMessage(int login_type, String login_name, String login_pwd) {
		this.login_type = login_type;
		this.login_name = login_name;
		this.login_pwd = login_pwd;
		header = new SGIPHeader();
		header.setTotalLength(SGIPHeader.LEN_SGIP_HEADER + LEN_SGIP_BIND);
		header.setCommandID(SGIPMessage.ID_SGIP_BIND, true);
	}

	public SGIPBindMessage(SGIPHeader header, byte[] body) {
		this.header = header;
		if (body.length != LEN_SGIP_BIND) {
			throw new IllegalArgumentException("bind message body: invalid size");
		}
	}

	protected byte[] getMsgBody() {
		byte[] buffer = new byte[LEN_SGIP_BIND];
		buffer[0] = (byte) login_type;
		byte[] tmp = login_name.getBytes();
		System.arraycopy(tmp, 0, buffer, 1, tmp.length);
		tmp = login_pwd.getBytes();
		System.arraycopy(tmp, 0, buffer, 17, tmp.length);
		return buffer;
	}
}