package com.clschina.common.jsf.tag;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SelectManyCheckboxTag extends
		com.sun.faces.taglib.html_basic.SelectManyCheckboxTag {
	private static Log log = LogFactory.getLog(SelectManyCheckboxTag.class);
	
	private ValueExpression columns;

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(ValueExpression columns) {
		this.columns = columns;
	}
	
	public void setProperties(UIComponent component) {
		super.setProperties(component);
		if (component == null){
			return;
		}
		if(log.isTraceEnabled()){
			log.trace("columns=" + columns);
		}

		if (null != columns) {
			component.setValueExpression("columns", columns);
		}
	}
	
	public void release() {
		super.release();
		columns = null;
	}

	public String getRendererType() {
		return "com.clschina.common.jsf.renderer.SelectManyCheckboxListRenderer";
	}

	public String getComponentType() {
        return "javax.faces.HtmlSelectManyCheckbox";
	}

	
}
