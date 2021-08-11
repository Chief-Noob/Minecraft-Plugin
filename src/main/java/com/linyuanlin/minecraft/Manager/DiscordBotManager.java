package com.linyuanlin.minecraft.Manager;

import com.linyuanlin.minecraft.App;
import com.tjplaysnow.discord.object.Bot;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;

public class DiscordBotManager {

    private App app;

    // Key: Channel Tag, Value: Channel ID
    private HashMap<String, String> textChannels = new HashMap<>();

    // Key: Bot Tag, Value: Bot
    private HashMap<String, Bot> bots = new HashMap<>();

    public DiscordBotManager(App app) {
        this.app = app;
    }

    public void registerNewBot(String tag, String token) {
        bots.put(tag, new Bot(token, tag));
    }

    public void registerNewTextChannel(String channelTag, String channelId) {
        textChannels.put(channelTag, channelId);
    }

    public void sendMessage(String botTag, String channelTag, String message) {
        Bot bot = bots.get(botTag);
        if (bot == null) {
            this.app.getLogger().warning("Cannot let bot " + botTag + " send message to channel " + channelTag + " because bot is not exist.");
            return;
        }
        String cId = textChannels.get(channelTag);
        if (cId == null) {
            this.app.getLogger().warning("Cannot let bot " + botTag + " send message to channel " + channelTag + " because channel is not exist.");
            return;
        }

        TextChannel c = bot.getBot().getTextChannelById(cId);
        if (c == null) {
            this.app.getLogger().warning("Cannot let bot " + botTag + " send message to channel " + channelTag + " because channel is not exist.");
            return;
        }

        c.sendMessage(message).queue();
    }

}
