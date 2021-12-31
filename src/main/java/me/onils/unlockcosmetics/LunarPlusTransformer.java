package me.onils.unlockcosmetics;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LunarPlusTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return new byte[0];
        }


        if(className.startsWith("lunar/")){
            ClassReader cr = new ClassReader(classfileBuffer);
            if(cr.getSuperName().equals("java/lang/Object") && Arrays.asList(cr.getInterfaces()).contains("java/lang/Runnable")){
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                for(MethodNode method : cn.methods){
                    Set<String> stringsToMatch = new HashSet<>();
                    stringsToMatch.add("identifier");
                    stringsToMatch.add("value");

                    boolean matchesAllStrings = Arrays.stream(method.instructions.toArray())
                            .filter(LdcInsnNode.class::isInstance)
                            .map(LdcInsnNode.class::cast)
                            .map(ldc -> ldc.cst)
                            .filter(stringsToMatch::remove)
                            .anyMatch(__ -> stringsToMatch.isEmpty());

                    if(matchesAllStrings && method.desc.equals("(Lcom/google/gson/JsonArray;)V")){
                        InsnList inject = new InsnList();
                        inject.add(new TypeInsnNode(Opcodes.NEW, "com/google/gson/JsonObject"));
                        inject.add(new InsnNode(Opcodes.DUP));
                        inject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/google/gson/JsonObject", "<init>", "()V"));
                        inject.add(new VarInsnNode(Opcodes.ASTORE, method.maxLocals));

                        inject.add(new VarInsnNode(Opcodes.ALOAD, method.maxLocals));
                        inject.add(new LdcInsnNode("identifier"));
                        inject.add(new LdcInsnNode("LunarPlus"));
                        inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/google/gson/JsonObject", "addProperty", "(Ljava/lang/String;Ljava/lang/String;)V"));

                        inject.add(new VarInsnNode(Opcodes.ALOAD, method.maxLocals));
                        inject.add(new LdcInsnNode("value"));
                        inject.add(new InsnNode(Opcodes.ICONST_1));
                        inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"));
                        inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/google/gson/JsonObject", "addProperty", "(Ljava/lang/String;Ljava/lang/Boolean;)V"));

                        inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        inject.add(new VarInsnNode(Opcodes.ALOAD, method.maxLocals));
                        inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/google/gson/JsonArray", "add", "(Lcom/google/gson/JsonElement;)V"));

                        method.instructions.insert(inject);
                        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                        cn.accept(cw);
                        byte[] bytes = cw.toByteArray();
                        try(OutputStream os = new FileOutputStream("/home/nils/Desktop/lunarapi.class")){
                            os.write(bytes);
                        }catch (IOException ignored){}
                        return bytes;
                    }
                }
            }
        }

        return classfileBuffer;
    }
}
