package com.clschina.common.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.faces.renderkit.html_basic.TableRenderer;


public class DataTableRenderer extends TableRenderer {
	private static Log log = LogFactory.getLog(DataTableRenderer.class);

	
    @Override
    public void encodeChildren(FacesContext context, UIComponent component)
          throws IOException {
    	if(log.isTraceEnabled()){
    		log.trace("DataTableRender.encodeChildren()");
    	}
		ResponseWriter writer = context.getResponseWriter();
    	UIData data = (UIData) component;
        TableMetaInfo info = getMetaInfo(context, data);
        if(log.isTraceEnabled()){
        	log.trace("rowcount=" + data.getRowCount() + "; columncount=" + info.columnCount + "; columns=" + info.columns);
        }
        
        if(data.getRowCount() == 0 && info.hasHeaderFacets) {
    		writer.startElement("tbody", data);
			writer.startElement("tr", data);
			writer.startElement("td", data);
			writer.writeAttribute("class", "nodata", null);
			writer.writeAttribute("colspan", String.valueOf(String.valueOf(info.columns.size())), null);
			writer.write("没有查询到数据。");
			writer.endElement("td");
    		writer.endElement("tr");
    		writer.endElement("tbody");
        	return;
        }
        super.encodeChildren(context, component);
    }


	/* (non-Javadoc)
	 * @see com.sun.faces.renderkit.html_basic.TableRenderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
	 */
	@Override
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
    	UIData data = (UIData) component;
    	int first = data.getFirst();
    	int itemCount = data.getRowCount();
    	int rows = data.getRows();
		if(first >= itemCount){
			int newFirst = 0;
			if(rows > 0){
				if(itemCount % rows > 0){
					newFirst = itemCount - (itemCount % rows);
				}else{
					newFirst = itemCount - rows;
				}
			}
			if(newFirst < 0){
				newFirst = 0;
			}
			if(log.isTraceEnabled()){
				log.trace("first row=" + first + "; itemCount=" + itemCount + "; change first to " + newFirst);
			}
			data.setFirst(newFirst);
		}
		super.encodeBegin(context, component);
	}

}
