package com.clschina.common.util;


import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;



/*
import java.util.Locale;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
*/

/**
 * 常用的一些函数,比如字符串处理,日期比较等等
 */
public class CommonUtil {
	private final static Log log = LogFactory.getLog(CommonUtil.class);

	/**
	 * 根据给定的参数生成一个日期型对象
	 * @param year  年
	 * @param month 月 （注意：1月为0，2月为1 ... 12月是11）
	 * @param day 日
	 * @return Date型对象
	 */
	public static Date generateDate(int year, int month, int day){
		return generateDate(year, month, day, 0, 0, 0, 0);
	}
	/**
	 * 根据给定的参数生成一个日期型对象
	 * @param year  年
	 * @param month 月 （注意：1月为0，2月为1 ... 12月是11）
	 * @param day 日
	 * @param hour 时
	 * @param minute 分
	 * @param second 秒
	 * @return Date型对象
	 */
	public static Date generateDate(int year, int month, int day, int hour, int minute, int second){
		return generateDate(year, month, day, hour, minute, second, 0);
	}
	
	/**
	 * 根据给定的参数生成一个日期型对象
	 * @param year  年
	 * @param month 月 （注意：1月为0，2月为1 ... 12月是11）
	 * @param day 日
	 * @param hour 时
	 * @param minute 分
	 * @param second 秒
	 * @param ms 毫秒
	 * @return Date型对象
	 */
	public static Date generateDate(int year, int month, int day, int hour, int minute, int second, int ms){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.AM_PM, Calendar.AM);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, ms);
		return c.getTime();
	}
    /**
     * compare two given date(ignor hour/minute...)
     * @param d1 Date
     * @param d2 Date
     * @return int 0 same day; 1 d1>d2; -1 d1 < d2
     */
    public static int dateCompare(Date d1, Date d2) {
        Date d11 = clearTime(d1);
        Date d22 = clearTime(d2);
        long ret = d11.getTime() - d22.getTime();
        if (ret < 0) {
            return -1;
        } else if (ret > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 把一个指定的日期型object的时间部分设置成0。
     * @param d
     * @return
     */
    public static Date clearTime(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        clearTime(c);
        return c.getTime();
    }
    public static void clearTime(Calendar c) {
        c.set(Calendar.HOUR, c.getActualMinimum(Calendar.HOUR));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
        c.set(Calendar.AM_PM, Calendar.AM);
    }
    /**
     * 把一个collection类型变成一个逗号分隔的字符串
     * @param c
     * @return
     */
    public static String collection2String(Collection<?> c){
        if (c == null){
            return "";
        }
        String ret = "";
        for(Iterator<?> kids = c.iterator(); kids.hasNext();){
            Object o = kids.next();
            if (o instanceof Date){
                ret += ((Date) o).getTime() + ",";
            }else{
                ret += o.toString() + ",";
            }
        }
        return ret;
    }

    /**
     * 把逗号分隔的字符串，转化成为日期型数组,逗号分隔的字符串每个都是date.getTime()返回的毫秒数
     * @param s
     * @return
     */
    public static Vector<Date> string2DateVector(String s){
        Vector<String> sv = string2StringVector(s);
        Vector<Date> dv = new Vector<Date>(sv.size());
        for(Iterator<?> kids = sv.iterator(); kids.hasNext();){
            Object o = kids.next();
            try{
                long l = Long.parseLong((String) o);
                dv.add(new Date(l));
            }catch(Exception e){
                //do nothing.
            }
        }
        return dv;
    }
    /**
     * 逗号分隔的字符串转化成字符串数组
     * @param s
     * @return
     */
    public static Vector<String>  string2StringVector(String s){
        Vector<String>  v = new Vector<String> ();
        if (s==null){
            return v;
        }
        String ary[] = s.split(",");
        for(int i=0; i<ary.length; i++){
            if (ary[i].trim().length() > 0){
                v.add(ary[i]);
            }
        }
        return v;
    }

    /**
     * 在一个字符串内，查找并替换
     * @param str  源字符串
     * @param pattern 要查找的字符串
     * @param replace 替换的字符串
     * @return 替换完成后的新字符串
     */
    public static String replace(String str, String pattern, String replace) {
        if (str == null) {
        	return "";
        }
        int s = 0, e = 0;
        StringBuffer result = new StringBuffer((int) str.length() * 2);
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * 把一个object类型变量转化成boolean型。如果是1,t,y,true,yes则转化为true，否则false
     * @param o
     * @return
     */
	public static boolean object2boolean(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof Number) {
			if (((Number) o).byteValue() == 0) {
				return false;
			} else {
				return true;
			}
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		} else {
			String str = o.toString();
			if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("true")
					|| str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("t")
					|| str.equalsIgnoreCase("y")) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * object类型变量转化为int型。不能转化返回0
	 * @param o
	 * @return
	 */
	public static int object2int(Object o) {
		try {
			Number n = (Number) o;
			return n.intValue();
		} catch (Exception e) {
            try{
                return Integer.parseInt(o.toString());
            }catch(Exception e2){
                return 0;
            }
		}
	}
	
	/**
	 * object类型变量转化为long型。不能转化的返回0
	 * @param o
	 * @return
	 */
	public static long object2long(Object o) {
		try {
			Number n = (Number) o;
			return n.longValue();
		} catch (Exception e) {
            try{
                return Long.parseLong(o.toString());
            }catch(Exception e2){
                return 0;
            }
		}
	}


    /*	
    public static Object getBackingBean(String backingBeanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return getBackingBean(context, backingBeanName);
    }

    public static Object getBackingBean(FacesContext context, String backingBeanName) {
        ApplicationFactory factory = (ApplicationFactory) FactoryFinder.
                                     getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application app = factory.getApplication();
        ValueBinding vb = app.createValueBinding(backingBeanName);
        if (vb != null) {
            return vb.getValue(context);
        } else {
            return null;
        }
    }

    
    public static String getRealPath(String file){
        try{
            FacesContext context = FacesContext.getCurrentInstance();
            String contextPath = context.getExternalContext().
                                 getRequestContextPath();
            ServletContext servletContext = (ServletContext) context.
                                            getExternalContext().getContext();

            return servletContext.getRealPath(file);
        }catch (Exception e){
            return null;
        }
    }

    public static String getContextPath() {
        FacesContext context = FacesContext.getCurrentInstance();
        String contextPath = context.getExternalContext().getRequestContextPath();
        HttpServletRequest request = (HttpServletRequest) context.
                                     getExternalContext().getRequest();
        return request.getContextPath();
    }

    public static Locale getLocale(){
        Locale _locale = Locale.getDefault();
        try{
            _locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        }catch (Exception e){
        }
        return _locale;
    }
    */

    /**
     * 把一段文字转化为可以在html中显示的文字，相当于htmlEncode(),并且保留了以前的换行
     * @param text
     * @return
     */
    public static String text2html(String text){
        String html = text;
        html = html.replaceAll("&", "&amp;");
        html = html.replaceAll("<", "&lt;");
        html = html.replaceAll(">", "&gt;");
        html = html.replaceAll("\n", "<br>");
        return html;
    }    
    /**
     * html encode
     * @param text
     * @return
     */
    public static String htmlEncode(String text){
        String s = replace(text, "&", "&amp;");
        s = replace(s, "<", "&lt;");
        s = replace(s, ">", "&gt;");
        s = replace(s, "\"", "&quot;");
        return s;
    }
    /**
     * xml encode
     * @param text
     * @return
     */
    public static String xmlEncode(String text){
    	if(text == null || text.length()==0)
    		return "";
        String s = replace(text, "&", "&amp;");
        s = replace(s, "<", "&lt;");
        s = replace(s, ">", "&gt;");
        s = replace(s, "\"", "&quot;");
//        s = replace(s, "'", "&apos;");
        return s;
    }

    /**
     * 把set类型变量转化为list类型
     * @param set
     * @return
     */
    public static List<?> set2list(Set<?> set){
    	List<Object> list = new ArrayList<Object>();
    	for(Iterator<?> its = set.iterator(); its.hasNext();){
    		list.add(its.next());
    	}
    	return list;
    }
    
    /**
     * 在一个字符串中找到所有的表达式.表达式以 expressBegin开始,以expressEnd结尾
     * @param str
     * @param expressBegin
     * @param expressEnd
     * @return 找到的表达式的集合.
     */
    public static Set<String> findAllExpress(String str, String expressBegin, String expressEnd){
    	Set<String> set = new HashSet<String>();
    	int beginPosition = 0;
    	beginPosition = str.indexOf(expressBegin);
    	while(beginPosition >= 0){
    		int endPosition;
    		endPosition = str.indexOf(expressEnd, beginPosition + expressBegin.length());
    		if(endPosition > 0){
    			String express = str.substring(beginPosition + (expressBegin.length()), endPosition);
    			if(!set.contains(express)){
    				set.add(express);
    			}
    		}else{
    			break;
    		}
    		beginPosition = str.indexOf(expressBegin, endPosition + expressEnd.length());
    	}
    	return set;
    }

    /**
     * 把日期转化成字符串, 尽可能的使用比较短的日期格式.
     * @param d
     * @return
     */
    public static String toShortDateString(Date d){
    	return toShortDateString(d, "KK:mm", "MM/dd", "yyyy/MM/dd");
    }
    /**
     * 把日期转化为字符串<br>
     * <ul>
     * <li>当日期是当天时,使用第一个日期表达式
     * <li>当日期是同年时,使用第二个日期表达式
     * <li>当日期不满足上述两个条件时,使用第3个表达式
     * </ul>
     * @param d
     * @param todayPattern 
     * @param sameYearPattern
     * @param defaultPattern
     * @return
     */
    public static String toShortDateString(Date d, String todayPattern, String sameYearPattern, String defaultPattern){
        if (d==null){
            return "N/A";
        }
        
        Calendar c = Calendar.getInstance();
        String pattern;
        
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d);
        if(c2.get(Calendar.YEAR) == c.get(Calendar.YEAR) &&
        		c2.get(Calendar.MONTH) == c.get(Calendar.MONTH) &&
        		c2.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)){
        	//today.
        	pattern = todayPattern;
        }else if(c2.get(Calendar.YEAR) == c.get(Calendar.YEAR)){
        	//same year.
        	pattern = sameYearPattern;
        }else{
        	//all default.
        	pattern = defaultPattern;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(d);
    }
    
    /**
     * 把秒转化为 h:mm:ss的格式来显示时间长度
     * @param seconds
     * @return
     */
	public static String seconds2timeString(int seconds){
		int h = seconds / 3600;
		int m = (seconds - (h * 3600)) / 60;
		int s = seconds % 60;
		StringBuffer buf = new StringBuffer();
		if(h<10){
			buf.append("0" + h);
		}else{
			buf.append("" + h);
		}
		buf.append(":");
		if(m<10){
			buf.append("0");
		}
		buf.append(m + ":");
		if(s<10){
			buf.append("0");
		}
		buf.append(s + "");
		return buf.toString();
	}

	/**
	 * 把html转化为纯文本
	 * @param html
	 * @return
	 */
	public static String html2text(String html){
		try{
			StringBuffer buf = new StringBuffer();
	        StringReader sr = new StringReader(html);
	        int c = sr.read();
	        boolean inTags = false;
	        while(c != -1){
	        	if(c == '<'){
	        		inTags = true;
	        	}else if(inTags && c == '>'){
	        		inTags = false;
	        		c = sr.read();
	        		continue;
	        	}
	        	if(!inTags){
	        		buf.append((char) c);
	        	}
	        	c = sr.read();
	        }
	        sr.close();	
	        sr = null;
	        return buf.toString();
		}catch(Exception e){
			log.warn("cannot change html '" + html + "' to text. " + e, e);
			return html;
		}
	}
	
	/**
	 * 根据某些关键字,对字符串进行切割
	 * @param str
	 * @param keywords
	 * @param length
	 * @return
	 */
	public static String cutStringWithKeywords(String str, String[] keywords, int length){
		if(str.length() <= length){
			return str;
		}
		int first = 0;
		for(int i=0; i<keywords.length; i++){
			int pos = str.indexOf(keywords[i]);
			if(pos >= 0 && pos < first){
				first = pos;
			}
		}
		if(first > (length / 3)){
			first = first - (length / 3);
		}else{
			first = 0;
		}
		if((first + length) > str.length()){
			first = str.length() - length;
		}
		StringBuffer buf = new StringBuffer();
		if(first > 0){
			buf.append("...");
		}
		buf.append(str.substring(first, first + length));
		if((first + length) < str.length()){
			buf.append("...");
		}
		return buf.toString();
	}
	
	/**
	 * 通过html语法,加量显示某些关键字
	 * @param str
	 * @param keywords
	 * @param css
	 * @return
	 */
	public static String highLightKeywords(String str, String[] keywords, String css){
		String ret = str;
		for(int i=0; i<keywords.length; i++){
			ret = replace(ret, keywords[i], "<span class=\"" + css + "\">" + keywords[i] + "</span>");
		}
		return ret;
	}
	
	/**
	 * 把querystring类型的encode的字符串,转化为变量和值的集合
	 * @param queryString
	 * @return
	 */
	public static Map<String, String> querystring2map(String queryString){
		Map<String, String> map = new HashMap<String, String>();
		if(queryString == null){
			return map;
		}

		String[] ary = queryString.split("&");
		for(int i=0; i<ary.length; i++){
			String[] ary2 = ary[i].split("=");
			if (ary2.length == 2){
				String key = ary2[0];
				String value = ary2[1];
				try {
					key = URLDecoder.decode(key, "UTF-8");
					value = URLDecoder.decode(value, "UTF-8");
//					key = URLDecoder.decode(key, "ISO-8859-1");
//					value = URLDecoder.decode(value, "ISO-8859-1");
//					key = new String(key.getBytes("ISO-8859-1"), "utf-8");
//					value = new String(value.getBytes("ISO-8859-1"), "utf-8");
					map.put(key, value);
				} catch (UnsupportedEncodingException e) {
					log.error("error while url decode.", e);
					map.put(key, value);
				}
			}
		}
		return map;
	}
	
	/**
	 * 从一个resourcebundle读取字符串，和rb.getString()相比，只是处理了MissingResourceException
	 * 异常，当发生此异常时，返回 ???+key+???，以免程序出错，会在界面上看到未找到字符串。
	 * @param rb
	 * @param key
	 * @return
	 */
	public static String getResourceString(ResourceBundle rb, String key){
		String value;
		try{
			value = rb.getString(key);
		}catch(MissingResourceException e){
			value = "???" + key + "???";
		}
		return value;
	}
	
	/**
	 * 把一个字符串做URLEncode，替代java.net.URLEncoder.encode(url,"utf-8")，因为
	 * java.net.URLEncoder会把' '变成'+'，而浏览器不认识
	 * @param url
	 * @return
	 */
	public static String urlEncoder(String url){
		String u;
		
		try {
			u = URLEncoder.encode(url, "UTF-8");
			// ' ' to '%20' instead of '+'
			u = u.replace("+", "%20");
			// keep '+' as it is
			u = u.replace("%2B", "+");
		} catch (UnsupportedEncodingException e) {
			log.error("this VM doesnot support UTF-8 encoding.", e);
			u = url;
		}
		
		return u;
	}
	
	/**
	 * 把某个字符串转化为可以用作javascript中的字符串
	 * 转化的字符包括 '''->'\'' '"'->'\"' '\n'->'' '\r'->''
	 * 
	 * @param s
	 * @return
	 */
	public static String jsEncode(String s){
		String s2 = replace(s, "'", "\\'");
		s2 = replace(s2, "\"", "\\\"");
		s2 = replace(s2, "\n", "\\n");
		s2 = replace(s2, "\r", "\\r");
		return s2;
	}
	
	/**
	 *	格式化金额，将金额四舍五入
	 * @param money  金额
	 * @param wei	  保留的小数位数
	 * @return
	 */
	public static double formatMoney(double value, int wei){
		return new BigDecimal(String.valueOf(value)).setScale(wei,
                RoundingMode.HALF_UP).doubleValue();
	}
	
	public static double formatNumber(double value, int wei){
		double v = new BigDecimal(String.valueOf(value)).setScale(wei+1, RoundingMode.HALF_UP).doubleValue();
		return new BigDecimal(String.valueOf(v)).setScale(wei, RoundingMode.HALF_UP).doubleValue();
	}
	
	/**
	 * 判断传入的字符串是否是整数数字
	 * @param s
	 * @return
	 */
	public static boolean isNumber(String s){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(s);
		return isNum.matches();
	}
	/**
	 * 根据userAgent参数取得客户端OS
	 * @param userAgent
	 * @return
	 */
	public static String getOS(String userAgent){
		if(userAgent == null){
			return "";
		}else if(userAgent.indexOf("Windows NT 5.0") >= 0){
			return "Windows 2000";
		}else if(userAgent.indexOf("Windows NT 5.1") >= 0){
			return "XP";
		}else if(userAgent.indexOf("Windows NT 5.2") >= 0){
			return "Windows 2003";
		}else if(userAgent.indexOf("Windows NT 6.0") >= 0){
			return "Vista";
		}else if(userAgent.indexOf("Windows NT 6.1") >= 0){
			return "Win 7";
		}else if(userAgent.indexOf("Windows") >= 0){
			return "windows";
		}else if(userAgent.indexOf("Mac OS") >= 0){
			return "Mac OS";
		}else if(userAgent.indexOf("Linux") >= 0){
			return "Linux";
		}else{
			return "其它";
		}
	}
	
	/**
	 * 根据userAgent参数取得客户端浏览器
	 * @param userAgent
	 * @return
	 */
	public static String getBrowser(String userAgent){
		if(log.isTraceEnabled()){
			log.trace("userAgent: " + userAgent);
		}
		if(userAgent == null){
			return "";
		}else if(userAgent.indexOf("KeFu") >= 0){
			return "客服系统(" + getNavigatorCore(userAgent) + ")";
		}else if(userAgent.indexOf("TencentTraveler") >= 0){
			return "腾讯(" + getNavigatorCore(userAgent) + ")";
		}else if(userAgent.indexOf("Maxthon") >= 0){
			return "傲游(" + getNavigatorCore(userAgent) + ")";
		}else if(userAgent.indexOf("360") >= 0){
			return "360浏览器(" + getNavigatorCore(userAgent) + ")";
		}else if(userAgent.indexOf("SE") >= 0){
			return "搜狗(" + getNavigatorCore(userAgent) + ")";
		}else if(userAgent.indexOf("TheWorld") >= 0){
			return "世界之窗(" + getNavigatorCore(userAgent) + ")";
		}else if(userAgent.indexOf("Chrome") >= 0){
			return "Chrome";
		}else if(userAgent.indexOf("Opera") >= 0){
			return "Opera";
		}else if(userAgent.indexOf("Firefox") >= 0){
			return "Firefox";
		}else if(userAgent.indexOf("Safari") >= 0){
			return "Safari";
		}else{
			String core = getNavigatorCore(userAgent);
			return core.contains("MSIE") ? core :  ("其它(" + core  + ")");
		}
	}
	private static String getNavigatorCore(String agent){
		if(agent.indexOf("Trident/7.") >= 0){
			return "IE11";
		}else if(agent.indexOf("Trident/6.") >= 0){
            return "IE10";
        }else if(agent.indexOf("Trident/5.") >= 0){
			return "IE9";
		}else if(agent.indexOf("Trident/4.") >= 0){
			return "IE8";
		}else if(agent.indexOf("MSIE 7") >= 0){
			return "IE7";
		}else if(agent.indexOf("MSIE 6") >= 0){
			return "IE6";
		}else if(agent.indexOf("Trident") >= 0){
			return "IE";
		}else if(agent.indexOf("AppleWebKit") >= 0){
			return "WebKit";
		}else if(agent.indexOf("Gecko") >= 0){
			return "Gecko";
		}else{
			return "";
		}
	}
	
	public static String encodeSQLString(String s) {
		if(s == null){
			return "";
		}
		return CommonUtil.replace(s, "'", "''");
	}
	public static String encodeSQLDate(Calendar c) {
		if(c == null){
			return "NULL";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return "'" + sdf.format(c.getTime()) + "'";
	}
	public static String encodeSQLSimpleDate(Calendar c) {
		if(c == null){
			return "NULL";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "'" + sdf.format(c.getTime()) + "'";
	}
	
	public static String confuseAddress(String addr){
		StringBuffer newAddr = new StringBuffer();
		for(int i=0; i<addr.length(); i++){
			char c = addr.charAt(i);
			if(c == '村' || c == '镇' || c == '乡' || c == '县' || c == '庄' ){
				newAddr.append(c);
			}else if(c == '号' || c=='弄' || c== '號' || c== '层' || c== '楼' || c== '室'){
				if(i > 0){
					newAddr.deleteCharAt(i-1);
					newAddr.append(Math.random() > 0.5 ? "**" : "*");
				}
				newAddr.append(c);
			}else if("1234567890１２３４５６７８９０".contains(c + "")){
				newAddr.append("*");
			}else if(i>addr.length() - 6){
				newAddr.append("*");
			}else{
				newAddr.append(c);
			}
		}
		return newAddr.toString();
	}
	
	/**
	* 测试该小写转换大写比较好,可以采用
	* by friend 2008/03/19 
	* 将小写的人民币转化成大写
	*/
	public static String convertToChineseNumber(double number){
		StringBuffer chineseNumber = new StringBuffer();
		String [] num={"零","壹","贰","叁","肆","伍","陆","柒","捌","玖"};
		String [] unit = {"分","角","元","拾","佰","仟","万","拾","佰","仟","亿","拾","佰","仟","万"};
		String tempNumber = String.valueOf(Math.round((number * 100)));
		int tempNumberLength = tempNumber.length();
		if ("0".equals(tempNumber)){
		    return "零元整";
		}
		if (tempNumberLength > 15){
		    try {
		        throw new Exception("超出转化范围.");
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		boolean preReadZero = true;    //前面的字符是否读零
		for (int i=tempNumberLength; i>0; i--){
		    if ((tempNumberLength - i + 2) % 4 == 0){
		        //如果在（圆，万，亿，万）位上的四个数都为零,如果标志为未读零，则只读零，如果标志为已读零，则略过这四位
		        if (i - 4 >= 0 && "0000".equals(tempNumber.substring(i - 4, i))) {
		            if (!preReadZero){
		                chineseNumber.insert(0, "零");
		                preReadZero = true;
		            }
		            i -= 3;    //下面还有一个i--
		            continue;
		        }
		        //如果当前位在（圆，万，亿，万）位上，则设置标志为已读零（即重置读零标志）
		        preReadZero = true;
		    }
		    int digit = Integer.parseInt(tempNumber.substring(i - 1, i));
		    if (digit == 0) {
		        //如果当前位是零并且标志为未读零，则读零，并设置标志为已读零
		        if (!preReadZero){
		            chineseNumber.insert(0, "零");
		            preReadZero = true;
		        }
		        //如果当前位是零并且在（圆，万，亿，万）位上，则读出（圆，万，亿，万）
		        if ((tempNumberLength - i + 2) % 4 == 0) {
		            chineseNumber.insert(0, unit[tempNumberLength - i]);
		        }
		    }
		    //如果当前位不为零，则读出此位，并且设置标志为未读零
		    else{
		        chineseNumber.insert(0, num[digit] + unit[tempNumberLength - i]);
		        preReadZero = false;
		    }
		}
		//如果分角两位上的值都为零，则添加一个“整”字
		if (tempNumberLength - 2 >= 0 && "00".equals(tempNumber.substring(tempNumberLength - 2, tempNumberLength))){
		    chineseNumber.append("整");
		}
		return chineseNumber.toString();
	} 
	
	public static double getRound(double dSource,int precision){
		double dRound;
		String d = String.valueOf(dSource);
//		BigDecimal的构造函数参数类型是double
		BigDecimal deSource = new BigDecimal(d);
//		deSource.setScale(0,BigDecimal.ROUND_HALF_UP) 返回值类型 BigDecimal
//		intValue() 方法将BigDecimal转化为int
		dRound= deSource.setScale(precision,BigDecimal.ROUND_HALF_UP).doubleValue();
		return dRound;
	}
	
	/**
	 * 查找一个地址所属的区域
	 * @param address
	 * @return
	 */
	public static String getAddressArea(String address){
		if(address.length()>5){
			address = address.substring(0, 5);
		}
		String val = null;	
		Pattern patt = Pattern.compile("(安徽|北京|福建|甘肃|广东|广西|贵州|海南|河北|河南|黑龙江|湖北|湖南|" +
				"吉林|江苏|江西|辽宁|内蒙古|宁夏|青海|山东|山西|陕西|上海|四川|天津|西藏|新疆|云南|浙江|重庆)+");
		Matcher m = patt.matcher(address);
		if(m.find()){
			val = m.group(1);
		}
		return val;
	}
	
	/**设置金额的显示格式为美式金额，每三位用逗号隔开
	 * @param dSource
	 * @return
	 */
	public static String getFormatMoney(double dSource,int precision){
		String strFormat = "##,###,###,###,##0";
		if(precision > 0){
			strFormat += ".";
			for(int i=0; i<precision; i++){
				strFormat+="0";
			}
		}
		DecimalFormat   fmt   =   new   DecimalFormat(strFormat);     
        String outStr = null;
        outStr = fmt.format(dSource);
        return outStr;
	}
	
	
	
}
