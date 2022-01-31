package me.onils.unlockcosmetics.proxy.packet;

import io.netty.buffer.ByteBuf;
import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.impl.*;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public abstract class WSPacket {
    public static final Map<Integer, Class<? extends WSPacket>> REGISTRY;

    public abstract void write(PacketBuffer buffer);
    public abstract void read(PacketBuffer buffer);
    public abstract PacketState process(Proxy proxy);

    static {
        REGISTRY = new HashMap<>();

        REGISTRY.put(8, WSPacketCosmeticGive.class);
        REGISTRY.put(20, WSPacketCosmeticEquip.class);
        REGISTRY.put(39, WSPacketEmotePlay.class);
        REGISTRY.put(56, WSPacketEquippedEmotes.class);
        REGISTRY.put(57, WSPacketEmoteGive.class);
    }
}
