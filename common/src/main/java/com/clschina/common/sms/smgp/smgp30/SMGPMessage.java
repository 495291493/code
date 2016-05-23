package com.clschina.common.sms.smgp.smgp30;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.sms.cmpp.cmpp20.CMPPMessage;

public abstract class SMGPMessage {
	private static Log log = LogFactory.getLog(SMGPMessage.class);

	public static final int ID_SMGP_LOGIN = 1;
	public static final int ID_SMGP_LOGIN_RESP = 0x80000001;
	public static final int ID_SMGP_EXIT = 6;
	public static final int ID_SMGP_EXIT_RESP = 0x80000006;
	public static final int ID_SMGP_SUBMIT = 2;
	public static final int ID_SMGP_SUBMIT_RESP = 0x80000002;
	public static final int ID_SMGP_DELIVER = 3;
	public static final int ID_SMGP_DELIVER_RESP = 0x80000003;
	public static final int ID_SMGP_ACTIVE_TEST = 4;
	public static final int ID_SMGP_ACTIVE_TEST_RESP = 0x80000004;

	public static final int ID_SMGP_FORWARD = 5;
	public static final int ID_SMGP_FORWARD_RESP = 0x80000005;

	private static String getCommandIDName(int id) {
		switch (id) {
		case ID_SMGP_LOGIN:
			return "ID_SMGP_LOGIN";
		case ID_SMGP_LOGIN_RESP:
			return "ID_SMGP_LOGIN_RESP";
		case ID_SMGP_EXIT:
			return "ID_SMGP_EXIT";
		case ID_SMGP_EXIT_RESP:
			return "ID_SMGP_EXIT_RESP";
		case ID_SMGP_SUBMIT:
			return "ID_SMGP_SUBMIT";
		case ID_SMGP_SUBMIT_RESP:
			return "ID_SMGP_SUBMIT_RESP";
		case ID_SMGP_DELIVER:
			return "ID_SMGP_DELIVER";
		case ID_SMGP_DELIVER_RESP:
			return "ID_SMGP_DELIVER_RESP";
		case ID_SMGP_ACTIVE_TEST:
			return "ID_SMGP_ACTIVE_TEST";
		case ID_SMGP_ACTIVE_TEST_RESP:
			return "ID_SMGP_ACTIVE_TEST_RESP";
		case ID_SMGP_FORWARD:
			return "ID_SMGP_FORWARD";
		case ID_SMGP_FORWARD_RESP:
			return "ID_SMGP_FORWARD_RESP";
		default:
			return "UNKNOW";
		}
	}

	protected SMGPHeader header = null;

	public SMGPHeader getHeader() {
		return header;
	}

	public void setHeader(SMGPHeader header) {
		this.header = header;
	}

	/** get the command id */
	public int getRequestID() {
		return header.getRequest_id();
	}

	public int getSequence_id() {
		return header.getSequence_id();
	}

	/**
	 * clone a message header
	 */
	public SMGPHeader getCloneMsgHeader() {
		return (SMGPHeader) header.clone();
	}

	/**
	 * this abstract method should be overrided
	 * 
	 * @return the byte array describes the current message body
	 */
	protected abstract byte[] getMsgBody();

	public abstract void resp(OutputStream out) throws IOException;

	/**
	 * construct a new {@link SMGPMessage} object according to the inputstream
	 * 
	 * @throws IOException
	 */
	public static SMGPMessage read(InputStream in) throws IOException {
		SMGPHeader tmp = new SMGPHeader();
		tmp.readMsgHeader(in); // get message header
		byte[] buffer = null;
		int body_length = tmp.getPacket_length() - SMGPHeader.LEN_SMGP_HEADER;
		if (body_length > 0) {
			if (body_length > 2000) {
				throw new SocketException("the body length overflow: " + body_length);
			}
			buffer = new byte[body_length];
			int actual_length = 0, read_bytes;
			do {
				read_bytes = in.read(buffer, actual_length, body_length - actual_length);
				actual_length += read_bytes;
			} while (actual_length < body_length && read_bytes > 0);
			if (body_length != actual_length) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < body_length; i++) {
					sb.append("body[").append(i).append("] = ").append(buffer[i] + ", ");
				}
				throw new IOException("can't get actual length of message body from the inputstream");
			}
		}

		if (log.isDebugEnabled()) {
			if (tmp.getRequest_id() != SMGPMessage.ID_SMGP_ACTIVE_TEST) {
				log.debug("read resp command(request) id(" + getCommandIDName(tmp.getRequest_id()) + "): "
						+ tmp.getSequence_id());
			}
		}

		switch (tmp.getRequest_id()) {
		case SMGPMessage.ID_SMGP_LOGIN:
			return new SMGPLoginMessage(tmp, buffer);

		case SMGPMessage.ID_SMGP_LOGIN_RESP:
			return new SMGPLoginRespMessage(tmp, buffer);

		case SMGPMessage.ID_SMGP_EXIT:
			return new SMGPExitMessage(tmp, buffer);

		case SMGPMessage.ID_SMGP_SUBMIT_RESP:
			return new SMGPSubmitRespMessage(tmp, buffer);

		case SMGPMessage.ID_SMGP_DELIVER:
			return new SMGPDeliverMessage(tmp, buffer);

		case SMGPMessage.ID_SMGP_ACTIVE_TEST:
			return new SMGPActivceTestMessage(tmp, buffer);

		}
		return null;

	}

	/**
	 * write a {@link SMGPMessage} object to the SocketOutputStream
	 * 
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		byte[] tmp_head = header.getMsgHeader(); // get message header
		byte[] tmp_body = getMsgBody(); // get message body
		int length = tmp_head.length;
		if (tmp_body != null)
			length += tmp_body.length;

		byte[] message = new byte[length];
		// write message header
		System.arraycopy(tmp_head, 0, message, 0, SMGPHeader.LEN_SMGP_HEADER);
		if (tmp_body != null) {
			// write message body
			System.arraycopy(tmp_body, 0, message, SMGPHeader.LEN_SMGP_HEADER, tmp_body.length);
		}
		out.write(message);

		if (log.isDebugEnabled()) {
			log.debug("send command [" + getCommandIDName(getRequestID()) + "]: " + getSequence_id());
		}
		out.flush();
	}

	public static int byte2int(byte[] bts, int offset) {
		int n = 0;
		int tem = 0;
		for (int j = offset; j < offset + 4; j++) {
			n <<= 8;
			tem = bts[j] & 0xff;
			n |= tem;
		}
		return n;
	}

	/**
	 * convert 4 bytes to a Integer
	 * 
	 * @param b
	 *            the byte array, sorted from height to low
	 * @param offset
	 *            the offset value
	 * @return byte[offset], byte[offset+1], byte[offset+2], byte[offset+3]
	 */
	protected static int byte4ToInteger(byte[] b, int offset) {
		return (0xff & b[offset]) << 24 | (0xff & b[offset + 1]) << 16 | (0xff & b[offset + 2]) << 8
				| (0xff & b[offset + 3]);
	}

	/**
	 * convert a integer to 4 bytes
	 * 
	 * @param n
	 *            the integer want to be converted to bytes
	 * @return byte array sorted from height to low, the size is 4
	 */
	protected static byte[] integerToByte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}

	public static int byte2int(byte[] bts) {
		int n = 0;
		int tem = 0;
		for (int j = 0; j < bts.length; j++) {
			n <<= 8;
			tem = bts[j] & 0xff;
			n |= tem;
		}
		return n;
	}
}
