package amidst.clazz.fabric;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class RemapRefMapASM implements Opcodes {
	
	public static byte[] dump() throws Exception {
		
		ClassWriter classWriter = new ClassWriter(0);
		FieldVisitor fieldVisitor;
		MethodVisitor methodVisitor;
		
		classWriter.visit(V1_8, ACC_PUBLIC | ACC_FINAL | ACC_SUPER,
				"org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", null, "java/lang/Object",
				new String[] { "org/spongepowered/asm/mixin/refmap/IClassReferenceMapper",
						"org/spongepowered/asm/mixin/refmap/IReferenceMapper" });
		
		classWriter.visitSource("RemappingReferenceMapper.java", null);
		
		{
			fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "logger",
					"Lorg/apache/logging/log4j/Logger;", null, null);
			fieldVisitor.visitEnd();
		}
		{
			fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "refMap",
					"Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;", null, null);
			fieldVisitor.visitEnd();
		}
		{
			fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "remapper",
					"Lorg/spongepowered/asm/mixin/extensibility/IRemapper;", null, null);
			fieldVisitor.visitEnd();
		}
		{
			fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "mappedReferenceCache", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;", null);
			fieldVisitor.visitEnd();
		}
		{
			fieldVisitor = classWriter.visitField(ACC_PRIVATE, "descRemapper",
					"Lamidst/clazz/fabric/NameDescriptorRemapper;", null, null);
			fieldVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(28, label0);
			methodVisitor.visitLdcInsn("mixin");
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/apache/logging/log4j/LogManager", "getLogger",
					"(Ljava/lang/String;)Lorg/apache/logging/log4j/Logger;", false);
			methodVisitor.visitFieldInsn(PUTSTATIC, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"logger", "Lorg/apache/logging/log4j/Logger;");
			methodVisitor.visitInsn(RETURN);
			methodVisitor.visitMaxs(1, 0);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "<init>",
					"(Lorg/spongepowered/asm/mixin/MixinEnvironment;Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;)V",
					null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			Label label1 = new Label();
			Label label2 = new Label();
			methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/Throwable");
			Label label3 = new Label();
			methodVisitor.visitLabel(label3);
			methodVisitor.visitLineNumber(47, label3);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			Label label4 = new Label();
			methodVisitor.visitLabel(label4);
			methodVisitor.visitLineNumber(43, label4);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitTypeInsn(NEW, "java/util/HashMap");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			methodVisitor.visitFieldInsn(PUTFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"mappedReferenceCache", "Ljava/util/Map;");
			Label label5 = new Label();
			methodVisitor.visitLabel(label5);
			methodVisitor.visitLineNumber(48, label5);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitFieldInsn(PUTFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			Label label6 = new Label();
			methodVisitor.visitLabel(label6);
			methodVisitor.visitLineNumber(49, label6);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/MixinEnvironment", "getRemappers",
					"()Lorg/spongepowered/asm/obfuscation/RemapperChain;", false);
			methodVisitor.visitFieldInsn(PUTFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(52, label0);
			methodVisitor.visitLdcInsn(Type.getType("Lorg/spongepowered/asm/obfuscation/RemapperChain;"));
			methodVisitor.visitLdcInsn("remappers");
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField",
					"(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
			methodVisitor.visitVarInsn(ASTORE, 3);
			Label label7 = new Label();
			methodVisitor.visitLabel(label7);
			methodVisitor.visitLineNumber(53, label7);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitInsn(ICONST_1);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
			Label label8 = new Label();
			methodVisitor.visitLabel(label8);
			methodVisitor.visitLineNumber(54, label8);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get",
					"(Ljava/lang/Object;)Ljava/lang/Object;", false);
			methodVisitor.visitTypeInsn(CHECKCAST, "java/util/List");
			methodVisitor.visitVarInsn(ASTORE, 4);
			Label label9 = new Label();
			methodVisitor.visitLabel(label9);
			methodVisitor.visitLineNumber(55, label9);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 4);
			methodVisitor.visitInsn(ICONST_0);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
			methodVisitor.visitTypeInsn(CHECKCAST, "amidst/clazz/fabric/NameDescriptorRemapper");
			methodVisitor.visitFieldInsn(PUTFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"descRemapper", "Lamidst/clazz/fabric/NameDescriptorRemapper;");
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(56, label1);
			Label label10 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label10);
			methodVisitor.visitLabel(label2);
			methodVisitor.visitFrame(Opcodes.F_NEW, 3,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
							"org/spongepowered/asm/mixin/MixinEnvironment",
							"org/spongepowered/asm/mixin/refmap/IReferenceMapper" },
					1, new Object[] { "java/lang/Throwable" });
			methodVisitor.visitVarInsn(ASTORE, 3);
			Label label11 = new Label();
			methodVisitor.visitLabel(label11);
			methodVisitor.visitLineNumber(57, label11);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "amidst/logging/AmidstLogger", "crash",
					"(Ljava/lang/Throwable;)V", false);
			methodVisitor.visitLabel(label10);
			methodVisitor.visitLineNumber(60, label10);
			methodVisitor.visitFrame(Opcodes.F_NEW, 3,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
							"org/spongepowered/asm/mixin/MixinEnvironment",
							"org/spongepowered/asm/mixin/refmap/IReferenceMapper" },
					0, new Object[] {});
			methodVisitor.visitFieldInsn(GETSTATIC, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"logger", "Lorg/apache/logging/log4j/Logger;");
			methodVisitor.visitLdcInsn("Remapping refMap {} using remapper chain");
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"getResourceName", "()Ljava/lang/String;", true);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "info",
					"(Ljava/lang/String;Ljava/lang/Object;)V", true);
			Label label12 = new Label();
			methodVisitor.visitLabel(label12);
			methodVisitor.visitLineNumber(61, label12);
			methodVisitor.visitInsn(RETURN);
			Label label13 = new Label();
			methodVisitor.visitLabel(label13);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label3, label13, 0);
			methodVisitor.visitLocalVariable("env", "Lorg/spongepowered/asm/mixin/MixinEnvironment;", null, label3,
					label13, 1);
			methodVisitor.visitLocalVariable("refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;", null,
					label3, label13, 2);
			methodVisitor.visitLocalVariable("f1", "Ljava/lang/reflect/Field;", null, label7, label1, 3);
			methodVisitor.visitLocalVariable("remappers", "Ljava/util/List;",
					"Ljava/util/List<Lorg/spongepowered/asm/mixin/extensibility/IRemapper;>;", label9, label1, 4);
			methodVisitor.visitLocalVariable("e", "Ljava/lang/Throwable;", null, label11, label10, 3);
			methodVisitor.visitMaxs(3, 5);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "isDefault", "()Z", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(68, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"isDefault", "()Z", true);
			methodVisitor.visitInsn(IRETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label1, 0);
			methodVisitor.visitMaxs(1, 1);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getResourceName", "()Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(77, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"getResourceName", "()Ljava/lang/String;", true);
			methodVisitor.visitInsn(ARETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label1, 0);
			methodVisitor.visitMaxs(1, 1);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getStatus", "()Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(85, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"getStatus", "()Ljava/lang/String;", true);
			methodVisitor.visitInsn(ARETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label1, 0);
			methodVisitor.visitMaxs(1, 1);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getContext", "()Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(93, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"getContext", "()Ljava/lang/String;", true);
			methodVisitor.visitInsn(ARETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label1, 0);
			methodVisitor.visitMaxs(1, 1);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setContext", "(Ljava/lang/String;)V", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(102, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"setContext", "(Ljava/lang/String;)V", true);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(103, label1);
			methodVisitor.visitInsn(RETURN);
			Label label2 = new Label();
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label2, 0);
			methodVisitor.visitLocalVariable("context", "Ljava/lang/String;", null, label0, label2, 1);
			methodVisitor.visitMaxs(2, 2);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "remap",
					"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(111, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"getContext", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapWithContext", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
					false);
			methodVisitor.visitInsn(ARETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label1, 0);
			methodVisitor.visitLocalVariable("className", "Ljava/lang/String;", null, label0, label1, 1);
			methodVisitor.visitLocalVariable("reference", "Ljava/lang/String;", null, label0, label1, 2);
			methodVisitor.visitMaxs(4, 3);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "remapMethodDescriptor",
					"(Lorg/spongepowered/asm/mixin/extensibility/IRemapper;Ljava/lang/String;)Ljava/lang/String;", null,
					null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(115, label0);
			methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
			methodVisitor.visitVarInsn(ASTORE, 2);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(116, label1);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitIntInsn(BIPUSH, 40);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
					"(C)Ljava/lang/StringBuilder;", false);
			methodVisitor.visitInsn(POP);
			Label label2 = new Label();
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLineNumber(117, label2);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/objectweb/asm/Type", "getArgumentTypes",
					"(Ljava/lang/String;)[Lorg/objectweb/asm/Type;", false);
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ASTORE, 6);
			methodVisitor.visitInsn(ARRAYLENGTH);
			methodVisitor.visitVarInsn(ISTORE, 5);
			methodVisitor.visitInsn(ICONST_0);
			methodVisitor.visitVarInsn(ISTORE, 4);
			Label label3 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label3);
			Label label4 = new Label();
			methodVisitor.visitLabel(label4);
			methodVisitor.visitFrame(Opcodes.F_NEW, 7,
					new Object[] { "org/spongepowered/asm/mixin/extensibility/IRemapper", "java/lang/String",
							"java/lang/StringBuilder", Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER,
							"[Lorg/objectweb/asm/Type;" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitVarInsn(ILOAD, 4);
			methodVisitor.visitInsn(AALOAD);
			methodVisitor.visitVarInsn(ASTORE, 3);
			Label label5 = new Label();
			methodVisitor.visitLabel(label5);
			methodVisitor.visitLineNumber(118, label5);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/Type", "getDescriptor",
					"()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper",
					"mapDesc", "(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
					"(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			methodVisitor.visitInsn(POP);
			Label label6 = new Label();
			methodVisitor.visitLabel(label6);
			methodVisitor.visitLineNumber(117, label6);
			methodVisitor.visitIincInsn(4, 1);
			methodVisitor.visitLabel(label3);
			methodVisitor.visitFrame(Opcodes.F_NEW, 7,
					new Object[] { "org/spongepowered/asm/mixin/extensibility/IRemapper", "java/lang/String",
							"java/lang/StringBuilder", Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER,
							"[Lorg/objectweb/asm/Type;" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ILOAD, 4);
			methodVisitor.visitVarInsn(ILOAD, 5);
			methodVisitor.visitJumpInsn(IF_ICMPLT, label4);
			Label label7 = new Label();
			methodVisitor.visitLabel(label7);
			methodVisitor.visitLineNumber(120, label7);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitIntInsn(BIPUSH, 41);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
					"(C)Ljava/lang/StringBuilder;", false);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/objectweb/asm/Type", "getReturnType",
					"(Ljava/lang/String;)Lorg/objectweb/asm/Type;", false);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/objectweb/asm/Type", "getDescriptor",
					"()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper",
					"mapDesc", "(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
					"(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
					false);
			methodVisitor.visitInsn(ARETURN);
			Label label8 = new Label();
			methodVisitor.visitLabel(label8);
			methodVisitor.visitLocalVariable("remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;", null,
					label0, label8, 0);
			methodVisitor.visitLocalVariable("desc", "Ljava/lang/String;", null, label0, label8, 1);
			methodVisitor.visitLocalVariable("newDesc", "Ljava/lang/StringBuilder;", null, label1, label8, 2);
			methodVisitor.visitLocalVariable("arg", "Lorg/objectweb/asm/Type;", null, label5, label6, 3);
			methodVisitor.visitMaxs(3, 7);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "remapWithContext",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(130, label0);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "isEmpty", "()Z", false);
			Label label1 = new Label();
			methodVisitor.visitJumpInsn(IFEQ, label1);
			Label label2 = new Label();
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLineNumber(131, label2);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitInsn(ARETURN);
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(134, label1);
			methodVisitor.visitFrame(Opcodes.F_NEW, 4,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"remapWithContext", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
					true);
			methodVisitor.visitVarInsn(ASTORE, 4);
			Label label3 = new Label();
			methodVisitor.visitLabel(label3);
			methodVisitor.visitLineNumber(135, label3);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"mappedReferenceCache", "Ljava/util/Map;");
			methodVisitor.visitVarInsn(ALOAD, 4);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get",
					"(Ljava/lang/Object;)Ljava/lang/Object;", true);
			methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
			methodVisitor.visitVarInsn(ASTORE, 5);
			Label label4 = new Label();
			methodVisitor.visitLabel(label4);
			methodVisitor.visitLineNumber(136, label4);
			methodVisitor.visitVarInsn(ALOAD, 5);
			Label label5 = new Label();
			methodVisitor.visitJumpInsn(IFNULL, label5);
			Label label6 = new Label();
			methodVisitor.visitLabel(label6);
			methodVisitor.visitLineNumber(137, label6);
			methodVisitor.visitVarInsn(ALOAD, 5);
			methodVisitor.visitInsn(ARETURN);
			methodVisitor.visitLabel(label5);
			methodVisitor.visitLineNumber(139, label5);
			methodVisitor.visitFrame(Opcodes.F_NEW, 6,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 4);
			methodVisitor.visitVarInsn(ASTORE, 6);
			Label label7 = new Label();
			methodVisitor.visitLabel(label7);
			methodVisitor.visitLineNumber(143, label7);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitInsn(ACONST_NULL);
			methodVisitor.visitInsn(ACONST_NULL);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"parse",
					"(Ljava/lang/String;Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;Ljava/lang/String;)Lorg/spongepowered/asm/mixin/injection/struct/MemberInfo;",
					false);
			methodVisitor.visitVarInsn(ASTORE, 7);
			Label label8 = new Label();
			methodVisitor.visitLabel(label8);
			methodVisitor.visitLineNumber(144, label8);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getName", "()Ljava/lang/String;", false);
			Label label9 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label9);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitJumpInsn(IFNONNULL, label9);
			Label label10 = new Label();
			methodVisitor.visitLabel(label10);
			methodVisitor.visitLineNumber(145, label10);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			Label label11 = new Label();
			methodVisitor.visitJumpInsn(IFNULL, label11);
			methodVisitor.visitTypeInsn(NEW, "org/spongepowered/asm/mixin/injection/struct/MemberInfo");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper", "map",
					"(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitInsn(ACONST_NULL);
			methodVisitor.visitInsn(ACONST_NULL);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"toString", "()Ljava/lang/String;", false);
			Label label12 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label12);
			methodVisitor.visitLabel(label11);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"toString", "()Ljava/lang/String;", false);
			methodVisitor.visitLabel(label12);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					1, new Object[] { "java/lang/String" });
			methodVisitor.visitInsn(ARETURN);
			methodVisitor.visitLabel(label9);
			methodVisitor.visitLineNumber(146, label9);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"isField", "()Z", false);
			Label label13 = new Label();
			methodVisitor.visitJumpInsn(IFEQ, label13);
			Label label14 = new Label();
			methodVisitor.visitLabel(label14);
			methodVisitor.visitLineNumber(147, label14);
			methodVisitor.visitTypeInsn(NEW, "org/spongepowered/asm/mixin/injection/struct/MemberInfo");
			methodVisitor.visitInsn(DUP);
			Label label15 = new Label();
			methodVisitor.visitLabel(label15);
			methodVisitor.visitLineNumber(148, label15);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getName", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper",
					"mapFieldName", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", true);
			Label label16 = new Label();
			methodVisitor.visitLabel(label16);
			methodVisitor.visitLineNumber(149, label16);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			Label label17 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label17);
			methodVisitor.visitInsn(ACONST_NULL);
			Label label18 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label18);
			methodVisitor.visitLabel(label17);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					3, new Object[] { label14, label14, "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper", "map",
					"(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitLabel(label18);
			methodVisitor.visitLineNumber(150, label18);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					4, new Object[] { label14, label14, "java/lang/String", "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			Label label19 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label19);
			methodVisitor.visitInsn(ACONST_NULL);
			Label label20 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label20);
			methodVisitor.visitLabel(label19);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					4, new Object[] { label14, label14, "java/lang/String", "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper",
					"mapDesc", "(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitLabel(label20);
			methodVisitor.visitLineNumber(147, label20);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					5, new Object[] { label14, label14, "java/lang/String", "java/lang/String", "java/lang/String" });
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
			Label label21 = new Label();
			methodVisitor.visitLabel(label21);
			methodVisitor.visitLineNumber(151, label21);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"toString", "()Ljava/lang/String;", false);
			Label label22 = new Label();
			methodVisitor.visitLabel(label22);
			methodVisitor.visitLineNumber(147, label22);
			methodVisitor.visitVarInsn(ASTORE, 6);
			Label label23 = new Label();
			methodVisitor.visitLabel(label23);
			methodVisitor.visitLineNumber(152, label23);
			Label label24 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label24);
			methodVisitor.visitLabel(label13);
			methodVisitor.visitLineNumber(153, label13);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			Label label25 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label25);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getName", "()Ljava/lang/String;", false);
			methodVisitor.visitJumpInsn(IFNULL, label25);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitLdcInsn("<init>");
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			methodVisitor.visitJumpInsn(IFNE, label25);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitLdcInsn("<clinit>");
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			methodVisitor.visitJumpInsn(IFNE, label25);
			Label label26 = new Label();
			methodVisitor.visitLabel(label26);
			methodVisitor.visitLineNumber(154, label26);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"descRemapper", "Lamidst/clazz/fabric/NameDescriptorRemapper;");
			methodVisitor.visitFieldInsn(GETFIELD, "amidst/clazz/fabric/NameDescriptorRemapper", "methodNameDescMap",
					"Ljava/util/Map;");
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get",
					"(Ljava/lang/Object;)Ljava/lang/Object;", true);
			methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/String");
			methodVisitor.visitVarInsn(ASTORE, 8);
			Label label27 = new Label();
			methodVisitor.visitLabel(label27);
			methodVisitor.visitLineNumber(155, label27);
			methodVisitor.visitVarInsn(ALOAD, 8);
			methodVisitor.visitJumpInsn(IFNULL, label25);
			methodVisitor.visitVarInsn(ALOAD, 8);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			methodVisitor.visitJumpInsn(IFNE, label25);
			Label label28 = new Label();
			methodVisitor.visitLabel(label28);
			methodVisitor.visitLineNumber(156, label28);
			methodVisitor.visitVarInsn(ALOAD, 8);
			methodVisitor.visitInsn(ACONST_NULL);
			methodVisitor.visitInsn(ACONST_NULL);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"parse",
					"(Ljava/lang/String;Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;Ljava/lang/String;)Lorg/spongepowered/asm/mixin/injection/struct/MemberInfo;",
					false);
			methodVisitor.visitVarInsn(ASTORE, 9);
			Label label29 = new Label();
			methodVisitor.visitLabel(label29);
			methodVisitor.visitLineNumber(157, label29);
			methodVisitor.visitTypeInsn(NEW, "org/spongepowered/asm/mixin/injection/struct/MemberInfo");
			methodVisitor.visitInsn(DUP);
			Label label30 = new Label();
			methodVisitor.visitLabel(label30);
			methodVisitor.visitLineNumber(158, label30);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getName", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper",
					"mapMethodName", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
					true);
			Label label31 = new Label();
			methodVisitor.visitLabel(label31);
			methodVisitor.visitLineNumber(159, label31);
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			Label label32 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label32);
			methodVisitor.visitInsn(ACONST_NULL);
			Label label33 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label33);
			methodVisitor.visitLabel(label32);
			methodVisitor.visitFrame(Opcodes.F_NEW, 10,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					3, new Object[] { label29, label29, "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper", "map",
					"(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitLabel(label33);
			methodVisitor.visitLineNumber(160, label33);
			methodVisitor.visitFrame(Opcodes.F_NEW, 10,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					4, new Object[] { label29, label29, "java/lang/String", "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			Label label34 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label34);
			methodVisitor.visitInsn(ACONST_NULL);
			Label label35 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label35);
			methodVisitor.visitLabel(label34);
			methodVisitor.visitFrame(Opcodes.F_NEW, 10,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					4, new Object[] { label29, label29, "java/lang/String", "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 9);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapMethodDescriptor",
					"(Lorg/spongepowered/asm/mixin/extensibility/IRemapper;Ljava/lang/String;)Ljava/lang/String;",
					false);
			methodVisitor.visitLabel(label35);
			methodVisitor.visitLineNumber(157, label35);
			methodVisitor.visitFrame(Opcodes.F_NEW, 10,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					5, new Object[] { label29, label29, "java/lang/String", "java/lang/String", "java/lang/String" });
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
			Label label36 = new Label();
			methodVisitor.visitLabel(label36);
			methodVisitor.visitLineNumber(161, label36);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"toString", "()Ljava/lang/String;", false);
			Label label37 = new Label();
			methodVisitor.visitLabel(label37);
			methodVisitor.visitLineNumber(157, label37);
			methodVisitor.visitVarInsn(ASTORE, 6);
			Label label38 = new Label();
			methodVisitor.visitLabel(label38);
			methodVisitor.visitLineNumber(163, label38);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"mappedReferenceCache", "Ljava/util/Map;");
			methodVisitor.visitVarInsn(ALOAD, 4);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
					"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
			methodVisitor.visitInsn(POP);
			Label label39 = new Label();
			methodVisitor.visitLabel(label39);
			methodVisitor.visitLineNumber(164, label39);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitInsn(ARETURN);
			methodVisitor.visitLabel(label25);
			methodVisitor.visitLineNumber(167, label25);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					0, new Object[] {});
			methodVisitor.visitTypeInsn(NEW, "org/spongepowered/asm/mixin/injection/struct/MemberInfo");
			methodVisitor.visitInsn(DUP);
			Label label40 = new Label();
			methodVisitor.visitLabel(label40);
			methodVisitor.visitLineNumber(168, label40);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getName", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper",
					"mapMethodName", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
					true);
			Label label41 = new Label();
			methodVisitor.visitLabel(label41);
			methodVisitor.visitLineNumber(169, label41);
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			Label label42 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label42);
			methodVisitor.visitInsn(ACONST_NULL);
			Label label43 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label43);
			methodVisitor.visitLabel(label42);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					3, new Object[] { label25, label25, "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getOwner", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper", "map",
					"(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitLabel(label43);
			methodVisitor.visitLineNumber(170, label43);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					4, new Object[] { label25, label25, "java/lang/String", "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			Label label44 = new Label();
			methodVisitor.visitJumpInsn(IFNONNULL, label44);
			methodVisitor.visitInsn(ACONST_NULL);
			Label label45 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label45);
			methodVisitor.visitLabel(label44);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					4, new Object[] { label25, label25, "java/lang/String", "java/lang/String" });
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 7);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"getDesc", "()Ljava/lang/String;", false);
			methodVisitor.visitMethodInsn(INVOKESTATIC, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapMethodDescriptor",
					"(Lorg/spongepowered/asm/mixin/extensibility/IRemapper;Ljava/lang/String;)Ljava/lang/String;",
					false);
			methodVisitor.visitLabel(label45);
			methodVisitor.visitLineNumber(167, label45);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					5, new Object[] { label25, label25, "java/lang/String", "java/lang/String", "java/lang/String" });
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
			Label label46 = new Label();
			methodVisitor.visitLabel(label46);
			methodVisitor.visitLineNumber(171, label46);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/injection/struct/MemberInfo",
					"toString", "()Ljava/lang/String;", false);
			Label label47 = new Label();
			methodVisitor.visitLabel(label47);
			methodVisitor.visitLineNumber(167, label47);
			methodVisitor.visitVarInsn(ASTORE, 6);
			methodVisitor.visitLabel(label24);
			methodVisitor.visitLineNumber(174, label24);
			methodVisitor.visitFrame(Opcodes.F_NEW, 8,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String",
							"java/lang/String", "org/spongepowered/asm/mixin/injection/struct/MemberInfo" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"mappedReferenceCache", "Ljava/util/Map;");
			methodVisitor.visitVarInsn(ALOAD, 4);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
					"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
			methodVisitor.visitInsn(POP);
			Label label48 = new Label();
			methodVisitor.visitLabel(label48);
			methodVisitor.visitLineNumber(175, label48);
			methodVisitor.visitVarInsn(ALOAD, 6);
			methodVisitor.visitInsn(ARETURN);
			Label label49 = new Label();
			methodVisitor.visitLabel(label49);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label49, 0);
			methodVisitor.visitLocalVariable("context", "Ljava/lang/String;", null, label0, label49, 1);
			methodVisitor.visitLocalVariable("className", "Ljava/lang/String;", null, label0, label49, 2);
			methodVisitor.visitLocalVariable("reference", "Ljava/lang/String;", null, label0, label49, 3);
			methodVisitor.visitLocalVariable("origInfoString", "Ljava/lang/String;", null, label3, label49, 4);
			methodVisitor.visitLocalVariable("remappedCached", "Ljava/lang/String;", null, label4, label49, 5);
			methodVisitor.visitLocalVariable("remapped", "Ljava/lang/String;", null, label7, label49, 6);
			methodVisitor.visitLocalVariable("info", "Lorg/spongepowered/asm/mixin/injection/struct/MemberInfo;", null,
					label8, label49, 7);
			methodVisitor.visitLocalVariable("withDesc", "Ljava/lang/String;", null, label27, label25, 8);
			methodVisitor.visitLocalVariable("newInfo", "Lorg/spongepowered/asm/mixin/injection/struct/MemberInfo;",
					null, label29, label25, 9);
			methodVisitor.visitMaxs(6, 10);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "of",
					"(Lorg/spongepowered/asm/mixin/MixinEnvironment;Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;)Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;",
					null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(188, label0);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"isDefault", "()Z", true);
			Label label1 = new Label();
			methodVisitor.visitJumpInsn(IFNE, label1);
			Label label2 = new Label();
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLineNumber(189, label2);
			methodVisitor.visitTypeInsn(NEW, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper");
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"<init>",
					"(Lorg/spongepowered/asm/mixin/MixinEnvironment;Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;)V",
					false);
			methodVisitor.visitInsn(ARETURN);
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(191, label1);
			methodVisitor.visitFrame(Opcodes.F_NEW, 2, new Object[] { "org/spongepowered/asm/mixin/MixinEnvironment",
					"org/spongepowered/asm/mixin/refmap/IReferenceMapper" }, 0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitInsn(ARETURN);
			Label label3 = new Label();
			methodVisitor.visitLabel(label3);
			methodVisitor.visitLocalVariable("env", "Lorg/spongepowered/asm/mixin/MixinEnvironment;", null, label0,
					label3, 0);
			methodVisitor.visitLocalVariable("refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;", null,
					label0, label3, 1);
			methodVisitor.visitMaxs(4, 2);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "remapClassName",
					"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(196, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"getContext", "()Ljava/lang/String;", false);
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapClassNameWithContext",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
			methodVisitor.visitInsn(ARETURN);
			Label label1 = new Label();
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label1, 0);
			methodVisitor.visitLocalVariable("className", "Ljava/lang/String;", null, label0, label1, 1);
			methodVisitor.visitLocalVariable("inputClassName", "Ljava/lang/String;", null, label0, label1, 2);
			methodVisitor.visitMaxs(4, 3);
			methodVisitor.visitEnd();
		}
		{
			methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "remapClassNameWithContext",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null);
			methodVisitor.visitCode();
			Label label0 = new Label();
			methodVisitor.visitLabel(label0);
			methodVisitor.visitLineNumber(202, label0);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitTypeInsn(INSTANCEOF, "org/spongepowered/asm/mixin/refmap/IClassReferenceMapper");
			Label label1 = new Label();
			methodVisitor.visitJumpInsn(IFEQ, label1);
			Label label2 = new Label();
			methodVisitor.visitLabel(label2);
			methodVisitor.visitLineNumber(203, label2);
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitTypeInsn(CHECKCAST, "org/spongepowered/asm/mixin/refmap/IClassReferenceMapper");
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IClassReferenceMapper",
					"remapClassNameWithContext",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitVarInsn(ASTORE, 4);
			Label label3 = new Label();
			methodVisitor.visitLabel(label3);
			methodVisitor.visitLineNumber(204, label3);
			Label label4 = new Label();
			methodVisitor.visitJumpInsn(GOTO, label4);
			methodVisitor.visitLabel(label1);
			methodVisitor.visitLineNumber(205, label1);
			methodVisitor.visitFrame(Opcodes.F_NEW, 4,
					new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper", "java/lang/String",
							"java/lang/String", "java/lang/String" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"refMap", "Lorg/spongepowered/asm/mixin/refmap/IReferenceMapper;");
			methodVisitor.visitVarInsn(ALOAD, 1);
			methodVisitor.visitVarInsn(ALOAD, 2);
			methodVisitor.visitVarInsn(ALOAD, 3);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/refmap/IReferenceMapper",
					"remapWithContext", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
					true);
			methodVisitor.visitVarInsn(ASTORE, 4);
			methodVisitor.visitLabel(label4);
			methodVisitor.visitLineNumber(207, label4);
			methodVisitor.visitFrame(
					Opcodes.F_NEW, 5, new Object[] { "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
							"java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String" },
					0, new Object[] {});
			methodVisitor.visitVarInsn(ALOAD, 0);
			methodVisitor.visitFieldInsn(GETFIELD, "org/spongepowered/asm/mixin/refmap/RemappingReferenceMapper",
					"remapper", "Lorg/spongepowered/asm/mixin/extensibility/IRemapper;");
			methodVisitor.visitVarInsn(ALOAD, 4);
			methodVisitor.visitIntInsn(BIPUSH, 46);
			methodVisitor.visitIntInsn(BIPUSH, 47);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(CC)Ljava/lang/String;",
					false);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/asm/mixin/extensibility/IRemapper", "map",
					"(Ljava/lang/String;)Ljava/lang/String;", true);
			methodVisitor.visitInsn(ARETURN);
			Label label5 = new Label();
			methodVisitor.visitLabel(label5);
			methodVisitor.visitLocalVariable("this", "Lorg/spongepowered/asm/mixin/refmap/RemappingReferenceMapper;",
					null, label0, label5, 0);
			methodVisitor.visitLocalVariable("context", "Ljava/lang/String;", null, label0, label5, 1);
			methodVisitor.visitLocalVariable("className", "Ljava/lang/String;", null, label0, label5, 2);
			methodVisitor.visitLocalVariable("remapped", "Ljava/lang/String;", null, label0, label5, 3);
			methodVisitor.visitLocalVariable("origInfoString", "Ljava/lang/String;", null, label3, label1, 4);
			methodVisitor.visitLocalVariable("origInfoString", "Ljava/lang/String;", null, label4, label5, 4);
			methodVisitor.visitMaxs(4, 5);
			methodVisitor.visitEnd();
		}
		classWriter.visitEnd();
		
		return classWriter.toByteArray();
	}
}
