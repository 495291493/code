package com.clschina.common.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.CommonUtil;


public class AddressTestCase extends TestCase {
	private static Log log = LogFactory.getLog(AddressTestCase.class);

	
	public void testAddressConfuse(){
		String addr1 = "上海市普陀区曹杨路1040弄1号中友大厦20楼易积通电子商务有限公司";
		String addr2 = CommonUtil.confuseAddress(addr1);
		log.trace(addr1 + " --> " + addr2);
		assertTrue(addr2.indexOf("1040") < 0);
		assertTrue(addr2.startsWith("上海市普陀区曹杨路*"));
	}
}
