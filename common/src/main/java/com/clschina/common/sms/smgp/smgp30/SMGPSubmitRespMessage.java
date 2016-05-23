package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.sms.smgp.SMGPApi;

public class SMGPSubmitRespMessage extends SMGPMessage {
	private static Log log = LogFactory.getLog(SMGPApi.class);

	public static final int LEN_SGMGP_SUBMIT = 14;
	private byte[] body = null;
	private String msg_id;
	private int status;
	private String tmpmsgid;

	public SMGPSubmitRespMessage(SMGPHeader header, byte[] body) throws IllegalArgumentException {
		this.header = header;
		this.body = body;
		String tmp_msg_id = null;
		try {
			tmp_msg_id = new String(body, 0, 10, "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			if (log.isErrorEnabled()) {
				log.error("error", e);
			}
		}
		byte tmp_1[] = new byte[3];
		byte tmp_2[] = new byte[4];
		byte tmp_3[] = new byte[3];
		System.arraycopy(body, 0, tmp_1, 0, 3);
		System.arraycopy(body, 3, tmp_2, 0, 4);
		System.arraycopy(body, 7, tmp_3, 0, 3);

		msg_id = BCDUtil.bcd2Str(tmp_1) + "-" + BCDUtil.bcd2Str(tmp_2) + "-" + BCDUtil.bcd2Str(tmp_3);

		status = byte4ToInteger(body, 10);

		if (body.length != LEN_SGMGP_SUBMIT) {
			throw new IllegalArgumentException("login response message body: invalid size");
		}
	}

	public String getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public String getTmpmsgid() {
		return tmpmsgid;
	}

	public void setTmpmsgid(String tmpmsgid) {
		this.tmpmsgid = tmpmsgid;
	}

}
