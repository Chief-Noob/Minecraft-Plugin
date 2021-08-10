package com.linyuanlin.minecraft;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.linyuanlin.minecraft.Manager.LocationManager;
import com.linyuanlin.minecraft.Manager.TeamManager;
import com.linyuanlin.minecraft.Manager.TradeManager;
import com.linyuanlin.minecraft.Manager.WorldManager;
import com.linyuanlin.minecraft.models.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class App extends JavaPlugin implements Listener {
    public HashMap<UUID, PlayerData> allPlayers = new HashMap<>();
    public WorldManager worldManager = new WorldManager();
    public LocationManager locationManager = new LocationManager();
    public TeamManager teamManager = new TeamManager(this);
    public TradeManager tradeManager = new TradeManager(this);
    public String mongodbConnectString = "";

    public void downloadAllUserData() throws Exception {
        for (Player p : Bukkit.getOnlinePlayers()) {
            allPlayers.put(p.getUniqueId(), new PlayerData(this, p.getUniqueId()));
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        this.saveDefaultConfig();

        mongodbConnectString = this.getConfig().getString("mongo_connection_string");

        if (mongodbConnectString == null || mongodbConnectString.equals("mongodb://username:password@host")) {
            getLogger().log(Level.SEVERE, "There is no valid mongodb connection string in config file !!");
            for (Player p : getServer().getOnlinePlayers()) {
                p.sendMessage(ChatColor.RED + "伺服器主系統啟動失敗，資料庫設定無效，請聯繫工程師處理！");
            }
        }

        try {
            downloadAllUserData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        worldManager.loadWorlds();

        getLogger().info("Main system enabled");
    }

    @Override
    public void onDisable() {

        for (Map.Entry<UUID, PlayerData> pair : allPlayers.entrySet()) {
            pair.getValue().saveData();
        }

        getLogger().info("See you again, SpigotMC!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws Exception {
        Player p = e.getPlayer();
        p.sendTitle(ChatColor.YELLOW + "歡迎光臨", ChatColor.GRAY + "本伺服器目前仍在開發階段", 20, 80, 40);
        e.setJoinMessage(
                ChatColor.WHITE + "玩家 " + ChatColor.YELLOW + e.getPlayer().getName() + ChatColor.WHITE + " 登入了, 讚啦！");
        allPlayers.put(p.getUniqueId(), new PlayerData(this, p.getUniqueId()));

        World lobbyWorld = Bukkit.getWorld("world_lobby");
        if (lobbyWorld != null) {
            p.teleport(lobbyWorld.getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        String newWorldName = p.getWorld().getName();
        if (newWorldName.equals("house_world")) {
            p.sendTitle(ChatColor.YELLOW + "小屋世界", "你能用收集來的資源建造你的居所，也能儲存你的戰利品以及勳章和物資", 20, 80, 20);
        }
        if (newWorldName.equals("world_lobby")) {
            p.sendTitle(ChatColor.YELLOW + "大廳", "所有玩家一開始進入遊戲時的交誼廳，擁有通往各個區域的傳送門", 20, 80, 20);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        allPlayers.get(e.getPlayer().getUniqueId()).logOut(allPlayers);
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent e) throws InterruptedException, ExecutionException {
        e.setFormat(ChatColor.WHITE + "[" + ChatColor.AQUA + "工程師" + ChatColor.WHITE + "] " + e.getPlayer().getName()
                + " 說 " + ChatColor.GRAY + e.getMessage());
        // 顯示對話泡泡
        Bukkit.getScheduler().callSyncMethod(this, () -> this.setholo(e.getPlayer(), e.getMessage(), 1)).get();
    }

    /**
     * 對話時產生 hologram 在玩家頭上
     */
    public boolean setholo(Player Player, String msg, int second) {
        final Hologram hologram = HologramsAPI.createHologram(this, Player.getLocation().add(0.0, 2.0, 0.0));
        hologram.appendTextLine(msg);

        new BukkitRunnable() {
            int ticksRun;

            @Override
            public void run() {
                ticksRun++;
                hologram.teleport(Player.getLocation().add(0.0, 2.7, 0.0));

                if (ticksRun > second * 20) {
                    hologram.teleport(Player.getLocation().add(0.0, 5, 0.0));
                }

                if (ticksRun > second * 20 + 0.2) {
                    hologram.delete();
                    cancel();
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        return true;

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event1) {
        Player p = event1.getPlayer();
        Entity entity = event1.getRightClicked();
        if (entity instanceof Player && event1.getHand() == EquipmentSlot.HAND) {
            p.sendMessage("他是 " + entity.getName());
            p.sendMessage("該玩家擁有財產 " + allPlayers.get(entity.getUniqueId()).balance + " 元");
            TextComponent a = new TextComponent("[傳送組隊邀請]");
            a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊發送組隊邀請給 " + entity.getName())));
            a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team invite " + entity.getName()));
            p.spigot().sendMessage(a);
        }
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String cmdlable, String[] args) {
        PlayerData senderPlayer = allPlayers.get(((Player) sender).getUniqueId());
        if (senderPlayer == null)
            return false;

        switch (cmdlable) {
            case "trade":
                return tradeManager.onCommandTrade(sender, cmd, cmdlable, args, senderPlayer);
            case "team":
                return teamManager.onCommandTeam(sender, cmd, cmdlable, args, senderPlayer);
            default:
                return false;
        }
    }
}
