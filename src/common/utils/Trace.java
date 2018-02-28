package common.utils;

import common.utils.DateUtils;

/**
 * 调试输出工具类
 * 
 * @author Nate
 * @date 2013-12-21
 */
public class Trace {
	public static boolean  DEBUG_MODE  = true;
	public static boolean trace_info = true;
	public static boolean trace_debug = true;
	public static boolean trace_error = true;
	
	public static void init() {
		if (DEBUG_MODE) {
			trace_info = true;
			trace_debug = true;
			trace_error = true;
		} else {
			trace_info = true;
			trace_debug = false;
			trace_error = true;
		}
	}
	
	public static void info(String msg) {
		if (!trace_info) return;
		System.err.format("[info ][%1$s] %2$s\n", DateUtils.getNowDate3(), msg);
	}
	
	public static void debug(String msg) {
		if (!trace_debug) return;
		System.err.format("[debug][%1$s] %2$s\n", DateUtils.getNowDate3(), msg);
	}
	
	public static void error(String msg) {
		if (!trace_error) return;
		System.err.format("[error][%1$s] %2$s\n", DateUtils.getNowDate3(), msg);
	}
	
	public static void printStackTrace(Throwable e) {
		System.err.format("[exception][%1$s] %2$s\n", DateUtils.getNowDate3());
		e.printStackTrace();
	}
	
	public static void debug1(Object...msg ) {
		if (!trace_debug) return;
		StringBuilder sb = new StringBuilder();
		sb.append("=========");
		for(Object o:msg){
			sb.append(o+"--");
		}
		sb.append("=========");
		System.err.format("[debug][%1$s] %2$s\n", DateUtils.getNowDate3(), sb.toString());
		sb = null;
	}
	
	public static void main(String args[]) {
		Trace.info("测试一下");
		Trace.info("测试一下");
		Trace.debug("测试一下");
		Trace.debug("测试一下");
		Trace.error("测试一下");
		Trace.error("测试一下");
	}

}
