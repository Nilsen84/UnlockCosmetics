package me.onils.unlockcosmetics;

import me.onils.unlockcosmetics.util.ASMUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.stream.Collectors;

public class EmoteTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!ASMUtil.isLunar(className)){
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if(cr.getInterfaces().length > 0 && ASMUtil.isLunar(cr.getSuperName())){
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            List<FieldNode> lists = ASMUtil.getFieldsOfType(cn, "Ljava/util/List;");
            List<FieldNode> biMaps = ASMUtil.getFieldsOfType(cn, "Lcom/google/common/collect/BiMap;");

            if(biMaps.size() == 1 && lists.size() == 1){
                FieldNode biMap = biMaps.get(0);
                if(!ASMUtil.isStatic(biMap)) return classfileBuffer;

                FieldNode emoteList = new FieldNode(
                        Opcodes.ACC_PUBLIC,
                        "emoteList",
                        "Ljava/util/List;",
                        null,
                        null
                );
                cn.fields.add(emoteList);

                ASMUtil.construct(cn, inject -> {
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    inject.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
                    inject.add(new InsnNode(Opcodes.DUP));
                    inject.add(new FieldInsnNode(Opcodes.GETSTATIC, cn.name, biMap.name, biMap.desc));
                    inject.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/google/common/collect/BiMap", "keySet", "()Ljava/util/Set;", true));
                    inject.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V"));
                    inject.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, emoteList.name, emoteList.desc));
                });

                for(MethodNode mn : cn.methods){
                    if(Type.getReturnType(mn.desc).getInternalName().equals("java/util/List")){
                        InsnList inject = new InsnList();
                        inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inject.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, emoteList.name, emoteList.desc));
                        inject.add(new InsnNode(Opcodes.ARETURN));
                        mn.instructions.insert(inject);
                    }else{
                        LabelNode dontOwn = ASMUtil.getPrevOfType(
                                ASMUtil.getConstant(mn, "Couldn't perform emote (%s) as you do not own it"),
                                LabelNode.class
                        );

                        if(dontOwn == null){
                            continue;
                        }

                        MethodInsnNode getPlayer = null;
                        for(AbstractInsnNode insnNode : mn.instructions){
                            if(insnNode.getOpcode() == Opcodes.INVOKEINTERFACE){
                                MethodInsnNode methodInsnNode = (MethodInsnNode)insnNode;

                                if(methodInsnNode.name.equals("bridge$getUniqueID")){
                                    getPlayer = (MethodInsnNode) ASMUtil.getPrevWithOpcode(insnNode, Opcodes.INVOKESTATIC);
                                    break;
                                }
                            }
                        }

                        if(getPlayer == null){
                            continue;
                        }

                        MethodNode setEmotePlay = null;
                        String emoteClass = null;

                        for(MethodNode method : cn.methods){
                            if(Type.getReturnType(method.desc) == Type.VOID_TYPE){
                                Type[] args = Type.getArgumentTypes(method.desc);
                                if(args.length == 3 &&
                                        args[0].getSort() == Type.OBJECT &&
                                        args[1].getSort() == Type.OBJECT &&
                                        args[2] == Type.INT_TYPE
                                ) {
                                    for(AbstractInsnNode insn : method.instructions){
                                        if(insn instanceof MethodInsnNode methodInsnNode
                                                && methodInsnNode.name.equals("bridge$setThirdPersonView")){
                                            setEmotePlay = method;
                                            emoteClass = args[1].getInternalName();
                                        }
                                    }
                                }
                            }
                        }

                        if(setEmotePlay == null){
                            continue;
                        }

                        int ownsEmoteIndex = mn.maxLocals;

                        for(AbstractInsnNode insnNode : mn.instructions){
                            if(insnNode.getOpcode() == Opcodes.IFEQ){
                                JumpInsnNode jumpNode = (JumpInsnNode) insnNode;
                                if(jumpNode.label.equals(dontOwn)){
                                    mn.instructions.insertBefore(jumpNode, new VarInsnNode(Opcodes.ISTORE, ownsEmoteIndex));
                                    mn.instructions.insertBefore(jumpNode, new InsnNode(Opcodes.ICONST_1));
                                }
                            }
                        }

                        MethodNode getEmoteById = null;
                        for(MethodNode method : cn.methods){
                            if(method.desc.equals("(I)L" + emoteClass + ';') && !ASMUtil.isStatic(method)){
                                getEmoteById = method;
                            }
                        }

                        if(getEmoteById == null){
                            continue;
                        }

                        for(AbstractInsnNode insnNode : mn.instructions){
                            if(insnNode.getOpcode() == Opcodes.NEW){
                                LabelNode sendPacket = new LabelNode();

                                InsnList inject = new InsnList();
                                inject.add(new VarInsnNode(Opcodes.ILOAD, ownsEmoteIndex));
                                inject.add(new JumpInsnNode(Opcodes.IFNE, sendPacket));
                                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                inject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, getPlayer.owner, getPlayer.name, getPlayer.desc));
                                inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                inject.add(new VarInsnNode(Opcodes.ILOAD, 1));
                                inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name, getEmoteById.name, getEmoteById.desc));
                                inject.add(new VarInsnNode(Opcodes.ILOAD, 2));
                                inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name, setEmotePlay.name, setEmotePlay.desc));

                                inject.add(new InsnNode(Opcodes.RETURN));
                                inject.add(sendPacket);

                                mn.instructions.insertBefore(insnNode, inject);
                            }
                        }
                    }
                }
                LunarClassWriter cw = new LunarClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader);
                cn.accept(cw);
                return cw.toByteArray();
            }
        }
        return classfileBuffer;
    }
}
