package com.linyuanlin.minecraft;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.linyuanlin.minecraft.manager.*;
import com.linyuanlin.minecraft.models.PlayerData;
import com.linyuanlin.minecraft.mongodb.MongodbClient;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends JavaPlugin implements Listener {
    public HashMap<UUID, PlayerData> allPlayers = new HashMap<>();
    public WorldManager worldManager;
    public DiscordBotManager discordBotManager;
    public TeamManager teamManager;
    public TradeManager tradeManager;
    public GuildManager guildManager;
    public LocationManager locationManager;
    public PluginMessageHandler pluginMessageHandler;
    public MongodbClient dbClient;
    public String mongodbConnectString = "";

    public void downloadAllUserData() throws Exception {
        for (Player p : Bukkit.getOnlinePlayers()) {
            allPlayers.put(p.getUniqueId(), new PlayerData(this, p.getUniqueId()));
        }
    }

    private void loadManagers() {
        this.worldManager = new WorldManager();
        this.discordBotManager = new DiscordBotManager();
        this.teamManager = new TeamManager();
        this.tradeManager = new TradeManager();
        this.guildManager = new GuildManager();
        this.locationManager = new LocationManager();
    }

    private void loadHandlers() {
        this.pluginMessageHandler = new PluginMessageHandler();
    }

    @Override
    public void onEnable() {
        // load managers
        this.loadManagers();

        // load handlers
        this.loadHandlers();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this.pluginMessageHandler);

        // Starting discord bot
        String discordBotToken = this.getConfig().getString("discord_bot_token");

        if (discordBotToken == null || discordBotToken.equals("null")) {
            getLogger().log(java.util.logging.Level.WARNING, "There is no valid discord bot token in config file !!");
        } else {
            discordBotManager.registerNewBot("TEST", discordBotToken);
            discordBotManager.registerNewTextChannel("Project-Minecraft", DiscordBotManager.channelId);
            Bukkit.getScheduler().runTaskLater(this,
                    () -> discordBotManager.sendMessage("TEST", "Project-Minecraft", "Server main system enabled."),
                    100L);
        }

        // register event listeners
        getServer().getPluginManager().registerEvents(this, this);

        // handling configs
        this.saveDefaultConfig();

        // starting database connections
        mongodbConnectString = this.getConfig().getString("mongo_connection_string");

        if (mongodbConnectString == null || mongodbConnectString.equals("mongodb://username:password@host")) {
            getLogger().log(java.util.logging.Level.WARNING,
                    "There is no valid mongodb connection string in config file !!");
            for (Player p : getServer().getOnlinePlayers()) {
                p.sendMessage(ChatColor.RED + "伺服器主系統啟動失敗，資料庫設定無效，請聯繫工程師處理！");
            }
        }

        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
        this.dbClient = new MongodbClient("Minecraft");

        // Download all user data

        try {
            downloadAllUserData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // load all worlds

        worldManager.loadWorlds();

        getLogger().info("Main system enabled");

        // load all custom items

        // load all locations
        locationManager.loadLocations();
    }

    @Override
    public void onDisable() {
        try {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this);

            discordBotManager.shutDownAllBot();

            for (Map.Entry<UUID, PlayerData> pair : allPlayers.entrySet()) {
                pair.getValue().saveData();
            }

            this.dbClient.close();

            getLogger().info("See you again, SpigotMC!");

        } catch (Exception exception) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try {
            Player p = e.getPlayer();
            PlayerData pd = new PlayerData(this, p.getUniqueId());
            allPlayers.put(p.getUniqueId(), pd);
            String msg = ChatColor.WHITE + "玩家 " + ChatColor.GOLD + e.getPlayer().getName() + ChatColor.WHITE
                    + " 登入了, 讚啦！";
            e.setJoinMessage(msg);
            World lobbyWorld = Bukkit.getWorld("world_lobby");
            discordBotManager.sendMessage("TEST", "Project-Minecraft", msg);

            if (lobbyWorld == null)
                throw new Exception("World lobby_spawn is missing!");

            Location location = locationManager.getLocation(LocationManager.lobby_spawn);

            if (location == null) {
                p.teleport(lobbyWorld.getSpawnLocation());
                throw new Exception("Location " + LocationManager.lobby_spawn + " is missing!");
            } else {
                p.teleport(location);
            }

            pd.sendWorldTitle(p.getWorld().getName());

            Bukkit.getScheduler().runTaskLater(this, () -> pluginMessageHandler
                    .sendPluginMessage("subChannel-player-join", p.getName() + " joined this server"), 20L);
        } catch (Exception exception) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
        }
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
        try {
            Player p = e.getPlayer();
            String newWorldName = p.getWorld().getName();
            PlayerData pd = allPlayers.get(p.getUniqueId());
            pd.sendWorldTitle(newWorldName);
        } catch (Exception exception) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        try {
            allPlayers.get(e.getPlayer().getUniqueId()).logOut();
            String msg = ChatColor.WHITE + "玩家 " + ChatColor.GOLD + e.getPlayer().getName() + ChatColor.WHITE + " 登出了！";
            e.setQuitMessage(msg);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", msg);
        } catch (Exception exception) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
        }
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent e) {
        try {
            String t = ChatColor.WHITE + "[" + ChatColor.AQUA + "工程師" + ChatColor.WHITE + "] " + e.getPlayer().getName()
                    + " 說 " + ChatColor.GRAY + e.getMessage();

            e.setFormat(t);

            discordBotManager.sendMessage("TEST", "Project-Minecraft", t);

            // 顯示對話泡泡
            Bukkit.getScheduler().callSyncMethod(this, () -> this.setHolo(e.getPlayer(), e.getMessage(), 1)).get();

            Bukkit.getScheduler().runTaskLater(this, () -> pluginMessageHandler
                    .sendPluginMessage("subChannel-player-message", e.getPlayer().getName() + " " + e.getMessage()),
                    20L);

        } catch (Exception exception) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
        }
    }

    /**
     * 對話時產生 hologram 在玩家頭上
     */
    public boolean setHolo(Player Player, String msg, int second) {
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
        try {
            Player p = event1.getPlayer();
            Entity entity = event1.getRightClicked();
            if (entity instanceof Player && event1.getHand() == EquipmentSlot.HAND) {
                p.sendMessage("他是 " + entity.getName());
                p.sendMessage("該玩家擁有財產 " + allPlayers.get(entity.getUniqueId()).balance() + " 元");
                TextComponent a = new TextComponent("[傳送組隊邀請]");
                a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊發送組隊邀請給 " + entity.getName())));
                a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team invite " + entity.getName()));
                p.spigot().sendMessage(a);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
        }
    }
}
