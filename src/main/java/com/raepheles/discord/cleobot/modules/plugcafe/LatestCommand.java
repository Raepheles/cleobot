package com.raepheles.discord.cleobot.modules.plugcafe;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Created by Rae on 19/12/2017.
 * Command for getting latest articles from plug cafe.
 */
@SuppressWarnings("unused")
public class LatestCommand {

    @BotCommand(command = {"plugcafe", "latest"},
            aliases = "plug",
            description = "Gets latest plug cafe notifications.",
            usage = "plugcafe latest",
            module = "Plug Cafe",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void latestPlugCafe(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(command.getArgCount() > 2) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        long guildId = command.getGuild().getLongID();
        long channelId = -1;
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                channelId = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.plugCafeChannel"))).longValue();
                break;
            }
        }
        if(channelId == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.setChannelFirst"), "plug cafe"));
            Logger.logCommand(command, "Notification channel not set");
            return;
        }
        if(command.getClient().getChannelByID(channelId) == null) {
            command.replyWith(String.format(Utilities.getProperty("notifications.statusDeleted"), "Plug cafe", command.getPrefix(), "plugcafe"));
            Logger.logCommand(command, "Notification channel is deleted");
            return;
        }

        int mode = (int)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.plugCafeMode"));

        sendSingleNotification(command, Utilities.PLUG_CAFE_EVENTS, channelId, mode);
        sendSingleNotification(command, Utilities.PLUG_CAFE_PATCH_NOTES, channelId, mode);
        sendSingleNotification(command, Utilities.PLUG_CAFE_NOTICES, channelId, mode);

        Logger.logCommand(command);

    }

    private static void sendSingleNotification(CommandContext command, String link, long channelId, int mode) {
        //long channelId = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.plugCafeChannel"))).longValue();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements elements = doc.getElementsByAttribute("data-articleid");
            String articleId = elements.get(0).attributes().get("data-articleid");
            String articleAuthor = elements.get(0).getElementsByAttributeValue("class", "desc_thumb").get(0).getElementsByAttribute("href").text();
            String articleTitle = elements.get(0).getElementsByAttributeValue("class", "tit_feed").text();
            String articleThumbnail = elements.get(0).getElementsByAttributeValue("class", "img").get(0).attributes().get("style");
            StringBuilder sb = new StringBuilder(articleThumbnail);
            sb.delete(0, 21);
            sb.deleteCharAt(sb.length() - 1);
            articleThumbnail = sb.toString();
            String articleAuthorIcon = elements.get(0).getElementsByAttributeValue("class", "thumb").get(0).attributes().get("src");
            String articleAuthorUrl = "https://www.plug.game" +
                    elements.get(0).getElementsByAttributeValue("class", "img_thumb").get(0).attributes().get("href");
            String articleText = elements.get(0).getElementsByAttributeValue("class", "txt_feed").text();

            EmbedBuilder embed = new EmbedBuilder();
            embed.withTitle(articleTitle);
            embed.withAuthorName(articleAuthor);
            embed.withTimestamp(LocalDateTime.now());
            embed.withThumbnail(articleThumbnail);
            embed.withAuthorIcon(articleAuthorIcon);
            embed.withAuthorUrl(articleAuthorUrl);
            embed.appendDesc(articleText);
            embed.withUrl(Utilities.PLUG_CAFE_BASE_URL + "/posts/" + articleId);

            if(mode == 0) {
                Utilities.sendMessage(command.getClient().getChannelByID(channelId), embed.build());
            } else if(mode == 1) {
                String str = String.format("Author: %s\nTitle: %s\nLink: <%s>\n%s",
                        articleAuthor,
                        articleTitle,
                        Utilities.PLUG_CAFE_BASE_URL + "/posts/" + articleId,
                        articleText);
                Utilities.sendMessage(command.getClient().getChannelByID(channelId), str);
            } else if(mode == 2) {
                String str = articleTitle + ": <" + Utilities.PLUG_CAFE_BASE_URL + "/posts/" + articleId + ">\n";
                Utilities.sendMessage(command.getClient().getChannelByID(channelId), embed.build(), str);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
