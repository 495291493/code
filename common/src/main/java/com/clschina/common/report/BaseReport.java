package com.clschina.common.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.report.conf.ReportColumn;
import com.clschina.common.report.conf.ReportConfiguration;

/**
 * 报表基础类
 * @author yjt
 *
 */
public class BaseReport {
	private static Log log = LogFactory.getLog(BaseReport.class);
	/**
	 * 数据集合
	 */
	private List<List<Object[]>> data = new ArrayList<List<Object[]>>();
	private InputStream excelTempletIS;
	private ReportConfiguration cfg = null;
	/**
	 * 自己拼接的表头（只有表头中的行与单元格）
	 */
	private String tableHeadString;
	/**
	 * 若是自己拼接的表头，在拼接数据时，就以该列为准
	 */
	private List<ReportColumn> tableHeadStringReportColumnList;
	
	
	


	/**
	 * HTML输出数据预览
	 * @return
	 */
	public String genHtmlTable(){
		//验证
		if(!verificationData()) {
			return null;
		}
		if(tableHeadString != null && tableHeadString.length() > 0) {//自己拼接表头
			return getHtmlTable(tableHeadString);
		}
		int bths = judgeIsMultistage(cfg.getColumns());//表头的行数
		if(bths > 1) {//根据配置文件，组成多级表头结构
			return getHtmlTableByMultistage();
		}
		return getHtmlTable();//原始表头结构
	}
	
	/**
	 * HTML输出数据预览
	 * @return
	 */
	public String getHtmlTable(){
		StringBuffer html = new StringBuffer();
		
		//验证
		if(!verificationData()) {
			return null;
		}
		
		//排序
		List<ReportColumn> cols = sortColumn();
		
		html.append("<table id='datatable' boder='0' cellspacing='1'");
		if(cfg.getTableClass() != null){
			html.append(" class=\"");
			html.append(cfg.getTableClass());
			html.append("\"");
		}
		html.append(">");
		html.append("<thead>");
		
		html.append("<tr>");
		if(cfg.isHtmlRowNum()){
			html.append("<th>　</th>");
		}
		//循环预览表头
		for(int j=0; j<cols.size(); j++){
			ReportColumn col = cols.get(j);
			if(col.getHtmlIndex() < 0){
				continue;
			}
			if(col.getHeadOnClick() != null && !"".equals(col.getHeadOnClick())){
				html.append("<th style=\"cursor:pointer;\" onclick=\"");
				html.append(col.getHeadOnClick());
				html.append("\">");
			}else{
				html.append("<th>");
			}
    		html.append(col.getLabel());
			html.append("</th>");
		}
		html.append("</tr>");
		
		html.append("</thead>\r\n");
		
		//根据数据，拼接html代码
		getStringBufferByData(html, cols);
		
		return html.toString();
	}
	
	/**
	 * HTML输出数据预览
	 * @param tableHeadString 自己拼接的表头（只有表头中的行与单元格）
	 * @return
	 */
	public String getHtmlTable(String tableHeadString){
		StringBuffer html = new StringBuffer();
		
		//验证
		if(!verificationData()) {
			return null;
		}
		
		//排序
		//List<ReportColumn> cols = sortColumn();
		
		html.append("<table id='datatable' boder='0' cellspacing='1'");
		if(cfg.getTableClass() != null){
			html.append(" class=\"");
			html.append(cfg.getTableClass());
			html.append("\"");
		}
		html.append(">");
		html.append("<thead>");
		
		html.append(tableHeadString);
		
		html.append("</thead>\r\n");
		
		//根据数据，拼接html代码
		getStringBufferByData(html, tableHeadStringReportColumnList);
		
		return html.toString();
	}
	
