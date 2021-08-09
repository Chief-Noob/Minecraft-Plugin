package com.linyuanlin.minecraft.models;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.mongodb.MongodbClient;
import com.mongodb.BasicDBObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

<<<<<<< HEAD
import java.util.HashMap;
=======
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
>>>>>>> a50e1de2b7255769c7b29d268ecf954e006892df
import java.util.concurrent.TimeUnit;

public class PlayerData {
    public Player player;
    public int balance = 0;
    public BasicDBObject mongoObject;
    public Optional<Team> team = Optional.empty();
    private App app;
    private InviteCommand inviteCommand;

<<<<<<< HEAD
    public HashMap<Player, Date> invitedTime = new HashMap<>();

    public PlayerData(UUID uuid) throws Exception {
=======
    public PlayerData(App app, UUID uuid) throws Exception {
        this.app = app;
>>>>>>> a50e1de2b7255769c7b29d268ecf954e006892df
        this.player = Bukkit.getServer().getPlayer(uuid);

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

<<<<<<< HEAD
    public boolean inviteIsCooling(Player p) {
=======
    public boolean inviteIsCooling() {
>>>>>>> a50e1de2b7255769c7b29d268ecf954e006892df
        Date date = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
        return date.after(invitedTime.get(p));
    }

<<<<<<< HEAD
    public boolean isInvitedBy(PlayerData p) {
        return p.invitedTime.get(this.player) != null;
=======
    public Player invitedPlayer() {
        return inviteCommand.invitedPlayer;
>>>>>>> a50e1de2b7255769c7b29d268ecf954e006892df
    }
}
