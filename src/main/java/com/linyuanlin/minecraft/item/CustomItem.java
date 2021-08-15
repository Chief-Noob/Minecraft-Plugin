package com.linyuanlin.minecraft.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class CustomItem extends JavaPlugin {
	public static ItemStack guildInvitationPaper;

	static {
		guildInvitationPaper = new ItemStack(Material.PAPER, 1);
		ItemMeta im_guildInvItemMeta = guildInvitationPaper.getItemMeta();
		im_guildInvItemMeta.setDisplayName(ChatColor.DARK_PURPLE + "公會創立卷");
		im_guildInvItemMeta.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可創立公會，", ChatColor.WHITE + "使用本卷的玩家將成為公會會長",
				ChatColor.WHITE + "在一天內需要有至少三人找到相應NPC附議"));
		guildInvitationPaper.setItemMeta(im_guildInvItemMeta);
	}
}