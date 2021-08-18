package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import org.bson.Document;
import org.bukkit.Location;

import java.util.HashMap;

public class LocationManager {
    public final static String lobby_spawn = "lobby_spawn";
    private final App app;
    private final HashMap<String, Location> tagLocationMap;

    public LocationManager(App app) {
        this.app = app;
        tagLocationMap = new HashMap<>();
    }

    public void loadLocations() {
        Document doc = app.dbClient.findOne("Location", "tag", lobby_spawn);
        tagLocationMap.put(lobby_spawn,
                new Location(this.app.worldManager.getWorldData(WorldManager.world_lobby).world,
                        doc.getDouble("x"), doc.getDouble("y"), doc.getDouble("z")));
    }

    public Location getLocation(String tag) {
        return tagLocationMap.get(tag);
    }
}
