package com.clschina.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clschina.common.util.DomUtil;

public class XmlDomTestCase extends TestCase {
	private static Log log = LogFactory.getLog(XmlDomTestCase.class);

	public void testBlank(){
		
	}
	public void readXml() throws IOException{
		/*URL url = new URL("http://127.0.0.1:8080/bank/querypoints");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		PrintWriter out = new PrintWriter(connection
				.getOutputStream());
		String userId = "ge";
		String password = "123456";
		String data = "userid=" + URLEncoder.encode(userId, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
		out.print(data);
		out.close();
		InputStream is =  connection.getInputStream();
		
		Element root = DomUtil.loadDocument(is);	
		NodeList list = root.getChildNodes();
		String error = null;
		String errorMessage = null;
		int points = 0;
		for(int i=0; i<list.getLength(); i++){
			Node node = list.item(i);
			if("error".equals(node.getNodeName())){
				error = DomUtil.getNodeValue(node);
			}else if("errormessage".equals(node.getNodeName())){
				errorMessage = DomUtil.getNodeValue(node);
			}else if("userid".equals(node.getNodeName())){
				userId = DomUtil.getNodeValue(node);
			}else if("points".equals(node.getNodeName())){
				try{
					points = Integer.parseInt(DomUtil.getNodeValue(node).trim());
				}catch(Exception e){
					if(log.isWarnEnabled()){
						log.warn("Invalid points " + DomUtil.getNodeValue(node));
					}
				}
			}
		}
		if(log.isTraceEnabled()){
			log.trace("error=" + error);
			log.trace("errormessage=" + errorMessage);
			log.trace("userid=" + userId);
			log.trace("points=" + points);
		}
		is.close();*/
	}
}
