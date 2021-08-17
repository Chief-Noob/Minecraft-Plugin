package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.mongodb.client.model.Filters;
import java.util.*;
import org.bukkit.*;
import org.bson.Document;

public class LocationManager {
	private App app;
	private HashMap<String, Location> tagLocationMap;
	public final static String lobby_spawn = "lobby_spawn";
	public final static String house_spawn = "house_spawn";

	public LocationManager(App app) {
		this.app = app;
		tagLocationMap = new HashMap<>();
	}

	public void loadLocations() {
		List<Document> docList = app.dbClient.findMany("Location", Filters.empty());
		for (Document doc : docList) {
			tagLocationMap.put(doc.getString("tag"),
					new Location(this.app.worldManager.getWorldData(WorldManager.world_lobby)
							.getWorld(), doc.getDouble("x"), doc.getDouble("y"),
							doc.getDouble("z")));
		}
	}

	public Location getLocation(String tag) {
		return tagLocationMap.get(tag);
	}
}
