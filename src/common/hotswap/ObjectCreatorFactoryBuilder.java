package common.hotswap;

import java.util.List;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

/**
 * 对象创建器工厂字节码生成器
 * 
 */
public class ObjectCreatorFactoryBuilder {
	
	public static final String CLASS_NAME = "common.hotswap.ObjectCreatorFactory";
	
	public static <T> byte[] build(List<Class<? extends T>> list, Class<T> parent) {
		try {
			
			String clzDesc = Type.getDescriptor(parent);
			
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			MethodVisitor mv;
			
			//类签名
			cw.visit(V1_8,
					ACC_PUBLIC + ACC_SUPER,
					"common/hotswap/ObjectCreatorFactory",
					null,
					"java/lang/Object",
					null);
			
			cw.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
			
			//默认构造函数
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			
			//builder方法
			//public static List<ObjectCreator<Cmd>> build() {
			//}
			
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "build", "()Ljava/util/List;", "()Ljava/util/List<Lcommon/hotswap/ObjectCreator<" + clzDesc +">;>;", null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/util/ArrayList");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
			mv.visitVarInsn(ASTORE, 0);
			
			//逐个类写入  class::new,生成对应的ObjectCreator对象,然后加入到输出的list中
			for (Class<?> c : list) {
				
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInvokeDynamicInsn("create", "()Lcommon/hotswap/ObjectCreator;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"), new Object[]{Type.getType("()Ljava/lang/Object;"), new Handle(H_NEWINVOKESPECIAL, Type.getInternalName(c), "<init>", "()V"), Type.getType("()" + clzDesc)});
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
				mv.visitInsn(POP);
			}
			
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
			
			cw.visitEnd();
			
			return cw.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
