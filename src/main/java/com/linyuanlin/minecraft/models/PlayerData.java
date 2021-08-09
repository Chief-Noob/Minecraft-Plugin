package com.linyuanlin.minecraft.models;

import com.linyuanlin.minecraft.mongodb.MongodbClient;
import com.mongodb.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerData {
  public Player player;

  public int balance = 0;

  public BasicDBObject mongoObject;

  public Optional<Team> team = Optional.empty();

  public PlayerData(UUID uuid) throws Exception {
    this.player = Bukkit.getServer().getPlayer(uuid);

    if (this.player == null) throw new Exception("PLAYER_NOT_ONLINE");

    /* Import data from database */

    player.sendMessage(ChatColor.GRAY + "你的資料已從資料庫同步完成");
  }

  public void wrapMongoObject() {
    this.mongoObject.append("balance", this.balance);
  }

  public void saveData() {
    /* Save data into database */
    MongodbClient client = new MongodbClient("PlayerData");
    this.wrapMongoObject();
    client.insert(this.mongoObject);

    player.sendMessage(ChatColor.GRAY + "你的資料已自動保存至資料庫");
  }
}
