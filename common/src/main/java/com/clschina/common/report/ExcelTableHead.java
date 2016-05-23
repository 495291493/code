package com.clschina.common.report;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * 自己设置导出Excel中的表头
 * @author 
 *
 */
public abstract class ExcelTableHead {
	/**
	 * 自己设置导出Excel中的表头
	 * @param sheet
	 * @param workbook（传入此参数，是为了设置样式或什么的）
	 */
	public abstract void settingsExcelTableHead(WritableSheet sheet, WritableWorkbook workbook);
	/**
	 * 自己设置导出Excel中的表头
	 * @param sheet
	 * @param workbook（传入此参数，是为了设置样式或什么的）
	 */
	public abstract void settingsExcelTableHead(HSSFSheet sheet, HSSFWorkbook workbook);
	
	/**
	 * 创建行
	 * @param sheet
	 * @param index
	 * @return
	 */
	protected HSSFRow getHSSFRowByIndex(HSSFSheet sheet, int index){
		HSSFRow excelRow = sheet.getRow(index);
		if (excelRow == null) {
			excelRow = sheet.createRow(index);
		}
		return excelRow;
	}
	
	/**
	 * 创建列
	 * @param excelRow
	 * @param index
	 * @param cellValue
	 * @return
	 */
	protected HSSFCell getHSSFCellByIndex(HSSFRow excelRow, int index, String cellValue){
		HSSFCell cell = excelRow.getCell(index);
		if (cell == null) {
			cell = excelRow.createCell(index);
		}
		cell.setCellValue(cellValue);
		return cell;
	}
}
