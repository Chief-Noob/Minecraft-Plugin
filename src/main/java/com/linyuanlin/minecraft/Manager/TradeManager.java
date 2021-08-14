package com.linyuanlin.minecraft.Manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TradeManager implements CommandExecutor {

    App app;

    public TradeManager(App app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        try {
            PlayerData p = app.allPlayers.get(((Player) sender).getUniqueId());

            if (p == null) {
                return false;
            }

            switch (args[0]) {
                case "pay":
                    return this.pay(p, args);
                case "me":
                    return this.me(p);
                default:
                    return this.help(p);
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            app.discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
            return false;
        }
    }

    private boolean me(PlayerData sender) {
        sender.player.sendMessage(ChatColor.GRAY + "當前可用餘額：" + ChatColor.WHITE + sender.getBalance() + " 元");
        return true;
    }

    private boolean pay(PlayerData sender, String[] args) {

        if (args.length != 3) {
            sender.player.sendMessage(ChatColor.GRAY + "使用方式： /trade pay <對象> <金額>");
            return false;
        }

        String target = args[1], amountStr = args[2];

        int amount = Integer.parseInt(amountStr);
        if (amount <= 0) {
            sender.player.sendMessage(ChatColor.RED + "請輸入正確的轉帳金額！");
            return false;
        }

        Player p = Bukkit.getPlayer(target);
        if (p == null) {
            sender.player.sendMessage(ChatColor.RED + "玩家 " + target + " 不存在或是不在線上！");
            return false;
        }

        if (sender.getBalance() < amount) {
            sender.player.sendMessage(ChatColor.GOLD + "你沒那麼多錢拉，窮逼。");
            return false;
        }

        PlayerData targetData = app.allPlayers.get(p.getUniqueId());
        if (targetData == null) {
            sender.player.sendMessage("你轉帳的玩家 " + ChatColor.GOLD + p.getName() + ChatColor.WHITE
                    + " 不存在於allPlayers中,請聯繫開發人員");
            return false;
        }

        targetData.modifyBalance(amount, "Receive transfer from " + sender.player.getName() + " (" + sender.player.getUniqueId() + ")");

        p.sendMessage(ChatColor.GREEN + "收到來自玩家 " + ChatColor.WHITE + sender.player.getName() + ChatColor.GREEN
                + " 的轉帳 " + ChatColor.WHITE + amountStr + ChatColor.GREEN + " 元");

        sender.modifyBalance(-amount, "Transfer to " + targetData.player.getName() + " (" + targetData.player.getUniqueId() + ")");

        sender.player.sendMessage(ChatColor.GREEN + "成功轉帳 " + ChatColor.WHITE + amountStr + ChatColor.GREEN + " 元給玩家 "
                + ChatColor.WHITE + target + ChatColor.GREEN + " 了～");

        return true;
    }

    private boolean help(PlayerData senderPlayer) {
        senderPlayer.player.sendMessage("沒有這個指令");
        senderPlayer.player.sendMessage(ChatColor.AQUA + "/trade 的使用方式：");
        senderPlayer.player.sendMessage(ChatColor.GRAY + "/trade pay <對象> <金額>");

        return false;
    }

}
