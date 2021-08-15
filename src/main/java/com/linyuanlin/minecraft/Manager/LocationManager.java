package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import java.util.*;
import org.bson.Document;
import org.bukkit.*;
import java.io.*;

public class LocationManager {
	private App app;
	private HashMap<String, Location> tagLocationMap = new HashMap<>();

	public LocationManager(App app) {
		this.app = app;
	}

	public void loadLocations() {
		Document doc = app.dbClient.findOne("Location", "tag", "lobby_spawn");
		tagLocationMap.put("lobby_spawn",
				new Location(this.app.worldManager.getWorldData(WorldManager.world_lobby).world,
						doc.getInteger("x"), doc.getInteger("y"), doc.getInteger("z")));
	}

	public Location getLocation(String tag) {
		return tagLocationMap.get(tag);
	}
}
