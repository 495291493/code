package com.clschina.common.util;

import java.util.Random;

public class WuliuUtil {
    /**
     * 宅急送zjs客户标识
     */
	public static  String zjsClientflag="FeiLi";
	/**
	 * 宅急送zjs密钥
	 */
	public static  String zjsStrSeed="D8B1B849-53D4-4312-B2CC-BC3B3D5F8F15";
	/**
	 * 宅急送zjs常量
	 */
	public static  String zjsStrConst="z宅J急S送g";
	/**
	 * 宅急送zjs查询跟踪地址
	 */
	public static  String zjsSearchUrl="http://edi.zjs.com.cn/svst/tracking.asmx";
	/**
	 * 宅急送zjs下单地址
	 */
	public static  String zjsOrderUrl = "http://edi.zjs.com.cn/svsr/receive.asmx";
	
	
	/**
	 * 随机生成指定长度的字符串
	 */
	public static String zjsRandomString(int length) {
        if (length < 1) {
            return null;
        }
        String basicchars="0123456789abcdefghijklmnopqrstuvwxy";
        //"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        Random randGen = new Random();
        char[] numbersAndLetters = basicchars.toCharArray();
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(basicchars.length()-1)];
        } 
        return new String(randBuffer);
   }		
	
	/**
	 * 生成验证数据
	 */
	public static String zjsGetVerifyData(String strData){
		String Verify=null;
		String rdm1=zjsRandomString(4);
		String rdm2 =zjsRandomString(4);
		String str = rdm1 + zjsClientflag + strData +zjsStrSeed + zjsStrConst + rdm2;
		MD5 md5 = new MD5();
		String strmd5 = md5.md5String(str.trim()).toLowerCase();
		Verify = rdm1+strmd5.substring(7, 28)+rdm2;
		return Verify;
	}		
	
	/**
	 * 查询跟踪获取请求的ＸＭＬ
	 * @param ccscode
	 * @return
	 */
	public static String getZjsSearchXml(String ccscode){
		StringBuffer xmlBuf = new StringBuffer();
		xmlBuf.append("<BatchQueryRequest><logisticProviderID>");
		xmlBuf.append(zjsClientflag);
		xmlBuf.append("</logisticProviderID><orders><order><mailNo>");
		xmlBuf.append(ccscode);
		xmlBuf.append("</mailNo></order></orders></BatchQueryRequest>");		
		return xmlBuf.toString();
	}		
	
	
}
