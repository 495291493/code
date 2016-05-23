package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.sms.smgp.SMGPApi;

public class SMGPDeliverMessage extends SMGPMessage {

	private static Log log = LogFactory.getLog(SMGPDeliverMessage.class);

	public static final int LEN_SMGP_DELIVER = 77;
	private byte[] body = null;
	private String msg_id;
	private int is_report;
	private int msg_format;
	private String recv_time;
	private String scr_terminal_id;
	private String dest_terminal_id;
	private int msg_length;
	private String msg_content;
	private String reserve;

	private String report_msg_id;
	private String report_stat;
	private String report_err;

	public SMGPDeliverMessage(SMGPHeader header, byte[] body) throws IllegalArgumentException {
		this.header = header;
		this.body = body;

		msg_id = new String(body, 0, 10);
		is_report = body[10];
		msg_format = body[11];
		try {
			recv_time = new String(body, 12, 14, "ISO_8859_1").trim();

			scr_terminal_id = new String(body, 26, 21, "ISO_8859_1").trim();
			dest_terminal_id = new String(body, 47, 21, "ISO_8859_1").trim();
		} catch (UnsupportedEncodingException e1) {
			if (log.isErrorEnabled()) {
				log.error("error", e1);
			}
		}
		msg_length = body[68];
		int body69 = body[69];

		if (msg_format == 0) {
			try {

				msg_content = new String(body, 69, msg_length, "ISO8859_1").trim();
			} catch (UnsupportedEncodingException e) {
				if (log.isErrorEnabled()) {
					log.error("error", e);
				}
			}
		} else if (msg_format == 8) {

			try {

				msg_content = new String(body, 69, msg_length, "UnicodeBigUnmarked").trim();
			} catch (UnsupportedEncodingException e) {
				if (log.isErrorEnabled()) {
					log.error("error", e);
				}
			}
		} else {
			try {

				msg_content = new String(body, 69, msg_length, "GBK").trim();
			} catch (UnsupportedEncodingException e) {
				if (log.isErrorEnabled()) {
					log.error("error", e);
				}
			}
		}
		if (is_report == 1) {
			try {

				int pos = msg_content.indexOf("id:");
				String tmp_report_msg_id = msg_content.substring(pos + 3, pos + 13);
				byte tmp_1[] = new byte[4];
				System.arraycopy(tmp_report_msg_id.substring(0, 3).getBytes(), 0, tmp_1, 1, 3);

				byte tmp_2[] = new byte[4];
				tmp_2 = tmp_report_msg_id.substring(3, 7).getBytes();
				byte tmp_3[] = new byte[4];
				System.arraycopy(tmp_report_msg_id.substring(7, 10).getBytes(), 0, tmp_3, 1, 3);

				report_msg_id = byte4ToInteger(tmp_1, 0) + "-" + byte4ToInteger(tmp_2, 0) + "-"
						+ byte4ToInteger(tmp_3, 0);

				pos = msg_content.indexOf("stat:");
				report_stat = msg_content.substring(pos + 5, pos + 12);
				pos = msg_content.indexOf("err:");
				report_err = msg_content.substring(pos + 4, pos + 7);

			} catch (Exception e1) {
				if (log.isErrorEnabled()) {
					log.error("error", e1);
				}
			}
		}
		if (body.length != LEN_SMGP_DELIVER + msg_length) {
			throw new IllegalArgumentException("login response message body: invalid size");
		}

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

	public String getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}

	public int getIs_report() {
		return is_report;
	}

	public void setIs_report(int is_report) {
		this.is_report = is_report;
	}

	public String getScr_terminal_id() {
		return scr_terminal_id;
	}

	public void setScr_terminal_id(String scr_terminal_id) {
		this.scr_terminal_id = scr_terminal_id;
	}

	public String getDest_terminal_id() {
		return dest_terminal_id;
	}

	public void setDest_terminal_id(String dest_terminal_id) {
		this.dest_terminal_id = dest_terminal_id;
	}

	public String getMsg_content() {
		return msg_content;
	}

	public void setMsg_content(String msg_content) {
		this.msg_content = msg_content;
	}

	public String getReport_msg_id() {
		return report_msg_id;
	}

	public void setReport_msg_id(String report_msg_id) {
		this.report_msg_id = report_msg_id;
	}

	public String getReport_stat() {
		return report_stat;
	}

	public void setReport_stat(String report_stat) {
		this.report_stat = report_stat;
	}

	public String getReport_err() {
		return report_err;
	}

	public void setReport_err(String report_err) {
		this.report_err = report_err;
	}

}
