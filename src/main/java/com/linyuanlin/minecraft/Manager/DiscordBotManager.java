package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.tjplaysnow.discord.object.Bot;
import com.tjplaysnow.discord.object.ThreadSpigot;
import net.dv8tion.jda.api.entities.TextChannel;
import java.util.*;

public class DiscordBotManager {
    // Key: Channel Tag, Value: Channel ID
    private final HashMap<String, String> textChannels = new HashMap<>();

    // Key: Bot Tag, Value: Bot
    private final HashMap<String, Bot> bots = new HashMap<>();

    public void registerNewBot(String tag, String token) {
        Bot b = new Bot(token, tag);
        b.setBotThread(new ThreadSpigot(App.getPlugin()));
        bots.put(tag, b);
    }

    public void registerNewTextChannel(String channelTag, String channelId) {
        textChannels.put(channelTag, channelId);
    }

    public void shutDownAllBot() {
        for (Map.Entry<String, Bot> pair : bots.entrySet()) {
            pair.getValue().getBot().shutdown();
        }
    }

    public void sendMessage(String botTag, String channelTag, String message) {
        Bot bot = bots.get(botTag);
        if (bot == null) {
            App.getPlugin().getLogger().warning("Cannot let bot " + botTag + " send message to channel " + channelTag
                    + " because bot is not exist.");
            return;
        }
        String cId = textChannels.get(channelTag);
        if (cId == null) {
            App.getPlugin().getLogger().warning("Cannot let bot " + botTag + " send message to channel " + channelTag
                    + " because channel is not exist.");
            return;
        }

        TextChannel c = bot.getBot().getTextChannelById(cId);
        if (c == null) {
            App.getPlugin().getLogger().warning("Cannot let bot " + botTag + " send message to channel " + channelTag
                    + " because channel is not exist.");
            return;
        }

        c.sendMessage(message).queue();
    }
}
