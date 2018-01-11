package com.raepheles.discord.cleobot.modules.plugcafe;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import sx.blah.discord.handle.obj.Permissions;

import java.io.IOException;

/**
 * Created by Rae on 19/12/2017.
 * Command for setting plug cafe notifications channel.
 * Command for deactivating plug cafe notifications channel. (Removing the channel)
 */
@SuppressWarnings("unused")
public class SetCommand {

    @BotCommand(command = {"plugcafe", "set"},
            aliases = "plug",
            description = "Sets current channel as plug cafe notifications channel.",
            usage = "plugcafe set",
            module = "Plug Cafe",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void plugCafeSet(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
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
        // Check if current channel is same with plug cafe plugcafe channel
        if(channelId == command.getChannel().getLongID()) {
            command.replyWith(String.format(Utilities.getProperty("notifications.alreadyOn"), "Plug cafe"));
            Logger.logCommand(command, "Same channel");
            return;
        }
        // Check if bot has required perms for current channel
        if(!Utilities.hasPerms(command.getChannel(), command.getClient().getOurUser())) {
            command.replyWith(String.format(Utilities.getProperty("notifications.permissions"), "Plug cafe"));
            Logger.logCommand(command, "Missing permissions");
            return;
        }

        if(!newNotificationChannel(guilds.getJSONObject(index))) {
            command.replyWith(Utilities.getProperty("notifications.errorWritingFile"));
            return;
        }
        guilds.getJSONObject(index).put(Utilities.getProperty("guilds.plugCafeChannel"), command.getChannel().getLongID());
        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));

        command.replyWith(String.format(Utilities.getProperty("notifications.successSet"), command.getChannel().mention(), "Plug cafe"));
        Logger.logCommand(command);
    }

    @BotCommand(command = {"plugcafe", "off"},
            aliases = "plug",
            description = "Deactivates plug cafe notifications.",
            usage = "plugcafe off",
            module = "Plug Cafe",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void plugCafeOff(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
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
            command.replyWith(String.format(Utilities.getProperty("notifications.alreadyOff"), "Plug cafe"));
            Logger.logCommand(command, "Already inactive");
            return;
        }

        guilds.getJSONObject(index).put(Utilities.getProperty("guilds.plugCafeChannel"), -1);
        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));

        command.replyWith(String.format(Utilities.getProperty("notifications.successOff"), "Plug cafe"));
        Logger.logCommand(command);

    }

    private static boolean newNotificationChannel(JSONObject guild) {
        Document patchNotes, notices, events;
        try {
            patchNotes = Jsoup.connect(Utilities.PLUG_CAFE_PATCH_NOTES).get();
            notices = Jsoup.connect(Utilities.PLUG_CAFE_NOTICES).get();
            events = Jsoup.connect(Utilities.PLUG_CAFE_EVENTS).get();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        Elements elementsPatchNotes = patchNotes.getElementsByAttribute("data-articleid");
        String articleIdPatchNotes = elementsPatchNotes.get(0).attributes().get("data-articleid");
        Elements elementsNotices = notices.getElementsByAttribute("data-articleid");
        String articleIdNotices = elementsNotices.get(0).attributes().get("data-articleid");
        Elements elementsEvents = events.getElementsByAttribute("data-articleid");
        String articleIdEvents = elementsEvents.get(0).attributes().get("data-articleid");
        long noticeId, eventId, patchNoteId;
        try {
            noticeId = Long.parseLong(articleIdNotices);
            eventId = Long.parseLong(articleIdEvents);
            patchNoteId = Long.parseLong(articleIdPatchNotes);
        } catch(NumberFormatException nfe) {
            nfe.printStackTrace();
            return false;
        }
        guild.put(Utilities.getProperty("guilds.lastNotice"), noticeId);
        guild.put(Utilities.getProperty("guilds.lastPatchNote"), patchNoteId);
        guild.put(Utilities.getProperty("guilds.lastEvent"), eventId);
        guild.put(Utilities.getProperty("guilds.plugCafeMode"), 0);
        return true;
    }
}
