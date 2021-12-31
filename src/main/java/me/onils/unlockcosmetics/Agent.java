package me.onils.unlockcosmetics;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String args, Instrumentation inst){
        inst.addTransformer(new WebsocketTransformer());
        inst.addTransformer(new EmoteTransformer());
        inst.addTransformer(new LunarPlusTransformer());
    }
}
