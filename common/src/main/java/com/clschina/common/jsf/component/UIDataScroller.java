package com.clschina.common.jsf.component;

import javax.faces.component.UIOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UIDataScroller extends UIOutput {
	private static Log log = LogFactory.getLog(UIDataScroller.class);
	private boolean showJump;
	
	public UIDataScroller(){
		if(log.isTraceEnabled()){
			log.trace("creating DataScroller.");
		}
	}

	/**
	 * @return the showJump
	 */
	public boolean isShowJump() {
		return showJump;
	}

	/**
	 * @param showJump the showJump to set
	 */
	public void setShowJump(boolean showJump) {
		this.showJump = showJump;
	}


}
