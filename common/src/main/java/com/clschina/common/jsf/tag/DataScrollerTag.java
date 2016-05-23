package com.clschina.common.jsf.tag;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.faces.util.MessageUtils;

public class DataScrollerTag extends UIComponentELTag {
	private static Log log = LogFactory.getLog(DataScrollerTag.class);
	
	private ValueExpression maxPages;
	private ValueExpression _for;
	private ValueExpression renderIfSinglePage;
	private ValueExpression styleClass;
	private ValueExpression summaryFormat;
	private ValueExpression value;
	private ValueExpression showJump;

	public DataScrollerTag(){
		if(log.isTraceEnabled()){
			log.trace("creating DataScrollerTag.");
		}
	}
	
	public void setFor(ValueExpression newValue) {
		_for = newValue;
	}
	
	public void setRenderIfSinglePage(ValueExpression newValue) {
		renderIfSinglePage = newValue;
	}
	public void setMaxPages(ValueExpression newValue) {
		maxPages = newValue;
	}
	public void setSummaryFormat(ValueExpression newValue){
		summaryFormat = newValue;
	}
	public void setShowJump(ValueExpression newValue) {
		showJump = newValue;
	}
	public void setStyleClass(ValueExpression newValue) {
		styleClass = newValue;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(ValueExpression value) {
		this.value = value;
	}



	public void setProperties(UIComponent component) {
		super.setProperties(component);
		if (component == null){
			return;
		}
		if(null != showJump){
			if (showJump.isLiteralText()) {
				Object params[] = { showJump };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("showJump", showJump);
			}
		}
		if (null != value) {
			if (value.isLiteralText()) {
				Object params[] = { value };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("value", value);
			}
		}
		if (null != maxPages) {
			if (maxPages.isLiteralText()) {
				Object params[] = { maxPages };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("maxPages", maxPages);
			}
		}
		if(null != renderIfSinglePage){
			if (renderIfSinglePage.isLiteralText()) {
				Object params[] = { renderIfSinglePage };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("renderIfSinglePage", renderIfSinglePage);
			}
		}
		if (null != _for) {
			component.setValueExpression("for", _for);
		}
		if (null != styleClass) {
			if (styleClass.isLiteralText()) {
				Object params[] = { styleClass };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("styleClass", styleClass);

			}
		}
		if(null != summaryFormat){
			if (summaryFormat.isLiteralText()) {
				Object params[] = { summaryFormat };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("summaryFormat", summaryFormat);

			}
		}
	}

	public void release() {
		super.release();
		maxPages = null;
		renderIfSinglePage = null;
		_for = null;
		styleClass = null;
	}

	public String getRendererType() {
		return "com.clschina.common.jsf.renderer.DataScrollerRenderer";
	}

	public String getComponentType() {
		return "com.clschina.common.jsf.component.UIDataScroller";
	}
}
