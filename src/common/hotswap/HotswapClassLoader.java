package common.hotswap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 热加载类加载器
 * 
 * @author Nate
 */
public class HotswapClassLoader extends URLClassLoader implements AutoCloseable {
	
	public HotswapClassLoader(String classPath, ClassLoader parent) {
		super(toURLs(classPath), parent);
	}

	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		//先从我们编译后的class文件缓存里面找
		ScriptBytes sb = ScriptCompiler.getScriptBytes(className);
		if (sb != null) {
			return defineClass(className, sb.getBytes(), 0, sb.getBytes().length);
		}
		return super.findClass(className);
	}
	
	private static URL[] toURLs(String classPath) {
		if (classPath == null) {
			return new URL[0];
		}
		
		List<URL> list = new ArrayList<URL>();
		StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			File file = new File(token);
			if (file.exists()) {
				try {
					list.add(file.toURI().toURL());
				} catch (MalformedURLException mue) {}
			} else {
				try {
					list.add(new URL(token));
				} catch (MalformedURLException mue) {}
			}
		}
		
		URL res[] = new URL[list.size()];
		list.toArray(res);
		return res;
	}

}
