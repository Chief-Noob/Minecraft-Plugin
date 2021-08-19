package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.mongodb.client.model.Filters;
import java.util.*;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.bson.Document;

public class LocationManager {
	/*
	 * constants
	 */
	public final static String lobby_spawn = "lobby_spawn";
	public final static String house_spawn = "house_spawn";

	private HashMap<String, Location> tagLocationMap = new HashMap<>();;

	public void loadLocations() {
		List<Document> docList = App.getPlugin().dbClient.findMany("Location", Filters.empty());
		for (Document doc : docList) {
			tagLocationMap.put(doc.getString("tag"), this.getLocation(
					App.getPlugin().worldManager.getWorldData(WorldManager.world_lobby).getWorld(),
					doc));
		}
	}

	private Location getLocation(@NotNull World world, @NotNull Document doc) {
		return new Location(world, doc.getDouble("x"), doc.getDouble("y"), doc.getDouble("z"));

	}

	public Location getLocation(String tag) {
		return tagLocationMap.get(tag);
	}
}
