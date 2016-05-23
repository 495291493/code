package com.clschina.common.jsf.renderer;

import java.io.IOException;

import java.util.Random;
import java.util.UUID;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.view.facelets.FaceletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.Text2ImageCache;

public class Text2ImageRenderer extends Renderer {

	private static Log log = LogFactory.getLog(Text2ImageRenderer.class);
	
	private static final int DefaultWidth = 100;

	public Text2ImageRenderer() {
		super();
		if (log.isTraceEnabled()) {
			log.trace("creating Text2ImageRenderer");
		}
	}

	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("encodeBegin with " + component.getClientId());
		}
		String id = component.getClientId(context);
		UIComponent parent = component;
		while (!(parent instanceof UIForm) && parent != null) {
			parent = parent.getParent();
		}
		if (parent == null) {
			throw new FaceletException(
					"text2image must be putted inside a form tag.");
		}
		String formId = parent.getClientId(context);

		ResponseWriter writer = context.getResponseWriter();

		String styleClass = (String) get(context, component, "styleClass");
		String text = (String) get(context, component, "text");
		String width = (String) get(context, component, "width");
		Random rnd = new Random();
		UUID uuid = null;
		if(text != null && !Text2ImageCache.getInstance().getText2uuid().containsValue(text)){
			uuid = UUID.randomUUID();
			Text2ImageCache.getInstance().getText2uuid().put(uuid.toString(), text);
			Text2ImageCache.getInstance().getUuid2text().put(text, uuid);
		}
		if(text != null && Text2ImageCache.getInstance().getText2uuid().containsValue(text)){
			uuid = Text2ImageCache.getInstance().getUuid2text().get(text);
		}
		if(text == null){
			text = "";
		}
		if(width == null){
			width = String.valueOf(DefaultWidth);
		}
		if (log.isTraceEnabled()) {
			log.trace("Text2ImageRenderer.encodeBegin(text=" + text
					+ ",width=" + width + ")");
		}
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, null);
		}
		writer.startElement("img", component);
		writer.writeAttribute("src", context.getExternalContext().getRequestContextPath()+"/servlet/textimage?id=" +
				uuid+"&width="+width+"&rnd="+rnd.nextInt(), null);
		writer.endElement("img");

	}

	private static Object get(FacesContext context, UIComponent component,
			String name) {
		ValueExpression binding = component.getValueExpression(name);
		if (binding != null) {
			return binding.getValue(context.getELContext());
		} else {
			return component.getAttributes().get(name);
		}
	}

}
