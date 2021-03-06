package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.models.PlayerData;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

import java.io.*;

public class TradeManager implements CommandExecutor {
	/*
	 * constants
	 */

	public static App getPlugin() {
		return JavaPlugin.getPlugin(App.class);
	}

	public TradeManager() {
		Objects.requireNonNull(getPlugin().getCommand("trade")).setExecutor(this);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {

		try {
			PlayerData p = getPlugin().allPlayers.get(((Player) sender).getUniqueId());

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
			getPlugin().discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
			return false;
		}
	}

	private boolean me(PlayerData senderPlayer) {
		senderPlayer.player().sendMessage(
				ChatColor.GRAY + "當前可用餘額：" + ChatColor.WHITE + senderPlayer.balance() + " 元");
		return true;
	}

	private boolean pay(PlayerData senderPlayer, String[] args) {
		if (args.length != 3) {
			senderPlayer.player().sendMessage(ChatColor.RED + "轉帳指令錯誤");
			this.help(senderPlayer);
			return false;
		}

		String receiverName = args[1], amountStr = args[2];

		int amount = Integer.parseInt(amountStr);
		if (amount <= 0) {
			senderPlayer.player().sendMessage(ChatColor.RED + "請輸入正確的轉帳金額！（轉帳金額不能為負數)");
			return false;
		}

		if (senderPlayer.balance() < amount) {
			senderPlayer.player().sendMessage(ChatColor.GOLD + "你沒那麼多錢拉，窮逼。");
			return false;
		}

		Player p = Bukkit.getPlayer(receiverName);
		if (p == null) {
			senderPlayer.player().sendMessage(
					"玩家 " + ChatColor.GOLD + receiverName + ChatColor.WHITE + " 不存在或是不在線上");
			return false;
		}

		PlayerData receiverPlayer = getPlugin().allPlayers.get(p.getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player().sendMessage(
					"玩家 " + ChatColor.GOLD + receiverName + ChatColor.WHITE + " 不存在或是不在線上");
			return false;
		}

		receiverPlayer.modifyBalance(amount, "Receive transfer from " + senderPlayer.player().getName() + " ("
				+ senderPlayer.player().getUniqueId() + ")");
		receiverPlayer.player().sendMessage(ChatColor.GREEN + "收到來自玩家 " + ChatColor.WHITE
				+ senderPlayer.player().getName() + ChatColor.GREEN + " 的轉帳 " + ChatColor.WHITE
				+ amountStr + ChatColor.GREEN + " 元 " + receiverPlayer.getBalanceString());

		senderPlayer.modifyBalance(-amount, "Transfer to " + receiverPlayer.player().getName() + " ("
				+ receiverPlayer.player().getUniqueId() + ")");
		senderPlayer.player()
				.sendMessage(ChatColor.GREEN + "成功轉帳 " + ChatColor.WHITE + amountStr + ChatColor.GREEN
						+ " 元給玩家 " + ChatColor.WHITE + receiverPlayer.player().getName()
						+ ChatColor.GREEN + " 了～ " + senderPlayer.getBalanceString());

		return true;
	}

	private boolean help(PlayerData senderPlayer) {
		senderPlayer.player().sendMessage(ChatColor.RED + "沒有這個指令");
		senderPlayer.player().sendMessage(ChatColor.AQUA + "/trade 的使用方式：");

		senderPlayer.player().sendMessage("/trade pay <對象> <金額>" + ChatColor.GRAY + " - 向其他玩家轉帳");
		senderPlayer.player().sendMessage("/trade me" + ChatColor.GRAY + " - 查看自己的經濟狀況");

		senderPlayer.player().sendMessage(ChatColor.GRAY + "/trade help - 取得幫助");
		return false;
	}

}
