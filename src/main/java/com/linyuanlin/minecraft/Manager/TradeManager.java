package com.linyuanlin.minecraft.Manager;

import com.linyuanlin.minecraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.linyuanlin.minecraft.App;
import java.util.HashMap;
import java.util.UUID;

import com.linyuanlin.minecraft.Manager.TradeManager;
import org.bukkit.entity.Player;

public class TradeManager {
	private HashMap<UUID, PlayerData> allPlayers;

	public TradeManager(App app) {
		this.allPlayers = app.allPlayers;
	}

	public boolean onCommandTrade(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		switch (args[0]) {
			case "pay":
				return this.pay(sender, cmd, cmdlable, args, senderPlayer);
			default:
				return this.help(senderPlayer);
		}

	}

	private boolean pay(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		if (args.length != 3) {
			senderPlayer.player.sendMessage(ChatColor.GRAY + "使用方式： /trade pay <對象> <金額>");
			return false;
		}

		String target = args[1], amountStr = args[2];
		int amount = Integer.parseInt(amountStr);
		Player p = Bukkit.getPlayer(target);
		if (p == null) {
			sender.sendMessage(ChatColor.RED + "玩家 " + target + " 不存在或是不在線上！");
			return false;
		}

		if (senderPlayer.balance < amount) {
			sender.sendMessage(ChatColor.GOLD + "你沒那麼多錢拉，窮逼。");
			sender.sendMessage(ChatColor.GOLD + "但是因為現在伺服器是開發階段，所以轉帳還是成功了，而你不用被扣錢，超讚");
		}

		PlayerData targetData = allPlayers.get(p.getUniqueId());
		if (targetData == null) {
			sender.sendMessage("你轉帳的玩家 " + ChatColor.GOLD + p.getName() + ChatColor.WHITE
					+ " 不存在於allPlayers中,請聯繫開發人員");
			return false;
		}
		targetData.balance += amount;
		sender.sendMessage(ChatColor.GREEN + "成功轉帳 " + ChatColor.WHITE + amountStr + ChatColor.GREEN + " 元給玩家 "
				+ ChatColor.WHITE + target + ChatColor.GREEN + " 了～");
		p.sendMessage(ChatColor.GREEN + "收到來自玩家 " + ChatColor.WHITE + sender.getName() + ChatColor.GREEN
				+ " 的轉帳 " + ChatColor.WHITE + amountStr + ChatColor.GREEN + " 元");
		return true;
	}

	private boolean help(PlayerData senderPlayer) {
		senderPlayer.player.sendMessage("沒有這個指令");
		senderPlayer.player.sendMessage(ChatColor.AQUA + "/trade 的使用方式：");
		senderPlayer.player.sendMessage(ChatColor.GRAY + "/trade pay <對象> <金額>");

		return false;
	}
}