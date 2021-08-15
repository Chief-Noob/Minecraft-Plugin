package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.models.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class WorldManager {

    private HashMap<String, WorldData> worlds;

    public void loadWorlds() {
        worlds = new HashMap<>();
        loadHouseWorld();
        loadLobbyWorld();
    }

    // Load the house world into server
    private void loadLobbyWorld() {
        final String worldName = "world_lobby";
        World world = Bukkit.createWorld(new WorldCreator(worldName));
        worlds.put(worldName, new WorldData(worldName, "大廳", "所有玩家一開始進入遊戲時的交誼廳，擁有通往各個區域的傳送門", world));
    }

    // Load the house world into server
    private void loadHouseWorld() {
        final String worldName = "house_world";
        World world = Bukkit.createWorld(new WorldCreator(worldName));
        worlds.put(worldName, new WorldData(worldName, "小屋世界", "你能用收集來的資源建造你的居所，也能儲存你的戰利品以及勳章和物資", world));
    }

    public WorldData getWorldData(String worldId) {
        return worlds.get(worldId);
    }

    // Check if a world folder exist
    public boolean checkWorldDataExist(String worldName) {

        File file = Bukkit.getWorldContainer();

        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        for (String d : Objects.requireNonNull(directories)) {
            if (d.equals(worldName))
                return true;
        }

        return false;
    }

}
