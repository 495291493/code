package com.clschina.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
	
	/**
	 * 转化日期为字符(今天，昨天，前天，3天前，3天以后则显示日期xx月xx日);
	 * @param cal
	 * @return
	 */
	public static String convertDate2String (Calendar date) {
		Calendar current = Calendar.getInstance();
		int currentDayNum = current.get(Calendar.DAY_OF_YEAR);
		int dayNum = date.get(Calendar.DAY_OF_YEAR);
		SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
		
		if (date.get(Calendar.YEAR) == current.get(Calendar.YEAR)) {
			
			int num = currentDayNum - dayNum;
			String str = null;
			
			switch (num) {
			case 0 : str = "今天"; break;
			case 1 : str = "昨天"; break;
			case 2 : str =  "前天"; break;
			default : 
				str = sdf.format(date.getTime());
			}
			
			return str;
			
		} else {
			
			return sdf.format(date.getTime());
			
		}
	}
	
	/**
	 * 设置一日期时间到一天的开始
	 * @param date
	 */
	public static void dayTimeBegin(Calendar date) {
	    CommonUtil.clearTime(date);
	}
	
	/**
	 * 设置日期日间到一天的结束
	 * @param date
	 */
	public static void dayTimeEnd(Calendar date) {
		date.set(Calendar.HOUR_OF_DAY, 23);
		date.set(Calendar.MINUTE, 59);
		date.set(Calendar.SECOND, 59);
		date.set(Calendar.MILLISECOND, 00);
	}
	/**
	 * 一年的开始时间
	 * @param date
	 */
	public static void dayYearBegin(Calendar date) {
		date.set(Calendar.MONDAY, 0);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 00);
	}
	/**
	 * 设置日期时间到月的开始
	 * @param date
	 */
	public static void monthTimeBegin(Calendar date) {
		date.set(Calendar.DAY_OF_MONTH, date.getMinimum(Calendar.DAY_OF_MONTH));
		dayTimeBegin(date);
	}
	/**
	 * 设置上月第一天开始时间
	 * @param date
	 */
	public static Calendar premonthTimeBegin(Calendar date) {
		date.add(Calendar.MONTH, -1);
		date.set(Calendar.DAY_OF_MONTH, date.getMinimum(Calendar.DAY_OF_MONTH));
		dayTimeBegin(date);
		return date;
	}
	/**
	 * 设置上月最后一天线束时间
	 * @param date
	 */
	public static Calendar premonthTimeEnd(Calendar date) {
		date.add(Calendar.MONTH, -1);
		date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
		dayTimeEnd(date);
		return date;
	}
	
	/**
	 * 设置日期时间到月的结尾
	 * @param date
	 */
	public static void monthTimeEnd(Calendar date) {
		date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
		dayTimeEnd(date);
	}
	
	
	/**
	 * 将日期换算为分钟
	 * @param date
	 * @return
	 */
	public static int minute(Calendar date) {
		return date.get(Calendar.HOUR_OF_DAY) * 60 + date.get(Calendar.MINUTE);
	}
	
	/**
	 * 计算两个日期间的分钟数
	 * @param start
	 * @param end
	 * @return
	 */
	public static int minute(Calendar start, Calendar end) {
		int startMinute = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE);
		int endMinute = end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE);
		return endMinute - startMinute;
	}
}
