package com.clschina.common.jsf.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.jsf.servlet.CaptchaServlet;

/**
 * 用于验证，验证码输入是否正确
 * @author gexiangdong
 * 使用方法
 * <h:inputText id="yanzhengma" styleClass="miniinput" maxlength="4"  required="true"  >
 *		<f:validator validatorId="CaptchaValidator" />
 * </h:inputText>
 * <img src="${facesContext.externalContext.requestContextPath}/servlet/captha" id="yanzhengmaimg" />
 * <h:message for="yanzhengma" styleClass="errormessage"/>
 * TODO
 * v 1.0 有个BUG，当用户通过javascript把验证码的输入框设置为disabled后，程序会跳过输入吗验证器。
 *
 */
public class CaptchaValidator implements Validator {
	private static Log log = LogFactory.getLog(CaptchaValidator.class);

	public void validate(FacesContext context, UIComponent component, Object object)
			throws ValidatorException {
		String value = object.toString();
		String code = (String) context.getExternalContext().getSessionMap().get(CaptchaServlet.SESSION_CAPTCHA_CODE);
		if(log.isTraceEnabled()){
			log.trace("captcha code " + (code == null ? "NULL" : code) + "; user input " + (value == null ? "NULL" : value));
		}
		if(code == null || value == null || !code.equalsIgnoreCase(value)){
			//不正确
			throw new ValidatorException(new FacesMessage("验证码不正确。")); 
		}
	}

}
