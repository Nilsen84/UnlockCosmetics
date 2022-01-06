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
                    boolean assetsWs = Arrays.stream(methodNode.instructions.toArray())
                            .filter(LdcInsnNode.class::isInstance)
                            .map(LdcInsnNode.class::cast)
                            .map(ldc -> ldc.cst)
                            .anyMatch("Assets"::equals);

                    if(assetsWs){
                        FieldNode proxy = new FieldNode(
                                Opcodes.ACC_PUBLIC,
                                "proxy",
                                "Lme/onils/unlockcosmetics/proxy/Proxy;",
                                null,
                                null
                        );

                        MethodNode send = new MethodNode(
                                Opcodes.ACC_PUBLIC,
                                "send",
                                "([B)V",
                                null,
                                null
                        );

                        send.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        send.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        send.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, proxy.name, proxy.desc));
                        send.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        send.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/onils/unlockcosmetics/proxy/Proxy", "send", "([B)[B"));
                        send.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, cr.getSuperName(), send.name, send.desc));
                        send.instructions.add(new InsnNode(Opcodes.RETURN));

                        for(MethodNode methodNode2 : cn.methods){
                            if(methodNode2.name.equals("onMessage") && methodNode2.desc.equals("(Ljava/nio/ByteBuffer;)V")){
                                for(AbstractInsnNode insnNode : methodNode2.instructions){
                                    if(insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL){
                                        MethodInsnNode methodInsnNode = (MethodInsnNode)insnNode;
                                        if(methodInsnNode.owner.equals("java/nio/ByteBuffer") && methodInsnNode.name.equals("array") && methodInsnNode.desc.equals("()[B")){
                                            InsnList inject = new InsnList();
                                            inject.add(new VarInsnNode(Opcodes.ASTORE, methodNode2.maxLocals));
                                            inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                            inject.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, proxy.name, proxy.desc));
                                            inject.add(new VarInsnNode(Opcodes.ALOAD, methodNode2.maxLocals));
                                            inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/onils/unlockcosmetics/proxy/Proxy", "receive", "([B)[B"));
                                            methodNode2.instructions.insert(methodInsnNode, inject);
                                        }
                                    }
                                }
                            }else if(methodNode2.name.equals("<init>") && methodNode2.desc.equals("(Ljava/util/Map;)V")){
                                AbstractInsnNode ret = methodNode2.instructions.getLast();
                                do{
                                    if(ret.getOpcode() == Opcodes.RETURN){
                                        break;
                                    }
                                }while ((ret = ret.getPrevious()) != null);
                                InsnList constructProxy = new InsnList();
                                constructProxy.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                constructProxy.add(new TypeInsnNode(Opcodes.NEW, "me/onils/unlockcosmetics/proxy/Proxy"));
                                constructProxy.add(new InsnNode(Opcodes.DUP));
                                constructProxy.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                constructProxy.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "me/onils/unlockcosmetics/proxy/Proxy", "<init>", "(Ljava/util/Map;)V"));
                                constructProxy.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, "proxy", "Lme/onils/unlockcosmetics/proxy/Proxy;"));
                                methodNode2.instructions.insertBefore(ret, constructProxy);
                            }
                        }

                        cn.methods.add(1, send);

                        cn.fields.add(proxy);

                        ClassWriter cw = new LunarClassWriter(cr, ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES, loader);
                        cn.accept(cw);

                        return cw.toByteArray();
                    }
                }
            }
        }
        return classfileBuffer;
    }
}