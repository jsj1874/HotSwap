package common.hotswap;

/**
 * 对象创建器接口
 * 
 * @param <T>
 */
@FunctionalInterface
public interface ObjectCreator<T> {
	
	/**
	 * 创建一个对象实例
	 * 
	 * @return
	 */
	T create();
	
}
