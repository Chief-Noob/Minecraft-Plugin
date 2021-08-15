package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import java.util.*;
import org.bson.Document;
import org.bukkit.*;

public class LocationManager {
	private App app;
	private HashMap<String, Location> tagLocationMap = new HashMap<>();

	public LocationManager(App app) {
		this.app = app;
	}

	public void loadLocations() {
		tagLocationMap.put("lobby_spawn", SpawnLocation());
	}

	private Location SpawnLocation() {
		Document doc = app.dbClient.findOne("Location", "tag", "lobby_spawn");
		return new Location(this.app.worldManager.getWorldData("world_lobby").world, doc.getInteger("x"),
				doc.getInteger("y"), doc.getInteger("z"));
	}

	public Location getLocation(String tag) {
		return tagLocationMap.get(tag);
	}
}
