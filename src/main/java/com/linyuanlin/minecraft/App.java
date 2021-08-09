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
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class App extends JavaPlugin implements Listener {

    public HashMap<UUID, PlayerData> allPlayers = new HashMap<>();
    public WorldManager worldManager = new WorldManager();

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
        allPlayers.put(p.getUniqueId(), new PlayerData(p.getUniqueId()));

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
        allPlayers.get(e.getPlayer().getUniqueId()).saveData();
        if (allPlayers.get(e.getPlayer().getUniqueId()).team.isPresent()) {
            e.getPlayer().performCommand("team leave");// command shouldn't include `/`
        }
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
                case "invite": {// p1 invite p to p1's team
                    Player p = Bukkit.getPlayer(args[1]);
                    TextComponent a = new TextComponent();
                    if (allPlayers.get(p.getUniqueId()).team.isPresent()) {
                        a = new TextComponent(p.getName() + "已經有隊伍了！");
                        p1.spigot().sendMessage(a);
                    } else {
                        a = new TextComponent("[確認 " + p1.getName() +" 的組隊邀請]");
                        a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text("點擊接受 " + p1.getName() + " 的組隊邀請")));
                        a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team join " + p1.getName()));
                        p.spigot().sendMessage(a);

                        TextComponent b = new TextComponent();
                        b = new TextComponent("已發送邀請給 " + p.getName());
                        p1.spigot().sendMessage(b);
                    }
                    return true;
                }
                case "join": {// p1 join p's team / Todo : check if p1 is invited by p or not
                    Player p = Bukkit.getPlayer(args[1]);
                    TextComponent msg = new TextComponent("");
                    if (!allPlayers.get(p.getUniqueId()).team.isPresent()) {
                        List<PlayerData> playerArray = new ArrayList<>();
                        playerArray.add(allPlayers.get(p.getUniqueId()));
                        playerArray.add(allPlayers.get(p1.getUniqueId()));
                        Optional<Team> team = Optional.of(new Team(playerArray));
                        allPlayers.get(p.getUniqueId()).team = team;
                        allPlayers.get(p1.getUniqueId()).team = team;
                        team.get().leader = allPlayers.get(p.getUniqueId());
                        msg = new TextComponent(p1.getName() + "已加入" + "("
                                + allPlayers.get(p1.getUniqueId()).team.get().size() + "/4)");
                    } else if (!allPlayers.get(p.getUniqueId()).team.get().isFull()) {
                        if (allPlayers.get(p.getUniqueId()).team.get().playerList
                                .contains((Object) allPlayers.get(p.getUniqueId()))) {
                            p1.sendMessage("您已在此隊伍中");
                            return true;
                        }
                        allPlayers.get(p.getUniqueId()).team.get().playerList.add(allPlayers.get(p.getUniqueId()));
                        allPlayers.get(Bukkit.getPlayer(p1.getName()).getUniqueId()).team = allPlayers
                                .get(p.getUniqueId()).team;
                        msg = new TextComponent(p1.getName() + "已加入" + "("
                                + allPlayers.get(p1.getUniqueId()).team.get().size() + "/4)");
                    } else {
                        msg = new TextComponent("隊伍已滿");
                    }
                    for (PlayerData pd : allPlayers.get(p.getUniqueId()).team.get().playerList) {
                        pd.player.spigot().sendMessage(msg);
                    }
                    return true;
                }
                case "list": {
                    StringBuilder teamMemberNameString = new StringBuilder();
                    Optional<Team> team = allPlayers.get(p1.getUniqueId()).team;

                    if (!team.isPresent()) {
                        p1.sendMessage("你沒隊伍拉");
                        return false;
                    }

                    for (PlayerData pd : team.get().playerList) {
                        teamMemberNameString.append(pd.player.getName()).append(" ");
                    }

                    TextComponent msg = new TextComponent(
                            "隊伍成員：" + teamMemberNameString + " | 隊長:" + team.get().leader.player.getName());
                    p1.spigot().sendMessage(msg);
                    return true;
                }
                case "leave": {
                    Optional<Team> team = allPlayers.get(p1.getUniqueId()).team;
                    if (!team.isPresent()) {
                        p1.spigot().sendMessage(new TextComponent("你沒有隊伍"));
                        return false;
                    }

                    team.get().playerList.remove((Object) allPlayers.get(p1.getUniqueId()));
                    if (team.get().size() == 1) {// 2 members-Team, this team should be deleted.
                        for (PlayerData pd : allPlayers.get(p1.getUniqueId()).team.get().playerList) {
                            pd.player.spigot().sendMessage(new TextComponent(p1.getName() + " 離開了隊伍, 隊伍人數不足，自動解散"));
                            pd.team = Optional.empty();
                        }
                    } else if (team.get().size() > 1) {// >2 members-Team, this Team should be remained.
                        if (team.get().leader == allPlayers.get(p1.getUniqueId())) {
                            for (PlayerData pd : allPlayers.get(p1.getUniqueId()).team.get().playerList) {
                                if (team.get().leader != p1) {
                                    team.get().leader = pd;
                                    break;
                                }
                            }
                            for (PlayerData pd : allPlayers.get(p1.getUniqueId()).team.get().playerList) {
                                pd.player.spigot()
                                        .sendMessage(new TextComponent("隊長 " + p1.getName() + " 離開了隊伍, 新隊長為"
                                                + team.get().leader.player.getName() + "("
                                                + allPlayers.get(p1.getUniqueId()).team.get().size() + "/4)"));
                            }
                        } else {
                            for (PlayerData pd : allPlayers.get(p1.getUniqueId()).team.get().playerList) {
                                pd.player.spigot().sendMessage(new TextComponent("隊員 " + p1.getName() + " 離開了隊伍" + "("
                                        + allPlayers.get(p1.getUniqueId()).team.get().size() + "/4)"));
                            }
                        }
                    }

                    allPlayers.get(p1.getUniqueId()).team = Optional.empty();
                    p1.spigot().sendMessage(new TextComponent("你離開了隊伍"));
                    return true;
                }
                default:
                    break;
            }

            return true;
        }
        return false;
    }

}
