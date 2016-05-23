package com.clschina.common.jsf.tag;

import com.sun.faces.taglib.html_basic.DataTableTag;


public class DataListTag extends DataTableTag {

	/* (non-Javadoc)
	 * @see com.sun.faces.taglib.html_basic.DataTableTag#getRendererType()
	 */
	@Override
	public String getRendererType() {
		return "com.clschina.common.jsf.renderer.DataListRenderer";
	}

}
