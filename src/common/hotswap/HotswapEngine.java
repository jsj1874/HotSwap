package common.hotswap;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import javax.tools.JavaFileObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import common.utils.Trace;

/**
 * 热加载引擎
 * 
 * @author Nate
 */
public class HotswapEngine {
	/** 脚本根路径 */
	public static final Path SCRIPT_ROOT_PATH = Paths.get("E:\\projects\\ZhangXiaoHe_Script", "res/scripts");
	/** 脚本handler路径 */
	public static final Path SCRIPT_HANDLERS_PATH = Paths.get("E:\\projects\\ZhangXiaoHe_Script", "/res/scripts/handlers");
	/** 脚本文件集合 */
	public static final Map<String, ScriptFile> SCRIPT_FILES = new HashMap<>();
	/** 重加载互斥锁对象 */
	private static AtomicBoolean reloadMutexLock = new AtomicBoolean(false);
	
	/**
	 * 重新加载所有被修改过的脚本
	 */
	public static void reloadAllModifiedScript() {
		if (!reloadMutexLock.compareAndSet(false, true)) {
			return;
		}
		
		try {
			
			//待编译的java源文件集合
			List<JavaFileObject> compilationUnits = new ArrayList<>();
			
			//遍历脚本目录，加载所有被修改过的java源文件
			forEachJavaFile((file, attrs) -> {
				
				try {
					String absFilePath = file.toAbsolutePath().toString();
					ScriptFile sf = SCRIPT_FILES.get(absFilePath);
					if (sf != null) {
						if (sf.getLastModifiedTime() == attrs.lastModifiedTime().toMillis()) {
							//没有修改过的就直接返回
							return;
						}
					}
					
					//加载java文件源码
					String sourceCode = new String(Files.readAllBytes(file));
					String sourceName = file.getFileName().toString();
					
					//生成java源码对象
					JavaSourceObject sourceObj = new JavaSourceObject(sourceName, sourceCode);
					compilationUnits.add(sourceObj);
					
					if (sf != null) {
						//更新最近修改时间
						sf.setLastModifiedTime(attrs.lastModifiedTime().toMillis());
					} else {
						//脚本文件信息对象
						sf = new ScriptFile();
						//文件名
						sf.setName(file.toAbsolutePath().toString());
						//最近修改时间
						sf.setLastModifiedTime(attrs.lastModifiedTime().toMillis());
						SCRIPT_FILES.put(sf.getName(), sf);
					} 
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			});
			
			if (compilationUnits.isEmpty()) {
				//没有需要编译的脚本
				return;
			}
			
			//编译脚本
			List<ScriptBytes> list = ScriptCompiler.compile(compilationUnits);
			List<String> scriptNames = new ArrayList<String>();
			
			//扫描并增强编译后的脚本
			list.forEach(s -> {
				
				if (!s.isScan()) {
					scanAndEnhanceScriptBytes(s);
				}
				if (s.isScriptHandler()) {
					scriptNames.add(s.getName().replace('.', '/'));
				}
			
			});
			
			//查找所有存在依赖关系的脚本
			list.forEach(s -> {
				searchAllDependScripts(s, scriptNames);
			});
			
			//注册脚本
			regScripts(scriptNames);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();	
		} finally {
			reloadMutexLock.set(false);
		}
		
	}
	
	static void searchAllDependScripts(ScriptBytes sbs, List<String> scriptNames) {
		for (String name : sbs.getDependClassNames()) {
			ScriptBytes dps = ScriptCompiler.getScriptBytes(name);
			if (dps == null) {
				continue;
			}
			if (dps.isScriptHandler()) {
				scriptNames.add(dps.getName().replace('.', '/'));
			}
			//递归遍历所有依赖的脚本
			searchAllDependScripts(dps, scriptNames);
		}
	}
	
	/**
	 * 重新加载所有脚本
	 */
	public static void reloadAllScript() {
		if (!reloadMutexLock.compareAndSet(false, true)) {
			return;
		}
		
		try {
			//Trace.info("============== start reg scripts ==============");
			//待编译的java源文件集合
			List<JavaFileObject> compilationUnits = new ArrayList<>();
			//遍历脚本目录，加载所有java源文件
			forEachJavaFile((file, attrs) -> {
				
				try {
					//加载java文件源码
					String sourceCode = new String(Files.readAllBytes(file));
					String sourceName = file.getFileName().toString();
					
					//生成java源码对象
					JavaSourceObject sourceObj = new JavaSourceObject(sourceName, sourceCode);
					compilationUnits.add(sourceObj);
					
					//脚本文件信息对象
					ScriptFile sf = new ScriptFile();
					//文件名
					sf.setName(file.toAbsolutePath().toString());
					//最近修改时间
					sf.setLastModifiedTime(attrs.lastModifiedTime().toMillis());
					SCRIPT_FILES.put(sf.getName(), sf);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			});
			
			if (compilationUnits.isEmpty()) {
				//没有需要编译的脚本
				return;
			}
			
			//编译脚本
			List<ScriptBytes> list = ScriptCompiler.compile(compilationUnits);
			
			List<String> scriptNames = new ArrayList<String>();
			//扫描并增强编译后的脚本
			list.forEach(s -> {
				if (!s.isScan()) {
					scanAndEnhanceScriptBytes(s);
				}
				if (s.isScriptHandler()) {
					scriptNames.add(s.getName().replace('.', '/'));
				}
			});
			
			//注册脚本
			regScripts(scriptNames);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reloadMutexLock.set(false);
		}
		
	}
	
	/**
	 * 注册脚本
	 */
	public static void regScripts(List<String> scriptNames) {
		
		try {
			//根据脚本名，动态生成MasterHandler.class
			byte[] bytes = MasterHandlerBuilder.builder(scriptNames);
			ScriptBytes sb = new ScriptBytes(MasterHandlerBuilder.CLASS_NAME, bytes);
			ScriptCompiler.addScriptBytes(sb);
			
			//加载MasterHandler.class并执行main()方法
			HotswapClassLoader loader = new HotswapClassLoader(SCRIPT_ROOT_PATH.toString(), ClassLoader.getSystemClassLoader());
			Class<?> clazz = loader.loadClass(MasterHandlerBuilder.CLASS_NAME);
			
			//执行MasterHandler的main()方法
			MethodType mt = MethodType.methodType(void.class, String[].class);
			MethodHandle mh = MethodHandles.publicLookup().findStatic(clazz, "main", mt);
			if (mh != null) {
				mh.invokeExact(new String[0]);
			}
			
			loader.close();
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 遍历脚本目录下的所有java源文件
	 * 
	 * @param action
	 */
	public static void forEachJavaFile(BiConsumer<Path, BasicFileAttributes> action) {
		forEachJavaFile(SCRIPT_HANDLERS_PATH, action);
	}
	
	/**
	 * 遍历指定目录下的所有java源文件
	 * 
	 * @param path
	 * @param action
	 */
	public static void forEachJavaFile(Path path, BiConsumer<Path, BasicFileAttributes> action) {
		try {
			//遍历目录
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					//只处理.java 源文件
					if (attrs.isRegularFile() && file.getFileName().toString().endsWith(".java")) {
						action.accept(file, attrs);
					}
					return FileVisitResult.CONTINUE;
				}
				
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 扫描并增强脚本字节码
	 * 
	 * @param sbs
	 */
	public static void scanAndEnhanceScriptBytes(ScriptBytes sbs) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ScriptBytesScanner sc = new ScriptBytesScanner(cw, sbs);
		ClassReader cr = new ClassReader(sbs.getBytes());
		cr.accept(sc, 0);
		sbs.setBytes(cw.toByteArray());
	}

}
