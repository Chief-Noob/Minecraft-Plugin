package com.linyuanlin.minecraft;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PluginMessageHandler implements PluginMessageListener {
    public static App getPlugin() {
        return JavaPlugin.getPlugin(App.class);
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        Bukkit.getConsoleSender().sendMessage("receive plugin message");
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        String msg = in.readUTF();

        Bukkit.getConsoleSender().sendMessage("receive plugin message: " + subChannel + msg);
        switch (subChannel) {
            case "subChannel-player-message": {
                String[] msgList = msg.split(" ", 2);
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    p.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA + "工程師" + ChatColor.WHITE + "] " + msgList[0]
                            + " 說 " + ChatColor.GRAY + msgList[1]);
                }
                break;
            }
            default:
                break;
        }
    }

    public void sendPluginMessage(String subChannel, String PluginMessage) {
        if (Bukkit.getOnlinePlayers().size() == 0)
            return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(subChannel);
        out.writeUTF(PluginMessage);
        Bukkit.getConsoleSender().sendMessage("send plugin message: " + subChannel + ' ' + PluginMessage);
        Objects.requireNonNull(Iterables.getFirst(Bukkit.getOnlinePlayers(), null)).sendPluginMessage(getPlugin(),
                "BungeeCord", out.toByteArray());
    }
}
