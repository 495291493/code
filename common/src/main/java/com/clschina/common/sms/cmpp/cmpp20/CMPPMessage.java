package com.clschina.common.sms.cmpp.cmpp20;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.log4j.Logger;

public abstract class CMPPMessage {
	private static Log log = LogFactory.getLog(CMPPMessage.class);

	// 规定好的参数，对应于各个命令和相应的类型
	public final static int ID_CMPP_CONNECT = 1;
	public final static int ID_CMPP_CONNECT_RESP = 0x80000001;
	public final static int ID_CMPP_TERMINATE = 2;
	public final static int ID_CMPP_TERMINATE_RESP = 0x80000002;
	public final static int ID_CMPP_SUBMIT = 4;
	public final static int ID_CMPP_SUBMIT_RESP = 0x80000004;
	public final static int ID_CMPP_DELIVER = 5;
	public final static int ID_CMPP_DELIVER_RESP = 0x80000005;

	public final static int ID_CMPP_ACTIVE = 8;
	public final static int ID_CMPP_ACTIVE_RESP = 0x80000008;

	private static String getCommandIDName(int id) {
		switch (id) {
		case ID_CMPP_CONNECT:
			return "ID_CMPP_CONNECT";
		case ID_CMPP_CONNECT_RESP:
			return "ID_CMPP_CONNECT_RESP";
		case ID_CMPP_SUBMIT:
			return "ID_CMPP_SUBMIT";
		case ID_CMPP_SUBMIT_RESP:
			return "ID_CMPP_SUBMIT_RESP";
		case ID_CMPP_DELIVER:
			return "ID_CMPP_DELIVER";
		case ID_CMPP_DELIVER_RESP:
			return "ID_CMPP_DELIVER_RESP";
		case ID_CMPP_ACTIVE:
			return "ID_CMPP_ACTIVE";
		case ID_CMPP_ACTIVE_RESP:
			return "ID_CMPP_ACTIVE_RESP";
		case ID_CMPP_TERMINATE:
			return "ID_CMPP_TERMINATE";
		case ID_CMPP_TERMINATE_RESP:
			return "ID_CMPP_TERMINATE_RESP";

		default:
			return "NONE";
		}
	}

	protected CMPPHeader header = new CMPPHeader();

	/** get the command id */
	public int getCommand_id() {
		return header.getCommand_id();
	}

	/** get the command sequence code */
	public int getSequence_id() {
		return header.getSequence_id();
	}

	public void setSequence_id(int sequence) {
		header.setSequence_id(sequence);
	}

	/**
	 * clone a message header
	 */
	public CMPPHeader getCloneMsgHeader() {
		return (CMPPHeader) header.clone();
	}

	/**
	 * this abstract method should be overrided
	 * 
	 * @return the byte array describes the current message body
	 */
	protected abstract byte[] getMsgBody();

	/**
	 * construct a new {@link CMPPMessage} object according to the inputstream
	 */
	public static CMPPMessage read(InputStream in) throws IOException {
		CMPPHeader tmp = new CMPPHeader();
		tmp.readMsgHeader(in); // get message header
		byte[] buffer = null;
		int body_length = tmp.getTotal_length() - CMPPHeader.LEN_CMPP_HEADER;
		if (body_length > 0) {
			if (body_length > 2000) {
				throw new SocketException("the body length overflow: "
						+ body_length);
			}
			buffer = new byte[body_length];
			int actual_length = read(in, buffer);
		}

		if (log.isDebugEnabled()) {
			if (tmp.getCommand_id() != CMPPMessage.ID_CMPP_ACTIVE_RESP) {
				log.debug("read resp command id("
						+ getCommandIDName(tmp.getCommand_id()) + "): "
						+ tmp.getSequence_id());
			}
		}

		switch (tmp.getCommand_id()) {
		case CMPPMessage.ID_CMPP_SUBMIT_RESP:
			return new CMPPSubmitRespMessage(tmp, buffer);

		case CMPPMessage.ID_CMPP_DELIVER:
			return new CMPPDeliverMessage(tmp, buffer);

		case CMPPMessage.ID_CMPP_ACTIVE_RESP:
			return new CMPPActiveRespMessage(tmp, buffer);

		case CMPPMessage.ID_CMPP_ACTIVE:
			return new CMPPActiveMessage(tmp);

		case CMPPMessage.ID_CMPP_CONNECT_RESP:
			return new CMPPConnectRespMessage(tmp, buffer);

		case CMPPMessage.ID_CMPP_TERMINATE_RESP:
			return new CMPPTerminateRespMessage(tmp);
		}
		return null;
	}

	/**
	 * 将请求变成字节流
	 * 
	 * @return
	 * @throws Exception
	 */
	public byte[] getWriteBytes() throws Exception {
		byte[] tmp_head = header.getMsgHeader(); // get message header
		byte[] tmp_body = getMsgBody(); // get message body
		int length = tmp_head.length;
		if (tmp_body != null)
			length += tmp_body.length;
		byte[] message = new byte[length];
		// write message header
		System.arraycopy(tmp_head, 0, message, 0, CMPPHeader.LEN_CMPP_HEADER);
		if (tmp_body != null) {
			// write message body
			System.arraycopy(tmp_body, 0, message, CMPPHeader.LEN_CMPP_HEADER,
					tmp_body.length);
		}

		if (log.isDebugEnabled()) {
			log.debug("send command [" + getCommandIDName(getCommand_id())
					+ "]: " + getSequence_id());
		}
		return message;
	}

	public static int read(InputStream in, byte[] buffer) throws IOException {
		// int i = 0;
		int actual_length = 0, read_bytes;
		do {
			read_bytes = in.read(buffer, actual_length, buffer.length
					- actual_length);
			actual_length += read_bytes;
		} while (actual_length < buffer.length && read_bytes > 0);

		if (buffer.length != actual_length) { // get message body
			if (log.isInfoEnabled()) {
				log.info("read message error:read: body_length="
						+ buffer.length
						+ ", actual_length="
						+ actual_length
						+ ",can't get actual length of message body from the inputstream");
			}
			throw new IOException(
					"can't get actual length of message body from the inputstream");
		}
		return buffer.length;//
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
	public static int byte4ToInteger(byte[] b, int offset) {
		return (0xff & b[offset]) << 24 | (0xff & b[offset + 1]) << 16
				| (0xff & b[offset + 2]) << 8 | (0xff & b[offset + 3]);
	}

	public static long byte8ToLong(byte[] b, int offset) {
		return ((long) (0xff & b[offset])) << 56
				| ((long) (0xff & b[offset + 1])) << 48
				| ((long) (0xff & b[offset + 2])) << 40
				| ((long) (0xff & b[offset + 3])) << 32
				| (0xff & b[offset + 4]) << 24 | (0xff & b[offset + 5]) << 16
				| (0xff & b[offset + 6]) << 8 | (0xff & b[offset + 7]);
	}

	/**
	 * convert a integer to 4 bytes
	 * 
	 * @param n
	 *            the integer want to be converted to bytes
	 * @return byte array sorted from height to low, the size is 4
	 */
	public static byte[] integerToByte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}

}