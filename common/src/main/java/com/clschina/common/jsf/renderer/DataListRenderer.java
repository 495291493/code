package com.clschina.common.jsf.renderer;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sun.faces.renderkit.html_basic.HtmlBasicRenderer;

public class DataListRenderer extends HtmlBasicRenderer {
	private static Log log = LogFactory.getLog(DataListRenderer.class);

	@Override
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("encodeBegin with " + component.getClientId());
		}

		UIData data = (UIData) component;
		data.setRowIndex(-1);

	}    
	@Override
    public void encodeEnd(FacesContext context, UIComponent component)
    		throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("encodeEnd with " + component.getClientId());
		}
	}


	@Override
	public void encodeChildren(FacesContext context, UIComponent component)
			throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("encodeChildren with " + component.getClientId());
		}

		UIData data = (UIData) component;

		// Iterate over the rows of data that are provided
		int processed = 0;
		int rowIndex = data.getFirst() - 1;
		int rows = data.getRows();

		while (true) {

			// Have we displayed the requested number of rows?
			if ((rows > 0) && (++processed > rows)) {
				break;
			}
			// Select the current row
			data.setRowIndex(++rowIndex);
			if (!data.isRowAvailable()) {
				break; // Scrolled past the last row
			}
			for (Iterator<UIComponent> kids = getChildren(data); kids.hasNext();) {
				encodeRecursive(context, kids.next());
			}

		}

		// Clean up after ourselves
		data.setRowIndex(-1);

	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	/**
	 * <p>
	 * Render nested child components by invoking the encode methods on those
	 * components, but only when the <code>rendered</code> property is
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param context
	 *            FacesContext for the current request
	 * @param component
	 *            the component to recursively encode
	 * 
	 * @throws IOException
	 *             if an error occurrs during the encode process
	 */
	protected void encodeRecursive(FacesContext context, UIComponent component)
			throws IOException {

		// suppress rendering if "rendered" property on the component is
		// false.
		if (!component.isRendered()) {
			return;
		}

		// Render this component and its children recursively
		component.encodeBegin(context);
		if (component.getRendersChildren()) {
			component.encodeChildren(context);
		} else {
			Iterator<UIComponent> kids = getChildren(component);
			while (kids.hasNext()) {
				UIComponent kid = kids.next();
				encodeRecursive(context, kid);
			}
		}
		component.encodeEnd(context);

	}

	/**
	 * @param component
	 *            <code>UIComponent</code> for which to extract children
	 * 
	 * @return an Iterator over the children of the specified component,
	 *         selecting only those that have a <code>rendered</code> property
	 *         of <code>true</code>.
	 */
	protected Iterator<UIComponent> getChildren(UIComponent component) {

		int childCount = component.getChildCount();
		if (childCount > 0) {
			return component.getChildren().iterator();
		} else {
			return Collections.<UIComponent> emptyList().iterator();
		}

	}

}
