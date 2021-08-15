package com.linyuanlin.minecraft.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.*;

public class CustomItem extends JavaPlugin {
	protected static ItemStack item01;

	static {
		item01 = new ItemStack(Material.DIAMOND_SWORD, (short) 1);
	}
}