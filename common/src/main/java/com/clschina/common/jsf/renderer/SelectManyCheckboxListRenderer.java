package com.clschina.common.jsf.renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.MenuRenderer;
import com.sun.faces.util.Util;
import com.sun.faces.util.RequestStateManager;

public class SelectManyCheckboxListRenderer extends MenuRenderer {
	private static Log log = LogFactory
			.getLog(SelectManyCheckboxListRenderer.class);

	private static final Attribute[] ATTRIBUTES = AttributeManager
			.getAttributes(AttributeManager.Key.SELECTMANYCHECKBOX);

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("encodeEnd of SelectManyCheckboxListRenderer");
		}
		rendererParamsNotNull(context, component);

		if (!shouldEncode(component)) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();
		assert (writer != null);

		Object borderObj;
		int border = 0;

		int columns = 1;
		try {
			columns = Integer.parseInt((String) component.getAttributes().get(
					"columns"));
		} catch (Exception e) {
			columns = 1;
		}
		if(columns < 1){
			columns = 1;
		}
		if (null != (borderObj = component.getAttributes().get("border"))) {
			border = (Integer) borderObj;
		}

		Converter converter = null;
		if (component instanceof ValueHolder) {
			converter = ((ValueHolder) component).getConverter();
		}

		renderBeginText(component, border, false, context, true);

		Iterator<SelectItem> items = RenderKitUtils.getSelectItems(context,
				component);

		Object currentSelections = getCurrentSelectedValues(component);
		Object[] submittedValues = getSubmittedSelectedValues(component);
		Map<String, Object> attributes = component.getAttributes();
		OptionComponentInfo optionInfo = new OptionComponentInfo(
				(String) attributes.get("disabledClass"), (String) attributes
						.get("enabledClass"), (String) attributes
						.get("unselectedClass"), (String) attributes
						.get("selectedClass"), Util
						.componentIsDisabled(component),
				isHideNoSelection(component));
		int idx = -1;
		while (items.hasNext()) {
			SelectItem curItem = items.next();
			// If we come across a group of options, render them as a nested
			// table.
			idx++;
			if (idx > 0 && idx % columns == 0) {
				writer.endElement("tr");
				writer.startElement("tr", component);
			}

			if (curItem instanceof SelectItemGroup) {
				// write out the label for the group.
				writer.startElement("td", component);
				if (curItem.getLabel() != null) {
					writer.writeText(curItem.getLabel(), component, "label");
				}

				renderBeginText(component, 0, true, context, false);
				// render options of this group.
				SelectItem[] itemsArray = ((SelectItemGroup) curItem)
						.getSelectItems();
				for (int i = 0; i < itemsArray.length; ++i) {
					renderOption(context, component, converter, itemsArray[i],
							currentSelections, submittedValues, true, i,
							optionInfo);
				}
				renderEndText(component, true, context);

				writer.endElement("td");
			} else {
				renderOption(context, component, converter, curItem,
						currentSelections, submittedValues, false, idx,
						optionInfo);
			}
		}
		while ((++idx) % columns != 0) {
			writer.startElement("td", component);
			writer.endElement("td");
		}
		writer.endElement("tr");
		renderEndText(component, false, context);

	}

	// ------------------------------------------------------- Protected Methods

	protected void renderBeginText(UIComponent component, int border,
			boolean alignVertical, FacesContext context, boolean outerTable)
			throws IOException {

		ResponseWriter writer = context.getResponseWriter();
		assert (writer != null);

		writer.startElement("table", component);
		if (border != Integer.MIN_VALUE) {
			writer.writeAttribute("border", border, "border");
		}

		// render style and styleclass attribute on the outer table instead of
		// rendering it as pass through attribute on every option in the list.
		if (outerTable) {
			// render "id" only for outerTable.
			if (shouldWriteIdAttribute(component)) {
				writeIdAttributeIfNecessary(context, writer, component);
			}
			String styleClass = (String) component.getAttributes().get(
					"styleClass");
			String style = (String) component.getAttributes().get("style");
			if (styleClass != null) {
				writer.writeAttribute("class", styleClass, "class");
			}
			if (style != null) {
				writer.writeAttribute("style", style, "style");
			}
		}
		writer.writeText("\n", component, null);

		if (!alignVertical) {
			writer.writeText("\t", component, null);
			writer.startElement("tr", component);
			writer.writeText("\n", component, null);
		}

	}

	protected void renderEndText(UIComponent component, boolean alignVertical,
			FacesContext context) throws IOException {

		ResponseWriter writer = context.getResponseWriter();
		assert (writer != null);

		if (!alignVertical) {
			writer.writeText("\t", component, null);
			writer.endElement("tr");
			writer.writeText("\n", component, null);
		}
		writer.endElement("table");

	}

	protected void renderOption(FacesContext context, UIComponent component,
			Converter converter, SelectItem curItem, Object currentSelections,
			Object[] submittedValues, boolean alignVertical, int itemNumber,
			OptionComponentInfo optionInfo) throws IOException {

		String valueString = getFormattedValue(context, component, curItem
				.getValue(), converter);

		Object valuesArray;
		Object itemValue;
		if (submittedValues != null) {
			valuesArray = submittedValues;
			itemValue = valueString;
		} else {
			valuesArray = currentSelections;
			itemValue = curItem.getValue();
		}
		if(log.isTraceEnabled()){
			if(valuesArray == null){
				log.trace("valuesArray=null");
			}else{
				if(valuesArray instanceof Object[]){
					Object[] ary = (Object[]) valuesArray;
					for(int i=0; i<ary.length; i++){
						log.trace("valuesArray[" + i + "]=/" + ary[i] + "/");
					}
				}else if(valuesArray instanceof List<?>){
					List<?> list = (List<?>) valuesArray;
					for(int i=0; i<list.size(); i++){
						log.trace("List valuesArray.get(" + i + ")=" + list.get(i));
					}
				}
			}
		}
		RequestStateManager.set(context,
				RequestStateManager.TARGET_COMPONENT_ATTRIBUTE_NAME, component);
		
		boolean isSelected = isSelected(context, component, itemValue,
				valuesArray, converter);
//		if(isSelected == false && valuesArray != null && itemValue != null){
//			if(valuesArray instanceof Object[]){
//				Object[] ary = (Object[]) valuesArray;
//				for(int i=0; i<ary.length; i++){
//					if(ary[i].toString().equals(itemValue.toString())){
//						isSelected = true;
//						break;
//					}
//				}
//			}
//		}
		if(log.isTraceEnabled()){
			log.trace("selected=" + isSelected + "  itemValue=/" + itemValue + "/; valueArray=" + valuesArray);
		}
		if (optionInfo.isHideNoSelection() && curItem.isNoSelectionOption()
				&& currentSelections != null && !isSelected) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();
		assert (writer != null);

		if (alignVertical) {
			writer.writeText("\t", component, null);
			writer.startElement("tr", component);
			writer.writeText("\n", component, null);
		}
		writer.startElement("td", component);
		writer.writeText("\n", component, null);

		writer.startElement("input", component);
		writer.writeAttribute("name", component.getClientId(context),
				"clientId");
		String idString = component.getClientId(context)
				+ UINamingContainer.getSeparatorChar(context)
				+ Integer.toString(itemNumber);
		writer.writeAttribute("id", idString, "id");

		writer.writeAttribute("value", valueString, "value");
		writer.writeAttribute("type", "checkbox", null);

		if (isSelected) {
			writer.writeAttribute(getSelectedTextString(), Boolean.TRUE, null);
		}

		// Don't render the disabled attribute twice if the 'parent'
		// component is already marked disabled.
		if (!optionInfo.isDisabled()) {
			if (curItem.isDisabled()) {
				writer.writeAttribute("disabled", true, "disabled");
			}
		}

		// Apply HTML 4.x attributes specified on UISelectMany component to all
		// items in the list except styleClass and style which are rendered as
		// attributes of outer most table.
		RenderKitUtils.renderPassThruAttributes(context, writer, component,
				ATTRIBUTES, getNonOnClickSelectBehaviors(component));

		RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

		RenderKitUtils.renderSelectOnclick(context, component, true);

		writer.endElement("input");
		writer.startElement("label", component);
		writer.writeAttribute("for", idString, "for");

		// Set up the label's class, if appropriate
		StringBuilder labelClass = new StringBuilder();
		String style;
		// If disabledClass or enabledClass set, add it to the label's class
		if (optionInfo.isDisabled() || curItem.isDisabled()) {
			style = optionInfo.getDisabledClass();
		} else { // enabled
			style = optionInfo.getEnabledClass();
		}
		if (style != null) {
			labelClass.append(style);
		}
		// If selectedClass or unselectedClass set, add it to the label's class
		if (isSelected(context, component, itemValue, valuesArray, converter)) {
			style = optionInfo.getSelectedClass();
		} else { // not selected
			style = optionInfo.getUnselectedClass();
		}
		if (style != null) {
			if (labelClass.length() > 0) {
				labelClass.append(' ');
			}
			labelClass.append(style);
		}
		writer.writeAttribute("class", labelClass.toString(), "labelClass");
		String itemLabel = curItem.getLabel();
		if (itemLabel == null) {
			itemLabel = valueString;
		}
		writer.writeText(" ", component, null);
		if (!curItem.isEscape()) {
			// It seems the ResponseWriter API should
			// have a writeText() with a boolean property
			// to determine if it content written should
			// be escaped or not.
			writer.write(itemLabel);
		} else {
			writer.writeText(itemLabel, component, "label");
		}
		if (isSelected(context, component, itemValue, valuesArray, converter)) {

		} else { // not selected

		}
		writer.endElement("label");
		writer.endElement("td");
		writer.writeText("\n", component, null);
		if (alignVertical) {
			writer.writeText("\t", component, null);
			writer.endElement("tr");
			writer.writeText("\n", component, null);
		}
	}

	// ------------------------------------------------- Package Private Methods

	String getSelectedTextString() {
		return "checked";

	}

} // end of class SelectManyCheckboxListRenderer
