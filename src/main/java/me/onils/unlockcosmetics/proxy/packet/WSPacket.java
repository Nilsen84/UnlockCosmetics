package me.onils.unlockcosmetics.proxy.packet;

import io.netty.buffer.ByteBuf;
import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.impl.WSPacketCosmeticEquip;
import me.onils.unlockcosmetics.proxy.packet.impl.WSPacketCosmeticGive;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public abstract class WSPacket {
    public static final Map<Integer, Class<? extends WSPacket>> REGISTRY;

    public abstract void write(PacketBuffer buffer);
    public abstract void read(PacketBuffer buffer);
    public abstract void process(Proxy proxy);

    static {
        REGISTRY = new HashMap<>();

        REGISTRY.put(8, WSPacketCosmeticGive.class);
        REGISTRY.put(20, WSPacketCosmeticEquip.class);
    }
}
