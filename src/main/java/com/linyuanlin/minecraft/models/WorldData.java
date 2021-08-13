package com.linyuanlin.minecraft.models;

import org.bukkit.World;

public class WorldData {

    public String worldId;
    public String worldName;
    public String worldDescription;
    public World world;

    public WorldData(String id, String name, String description, World world) {
        worldId = id;
        worldName = name;
        worldDescription = description;
        this.world = world;
    }

}
