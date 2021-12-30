package me.onils.unlockcosmetics.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProxyServer extends WebSocketServer {
    public ProxyServer(int port){
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Map<String, String> headers = new HashMap<>();

        handshake.iterateHttpFields().forEachRemaining(key -> {
            headers.put(key, handshake.getFieldValue(key));
        });

        for(Map.Entry<String, String> entry : headers.entrySet()){
            System.err.printf("%s: %s\n", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.err.println(remote + " " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
    }
}
