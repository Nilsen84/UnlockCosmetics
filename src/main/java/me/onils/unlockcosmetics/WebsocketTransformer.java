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

public class WebsocketTransformer implements ClassFileTransformer {
    private int count = 0;

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!className.startsWith("lunar/"))
            return classfileBuffer;

        ClassReader cr = new ClassReader(classfileBuffer);
        if(cr.getSuperName().equals("org/java_websocket/client/WebSocketClient")) {
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            for(MethodNode methodNode : cn.methods){
                if(methodNode.name.equals("<init>")){
                    System.err.println("CONSTRUCTOR");

                    boolean assetsWs = Arrays.stream(methodNode.instructions.toArray())
                            .filter(LdcInsnNode.class::isInstance)
                            .map(LdcInsnNode.class::cast)
                            .map(ldc -> ldc.cst)
                            .anyMatch("Assets"::equals);

                    if(assetsWs){
                        for(AbstractInsnNode insnNode : methodNode.instructions){
                            if(insnNode.getOpcode() == Opcodes.INVOKESPECIAL){
                                System.err.println("INVOKESPECIAL");
                                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                                if(methodInsnNode.owner.equals("java/net/URI") && methodInsnNode.name.equals("<init>")){
                                    System.err.println("URI");
                                    InsnList inject = new InsnList();
                                    inject.add(new InsnNode(Opcodes.POP));
                                    inject.add(new LdcInsnNode("ws://localhost:1234"));
                                    methodNode.instructions.insertBefore(methodInsnNode, inject);
                                    break;
                                }
                            }
                        }

                        ClassWriter cw = new ClassWriter(cr, 0);
                        cn.accept(cw);
                        return cw.toByteArray();
                    }
                }
            }
        }
        return classfileBuffer;
    }
}