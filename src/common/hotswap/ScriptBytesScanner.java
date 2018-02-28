package common.hotswap;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F_APPEND;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 脚本字节码扫描器
 * 
 */
public class ScriptBytesScanner extends ClassVisitor {

	private ScriptBytes sbs;

	public ScriptBytesScanner(ClassVisitor cv, ScriptBytes sbs) {
		super(ASM5, cv);
		this.sbs = sbs;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		//访问标识
		sbs.setClassAccess(access);
		//父类
		sbs.setSuperName(superName);
		//接口
		sbs.setInterfaces(interfaces);
		
		super.visit(version, access, name, signature, superName, interfaces);
		
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		if (sbs.isImplementIScriptHandler()) {
			
			//如果自己实现了execute()就删掉不要了
			if ("execute".equals(name) && "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;".equals(desc)) {
				return null;
			}
			
			if (!"<init>".equals(name) && !"<cinit>".equals(name)) {
				
				//只有public方法才做脚本方法
				if ((access & ACC_PUBLIC) != 0) {
					//System.out.println("方法:" + name);
					MethodInfo m = sbs.getMethods().get(name);
					if (m == null) {
						m = MethodInfo.newInstance(name, desc);
						sbs.getMethods().put(name, m);
					} else {
						System.err.println("脚本[" + sbs.getName() + "]含有重名方法[" + name + "]，最好同一个脚本内不要起相同的方法名!!!");
						m.addDesc(desc);
					}
					
				}
			}
		}
		
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	@Override
	public void visitEnd() {
		
		if (sbs.isImplementIScriptHandler()) {
			implementExecuteMethod();
		}
		
		cv.visitEnd();
		
		sbs.setScan(true);
	}
	
	void implementExecuteMethod() {
		//实现 public Object execute(String method, Object... objs)
		
		List<MethodInfo> methods = new ArrayList<MethodInfo>();
		for (MethodInfo m : sbs.getMethods().values()) {
			methods.add(m);
		}
		
		//根据hashCode()升序排序
		methods.sort((o1, o2) -> {
			int h1 = o1.getName().hashCode();
			int h2 = o2.getName().hashCode();
			if (h1 < h2) {
				return -1;
			}
			if (h1 > h2) {
				return 1;
			}
			return 0;
		} );
		
		
		
		Label defaultLablel = new Label();
		Label[] fLbs = new Label[methods.size()];
		Label[] sLbs = new Label[methods.size()];
		int[] keys = new int[methods.size()];
		
		for (int i = 0; i < fLbs.length; i++) {
			fLbs[i] = new Label();
			sLbs[i] = new Label();
			keys[i] = methods.get(i).getName().hashCode();
		}
		
		//生产方法execute()
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_VARARGS,
				"execute",
				"(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
				null,
				null);
		
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 3);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		
		//生成switch()
		//case值不连续，用lookupswitch指令
		mv.visitLookupSwitchInsn(defaultLablel, keys, fLbs);
		
		MethodInfo mi;
		for (int i = 0; i < methods.size(); i++) {
			mi = methods.get(i);
			//构造各个label,主要是进行传进来的method参数和指定的方法名equals判断
			mv.visitLabel(fLbs[i]);
			if (i == 0) {
				mv.visitFrame(F_APPEND,1, new Object[] {"java/lang/String"}, 0, null);
			} else {
				mv.visitFrame(F_SAME, 0, null, 0, null);
			}
			mv.visitVarInsn(ALOAD, 3);
			mv.visitLdcInsn(mi.getName());
			//执行"test1".equals(method)
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			//如果和test1相等，就跳到13
			mv.visitJumpInsn(IFNE, sLbs[i]);
			//否则跳转到12
			mv.visitJumpInsn(GOTO, defaultLablel);
			
			//====================条件符合后的处理======================================
			
			mv.visitLabel(sLbs[i]);
			mv.visitFrame(F_SAME, 0, null, 0, null);
			
			//方法签名集合,有可能同名的方法有多个
			List<String> descs = mi.getDescs();
			
			//如果没有同名方法
			if (descs.size() == 1) {
				//压入this到栈顶
				mv.visitVarInsn(ALOAD, 0);
				
				String desc = descs.get(0);
				//根据具体方法进行参数转换
				Type[] types = Type.getArgumentTypes(desc);
				Type t;
				for (int j = 0; j < types.length; j++) {
					t = types[j];
					//压入参数数组到栈顶
					mv.visitVarInsn(ALOAD, 2);
					
					if (j <= 5) { //处理器立即数寻址
						mv.visitInsn(j + ICONST_0);
					} else {
						mv.visitIntInsn(BIPUSH, j + ICONST_0);
					}
					mv.visitInsn(AALOAD);
					//基础类型需要先转成对应的对象类型
					//数组和对象类型可以直接转换
					switch (t.getSort()) {
					case Type.BOOLEAN:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
						break;
					case Type.CHAR:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
						break;
					case Type.BYTE:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
						break;
					case Type.SHORT:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
						break;
					case Type.INT:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
						break;
					case Type.FLOAT:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
						break;
					case Type.LONG:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
						break;
					case Type.DOUBLE:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
						break;
					default:
						mv.visitTypeInsn(CHECKCAST, t.getInternalName());
					}
				}
				
				//调用DemoScript.test1(Ljava/lang/String;I);
				String className = sbs.getName().replace('.', '/');
				mv.visitMethodInsn(INVOKEVIRTUAL, className, mi.getName(), desc, false);
				
				//如果返回值不为void，就返回返回值,否则，返回null
				Type rt = Type.getReturnType(desc);
				if (!rt.equals(Type.VOID_TYPE)) {
					
					switch (rt.getSort()) {
					case Type.BOOLEAN:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
						break;
					case Type.CHAR:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
						break;
					case Type.BYTE:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
						break;
					case Type.SHORT:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
						break;
					case Type.INT:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
						break;
					case Type.FLOAT:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
						break;
					case Type.LONG:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
						break;
					case Type.DOUBLE:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
						break;
					}
					
					mv.visitInsn(ARETURN);
					
				} else {
					//return null;
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ARETURN);
				}
			} else {
				//TODO 重名方法的处理
				
//				for (int j = 0; j < descs.size(); j++) {
//					String desc = descs.get(j);
//					Type[] types = Type.getArgumentTypes(desc);
//					mv.visitVarInsn(ALOAD, 2);
//					mv.visitInsn(ARRAYLENGTH);
//					mv.visitInsn(ICONST_3);
//				}
				//压入this到栈顶
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ARETURN);
			}

		}
		
		//default:实现
		//如果父类实现了接口IScriptHandler,就调用super.execute(....);
		//否则，表示没有找到对应的脚本方法， return null;
		mv.visitLabel(defaultLablel);
		if (sbs.isParentImplementIScriptHandler()) {
			mv.visitFrame(F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESPECIAL, sbs.getSuperName(), "execute", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", false);
			mv.visitInsn(ARETURN);
		} else {
			mv.visitFrame(F_SAME, 0, null, 0, null);
			
			//提示方法找不到
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("method: ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(" not found");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
			
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
		}
		
		mv.visitMaxs(0, 0);
		
		mv.visitEnd();
	}

}
