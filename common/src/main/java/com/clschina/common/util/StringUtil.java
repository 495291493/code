package com.clschina.common.util;

import java.sql.SQLException;

import org.hibernate.lob.SerializableClob;

public class StringUtil {
	
	public static boolean isNullOrEmpty(String str){
		return (str == null || str.trim().length() == 0);
	}
	
	public static String convertToString(Object o){
		if(o == null)
			return null;
		if(o instanceof String){
			return (String)o;
		}else if(o instanceof Integer){
			return o.toString();
		}else if(o instanceof SerializableClob){
			SerializableClob clob = (SerializableClob) o ;
			try {
				return clob.getSubString(1, (int) clob.length());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return o.toString();
	}
	
}
