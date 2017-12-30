package com.raepheles.discord.cleobot.modules.newday;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Created by Rae on 20/12/2017.
 * Command for setting new day notifications channel.
 * Commands for activating/deactivating new day notifications for servers.
 */
@SuppressWarnings("unused")
public class SetCommand {

    @BotCommand(command = {"newday", "set"},
            description = "Sets new day notifications channel.",
            usage = "newday set",
            module = "New Day",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void setNewDayCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            return;
        }
        if(command.getArgCount() > 2) {
            command.sendUsage();
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
            guilds.getJSONObject(index).put(Utilities.getProperty("guilds.newDayChannel"), command.getChannel().getLongID());
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            command.replyWith(String.format(Utilities.getProperty("notifications.successSet"), command.getChannel().mention(), "new day"));
            Logger.logCommand(command);
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.permissions"), "new day"));
        }
    }

    @BotCommand(command = {"newday", "on"},
            description = "Activates new day notifications for *server_name*.",
            usage = "newday on *server_name*",
            module = "New Day",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void onNewDayCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            return;
        }
        if(command.getArgCount() != 3) {
            command.sendUsage();
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
        long newDayChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.newDayChannel"))).longValue();
        if(newDayChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.activateFail"), "new day", command.getPrefix(), "newday"));
            return;
        }
        int newDayStatus = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).get(serverName);
        if(newDayStatus == 0) {
            guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).put(serverName, 1);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            command.replyWith(String.format(Utilities.getProperty("notifications.statusActivated"), "New day", serverName));
            Logger.logCommand(command);
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.alreadyActive"), "New day", serverName));
            Logger.logCommand(command, "Already active");
        }
    }

    @BotCommand(command = {"newday", "off"},
            description = "Deactivates new day notifications for *server_name*.",
            usage = "newday off *server_name*",
            module = "New Day",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void offNewDayCommand(CommandContext command) {
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
        long newDayChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.newDayChannel"))).longValue();
        if(newDayChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.activateFail"), "new day", command.getPrefix(), "newday"));
            Logger.logCommand(command, "New day channel not set");
            return;
        }
        int newDayStatus = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).get(serverName);
        if(newDayStatus == 1) {
            guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).put(serverName, 0);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            command.replyWith(String.format(Utilities.getProperty("notifications.statusDeactivated"), "New day", serverName));
            Logger.logCommand(command);
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.alreadyInactive"), "New day", serverName));
            Logger.logCommand(command, "Already inactive");
        }
    }
}
