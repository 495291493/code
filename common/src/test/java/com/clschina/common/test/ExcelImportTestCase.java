package com.clschina.common.test;

import java.io.FileOutputStream;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.backingbean.AbstractExcelImportBackingBean;
import com.clschina.common.backingbean.AbstractExcelImportBackingBean.Column;


public class ExcelImportTestCase extends TestCase {
	private static Log log = LogFactory.getLog(ExcelImportTestCase.class);

	public void testSplitExcel() throws Exception{
/*		
		AbstractExcelImportBackingBean eib = new AbstractExcelImportBackingBean(){

			@Override
			protected void validateDataRow(Map<String, String> row, int rowNo) {
				if(rowNo % 2 == 0){
					addErrorMessage(rowNo, null, "EVEN ROW ERROR");
				}
			}
			
			@Override
			public void setExcelUploadReleatedFolder(String folder){
				super.uploadFolder = "src/test/resources";
			}
		};
		Column c = eib. new Column("序号", Column.NUMERIC, true, "presentid");
		eib.addColumn(c);
		c = eib.new Column("订单编号", Column.STRING, true, "cinvcode");
		eib.addColumn(c);
		eib.setExcelUploadReleatedFolder(null);
		eib.setExcelFileName("导入样例.xls");
		log.trace("will save split file to target/split.zip");
		FileOutputStream os = new FileOutputStream("target/拆分-导入样例.zip");
		eib.splitErrorRows(os);
		os.flush();
		os.close();
*/	}
}
