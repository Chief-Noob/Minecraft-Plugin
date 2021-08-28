package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.item.CustomItem;
import com.linyuanlin.minecraft.models.PlayerData;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.chat.TextComponent;

import java.io.*;
import java.util.*;

import javax.swing.text.DefaultStyledDocument.ElementSpec;

public class GuildManager implements CommandExecutor {
	public static App getPlugin() {
		return JavaPlugin.getPlugin(App.class);
	}

	public GuildManager() {
		Objects.requireNonNull(getPlugin().getCommand("guild")).setExecutor(this);
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
				case "getInvitationPaper":
					return this.getInvitationPaper(p);
				case "useInvitationPaper":
					return this.useInvitationPaper(p, args);
				case "second":
					return this.second(p, args);
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

	private boolean useInvitationPaper(PlayerData sendPlayer, String[] args) {
		if (args.length == 0) {
			sendPlayer.player().sendMessage("請至少邀請一人");
			return false;
		}

		for (String name : args) {
			Player receiverPlayer = Bukkit.getPlayer(name);
			if (receiverPlayer == null) {
				sendPlayer.player()
						.sendMessage("邀請對象" + ChatColor.GOLD + name + ChatColor.WHITE + "不存在");
			} else {
				receiverPlayer.sendMessage("err");
			}
		}
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
