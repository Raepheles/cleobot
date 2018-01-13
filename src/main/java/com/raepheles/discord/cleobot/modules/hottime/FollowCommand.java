package com.raepheles.discord.cleobot.modules.hottime;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Created by Rae on 23/12/2017.
 * Commands for following/unfollowing hot time notifications.
 */
@SuppressWarnings("unused")
public class FollowCommand {

    @BotCommand(command = {"hottime", "follow"},
            description = "Adds user to hot time notifications follow list.",
            usage = "hottime follow *server_name*",
            module = "Hot Time")
    public static void followHotTimeCommand(CommandContext command) {
        if (!Utilities.checkBotChannel(command)) {
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
        JSONArray followers = guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeFollowers")).getJSONArray(serverName);
        boolean isFollower = false;
        for(int i = 0; i < followers.length(); i++) {
            long userId = ((Number)followers.get(i)).longValue();
            if(userId == command.getAuthor().getLongID()) {
                isFollower = true;
            }
        }
        if(isFollower) {
            command.replyWith(String.format(Utilities.getProperty("notifications.followFail"), "hot time", " for server: `" + serverName + "`!"));
            return;
        }
        guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeFollowers")).getJSONArray(serverName).put(command.getAuthor().getLongID());
        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
        long hotTimeChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.hotTimeChannel"))).longValue();
        if(hotTimeChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "hot time", " for server: `" + serverName +
                    "`. Current notification channel: NO CHANNEL"));
        } else {
            IChannel hotTimeIChannel = command.getClient().getChannelByID(hotTimeChannel);
            if(hotTimeIChannel != null) {
                command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "hot time", " for server: `" + serverName +
                        "`. Current notification channel: " + command.getClient().getChannelByID(hotTimeChannel).mention()));
            } else {
                guilds.getJSONObject(index).put(Utilities.getProperty("guilds.hotTimeChannel"), -1);
                Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "hot time", " for server: `" + serverName +
                        "`. Current notification channel: NO CHANNEL"));
            }
        }
    }

    @BotCommand(command = {"hottime", "unfollow"},
            description = "Removes user from hot time notifications follow list.",
            usage = "hottime unfollow *server_name*",
            module = "Hot Time")
    public static void unfollowHotTimeCommand(CommandContext command) {
        if (!Utilities.checkBotChannel(command)) {
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
        JSONArray followers = guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeFollowers")).getJSONArray(serverName);
        boolean wasFollower = false;
        for(int i = 0; i < followers.length(); i++) {
            long userId = ((Number)followers.get(i)).longValue();
            if(userId == command.getAuthor().getLongID()) {
                guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.hotTimeFollowers")).getJSONArray(serverName).remove(i);
                Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                command.replyWith(String.format(Utilities.getProperty("notifications.unfollowSuccess"), "hot time", " for server: `" + serverName + "`"));
                wasFollower = true;
                break;
            }
        }
        if(!wasFollower) {
            command.replyWith(String.format(Utilities.getProperty("notifications.unfollowFail"), "hot time", " for server: `" + serverName + "`!"));
        }
    }
}
