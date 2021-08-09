package com.linyuanlin.minecraft.models;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.mongodb.MongodbClient;
import com.mongodb.BasicDBObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.lang.StackWalker.Option;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerData {
  public Player player;
  public int balance = 0;
  public BasicDBObject mongoObject;
  public Optional<Team> team;
  private App app;
  public HashMap<Player, Date> invitedTime;

  public PlayerData(App app, UUID uuid) throws Exception {
    this.app = app;

    this.player = Bukkit.getServer().getPlayer(uuid);

    this.mongoObject = new BasicDBObject();

        this.invitedTime = new HashMap<>();

        this.team = Optional.empty();

        if (this.player == null)
            throw new Exception("PLAYER_NOT_ONLINE");

    /* Import data from database */

    player.sendMessage(ChatColor.GRAY + "你的資料已從資料庫同步完成");
  }

  public void wrapMongoObject() {
    this.mongoObject.append("balance", this.balance);
  }

  public void saveData() {
    /* Save data into database */
    MongodbClient client = new MongodbClient(this.app, "PlayerData");
    this.wrapMongoObject();
    client.insert(this.mongoObject);

    player.sendMessage(ChatColor.GRAY + "你的資料已自動保存至資料庫");
  }

    public boolean inviteIsCooling(Player p) {
        Date t = invitedTime.get(p);
        return t == null ? false : new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)).before(t);
    }

  public boolean isInvitedBy(PlayerData p) {
    return p.invitedTime.get(this.player) != null;
  }
}
