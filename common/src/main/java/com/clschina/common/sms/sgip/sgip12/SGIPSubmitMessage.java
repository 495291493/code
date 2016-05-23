package com.clschina.common.sms.sgip.sgip12;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SGIPSubmitMessage extends SGIPMessage {

	public static final int LEN_SGIP_SUBMIT = 144;
	private byte[] body = null;

	/** create a new submit message */
	public SGIPSubmitMessage(String sp_number, String dest_termid, String msg_content, String corp_id, String service_id)
			throws IllegalArgumentException {

		String fee_number = "000000000000000000000";
		// String corp_id = AppConfig.SGIP_CORPID;
		// String service_id = AppConfig.SGIP_SERVER;
		int fee_type = 2;
		String fee_value = "0";
		String give_value = "0";
		int agent_flag = 0;
		int mt_flag = 2;
		int priority = 0;
		Date valid_time = null;
		Date at_time = null;
		int need_report = 1;
		int msg_format = 15;
		String linkid = "";
		// check out the illegal arguments
		byte[] msg_bytes = null;
		int msg_length = 0;
		try {
			if (msg_content == null) {
				throw new IllegalArgumentException("the message content is null");
			}
			if (msg_format == 15) { // GBK
				msg_bytes = msg_content.getBytes("GBK");
			} else if (msg_format == 8) { // UCS2
				msg_bytes = msg_content.getBytes("UnicodeBigUnmarked");
			} else {
				msg_bytes = msg_content.getBytes("ISO8859_1");
			}
			msg_length = msg_bytes.length;
			if (msg_length > 350) {
				throw new IllegalArgumentException("too many characters to submit");
			}
			body = new byte[LEN_SGIP_SUBMIT + msg_length];
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("unsupported encoding");
		}

		System.arraycopy(sp_number.getBytes(), 0, body, 0, sp_number.length()); // put
																				// sp
																				// number
		System.arraycopy(fee_number.getBytes(), 0, body, 21, fee_number.length()); // fee
																					// terminal
																					// id
		body[42] = 1; // user count

		// dest terminal id
		if (dest_termid.length() > 21) {
			throw new IllegalArgumentException("the dest terminal id is too long, only permits one mobile");
		}
		System.arraycopy(dest_termid.getBytes(), 0, body, 43, dest_termid.length());

		System.arraycopy(corp_id.getBytes(), 0, body, 64, corp_id.length()); // corp
																				// id
		System.arraycopy(service_id.getBytes(), 0, body, 69, service_id.length()); // service
																					// type
		body[79] = (byte) fee_type; // fee type
		System.arraycopy(fee_value.getBytes(), 0, body, 80, fee_value.length()); // fee_value
		System.arraycopy(give_value.getBytes(), 0, body, 86, give_value.length()); // given
																					// value
		body[92] = (byte) agent_flag; // agent_flag
		body[93] = (byte) mt_flag; // mt_flag
		body[94] = (byte) priority; // priority

		if (valid_time != null || at_time != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
			String timestamp = null;
			if (valid_time != null) {
				timestamp = dateFormat.format(valid_time).concat("032+"); // according
																			// to
																			// SMPP
				System.arraycopy(timestamp.getBytes(), 0, body, 95, 16);
			}
			if (at_time != null) {
				timestamp = dateFormat.format(at_time).concat("032+");
				System.arraycopy(timestamp.getBytes(), 0, body, 111, 16);
			}
		}

		body[127] = (byte) need_report; // need report
		body[128] = 0; // TP_pid
		body[129] = 0; // TP_udhi
		body[130] = (byte) msg_format; // message format
		body[131] = 0; // message type
		System.arraycopy(SGIPMessage.integerToByte(msg_length), 0, body, 132, 4);// message
																					// length

		// message content
		System.arraycopy(msg_bytes, 0, body, 136, msg_length);

		// link id
		if (linkid != null) {
			System.arraycopy(linkid.getBytes(), 0, body, 136 + msg_length, Math.min(8, linkid.length()));
		}

		// create message header
		header = new SGIPHeader();
		header.setTotalLength(body.length + SGIPHeader.LEN_SGIP_HEADER);
		header.setCommandID(SGIPMessage.ID_SGIP_SUBMIT, true);
	}

	// ###########处理SGIP1.2长短信下发############# min
	public SGIPSubmitMessage(String sp_number, String dest_termid,

	String msg_content, String corp_id, String service_id, long node_id, int pktotal, int pknumber)
			throws IllegalArgumentException {

		String fee_number = "000000000000000000000";
		// String corp_id = corp_id;
		// String service_id = service_id;
		int fee_type = 2;
		String fee_value = "0";
		String give_value = "0";
		int agent_flag = 0;
		int mt_flag = 2;
		int priority = 0;
		Date valid_time = new Date();
		int need_report = 1;
		int msg_format = 8;
		String linkid = "";
		Date at_time = new Date();
		// check out the illegal arguments
		byte[] msg_bytes = null;
		int msg_length = 0;

		byte[] tp_udhiHead = new byte[6];
		tp_udhiHead[0] = 0x05;
		tp_udhiHead[1] = 0x00;
		tp_udhiHead[2] = 0x03;
		tp_udhiHead[3] = 0x0A;
		tp_udhiHead[4] = (byte) pktotal;
		tp_udhiHead[5] = (byte) pknumber;

		try {
			if (msg_content == null) {
				throw new IllegalArgumentException("the message content is null");
			}
			if (msg_format == 15) { // GBK
				msg_bytes = msg_content.getBytes("GBK");
			} else if (msg_format == 8) { // UCS2
				// msg_bytes = new
				// String(msg_content.getBytes("gbk")).getBytes("UnicodeBigUnmarked");
				msg_bytes = msg_content.getBytes("UnicodeBigUnmarked");
			} else {
				msg_bytes = msg_content.getBytes("ISO8859_1");
			}

			msg_length = msg_bytes.length + 6;// 6字节的协议头+短信内容长度
			/*
			 * if(msg_length > 160) { throw new
			 * IllegalArgumentException("too many characters to submit"); }
			 */
			body = new byte[LEN_SGIP_SUBMIT + msg_length];
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("unsupported encoding");
		}

		System.arraycopy(sp_number.getBytes(), 0, body, 0, sp_number.length()); // put
																				// sp
																				// number
		System.arraycopy(fee_number.getBytes(), 0, body, 21, fee_number.length()); // fee
																					// terminal
																					// id
		body[42] = 1; // user count

		if (dest_termid.length() > 21) {
			throw new IllegalArgumentException("the dest terminal id is too long, only permits one mobile");
		}
		System.arraycopy(dest_termid.getBytes(), 0, body, 43, dest_termid.length());

		System.arraycopy(corp_id.getBytes(), 0, body, 64, corp_id.length()); // corp
																				// id
		System.arraycopy(service_id.getBytes(), 0, body, 69, service_id.length()); // service
																					// type
		body[79] = (byte) fee_type; // fee type
		System.arraycopy(fee_value.getBytes(), 0, body, 80, fee_value.length()); // fee_value
		System.arraycopy(give_value.getBytes(), 0, body, 86, give_value.length()); // given
																					// value
		body[92] = (byte) agent_flag; // agent_flag
		body[93] = (byte) mt_flag; // mt_flag
		body[94] = (byte) priority; // priority

		body[127] = (byte) need_report; // need report
		body[128] = 0; // TP_pid
		body[129] = 1; // TP_udhi
		body[130] = (byte) msg_format; // message format
		body[131] = 0; // message type
		System.arraycopy(SGIPMessage.integerToByte(msg_length), 0, body, 132, 4);// message
																					// length

		// message content
		System.arraycopy(tp_udhiHead, 0, body, 136, 6);// 写入消息头
		System.arraycopy(msg_bytes, 0, body, 142, msg_bytes.length);

		// link id
		if (linkid != null) {
			System.arraycopy(linkid.getBytes(), 0, body, 136 + msg_length, Math.min(8, linkid.length()));
		}

		// create message header
		header = new SGIPHeader();
		header.setTotalLength(body.length + SGIPHeader.LEN_SGIP_HEADER);
		header.setCommandID(SGIPMessage.ID_SGIP_SUBMIT, true);
	}

	/** return the message body */
	protected byte[] getMsgBody() {
		return body;
	}
}