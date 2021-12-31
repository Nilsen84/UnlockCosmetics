package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WSPacketEquippedEmotes extends WSPacket {
    private Set<Integer> equipped;

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(equipped.size());

        for(Integer id : equipped){
            buffer.writeVarIntToBuffer(id);
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        int numEquipped = buffer.readVarIntFromBuffer();
        equipped = new HashSet<>(numEquipped);

        for(int i = 0; i < numEquipped; ++i){
            equipped.add(buffer.readVarIntFromBuffer());
        }
    }

    @Override
    public boolean process(Proxy proxy) {
        try(OutputStream os = new FileOutputStream(System.getProperty("user.home") + "/.lunarclient/emotes")){
            PrintStream printStream = new PrintStream(os, false);

            for(Integer id : this.equipped){
                printStream.println(id);
            }
            printStream.flush();
            printStream.close();
        }catch (IOException ignored) {}

        equipped.removeIf(id -> !proxy.getPurchasedCosmetics().contains(id));
        return false;
    }
}
