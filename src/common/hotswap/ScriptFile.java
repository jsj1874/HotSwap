package common.hotswap;

/**
 * 脚本信息
 * 
 */
public class ScriptFile {
	/** 名字 */
	private String name;
	/** 最近修改时间 */
	private long lastModifiedTime;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

}
