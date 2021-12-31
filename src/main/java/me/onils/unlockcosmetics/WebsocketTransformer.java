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
                        for(MethodNode methodNode2 : cn.methods){
                            if(methodNode2.name.equals("onMessage") && methodNode2.desc.equals("(Ljava/nio/ByteBuffer;)V")){
                                for(AbstractInsnNode insnNode : methodNode2.instructions){
                                    if(insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL){
                                        MethodInsnNode methodInsnNode = (MethodInsnNode)insnNode;
                                        if(methodInsnNode.owner.equals("java/nio/ByteBuffer") && methodInsnNode.name.equals("array") && methodInsnNode.desc.equals("()[B")){
                                            methodNode2.instructions.insert(methodInsnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/onils/unlockcosmetics/proxy/Proxy", "receive", "([B)[B"));
                                        }
                                    }
                                }
                            }
                        }
                        MethodNode send = new MethodNode(
                                Opcodes.ACC_PUBLIC,
                                "send",
                                "([B)V",
                                null,
                                null
                        );

                        send.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        send.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        send.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/onils/unlockcosmetics/proxy/Proxy", "send", "([B)[B"));
                        send.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, cr.getSuperName(), send.name, send.desc));
                        send.instructions.add(new InsnNode(Opcodes.RETURN));
                        cn.methods.add(1, send);

                        ClassWriter cw = new LunarClassWriter(cr, ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES, loader);
                        cn.accept(cw);
                        byte[] bytes = cw.toByteArray();

                        try(OutputStream os = new FileOutputStream("/home/nils/Desktop/ws.class")){
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