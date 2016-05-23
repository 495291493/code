package com.clschina.common.sms.cmpp.cmpp20;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CMPPSequence {
	private static Log log = LogFactory.getLog(CMPPSequence.class);
	private final static int MIN_SEQ = 0;
	private final static int MAX_SEQ = 0x7fffffff;

	private static int seq_index;

	static {
		//增加初始值
		Random random = new Random();
		seq_index = random.nextInt();
		while (seq_index <= MIN_SEQ || seq_index >= MAX_SEQ) {
			seq_index = random.nextInt();
		}
		if(log.isInfoEnabled()){
			log.info("sequence start from " + seq_index);
		}
	}

	public static synchronized int createSequence() {
		if (seq_index == MAX_SEQ) {
			seq_index = MIN_SEQ;
		}

		return ++seq_index; // sequence increment step one
	}
}