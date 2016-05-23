package com.clschina.common.report;


public interface CellDataConverterInterface {

	public Object htmlCellConverter(Object data, int column, int row);
	
	public Object excelCellConverter(Object data, int column, int row, Object[] rowData);
}
