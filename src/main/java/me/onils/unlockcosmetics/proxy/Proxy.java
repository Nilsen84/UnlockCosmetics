package me.onils.unlockcosmetics.proxy;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
@Setter
public class Proxy {
    @Getter
    private static final Map<Integer, CosmeticIndexEntry> index = new HashMap<>();

    private Set<Integer> purchasedCosmetics = new HashSet<>();

    private Set<Integer> purchasedEmotes = new HashSet<>();

    private boolean lunarPlus = false;

    private final UUID playerId;

    public Proxy(Map<String, String> headers){
        playerId = UUID.fromString(headers.get("playerId"));
    }

    private static byte[] packetToBytes(int packetId, WSPacket packet){
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

        packetBuffer.writeVarIntToBuffer(packetId);
        packet.write(packetBuffer);

        byte[] bytes = new byte[packetBuffer.readableBytes()];
        packetBuffer.readBytes(bytes);
        packetBuffer.release();
        return bytes;
    }

    private byte[] processPacket(byte[] buffer){
        PacketBuffer packetBuffer = new PacketBuffer(buffer);
        int packetId = packetBuffer.readVarIntFromBuffer();

        Class<? extends WSPacket> packetClass = WSPacket.REGISTRY.get(packetId);
        if(packetClass != null) {
            try {
                WSPacket packet = packetClass.getDeclaredConstructor().newInstance();
                packet.read(packetBuffer);
                packetBuffer.release();
                if(packet.process(this)){
                    return packetToBytes(packetId, packet);
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }
        return buffer;
    }

    public byte[] receive(byte[] buffer){
        return processPacket(buffer);
    }

    public byte[] send(byte[] buffer){
        return processPacket(buffer);
    }

    static {
        try{
            Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + "/.lunarclient/textures/assets/lunar/cosmetics/index")));

            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                if(line.isEmpty())
                    continue;
                CosmeticIndexEntry indexEntry = new CosmeticIndexEntry(line);
                index.put(indexEntry.getId(), indexEntry);
            }
        }catch (FileNotFoundException ex){
            System.err.println("INDEX COULDN'T BE FOUND");
            System.exit(1);
        }
    }
}