	/**
	 * 
	 * 
	 * 配置多级表头（即几行），通过逗号隔开。
	 * 例1，如：
	 * label=‘礼品名称’ multistageLabel=‘礼品,    一号仓库,仓库’//‘礼品’通两列，‘仓库’通三列，
	 * label=‘礼品数量’ multistageLabel=‘礼品,    二号仓库,仓库’
	 * label=‘礼品单位’ multistageLabel=‘礼品单位,三号仓库,仓库’//‘礼品单位’通两行
	 * 以上有四级表头结构
	 * 即：
	 * label=‘最后一级的该列’ multistageLabel=‘第三行的该列,第二行的该列,第一行的该列’
	 * 
	 * 例2，如：
	 * label="订单日期" multistageLabel="订单,    商城"//‘订单’通两列，‘商城’通两列
	 * label="订单号"   multistageLabel="订单,    商城"
	 * label="礼品编号" multistageLabel="礼品编号,仓库"//‘礼品编号’通两行，‘仓库’通四列
	 * label="礼品名称" multistageLabel="礼品,    仓库"
	 * label="采购数量" multistageLabel="库存,    仓库"//‘库存’通两列
	 * label="入库数量" multistageLabel="库存,    仓库"
	 * label="已到票数量"                              //‘已到票数量’通三行
	 * label="未到票数量"                              //‘未到票数量’通三行
	 * 以上有三级表头结构（没有multistageLabel属性的，表示最后一级的单元格，通所有行）
	 * 
	 * 
	 * HTML输出数据预览（多级表头结构）
	 * @return
	 */
	public String getHtmlTableByMultistage() {
        StringBuffer html = new StringBuffer();
		
        //验证
		if(!verificationData()) {
			return null;
		}
		
		//排序
		List<ReportColumn> cols = sortColumn();
		
		/*确定表头的行数*/
		int bths = judgeIsMultistage(cols);//表头的行数
		
		/*创建各行中的表头列*/
		List<List<ReportColumn>> multistageLabelList = new ArrayList<List<ReportColumn>>();
		if(bths > 1) {
			for(int ii = 0; ii < bths; ii ++) {
				List<ReportColumn> reportColumnList = new ArrayList<ReportColumn>();
				for(int j = 0; j< cols.size(); j++) {
					ReportColumn col = cols.get(j);
					String multistageLabel = (col.getMultistageLabel() == null) ? "" : col.getMultistageLabel();//多级表头
					String label = col.getLabel();//最后一级表头
					
					if(log.isWarnEnabled()) {
						log.warn("第" + ii + "行第" + j + "列[" + multistageLabel + "]label[" + label + "]");
					}
					
					if(multistageLabel.length() > 0) {
						
						if(ii == (bths - 1)) {//最后一级表头，要特殊处理
							ReportColumn rc = multistageLabelList.get(ii - 1).get(j);
							if(label.equals(rc.getLabel())) {//判断是否，与上一行的该列相同
								//...
								multistageLabelList.get(ii - 1).get(j).setHeadOnClick(col.getHeadOnClick());								
							}else {
								col.setRow(1);//该列的通行个数
								reportColumnList.add(col);
								if(log.isWarnEnabled()) {
									log.warn("第" + ii + "行第" + j + "列[" + reportColumnList.get(reportColumnList.size() - 1).getLabel() + "]");
								}
							}
							continue;
						}
						
						String labelTwo = "";//该表头单元格的名称
						String[] strTwo = null;
						if(multistageLabel.indexOf(",") != -1) {
							strTwo = multistageLabel.split(",");
							labelTwo = strTwo[((strTwo.length - 1) - ii)];
						}else {
							labelTwo = multistageLabel;
						}
						
						if(j > 0) {//判断是否，与上一列相同
							ReportColumn rc = reportColumnList.get(j - 1);							
							if(labelTwo.equals(rc.getLabel())) {//与上一列相同
								ReportColumn c = new ReportColumn();
								c.setLabel(labelTwo);
								c.setHtmlIndex(-1);//不启用，加入该列，是为了下一个列的判断
								reportColumnList.add(c);
								if(log.isWarnEnabled()) {
									log.warn("第" + ii + "行第" + j + "列[" + reportColumnList.get(reportColumnList.size() - 1).getLabel() + "]");
								}
								continue;
							}
						}
						
						if(ii > 0) {//判断是否，与上一行的该列相同
							ReportColumn rc = multistageLabelList.get(ii - 1).get(j);
							if(labelTwo.equals(rc.getLabel())) {//与上一行的该列相同
								ReportColumn c = new ReportColumn();
								c.setLabel(labelTwo);
								c.setHtmlIndex(-1);//不启用，加入该列，是为了下一个列的判断
								reportColumnList.add(c);
								if(log.isWarnEnabled()) {
									log.warn("第" + ii + "行第" + j + "列[" + reportColumnList.get(reportColumnList.size() - 1).getLabel() + "]");
								}
								continue;
							}
						}
						
						if(multistageLabel.indexOf(",") != -1) {//超过两级
							ReportColumn c = new ReportColumn();
							c.setLabel(labelTwo);
							//判断通几列
							int column = 1;
							for(int n = 0; n < cols.size(); n ++) {
								if(n > j) {//从下一个列，开始判断
									ReportColumn colT = cols.get(n);
									String multistageLabelT = colT.getMultistageLabel();
									if(multistageLabelT == null || multistageLabelT.length() <= 0) {
										break;
									}
									String[] strT = multistageLabelT.split(",");
									String labelT = strT[((strT.length - 1) - ii)];
									if(labelTwo.equals(labelT)) {//如果同行的两个列名称相同，即这连个列要通成一个列
										column ++;
									}else {
										break;
									}
								}
							}
							c.setColumn(column);//通列
							//判断通几行
							int row = 1;
							if(ii == (strTwo.length -1)) {
								if(label.equals(labelTwo)){
									row ++;
								}
							}else{
								for(int m = (strTwo.length -1); m >= 0; m --) {
									int mm = (strTwo.length -1) - m;//获得反过来的顺序
									if(mm > ii) {//从下一个行，开始判断
										if(strTwo[m].equals(labelTwo)) {
											row ++;
											if(mm == (strTwo.length -1)) {//判断最后一级的该列，是否要与该列通行
												if(label.equals(labelTwo)){
													row ++;
												}
											}
										}else {
											break;
										}
									}
								}
							}							
							c.setRow(row);//通行
							reportColumnList.add(c);
							if(log.isWarnEnabled()) {
								log.warn("第" + ii + "行第" + j + "列[" + reportColumnList.get(reportColumnList.size() - 1).getLabel() + "]");
							}
						}else {
							ReportColumn c = new ReportColumn();
							c.setLabel(labelTwo);
							//判断通几列
							int column = 1;
							for(int n = 0; n < cols.size(); n ++) {
								if(n > j) {//从下一个列，开始判断
									ReportColumn colT = cols.get(n);
									String multistageLabelT = colT.getMultistageLabel();
									if(multistageLabelT == null || multistageLabelT.length() <= 0) {
										break;
									}
									String labelT = multistageLabelT;
									if(labelTwo.equals(labelT)) {//如果同行的两个列名称相同，即这连个列要通成一个列
										column ++;
									}else {
										break;
									}
								}
							}
							c.setColumn(column);//通列
							//判断通几行
							int row = 1;
							if(label.equals(labelTwo)){
								row ++;
							}							
							c.setRow(row);//通行
							reportColumnList.add(c);
							if(log.isWarnEnabled()) {
								log.warn("第" + ii + "行第" + j + "列[" + reportColumnList.get(reportColumnList.size() - 1).getLabel() + "]");
							}
						}
						
					}else {
						
						//没有多级属性multistageLabel，表示该列通表头的所有行
						if(ii == 0){
							col.setRow(bths);//该列的通行个数
							reportColumnList.add(col);
							if(log.isWarnEnabled()) {
								log.warn("第" + ii + "行第" + j + "列[" + reportColumnList.get(reportColumnList.size() - 1).getLabel() + "]");
							}
						}
						
					}
				}
				multistageLabelList.add(reportColumnList);//
			}
		}
		
		html.append("<table id='datatable' boder='0' cellspacing='1'");
		if(cfg.getTableClass() != null){
			html.append(" class=\"");
			html.append(cfg.getTableClass());
			html.append("\"");
		}
		html.append(">");
		html.append("<thead>");
		int index = 0;
		for (List<ReportColumn> list : multistageLabelList) {
			html.append("<tr>");
			if(cfg.isHtmlRowNum() && index == 0){
				html.append("<th rowspan=\"" + multistageLabelList.size() + "\">　</th>");
			}
			index ++;
			for (ReportColumn reportColumn : list) {
				if(reportColumn.getHtmlIndex() < 0){
					continue;
				}
				html.append("<th ");
				if(reportColumn.getHeadOnClick() != null && !"".equals(reportColumn.getHeadOnClick())){
					html.append("style=\"cursor:pointer;\" onclick=\"");
					html.append(reportColumn.getHeadOnClick());
					html.append("\"");
				}
				if(reportColumn.getRow() > 1) {
					html.append(" rowspan=\"" + reportColumn.getRow() + "\"");
				}
				if(reportColumn.getColumn() > 1) {
					html.append(" colspan=\"" + reportColumn.getColumn() + "\"");
				}
				html.append(">");
	    		html.append(reportColumn.getLabel());
				html.append("</th>");
			}
			html.append("</tr>");
		}
		html.append("</thead>\r\n");
		
		//根据数据，拼接html代码
		getStringBufferByData(html, cols);
		
		return html.toString();
	}
	
