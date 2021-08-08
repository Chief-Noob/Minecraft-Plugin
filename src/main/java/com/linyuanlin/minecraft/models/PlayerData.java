package com.linyuanlin.minecraft.models;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlayerData {

    public Player player;

    public int balance = 0;
    
    public Team team;

    public PlayerData(UUID uuid) throws Exception {

        this.player = Bukkit.getServer().getPlayer(uuid);

        if (this.player == null) throw new Exception("PLAYER_NOT_ONLINE");

        /* Import data from database */

        player.sendMessage(ChatColor.GRAY + "你的資料已從資料庫同步完成");
    }

    public void saveData() {

        /* Save data into database */

        player.sendMessage(ChatColor.GRAY + "你的資料已自動保存至資料庫");

    }

}
