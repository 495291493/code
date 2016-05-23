package com.clschina.common.datasync;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.component.ThreadLocalManager;

/**
 * 上传文件的发送文件
 * @author ch
 *
 */
public class DataSender  {
	/**
	 * 日志
	 */
	private static Log log = LogFactory.getLog(DataSender.class);
	public static final String HTTP_METHOD_GET = "GET";	 
	public static final String HTTP_METHOD_POST = "POST";
	
	
	public boolean send(String receiverServletUrl, String[] sql, List<String> list){  
        File rootFolder = null;
        try{
        	rootFolder = ThreadLocalManager.getInstance().getFilesFolder();
        }catch(Exception e){
        	log.error("没有配置上传文件所在路径。", e);
        	return false;
        }
        try {  
            String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线  
            URL url = new URL(receiverServletUrl);  
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
            // 发送POST请求必须设置如下两行  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            conn.setUseCaches(false);  
            conn.setRequestMethod(HTTP_METHOD_POST);  
            conn.setRequestProperty("connection", "Keep-Alive");  
//            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");  
            conn.setRequestProperty("Charsert", "UTF-8");   
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);  
              
            OutputStream out = new DataOutputStream(conn.getOutputStream());  
            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
            for(int i=0; i<sql.length; i++){
            	StringBuilder sb = new StringBuilder();
            	sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
            	sb.append("Content-Disposition:form-data;name=\"sql\"\r\n\r\n");
            	sb.append(sql[i]);
            	sb.append("\r\n");
            	byte[] data = sb.toString().getBytes();  
                out.write(data);
            }
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					String fname = list.get(i);
					File file = new File(rootFolder, fname);
					if(log.isTraceEnabled()){
						log.trace("上传文件 " + file.getAbsolutePath());
					}
					StringBuilder sb = new StringBuilder();
					sb.append("--");
					sb.append(BOUNDARY);
					sb.append("\r\n");
					sb
							.append("Content-Disposition: form-data;name=\"file\";filename=\""
									+ fname + "\"\r\n");
					sb.append("Content-Type:application/octet-stream\r\n\r\n");

					byte[] data = sb.toString().getBytes();
					out.write(data);
					DataInputStream in = new DataInputStream(
							new FileInputStream(file));
					int bytes = 0;
					byte[] bufferOut = new byte[1024];
					while ((bytes = in.read(bufferOut)) != -1) {
						out.write(bufferOut, 0, bytes);
					}
					out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
					in.close();
				}
			}
            out.write(end_data);  
            out.flush();    
            out.close();   
              
            // 定义BufferedReader输入流来读取URL的响应  
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
            String line = null;
            StringBuffer responseText = new StringBuffer();
            while ((line = reader.readLine()) != null) {  
            	responseText.append(line);
            	responseText.append("\r\n");
            }
            if(log.isTraceEnabled()){
            	log.trace("返回数据:\r\n" + responseText);
            }
            if(responseText.toString().startsWith("ok")){
            	return true;
            }else{
            	return false;
            }
        } catch (Exception e) {  
        	if(log.isErrorEnabled()){
        		StringBuffer msg = new StringBuffer();
				if (list != null && list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						msg.append("#" + i + ": " + list.get(i) + "; ");
					}
				}
        		msg.append("sql=" + sql + ";");
        		log.error("同步数据失败" + receiverServletUrl + ", " + msg, e);
        	}
            return false;
        }  
    }  
	
	public static void main(String[] args) throws Exception{
		DataSender uploader = (new DataSender());
		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		list.add("/java/Ttt.java");
		list.add("/logo/S241.gif");
		list.add("0z/IPAD-网页头.jpg");
		uploader.send("http://localhost:8080/feiliadmin/servlet/filereceiver", new String[]{"UPDATE city set name='' where id='xyz'"}, list);
	}

}
