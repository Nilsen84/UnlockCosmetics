package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class WSPacketEquippedEmotes extends WSPacket {
    private List<Integer> equipped;

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
        equipped = new ArrayList<>(numEquipped);

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