	/**
	 * 判断表头，是否是多级表头
	 * @param cols
	 * @return 返回表头的行数（0表示没多级，大于0表示多级）
	 */
	private int judgeIsMultistage(List<ReportColumn> cols) {
		/*确定表头的行数*/
		int bths = 0;//表头的行数
		for(ReportColumn col : cols) {
			String multistageLabel = col.getMultistageLabel();
			if(multistageLabel != null && multistageLabel.length() > 0) {
				if(multistageLabel.indexOf(",") != -1) {
					String[] str = multistageLabel.split(",");
					if(str.length > bths) {
						bths = str.length + 1;
					}
				}else {
					bths = 1 + 1;
				}
				break;
			}
		}
		return bths;
	}
	
	/**
	 * 验证是否存在数据
	 * @return
	 */
	private boolean verificationData() {
		if(data == null || data.isEmpty()){
			if(log.isTraceEnabled()){
				log.trace("List Data not set");
			}
			return false;
		}
		List<ReportColumn> cols = cfg.getColumns();
		if(cols == null || cols.size()==0){
			return false;
		}
		return true;
	}
	
	/**
	 * 排序表头的列
	 * @return
	 */
	private List<ReportColumn> sortColumn() {
		List<ReportColumn> cols = cfg.getColumns();
		Collections.sort(cols, new Comparator<ReportColumn>(){
			public int compare(ReportColumn o1, ReportColumn o2) {
				return o1.getHtmlIndex() - o2.getHtmlIndex();
			}});
		return cols;
	}
	
