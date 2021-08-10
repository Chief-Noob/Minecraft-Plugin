package com.linyuanlin.minecraft.models;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

public class Team {
	private List<PlayerData> playerList;
	private PlayerData leader;

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
		if (this.size() == 0) {
			this.leader = null;
		} else {
			for (PlayerData pd : this.playerList) {
				if (this.leader != pd) {
					this.leader = pd;
					break;
				}
			}
		}
	}

	public void add(PlayerData p) throws Exception {
		this.playerList.add(p);
	}

	public void delete(PlayerData p) throws Exception {
		if (this.leader == p)
			this.newLeader();

		this.playerList.remove((Object) p);
	}

	public boolean isContain(PlayerData p) {
		return this.playerList.contains((Object) p);
	}

	public PlayerData leader() {
		return this.leader;
	}

	public String allTeamMemberString() {
		String teamMemberNameString = new String();
		for (PlayerData pd : this.playerList) {
			teamMemberNameString += pd.player.getName() + ",";
		}
		return ChatColor.GOLD + teamMemberNameString;
	}
}
