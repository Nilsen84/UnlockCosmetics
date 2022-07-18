package me.onils.unlockcosmetics.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@UtilityClass
public class ASMUtil {
    public List<FieldNode> getFieldsOfType(ClassNode cn, String desc){
        return cn.fields.stream().filter(
                field -> desc.equals(field.desc)
        ).collect(Collectors.toList());
    }

    public boolean isStatic(FieldNode fn){
        return (fn.access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isStatic(MethodNode mn){
        return (mn.access & Opcodes.ACC_STATIC) != 0;
    }

    public void construct(ClassNode cn, Consumer<InsnList> consumer) {
        for (MethodNode methodNode : cn.methods) {
            if(!"<init>".equals(methodNode.name)) continue;

            for(AbstractInsnNode insn : methodNode.instructions){
                if(insn.getOpcode() == Opcodes.RETURN){
                    InsnList inject = new InsnList();
                    consumer.accept(inject);
                    methodNode.instructions.insertBefore(insn, inject);
                }
            }
        }
    }

    public LdcInsnNode getConstant(MethodNode mn, Object value) {
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof LdcInsnNode ldcNode && value.equals(ldcNode.cst)) {
                 return ldcNode;
            }
        }
        return null;
    }

    public <T extends AbstractInsnNode> T getNextOfType(AbstractInsnNode insn, Class<T> clazz){
        if(insn == null) return null;

        while((insn = insn.getNext()) != null){
            if(clazz.isInstance(insn)){
                return clazz.cast(insn);
            }
        }

        return null;
    }

    public <T extends AbstractInsnNode> T getPrevOfType(AbstractInsnNode insn, Class<T> clazz){
        if(insn == null) return null;

        while((insn = insn.getPrevious()) != null){
            if(clazz.isInstance(insn)){
                return clazz.cast(insn);
            }
        }

        return null;
    }

    public AbstractInsnNode getPrevWithOpcode(AbstractInsnNode insn, int opcode){
        if(insn == null) return null;

        while((insn = insn.getPrevious()) != null){
            if(insn.getOpcode() == opcode){
                return insn;
            }
        }
        return null;
    }
    public AbstractInsnNode getNextWithOpcode(AbstractInsnNode insn, int opcode){
        if(insn == null) return null;

        while((insn = insn.getNext()) != null){
            if(insn.getOpcode() == opcode){
                return insn;
            }
        }
        return null;
    }
}
