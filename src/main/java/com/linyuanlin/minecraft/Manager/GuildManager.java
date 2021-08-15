package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.*;

import java.util.*;
import java.io.*;

public class GuildManager implements CommandExecutor {
	private App app;

	public GuildManager(App app) {
		this.app = app;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		try {
			PlayerData p = app.allPlayers.get(((Player) sender).getUniqueId());

			if (p == null) {
				return false;
			}

			switch (args[0]) {
				case "getInvitationPaper":
					return this.getInvitationPaper(p);
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

	private boolean getInvitationPaper(PlayerData senderPlayer) {
		ItemStack stack = new ItemStack(Material.PAPER, 1);
		ItemMeta im = stack.getItemMeta();
		im.setDisplayName(ChatColor.DARK_PURPLE + "家族創立卷");
		im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可創立家族，",
				ChatColor.WHITE + "使用本卷的玩家將成為家族長，且該玩家必須是當前隊伍的隊長",
				ChatColor.WHITE + "在該玩家的隊伍中的其他成員將成為家族成員，且人數必須為10人。"));
		stack.setItemMeta(im);

		senderPlayer.player.getInventory().addItem(new ItemStack(stack));
		
		return true;
	}

	private boolean help(PlayerData senderPlayer) {
		senderPlayer.player.sendMessage(ChatColor.RED + "沒有這個指令");
		senderPlayer.player.sendMessage(ChatColor.AQUA + "/guild 的使用方式：");
		senderPlayer.player.sendMessage(ChatColor.GRAY + "/guild help - 取得幫助");
		return false;
	}
}
