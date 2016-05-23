package com.clschina.common.jsf.renderer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.view.facelets.FaceletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.jsf.component.UIDataScroller;
import com.clschina.common.util.CommonUtil;

public class DataScrollerRenderer extends Renderer {
	private static Log log = LogFactory.getLog(DataScrollerRenderer.class);

	public DataScrollerRenderer() {
		if (log.isTraceEnabled()) {
			log.trace("creating DataScrollerRenderer.");
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
					"dataScroller must be putted inside a form tag.");
		}
		String formId = parent.getClientId(context);

		ResponseWriter writer = context.getResponseWriter();

		String styleClass = (String) get(context, component, "styleClass");
		String dataTableId = (String) get(context, component, "for");
		if (dataTableId == null) {
			throw new FaceletException("attribute 'for' [Id of the DataTable component] is required");
		}
		int maxPages;
		try {
			String p = get(context, component, "maxPages").toString();
			maxPages = Integer.parseInt(p);
		} catch (Exception e) {
			maxPages = 0;
		}

		// find the component with the given ID

		UIData data = (UIData) findComponent(context.getViewRoot(), getId(
				dataTableId, id), context);

		int first = data.getFirst();
		int itemcount = data.getRowCount();
		int pagesize = data.getRows();
		if (pagesize <= 0) {
			pagesize = itemcount;
		}
		if(pagesize == 0){
		    pagesize = 100;
		}

		int pages = 0;
		if(pagesize > 0){
			pages = itemcount / pagesize;
		}
		if (itemcount % pagesize != 0) {
			pages++;
		}
		int currentPage = 0;
		if(pagesize > 0){
			currentPage = first / pagesize;
		}
		if (first >= itemcount - pagesize) {
			currentPage = pages - 1;
		}
		
