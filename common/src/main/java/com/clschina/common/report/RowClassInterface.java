package com.clschina.common.report;


/**
 * 如果需要根据数据值，来显示不同的行css，需要实现此接口
 *
 */
public interface RowClassInterface {
	
	/**
	 * 返回table tr的classname，此clasname和原tr该有的放置在一起
	 * @param rowData 行数据
	 * @param row  行号
	 * @return null表示无单独的className;字符串表示这行的className
	 */
	public String getRowClass(Object[] rowData, int row);
}
