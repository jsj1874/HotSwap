package common.hotswap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * 脚本编译器，编译后，所有class字节码保存到内存中
 * 
 */
public class ScriptCompiler {
	/** eclipse编译器实现 */
	private static final JavaCompiler compiler = new EclipseCompiler();
	/** 保存已编译的class字节码 */
	public static Map<String, ScriptBytes> scriptBytes = new HashMap<>();
	
	/**
	 * 编译指定的java源文件
	 * 
	 * @param compilationUnits
	 */
	public static List<ScriptBytes> compile(List<JavaFileObject> compilationUnits) {
		
		try {
			//java文件管理器
			JavaFileManager fileManager = new JavaFileManager();
			
			//编译选项
			List<String> options = new ArrayList<String>();
			//生成所有调试信息
			options.add("-g");
			//忽略WARNING
			options.add("-warn:none");
			//显示每种不鼓励使用的成员或类的使用或覆盖的说明
			options.add("-deprecation");
			options.add("-1.8");
			//源文件编码
			options.add("-encoding");
			options.add("UTF-8");
			//类路径
			options.add("-classpath");
			options.add(HotswapEngine.SCRIPT_ROOT_PATH.toString());
			//输出编译过程的详细信息
			//options.add("-verbose");
			//输出编译所用时间
			options.add("-time");
			
			//执行编译任务
			CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);
			if (!task.call()) {
				//编译失败
			}
			//返回编译结果
			return fileManager.getCompiledClass();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
		
	}
	
	public static void addScriptBytes(ScriptBytes sbs) {
		scriptBytes.put(sbs.getName(), sbs);
	}
	
	public static ScriptBytes getScriptBytes(String name) {
		return scriptBytes.get(name);
	}

}
