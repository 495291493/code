package com.clschina.common.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class VerifyCode {
	private static Log log= LogFactory.getLog(VerifyCode.class);
	public BufferedImage makeImage(String s,String widths){
		if("".equals(s)){
			s = "	";
		}
		int width = 100;
		int height = 100;
		if(widths !=null && widths.trim().length()>0){
			try{
				width = Integer.parseInt(widths) ;
			}catch(Exception e){
				if(log.isErrorEnabled()){
					log.error("数字类型转换错误 ", e);
				}
				width = 100;
			}
			width = width >= 10 ? width : 100;	
		}
		
		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, width, height);
		g2.setPaint(Color.BLACK);
		
		
		AttributedString attribString = new AttributedString(s);
		AttributedCharacterIterator attribCharIterator;
		attribString.addAttribute(TextAttribute.FOREGROUND, Color.BLACK, 0, s
				.length());
		attribString.addAttribute(TextAttribute.FONT, new Font("Serif",
				Font.PLAIN, 12), 0, s.length());
		attribCharIterator = attribString.getIterator();

		FontRenderContext frc = new FontRenderContext(null, false, false);
		LineBreakMeasurer lbm = new LineBreakMeasurer(attribCharIterator, frc);

		int x = 3; // 左右各留3个像素余白
		int y = 0;

		while (lbm.getPosition() < s.length()) {
			TextLayout layout = lbm.nextLayout(width - x * 2); // 左右留余白
			y += layout.getAscent();
			layout.draw(g2, x, y);
			y += layout.getDescent() + layout.getLeading();
		}
		if (y == 0) {
			y = 10;
		}
		BufferedImage bi2 = bi.getSubimage(0, 0, width, y);
		return bi2;
	}

	
	
	
	
	
	
	
	
	
}
