package me.onils.unlockcosmetics.proxy;

import java.nio.ByteBuffer;

public class Proxy {
    public static byte[] receive(byte[] buffer){
        System.err.println("RECIEVED DATA");
        return buffer;

    }

    public static byte[] send(byte[] buffer){
        System.err.println("SENT DATA");
        return buffer;
    }
}
