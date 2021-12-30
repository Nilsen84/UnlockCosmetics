package me.onils.unlockcosmetics;

import me.onils.unlockcosmetics.websocket.ProxyServer;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String args, Instrumentation inst){
        ProxyServer proxyServer = new ProxyServer(1234);
        proxyServer.start();

        inst.addTransformer(new WebsocketTransformer());
    }
}
