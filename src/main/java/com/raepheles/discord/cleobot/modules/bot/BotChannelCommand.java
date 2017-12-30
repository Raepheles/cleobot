package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Created by Rae on 19/12/2017.
 * Command for setting bot channel.
 * Command for checking bot channel status.
 */
@SuppressWarnings("unused")
public class BotChannelCommand {


    @BotCommand(command = {"botchannel", "set"},
            description = "Sets the current channel as bot channel.",
            usage = "botchannel set",
            module = "Bot",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void botChannelSet(CommandContext command) {
        if(command.getArgCount() > 2) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile("guilds.json");
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        int index = -1;
        long guildId = command.getGuild().getLongID();
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(currentId == guildId) {
                index = i;
                break;
            }
        }

        if(index == -1) {
            JSONObject newGuild = Utilities.newGuildEntry(command.getGuild().getLongID());
            guilds.put(newGuild);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
        }

        if(!Utilities.hasPerms(command.getChannel(), command.getClient().getOurUser())) {
            command.replyWith(String.format(Utilities.getProperty("botchannel.noPerms"), command.getChannel().mention()));
            Logger.logCommand(command, "Missing permissions");
            return;
        }

        guilds.getJSONObject(index).put("botchannel", command.getChannel().getLongID());
        Utilities.writeToJsonFile(guilds, "guilds.json");

        command.replyWith(Utilities.getProperty("botchannel.success"));
        Logger.logCommand(command);

    }

    @BotCommand(command = {"botchannel", "status"},
            description = "Shows the current bot channel.",
            usage = "botchannel status",
            module = "Bot")
    public static void botChannelStatus(CommandContext command) {
        if(command.getArgCount() > 2) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile("guilds.json");
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        long guildId = command.getGuild().getLongID();
        long botchannel = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentGuildId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(currentGuildId == guildId) {
                botchannel = ((Number)guilds.getJSONObject(i).get("botchannel")).longValue();
                break;
            }
        }
        Logger.logCommand(command);
        if(botchannel != -1) {
            command.replyWith(String.format(Utilities.getProperty("botchannel.current"), command.getChannel().mention()));
        } else {
            command.replyWith(Utilities.getProperty("botchannel.notExists"));
        }
    }

}