	/**
	 * 根据数据，拼接html代码
	 * @param html
	 */
	private void getStringBufferByData(StringBuffer html, List<ReportColumn> cols) {
		html.append("<tbody>");
		if(this.getData().size() == 0){
			html.append("<tr><td colspan=\"" + (cols.size() + (cfg.isHtmlRowNum() ? 1 : 0)) + "\" class=\"nodata\">无数据</td></tr>");
		}
		Map<String,Double> mapSum = new HashMap<String, Double>();//合计 
		Map<String,Double> mapAvg = new HashMap<String, Double>();//平均 
		//循环显示数据
		for(int i=0; i<this.getData().size(); i++){
			Object[] row = this.getData().get(i);
			html.append("<tr");
			if(cfg.getRowClasses() != null && cfg.getRowClasses().size() > 0){
				html.append(" class=\"");
				html.append(cfg.getRowClasses().get(i % cfg.getRowClasses().size()));
				if(cfg.getRowClass() != null){
					String cn = cfg.getRowClass().getRowClass(row, i);
					if(cn != null){
						html.append(" ");
						html.append(cn);
					}
				}
				html.append("\"");
			}
			html.append(">");
			if(cfg.isHtmlRowNum()){
				html.append("<td class='rowno'>" + (i+1) + ".</td>");
			}
			boolean shouldMegred = true;
			int[] columnsMerged = cfg.getColumnsMergedWithPrevious();
			if(i > 0){
				Object[] previousRow = this.getData().get(i - 1);
				for(int k=0; k<columnsMerged.length; k++){
					int l = columnsMerged[k];
					if(!previousRow[l].equals(row[l])){
						if(log.isTraceEnabled()){
							log.trace("ROW #" + i + ", column #" + l + ", '" + previousRow[l] + "' vs '" + row[l] + "' not equeal.");
						}
						shouldMegred = false;
					}
				}
				if(columnsMerged.length == 0){
					shouldMegred = false;
				}
			}else{
				shouldMegred = false;
			}
			if(log.isTraceEnabled()){
				String v = "";
				for(int l=0; l<columnsMerged.length; l++){
					v += columnsMerged[l] + ", ";
				}
				log.trace("ROW #" + i + "  shouldMegred=" + shouldMegred + "; columnsMerged=" + v);
			}
			//显示每列数据
			for(int j=0; j<cols.size(); j++){
				ReportColumn col = cols.get(j);
				if(col.getHtmlIndex() < 0){
					continue;
				}				
				html.append("<td");
				if(col.getStyleClass() != null){
					html.append(" class=\"");
					html.append(col.getStyleClass());
					html.append("\"");
				}
				if(col.getTitleDataIndex() != -1 
						&& row[col.getTitleDataIndex()] != null){
					html.append(" title=\"");
					html.append((String)row[col.getTitleDataIndex()]);
					html.append("\"");
				}
				html.append(">");
				Object value = row[col.getDataIndex()];
				//合计
				if(col.isSumOnFooter()){
					Double thisValue =((Number) value).doubleValue();
					Double	sum = mapSum.get(col.getLabel());
					if(sum != null){
					    sum += thisValue;
					}else{
					    sum = thisValue;
					}
					mapSum.put(col.getLabel(), sum);
				}
				//求平均
				if(col.isAvgOnFooter()){
					Double thisValue =((Number) value).doubleValue();
					Double	sum = mapSum.get(col.getLabel());
					if(sum != null){
					    sum += thisValue;
					}else{
					    sum = thisValue;
					}
					mapAvg.put(col.getLabel(), sum);
				}
				
				if(shouldMegred){
					for(int k=0; k<columnsMerged.length; k++){
						if(columnsMerged[k] == j){
							//该不显示的列
							value = "";
							break;
						}
					}
				}
				if(cfg.getConverter() != null){
					value = cfg.getConverter().htmlCellConverter(value, col.getDataIndex(), i);
				}
				if(value instanceof Date){
					String nv = col.formatDate((Date) value);
					if(nv != null){
						value = nv;
					}
				}else if(value instanceof Number){
					String nv = col.formatNumber((Number) value);
					if(nv != null){
						value = nv;
					}
				}
				if(value == null){
					value = "";
				}
				html.append(value.toString());
				
				html.append("</td>");
			}
			html.append("</tr>");
		}
        html.append("</tbody>");
		
		//显示尾部统计信息
		if(mapSum.size() >0  || mapAvg.size() >0){
			 html.append("<tfoot>");
			 //合计
			 if(mapSum.size() >0){
				 html.append("<tr>");
			    if(cfg.isHtmlRowNum()){//行号
			    	html.append("<td></td>");
			    }
				for(int j=0; j<cols.size(); j++){
					ReportColumn col = cols.get(j);
					if(col.getHtmlIndex() < 0){
						continue;
					}
					html.append("<td>");
	                if(col.isSumOnFooter()){
	                    if(log.isTraceEnabled()){
	                        log.trace(col.getLabel() + " 需要合计。");
	                        log.trace("mapSum变量中有" + mapSum.size() + "个信息 " + mapSum);
	                    }
	                    double v = mapSum.get(col.getLabel());
	                    String nv = col.formatNumber((Number) v);
	                    html.append("合计：");
	                    if(nv != null){
	                        html.append(nv);
	                    }else{
	                        String f = String.valueOf(v);
	                        if(f.endsWith(".00") || f.endsWith(".0")){
	                            DecimalFormat df = new DecimalFormat("#,##0");
	                            html.append(df.format(v));
	                        }else{
	                            html.append(v);
	                        }
	                    }
	                }
					html.append("</td>");
				}
				html.append("</tr>");
			 }
             //显示平均
			if(mapAvg.size() >0){
				 html.append("<tr>");
			    if(cfg.isHtmlRowNum()){//行号
			    	html.append("<td></td>");
			    }	
				for(int j=0; j<cols.size(); j++){
					ReportColumn col = cols.get(j);
					if(col.getHtmlIndex() < 0){
						continue;
					}
					html.append("<td>");
	                if(col.isAvgOnFooter()){
	                    if(log.isTraceEnabled()){
	                        log.trace(col.getLabel() + " 需要求平均。");
	                        log.trace("mapAvg变量中有" + mapAvg.size() + "个信息 " + mapAvg);
	                    }
	                    double v = mapAvg.get(col.getLabel())/data.size();	           
	                    String nv = col.formatNumber((Number) v);
	                    html.append("平均：");
	                    if(nv != null){
	                        html.append(nv);
	                    }else{
	                        html.append(v);
	                    }	                    
	                }
					html.append("</td>");
				}
			   html.append("</tr>");
			}
	        html.append("</tfoot>");
		}
		html.append("</table>");
	}
	
