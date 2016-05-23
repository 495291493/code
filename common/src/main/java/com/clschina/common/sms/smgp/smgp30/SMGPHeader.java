package com.clschina.common.sms.smgp.smgp30;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class SMGPHeader implements Cloneable {
	
	public final static int LEN_SMGP_HEADER = 12;
	private int packet_length;
	private int request_id;
	private int sequence_id;

	public int getPacket_length() {
		return packet_length;
	}

	public void setPacket_length(int packet_length) {
		this.packet_length = packet_length;
	}

	public void setRequest_id(int request_id, boolean is_create_seq) {
		this.request_id = request_id;
		if (is_create_seq) {
			sequence_id = SMGPSequence.createSequence();
		}
	}

	public int getRequest_id() {
		return request_id;
	}

	public int getSequence_id() {
		return sequence_id;
	}

	public void setSequence_id(int sequence_id) {
		this.sequence_id = sequence_id;
	}

	public static int getLEN_SMGP_HEADER() {
		return LEN_SMGP_HEADER;
	}

	public byte[] getMsgHeader() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(LEN_SMGP_HEADER);
		DataOutputStream dos = new DataOutputStream(out);
		dos.writeInt(packet_length);
		dos.writeInt(request_id);
		dos.writeInt(sequence_id);

		return out.toByteArray();
	}

	public void readMsgHeader(InputStream in) throws IOException {
		byte[] buffer = new byte[LEN_SMGP_HEADER];
		// int actual_length = in.read(buffer);
		int actual_length = 0, read_bytes;
		// while(read_bytes > 0 && actual_length < LEN_SGIP_HEADER) {
		do {
			read_bytes = in.read(buffer, actual_length, LEN_SMGP_HEADER - actual_length);
			actual_length += read_bytes;
		} while (actual_length < LEN_SMGP_HEADER && read_bytes > 0);
		if (LEN_SMGP_HEADER != actual_length) {
			if (actual_length == -1) {
				throw new SocketException("get the end of the inputStream, maybe the connection is broken");
			} else {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < LEN_SMGP_HEADER; i++) {
					sb.append("head[").append(i).append("] = ").append(buffer[i] + ", ");
				}
				throw new IOException("can't get actual length of message header from the inputstream:" + actual_length);
			}
		}

		packet_length = SMGPMessage.byte4ToInteger(buffer, 0);
		request_id = SMGPMessage.byte4ToInteger(buffer, 4);
		sequence_id = SMGPMessage.byte4ToInteger(buffer, 8);

	}

	public void reCreateSeq() {
		sequence_id = SMGPSequence.createSequence();
	}

	protected Object clone() {
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
