package com.clschina.common.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.report.BaseReport;
import com.clschina.common.report.CellDataConverterInterface;
import com.clschina.common.report.SQLReport;
import com.clschina.common.report.conf.ReportColumn;
import com.clschina.common.report.conf.ReportConfiguration;
import com.clschina.common.report.conf.SQLXMLReportConfiguration;
import com.clschina.common.report.conf.XMLReportConfiguration;


public class ReportTestCase extends TestCase {
	private static Log log = LogFactory.getLog(ReportTestCase.class);
	
	private HashMap<String, String> idNamePaire;
	
	public ReportTestCase(){
		idNamePaire = new HashMap<String, String>();
		String tiangan = "甲乙丙丁戊己庚辛壬癸";
		String dizhi = "子丑寅卯辰巳午未申酉戌亥";
		for(int i=0; i<tiangan.length(); i++){
			for(int j=0; j<dizhi.length(); j++){
				int l = i * tiangan.length() + j;
				String k = String.valueOf(l);
				String v = String.valueOf(tiangan.charAt(l % tiangan.length())) + String.valueOf(dizhi.charAt(l % dizhi.length()));
				idNamePaire.put(k, v);
			}
		}
	}
	
	public void testReport() throws Exception{
		if(log.isTraceEnabled()){
			log.trace("testReport");
		}
		List<Object[]> list = getData();

		ReportConfiguration cfg = getConfiguration();
		//cfg.setExcelAPI(ReportConfiguration.JXL);
		doTest(list, cfg, "report.html", "report-jxl.xls");
	}
	public void testXMLReport() throws Exception{
		if(log.isTraceEnabled()){
			log.trace("testXMLReport");
		}
		List<Object[]> list = getData();
		ReportConfiguration cfg = new XMLReportConfiguration(new File("src/test/resources/sample.reportconfig.xml"));
		doTest(list, cfg, "report-xml.html", "report-xml.xls");

	}
	
	public void testSQLXML() throws Exception{
		InputStream is = this.getClass().getResourceAsStream("/jsjc-report-config.xml");
		SQLReport r = new SQLReport(is);
		SQLXMLReportConfiguration conf = (SQLXMLReportConfiguration) r.getConfiguration();
		assertTrue(conf.getSql().length() > 5);
		assertTrue(conf.getExcelAPI() == ReportConfiguration.JXL);
	}
	private void doTest(List<Object[]> list, ReportConfiguration cfg, String htmlFileName, String excelFileName) throws Exception{
		BaseReport br = new BaseReport();
		br.setData(list);
		br.setConfiguration(cfg);
		File f = new File(".");
		System.out.println(f.getAbsolutePath());
		FileInputStream fis = new FileInputStream("src/test/resources/templet.xls");
		br.setExcelTempletInputStream(fis);
		
		File htmlFile = new File("target/" + htmlFileName);
		FileOutputStream excelOS = new FileOutputStream("target/" + excelFileName);
		BufferedWriter output = new BufferedWriter(new FileWriter(htmlFile));
		if(br.genHtmlTable() != null){
	    	output.write(br.genHtmlTable());
		}
		output.close();
		br.genExcel(excelOS);
		excelOS.close();
		fis.close();
	}
	
	private List<Object[]> getData(){
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		for(int i=0; i<10; i++){
			Object[] row = new Object[5];
			row[0] = "String " + i + ",0";
			row[1] = new Date();
			row[2] = i * 10 + 2;
			row[3] = i * 1.0 + 0.3;
			row[4] = String.valueOf(i);
			list.add(row);
		}
		return list;
	}
	private ReportConfiguration getConfiguration(){
		ReportConfiguration cfg = new ReportConfiguration();
		cfg.setConverter(getConverter());
		cfg.setExcelStartRow(2);
		for(int i=0; i<5; i++){
			ReportColumn col = new ReportColumn();
			if(i == 0){
				col.setLabel("Name");
			}else if(i == 1){
				col.setLabel("Date");
			}else if(i == 2){
				col.setLabel("Integer");
			}else if(i == 3){
				col.setLabel("Float");
				col.setSumOnFooter(true);
			}else if(i == 4){
				col.setLabel("Another String");
			}else{
				col.setLabel("UNKNOW");
			}
			col.setDataIndex(i);
			col.setExcelIndex(i + 1);
			col.setHtmlIndex(i);
			cfg.addColumn(col);
		}
		cfg.setHtmlRowNum(true);
		cfg.setExcelRowNum(true);
		return cfg;
	}
	
	private CellDataConverterInterface getConverter(){
		return new CellDataConverterInterface(){


			public String htmlCellConverter(Object data, int column, int row) {
				if(column == 4){
					if(idNamePaire.containsKey(data)){
						return idNamePaire.get(data);
					}
				}
				return data.toString();
			}

			@Override
			public Object excelCellConverter(Object data, int column, int row,
					Object[] rowData) {
				if(column == 4){
					if(idNamePaire.containsKey(data)){
						return idNamePaire.get(data);
					}
				}
				return data;
			}
			
		};
	}
}