		if(log.isTraceEnabled()){
			log.trace("DataScrollerRenderer.encodeBegin(pages=" +  pages 
					+ ",itemcount=" + itemcount + ",pagesize=" + pagesize 
					+ ",first=" + first + ")");
		}
		if (pages <= 1) {
			String s = (String) get(context, component, "renderIfSinglePage");
			if (s == null || "f".equalsIgnoreCase(s)
					|| "false".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s)
					|| "no".equalsIgnoreCase(s) || "0".equalsIgnoreCase(s)) {
				return;
			} else {
				// renderIfSinglePage == true
			}
		}
		int startPage = 0;
		int endPage = pages;
		if (maxPages > 0) {
			startPage = Math.max(currentPage - maxPages / 2, 0); //(currentPage / maxPages) * maxPages;
			endPage = Math.min(startPage + maxPages, pages);
			if(endPage == pages ){
				startPage = endPage - maxPages;
				if(startPage < 0){
					startPage = 0;
				}
			}
		}

		writer.startElement("div", component);
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, null);
		}
		writer.startElement("div", component);
		writer.writeAttribute("class", "links", null);
		//if (startPage > 0) {
		//第一页
			//writeLink(writer, component, formId, id, "0", "", "jump firstpage");
		//}
		if (currentPage > 0) {
			//上一页
			writeLink(writer, component, formId, id, String.valueOf(currentPage), "", "jump previous");
		}else{
			writeLink(writer, component, formId, id, "", "", "jump previousdisabled");
		}
		
		if(startPage != 0){
			writeLink(writer, component, formId, id, "1", "1",
					0 == currentPage ? "pagenum selected" : "pagenum");
		}
		if(startPage > 1){
			writer.write("<span class='omit'>...</span>");
		}
		
		for (int i = startPage; i < endPage; i++) {
			writeLink(writer, component, formId, id, "" + (i + 1), "" + (i + 1),
					i == currentPage ? "pagenum selected" : "pagenum");
		}
		if(endPage < (pages - 1)){
			writer.write("<span class='omit'>...</span>");
		}
		if(endPage != pages){
			writeLink(writer, component, formId, id, "" + pages, "" + pages,
					pages == currentPage ? "pagenum selected" : "pagenum");
		}

		if (first < itemcount - pagesize) {
			//下一页
			writeLink(writer, component, formId, id, String.valueOf(currentPage + 2), "", "jump next");
		}else{
			writeLink(writer, component, formId, id, "", "", "jump nextdisabled");
		}
		//if (endPage < pages) {
			//最后一页
			//writeLink(writer, component, formId, id, "" + (pages), "", "jump lastpage");
		//}
		// hidden field to hold result
		writeHiddenField(writer, component, id, ((UIDataScroller) component).getValue());
		
		
		//跳转到第___页
		boolean showJump = ((UIDataScroller) component).isShowJump();
		if(showJump){
			String scriptNameAdd = CommonUtil.replace(id, ":", "_");
			scriptNameAdd = CommonUtil.replace(scriptNameAdd, " ", "");
			writer.startElement("script", component);
			writer.writeAttribute("type", "text/javascript", null);
			//writer.startCDATA();
			writer.write("\r\nfunction keydownon_" + scriptNameAdd + "(e, ele){\r\n");
			writer.write("var keyNum;");
			writer.write("var keyChar;");
			writer.write("if(window.event){");
			writer.write("keyNum = e.keyCode;");
			writer.write("}else if(e.which){");
			writer.write("keyNum = e.which;");
			writer.write("}");
			writer.write("if(keyNum == 13){");
			writer.write("submit_" + scriptNameAdd + "(ele.form);");
			writer.write("return true;");
			writer.write("}else{");
			writer.write("return true;");
			writer.write("}");
			writer.write("}\r\n");
			
			writer.write("\r\nfunction submit_" + scriptNameAdd + "(frm){\r\n");
			writer.write("var e = frm.elements['" + id + "_inputnum'];");
			writer.write("var v = e.value;");
			writer.write("if(v=='' || isNaN(v) || v == '0' || v > " + pages + "){");
			writer.write("e.focus();");
			writer.write("e.select();");
			writer.write("alert(\"输入的页数不是合法数字。\");");
			writer.write("return false;");
			writer.write("}else{");
			writer.write("frm.elements['" + id + "'].value=v;");
			writer.write("frm.submit();");
			writer.write("return true;");
			writer.write("}");
			
			writer.write("}\r\n");
			//writer.endCDATA();
			writer.endElement("script");
			writer.startElement("span", component);
			writer.writeAttribute("class", "pagingjump", null);
			writer.write("转到第");
			writer.startElement("input", component);
			writer.writeAttribute("id", id + "_inputnum", null);
			writer.writeAttribute("name", id + "_inputnum", null);
			writer.writeAttribute("type", "text", null);
			writer.writeAttribute("size", "4", null);
			writer.writeAttribute("class", "pagingjumpinput", null);
			writer.writeAttribute("onkeydown", "return keydownon_" + scriptNameAdd + "(event, this);", null); //enterkey
			writer.endElement("input");
			writer.write("页");
			writer.endElement("span");
			writer.startElement("input", component);
			writer.writeAttribute("type", "button", null);
			writer.writeAttribute("class", "pagingjumpok", null);
			writer.writeAttribute("onclick", "submit_" + scriptNameAdd + "(this.form);", null);
			writer.writeAttribute("value", "确定", null);
			writer.endElement("input");
		}
		writer.endElement("div");
		
		writer.startElement("div", component);
		writer.writeAttribute("class", "info", null);
		String summaryFormat = (String) get(context, component, "summaryFormat");
		if(summaryFormat == null){
			summaryFormat = "共{1,number,#,##0}页，{2,number,#,##0}条记录。";
		}
		MessageFormat mf = new MessageFormat(summaryFormat);
		//DecimalFormat format = new DecimalFormat("#,##0");
		//writer.write("共" + format.format(pages) + "页，" + format.format(itemcount) + "条记录。");
		//四个参数依次是： 0当前页、1总页数、2总记录数、3每页显示记录数
		writer.write(mf.format(new Object[]{currentPage, pages, itemcount, pagesize}));
		writer.write("<!-- first=" + data.getFirst() + "; rows=" + data.getRows() + " -->");
		writer.endElement("div");
		writer.endElement("div");
	}

	private void writeLink(ResponseWriter writer, UIComponent component,
			String formId, String id, String value, String text, String styleClass)
			throws IOException {
		writer.startElement("a", component);
		writer.writeAttribute("href", "javascript:;", null);
		writer.writeAttribute("onclick", onclickCode(formId, id, value), null);
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}
		if(text == null || text.length() == 0){
			writer.write("&nbsp;");
		}else{
			writer.writeText(text, null);
		}
		
		writer.endElement("a");
	}

	private String onclickCode(String formId, String id, String value) {
		StringBuffer buffer = new StringBuffer();
		if(value.length() > 0){
			buffer.append("document.forms[");
			buffer.append("'");
			buffer.append(formId);
			buffer.append("'");
			buffer.append("]['");
			buffer.append(id);
			buffer.append("'].value='");
			buffer.append(value);
			buffer.append("';");
			buffer.append(" document.forms[");
			buffer.append("'");
			buffer.append(formId);
			buffer.append("'");
			buffer.append("].submit()");
			buffer.append("; return false;");
		}else{
			buffer.append("return false;");
		}
		return buffer.toString();
	}

	private void writeHiddenField(ResponseWriter writer, UIComponent component,
			String id, Object value) throws IOException {
		writer.startElement("input", component);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("name", id, null);
		if(value != null){
			//writer.writeAttribute("value", value, null);
		}
		if(log.isTraceEnabled()){
			log.trace("PAGE VALUE: " + value);
		}
		writer.endElement("input");
	}

	public void decode(FacesContext context, UIComponent component) {
		if(log.isTraceEnabled()){
			log.trace("DataScrollerRenderer.decode()");
		}
		String id = component.getClientId(context);
		Map<?, ?> parameters = context.getExternalContext()
				.getRequestParameterMap();
		String response = (String) parameters.get(id);
		if(response == null){
			if(log.isTraceEnabled()){
				log.trace("DataScrollerRender.decode() cannot found value " + id + "");
			}
			return;
		}
		String dataTableId = (String) get(context, component, "for");
		int maxPages;
		try {
			String p = get(context, component, "maxPages").toString();
			maxPages = Integer.parseInt(p);
		} catch (Exception e) {
			maxPages = 0;
		}

		UIData data = (UIData) findComponent(context.getViewRoot(), getId(
				dataTableId, id), context);

		int first = data.getFirst();
		int itemcount = data.getRowCount();
		int pagesize = data.getRows();
		if (pagesize <= 0) {
			pagesize = itemcount;
		}
		//first = 0;
		if(log.isTraceEnabled()){
			log.trace("DataScrollerRenderer decode(before set first) first=" + first);
		}		
		if (response.equals("<")) {
			first -= pagesize;
		} else if (response.equals(">")) {
			first += pagesize;
			if(first > itemcount){
				first -= pagesize;
			}
		} else if (response.equals("<<")) {
			first -= pagesize * maxPages;
		} else if (response.equals(">>")) {
			first += pagesize * maxPages;
		} else {
			try{
				int page = Integer.parseInt(response);
				first = (page - 1) * pagesize;
			}catch(NumberFormatException e){
			}
			
		}
		if (first > itemcount) {
			if(log.isTraceEnabled()){
				log.trace("FIRST=" + first + "; itemCount=" + itemcount);
			}
			if(itemcount % pagesize == 0){
				first = itemcount - pagesize;
			}else{
				first = itemcount - (itemcount % pagesize);
			}
			if(log.isTraceEnabled()){
				log.trace("FIRST=" + first + "; itemCount=" + itemcount);
			}
		}
		if (first < 0) {
			first = 0;
		}
		data.setFirst(first);
		if(log.isTraceEnabled()){
			log.trace("DataScrollerRenderer decode set first to " + first);
		}
		try{
			if(data.getValueExpression("first") != null){
				data.getValueExpression("first").setValue(context.getELContext(), first);
			}
		}catch(Exception e){
			if(log.isTraceEnabled()){
				log.trace("error while setting back first value ", e);
			}
		}
		//((UIDataScroller) component).setSubmittedValue(first);
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

	private static UIComponent findComponent(UIComponent component, String id,
			FacesContext context) {
		String componentId = component.getClientId(context);
		if (componentId.equals(id)) {
			return component;
		}
		Iterator<UIComponent> kids = component.getChildren().iterator();
		while (kids.hasNext()) {
			UIComponent kid = kids.next();
			UIComponent found = findComponent(kid, id, context);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static String getId(String id, String baseId) {
		String separator = ""
				+ UINamingContainer.getSeparatorChar(FacesContext
						.getCurrentInstance());
		String[] idSplit = id.split(separator);
		String[] baseIdSplit = baseId.split(separator);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < baseIdSplit.length - idSplit.length; i++) {
			buffer.append(baseIdSplit[i]);
			buffer.append(separator);
		}
		buffer.append(id);
		return buffer.toString();
	}
}
