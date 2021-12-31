package me.onils.unlockcosmetics.proxy;

import me.onils.unlockcosmetics.util.PacketBuffer;

public class Proxy {
    public static byte[] receive(byte[] buffer){
        System.err.println("RECEIVED PACKET: " + new PacketBuffer(buffer).readVarIntFromBuffer());

        return buffer;

    }

    public static byte[] send(byte[] buffer){
        System.err.println("SENT PACKET: " + new PacketBuffer(buffer).readVarIntFromBuffer());

        return buffer;
    }
}
