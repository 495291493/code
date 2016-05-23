package com.clschina.common.backingbean;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.util.CommonUtil;

/**
 * 处理Excel文件上传、数据校验、预览、导入的基类
 *
 */
public abstract class AbstractExcelImportBackingBean {
	private static Log log = LogFactory.getLog(AbstractExcelImportBackingBean.class);
	protected final String ROWERRORKEY = "row-error";  
	/**
	 * 预览页面是否显示序号，非excel数据行号，按记录累加的序号
	 */
	protected boolean rowNum = false;
	/**
	 * 上传的Excel文件
	 */
	protected FileItem excelFile;
	
	/**
	 * 已经保存到服务器上的Excel文件名
	 */
	protected String excelFileName;
	/**
	 * 标题行的开始行，如果是-1；表示没有标题行。
	 * 如果是1，则标题行前还存在一行
	 */
	protected int titleRow = 0;
	
	/**
	 * 标题行有几行。
	 */
	protected int titleRowCount = 1;
	/**
	 * 数据的开始行
	 */
	protected int dataStartRow = 1;
	/**
	 * 略过最后的几行，适用于有合计或其他等数据
	 */
	protected int omitLastRows = 0;
	/**
	 * Excel数据的标题
	 */
	protected List<String> headers;	
	/**
	 * 需要的数据
	 */
	private List<Column> columns;	
	/**
	 * 如果有标题行的Excel，则key是标题；如果没标题行的，key是列号0~n
	 */
	protected List<Map<String, String>> data;
	
	/**
	 * 警告信息，有警告信息，可以下一步。
	 * 针对某个数据的警告信息 key = row-no:column-header
	 * 针对整行的警告信息 key = row-no
	 * 针对整个文件的警告信息 key = ""
	 * 例如：文件名重复， 
	 */
	private Map<String, String> warnMessages = new HashMap<String, String>();

	/**
	 * 错误信息，有错误信息，不可以下一步。
	 * 针对某个数据的错误信息 key = row-no:column-header
	 * 针对整行的错误信息 key = row-no
	 * 针对整个文件的错误信息 key = ""
	 * 例如：缺少必须的列；有重复数据等
	 */	
	private Map<String, String> errorMessages = new HashMap<String, String>();
	/**
	 * 上传文件的存放路径
	 */
	protected String uploadFolder;
	/**
	 * 校验文件上传的错误信息会被增加到这个控件上
	 */
	protected String jsfComponentId;
	
	protected DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");  

	/**
	 * HTML的tableid；如果设置，会在render html table时使用
	 */
	protected String htmlTableId;

	/**
	 * 验证某行数据是否合法
	 *  如果没有错误，调用addErrorMessage或addWarnMessage记录错误/警告信息；如果是整行的信息，列传递零长度字符串。
	 * @param data
	 * @return
	 */
	protected abstract void validateDataRow(Map<String, String> row, int rowNo);
	
