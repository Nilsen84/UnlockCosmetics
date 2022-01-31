package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.PacketState;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

public class WSPacketEmotePlay extends WSPacket {
    private int id;

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(id);
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.id = buffer.readInt();
    }

    @Override
    public PacketState process(Proxy proxy) {
        if(proxy.getPurchasedEmotes().contains(id)){
            return PacketState.UNTOUCHED;
        }
        return PacketState.CANCELLED;
    }
}
