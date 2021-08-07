package com.linyuanlin.minecraft;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class App extends JavaPlugin implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Hello, SpigotMC!");
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendTitle(ChatColor.YELLOW + "歡迎光臨", ChatColor.GRAY + "本伺服器目前仍在開發階段", 20, 80, 40);
        e.setJoinMessage(
                ChatColor.WHITE + "玩家 " + ChatColor.YELLOW + e.getPlayer().getName() + ChatColor.WHITE + " 登入了, 讚啦！");
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent e) throws InterruptedException, ExecutionException {
        e.setFormat(ChatColor.WHITE + "[" + ChatColor.AQUA + "工程師" + ChatColor.WHITE + "] " + e.getPlayer().getName()
                + " 說 " + ChatColor.GRAY + e.getMessage());
        // 顯示對話泡泡
        Bukkit.getScheduler().callSyncMethod(this, () -> this.setholo(e.getPlayer(), e.getMessage(), 1)).get();
    }

    /** 對話時產生 hologram 在玩家頭上 */
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
        if (entity instanceof Player == true) {
            p.sendMessage("他是 " + ((Player) entity).getName());
            TextComponent a = new TextComponent("[傳送組隊邀請]");
            a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊發送組隊邀請給 " + entity.getName())));
            a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team invite " + entity.getName()));
        }
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String cmdlable, String[] args) {
        Player senderp = (Player) sender;
        Player p = senderp;
        if (cmdlable.equals("team")) {
            p.sendMessage("組隊功能還沒做好 哭哭");
            return true;
        }
        return false;
    }

}
