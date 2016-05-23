package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class SMGPSubmitMessageLongTlv extends SMGPMessage {
	public static final int LEN_SGMGP_SUBMIT = 114 + 12;
	private byte[] body = null;

	private int subType = 6;
	private int is_need_report = 1;
	private int priority = 0;
	private String service_id = "help";
	private String fee_type = "00";
	private String fee_code = "0";
	private int msg_format = 15;
	private String valid_date = "";
	private String at_time = "";
	private String src_terminal_id = "";
	private String charge_terminal_id = "";
	private int dest_terminal_count = 1;
	private String dest_terminal_id = "";
	private int msg_length = 0;

	private String reserve;

	public SMGPSubmitMessageLongTlv(String src_terminal_id, String dest_terminal_id, String msg_content,
			String service_id, String spid) throws UnsupportedEncodingException {

		byte[] msg = null;
		if (msg_content == null) {
			throw new IllegalArgumentException("the message content is null");
		}
		if (msg_format == 15) { // GBK
			msg = msg_content.getBytes("GBK");
		} else if (msg_format == 8) { // UCS2
			msg = msg_content.getBytes("UnicodeBigUnmarked");

		} else {
			msg = msg_content.getBytes("ISO8859_1");
		}

		msg_length = msg.length;

		this.dest_terminal_id = dest_terminal_id;
		this.src_terminal_id = src_terminal_id;
		this.charge_terminal_id = dest_terminal_id;
		this.service_id = service_id;

		String tmpMobile[] = dest_terminal_id.split(",");
		body = new byte[LEN_SGMGP_SUBMIT + msg_length + 21 * tmpMobile.length];

		body[0] = (byte) subType;
		body[1] = (byte) is_need_report;
		body[2] = (byte) priority;
		byte[] tmp = service_id.getBytes("GBK");
		System.arraycopy(tmp, 0, body, 3, tmp.length);
		tmp = fee_type.getBytes();
		System.arraycopy(tmp, 0, body, 13, tmp.length);
		tmp = fee_code.getBytes();
		System.arraycopy(tmp, 0, body, 15, tmp.length);
		String fixedFee = "0";
		tmp = fixedFee.getBytes();
		System.arraycopy(tmp, 0, body, 21, tmp.length);
		body[27] = (byte) msg_format;
		tmp = this.src_terminal_id.getBytes();
		System.arraycopy(tmp, 0, body, 62, tmp.length);
		tmp = charge_terminal_id.getBytes();
		System.arraycopy(tmp, 0, body, 83, tmp.length);
		body[104] = (byte) tmpMobile.length;
		int i = 0;
		for (i = 0; i < tmpMobile.length; i++)
			System.arraycopy(tmpMobile[i].getBytes(), 0, body, 105 + i * 21, tmpMobile[i].length());

		int loc = 105 + i * 21;

		body[loc] = (byte) msg_length;
		System.arraycopy(msg, 0, body, loc + 1, msg.length);
		loc = loc + 1 + msg.length + 8;
		tmp = integerToByte(0x0010);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		String SPID = spid;
		tmp = integerToByte(8);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		tmp = SPID.getBytes("GBK");
		System.arraycopy(tmp, 0, body, loc, tmp.length);
		header = new SMGPHeader();
		header.setPacket_length(body.length + SMGPHeader.LEN_SMGP_HEADER);
		header.setRequest_id(SMGPMessage.ID_SMGP_SUBMIT, true);
	}

	public SMGPSubmitMessageLongTlv(String src_terminal_id, String dest_terminal_id, String msg_content,
			String service_id, String spid, int total, int index) throws UnsupportedEncodingException {

		byte[] msg = null;
		if (msg_content == null) {
			throw new IllegalArgumentException("the message content is null");
		}
		if (msg_format == 15) { // GBK
			msg = msg_content.getBytes("GBK");
		} else if (msg_format == 8) { // UCS2
			msg = msg_content.getBytes("UnicodeBigUnmarked");
		} else {
			msg = msg_content.getBytes("ISO8859_1");
		}
		msg_length = msg.length;

		this.dest_terminal_id = dest_terminal_id;
		this.src_terminal_id = src_terminal_id;
		this.charge_terminal_id = dest_terminal_id;
		this.service_id = service_id;

		String tmpMobile[] = dest_terminal_id.split(",");
		body = new byte[LEN_SGMGP_SUBMIT + msg_length + 6 + 15 + 21 * tmpMobile.length];

		body[0] = (byte) subType;
		body[1] = (byte) is_need_report;
		body[2] = (byte) priority;
		byte[] tmp = service_id.getBytes("GBK");
		System.arraycopy(tmp, 0, body, 3, tmp.length);
		tmp = fee_type.getBytes();
		System.arraycopy(tmp, 0, body, 13, tmp.length);
		tmp = fee_code.getBytes();
		System.arraycopy(tmp, 0, body, 15, tmp.length);
		String fixedFee = "0";
		tmp = fixedFee.getBytes();
		System.arraycopy(tmp, 0, body, 21, tmp.length);
		body[27] = (byte) msg_format;
		tmp = this.src_terminal_id.getBytes();
		System.arraycopy(tmp, 0, body, 62, tmp.length);
		tmp = charge_terminal_id.getBytes();
		System.arraycopy(tmp, 0, body, 83, tmp.length);
		body[104] = (byte) tmpMobile.length;
		int i = 0;
		for (i = 0; i < tmpMobile.length; i++)
			System.arraycopy(tmpMobile[i].getBytes(), 0, body, 105 + i * 21, tmpMobile[i].length());

		int loc = 105 + i * 21;

		msg_length = msg_length + 6;

		body[loc] = (byte) msg_length;
		body[loc + 1] = 0x05;
		body[loc + 2] = 0x00;
		body[loc + 3] = 0x03;
		body[loc + 4] = 0;
		body[loc + 5] = (byte) total;
		body[loc + 6] = (byte) index;
		System.arraycopy(msg, 0, body, loc + 7, msg.length);

		loc = loc + 7 + msg.length + 8;

		tmp = integerToByte(0x0002);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;

		tmp = integerToByte(1);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		tmp = integerToByte(1);
		System.arraycopy(tmp, 3, body, loc, 1);

		loc = loc + 1;

		tmp = integerToByte(0x0009);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		tmp = integerToByte(1);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		tmp = integerToByte(total);
		System.arraycopy(tmp, 3, body, loc, 1);
		loc = loc + 1;

		tmp = integerToByte(0x000a);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		tmp = integerToByte(1);
		System.arraycopy(tmp, 2, body, loc, 2);
		loc = loc + 2;
		tmp = integerToByte(index);
		System.arraycopy(tmp, 3, body, loc, 1);
		loc = loc + 1;

		tmp = integerToByte(0x0010);
		System.arraycopy(tmp, 2, body, loc, 2);

		loc = loc + 2;

		String SPID = spid;

		tmp = integerToByte(8);
		System.arraycopy(tmp, 2, body, loc, 2);

		loc = loc + 2;

		tmp = SPID.getBytes("GBK");
		System.arraycopy(tmp, 0, body, loc, tmp.length);

		header = new SMGPHeader();
		header.setPacket_length(body.length + SMGPHeader.LEN_SMGP_HEADER);
		header.setRequest_id(SMGPMessage.ID_SMGP_SUBMIT, true);

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
