package com.linyuanlin.minecraft.models;

import org.bukkit.*;

public class WorldData {
    private String worldId;
    private String worldName;
    private String worldDescription;
    private World world;

    public WorldData(String id, String name, String description, World world) {
        this.worldId = id;

        this.worldName = name;

        this.worldDescription = description;

        this.world = world;
    }

    public String getWoldId() {
        return worldId;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getWorldDescription() {
        return worldDescription;
    }

    public World getWorld() {
        return world;
    }
}
