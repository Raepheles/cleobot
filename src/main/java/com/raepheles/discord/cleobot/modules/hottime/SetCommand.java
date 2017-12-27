package com.raepheles.discord.cleobot.modules.hottime;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Created by Rae on 23/12/2017.
 */
public class SetCommand {

    @BotCommand(command = {"hottime", "set"},
            description = "Sets hot time notifications channel.",
            usage = "hottime set",
            module = "Hot Time",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void setHotTimeCommand(CommandContext command) {
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
        long guildId = command.getGuild().getLongID();
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                break;
            }
        }

        if(Utilities.hasPerms(command.getChannel(), command.getClient().getOurUser())) {
            guilds.getJSONObject(index).put(Utilities.getProperty("guilds.hotTimeChannel"), command.getChannel().getLongID());
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            command.replyWith(String.format(Utilities.getProperty("notifications.successSet"), command.getChannel().mention(), "hot time"));
            Logger.logCommand(command);
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.permissions"), "hot time"));
            Logger.logCommand(command, "Missing permissions");
        }
    }

    @BotCommand(command = {"hottime", "on"},
            description = "Activates hot time notifications for *server_name*.",
            usage = "hottime on *server_name*",
            module = "Hot Time",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void onHotTimeCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(command.getArgCount() != 3) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName;
        if(command.getArgument(2).equalsIgnoreCase("eu")) {
            serverName = "EUROPE";
        } else if(command.getArgument(2).equalsIgnoreCase("america")) {
            serverName = "AMERICA";
        } else if(command.getArgument(2).equalsIgnoreCase("asia")) {
            serverName = "ASIA";
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.illegalServerArg"), command.getArgument(2)));
            Logger.logCommand(command, "Illegal argument");
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        long guildId = command.getGuild().getLongID();
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                break;
            }
        }
        long hotTimeChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.hotTimeChannel"))).longValue();
        if(hotTimeChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.activateFail"), "hot time", command.getPrefix(), "hottime"));
            Logger.logCommand(command, "Hot time channel not set");
            return;
        }
        int hotTimeStatus = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeStatus")).get(serverName);
        if(hotTimeStatus == 0) {
            guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeStatus")).put(serverName, 1);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            command.replyWith(String.format(Utilities.getProperty("notifications.statusActivated"), "Hot time", serverName));
            Logger.logCommand(command);
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.alreadyActive"), "Hot time", serverName));
            Logger.logCommand(command, "Already active");
        }
    }

    @BotCommand(command = {"hottime", "off"},
            description = "Deactivates hot time notifications for *server_name*.",
            usage = "hottime off *server_name*",
            module = "Hot Time",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void offHotTimeCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(command.getArgCount() != 3) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName;
        if(command.getArgument(2).equalsIgnoreCase("eu")) {
            serverName = "EUROPE";
        } else if(command.getArgument(2).equalsIgnoreCase("america")) {
            serverName = "AMERICA";
        } else if(command.getArgument(2).equalsIgnoreCase("asia")) {
            serverName = "ASIA";
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.illegalServerArg"), command.getArgument(2)));
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        long guildId = command.getGuild().getLongID();
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                break;
            }
        }
        long hotTimeChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.hotTimeChannel"))).longValue();
        if(hotTimeChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.activateFail"), "hot time", command.getPrefix(), "hottime"));
            Logger.logCommand(command, "Hot time channel not set");
            return;
        }
        int hotTimeStatus = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeStatus")).get(serverName);
        if(hotTimeStatus == 1) {
            guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeStatus")).put(serverName, 0);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            command.replyWith(String.format(Utilities.getProperty("notifications.statusDeactivated"), "Hot Time", serverName));
            Logger.logCommand(command);
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.alreadyInactive"), "Hot Time", serverName));
            Logger.logCommand(command, "Already inactive");
        }
    }
}
