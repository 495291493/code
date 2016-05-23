package com.clschina.common.jsf.converter;

import java.io.InputStream;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clschina.common.util.DomUtil;


public class HTMLEditorUploadConverter extends ReplacementConverter {
	private static Log log = LogFactory.getLog(HTMLEditorUploadConverter.class);

	public HTMLEditorUploadConverter() throws Exception{
		if(log.isTraceEnabled()){
			log.trace("HTMLEditorUploadConverter ...");
		}

		InputStream is = this.getClass().getResourceAsStream("/config.properties");
		Properties conf = new Properties();
		conf.load(is);
		String source = conf.getProperty("HTMLEditorBase");
		String target = conf.getProperty("HTMLEditorFull");
		setSources(new String[]{source});
		setTargets(new String[]{target});
		if(log.isTraceEnabled()){
			log.trace("HTMLEditorUploadConverter:  " + source + "   -->  " + target);
		}

	}
}
