package com.linyuanlin.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;

public class WorldManager {

    public void loadWorlds() {
        loadHouseWorld();
        loadLobbyWorld();
    }

    // Load the house world into server
    private void loadLobbyWorld() {
        final String worldName = "world_lobby";
        Bukkit.createWorld(new WorldCreator(worldName));
    }

    // Load the house world into server
    private void loadHouseWorld() {
        final String worldName = "house_world";
        Bukkit.createWorld(new WorldCreator(worldName));
    }

    // Check if a world folder is exist
    private boolean checkWorldDataExist(String worldName) {

        File file = Bukkit.getWorldContainer();

        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
        assert directories != null;

        for (String d : directories) {
            if (d.equals(worldName)) return true;
        }

        return false;

    }

}
