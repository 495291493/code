package com.clschina.common.jsf.tag;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.faces.util.MessageUtils;

public class Text2ImageTag extends UIComponentELTag{
	
	private static Log log = LogFactory.getLog(Text2ImageTag.class);
	
	private ValueExpression text;
	private ValueExpression width;
	private ValueExpression styleClass;
	
	public Text2ImageTag() {
		super();
		// TODO Auto-generated constructor stub
		if(log.isTraceEnabled()){
			log.trace("creating Text2ImageTage ");
		}
	}

	public void setText(ValueExpression text) {
		this.text = text;
	}

	public void setWidth(ValueExpression width) {
		this.width = width;
	}

	public void setStyleClass(ValueExpression styleClass) {
		this.styleClass = styleClass;
	}

	public void setProperties(UIComponent component) {
		super.setProperties(component);
		if (component == null){
			return;
		}
		if (null != text) {
			if (text.isLiteralText()) {
				Object params[] = { text };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("text", text);
			}
		}
		if (null != width) {
			if (width.isLiteralText()) {
				Object params[] = { width };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				component.setValueExpression("width", width);
			}
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
		
	}

	public void release() {
		super.release();
		text = null;
		width = null;
		styleClass = null;
	}

	
	@Override
	public String getComponentType() {
		// TODO Auto-generated method stub
		return "com.clschina.common.jsf.component.UIText2Image";
	}

	@Override
	public String getRendererType() {
		// TODO Auto-generated method stub
		return "com.clschina.common.jsf.renderer.Text2ImageRenderer";
	}

}
