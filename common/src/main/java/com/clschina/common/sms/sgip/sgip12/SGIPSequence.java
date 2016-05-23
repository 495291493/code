package com.clschina.common.sms.sgip.sgip12;

import java.util.Calendar;

public class SGIPSequence {

	private final static int MIN_SEQ = 0;
	private final static int MAX_SEQ = 0x7fffffff;

	private static int seq_index;
	private int seq_no2, seq_no3;

	/**
	 * construtct a new sequence
	 */
	private SGIPSequence(int no2, int no3) {
		this.seq_no2 = no2;
		this.seq_no3 = no3;
	}

	public int getSeq_no1() {
		return (int) Long.parseLong("3000091546");// 节点编号
	}

	public int getSeq_no2() {
		return seq_no2;
	}

	public int getSeq_no3() {
		return seq_no3;
	}

	/**
	 * create a new sequence Object
	 */
	public synchronized static SGIPSequence createSequence() {
		Calendar cal = Calendar.getInstance();
		int seq_date = (cal.get(Calendar.MONTH) + 1) * 0x5f5e100 + cal.get(Calendar.DAY_OF_MONTH) * 0xf4240
				+ cal.get(Calendar.HOUR_OF_DAY) * 10000 + cal.get(Calendar.MINUTE) * 100 + cal.get(Calendar.SECOND);
		if (seq_index == MAX_SEQ) {
			seq_index = MIN_SEQ;
		} else {
			seq_index++;
		}
		return new SGIPSequence(seq_date, seq_index);
	}

}