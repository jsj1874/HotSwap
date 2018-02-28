package common.hotswap;

import common.scripts.handler.ScriptHandler;

public class Test {

	public static void main(String[] args) throws Exception {
		//加载所有脚本
		HotswapEngine.reloadAllScript();
		//调用脚本方法
		ScriptHandler.execute("DemoScriptChild", "test1", "aaaa", 1);
		
		System.err.println("==> 请修改文件，然后按任意键重新加载");
		System.in.read();
		
		//加载所有被修改过的脚本
		HotswapEngine.reloadAllModifiedScript();
		//调用脚本方法
		ScriptHandler.execute("DemoScriptChild", "test1", "aaaa", 1);
		
	}

}
