package com.raepheles.discord.cleobot.modules.newday;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Created by Rae on 23/12/2017.
 * Commands for following/unfollowing new day notifications.
 */
@SuppressWarnings("unused")
public class FollowCommand {

    @BotCommand(command = {"newday", "follow"},
            description = "Adds user to new day notifications follow list.",
            usage = "newday follow *server_name*",
            module = "New Day")
    public static void followNewDayCommand(CommandContext command) {
        if (!Utilities.checkBotChannel(command)) {
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
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        long guildId = command.getGuild().getLongID();
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                break;
            }
        }
        JSONArray followers = guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayFollowers")).getJSONArray(serverName);
        boolean isFollower = false;
        for(int i = 0; i < followers.length(); i++) {
            long userId = ((Number)followers.getJSONObject(i).get("id")).longValue();
            if(userId == command.getAuthor().getLongID()) {
                isFollower = true;
            }
        }
        if(isFollower) {
            command.replyWith(String.format(Utilities.getProperty("notifications.followFail"), "new day", " for server: `" + serverName + "`!"));
            Logger.logCommand(command, "Already follower");
            return;
        }
        Logger.logCommand(command);
        guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayFollowers")).getJSONArray(serverName).put(command.getAuthor().getLongID());
        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
        long newDayChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.newDayChannel"))).longValue();
        if(newDayChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "new day", " for server: `" + serverName +
                    "`. Current notification channel: NO CHANNEL"));
        } else {
            IChannel newDayIChannel = command.getClient().getChannelByID(newDayChannel);
            if(newDayIChannel != null) {
                command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "new day", " for server: `" + serverName +
                        "`. Current notification channel: " + command.getClient().getChannelByID(newDayChannel).mention()));
            } else {
                guilds.getJSONObject(index).put(Utilities.getProperty("guilds.newDayChannel"), -1);
                Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "new day", " for server: `" + serverName +
                        "`. Current notification channel: NO CHANNEL"));
            }
        }
    }

    @BotCommand(command = {"newday", "unfollow"},
            description = "Removes user from new day notifications follow list.",
            usage = "newday unfollow *server_name*",
            module = "New Day")
    public static void unfollowNewDayCommand(CommandContext command) {
        if (!Utilities.checkBotChannel(command)) {
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
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        long guildId = command.getGuild().getLongID();
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                break;
            }
        }
        JSONArray followers = guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayFollowers")).getJSONArray(serverName);
        boolean wasFollower = false;
        for(int i = 0; i < followers.length(); i++) {
            long userId = ((Number)followers.getJSONObject(i).get("id")).longValue();
            if(userId == command.getAuthor().getLongID()) {
                guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayFollowers")).getJSONArray(serverName).remove(i);
                Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                command.replyWith(String.format(Utilities.getProperty("notifications.unfollowSuccess"), "new day", " for server: `" + serverName + "`"));
                Logger.logCommand(command);
                wasFollower = true;
                break;
            }
        }
        if(!wasFollower) {
            command.replyWith(String.format(Utilities.getProperty("notifications.unfollowFail"), "new day", " for server: `" + serverName + "`!"));
            Logger.logCommand(command, "Already not follower");
        }
    }
}
