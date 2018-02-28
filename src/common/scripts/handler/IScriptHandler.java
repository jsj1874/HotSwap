package common.scripts.handler;

import java.util.Map;

/**
 * 脚本根接口
 * 
 * @author Nate
 */
public interface IScriptHandler {
	
	default public Object handlerTask(Map<String, Object> args) {
		return null;
	};
	
	default Object execute(String method, Object... objs) {
		return null;
	}
	
	default public void afterRegister() {
		
	}
	
	
}