	/**
	 * JXL下载EXCEL
	 * @param os
	 * @param excelTempletIS
	 * @param closeInputStream
	 * @throws Exception
	 */
	private void genExcelUsingJXL(OutputStream os, InputStream excelTempletIS, boolean closeInputStream) throws Exception{
		Workbook wb = Workbook.getWorkbook(excelTempletIS);
		WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
		WritableSheet sheet = null;//wwb.getSheet(0);
		
		List<ReportColumn> cols = null;
		if (cfg.getExcelTableHead() != null) {//自己设置Excel表头
			cols = tableHeadStringReportColumnList;
		}else {
			cols = cfg.getColumns();
		}
		
		if (cols != null && cols.size() > 0) {
			
			for (int index = 0; index < data.size(); index++) {
				sheet = wwb.getSheet(index);
				if (cfg.getExcelTableHead() != null) {//自己设置Excel表头
					cfg.getExcelTableHead().settingsExcelTableHead(sheet, wwb);
				}
				
				for (int i = 0; i < this.data.get(index).size(); i++) {
					Object[] row = this.data.get(index).get(i);
					if (cfg.isExcelRowNum()) {
						jxl.write.Number num = new jxl.write.Number(0, cfg.getExcelStartRow() + i, (i + 1));
						sheet.addCell(num);
					}
					for (int j = 0; j < cols.size(); j++) {
						ReportColumn col = cols.get(j);
						if (col.getHtmlIndex() < 0) {
							continue;
						}
						Object cellData = null;
						if (cfg.getConverter() != null) {
							cellData = cfg.getConverter().excelCellConverter(row[col.getDataIndex()], col.getDataIndex(), i, row);
						} else {
							cellData = row[col.getDataIndex()];
						}

						int rowNum = cfg.getExcelStartRow() + i;
						int colNum = col.getExcelIndex();
						WritableCell cell = null;

						if (cellData == null) {
							cell = new Label(colNum, rowNum, "");
						} else if (cellData instanceof Date) {
							cell = new jxl.write.DateTime(colNum, rowNum, ((Date) cellData));
						} else if (cellData instanceof Number) {
							cell = new jxl.write.Number(colNum, rowNum, ((Number) cellData).doubleValue());
						} else {
							cell = new Label(colNum, rowNum, cellData.toString());
						}
						sheet.addCell(cell);
					}
				}
			}
		}
		wwb.write();
		wwb.close();
		if(closeInputStream){
			wb.close();
		}
	}
	
