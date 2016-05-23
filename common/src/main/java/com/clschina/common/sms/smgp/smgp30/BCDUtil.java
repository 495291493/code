package com.clschina.common.sms.smgp.smgp30;



public class BCDUtil {
	public static String bcd2Str(byte[] bytes){
		
	    StringBuffer temp=new StringBuffer(bytes.length*2);
	    for(int i=0;i<bytes.length;i++){
	     temp.append((byte)((bytes[i]& 0xf0)>>4));
	     temp.append((byte)(bytes[i]& 0x0f));
	    }
	    return temp.toString().substring(0,1).equalsIgnoreCase("0")?temp.toString().substring(1):temp.toString();
	    
	}
}
