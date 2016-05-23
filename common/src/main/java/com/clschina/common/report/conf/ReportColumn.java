package com.clschina.common.report.conf;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class ReportColumn {
	 public static final int TYPE_STRING = 1;	//字符串型
	 public static final int TYPE_NUMBER = 2;	//数值型，整型、浮点、双精度等都用这个
	 public static final int TYPE_DATE = 3;		//日期、时间
	 
	 private int dataIndex;
	 private String label;
	 /**
	  * 配置多级表头（即几行），通过逗号隔开。
	  * 如：
	  * label=‘礼品名称’ multistageLabel=‘礼品,    一号仓库,仓库’//‘礼品’通两列，‘仓库’通三列，
	  * label=‘礼品数量’ multistageLabel=‘礼品,    二号仓库,仓库’
	  * label=‘礼品单位’ multistageLabel=‘礼品单位,三号仓库,仓库’//‘礼品单位’通两行
	  * 即：
	  * label=‘最后一级的该列’ multistageLabel=‘第三行的该列,第二行的该列,第一行的该列’
	  */
	 private String multistageLabel;
	 /**
	  * 该列通列的个数
	  */
	 private int column;
	 /**
	  * 该列通行的个数
	  */
	 private int row;
	 
	 private int dataType = TYPE_STRING;
	 private String styleClass;
	 
	 private int htmlIndex = 0; //-1表示不显示在HTML里
	 private int excelIndex = 0; //-1表示不显示在Excel里
	 
	 private boolean sumOnFooter = false;	//在数据后补充一行合计值
	 private boolean avgOnFooter = false;	//在数据后补充一行平均值
	 
	 private String headOnClick;//表头触发事件
	 private int titleDataIndex = -1;//html title 标签的数据索引
	  
	 
	 /**
	  * 日期类型的格式，例如yyyy/MM/dd HH:mm
	  */
	 private String datePattern; 
	 
	 /**
	  * 数值类型格式，例如: #,##0.00
	  */
	 private String numberPattern;
	 
	 private SimpleDateFormat dateFormat;
	 private DecimalFormat numberFormat;
	 
	/**
	 * @return the sqlIndex
	 */
	public int getDataIndex() {
		return dataIndex;
	}
	/**
	 * @param sqlIndex the sqlIndex to set
	 */
	public void setDataIndex(int dataIndex) {
		this.dataIndex = dataIndex;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}
	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return the styleClass
	 */
	public String getStyleClass() {
		return styleClass;
	}
	/**
	 * @param styleClass the styleClass to set
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	/**
	 * @return the htmlIndex
	 */
	public int getHtmlIndex() {
		return htmlIndex;
	}
	/**
	 * @param htmlIndex the htmlIndex to set
	 */
	public void setHtmlIndex(int htmlIndex) {
		this.htmlIndex = htmlIndex;
	}
	/**
	 * @return the excelIndex
	 */
	public int getExcelIndex() {
		return excelIndex;
	}
	/**
	 * @param excelIndex the excelIndex to set
	 */
	public void setExcelIndex(int excelIndex) {
		this.excelIndex = excelIndex;
	}
	/**
	 * @return the sumOnFooter
	 */
	public boolean isSumOnFooter() {
		return sumOnFooter;
	}
	/**
	 * @param sumOnFooter the sumOnFooter to set
	 */
	public void setSumOnFooter(boolean sumOnFooter) {
		this.sumOnFooter = sumOnFooter;
	}
	/**
	 * @return the avgOnFooter
	 */
	public boolean isAvgOnFooter() {
		return avgOnFooter;
	}
	/**
	 * @param avgOnFooter the avgOnFooter to set
	 */
	public void setAvgOnFooter(boolean avgOnFooter) {
		this.avgOnFooter = avgOnFooter;
	}
	/**
	 * @return the headOnClick
	 */
	public String getHeadOnClick() {
		return headOnClick;
	}
	/**
	 * @param headOnClick
	 */
	public void setHeadOnClick(String headOnClick) {
		this.headOnClick = headOnClick;
	}
	/**
	 * @return the datePattern
	 */
	public String getDatePattern() {
		return datePattern;
	}
	/**
	 * @param datePattern the datePattern to set
	 */
	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
		if(datePattern != null){
			this.dateFormat = new SimpleDateFormat(datePattern);
		}else{
			this.dateFormat = null;
		}
	}
	/**
	 * @return the numberPattern
	 */
	public String getNumberPattern() {
		return numberPattern;
	}
	
	/**
	 * @param numberPattern the numberPattern to set
	 */
	public void setNumberPattern(String numberPattern) {
		this.numberPattern = numberPattern;
		if(numberPattern != null){
			this.numberFormat = new DecimalFormat(numberPattern);
		}else{
			this.numberFormat = null;
		}
	}

	public String formatNumber(Number n){
		if(this.numberFormat != null){
			return numberFormat.format(n.doubleValue());
		}else{
			return null;
		}
	}
	public String formatDate(Date d){
		if(this.dateFormat != null){
			return dateFormat.format(d);
		}else{
			return null;
		}
	}

	public int getTitleDataIndex() {
		return titleDataIndex;
	}
	public void setTitleDataIndex(int titleDataIndex) {
		this.titleDataIndex = titleDataIndex;
	}

	
	public String getMultistageLabel() {
		return multistageLabel;
	}
	public void setMultistageLabel(String multistageLabel) {
		this.multistageLabel = multistageLabel;
	}
	public int getColumn() {
		return column;
	}
	public void setColumn(int column) {
		this.column = column;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}

}
