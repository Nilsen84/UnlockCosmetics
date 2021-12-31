package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public class WSPacketCosmeticEquip extends WSPacket {
    private Map<Long, Boolean> cosmetics;
    private boolean clothCloaks;

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(cosmetics.size());

        for(Map.Entry<Long, Boolean> entry : cosmetics.entrySet()){
            buffer.writeLong(entry.getKey());
            buffer.writeBoolean(entry.getValue());
        }
        buffer.writeBoolean(clothCloaks);
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.cosmetics = new HashMap<>();
        int size = buffer.readInt();
        for(int i = 0; i < size; ++i){
            cosmetics.put(buffer.readLong(), buffer.readBoolean());
        }
        this.clothCloaks = buffer.readBoolean();
    }

    @Override
    public boolean process(Proxy proxy) {
        if(!proxy.isLunarPlus())
            this.clothCloaks = false;
        this.cosmetics.keySet().removeIf(id -> !proxy.getPurchasedCosmetics().contains(id.intValue()));
        return true;
    }
}
