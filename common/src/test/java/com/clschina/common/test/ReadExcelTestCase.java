package com.clschina.common.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

public class ReadExcelTestCase extends TestCase {
	private static Log log = LogFactory.getLog(ReadExcelTestCase.class);

	public void testRead() {

	}

	private static void splitExcel() throws Exception{
		FileInputStream sourceExcel = new FileInputStream("src/test/resources/导入样例.xls");
		POIFSFileSystem fs = new POIFSFileSystem(sourceExcel);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		FileInputStream s2 = new FileInputStream("src/test/resources/导入样例.xls");
		POIFSFileSystem fs2 = new POIFSFileSystem(s2);
		HSSFWorkbook wb2 = new HSSFWorkbook(fs2);
		HSSFSheet sheet2= wb2.getSheetAt(0);
		for(int i=sheet.getLastRowNum(); i>5; i--){
			if(i % 2 == 0){
				removeRow(sheet, i);
			}else{
				removeRow(sheet2, i);
			}
		}
		FileOutputStream os = new FileOutputStream("target/sample-odd.xls");
		wb.write(os);
        os.flush();
        
        FileOutputStream os2 = new FileOutputStream("target/sample-even.xls");
		wb2.write(os2);
        os2.flush();
	}
	 public static void removeRow(HSSFSheet sheet, int rowIndex) {   
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
	public static void main(String[] args) throws Exception {
		splitExcel();
		if(true){
			System.out.println("completed");
			return;
		}
		FileInputStream fis = new FileInputStream(
				"d:/workspace/common/src/test/resources/2rowhead.xls");
		POIFSFileSystem fs = new POIFSFileSystem(fis);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		HSSFRow row = sheet.getRow(5);
		HSSFCell c1 = row.getCell(1); //num
		HSSFCell c2 = row.getCell(2); //date
		System.out.println("getDataFormat = " + c1.getCellStyle().getDataFormat());
		System.out.println("getDataFormatString = " + c1.getCellStyle().getDataFormatString());
		System.out.println("getDataFormatString = " + c1.getCellStyle().getDataFormatString(wb));
		DataFormatter formatter = new DataFormatter();
		System.out.println("THE VALUE of c1 " + formatter.formatCellValue(c1));
		System.out.println("THE VALUE of c2 " + formatter.formatCellValue(c2));
		
		ArrayList<CellRangeAddress> ranges = new ArrayList<CellRangeAddress>();
		for(int i=0; i<sheet.getNumMergedRegions(); i++){
			ranges.add(sheet.getMergedRegion(i));
		}
		ArrayList<String> headers = new ArrayList<String>();

		int titleRow = 0;
		int titleRowCount = 2;
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
						//System.out.println("range, row=" + range.getFirstRow() + "," + range.getLastRow() + ", col:" + range.getFirstColumn() + "," + range.getLastColumn());
						if(range.getFirstColumn() < i && i <= range.getLastColumn()){
							c = rows[k].getCell(range.getFirstColumn());
						}
					}
				}
				head += c.getStringCellValue().trim();
			}
			System.out.println("#" + i +  "," + "" + " " + head);
		}
		
		
	}

}
