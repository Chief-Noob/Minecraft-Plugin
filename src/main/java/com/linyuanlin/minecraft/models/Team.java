package com.linyuanlin.minecraft.models;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Team {
	public List<PlayerData> playerList;
	public PlayerData leader;

	public Team(List<PlayerData> playerList) {
		this.playerList = playerList;
		this.leader = playerList.get(0);
	}

	public boolean isEmpty() {
		return this.playerList.size() == 0;
	}

	public boolean isFull() {
		return this.playerList.size() == 4;
	}

	public int size() {
		return this.playerList.size();
	}
}
