package com.linyuanlin.minecraft.models;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class Team {
    private final List<PlayerData> playerList = new ArrayList<>();
    private PlayerData leader;

    /*
     * constants
     */
    public final static int MAX_CAPACITY = 4;

    /*
     * return true if this team is empty
     */
    public boolean isEmpty() {
        return this.playerList.size() == 0;
    }

    /*
     * return true if this team is full
     */
    public boolean isFull() {
        return this.playerList.size() == MAX_CAPACITY;
    }

    /*
     * return the number of members of this team
     */
    public int size() {
        return this.playerList.size();
    }

    /*
     * send message to chat box of all members in the team
     */
    public void sendMessageToAll(TextComponent msg) {
        for (PlayerData pd : this.playerList) {
            pd.player().spigot().sendMessage(msg);
        }
    }

    /*
     * assign a new leader among the members
     */
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

    /*
     * add a player to this team
     * 
     * if we have no leader initially, assign this player as the leader
     */
    public void add(PlayerData p) {
        if (leader == null) {
            leader = p;
        }

        this.playerList.add(p);
    }

    /*
     * delete a player in the team
     */
    public boolean delete(PlayerData p) {
        if (this.leader == p) {
            this.newLeader();
        }

        return this.playerList.remove(p);
    }

    /*
     * return true if this team contain this player
     */
    public boolean isTeamMember(PlayerData p) {
        return this.playerList.contains(p);
    }

    /*
     * return this team's leader
     */
    public PlayerData leader() {
        return this.leader;
    }

    /*
     * return all member's name as a concatenated sting
     */
    public String allTeamMemberString() {
        StringBuilder teamMemberNameString = new StringBuilder();
        for (PlayerData pd : this.playerList) {
            teamMemberNameString.append(pd.player().getName()).append(" ");
        }

        return ChatColor.GOLD + teamMemberNameString.toString();
    }
}
