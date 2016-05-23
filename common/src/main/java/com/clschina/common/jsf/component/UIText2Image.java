package com.clschina.common.jsf.component;

import javax.faces.component.UIOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UIText2Image extends UIOutput{
	private static Log log = LogFactory.getLog(UIText2Image.class);

	public UIText2Image() {
		super();
		// TODO Auto-generated constructor stub
		if(log.isTraceEnabled()){
			log.trace("creating UIText2Image.");
		}
	}
	
}
