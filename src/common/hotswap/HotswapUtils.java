package common.hotswap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
/*import cmd.Cmd;
import cmd.CmdDrawCard;
import cmd.CmdGm;*/


/**
 * 热加载工具类
 * 
 */
public class HotswapUtils {
	
	/**
	 * 获取一堆类的创建器集合
	 * 
	 * @return
	 */
	public static <T> List<ObjectCreator<T>> getObjectCreators(List<Class<? extends T>> classes, Class<T> parent) {
		
		try {
			List<ObjectCreator<T>> list = new ArrayList<>();
			
			byte[] bytes = ObjectCreatorFactoryBuilder.build(classes, parent);
			
			ScriptBytes sb = new ScriptBytes(ObjectCreatorFactoryBuilder.CLASS_NAME, bytes);
			ScriptCompiler.addScriptBytes(sb);
			
			//加载MasterHandler.class并执行main()方法
			try (HotswapClassLoader loader = new HotswapClassLoader("", ClassLoader.getSystemClassLoader())) {
				
				Class<?> clazz = loader.loadClass(ObjectCreatorFactoryBuilder.CLASS_NAME);
				
				//执行MasterHandler的main()方法
				MethodType mt = MethodType.methodType(List.class);
				MethodHandle mh = MethodHandles.publicLookup().findStatic(clazz, "build", mt);
				if (mh != null) {
					list = (List<ObjectCreator<T>>)mh.invoke();
				}
				
			};
			
			return list;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*public static void main(String args[]) {
		List<Class<? extends Cmd>> classes = new ArrayList<>();
		classes.add(CmdGm.class);
		classes.add(CmdDrawCard.class);
		
		List<ObjectCreator<Cmd>> list = getObjectCreators(classes, Cmd.class);
		System.out.println(list.get(0).create());
		System.out.println(list.get(1).create());
	}*/

}
