package me.onils.unlockcosmetics.proxy.packet.impl;

import me.onils.unlockcosmetics.proxy.Proxy;
import me.onils.unlockcosmetics.proxy.packet.PacketState;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class WSPacketEmoteGive extends WSPacket {
    private Set<Integer> owned;
    private List<Integer> equipped;

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(owned.size());
        for(Integer integer : owned){
            buffer.writeVarIntToBuffer(integer);
        }

        buffer.writeVarIntToBuffer(equipped.size());
        for(Integer id : equipped){
            buffer.writeVarIntToBuffer(id);
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        int numEmotes = buffer.readVarIntFromBuffer();
        owned = new HashSet<>(numEmotes);

        for(int i = 0; i < numEmotes; ++i){
            owned.add(buffer.readVarIntFromBuffer());
        }

        int numEquipped = buffer.readVarIntFromBuffer();
        for(int i = 0; i < numEquipped; ++i){
            buffer.readVarIntFromBuffer();
        }
    }

    @Override
    public PacketState process(Proxy proxy) {
        equipped = new ArrayList<>();
        try(Scanner scanner = new Scanner(new File(System.getProperty("user.home") + "/.lunarclient/emotes"));){
            while(scanner.hasNextLine()){
                equipped.add(Integer.parseInt(scanner.nextLine()));
            }
        }catch (FileNotFoundException ignored){}

        proxy.setPurchasedEmotes(owned);
        return PacketState.MODIFIED;
    }
}
