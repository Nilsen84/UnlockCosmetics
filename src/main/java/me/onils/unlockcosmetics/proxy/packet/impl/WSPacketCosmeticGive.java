package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.CosmeticIndexEntry;
import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.util.*;

public class WSPacketCosmeticGive extends WSPacket {
    private Map<Integer, Boolean> cosmetics;
    private UUID playerId;
    private int color;
    private boolean update;
    private boolean lunarPlus;
    private boolean clothCloaks;

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeLong(playerId.getMostSignificantBits());
        buffer.writeLong(playerId.getLeastSignificantBits());

        buffer.writeVarIntToBuffer(cosmetics.size());

        for(Map.Entry<Integer, Boolean> cosmetic : cosmetics.entrySet()){
            buffer.writeVarIntToBuffer(cosmetic.getKey());
            buffer.writeBoolean(cosmetic.getValue());
        }

        buffer.writeInt(color);
        buffer.writeBoolean(true);
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.playerId = new UUID(buffer.readLong(), buffer.readLong());

        int numCosmetics = buffer.readVarIntFromBuffer();
        Map<Integer, CosmeticIndexEntry> index = Proxy.getIndex();
        this.cosmetics = new HashMap<>(numCosmetics);

        for(int i = 0; i < numCosmetics; ++i){
            int cosmeticId = buffer.readVarIntFromBuffer();
            boolean equipped = buffer.readBoolean();
            CosmeticIndexEntry entry = index.get(cosmeticId);
            if(entry != null){
                this.cosmetics.put(cosmeticId, equipped);
            }
        }
        this.color = buffer.readInt();
        this.update = buffer.readBoolean();
        this.lunarPlus = buffer.readBoolean();
        this.clothCloaks = buffer.readBoolean();
    }

    @Override
    public boolean process(Proxy proxy) {
        if(this.playerId.equals(proxy.getPlayerId())){
            proxy.setLunarPlus(lunarPlus);
            proxy.getPurchasedCosmetics().addAll(this.cosmetics.keySet());

            for(CosmeticIndexEntry entry : Proxy.getIndex().values()){
                cosmetics.putIfAbsent(entry.getId(), false);
            }
            return true;
        }
        return false;
    }
}
