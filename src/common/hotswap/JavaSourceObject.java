package common.hotswap;

import java.io.File;
import java.net.URI;
import java.nio.CharBuffer;
import javax.tools.SimpleJavaFileObject;

/**
 * java源文件对象
 * 
 */
public class JavaSourceObject extends SimpleJavaFileObject {
	
	final String source;
	
	JavaSourceObject(String name, String source) {
		super(toURI(name), Kind.SOURCE);
		this.source = source;
	}
	
	@Override
	public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
		return CharBuffer.wrap(source);
	}
	
	static final String JAVA_FILE_EXT = ".java";
	static URI toURI(String name) {
		File file = new File(name);
		if (file.exists()) {
			return file.toURI();
		}
		try {
			final StringBuilder newUri = new StringBuilder();
			newUri.append("file:///");
			newUri.append(name.replace('.', '/'));
			if (name.endsWith(JAVA_FILE_EXT)) {
				newUri.replace(newUri.length() - JAVA_FILE_EXT.length(), newUri.length(), JAVA_FILE_EXT);
			}
			return URI.create(newUri.toString());
		} catch (Exception exp) {
			return null;
		}
	}

}
