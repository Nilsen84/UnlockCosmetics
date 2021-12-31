package me.onils.unlockcosmetics;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.stream.Collectors;

public class EmoteTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return new byte[0];
        }

        if(className.startsWith("lunar/")){
            ClassReader cr = new ClassReader(classfileBuffer);
            if(cr.getInterfaces().length > 0 && cr.getSuperName().startsWith("lunar/")){
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);

                List<FieldNode> lists = cn.fields.stream()
                        .filter(field -> "Ljava/util/List;".equals(field.desc))
                        .collect(Collectors.toList());

                List<FieldNode> biMaps = cn.fields.stream()
                        .filter(field -> "Lcom/google/common/collect/BiMap;".equals(field.desc))
                        .collect(Collectors.toList());

                if(biMaps.size() == 1 && lists.size() == 1){
                    FieldNode biMap = biMaps.get(0);

                    FieldNode emoteList = new FieldNode(Opcodes.ACC_PUBLIC, "emoteList", "Ljava/util/List;", null, null);
                    cn.fields.add(emoteList);

                    for(MethodNode methodNode : cn.methods){
                        if(Type.getReturnType(methodNode.desc).getInternalName().equals("java/util/List")){
                            InsnList inject = new InsnList();
                            inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            inject.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, emoteList.name, emoteList.desc));
                            inject.add(new InsnNode(Opcodes.ARETURN));
                            methodNode.instructions.insert(inject);
                        }else if("<init>".equals(methodNode.name)){
                            AbstractInsnNode ret = methodNode.instructions.getLast();
                            do {
                                if(ret.getOpcode() == Opcodes.RETURN){
                                    InsnList inject = new InsnList();
                                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inject.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
                                    inject.add(new InsnNode(Opcodes.DUP));
                                    inject.add(new FieldInsnNode(Opcodes.GETSTATIC, cn.name, biMap.name, biMap.desc));
                                    inject.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/google/common/collect/BiMap", "keySet", "()Ljava/util/Set;", true));
                                    inject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V"));
                                    inject.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, emoteList.name, emoteList.desc));

                                    methodNode.instructions.insertBefore(ret, inject);
                                    break;
                                }
                            }while ((ret = ret.getPrevious()) != null);
                        }else{
                            LabelNode dontOwn = null;
                            outer:
                            for(AbstractInsnNode insnNode : methodNode.instructions){
                                if(insnNode.getOpcode() == Opcodes.LDC){
                                    LdcInsnNode ldcNode = (LdcInsnNode) insnNode;

                                    if("Couldn't perform emote (%s) as you do not own it".equals(ldcNode.cst)){
                                        while((insnNode = insnNode.getPrevious()) != null){
                                            if(insnNode instanceof LabelNode){
                                                dontOwn = (LabelNode) insnNode;
                                                break outer;
                                            }
                                        }
                                    }
                                }
                            }

                            if(dontOwn == null){
                                continue;
                            }

                            MethodInsnNode getPlayer = null;
                            outer:
                            for(AbstractInsnNode insnNode : methodNode.instructions){
                                if(insnNode.getOpcode() == Opcodes.INVOKEINTERFACE){
                                    MethodInsnNode methodInsnNode = (MethodInsnNode)insnNode;

                                    if(methodInsnNode.name.equals("bridge$getUniqueID")){
                                        AbstractInsnNode tmp = insnNode;
                                        while((tmp = tmp.getPrevious()) != null){
                                            if(tmp.getOpcode() == Opcodes.INVOKESTATIC){
                                                getPlayer = (MethodInsnNode) tmp;
                                                break outer;
                                            }
                                        }
                                    }
                                }
                            }

                            if(getPlayer == null){
                                System.err.println("GET PLAYER IS NULL");
                                continue;
                            }

                            Type[] arguments = Type.getArgumentTypes(methodNode.desc);

                            if(arguments.length <= 0)
                                continue;

                            MethodNode setEmotePlay = null;

                            for(MethodNode method : cn.methods){
                                if(Type.getReturnType(method.desc) == Type.VOID_TYPE){
                                    Type[] args = Type.getArgumentTypes(method.desc);
                                    if(args.length == 2 && args[1].equals(arguments[0])){
                                        setEmotePlay = method;
                                    }
                                }
                            }

                            if(setEmotePlay == null){
                                System.err.println("SET EMOTE PLAY IS NULL");
                                continue;
                            }

                            int ownsEmoteIndex = methodNode.maxLocals;

                            for(AbstractInsnNode insnNode : methodNode.instructions){
                                if(insnNode.getOpcode() == Opcodes.IFEQ){
                                    JumpInsnNode jumpNode = (JumpInsnNode) insnNode;
                                    if(jumpNode.label.equals(dontOwn)){
                                        methodNode.instructions.insertBefore(jumpNode, new VarInsnNode(Opcodes.ISTORE, ownsEmoteIndex));
                                        methodNode.instructions.insertBefore(jumpNode, new InsnNode(Opcodes.ICONST_1));
                                    }
                                }
                            }

                            for(AbstractInsnNode insnNode : methodNode.instructions){
                                if(insnNode.getOpcode() == Opcodes.NEW){
                                    LabelNode sendPacket = new LabelNode();

                                    InsnList inject = new InsnList();
                                    inject.add(new VarInsnNode(Opcodes.ILOAD, ownsEmoteIndex));
                                    inject.add(new JumpInsnNode(Opcodes.IFNE, sendPacket));
                                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, getPlayer.owner, getPlayer.name, getPlayer.desc));
                                    inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                    inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name, setEmotePlay.name, setEmotePlay.desc));

                                    inject.add(new InsnNode(Opcodes.RETURN));
                                    inject.add(sendPacket);

                                    methodNode.instructions.insertBefore(insnNode, inject);
                                }
                            }
                        }
                    }
                    LunarClassWriter cw = new LunarClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader);
                    cn.accept(cw);
                    return cw.toByteArray();
                }
            }
        }
        return classfileBuffer;
    }
}
