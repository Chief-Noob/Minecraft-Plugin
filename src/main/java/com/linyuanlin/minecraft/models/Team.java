package com.linyuanlin.minecraft.models;

import java.util.List;

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
