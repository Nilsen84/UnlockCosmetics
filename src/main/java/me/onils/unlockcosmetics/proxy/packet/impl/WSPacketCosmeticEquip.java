package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public class WSPacketCosmeticEquip extends WSPacket {
    public Map<Long, Boolean> cosmetics;

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(cosmetics.size());

        for(Map.Entry<Long, Boolean> entry : cosmetics.entrySet()){
            buffer.writeLong(entry.getKey());
            buffer.writeBoolean(entry.getValue());
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.cosmetics = new HashMap<>();
        int size = buffer.readInt();
        for(int i = 0; i < size; ++i){
            cosmetics.put(buffer.readLong(), buffer.readBoolean());
        }
    }

    @Override
    public boolean process(Proxy proxy) {
        cosmetics.keySet().removeIf(id -> !proxy.getPurchasedCosmetics().contains(id.intValue()));
        return true;
    }
}
