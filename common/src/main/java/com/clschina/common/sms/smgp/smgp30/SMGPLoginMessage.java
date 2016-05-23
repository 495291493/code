package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

public class SMGPLoginMessage extends SMGPMessage {
	public final static int LEN_SMGP_LOGIN = 30;
	private int login_type;
	private String login_name, login_pwd, spid;

	private int version = 0x13;
	private SimpleDateFormat format = new SimpleDateFormat("MMddhhmmss");

	public SMGPLoginMessage(int login_type, String login_name, String login_pwd, String spid) {
		this.login_type = login_type;
		this.login_name = login_name;
		this.login_pwd = login_pwd;
		// this.spid = spid;
		header = new SMGPHeader();
		header.setPacket_length(SMGPHeader.LEN_SMGP_HEADER + LEN_SMGP_LOGIN);
		header.setRequest_id(ID_SMGP_LOGIN, true);

	}

	public SMGPLoginMessage(SMGPHeader header, byte[] body) {
		this.header = header;
		if (body.length != LEN_SMGP_LOGIN) {
			throw new IllegalArgumentException("bind message body: invalid size");
		}
	}

	@Override
	protected byte[] getMsgBody() {
		byte[] body = new byte[LEN_SMGP_LOGIN];
		System.arraycopy(login_name.getBytes(), 0, body, 0, login_name.getBytes().length);

		String timestamp = new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis());
		byte[] tmp = new byte[login_name.getBytes().length + 7 + login_pwd.getBytes().length
				+ timestamp.getBytes().length];

		System.arraycopy(login_name.getBytes(), 0, tmp, 0, login_name.getBytes().length);

		System.arraycopy(new byte[] { 0, 0, 0, 0, 0, 0, 0 }, 0, tmp, login_name.getBytes().length, 7);

		System.arraycopy(login_pwd.getBytes(), 0, tmp, login_name.getBytes().length + 7, login_pwd.getBytes().length);

		System.arraycopy(timestamp.getBytes(), 0, tmp, login_name.getBytes().length + 7 + login_pwd.getBytes().length,
				timestamp.getBytes().length);
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(tmp);
			tmp = md5.digest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.arraycopy(tmp, 0, body, 8, tmp.length);
		body[24] = (byte) login_type;
		tmp = integerToByte(Integer.parseInt(timestamp));
		System.arraycopy(tmp, 0, body, 25, 4);
		body[29] = (byte) version;
		return body;
	}

	@Override
	public void resp(OutputStream out) throws IOException {
		new SMGPLoginRespMessage(this).write(out);

	}

}
