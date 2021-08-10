package com.linyuanlin.minecraft.models;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
	public List<PlayerData> playerList;
	public PlayerData leader;

	public boolean isEmpty() {
		return this.playerList.size() == 0;
	}

	public boolean isFull() {
		return this.playerList.size() == 4;
	}

	public int size() {
		return this.playerList.size();
	}

	public void sendMessageToAll(TextComponent msg) {
		for (PlayerData pd : this.playerList) {
			pd.player.spigot().sendMessage(msg);
		}
	}

	public void newLeader() {
		for (PlayerData pd : this.playerList) {
			if (this.leader != pd) {
				this.leader = pd;
				break;
			}
		}
	}

	public void add(PlayerData p) throws Exception {
		this.playerList.add(p);
	}

	public void delete(PlayerData p) throws Exception {
		this.playerList.remove((Object) p);
	}

	public boolean contain(PlayerData p){
		return this.playerList.contains((Object) p);
	}
}
