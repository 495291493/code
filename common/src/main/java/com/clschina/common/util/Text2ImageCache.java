package com.clschina.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Text2ImageCache {
	
	private static Text2ImageCache tic = new Text2ImageCache();
	
	private Map<String,String> text2uuid = new HashMap<String,String>();
	
	private Map<String,UUID> uuid2text = new HashMap<String,UUID>();
	
	private Text2ImageCache(){
		
	}
	
	public static Text2ImageCache getInstance(){
		return tic;
	}

	public Map<String,String> getText2uuid() {
		return text2uuid;
	}

	public Map<String,UUID> getUuid2text() {
		return uuid2text;
	}
}
