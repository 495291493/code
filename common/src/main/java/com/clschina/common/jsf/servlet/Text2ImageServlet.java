package com.clschina.common.jsf.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.Text2ImageCache;
import com.clschina.common.util.VerifyCode;

public class Text2ImageServlet extends HttpServlet {
	private static final long serialVersionUID = -7765158001602032735L;
	private static Log log=LogFactory.getLog(Text2ImageServlet.class);
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession  session = request.getSession();   
		//PrintWriter out=response.getWriter();
		 String s = "";
		    String id = request.getParameter("id");
		    //String randomnumber = request.getParameter("rnd");
		    String width = request.getParameter("width");
		     
		    if(id != null && id.trim().length()>0){
		    	s = Text2ImageCache.getInstance().getText2uuid().get(id);
		    }
		    if(s == null || s.trim().length() == 0){
		    	s = "NULL";
		    }
		    VerifyCode code = new VerifyCode();
		  	BufferedImage buffimg = code.makeImage(s,width);   
				//session.setAttribute("validateCode", s);
				//禁止图像缓存
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
				
				try {
					ServletOutputStream sos = response.getOutputStream();
					ImageIO.write(buffimg, "jpeg", sos);
					sos.close();
				/*	out.clear();
					out=pageContext.pushBody();*/
				} catch (IOException e) {
					if(log.isErrorEnabled()){
						log.error("图片输出错误", e);
					}
				}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}