	/**
	 * 下载
	 * @param os
	 * @throws IOException
	 */
	public void genExcel(OutputStream os) throws IOException{
		genExcel(os, null);
	}
	
	public void genExcel(OutputStream os, InputStream excelTemplate) throws IOException{
		//读取模板
		boolean closeInputStream = false;
		if(excelTemplate != null){
			excelTempletIS = excelTemplate;
		}else if(cfg.getExcelTempletFile() != null){
			String f = ThreadLocalManager.getContextFolder() + "/WEB-INF/report-config/xls/" + cfg.getExcelTempletFile();
			InputStream fis = new FileInputStream(f);
			closeInputStream = true;
			excelTempletIS = fis;
		}
		if(excelTempletIS == null){
			throw new NullPointerException("请首先设置Excel模板，之后导出。");
		}

		switch (cfg.getExcelAPI()){
			case ReportConfiguration.JXL:
				try {
					genExcelUsingJXL(os, excelTempletIS, closeInputStream);
				} catch (Exception e) {
					if(log.isErrorEnabled()){
					    log.error("Error occured.", e);
					}
					throw new IOException("生成Excel出错" + e);
				}
				break;
			case ReportConfiguration.POI:
				genExcelUsingPOI(os, excelTempletIS, closeInputStream);
				break;
			default:
				genExcelUsingPOI(os, excelTempletIS, closeInputStream);
		}
	}
	
