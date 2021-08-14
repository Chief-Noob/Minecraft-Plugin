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
    public Optional<Team> team;
    private HashMap<Player, Date> invitedTimeMap, inviteTimeMap;
    private App app;

    public PlayerData(App app, UUID uuid) throws Exception {
        this.app = app;

        Document d = app.dbClient.findOne("PlayerData", "uuid", uuid.toString());

        this.player = Bukkit.getServer().getPlayer(uuid);

        this.invitedTimeMap = new HashMap<>();

        this.inviteTimeMap = new HashMap<>();

        this.team = Optional.empty();

        if (this.player == null)
            throw new Exception("PLAYER_NOT_ONLINE");

        if (d != null) {
            this.balance = d.getInteger("balance");
        } else {
            Document newDocument = new Document();
            newDocument.append("uuid", player.getUniqueId().toString());
            newDocument.append("balance", this.balance);
            app.dbClient.insert("PlayerData", newDocument);
        }

        player.sendMessage(ChatColor.GRAY + "你的資料已從資料庫同步完成");
    }

    public void saveData() {
        Document d = new Document();
        d.append("uuid", player.getUniqueId().toString());
        d.append("balance", this.balance);
        app.dbClient.updateOne("PlayerData", "uuid", player.getUniqueId().toString(), d);
        player.sendMessage(ChatColor.GRAY + "你的資料已自動保存至資料庫");
    }

    public void recordInvite(PlayerData p) {
        Date t = new Date();
        this.inviteTimeMap.put(p.player, t);
        p.invitedTimeMap.put(this.player, t);
    }

    // possible exception
    public void destroyInvitedRecord() {
        for (Player p : this.invitedTimeMap.keySet()) {
            app.allPlayers.get(p.getUniqueId()).inviteTimeMap.remove(this.player);
        }
        this.invitedTimeMap.clear();
    }

    public void destroyInviteRecord() {
        for (Player p : this.inviteTimeMap.keySet()) {
            app.allPlayers.get(p.getUniqueId()).invitedTimeMap.remove(this.player);
        }
        this.inviteTimeMap.clear();
    }

    public boolean isInviteCooling(PlayerData p) {
        Date t = inviteTimeMap.get(p.player);
        return t != null && new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)).before(t);
    }

    public boolean isInvitedBy(PlayerData p) {
        if (p.inviteTimeMap.get(this.player) != null && this.invitedTimeMap.get(p.player) != null) {
            Date t = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
            if (this.invitedTimeMap.get(p.player).before(t) && p.inviteTimeMap.get(this.player).before(t)) {
                this.invitedTimeMap.remove(p.player);
                p.inviteTimeMap.remove(this.player);
                return false;
            }
            return true;
        }
        return false;
    }

    public String teamCapacityStatus() {
        return ChatColor.GRAY + "(" + this.team.get().size() + "/4)";
    }

    public void logOut() {
        this.saveData();
        if (this.team.isPresent()) {
            this.player.performCommand("team leave");// command shouldn't include `/`
        }

        this.destroyInvitedRecord();
        this.destroyInviteRecord();
    }

    public void sendWorldTitle(String worldName) {
        WorldData wd = app.worldManager.getWorldData(worldName);
        if (wd == null) return;
        player.sendTitle(ChatColor.YELLOW + wd.worldName, wd.worldDescription, 20, 80, 20);
    }
}
