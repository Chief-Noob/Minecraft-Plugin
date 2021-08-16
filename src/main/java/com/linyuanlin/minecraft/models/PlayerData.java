package com.linyuanlin.minecraft.models;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.manager.TeamManager;
import com.mongodb.client.model.Filters;
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
    public Optional<Team> team;
    public Optional<Guild> guild;
    private final HashMap<Player, Date> invitedTimeMap;
    private final HashMap<Player, Date> inviteTimeMap;
    private final App app;
    private int balance = 0;

    public PlayerData(App app, UUID uuid) throws Exception {
        this.app = app;

        this.player = Bukkit.getServer().getPlayer(uuid);

        this.invitedTimeMap = new HashMap<>();

        this.inviteTimeMap = new HashMap<>();

        this.team = Optional.empty();

        if (this.player == null)
            throw new Exception("PLAYER_NOT_ONLINE");

        Document d = app.dbClient.findOne("PlayerData", "uuid", uuid.toString());
        if (d != null) {
            // should crate a readPlayerFromDoc function
            this.balance = d.getInteger("balance");
        } else {
            // should crate a createDocForPlayer function
            Document newDocument = new Document();
            newDocument.append("uuid", player.getUniqueId().toString());
            newDocument.append("balance", this.balance);
            app.dbClient.insert("PlayerData", newDocument);
        }

        player.sendMessage(ChatColor.GRAY + "你的資料已從資料庫同步完成");
    }

    /**********
     * System *
     *********/

    /*
     * Save player's data into DataBase
     */
    public void saveData() {
        Document d = new Document();
        d.append("uuid", player.getUniqueId().toString());
        d.append("balance", this.balance);

        app.dbClient.replaceOne("PlayerData", Filters.eq("uuid", player.getUniqueId().toString()), d);
        player.sendMessage(ChatColor.GRAY + "你的資料已自動保存至資料庫");
    }

    /*
     * Perform all actions when player log out
     */
    public void logOut() throws Exception {
        this.saveData();
        if (this.team.isPresent()) {
            this.player.performCommand("team leave");
        }

        this.destroyInvitedRecord();
        this.destroyInviteRecord();
    }

    /********
     * team *
     *******/

    /*
     * Record the invitation records when player invite p
     */
    public void recordInvite(PlayerData p) {
        Date t = new Date();
        this.inviteTimeMap.put(p.player, t);
        p.invitedTimeMap.put(this.player, t);
    }

    /*
     * Destroy invited records(both player's invitedTimeMap and p's inviteTimeMap)
     */
    public void destroyInvitedRecord() throws Exception {
        for (Player p : this.invitedTimeMap.keySet()) {
            app.allPlayers.get(p.getUniqueId()).inviteTimeMap.remove(this.player);
        }
        this.invitedTimeMap.clear();
    }

    /*
     * Destroy invite records(both player's inviteTimeMap and p's invitedTimeMap)
     */
    public void destroyInviteRecord() throws Exception {
        for (Player p : this.inviteTimeMap.keySet()) {
            app.allPlayers.get(p.getUniqueId()).invitedTimeMap.remove(this.player);
        }
        this.inviteTimeMap.clear();
    }

    /*
     * Return whether player invite p is cooling
     */
    public boolean isInviteCooling(PlayerData p) {
        Date t = inviteTimeMap.get(p.player);
        return t != null && new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)).before(t);
    }

    /*
     * Return whether player is invited by p THIS WILL REMOVE EXPIRED RECORDS!!
     */
    public boolean isInvitedBy(PlayerData p) {
        if (p.inviteTimeMap.get(this.player) != null && this.invitedTimeMap.get(p.player) != null) {
            Date t = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(TeamManager.INVITE_COOLING_MINS));
            if (this.invitedTimeMap.get(p.player).before(t) && p.inviteTimeMap.get(this.player).before(t)) {
                this.invitedTimeMap.remove(p.player);
                p.inviteTimeMap.remove(this.player);
                return false;
            }
            return true;
        }
        return false;
    }

    /*
     * Return the String of Team Capacity Status with ChatColor
     */
    public String teamCapacityStatus() {
        if (!team.isPresent())
            return "";
        return ChatColor.GRAY + "(" + this.team.get().size() + "/" + Integer.toString(Team.MAX_CAPACITY) + ")";
    }

    /*********
     * world *
     ********/

    /**
     * Send a title to player about a world's information
     */
    public void sendWorldTitle(String worldName) {
        WorldData wd = app.worldManager.getWorldData(worldName);
        if (wd == null)
            return;
        player.sendTitle(ChatColor.YELLOW + wd.getWorldName(), wd.getWorldDescription(), 20, 80, 20);
    }

    /*********
     * trade *
     ********/

    /**
     * Modify the balance of player and leave a modification record into database
     */
    public void modifyBalance(int delta, String reason) {
        Document d = new Document();
        d.append("before", balance);
        d.append("after", balance + delta);
        d.append("reason", reason);
        this.app.dbClient.insert("BalanceModify", d);
        balance += delta;
    }

    /**
     * Get player's balance
     */
    public int getBalance() {
        return this.balance;
    }

    /*
     * Get player's balance string
     */
    public String getBalanceString() {
        return ChatColor.GRAY + "(餘額： " + this.getBalance() + ")";
    }
}
