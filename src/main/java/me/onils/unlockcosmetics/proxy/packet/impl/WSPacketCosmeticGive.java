package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.CosmeticIndexEntry;
import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WSPacketCosmeticGive extends WSPacket {
    private List<Integer> cosmetics;
    private UUID playerId;

    @Override
    public void write(PacketBuffer buffer) {

    }

    @Override
    public void read(PacketBuffer buffer) {
        this.playerId = new UUID(buffer.readLong(), buffer.readLong());

        int numCosmetics = buffer.readVarIntFromBuffer();
        Map<Integer, CosmeticIndexEntry> index = Proxy.getIndex();
        this.cosmetics = new ArrayList<>(numCosmetics);

        for(int i = 0; i < numCosmetics; ++i){
            int cosmeticId = buffer.readVarIntFromBuffer();
            boolean equipped = buffer.readBoolean();
            CosmeticIndexEntry entry = index.get(cosmeticId);
            if(entry != null){
                System.err.printf("%d, %b, %s\n", cosmeticId, equipped, entry.getName());
            }
        }
    }
}
