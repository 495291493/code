package com.clschina.common.util;

import java.io.File;
import java.net.URL;
import java.util.Comparator;



/**
 * 用于比较两个文件名，进行排序，可以使用File，也可以使用文件名String<br/>
 * 所有的比较仅仅是比较文件名，而不比较路径。例如/aaa/zzz.jar会排在/zzz/aaa.jar的后面，因为参与比较的是'zzz.jar'和'aaa.jar'。
 * for example:<br/>
 * Collections.sort(List<File>, new FileNameComparator()); <br/>
 * Collections.sort(List<String>, new FileNameComparator());<br/>
 * Collections.sort<List<URL>, new FileNameComparator());<br/>
 * 如果List<Object>进行比较，则会调用Object.toString()进行比较
 * 
 */
public class FileNameComparator implements Comparator<Object> {


	/**
	 * @see java.util.Comparator.compare
	 */
	public int compare(Object o1, Object o2) {
		String f1, f2;
		if(o1 instanceof File){
			f1 = ((File) o1).getName();
		}else if(o1 instanceof URL){
			f1 = (new File(((URL) o1).getFile())).getName();
		}else{
			f1 = new File(o1.toString()).getName();
		}
		if(o2 instanceof File){
			f2 = ((File) o2).getName();
		}else if(o1 instanceof URL){
			f2 = (new File(((URL) o2).getFile())).getName();
		}else{
			f2 = new File(o2.toString()).getName();
		}
		return f1.compareTo(f2);
	}
}
