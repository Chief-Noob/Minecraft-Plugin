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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class App extends JavaPlugin implements Listener {

    public HashMap<UUID, PlayerData> allPlayers = new HashMap<>();
    public WorldManager worldManager = new WorldManager();
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
        PlayerData player = allPlayers.get(e.getPlayer().getUniqueId());
        player.saveData();
        if (player.team.isPresent()) {
            player.player.performCommand("team leave");// command shouldn't include `/`
        }
        for (Player p : player.inviteTimeMap.keySet()) {
            player.destroyInviteRecord(allPlayers.get(p.getUniqueId()));
        }

        for (Player p : player.invitedTimeMap.keySet()) {
            allPlayers.get(p.getUniqueId()).destroyInviteRecord(player);
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
    public boolean onPayCommand(CommandSender sender, Command cmd, String cmdlable, String[] args) {
        Player senderPlayer = (Player) sender;
        if (cmdlable.equals("pay")) {
            String target = args[0];
            String amountStr = args[1];
            int amount = Integer.parseInt(amountStr);
            Player p = Bukkit.getPlayer(target);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "玩家 " + target + " 不在線上！");
                return false;
            }
            PlayerData senderData = allPlayers.get(((Player) sender).getUniqueId());
            if (senderData.balance < amount) {
                sender.sendMessage(ChatColor.GOLD + "你沒那麼多錢拉，窮逼。");
                sender.sendMessage(ChatColor.GOLD + "但是因為現在伺服器是開發階段，所以轉帳還是成功了，而你不用被扣錢，超讚");
            }
            PlayerData targetData = allPlayers.get(p.getUniqueId());
            targetData.balance += amount;
            sender.sendMessage(ChatColor.GREEN + "成功轉帳 " + ChatColor.WHITE + amountStr + ChatColor.GREEN + " 元給玩家 "
                    + ChatColor.WHITE + target + ChatColor.GREEN + " 了～");
            p.sendMessage(ChatColor.GREEN + "收到來自玩家 " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN + " 的轉帳 "
                    + ChatColor.WHITE + amountStr + ChatColor.GREEN + " 元");
        }
        return false;
    }

    @EventHandler
    public boolean onTeamCommand(CommandSender sender, Command cmd, String cmdlable, String[] args) {
        PlayerData senderPlayer = allPlayers.get(((Player) sender).getUniqueId());
        if (senderPlayer == null) {

        }
        if (cmdlable.equals("team")) {
            switch (args[0]) {
                case "invite": {// sender invite receiver to sender's team
                    PlayerData receiverPlayer = allPlayers.get((Object) Bukkit.getPlayer(args[1]));
                    if (receiverPlayer == null) {
                        senderPlayer.player.sendMessage("你邀請的人不存在");
                        return false;
                    }

                    TextComponent msg = new TextComponent();
                    Optional<Team> team = allPlayers.get(receiverPlayer.player.getUniqueId()).team;

                    if (team.isPresent()) {
                        msg = new TextComponent(receiverPlayer.player.getName() + "已經有隊伍了！");
                        senderPlayer.player.spigot().sendMessage(msg);
                        return false;
                    }

                    if (senderPlayer.isInviteCooling(receiverPlayer)) {
                        sender.sendMessage("邀請" + receiverPlayer.player.getName() + "冷卻中");
                        return false;
                    }

                    msg = new TextComponent("[確認 " + senderPlayer.player.getName() + " 的組隊邀請]");
                    msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text("點擊接受 " + senderPlayer.player.getName() + " 的組隊邀請")));
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/team join " + senderPlayer.player.getName()));
                    receiverPlayer.player.spigot().sendMessage(msg);

                    senderPlayer.player.spigot()
                            .sendMessage(new TextComponent("已發送邀請給 " + receiverPlayer.player.getName()));

                    senderPlayer.recordInvite(receiverPlayer);
                    return true;
                }
                case "join": {// sender join receiver's team
                    PlayerData receiverPlayer = allPlayers.get((Object) Bukkit.getPlayer(args[1]));
                    if (receiverPlayer == null) {
                        senderPlayer.player.sendMessage("你要加入的隊伍的邀請人不存在");
                        return false;
                    }

                    TextComponent msg = new TextComponent("");
                    Optional<Team> team = receiverPlayer.team;

                    if (!senderPlayer.isInvitedBy(receiverPlayer)) {
                        sender.sendMessage("你並沒有被邀請至 " + receiverPlayer.player.getName() + " 的隊伍");
                        return false;
                    }

                    if (!team.isPresent()) {
                        List<PlayerData> playerArray = new ArrayList<>();
                        playerArray.add(receiverPlayer);
                        playerArray.add(senderPlayer);

                        Optional<Team> newTeam = Optional.of(new Team(playerArray));

                        receiverPlayer.team = newTeam;
                        senderPlayer.team = newTeam;

                        newTeam.get().leader = receiverPlayer;

                        msg = new TextComponent(
                                senderPlayer.player.getName() + "已加入" + "(" + senderPlayer.team.get().size() + "/4)");
                    } else if (!team.get().isFull()) {
                        if (team.get().playerList.contains((Object) senderPlayer)) {
                            senderPlayer.player.sendMessage("你已在此隊伍中");
                            return false;
                        }

                        team.get().playerList.add(senderPlayer);
                        senderPlayer.team = team;

                        msg = new TextComponent(
                                senderPlayer.player.getName() + "已加入" + "(" + senderPlayer.team.get().size() + "/4)");
                    } else {
                        senderPlayer.player.sendMessage("隊伍已滿");
                        return false;
                    }

                    for (PlayerData pd : receiverPlayer.team.get().playerList) {
                        pd.player.spigot().sendMessage(msg);
                    }

                    receiverPlayer.destroyInviteRecord(senderPlayer);
                    return true;
                }
                case "list": {
                    StringBuilder teamMemberNameString = new StringBuilder();
                    Optional<Team> team = senderPlayer.team;

                    if (!team.isPresent()) {
                        senderPlayer.player.sendMessage("你沒隊伍拉");
                        return false;
                    }

                    for (PlayerData pd : team.get().playerList) {
                        teamMemberNameString.append(pd.player.getName()).append(" ");
                    }

                    TextComponent msg = new TextComponent(
                            "隊伍成員：" + teamMemberNameString + " | 隊長:" + team.get().leader.player.getName());
                    senderPlayer.player.spigot().sendMessage(msg);
                    return true;
                }
                case "leave": {
                    Optional<Team> team = senderPlayer.team;

                    if (!team.isPresent()) {
                        senderPlayer.player.spigot().sendMessage(new TextComponent("你沒有隊伍"));
                        return false;
                    }

                    team.get().playerList.remove((Object) senderPlayer);
                    if (team.get().size() == 1) {// 2 members-Team, this team should be deleted.
                        for (PlayerData pd : senderPlayer.team.get().playerList) {
                            pd.player.spigot().sendMessage(
                                    new TextComponent(senderPlayer.player.getName() + " 離開了隊伍, 隊伍人數不足，自動解散"));
                            pd.team = Optional.empty();
                        }
                    } else if (team.get().size() > 1) {// >2 members-Team, this Team should be remained.
                        if (team.get().leader == senderPlayer) {
                            for (PlayerData pd : senderPlayer.team.get().playerList) {
                                if (team.get().leader != senderPlayer) {
                                    team.get().leader = pd;
                                    break;
                                }
                            }
                            for (PlayerData pd : senderPlayer.team.get().playerList) {
                                pd.player.spigot()
                                        .sendMessage(new TextComponent("隊長 " + senderPlayer.player.getName()
                                                + " 離開了隊伍, 新隊長為" + team.get().leader.player.getName() + "("
                                                + senderPlayer.team.get().size() + "/4)"));
                            }
                        } else {
                            for (PlayerData pd : senderPlayer.team.get().playerList) {
                                pd.player.spigot().sendMessage(new TextComponent("隊員 " + senderPlayer.player.getName()
                                        + " 離開了隊伍" + "(" + senderPlayer.team.get().size() + "/4)"));
                            }
                        }
                    }

                    senderPlayer.team = Optional.empty();
                    senderPlayer.player.spigot().sendMessage(new TextComponent("你離開了隊伍"));
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
