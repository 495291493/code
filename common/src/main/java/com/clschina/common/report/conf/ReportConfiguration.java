package com.clschina.common.report.conf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.report.CellDataConverterInterface;
import com.clschina.common.report.ExcelTableHead;
import com.clschina.common.report.RowClassInterface;

/**
 * 报表配置文件类
 * @author yjt
 *
 */
public class ReportConfiguration {
	private static Log log = LogFactory.getLog(ReportConfiguration.class);
	
	public static final int POI = 1;
	public static final int JXL = 2;

	private List<ReportColumn> columns;
	private List<String> rowClasses;	//HTML的行的className
	private String tableClass; 	//HTML table的class name
	
	private int excelStartRow = 0;
	private CellDataConverterInterface converter;
	private RowClassInterface rowClass;
	/**
	 * 自己设置导出Excel中的表头
	 */
	private ExcelTableHead excelTableHead;

	private boolean htmlRowNum = false; // HTML需要显示行号
	private boolean excelRowNum = false; 	//Excel需要显示行号	
	
	private int[] columnsMergedWithPrevious;
	
	private String excelTempletFile;
	
	/**
	 * 生成Excel的api
	 */
	private int excelAPI = POI;
	
	public List<ReportColumn> getColumns(){
		return columns;
	}

	public void addColumn(ReportColumn col){
		if(log.isTraceEnabled()){
			log.trace("Adding column configuration. " + col);
		}
		if(columns == null){
			columns = new ArrayList<ReportColumn>();
		}
		columns.add(col);
	}

	/**
	 * @return the rowClasses
	 */
	public List<String> getRowClasses() {
		return rowClasses;
	}

	/**
	 * @param rowClasses the rowClasses to set
	 */
	public void setRowClasses(List<String> rowClasses) {
		this.rowClasses = rowClasses;
	}

	/**
	 * @return the tableClass
	 */
	public String getTableClass() {
		return tableClass;
	}

	/**
	 * @param tableClass the tableClass to set
	 */
	public void setTableClass(String tableClass) {
		this.tableClass = tableClass;
	}

	/**
	 * @return the excelStartRow
	 */
	public int getExcelStartRow() {
		return excelStartRow;
	}

	/**
	 * @param excelStartRow the excelStartRow to set
	 */
	public void setExcelStartRow(int excelStartRow) {
		this.excelStartRow = excelStartRow;
	}

	/**
	 * @return the converter
	 */
	public CellDataConverterInterface getConverter() {
		return converter;
	}

	/**
	 * @param converter the converter to set
	 */
	public void setConverter(CellDataConverterInterface converter) {
		this.converter = converter;
	}
	
	/**
	 * 自己设置导出Excel中的表头，get
	 */
	public ExcelTableHead getExcelTableHead() {
		return excelTableHead;
	}

	/**
	 * 自己设置导出Excel中的表头，set
	 */
	public void setExcelTableHead(ExcelTableHead excelTableHead) {
		this.excelTableHead = excelTableHead;
	}

	/**
	 * @return the htmlRowNum
	 */
	public boolean isHtmlRowNum() {
		return htmlRowNum;
	}

	/**
	 * @param htmlRowNum the htmlRowNum to set
	 */
	public void setHtmlRowNum(boolean htmlRowNum) {
		this.htmlRowNum = htmlRowNum;
	}

	/**
	 * @return the excelRowNum
	 */
	public boolean isExcelRowNum() {
		return excelRowNum;
	}

	/**
	 * @param excelRowNum the excelRowNum to set
	 */
	public void setExcelRowNum(boolean excelRowNum) {
		this.excelRowNum = excelRowNum;
	}

	/**
	 * @return the columnsMergedWithPrevious
	 */
	public int[] getColumnsMergedWithPrevious() {
		if(columnsMergedWithPrevious == null){
			columnsMergedWithPrevious = new int[0];
		}
		return columnsMergedWithPrevious;
	}

	/**
	 * @param columnsMergedWithPrevious the columnsMergedWithPrevious to set
	 */
	public void setColumnsMergedWithPrevious(int[] columnsMergedWithPrevious) {
		this.columnsMergedWithPrevious = columnsMergedWithPrevious;
	}

	/**
	 * @return the excelTemplet
	 */
	public String getExcelTempletFile() {
		return excelTempletFile;
	}

	/**
	 * @param excelTemplet the excelTemplet to set
	 */
	public void setExcelTempletFile(String excelTempletFile) {
		this.excelTempletFile = excelTempletFile;
	}

	/**
	 * @return the excelAPI
	 */
	public int getExcelAPI() {
		return excelAPI;
	}

	/**
	 * @param excelAPI the excelAPI to set
	 */
	public void setExcelAPI(int excelAPI) {
		this.excelAPI = excelAPI;
	}

	/**
	 * @return the rowClass
	 */
	public RowClassInterface getRowClass() {
		return rowClass;
	}

	/**
	 * @param rowClass the rowClass to set
	 */
	public void setRowClass(RowClassInterface rowClass) {
		this.rowClass = rowClass;
	}

}
