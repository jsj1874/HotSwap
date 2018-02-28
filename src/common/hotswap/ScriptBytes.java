package common.hotswap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.SimpleJavaFileObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.helpers.Loader;
import org.objectweb.asm.Opcodes;

import common.scripts.handler.IScriptHandler;
import common.utils.Trace;

/**
 * 脚本字节码
 * 
 */
public class ScriptBytes extends SimpleJavaFileObject {
	
	private String name;
	private byte[] bytes;
	private int classAccess;
	private String superName;
	private String[] interfaces;
	private Map<String, MethodInfo> methods = new HashMap<>(0);
	private List<String> dependClassNames = new ArrayList<>(0);
	private Boolean implementIScriptHandler = null;
	private boolean scan;
	
	public ScriptBytes(String name) {
		super(toURI(name), Kind.CLASS);
		this.name = name;
	}
	
	public ScriptBytes(String name, byte[] bytes) {
		this(name);
		this.bytes = bytes;
	}
	
	/**
	 * 编译器编译时回调， 这里我们用自己的输出流来接受编译后的class文件
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		
		return new FilterOutputStream(new ByteArrayOutputStream()) {
			@Override
			public void close() throws IOException {
				out.close();
				ByteArrayOutputStream bos = (ByteArrayOutputStream)out;
				//System.out.println("编译类:" + name);
				ScriptBytes.this.bytes = bos.toByteArray();
				
				ScriptBytes old = ScriptCompiler.getScriptBytes(name);
				if (old != null) {
					for (String name : old.getDependClassNames()) {
						ScriptBytes.this.dependClassNames.add(name);
					}
				}
				
				ScriptCompiler.addScriptBytes(ScriptBytes.this);
			}
		};
	}
	
	public boolean isScriptHandler() {
		if (!isImplementIScriptHandler()) {
			return false;
		}
		if ((classAccess & Opcodes.ACC_ABSTRACT) != 0) {
			return false;
		}
		if ((classAccess & Opcodes.ACC_INTERFACE) != 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 是否实现了IScriptHandler接口
	 * 
	 * @return
	 */
	public boolean isImplementIScriptHandler() {
		if (implementIScriptHandler != null) {
			return implementIScriptHandler;
		}
		//从接口检查
		if (interfaces != null && interfaces.length > 0) {
			for (String itf : interfaces) {
				if ("common/scripts/handler/IScriptHandler".equals(itf)) {
					implementIScriptHandler = true;
					return true;
				}
				
				ClassLoader loder = Thread.currentThread().getContextClassLoader();
				try {
					Class<?> clz = Loader.loadClass(itf.replace("/", "."));
					if (IScriptHandler.class.isAssignableFrom(clz)) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//从父类检查
		if (StringUtils.isNotBlank(superName) && !"java/lang/Object".equals(superName)) {
			if ("common/scripts/handler/AbstractScriptHandler".equals(superName)) {
				implementIScriptHandler = true;
				return true;
			}
			String name = superName.replace('/', '.');
			ScriptBytes sbs = ScriptCompiler.getScriptBytes(name);
			if (sbs != null) {
				//检查父类是否已扫描过，没的话要先扫描
				if (sbs.getClassAccess() == 0) {
					HotswapEngine.scanAndEnhanceScriptBytes(sbs);
				}
				implementIScriptHandler = sbs.isImplementIScriptHandler();
				return implementIScriptHandler;
			}
		}
		implementIScriptHandler = false;
		return false;
	}
	
	/**
	 * 父类是否实现了IScriptHandler接口
	 * @return
	 */
	public boolean isParentImplementIScriptHandler() {
		//从父类检查
		if (StringUtils.isNotBlank(superName) && !"java/lang/Object".equals(superName)) {
			if ("common/scripts/handler/AbstractScriptHandler".equals(superName)) {
				return true;
			}
			String name = superName.replace('/', '.');
			ScriptBytes sbs = ScriptCompiler.getScriptBytes(name);
			if (sbs != null) {
				//检查父类是否已扫描过，没的话要先扫描
				if (sbs.getClassAccess() == 0) {
					HotswapEngine.scanAndEnhanceScriptBytes(sbs);
				}
				
				if (sbs.interfaces != null && sbs.interfaces.length > 0) {
					for (String itf : sbs.interfaces) {
						if ("common/scripts/handler/IScriptHandler".equals(itf)) {
							return true;
						}
					}
				}
				
				return sbs.isParentImplementIScriptHandler();
			}
		}
		return false;
	}
	
	public void addDependClassName(String className) {
		this.dependClassNames.add(className);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getClassAccess() {
		return classAccess;
	}

	public void setClassAccess(int classAccess) {
		this.classAccess = classAccess;
	}

	public String getSuperName() {
		return superName;
	}

	public void setSuperName(String superName) {
		this.superName = superName;
		
		//注册依赖关系
		String name = superName.replace('/', '.');
		ScriptBytes sbs = ScriptCompiler.getScriptBytes(name);
		if (sbs != null) {
			sbs.addDependClassName(this.getName());
		}
	}

	public String[] getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String[] interfaces) {
		this.interfaces = interfaces;
		
		//注册依赖关系
		if (interfaces != null && interfaces.length > 0) {
			for (String itf : interfaces) {
				String name = itf.replace('/', '.');
				ScriptBytes sbs = ScriptCompiler.getScriptBytes(name);
				if (sbs != null) {
					sbs.addDependClassName(this.getName());
				}
			}
		}
		
	}

	public Map<String, MethodInfo> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, MethodInfo> methods) {
		this.methods = methods;
	}

	public boolean isScan() {
		return scan;
	}

	public void setScan(boolean scan) {
		this.scan = scan;
	}
	
	public List<String> getDependClassNames() {
		return dependClassNames;
	}

	public void setDependClassNames(List<String> dependClassNames) {
		this.dependClassNames = dependClassNames;
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
