package common.hotswap;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * MasterHandler字节码生成器
 * 
 */
public class MasterHandlerBuilder {
	
	public static final String CLASS_NAME = "handlers.MasterHandler";
	
	public static byte[] builder(List<String> scripts) {
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		MethodVisitor mv;
		
		//类header
		cw.visit(Opcodes.V1_8,
				Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
				"handlers/MasterHandler",
				null,
				"java/lang/Object",
				null);
		
		//默认的构造函数
		//this.super();
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
				"<init>",
				"()V",
				null,
				null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/Object",
				"<init>",
				"()V",
				false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		//main方法
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
				"main",
				"([Ljava/lang/String;)V",
				null,
				null);
		
		//根据脚本名注册脚本
		//ScriptHandler.registerScriptHandler(new XXXXX());
		for (String name : scripts) {
			mv.visitTypeInsn(Opcodes.NEW, name);
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "common/scripts/handler/ScriptHandler", "registerScriptHandler", "(Lcommon/scripts/handler/IScriptHandler;)V", false);
		}
		
		//System.out.println("===初始化脚本完成===");
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitLdcInsn("=== Load script files successfully ===");
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		
		cw.visitEnd();
		
		return cw.toByteArray();
		
	}

}
