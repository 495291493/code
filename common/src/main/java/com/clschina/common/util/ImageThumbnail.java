package com.clschina.common.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;

import com.clschina.common.component.ThreadLocalManager;



public class ImageThumbnail {
	private final static Log log = LogFactory.getLog(ImageThumbnail.class);
	private static String imageMagickPath;
	static {
		// 这个必须写(ImageMagic的安装路径)
		imageMagickPath = ThreadLocalManager.getInstance().getConfigProperties().getProperty("imageMagickPath");
	}   
	
	/**
	 * 用ImageMagick生成缩略图
	 * @param srcPath	源文件
	 * @param destPath	目标文件
	 * @param width		宽
	 * @param height	高
	 * @param fixed		是否固定宽、高
	 * @param backgroundColor	源文件与固定宽高比例不匹配时补的背景色(默认：#ffffff)
	 * @throws Exception
	 */
	public static void makeThumbnail(String srcPath, String destPath,
			int width, int height, boolean fixed, String backgroundColor) throws Exception {
		IMOperation op = new IMOperation();
		if(!fixed){
			op.addImage(srcPath);
			op.resize(width, height);
			op.addImage(destPath);
		} else {
			//固定高宽并且目标图与原图比例不匹配，补背景色
			/*cmd:convert -size 100x100 xc:#ffffff e:\xh.jpg -resize 200x200 -gravity Center  -composite e:\5.jpg*/
			if(backgroundColor == null){
				backgroundColor = "#ffffff";
			}
			op.size(width, height);
			op.addImage("xc:" + backgroundColor);//补的背景色
		    op.addImage(srcPath);
		    op.resize(width, height);
		    op.gravity("Center");	//缩略图位于背景图的中间
		    op.composite();			//合并缩略后的图到背景图上
		    op.addImage(destPath);
		}
		
		ConvertCmd convert = new ConvertCmd();
		convert.setSearchPath(imageMagickPath);
		convert.run(op);
	}
	
	public static void makeThumbnail(String srcPath, String destPath,
			int width, int height) throws Exception {
		makeThumbnail(srcPath, destPath, width, height, false, null);
	}


	public static int[] getSize(InputStream is) throws Exception{
		BufferedImage img = ImageIO.read(is);  
		int[] sizes = new int[2];
        sizes[0] = img.getWidth();  
		sizes[1] = img.getHeight();
		return sizes;
	}
	/**
	 * 取一个图形的宽和高，返回数组，第1个元素是宽，第二个是高 
	 * @param imageFileName
	 * @return   int[]     returnValue[0] 是宽  returnValue[1] 是高
	 */
	public static int[] getSize(String imageFileName) throws Exception{
		Info info = new Info(imageFileName);
        int[] sizes = new int[2];
        sizes[0] = info.getImageWidth();  
		sizes[1] = info.getImageHeight();
		return sizes;
	}
	
	/**
	 * 制作缩略图
	 * @param imageFileName  	源文件，用绝对路径
	 * @param thumbWidth		最大的宽度，实际生成大小不会超过这个值
	 * @param thumbHeight		最大的高度，实际生成大小不会超过这个值
	 * @return 生成的缩略图文件，生成的缩略图文件名（和原文件放置在同一目录下）
	 * @throws Exception
	 */
	public static String makeThumbnail(String srcPath, 
			int thumbWidth, int thumbHeight) throws Exception{
		File dest = getDestFile(srcPath);
		makeThumbnail(srcPath, dest.getPath(), thumbWidth, thumbHeight, false, null);
		return dest.getName();
	}
	
	/**
	 * 制作固定宽高的缩略图，宽高不够的用背景色替代
	 * @param srcPath
	 * @param width
	 * @param height
	 * @param backgroundColor 背景色（例：#ffffff 要严格按照此写法）
	 * @return 小图文件名
	 */
	public static String makeFixedThumbnail(String srcPath, int width, int height, String backgroundColor)throws Exception{
		File dest = getDestFile(srcPath);
		makeThumbnail(srcPath, dest.getPath(), width, height, true, backgroundColor);
		return dest.getName();
	}
	
	private static File getDestFile(String srcPath){
		File source = new File(srcPath);
		File folder = source.getParentFile();
		String ext = "jpg";
		String name = source.getName().substring(0,source.getName().lastIndexOf("."));
		File thumbnail = new File(folder, name + ".thumbnail." + ext);
		int i=0;
		while (thumbnail.exists()){
			i++;
			thumbnail = new File(folder, name + ".thumbnail_" + i + "." + ext);
		}
		return thumbnail;
	}
}
