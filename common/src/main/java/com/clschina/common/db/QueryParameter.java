package com.clschina.common.db;

import org.hibernate.type.Type;



/**
 * 查询参数。参见DAOBase.queryTable(String entityName, List params, String orderBy, int first, int max);
 * @see DAOBase.queryTable
 * @author acer
 *
 */
public class QueryParameter {
	private String property, sign;
	private Object value;
	private Type type;
	
	public QueryParameter(){
		
	}
	/**
	 * @param property 属性名
	 * @param sign 符号，例如 > < >= like <= =等等
	 * @param value 值
	 */
	public QueryParameter(String property, String sign, Object value){
		this.property = property;
		this.sign = sign;
		this.value = value;
	}
	public QueryParameter(String property, String sign, Object value, Type type){
		this(property, sign, value);
		this.type = type;
	}
	
	public String getProperty() {
		return property;
	}
	/**
	 * 查询条件中的属性， 例如：where <b><i>SomeProperty</i></b>=:somevalue
	 * @param property
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	public String getSign() {
		return sign;
	}
	/**
	 * 查询条件，'>', '>=', '<', '<=', '='等
	 * @param sign
	 */
	public void setSign(String sign) {
		this.sign = sign;
	}
	public Object getValue() {
		return value;
	}
	/**
	 * 值
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	public Type getType() {
		return type;
	}
	
	/**
	 * 设置,当hibernate无法正确自动识别getValue中的object时，需要设置type。
	 * 一般当参数值的类是hibernate定义文件中定义的类的子类时，hibernate无法正确识别。
	 * @param type
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
}
