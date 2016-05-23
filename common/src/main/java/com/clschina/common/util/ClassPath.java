package com.clschina.common.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用于查找在某个ClassLoader范围的资源
 */
public final class ClassPath {

	private final static Log log = LogFactory.getLog(ClassPath.class);

	public ClassPath() {
		super();
	}

	/**
	 * 使用Thread的ClassLoader查找
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public static URL[] search(String prefix, String suffix) throws IOException {
        return search(Thread.currentThread().getContextClassLoader(), prefix,
                suffix);
	}

	public static URL[] search(ClassLoader cl, String prefix, String suffix) throws IOException {
		log.trace("search class path with prefix='" + prefix + "' and suffix='" + suffix + "'");
		//cl.getResources("META-INF/MANIFEST.MF")是为了取得所有的jar文件
		//有些jar文件可能不把目录打包进入。例如proguard混淆过的jar
		//Set all不能增加重复项目，可以过滤掉两个可能引起的重复
		Enumeration<?>[] e = new Enumeration[] {
				cl.getResources(prefix),
				cl.getResources("META-INF/MANIFEST.MF")
			};
		Set<URL> all = new LinkedHashSet<URL>();
		URL url;
		URLConnection conn;
		JarFile jarFile;
		for (int i = 0, s = e.length; i < s; ++i) {
			log.trace("process with e[" + i + "] ");
			while (e[i].hasMoreElements()) {
				url = (URL) e[i].nextElement();
				conn = url.openConnection();
				conn.setUseCaches(false);
				conn.setDefaultUseCaches(false);
				if (conn instanceof JarURLConnection) {
					jarFile = ((JarURLConnection) conn).getJarFile();
				} else {
					jarFile = getAlternativeJarFile(url);
				}
				if (jarFile != null) {
					searchJar(cl, all, jarFile, prefix, suffix);
				} else {
                    searchDir(all,
                    	      new File(URLDecoder.decode(url.getFile(), "UTF-8")),
                              suffix);
				}
			}
		}
		URL[] urlArray = (URL[]) all.toArray(new URL[all.size()]);
		return urlArray;
	}

    private static void searchDir(Set<URL> result, File file, String suffix)
            throws IOException {
		if (file.exists() && file.isDirectory()) {
			File[] fc = file.listFiles();
			String path;
			for (int i = 0; i < fc.length; i++) {
				path = fc[i].getAbsolutePath();
				if (fc[i].isDirectory()) {
					searchDir(result, fc[i], suffix);
				} else if (suffix == null || path.endsWith(suffix)) {
					// result.add(new URL("file:/" + path));
					result.add(fc[i].toURI().toURL());
				}
			}
		}
	}

    /** For URLs to JARs that do not use JarURLConnection - allowed by
     * the servlet spec - attempt to produce a JarFile object all the same.
     * Known servlet engines that function like this include Weblogic
     * and OC4J.
     * This is not a full solution, since an unpacked WAR or EAR will not
     * have JAR "files" as such.
	 */
	private static JarFile getAlternativeJarFile(URL url) throws IOException {
		String urlFile = url.getFile();
		// Trim off any suffix - which is prefixed by "!/" on Weblogic
		int separatorIndex = urlFile.indexOf("!/");

		// OK, didn't find that. Try the less safe "!", used on OC4J
		if (separatorIndex == -1) {
			separatorIndex = urlFile.indexOf('!');
		}

		if (separatorIndex != -1) {
			String jarFileUrl = urlFile.substring(0, separatorIndex);
			// And trim off any "file:" prefix.
			if (jarFileUrl.startsWith("file:")) {
				jarFileUrl = jarFileUrl.substring("file:".length());
			}
			return new JarFile(jarFileUrl);
		}
		return null;
	}

    private static void searchJar(ClassLoader cl, Set<URL> result, JarFile file,
            String prefix, String suffix) throws IOException {
		Enumeration<JarEntry> e = file.entries();
		JarEntry entry;
		String name;
		while (e.hasMoreElements()) {
			try {
				entry = (JarEntry) e.nextElement();
			} catch (Throwable t) {
				continue;
			}
			name = entry.getName();
			if ((prefix == null || name.startsWith(prefix)) && 
					(suffix == null || name.endsWith(suffix))) {
				Enumeration<URL> e2 = cl.getResources(name);
				while (e2.hasMoreElements()) {
					result.add(e2.nextElement());
				}
			}
		}
	}

}
