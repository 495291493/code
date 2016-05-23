package com.clschina.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 使用DOM方式解析xml文件常用的一些函数集合
 *
 */
public class DomUtil {

	private final static Log log = LogFactory.getLog(DomUtil.class);

	/**
	 * 读入一个xml文件,并返回root element
	 * @param fileName
	 * @return
	 */
	public static Element loadDocument(String fileName) {
		log.trace("load document from file " + fileName);
		return loadDocument(new File(fileName));
	}
	/**
	 * 读入一个xml文件,并返回root element
	 * @param file
	 * @return root element or null if error occus
	 */
	public static Element loadDocument(File file){
		try {
			return loadDocument(new FileInputStream(file), file.getPath());
		} catch (FileNotFoundException e) {
			log.error("XML file [" + file.getPath() + "] not found. " + e, e);
			return null;
		}
	}
	public static Element loadDocument(InputStream is){
		return loadDocument(is, is.getClass().getName());
	}
	private static Element loadDocument(InputStream is, String fileName) {
		Document doc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = docBuilderFactory.newDocumentBuilder();
			doc = parser.parse(is);
			Element root = doc.getDocumentElement();
			root.normalize();
			return root;
		} catch (SAXParseException err) {
			log.error("XML[" + fileName + "] Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			log.error("XML[" + fileName + "] error: " + err.getMessage(),
					err);
		} catch (SAXException e) {
			log.error("XML[" + fileName + "] error: " + e, e);
		} catch (java.net.MalformedURLException mfx) {
			log.error("XML[" + fileName + "] error: " + mfx, mfx);
		} catch (java.io.IOException e) {
			log.error("XML[" + fileName + "] error: " + e, e);
		} catch (Exception pce) {
			log.error("XML[" + fileName + "] error: " + pce, pce);
		}
		return null;
	}

	/**
	 * 取某个节点的某个属性的值
	 * 
	 * @param node
	 * @param paramName
	 * @return
	 */
	public static String getNodeAttribute(Node node, String paramName) {
		try {
			Node attr = node.getAttributes().getNamedItem(paramName);
			if (attr.getNodeValue() != null) {
				return attr.getNodeValue();
			} else {
				return null;
			}

		} catch (Exception e) {
			log.debug("error while get '" + paramName + "' attribute from node "
					+ node);
			return null;
		}
	}

	/**
	 * 查找某个节点，并取值
	 * 
	 * @param root
	 * @param tagName
	 * @return
	 */
	public static String findTagValue(Element root, String tagName) {
		String returnString = "";
		NodeList list = root.getElementsByTagName(tagName);
		for (int loop = 0; loop < list.getLength(); loop++) {
			Node node = list.item(loop);
			if (node != null) {
				Node child = node.getFirstChild();
				if ((child != null) && child.getNodeValue() != null)
					return child.getNodeValue();
			}
		}
		return returnString;
	}

	/**
	 * 取某个节点的值
	 * 
	 * @param node
	 * @return
	 */
	public static String getNodeValue(Node node) {
		Node child = node.getFirstChild();
		if ((child != null) && child.getNodeValue() != null) {
			return child.getNodeValue();
		}
		return null;
	}

	/**
	 * 取某node的某个子节点的某个属性
	 * @param root
	 * @param tagName
	 * @param subTagName
	 * @param attribute
	 * @return
	 */
	public static String getSubTagAttribute(Element root, String tagName,
			String subTagName, String attribute) {
		String returnString = "";
		NodeList list = root.getElementsByTagName(tagName);
		for (int loop = 0; loop < list.getLength(); loop++) {
			Node node = list.item(loop);
			if (node != null) {
				NodeList children = node.getChildNodes();
				for (int innerLoop = 0; innerLoop < children.getLength(); innerLoop++) {
					Node child = children.item(innerLoop);
					if ((child != null) && (child.getNodeName() != null)
							&& child.getNodeName().equals(subTagName)) {
						if (child instanceof Element) {
							return ((Element) child).getAttribute(attribute);
						}
					}
				} // end inner loop
			}
		}
		return returnString;
	}

	/**
	 * 取某node的某个子节点的值
	 * @param root
	 * @param tagName
	 * @param subTagName
	 * @return
	 */
	public static String getSubTagValue(Element root, String tagName,
			String subTagName) {
		String returnString = "";
		NodeList list = root.getElementsByTagName(tagName);
		for (int loop = 0; loop < list.getLength(); loop++) {
			Node node = list.item(loop);
			if (node != null) {
				NodeList children = node.getChildNodes();
				for (int innerLoop = 0; innerLoop < children.getLength(); innerLoop++) {
					Node child = children.item(innerLoop);
					if ((child != null) && (child.getNodeName() != null)
							&& child.getNodeName().equals(subTagName)) {
						Node grandChild = child.getFirstChild();
						if (grandChild.getNodeValue() != null)
							return grandChild.getNodeValue();
					}
				} // end inner loop
			}
		}
		return returnString;
	}
}
