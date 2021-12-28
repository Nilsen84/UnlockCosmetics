package me.onils.unlockcosmetics;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class LunarClassWriter extends ClassWriter {
    ClassLoader lunarClassLoader;
    public LunarClassWriter(int flags, ClassLoader lcl){
        super(flags);
        this.lunarClassLoader = lcl;
    }


    public LunarClassWriter(ClassReader cr, int flags, ClassLoader lcl){
        super(cr, flags);
        this.lunarClassLoader = lcl;
    }

    @Override
    protected ClassLoader getClassLoader(){
        return lunarClassLoader;
    }
}
