package com.linyuanlin.minecraft;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.linyuanlin.minecraft.models.PlayerData;
import com.linyuanlin.minecraft.models.Team;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class App extends JavaPlugin implements Listener {

    public HashMap<UUID, PlayerData> allPlayers = new HashMap<>();

    public void downloadAllUserData() throws Exception {
        for (Player p : Bukkit.getOnlinePlayers()) {
            allPlayers.put(p.getUniqueId(), new PlayerData(p.getUniqueId()));
        }
    }

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);

        try {
            downloadAllUserData();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        allPlayers.put(p.getUniqueId(), new PlayerData(p.getUniqueId()));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        allPlayers.get(e.getPlayer().getUniqueId()).saveData();
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
        if (entity instanceof Player) {
            p.sendMessage("他是 " + entity.getName());
            TextComponent a = new TextComponent("[傳送組隊邀請]");
            a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊發送組隊邀請給 " + entity.getName())));
            a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team invite " + entity.getName()));
            p.spigot().sendMessage(a);
        }
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String cmdlable, String[] args) {
        Player p1 = (Player) sender;
        if (cmdlable.equals("team")) {

            switch (args[0]) {
                case "invite": {
                    Player p = Bukkit.getPlayer(args[1]);
                    TextComponent a = new TextComponent("[確認組隊邀請]");
                    a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊接受 " + sender.getName() + " 的組隊邀請 ")));
                    a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team join " + args[1]));
                    p.spigot().sendMessage(a);
                    break;
                }
                case "join": {
                    Player p = Bukkit.getPlayer(args[1]);
                    TextComponent msg = new TextComponent("");
                    if (allPlayers.get(p1.getUniqueId()).team.isEmpty()) {
                        List<PlayerData> playerArray = new ArrayList<>();
                        playerArray.add(allPlayers.get(p1.getUniqueId()));
                        playerArray.add(allPlayers.get(p.getUniqueId()));
                        Team team = new Team(playerArray);
                        allPlayers.get(p1.getUniqueId()).team = team;
                        allPlayers.get(p.getUniqueId()).team = team;
                        team.leader = allPlayers.get(p1.getUniqueId());
                        msg = new TextComponent(p.getName() + "已加入" + "(" + allPlayers.get(p1.getUniqueId()).team.size() + "/4");
                    } else if (!allPlayers.get(p1.getUniqueId()).team.isFull()) {
                        allPlayers.get(p1.getUniqueId()).team.playerList.add(allPlayers.get(Bukkit.getPlayer(args[0]).getUniqueId()));
                        allPlayers.get(Bukkit.getPlayer(p.getName()).getUniqueId()).team = allPlayers.get(p1.getUniqueId()).team;
                        msg = new TextComponent(p.getName() + "已加入" + "(" + allPlayers.get(p1.getUniqueId()).team.size() + "/4");
                    } else {
                        msg = new TextComponent("隊伍已滿");
                    }
                    p.spigot().sendMessage(msg);
                    p1.spigot().sendMessage(msg);
                    break;
                }
                case "list": {
                    StringBuilder teamMemberNameString = new StringBuilder();
                    for (int i = 0; i < allPlayers.get(p1.getUniqueId()).team.size(); i++) {
                        teamMemberNameString.append(allPlayers.get(p1.getUniqueId()).team.playerList.get(i).player.getName()).append(" ");
                    }
                    TextComponent msg = new TextComponent("隊伍成員：" + teamMemberNameString);
                    for (int i = 0; i < allPlayers.get(p1.getUniqueId()).team.size(); i++) {
                        allPlayers.get(p1.getUniqueId()).team.playerList.get(i).player.spigot().sendMessage(msg);
                    }

                }
                break;
                default:
                    break;
            }

            return true;
        }
        return false;
    }

}
