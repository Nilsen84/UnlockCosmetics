package me.onils.unlockcosmetics.proxy;

import lombok.Getter;

public class CosmeticIndexEntry {
    @Getter
    private final int id;
    @Getter
    private final String name;

    public CosmeticIndexEntry(String line){
        String[] split = line.split(",");
        this.id = Integer.parseInt(split[0]);
        this.name = split[3];
    }

}