	/**
	 * 从Excel中读取标题行
	 * @param sheet
	 */
	protected void retrieveHeaders(HSSFSheet sheet){
		headers = new ArrayList<String>();
		if(titleRow == -1){//titleRow=-1则没有标题
			if(log.isTraceEnabled()){
				log.trace("没有设置titleRow");
			}
			Row row=sheet.getRow(dataStartRow);
			if(row == null){
				if(log.isTraceEnabled()){
					log.trace("dataStartRow不正确，读取不到header数据");
				}
				addErrorMessage(-1, "", "Excel格式不正确，无有效的数据行。");
				return;
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
				}
				boolean added = false;
				for(int j=0; j<columns.size(); j++){
					if(columns.get(j).getName().equals(head)){
						headers.add(head);
						added = true;
						break;
					}
				}
				if(!added){
					headers.add("");
				}
			}
		}
	}
	/**
	 * 从Excel中读取数据
	 * @param sheet
	 */
	protected void retrieveData(HSSFSheet sheet){
		//从第dataStartRow行开始
		String v;
		data =new ArrayList<Map<String, String>>();
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
			Map<String,String> map=new HashMap<String,String>();
			for(int j=0;j<headers.size();j++){
				if(log.isTraceEnabled()){
					log.trace("headers.size()= " + (headers == null ? "null" : headers.size()) + "; j=" + j + "; row=" + i + ", lastCellNum=" + (row == null ? "null" : row.getLastCellNum()));
		    		log.trace("headers:"+headers.get(j)+" "+row.getCell(j));
				}
				if(headers.get(j).length() == 0){
					//不需要的列
					continue;
				}
				Column col = null;
				for(int cIndex = 0; cIndex<columns.size(); cIndex++){
					if(columns.get(cIndex).getName().equals(headers.get(j))){
						col = columns.get(cIndex);
						break;
					}
				}
				if(col == null){
					if(log.isTraceEnabled()){
						log.trace("无法找到列" + headers.get(j));
					}
					continue;
				}
				Cell c = row.getCell(j);
				if(c != null){
					switch (c.getCellType()) { 
                        case HSSFCell.CELL_TYPE_FORMULA : 
                            addErrorMessage(i, headers.get(j), "此单元格是公式，不支持公式。");
                            v = c.getCellFormula();
                            break; 
                        case HSSFCell.CELL_TYPE_NUMERIC : 
                        	if(HSSFDateUtil.isCellDateFormatted(c)) {  
                        	    Date d = c.getDateCellValue();
                        	    v = dateFormater.format(d);
                        	}else{
                        		if(col.getType() == Column.STRING){
                        			//取字符类型数值
	                        		DataFormatter formater = new DataFormatter();
	                         		v = formater.formatCellValue(c); 
                        		}else{
                        			//取数值类型数值
                        			v = String.valueOf(c.getNumericCellValue());
                        		}
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
	}
	 
	/**
	 * 校验数据
	 * @return
	 */
	protected boolean validateExcelData(){
		//校验整个excel数据，是否存在必须的列缺少等
		if (columns != null && columns.size() > 0) {
			//检查是否有必须的列在excel中不存在
			for (int i = 0; i < columns.size(); i++) {
				if (columns.get(i).required) {
					if (!headers.contains(columns.get(i).header)) {
						addErrorMessage(-1, null, "“" + columns.get(i).header+ "”列是必需的。");
					}
				}
			}
		}else{
			if(log.isTraceEnabled()){
				log.trace("未设置列，所以不校验整体数据合法性。");
			}
		}
		if(getGlobalErrorMessage() != null){
			//Excel缺少必须的列，不再继续校验每行数据。
			if(log.isTraceEnabled()){
				log.trace("列数据不合法，不继续校验单独的行数据。");
			}
			return false;
		}
		
		if(data == null || data.size() == 0){
			addErrorMessage(-1, null, "Excel必须有数据");
			return false;
		}
		
		//调用子类的验证方法
		if(log.isTraceEnabled()){
			log.trace("需验证数据data" + data.size() + "行数据是否合法。");
		}
		for (int i = 0; i < data.size(); i++) {
			validateDataRow(data.get(i), i);
		}
		if (log.isTraceEnabled()) {
			log.trace("共校验" + data.size() + "行数据是否合法。");
		}
		return true;
	}
	
	/**
	 * 接收上传文件，并校验数据
	 * @return boolean 数据通过校验，返回TRUE，否则返回FALSE
	 * 
	 */
	public boolean uploadAndValidate(){
		saveFile();
		return readDataFromExcel();
	}
	private boolean readDataFromExcel(){
		try {
			if(log.isTraceEnabled()){
				log.trace("will read excel from " + excelFileName);
			}
			readExcel();
			if(log.isTraceEnabled()){
				log.trace("Excel read finished.");
				StringBuffer h = new StringBuffer();
				if(headers != null){
					for(int i=0; i<headers.size(); i++){
						h.append(headers.get(i));
						h.append(" , ");
					}
					log.trace("found " + headers.size() + " headers. " + headers);
				}else{
					log.trace("无headers");
				}
				log.trace("found " + data.size() + " data rows.");
			}
			return validateExcelData();
		} catch (Exception e) {
			if(log.isErrorEnabled()){
			    log.error("处理上传的Excel出错。", e);
			}
			addErrorMessageToContext("无法读取Excel文件，请确认文件是正确的Excel 2003格式文件。", jsfComponentId);
			return false;
		}
	}
	/**
	 * 读取Excel文件
	 * @throws FileNotFoundException 
	 */
	protected void readExcel() throws Exception{
		InputStream is = getUploadExcelInputStream();
		POIFSFileSystem fs = new POIFSFileSystem(is);  
		HSSFWorkbook wb = new HSSFWorkbook(fs);  
		HSSFSheet sheet = wb.getSheetAt(0);
		retrieveHeaders(sheet);
		retrieveData(sheet);
		is.close();
	}
	
	private InputStream getUploadExcelInputStream() throws IOException{
		File f = new File(uploadFolder, excelFileName);
		if(log.isTraceEnabled()){
			log.trace("read excel file " + f.getAbsolutePath() + "[uploadFolder=" + uploadFolder + "; excelFileName=" + excelFileName);
		}
		FileInputStream fis = new FileInputStream(f);
		return fis;
	}
	
	/**
	 * 显示一个包含所有数据的TABLE，用来预览或显示错误信息。
	 * @return
	 */
	public String getPreviewHtml(){
		StringBuffer html = new StringBuffer();
		if(data == null || data.size() == 0){
			if(log.isTraceEnabled()){
				log.trace("List Data not set");
			}
			return null;
		}
		
		html.append("<table cellspacing='1' class='datapreview' " + (htmlTableId == null ? "" : " id='" + htmlTableId + "'") + ">");
		if (headers != null && headers.size() > 0) {
			html.append("<thead><tr>");
			if(rowNum){
				html.append("<th>&nbsp;</th>");
			}
			for (int j = 0; j < columns.size(); j++) {
				String head = columns.get(j).getName();
				if(headers.contains(head)){
					html.append("<th");
					if(columns.get(j).getWidth() > 0){
						html.append(" width='" + columns.get(j).getWidth() + "'");
					}
					html.append(">");
					html.append(head);
					html.append("</th>");
				}
			}
			html.append("</tr></thead>\r\n");
		}
		html.append("<tbody>");
		for(int i=0; i<data.size(); i++){			
			boolean hasWarn = false;
			boolean hasError = false;
			StringBuffer rowHtml = new StringBuffer();
			Map<String, String> map = data.get(i);
			
			for (int h = 0; h < columns.size(); h++) {
				String key = columns.get(h).getName();
				if(!headers.contains(key)){
					continue;
				}
				rowHtml.append("<td");
				String warn = warnMessages.get(i + ":" + key);
				String error = errorMessages.get(i + ":" + key);
				String styleClass = columns.get(h).getStyleClass();
				if(warn != null){
					if(styleClass == null){
						styleClass = "warn";
					}else{
						styleClass += " warn";
					}
					hasWarn = true;
				}
				if(error != null){
					if(styleClass == null){
						styleClass = "error";
					}else{
						styleClass += " error";
					}
					hasError = true;
				}
				if(styleClass != null){
					rowHtml.append(" class=\"" + styleClass + "\"");
					rowHtml.append(" title=\"" + CommonUtil.htmlEncode((error != null ? error : "") + (warn != null ? warn : "")) + "\"");
				}
				rowHtml.append(">");
				rowHtml.append(CommonUtil.htmlEncode(map.get(key)));
				rowHtml.append("</td>");
			}
			html.append("<tr");
			String warn = warnMessages.get(i + ":");
			String error = errorMessages.get(i + ":");
			String styleClass = null;
			if(warn != null){
				hasWarn = true;
			}
			if(error != null){
				hasError = true;
			}
			if(hasWarn){
				styleClass = "warn";
			}
			if(hasError){
				styleClass = "error";
			}
			if(!hasWarn && !hasError){
				//没有错误，设置为pass
				styleClass = "pass";
			}
			if(styleClass != null){
				html.append(" class=\"" + styleClass + "\"");
			}
			if(error != null || warn != null){
				html.append(" title=\"" + CommonUtil.htmlEncode((error != null ? error : "") + (warn != null ? warn : "")) + "\"");
			}
			html.append(">");
			if(rowNum){
				html.append("<td class=\"rowno\">" + (i + 1) + ".</td>");
			}
			html.append(rowHtml);
			html.append("</tr>");
		}
		html.append("</tbody>");
		html.append("</table>");
		
		return html.toString();
	}
	
	public String getGlobalErrorMessage(){
		return errorMessages.get("");
	}
	public String getGlobalWarnMessage(){
		return warnMessages.get("");
	}
	public void addErrorMessage(int row, String columnKey, String message){
		addMessage(errorMessages, row, columnKey, message);
	}
	public void addWarnMessage(int row, String columnKey, String message){
		addMessage(warnMessages, row, columnKey, message);
	}
	private void addMessage(Map<String, String> map, int row, String columnKey, String message){
		if(log.isTraceEnabled()){
			log.trace(" 增加错误信息 " + row + ", " + columnKey + "  [" + message + "]");
		}
		if(row < 0){
			//全局性错误
			String msg = map.get("");
			if(msg != null){
				message = msg + "\r\n" + message;
			}
			map.put("", message);
		}else{
			if(columnKey == null){
				map.put(String.valueOf(row), message);
			}else{
				map.put(row + ":" + columnKey, message);
			}
		}
	}
	/**
	 * 保存上传的文件。保存后，会把文件名设置到excelFileName变量中。
	 * @return 文件名
	 */
	protected File saveFile(){
		if (log.isTraceEnabled()) {
			log.trace("saveFile(); item="
					+ (excelFile == null ? "null" : excelFile.getName()));
		}
		if (excelFile != null) {
			String fileName = excelFile.getName();
			int pos = fileName.lastIndexOf("\\");
			if (pos < fileName.lastIndexOf("/")) {
				pos = fileName.lastIndexOf("/");
			}
			fileName = fileName.toLowerCase();
			if (!(fileName.endsWith(".xls"))) {
				// 无效的格式
				addErrorMessageToContext("文件格式无效。", jsfComponentId);
				return null;
			}
			fileName = fileName.substring(pos + 1);
			
			try {
				File folder = new File(uploadFolder);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				File f = new File(folder, fileName);
				if (f.exists()) {
					// 保存到一个新文件
					int i = 1;
					pos = fileName.lastIndexOf(".");
					String extName = "";
					String name = fileName;
					if (pos > 0) {
						extName = fileName.substring(pos + 1);
						name = fileName.substring(0, pos);
					}
					while (f.exists()) {
						f = new File(folder, name + "_" + i + "." + extName);
						i++;
					}
				}
				InputStream in = excelFile.getInputStream();
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(f));
				byte[] buff = new byte[32 * 1024];
				int len;
				while ((len = in.read(buff)) > 0) {
					out.write(buff, 0, len);
				}
				in.close();
				out.close();
				excelFileName = f.getName();
				return f;
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("保存文件失败", e);
				}
				return null;
			}
		}
		return null;
	}
	
	/**
	 * 数据是否可以导入。如果没有错误信息即可导入。
	 * @return
	 */
	public boolean isImportEnabled(){
		if(log.isTraceEnabled()){
			log.trace("isImportEnabled() " + errorMessages.size() + " errors.");
		}
		return (errorMessages.size() == 0);
	}
	
	public boolean isWarning(){
		return (warnMessages.size() > 0);
	}
	
	/**
	 * 添加错误信息到context
	 */
	protected void addErrorMessageToContext(String error, String formId) {
		FacesContext context = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage();
		message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(error);
		context.addMessage(formId, message);
	}
	
	protected Date string2Date(){
		return null;
	}
	protected int string2Int(){
		return 0;
	}
	protected float string2Float(){
		return 0f;
	}
	
	/**
	 * 增加一个列
	 * @param index
	 * @param col
	 */
	public void addColumn(int index, Column col){
		if(columns == null){
			columns = new ArrayList<Column>();
		}
		columns.add(index, col);
	}
	public void addColumn(Column col){
		if(columns == null){
			columns = new ArrayList<Column>();
		}
		columns.add(col);
	}
	/**
	 * @return the dataHeader
	 */
	public List<Column> getColumns() {
		return columns;
	}
	
	
	public class Column{
		private String header;
		private int type;
		private boolean required;
		private String styleClass; //预览时的列的CSS
		private int width; //列预览时的宽度,单位：px
		 /**
		  * 字段的长度
		  */
		 private int length = 0;
		
		public static final int STRING = 1;
		public static final int NUMERIC = 2;
		public static final int DATE = 3;
		
		public Column(String name, int type, boolean required){
			this(name, type, required, null);
		}
		public Column(String name, int type, boolean required, String styleClass){
			this(name, type, required, null, 0,0);
		}
		public Column(String name, int type, boolean required, String styleClass, int width){
			this(name, type, required, null, width,0);
		}		
		public Column(String name, int type, boolean required, String styleClass, int width,int length){
			this.header = name;
			this.type = type;
			this.required = required;
			this.styleClass = styleClass;
			this.width = width;
			this.length = length;
		}

		/**
		 * @return the header
		 */
		public String getName() {
			return header;
		}

		/**
		 * @return the type
		 */
		public int getType() {
			return type;
		}

		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return required;
		}

		/**
		 * @return the styleClass
		 */
		public String getStyleClass() {
			return styleClass;
		}
		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}
		/**
		 * @param width the width to set
		 */
		public void setWidth(int width) {
			this.width = width;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		
	}


	public FileItem getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(FileItem excelFile) {
		this.excelFile = excelFile;
	}


	/**
	 * @return the excelFileName
	 */
	public String getExcelFileName() {
		return excelFileName;
	}


	/**
	 * @param excelFileName the excelFileName to set
	 */
	public void setExcelFileName(String excelFileName) {
		this.excelFileName = excelFileName;
		readDataFromExcel();
	}

	public void setExcelUploadReleatedFolder(String folder){
		this.uploadFolder = ThreadLocalManager.getContextFolder() + File.separator + "files"  + File.separator + folder;
	}


	/**
	 * @return the titleRowCount
	 */
	public int getTitleRowCount() {
		return titleRowCount;
	}


	/**
	 * @param titleRowCount the titleRowCount to set
	 */
	public void setTitleRowCount(int titleRowCount) {
		this.titleRowCount = titleRowCount;
	}
	
	
	/**
	 * 把当前的Excel分割成2个excel，一个是所有可以导入的(ok or warn)；一个是所有不可导入的(error)。
	 * 这2个打包成一个ZIP
	 * @param os
	 */
	public void splitErrorRows(OutputStream out) throws IOException{
		ZipOutputStream zip = new ZipOutputStream(out); 
		zip.setEncoding("GB2312");
		ArrayList<Integer> errorRows = new ArrayList<Integer>();
		for(String k : errorMessages.keySet()){
			String[] ary = k.split("[:]");
			k = ary[0];
			if(log.isTraceEnabled()){
				log.trace("splitErrorRows: errorMessage.keys #" + k + ", isNumber=" + CommonUtil.isNumber(k));
			}
			if(k.length() > 0 && CommonUtil.isNumber(k)){
				int row = Integer.parseInt(k);
				if(row >= 0){
					row += dataStartRow;
					errorRows.add(row);
				}
			}
		}
		if(log.isTraceEnabled()){
			log.trace("splitErrorRows:  发现" + errorRows.size() + "行错误数据；共" + (data==null ? 0 : data.size()) + "行数据。errorMessages.size()=" + errorMessages.size());
		}
		if(data == null || data.size() == 0 || errorRows.size() == 0){
			String msg = "no data 无数据。" + (errorRows.size() == 0 ? "未发现错误数据。" : "");
			zip.putNextEntry(new ZipEntry("error.txt"));
			zip.write(msg.getBytes());
			zip.closeEntry(); 
			zip.setComment(msg);
			zip.close();
			return;
		}
		
		//可以导入的部分
		zip.putNextEntry(new ZipEntry("可以导入的部分-" +excelFileName));
		InputStream isPass = getUploadExcelInputStream();
		POIFSFileSystem fsPass = new POIFSFileSystem(isPass);
		HSSFWorkbook wbPass = new HSSFWorkbook(fsPass);
		HSSFSheet sheetPass = wbPass.getSheetAt(0);
		for(int i=sheetPass.getLastRowNum() - omitLastRows; i>=dataStartRow; i--){
			if(errorRows.contains(i)){
				removeRow(sheetPass, i);
			}
		}
		wbPass.write(zip);
        zip.flush();
		zip.closeEntry();
		
		//无法导入的部分
		zip.putNextEntry(new ZipEntry("无法导入的部分-" + excelFileName));
		InputStream isFailed = getUploadExcelInputStream();
		POIFSFileSystem fsFailed = new POIFSFileSystem(isFailed);
		HSSFWorkbook wbFailed = new HSSFWorkbook(fsFailed);
		HSSFSheet sheetFailed = wbFailed.getSheetAt(0);
		for(int i=sheetFailed.getLastRowNum() - omitLastRows; i>=dataStartRow; i--){
			if(!errorRows.contains(i)){
				removeRow(sheetFailed, i);
			}
		}
		wbFailed.write(zip);
		zip.closeEntry();
		zip.close();
		
		isFailed.close();
		isPass.close();
	}
	
	/**
	 * 删除中的一行
	 * @param sheet
	 * @param rowIndex
	 */
	 private void removeRow(HSSFSheet sheet, int rowIndex) {   
	        int lastRowNum=sheet.getLastRowNum();   
	        if(rowIndex>=0&&rowIndex<lastRowNum){   
	            sheet.shiftRows(rowIndex+1,lastRowNum, -1);   
	        }   
	        if(rowIndex==lastRowNum){   
	            Row removingRow=sheet.getRow(rowIndex);   
	            if(removingRow!=null){   
	                sheet.removeRow(removingRow);   
	            }   
	        }   
	    } 
	/**
	 * 下载由splitErrorRows生成的zip
	 */
	public void downloadSplitedExcel(){
		//String contentType = "application/x-zip-compressed";
		String contentType = "application/zip";
//		String contentType = "application/binary";
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
		response.setContentType(contentType);
		StringBuffer contentDisposition = new StringBuffer();
		contentDisposition.append("attachment;filename=\"");
		String fileName = this.excelFileName + "拆分.zip";
		if(fileName.contains("/") || fileName.contains("\\")){
			int p = fileName.lastIndexOf('/');
			if(p < fileName.lastIndexOf('\\')){
				p = fileName.lastIndexOf('\\');
			}
			fileName = fileName.substring(p + 1);
		}
		
		contentDisposition.append(fileName);
		contentDisposition.append("\"");
		try{
			response.setHeader("Content-Disposition", new String(
					contentDisposition.toString().getBytes("gb2312"), "iso8859-1"));
			ServletOutputStream out = response.getOutputStream();
			splitErrorRows(out);
			ctx.responseComplete();
		}catch(Exception e){
			log.error("导出呆Excel失败。(" + fileName + ")", e);
		}
	}

	/**
	 * @return the rowNum
	 */
	public boolean isRowNum() {
		return rowNum;
	}

	/**
	 * @param rowNum the rowNum to set
	 */
	public void setRowNum(boolean rowNum) {
		this.rowNum = rowNum;
	}
}
