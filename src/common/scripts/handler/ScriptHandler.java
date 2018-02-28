package common.scripts.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;
import common.utils.Trace;

/**
 * 
 * 
 */
public class ScriptHandler {
	
	private static Map<String, IScriptHandler> _datatable = new ConcurrentHashMap<>();

	/**
	 * 注册脚本处理器
	 * 
	 * @date 2010-8-19
	 * @param handler
	 */
	public static void registerScriptHandler(IScriptHandler handler) {
		if (handler != null) {
			String scriptName = handler.getClass().getSimpleName();
			_datatable.put(scriptName, handler);
			//注册回调
			handler.afterRegister();
			Trace.info("[script]" + scriptName);
		}
			
	}

	public static IScriptHandler getScriptHandler(String handlerName) {
		if (StringUtils.isEmpty(handlerName))
			return null;
		return _datatable.get(handlerName);
	}

	public static Object handlerTask(String handlerName, Object... args) {
		if(StringUtils.isEmpty(handlerName))return null;
		IScriptHandler handler = getScriptHandler(handlerName);
		if (handler == null) {
			System.out.println("handler is not exists:" + handlerName);
			//Thread.dumpStack();
			return null;
		}
		Map<String, Object> m=null;
		if (args != null) {
			m = new HashMap<>();
			for (int i = 0; i < args.length; i++) {
				m.put((String) args[i], args[++i]);
			}
		}
		return handler.handlerTask(m);
	}
	
	/**
	 * 执行指定脚本的方法
	 * 
	 * @param handlerName
	 * @param method
	 * @param args
	 * @return
	 */
	public static Object execute(String handlerName, String method, Object... args) {
		if(StringUtils.isEmpty(handlerName))return null;
		IScriptHandler handler = getScriptHandler(handlerName);
		if (handler == null) {
			Trace.error("handler is not exists:" + handlerName);
			//Thread.dumpStack();
			return null;
		}
		return handler.execute(method, args);
	}
	
}
