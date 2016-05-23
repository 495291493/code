package com.clschina.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.CommonUtil;


/**
 * 供 Velocity 模板调用的，用于格式化的类
 * 
 */
public class DataFormaterTool {
	private static Log log = LogFactory.getLog(DataFormaterTool.class);
	
	/** yyyy/MM/dd HH:mm:ss 格式时间*/
	public  static final String YYYMMDDHHMMSS = "yyyy/MM/dd HH:mm:ss";
	
	/** yyyy-MM-dd HH:mm:ss 格式时间*/
	public  static final String YYYY_MM_DDHHMMSS = "yyyy-MM-dd HH:mm:ss";
	
	/** yyyy/MM/dd 格式时间*/
	public  static final String YYYYMMDD = "yyyy/MM/dd";
	
	/** yyyy-MM-dd 格式时间*/
	public static final String YYYY_MM_DD = "yyyy-MM-dd";

	/**
	 * 格式化日期
	 * 
	 * @param format
	 * @param date
	 * @return
	 */
	public String formatDate(String format, Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * 格式化日期"3日 星期一" 前3天 分别显示 今天 明天 后天
	 * @param date
	 * @return
	 */
	public String formatDateOnlyDay(Date date) {
		if (date == null) {
			return "";
		}
		Calendar calN = Calendar.getInstance();
		Calendar calC = Calendar.getInstance();
		calC.setTime(date);
		int day = calC.get(Calendar.DAY_OF_YEAR) - calN.get(Calendar.DAY_OF_YEAR);
		log.trace(calC.get(Calendar.DAY_OF_YEAR)  +"              "+calN.get(Calendar.DAY_OF_YEAR));
		switch (day) {
		case 0:
			return "今天";
		case 1:
			return "明天";	
		case 2:
			return "后天";
		case -1:
			return "昨天";
		case -2:
			return "前天";
		default:
			SimpleDateFormat sdf = new SimpleDateFormat("dd日");
			return formatDayNofZero(sdf.format(date),"&nbsp;&nbsp;");			
		}	
	}
	
	/**
	 * 日期 日去掉前面0补空格
	 * '01日'转' 1日'
	 * @return
	 */
	public String formatDayNofZero(String date,String format){
		if (date == null) {
			return "";
		}
		if(date.substring(0,1).equals("0")){
			String dateStr = format+date.substring(1);
			return dateStr;
		}else {
			return date;
		}
	}
	
	/**
	 * 日期 日去掉前面0补空格
	 * '01月01日'转' 1月1日'
	 * @return
	 */
	public String formatDayNofZeroMMDD(String date,String format){
		if (date == null) {
			return "";
		}
		if(date.substring(3,4).equals("0")){
			date =date.substring(0,3)+ format+date.substring(4);
		}	
		if(date.substring(0,1).equals("0")){
			date= format+date.substring(1);
		}
		return date;
	}
	
	/**
	 * 日期 日去掉前面0补空格
	 * '2007年01月02日'转'2007年1月2日'
	 * @return
	 */
	public String formatDateNofZeroFull(String date,String format){
		if(date.substring(8,9).equals("0")){			
			date =date.substring(0,8)+ format+date.substring(9);
		}	
		if(date.substring(5,6).equals("0")){
			date =date.substring(0,5)+ format+date.substring(6);
		}	

		return date;
	}
	/**
	 * 格式化日期(传入日期字符串)
	 * 转成（xxxx年xx月xx日）
	 * @param dateStr
	 * @return
	 */
	public String formatDateOnlyDay(String dateStr) {    		
		if (dateStr == null) {
			return "";
		}
		try {   
			Long time=Long.parseLong(dateStr);		
			SimpleDateFormat fm1 = new SimpleDateFormat("yyyy年MM月dd日"); 			
		    return  fm1.format(time);
		} catch (Exception ex) {   
			log.error("日期转换异常",ex);
		}   
		return null;
	}
	

	/**
	 * 格式化日期(传入日期字符串)
	 * 任意格式
	 * @param dateStr
	 * @return
	 */
	public String formatDateOnlyDay(String dateStr,String format) {    		
		if (dateStr == null) {
			return "";
		}
		try {   
			Long time=Long.parseLong(dateStr);		
			SimpleDateFormat fm1 = new SimpleDateFormat(format); 			
		    return  fm1.format(time);
		} catch (Exception ex) {   
			log.error("日期转换异常",ex);
		}   
		return null;
	}
	 /**
     * 格式化日期星期(中文)
     * 
     * @return
     */
    public String formatWeekday(Date date){
    	Calendar cal=Calendar.getInstance();
    	cal.setTime(date);
    	return formatDate("EEE",date);
    }
	
    public String formatWeekday(String date){
    	Long time=Long.parseLong(date);	
    	Date d=null;
		try {
			d = new Date(time);
		} catch (Exception ex) {
			log.error("日期转换异常",ex);
		}
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(d);
    	return formatDate("EEE", calendar.getTime());
    }
	/**
	 * 格式化数字
	 * 
	 * @param format
	 * @param n
	 * @return
	 */
	public String formatNumber(String format, Object n) {
//	    if (log.isTraceEnabled()) {
//            log.trace("formatNumber(" + format + ", " + n + ")");
//        }
        if (n == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat(format);
        if(n instanceof String){
            double d = Double.parseDouble((String) n);
            return df.format(d);
        }else{
            return df.format(n);
        }
	}
	/**
	 * 把一个字符串中的换行转成 <br />
	 * 同时把html字符也编码。
	 * 
	 * @param obj
	 * @return
	 */
	public String multiLineConvert(Object obj) {
		if (log.isTraceEnabled()) {
			log.trace("multiLineConvert called");
		}
		if (obj == null) {
			return "";
		}
		String orignal = obj.toString();
		String html = CommonUtil.htmlEncode(orignal);
		html=CommonUtil.replace(html, "\r\n", "\n");
		html=CommonUtil.replace(html, "\r", "\n");
        html=CommonUtil.replace(html, "\n", "<br/>");
        html=CommonUtil.replace(html, "  ", "　");//2个英文空格的转成1个中文空格
        html=CommonUtil.replace(html, " ", "&nbsp;");
        return html;
	}
	
	/**
	 * 把一个字符串中的换行转成 <br />
	 * 同时把html字符也编码。
	 * 
	 * @param obj
	 * @return
	 */
	public String multiLineConvertNoToNabsp(Object obj) {
		if (log.isTraceEnabled()) {
			log.trace("multiLineConvert called");
		}
		if (obj == null) {
			return "";
		}
		String orignal = obj.toString();
		String html = orignal;		
        html=CommonUtil.replace(html, "\r\n", "<br/>");
        html=CommonUtil.replace(html, "\r", "<br/>");
        html=CommonUtil.replace(html, "  ", "　");//2个英文空格的转成1个中文空格
        html=CommonUtil.replace(html, "\"", "\'");
        html=html.replace("<P>", "");
        html=html.replace("</P>", "");
        return html;
	}

	public String jsEncode(String s) {
		return CommonUtil.jsEncode(s);
	}
	public String htmlEncode(String s) {
        return CommonUtil.htmlEncode(s);
    }
	public int parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception ex) {
			return 0;
		}
	}