	/**
	 * POI下载EXCEL
	 * @param os
	 * @param excelTempletIS
	 * @param closeInputStream
	 * @throws IOException
	 */
	private void genExcelUsingPOI(OutputStream os, InputStream excelTempletIS, boolean closeInputStream) throws IOException{
		HSSFWorkbook workbook = new HSSFWorkbook(excelTempletIS);
		HSSFSheet sheet = null;
		
		List<ReportColumn> cols = null;
		if (cfg.getExcelTableHead() != null) {//自己设置Excel表头
			cols = tableHeadStringReportColumnList;
		}else {
			cols = cfg.getColumns();
		}
		
		if (cols != null && cols.size() > 0) {
			SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
			
			for (int index = 0; index < data.size(); index++) {
				sheet = workbook.getSheetAt(index);
				if (cfg.getExcelTableHead() != null) {//自己设置Excel表头
					cfg.getExcelTableHead().settingsExcelTableHead(sheet, workbook);
				}

				for (int i = 0; i < data.get(index).size(); i++) {
					Object[] row = data.get(index).get(i);
					if (cfg.isExcelRowNum()) {
						HSSFRow excelRow = null;
						excelRow = sheet.getRow(cfg.getExcelStartRow() + i);
						if (excelRow == null) {
							excelRow = sheet.createRow(cfg.getExcelStartRow() + i);
						}
						HSSFCell cell = excelRow.getCell(0);
						if (cell == null) {
							cell = excelRow.createCell(0);
						}
						cell.setCellValue(i + 1);
					}
					for (int j = 0; j < cols.size(); j++) {
						ReportColumn col = cols.get(j);
						if (col.getHtmlIndex() < 0) {
							continue;
						}
						Object cellData = null;
						if (cfg.getConverter() != null) {
							cellData = cfg.getConverter().excelCellConverter(row[col.getDataIndex()], col.getDataIndex(), i, row);
						} else {
							cellData = row[col.getDataIndex()];
						}

						HSSFRow excelRow = null;
						excelRow = sheet.getRow(cfg.getExcelStartRow() + i);
						if (excelRow == null) {
							excelRow = sheet.createRow(cfg.getExcelStartRow() + i);
						}
						if (log.isTraceEnabled()) {
							log.trace("[allColSize=" + cols.size() + "]i=" + i + " j=" + j + "; data=" + cellData);
						}
						if (excelRow == null) {
							throw new NullPointerException("无法得到Excel行#" + (cfg.getExcelStartRow() + i) + "，很可能是模板中行数不足。");
						}
						HSSFCell cell = excelRow.getCell(col.getExcelIndex());
						if (cell == null) {
							cell = excelRow.createCell(col.getExcelIndex());
						}
						if (cellData == null) {
							// do nothing
						} else if (cellData instanceof Date) {
							cell.setCellValue(dateFormat.format((Date) cellData));
						} else if (cellData instanceof Number) {
							cell.setCellValue(((Number) cellData).doubleValue());
						} else {
							cell.setCellValue(cellData.toString());
						}
					}
				}
			}
		}
		
		//输出到OutputStream
		workbook.write(os);
        os.flush();
        if(closeInputStream){
        	excelTempletIS.close();
        }
	}

