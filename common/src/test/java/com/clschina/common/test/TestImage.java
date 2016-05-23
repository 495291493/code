package com.clschina.common.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.ImageThumbnail;

public class TestImage extends TestCase {
	Log log = LogFactory.getLog(TestImage.class);
	
	public void testImageThumbnail(){
		try{
//			ImageThumbnail.makeThumbnail("src/test/resources/defaultleftimage.jpg"
//					,"d:/dest.jpg", 300,280, true, "#000");
//			int size[] = ImageThumbnail.getSize("D:\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\saofenbaovendor\\files\\goods\\temporarysave\\00020005560x560_5.jpg");
//			log.trace("size:" + size[0]);
			
		}catch(Exception ex){
			log.error("///ERROR:"+ex.getMessage(), ex);
		}
	}
}
