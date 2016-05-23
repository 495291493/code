package com.clschina.common.util;

import java.util.Calendar;

public class UFBaseUtil {
	public enum ZT {
		DZSW, QLH, XXJS, HAKN, BZW, FL
	}
	public static String ztFormat(ZT zt) {
		return ztFormat(zt, getZtYear());
	}
	public static String ztFormat(ZT zt, int year) {
		if (zt == ZT.DZSW) {
			return "666";
		} else if (zt == ZT.HAKN) {
			return "805";
		} else if (zt == ZT.QLH) {
			return "803";
		} else if (zt == ZT.XXJS) {
			return "802";
		} else if (zt == ZT.BZW) {
			return "805";
		} else if (zt == ZT.FL) {
			return "808";
		}
		return null;
	}

	/**
	 * 是否电子商务帐套
	 * 
	 * @param zt
	 * @return
	 */
	public static boolean isDzswZt(String zt) {
		return zt == null ? false : (zt.indexOf("666") != -1 || zt.indexOf("777") != -1);
	}
	
	/**
	 * 电子商务账套编号
	 * @return
	 */
	public static String getDzswZtBh(){
		return ztFormat(ZT.DZSW, getZtYear());
	}
	

	public static String getDzswZT(int year) {
		return getZT(ZT.DZSW, year);
	}

	public static String getDzswZT() {
		return getZT(ZT.DZSW);
	}

	public static String getDzswZTLastYear() {
		return getZTLastYear(ZT.DZSW);
	}

	public static String getXxjsZT(int year) {
		return getZT(ZT.XXJS, year);
	}

	public static String getXxjsZT() {
		return getZT(ZT.XXJS);
	}

	public static String getXxjsZTLastYear() {
		return getZTLastYear(ZT.XXJS);
	}

	public static String getHaknZT(int year) {
		return getZT(ZT.HAKN, year);
	}

	public static String getHaknZT() {
		return getZT(ZT.HAKN);
	}

	public static String getHaknZTLastYear() {
		return getZTLastYear(ZT.HAKN);
	}

	public static String getBZWZT(int year) {
		return getZT(ZT.BZW, year);
	}

	public static String getBZWZT() {
		return getZT(ZT.BZW);
	}

	public static String getBZWZTLastYear() {
		return getZTLastYear(ZT.BZW);
	}

	public static String getQlhZT(int year) {
		return getZT(ZT.QLH, year);
	}

	public static String getQlhZT() {
		return getZT(ZT.QLH);
	}

	public static String getQlhZTLastYear() {
		return getZTLastYear(ZT.QLH);
	}
	
	public static String getFlZT(int year) {
		return getZT(ZT.FL, year);
	}

	public static String getFlZT() {
		return getZT(ZT.FL);
	}
	public static String getFlZTLastYear() {
		return getZTLastYear(ZT.FL);
	}

	private static String getZT(ZT bh, int year) {
		return "UFDATA_" + ztFormat(bh, year) + "_" + year;
	}

	public static String getZT(ZT bh) {
		return getZT(bh, getZtYear());
	}

	public static String getZTLastYear(ZT bh) {
		return getZT(bh, getZtYear() - 1);
	}

	public static String getZT(String bh) {
		ZT zt = ZT.valueOf(bh);
		return getZT(zt, getZtYear());
	}

	public static String getZTLastYear(String bh) {
		ZT zt = ZT.valueOf(bh);
		return getZT(zt, getZtYear() - 1);
	}

	public static int getZtYear() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR);
	}
}
