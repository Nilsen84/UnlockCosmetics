package me.onils.unlockcosmetics.proxy;

import lombok.Getter;
import me.onils.unlockcosmetics.proxy.packet.WSPacket;
import me.onils.unlockcosmetics.util.PacketBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Proxy {
    @Getter
    private static Map<Integer, CosmeticIndexEntry> index = new HashMap<>();

    public static byte[] receive(byte[] buffer){
        PacketBuffer packetBuffer = new PacketBuffer(buffer);
        int packetId = packetBuffer.readVarIntFromBuffer();

        System.err.println("RECEIVED PACKET: " + packetId);

        Class<? extends WSPacket> packetClass = WSPacket.REGISTRY.get(packetId);
        if(packetClass != null){
            try{
                WSPacket packet = packetClass.getDeclaredConstructor().newInstance();
                packet.read(packetBuffer);


            }catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ex){
                ex.printStackTrace();
            }
        }

        return buffer;

    }

    public static byte[] send(byte[] buffer){
        System.err.println("SENT PACKET: " + new PacketBuffer(buffer).readVarIntFromBuffer());

        return buffer;
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
