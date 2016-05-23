package com.clschina.common.jsf.tag;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletException;
import javax.faces.webapp.UIComponentELTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.jsf.component.UIFileUpload;
import com.sun.faces.util.MessageUtils;

public class FileUploadTag extends UIComponentELTag {
	private static Log log = LogFactory.getLog(FileUploadTag.class);
	
	private ValueExpression rendered;
	private ValueExpression value;
	//private ValueExpression required;
	private ValueExpression required;
	private ValueExpression styleClass;

	public FileUploadTag(){
		if(log.isTraceEnabled()){
			log.trace("creating FileUploadTag.");
		}
	}
	
    
    public void setRequired(ValueExpression required) {
        this.required = required;
    }

	/**
	 * CSS Class
	 * @param newValue
	 */
	public void setStyleClass(ValueExpression newValue) {
		styleClass = newValue;
	}


	/**
	 * @param rendered the rendered to set
	 */
	public void setRendered(ValueExpression rendered) {
		this.rendered = rendered;
	}



	/**
	 * @param value the value to set
	 */
	public void setValue(ValueExpression value) {
		this.value = value;
	}



	/**
	 * @param required the required to set
	 */
	//public void setRequired(ValueExpression required) {
	//	this.required = required;
	//}



	public void setProperties(UIComponent component) {
		super.setProperties(component);
		if (component == null){
			return;
		}
		if(!(component instanceof UIFileUpload)){
			throw new FaceletException("Invalid setting, must be UIFileUpload component.");
		}
		UIFileUpload upload = (UIFileUpload) component;
		
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
		if(null != rendered){
			if (rendered.isLiteralText()) {
				Object params[] = { rendered };
				throw new javax.faces.FacesException(MessageUtils
						.getExceptionMessageString(
								MessageUtils.INVALID_EXPRESSION_ID, params));
			} else {
				if("false".equals(rendered.getExpressionString())){
					upload.setRendered(false);
				}
			}
		}
        if (required != null) {
        	//component.setValueExpression("required", required);
//        	if("true".equalsIgnoreCase(required.getExpressionString())){
//				upload.setRendered(true);
//			}
        	upload.setValueExpression("required", required);
        	if(log.isTraceEnabled()){
        		log.trace("required=" + required + "; ");
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
		rendered = null;
		value = null;
		required = null;
		styleClass = null;
	}

	public String getRendererType() {
		return "com.clschina.common.jsf.renderer.FileUploadRenderer";
	}

	public String getComponentType() {
		return "com.clschina.common.jsf.component.FileUpload";
	}
}
