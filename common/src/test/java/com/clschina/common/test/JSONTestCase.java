package com.clschina.common.test;

import junit.framework.TestCase;

import org.json.JSONObject;


public class JSONTestCase extends TestCase {

	public void testJSONObject() throws Exception{
		String s = "{\"weatherinfo\":{\"city\":\"上海\",\"cityid\":\"101020100\",\"temp\":\"33\",\"WD\":\"东南风\",\"WS\":\"2级\",\"SD\":\"51%\",\"WSE\":\"2\",\"time\":\"11:00\",\"isRadar\":\"1\",\"Radar\":\"JC_RADAR_AZ9210_JB\"}}";
		JSONObject o1 = new JSONObject(s);
		assertEquals("101020100", o1.getJSONObject("weatherinfo").getString("cityid"));
		assertEquals(33, o1.getJSONObject("weatherinfo").getInt("temp"));
	}
}
