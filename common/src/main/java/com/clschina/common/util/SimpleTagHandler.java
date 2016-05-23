package com.clschina.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleTagHandler implements UBBTagHandler {
	private final static Log log = LogFactory.getLog(SimpleTagHandler.class);

	// [b]文字加粗体效果[/b]
	// [i]文字加倾斜效果[/i]
	// [u]文字加下划线效果[/u]
	// [size=4]改变文字大小[/size]
	// [color=red]改变文字颜色[/color]
	// [quote]这个标签是用来做为引用所设置的，如果你有什么内容是引用自别的地方，请加上这个标签！[/quote]
	// [url]http://www.cnjm.net[/url]
	// [url=http://www.cnjm.net]JAVA手机网[/url]
	// [email=webmaster@cnjm.net]写信给我[/email]
	// [email]webmaster@cnjm.net[/email]
	// [img]http://www.cnjm.net/myimages/mainlogo.gif[/img]

	public SimpleTagHandler() {
	}

	public String[] parseTag(String s, boolean isEmpty) {
		if (isEmpty) { // 本处理器不支持空标签
			return null;
		}
		// 如果标签中有'='号就把标签分为UBB标记和属性两部分，否则属性为null
		String tag = s, attr = null;
		int idx = s.indexOf('=');
		if (idx >= 0) {
			tag = s.substring(0, idx);
			attr = s.substring(idx + 1);
		}
		String tmp = tag.toLowerCase(); // 大小写不敏感
		// 只有下面的标记是本处理器支持的
		if ("b".equals(tmp) || "i".equals(tmp) || "u".equals(tmp)
				|| "size".equals(tmp) || "color".equals(tmp)
				|| "quote".equals(tmp) || "url".equals(tmp)
				|| "email".equals(tmp) || "img".equals(tmp)) {
			return new String[] { tag, attr };
		}
		// 不是一个合法的UBB标签，作为普通文本处理
		return null;
	}

	public String compose(String tag, String attr, String data, boolean isEmpty) {
		// 针对不同标记进行组合工作
		String tmp = tag;
		if ("b".equals(tmp) || "i".equals(tmp) || "u".equals(tmp)) {
			return "<" + tag + ">" + data + "</" + tag + ">";
		} else if ("size".equals(tmp) || "color".equals(tmp)) {
			return "<font " + tag + "='" + attr + "'>" + data + "</font>";
		} else if ("quote".equals(tmp)) {
			StringBuffer buf = new StringBuffer();
			if (attr == null) {
				log.trace("quote without name");
				buf.append("<div style='margin: 10px 30px 10px 30px;"
								+ " background-color:#EFEFEF; border:1px solid #D7D7D7; "
								+ "padding:7px;'>" + data + "</div>");
			} else {
				log.trace("quote with name");
				buf.append("<div style='margin: 10px 30px 10px 30px;'><b>"
								+ attr
								+ "</b>:<div style='margin:0px;  "
								+ "background-color:#EFEFEF; border:1px solid #D7D7D7; "
								+ " padding:7px;'>" + data + "</div></div>");
			}
			return buf.toString();
		} else if ("url".equals(tmp)) {
			String url = attr != null ? attr : data;
			return "<a href='" + url + "' target=_blank>" + data + "</a>";
		} else if ("email".equals(tmp)) {
			String email = attr != null ? attr : data;
			return "<a href='mailto:" + email + "'>" + data + "</a>";
		} else if ("img".equals(tmp)) {
			return "<img src='" + data + "' border=0>";
		}
		return data;
	}

}
