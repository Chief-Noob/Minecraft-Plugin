package com.linyuanlin.minecraft.models;

import com.linyuanlin.minecraft.App;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerData {
    public Player player;
    public int balance = 0;
    public Document mongoObject;
    public Optional<Team> team;
    private HashMap<Player, Date> invitedTimeMap, inviteTimeMap;
    private App app;

    public PlayerData(App app, UUID uuid) throws Exception {
        this.app = app;

        this.player = Bukkit.getServer().getPlayer(uuid);

        this.mongoObject = new Document();

        this.invitedTimeMap = new HashMap<>();

        this.inviteTimeMap = new HashMap<>();

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
        this.wrapMongoObject();
        this.app.dbClient.insert("PlayerData", this.mongoObject);

        player.sendMessage(ChatColor.GRAY + "你的資料已自動保存至資料庫");
    }

    public void recordInvite(PlayerData p) {
        Date t = new Date();
        this.inviteTimeMap.put(p.player, t);
        p.invitedTimeMap.put(this.player, t);
    }

    public void destroyInvitedRecord() {// possible exception
        for (Player p : this.invitedTimeMap.keySet()) {
            invitedTimeMap.remove(p);
            app.allPlayers.get(p.getUniqueId()).inviteTimeMap.remove(this.player);
        }
    }

    public boolean isInviteCooling(PlayerData p) {
        Date t = inviteTimeMap.get(p.player);
        return t == null ? false : new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)).before(t);
    }

    public boolean isInvitedBy(PlayerData p) {
        if (p.inviteTimeMap.get(this.player) != null && this.invitedTimeMap.get(p.player) != null) {
            Date t = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
            if (this.invitedTimeMap.get(p.player).before(t) && p.inviteTimeMap.get(this.player).before(t)) {
                this.invitedTimeMap.remove((Object) p.player);
                p.inviteTimeMap.remove((Object) this.player);
                return false;
            }
            return true;
        }
        return false;
    }

    public String teamCapacityStatus() {
        return ChatColor.GRAY + "(" + this.team.get().size() + "/4)";
    }

    public void logOut(HashMap<UUID, PlayerData> allPlayers) {
        this.saveData();
        if (this.team.isPresent()) {
            this.player.performCommand("team leave");// command shouldn't include `/`
        }

        this.destroyInvitedRecord();
    }
}
