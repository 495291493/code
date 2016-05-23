package com.clschina.common.report.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clschina.common.report.CellDataConverterInterface;
import com.clschina.common.report.RowClassInterface;
import com.clschina.common.util.DomUtil;


/**
 * 从XML读取配置信息；XML例子参照src/test/resource/sample.reportconfig.xml
 *
 */
public class XMLReportConfiguration extends ReportConfiguration {
	private static Log log = LogFactory.getLog(XMLReportConfiguration.class);

	public XMLReportConfiguration(File xmlFile) throws IOException{
		this(new FileInputStream(xmlFile));
	}
	public XMLReportConfiguration(InputStream is) throws IOException{		
		if(log.isTraceEnabled()){
			log.trace("进入XMLReportConfiguration");
		}
		Element root=getRoot(is);
		if(root == null){
			return;
		}
		init(root);
	}
	/**
	 * 获取配置文件
	 * @param is
	 * @return
	 */
	private Element getRoot(InputStream is){
		Element root = DomUtil.loadDocument(is);
		return root;
	}
	/**
	 * 解析配置文件
	 * @param root
	 * @return
	 */
	protected void init(Element root){		
		NodeList nodeList = root.getElementsByTagName("cellDataConverterClass");
		if (nodeList != null && nodeList.getLength() > 0) {
			String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
			Object inst;
			try {
				inst = Class.forName(v).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(" 无法实例化" + v);
			}
			if (log.isTraceEnabled()) {
				log.trace(" converter " + inst.getClass().getName());
			}
			if (!(inst instanceof CellDataConverterInterface)) {
				throw new RuntimeException(" " + inst.getClass().getName()
						+ " 没有实现CellDataConverterInterface接口。");
			}
			CellDataConverterInterface fa = (CellDataConverterInterface) inst;
			setConverter(fa);
		}
		
		nodeList = root.getElementsByTagName("rowClassClass");
		if (nodeList.getLength() > 0) {
			String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
			Object inst;
			try {
				inst = Class.forName(v).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(" 无法实例化" + v);
			}
			if (log.isTraceEnabled()) {
				log.trace(" rowClassClass " + inst.getClass().getName());
			}
			if (!(inst instanceof RowClassInterface)) {
				throw new RuntimeException(" " + inst.getClass().getName()
						+ " 没有实现RowClassInterface接口。");
			}
			RowClassInterface rc = (RowClassInterface) inst;
			setRowClass(rc);
		}
		
		/*自己设置导出Excel中的表头
		nodeList = root.getElementsByTagName("excelTableHeadClass");
		if (nodeList != null && nodeList.getLength() > 0) {
			String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
			Object inst;
			try {
				inst = Class.forName(v).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(" 无法实例化" + v);
			}
			if (log.isTraceEnabled()) {
				log.trace(" converter " + inst.getClass().getName());
			}
			if (!(inst instanceof ExcelTableHead)) {
				throw new RuntimeException(" " + inst.getClass().getName()
						+ " 没有实现ExcelTableHeadInterface接口。");
			}
			ExcelTableHead et = (ExcelTableHead) inst;
			setExcelTableHead(et);
		}*/
		
		nodeList = root.getElementsByTagName("excelAPI");
		if (nodeList.getLength() > 0) {
	    	String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
	    	if(v.trim().equalsIgnoreCase("JXL")){
	    		super.setExcelAPI(ReportConfiguration.JXL);
	    	}
		}
		
		nodeList = root.getElementsByTagName("excelStartRow");
		if (nodeList.getLength() > 0) {
	    	String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
	    	setExcelStartRow(Integer.parseInt(v));
		}
		
		nodeList = root.getElementsByTagName("tableClass");
		if (nodeList.getLength() > 0) {
			String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
			setTableClass(v);
		}
		
		nodeList = root.getElementsByTagName("excelTempletFile");
		if (nodeList.getLength() > 0) {
			String v = DomUtil.getNodeValue(nodeList.item(0));
			this.setExcelTempletFile(v);
		}
	
		nodeList = root.getElementsByTagName("columnsmergedwithprevious");
		if(nodeList.getLength() > 0){
			String value = DomUtil.getNodeValue(nodeList.item(0)).trim();
			if(log.isTraceEnabled()){
				log.trace("columnsmergedwithprevious=" + value);
			}
			String[] ary = value.split(",");
			int[] cols = new int[ary.length];
			for(int i=0; i<cols.length; i++){
				cols[i] = Integer.parseInt(ary[i].trim());
			}
			this.setColumnsMergedWithPrevious(cols);
		}
		
		nodeList = root.getElementsByTagName("rowClasses");		
		if (nodeList.getLength() > 0) {
			String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
			if (v != null && v.length() > 0) {
				String[] r = v.split(",");
				List<String> rowClassesList = new ArrayList<String>();
				for (int i = 0; i < r.length; i++) {
					String s = r[i];
					rowClassesList.add(s);
				}
				setRowClasses(rowClassesList);
			}
		}
		
		nodeList = root.getElementsByTagName("htmlRowNum");
		if(nodeList.getLength() > 0){
			String v = DomUtil.getNodeValue(nodeList.item(0)).trim();
			if(v.equals("1") || v.equalsIgnoreCase("t") || v.equalsIgnoreCase("y") || v.equals("yes") || v.equals("true")){
				setHtmlRowNum(true);
			}else{
				setHtmlRowNum(false);
			}
		}
		
		nodeList = root.getElementsByTagName("excelRowNum");
		if (nodeList.getLength() > 0) {
			String e = DomUtil.getNodeValue(nodeList.item(0)).trim();
			if(e.equals("1") || e.equalsIgnoreCase("t") || e.equalsIgnoreCase("y") || e.equals("yes") || e.equals("true")){
				setExcelRowNum(true);
			}else{
				setExcelRowNum(false);
			}
		}
		
		nodeList = root.getElementsByTagName("columns");
		if (nodeList.getLength() > 0) {
			for (int loop = 0; loop < nodeList.getLength(); loop++) {
				Node node = nodeList.item(loop);
				NodeList columnNodes = node.getChildNodes();
				for (int j = 0; j < columnNodes.getLength(); j++) {
					Node lNode = columnNodes.item(j);
					if (lNode != null && lNode.getNodeName().equals("column")) {
						ReportColumn col = new ReportColumn();
						for (int i = 0; i < lNode.getAttributes().getLength(); i++) {
							Node n = lNode.getAttributes().item(i);
							String k = n.getNodeName();
							String v = n.getNodeValue();
							if (k == null || v == null) {
								continue;
							}
							if ("dataIndex".equalsIgnoreCase(k)) {
								col.setDataIndex(Integer.parseInt(v));
							} else if ("label".equalsIgnoreCase(k)) {
								col.setLabel(v);
							} else if ("multistageLabel".equalsIgnoreCase(k)) {//设置多级表头
								col.setMultistageLabel(v);
							}  else if ("htmlIndex".equalsIgnoreCase(k)) {
								col.setHtmlIndex(Integer.parseInt(v));
							} else if ("excelIndex".equalsIgnoreCase(k)) {
								col.setExcelIndex(Integer.parseInt(v));
							} else if ("headOnClick".equals(k)){
								col.setHeadOnClick(v);
							} else if ("datePattern".equals(k)){
								col.setDatePattern(v);
							} else if ("numberPattern".equals(k)){
								col.setNumberPattern(v);
							} else if ("styleClass".equals(k)) {
								col.setStyleClass(v);
							} else if ("sumOnFooter".equals(k)) {
								if (v.equals("1") || v.equalsIgnoreCase("t") || v.equalsIgnoreCase("y") || v.equals("yes") || v.equals("true")) {
									col.setSumOnFooter(true);
								} else {
									col.setSumOnFooter(false);
								}
							} else if ("avgOnFooter".equals(k)) {
								if (v.equals("1") || v.equalsIgnoreCase("t") || v.equalsIgnoreCase("y") || v.equals("yes") || v.equals("true")) {
									col.setAvgOnFooter(true);
								} else {
									col.setAvgOnFooter(false);
								}
							} else if ("titleDataIndex".equals(k)){
								col.setTitleDataIndex(Integer.parseInt(v));
							}
						}
						addColumn(col);
					}
				}
			}
		}
	}
}