	/**
	 * @return the data
	 */
	public List<Object[]> getData() {
		return this.data.size() == 0 ? new ArrayList<Object[]>() : this.data.get(0);
	}
	
	/**
	 * 得到所有SQL的查询数据
	 * @return
	 */
	public List<List<Object[]>>  getAllData(){
		return this.data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<Object[]> data) {
		this.data.add(0,data);
	}
	
	
	/**
	 * 增加数据，每条SQL对应一个DATA
	 * @param data
	 */
	public void addData(List<Object[]> data) {
		this.data.add(data);
	}

	/**
	 * @param excelTempletIS the excelTempletIS to set
	 */
	public void setExcelTempletInputStream(InputStream excelTempletIS) {
		this.excelTempletIS = excelTempletIS;
	}

	/**
	 * @return the cfg
	 */
	public ReportConfiguration getConfiguration() {
		return cfg;
	}

	/**
	 * @param cfg the cfg to set
	 */
	public void setConfiguration(ReportConfiguration cfg) {
		this.cfg = cfg;
	}
	
	public String getTableHeadString() {
		return tableHeadString;
	}

	/**
	 * 自己拼接的表头（只有表头中的行与单元格）
	 */
	public void setTableHeadString(String tableHeadString) {
		this.tableHeadString = tableHeadString;
	}
	
	public List<ReportColumn> getTableHeadStringReportColumnList() {
		return tableHeadStringReportColumnList;
	}

	/**
	 * 若是自己拼接的表头，在拼接数据时，就以该列为准
	 * @param tableHeadStringReportColumnList
	 */
	public void setTableHeadStringReportColumnList(
			List<ReportColumn> tableHeadStringReportColumnList) {
		this.tableHeadStringReportColumnList = tableHeadStringReportColumnList;
	}
		
	/**
	 * 导出到客户端，用于下载
	 * @param fileName
	 */
	public void exportExcelToHttpClient(String fileName){
		String contentType = "application/msexcel";
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
		response.setContentType(contentType);
		StringBuffer contentDisposition = new StringBuffer();
		contentDisposition.append("attachment;filename=\"");
		contentDisposition.append(fileName);
		contentDisposition.append("\"");
		try{
			response.setHeader("Content-Disposition", new String(
					contentDisposition.toString().getBytes("gb2312"), "iso8859-1"));
			ServletOutputStream out = response.getOutputStream();
			genExcel(out);
			ctx.responseComplete();
		}catch(Exception e){
			log.error("导出Excel失败。(" + fileName + ")", e);
		}
	}
}
