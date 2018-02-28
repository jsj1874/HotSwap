package common.hotswap;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
	
	public String name;
	
	public List<String> descs = new ArrayList<String>(1);
	
	public static MethodInfo newInstance(String name, String desc) {
		MethodInfo m = new MethodInfo();
		m.name = name;
		m.descs.add(desc);
		return m;
	}
	
	public List<String> getDescs() {
		return this.descs;
	}
	
	public void addDesc(String desc) {
		this.descs.add(desc);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
