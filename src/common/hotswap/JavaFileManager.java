package common.hotswap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

/**
 * Java文件管理器
 * 
 */
public class JavaFileManager extends EclipseFileManager {
	
	/** 编译后的class文件集合 */
	private List<ScriptBytes> compileds = new ArrayList<>();
	
	public JavaFileManager() {
		super(null, null);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
			throws IOException {
		if (kind == Kind.CLASS) {
			ScriptBytes clz = new ScriptBytes(className.replace('/', '.'));
			compileds.add(clz);
			return clz;
		}
		return super.getJavaFileForOutput(location, className, kind, sibling);
	}
	
	public List<ScriptBytes> getCompiledClass() {
		return this.compileds;
	} 
}
