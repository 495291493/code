package com.clschina.common.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
/**
 * Excel处理类
 * @author ch
 *
 */
public class ExcelUtil {
	private final static Log log = LogFactory.getLog(DomUtil.class);
	/**
	 *  从Excel中读取标题行 
	 * @param sheet 
	 * @param titleRow    标题行的开始行，如果是-1；表示没有标题行, 如果是1，则标题行前还存在一行
	 * @param dataStartRow     数据的开始行
	 * @param titleRowCount   标题行有几行。
	 */
	protected static  List<String>  retrieveHeaders(HSSFSheet sheet, int titleRow, int dataStartRow, int titleRowCount){
		if(log.isTraceEnabled()){
			log.trace("HSSFSheet："+sheet+"，titleRow："+titleRow+"，dataStartRow："+dataStartRow+"，titleRowCount："+ titleRowCount);
		}
		// Excel数据的标题
		 List<String> headers = new ArrayList<String>();
		if(titleRow == -1){//titleRow=-1则没有标题
			if(log.isTraceEnabled()){
				log.trace("没有设置titleRow");
			}
			Row row=sheet.getRow(dataStartRow);
			if(row == null){
				if(log.isTraceEnabled()){
					log.trace("dataStartRow不正确，读取不到header数据");
				}
				return null;
			}
			for(int i=0;i<row.getPhysicalNumberOfCells() && i < 26;i++){
				headers.add(String.valueOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(i)));
			}
		}else{
			ArrayList<CellRangeAddress> ranges = new ArrayList<CellRangeAddress>();
			for(int i=0; i<sheet.getNumMergedRegions(); i++){
				ranges.add(sheet.getMergedRegion(i));
			}
			HSSFRow[] rows = new HSSFRow[titleRowCount];
			for(int i=0; i<titleRowCount; i++){
				rows[i] = sheet.getRow(titleRow + i);
			}
			for (int i = rows[0].getFirstCellNum(); i < rows[0].getPhysicalNumberOfCells(); i++) {
				String head = "";
				for(int k=0; k<rows.length; k++){
					HSSFCell c = rows[k].getCell(i);
					for(int j=0; j<ranges.size(); j++){
						CellRangeAddress range = ranges.get(j);
						if(range.getFirstRow() == rows[k].getRowNum()){
							if(range.getFirstColumn() < i && i <= range.getLastColumn()){
								c = rows[k].getCell(range.getFirstColumn());
							}
						}
					}
					head += c.getStringCellValue().trim();
					headers.add(head);
				}
			}
		}
		return headers;
	}	
	
	/**
	 * 从Excel中读取数据
	 * @param sheet
	 * @param headers 表头
	 * @param dataStartRow 开始行
	 * @param omitLastRows 合并行
	 * @return
	 */
	protected static List<Map<String, Object>> retrieveData(HSSFSheet sheet,List<String> headers , int dataStartRow, int omitLastRows){
		if(log.isTraceEnabled()){
			log.trace("HSSFSheet："+sheet+"，headers："+headers+"，dataStartRow："+dataStartRow+"，omitLastRows："+ omitLastRows);
		}
		//从第dataStartRow行开始
		String v;
		List<Map<String, Object>> data =new ArrayList<Map<String, Object>>();
		DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");    
		if(log.isTraceEnabled()){
			log.trace("sheet.getLastRowNum():"+sheet.getLastRowNum() + "; omitLastRows=" + omitLastRows);
		}

		for(int i=dataStartRow;i<= sheet.getLastRowNum() - omitLastRows;i++){
			Row row=sheet.getRow(i);
			if(row == null){
				if(log.isInfoEnabled()){
					log.info("在第" + i + "行遇到空行，退出检索数据。");
				}
				break;
			}
			Map<String,Object> map=new HashMap<String,Object>();
			for(int j=0;j<headers.size();j++){
				if(log.isTraceEnabled()){
					log.trace("headers.size()= " + (headers == null ? "null" : headers.size()) + "; j=" + j + "; row=" + i + ", lastCellNum=" + (row == null ? "null" : row.getLastCellNum()));
		    		log.trace("headers:"+headers.get(j)+" "+row.getCell(j));
				}
				if(headers.get(j).length() == 0){
					//不需要的列
					continue;
				}
				Cell c = row.getCell(j);
				if(c != null){
					switch (c.getCellType()) { 
                        case HSSFCell.CELL_TYPE_FORMULA : 
                            v = c.getCellFormula();
                            break; 
                        case HSSFCell.CELL_TYPE_NUMERIC : 
                        	if(HSSFDateUtil.isCellDateFormatted(c)) {  
                        	    Date d = c.getDateCellValue();
                        	    v = dateFormater.format(d);
                        	}else{
                        		v = String.valueOf(c.getNumericCellValue());
                        	}
                            break; 
                        case HSSFCell.CELL_TYPE_STRING : 
                            v = c.getStringCellValue(); 
                            break;                                                   	
                        case HSSFCell.CELL_TYPE_BLANK : 
                            v = ""; 
                            break; 
                        default : 
                            v = ""; 
                            break; 
                    } 
                    //如果读取的是科学计数法的格式，则转换为普通格式 
                    if (null != v && v.indexOf(".") != -1 &&  v.indexOf("E") != -1) { 
                        DecimalFormat df = new DecimalFormat(); 
                        try {
							v = df.parse(v).toString();
						} catch (ParseException e) {
							if(log.isWarnEnabled()){
							    log.warn("转换科学数法格式数据出错。" + v, e);
							}
						} 
                    } 

                    //如果读取的是数字格式，并且以".0"结尾格式，则转换为普通格式 
                    if (null != v && v.endsWith(".0")) { 
                        int size = v.length(); 
                        v = v.substring(0, size - 2); 
                    } 
				}else{
					v = "";
				}				
				map.put(headers.get(j).toString(), v);
			}			
			data.add(map);
		}
		return data;
	}
	
	/**
	 * 读取EXCEL的数据，按列名和数值存储
	 * @param sheet
	 * @param titleRow 表头有几行
	 * @param dataStartRow 数据的开始行
	 * @param titleRowCount 标题行的开始行
	 * @param omitLastRows  略过最后的几行，适用于有合计或其他等数据
	 * @return
	 */
	public static List<Map<String, Object>> getDate(HSSFSheet sheet, int titleRow, int dataStartRow, int titleRowCount, int omitLastRows){
		if(log.isTraceEnabled()){
			log.trace("HSSFSheet："+sheet+"，titleRow："+ titleRow+"，dataStartRow："
		                      +dataStartRow+"，titleRowCount："+titleRowCount+"，omitLastRows："+ omitLastRows);
		}
		List<String> headers = retrieveHeaders(sheet,  titleRow,  dataStartRow, titleRowCount);
		if(headers == null || headers.size() == 0){
			return null;
		}
		List<Map<String, Object>> data = retrieveData(sheet,headers, dataStartRow, omitLastRows);
		return data;
	}
	
}
