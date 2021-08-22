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

    /*
     * return the world Id
     */
    public String getWoldId() {
        return worldId;
    }

    /*
     * return the world name
     */
    public String getWorldName() {
        return worldName;
    }

    /*
     * return the world description
     */
    public String getWorldDescription() {
        return worldDescription;
    }

    /*
     * return the world
     */
    public World getWorld() {
        return world;
    }
}
