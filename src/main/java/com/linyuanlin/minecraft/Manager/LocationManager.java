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
		tagLocationMap.put("lobby_spawn", SpawnLocation());
	}

	private Location SpawnLocation() {
		try {
			Document doc = app.dbClient.findOne("Location", "tag", "lobby_spawn");
			return new Location(this.app.worldManager.getWorldData(WorldManager.world_lobby).world,
					doc.getInteger("x"), doc.getInteger("y"), doc.getInteger("z"));
		} catch (Exception exception) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			this.app.discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
		}
		return new Location(this.app.worldManager.getWorldData(WorldManager.world_lobby).world, 0, 60, 110);

	}

	public Location getLocation(String tag) {
		return tagLocationMap.get(tag);
	}
}
