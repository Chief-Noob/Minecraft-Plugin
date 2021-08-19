package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.item.CustomItem;
import com.linyuanlin.minecraft.models.PlayerData;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class GuildManager implements CommandExecutor {
	public GuildManager() {
		Objects.requireNonNull(App.getPlugin().getCommand("guild")).setExecutor(this);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		try {
			PlayerData p = App.getPlugin().allPlayers.get(((Player) sender).getUniqueId());
			if (p == null) {
				return false;
			}

			switch (args[0]) {
				case "getInvitationPaper":
					return this.getInvitationPaper(p);
				case "useInvitationPaper":
					return this.useInvitationPaper(p);
				case "second":
					return this.second(p, args);
				default:
					return this.help(p);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			App.getPlugin().discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
			return false;
		}
	}

	private boolean getInvitationPaper(PlayerData senderPlayer) {
		if (senderPlayer.balance() < 1000) {
			senderPlayer.player().sendMessage(
					"你的錢不夠購買公會創立卷！ " + ChatColor.RED + "(" + senderPlayer.balance() + "/1000)");
			return false;
		}
		senderPlayer.modifyBalance(-1000, "Buy Guild Invitation Paper");
		senderPlayer.player().sendMessage("你已成功購買公會創立卷！ " + senderPlayer.getBalanceString());
		senderPlayer.player().getInventory().addItem(new ItemStack(CustomItem.guildInvitationPaper));
		return true;
	}

	private boolean useInvitationPaper(PlayerData sendPlayer) {

		return true;
	}

	private boolean second(PlayerData senderPlayer, String[] args) {
		return true;
	}

	private boolean help(PlayerData senderPlayer) {
		senderPlayer.player().sendMessage(ChatColor.RED + "沒有這個指令");
		senderPlayer.player().sendMessage(ChatColor.AQUA + "/guild 的使用方式：");

		senderPlayer.player().sendMessage("/guild getInvitationPaper" + ChatColor.GRAY + " - 獲得公會創立卷");

		senderPlayer.player().sendMessage(ChatColor.GRAY + "/guild help - 取得幫助");
		return false;
	}
}
