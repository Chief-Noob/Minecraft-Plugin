package com.linyuanlin.minecraft.models;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final List<PlayerData> playerList = new ArrayList<>();
    private PlayerData leader;
    public final static int MAX_CAPACITY = 4;

    public boolean isEmpty() {
        return this.playerList.size() == 0;
    }

    public boolean isFull() {
        return this.playerList.size() == MAX_CAPACITY;
    }

    public int size() {
        return this.playerList.size();
    }

    public void sendMessageToAll(TextComponent msg) {
        for (PlayerData pd : this.playerList) {
            pd.player().spigot().sendMessage(msg);
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

    public void add(PlayerData p) {
        if (leader == null) {
            leader = p;
        }

        this.playerList.add(p);
    }

    public void delete(PlayerData p) {
        if (this.leader == p) {
            this.newLeader();
        }

        this.playerList.remove(p);
    }

    public boolean isContain(PlayerData p) {
        return this.playerList.contains(p);
    }

    public PlayerData leader() {
        return this.leader;
    }

    public String allTeamMemberString() {
        StringBuilder teamMemberNameString = new StringBuilder();
        for (PlayerData pd : this.playerList) {
            teamMemberNameString.append(pd.player().getName()).append(" ");
        }

        return ChatColor.GOLD + teamMemberNameString.toString();
    }
}