	public String urlEncode(String url) {
		if (url == null) {
			return "";
		}
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("encoding " + url + "failed", e);
			return url;
		}
	}

	/**
	 * 把手机号码 13512345678 转成 135-1234-5678的格式
	 * 
	 * @param s
	 * @return
	 */
	public String formatMobileNumber(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() == 11) {
			return s.substring(0, 3) + "-" + s.substring(3, 7) + "-"
					+ s.substring(7);
		} else {
			return s;
		}
	}

	/**
	 * 把物流单号格式化
	 * */
	public StringBuffer formatWuLiuNumber(String s) {
		int c = 0;
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < s.length() / 4; i++) {
			strBuf.append(s.substring(c, c + 4) + "<span></span>");
			c = c + 4;
		}
		if (s.length() % 4 != 0) {
			strBuf.append(s.substring(s.length() - s.length() % 4));
		}
		return strBuf;
	}
	
	/**
	 * 得到某个时间到当前天间隔天数
	 * 
	 * @param date
	 * @return
	 */
	public long diffDate(String date) {
		long dayDiff = 0;
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		  //给定的时间
		try {
			Date d = format.parse(date);
		  //当前时间处理
		  Calendar cal = Calendar.getInstance();
		  //给定时间处理
		  Calendar setCal = Calendar.getInstance();
		  setCal.setTime(d);
		  dayDiff =(cal.getTimeInMillis()-setCal.getTimeInMillis())/(1000*60*60*24);
		} catch (ParseException e) {
			log.error("date error", e);
		}
		  return dayDiff;
	}
	
	public String formatJR(String str){
		if(str.length()==2){
			str=str.charAt(0)+"&nbsp;&nbsp;"+str.charAt(1);
		}
		return str;
	}
	
	/**
	 * 格式化得到百分比，并保留两位小数100.00%
	 * @param numParam
	 *            项参数
	 * @param totalParam
	 *            总数参数
	 * @return  返回项参数所占比率
	 */
	public static String formatBaifeibi(int numParam, int totalParam) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        String k = df.format(numParam * 100.00 / totalParam) + "%";
        return k;
    }
	
	/**
     * 格式化日期
     * 
     * @param format
     * @param date
     * @return
     */
    public String formatDate(String dateStr, String format) {
        if(dateStr == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return this.formatDate(format, sdf.parse(dateStr));
        } catch (ParseException e) {
            if(log.isErrorEnabled()){
                log.error("["+ dateStr +"时间]解析失败:", e);
            }
        }
        return null;
    }
}
