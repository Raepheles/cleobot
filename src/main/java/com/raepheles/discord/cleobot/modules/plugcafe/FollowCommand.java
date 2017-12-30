package com.raepheles.discord.cleobot.modules.plugcafe;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

/**
 * Created by Rae on 19/12/2017.
 * Commands for following/unfollowing plug cafe notifications.
 */
@SuppressWarnings("unused")
public class FollowCommand {

    @BotCommand(command = {"plugcafe", "follow"},
            aliases = "plug",
            description = "Follow plug cafe notifications.",
            usage = "plugcafe follow",
            module = "Plug Cafe")
    public static void plugCafeFollow(CommandContext command) {
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

        String plugCafeFollowers = Utilities.getProperty("guilds.plugCafeFollowers");
        int followersLength = guilds.getJSONObject(index).getJSONArray(plugCafeFollowers).length();
        for(int i = 0; i < followersLength; i++) {
            if( ((Number)guilds.getJSONObject(index).getJSONArray(plugCafeFollowers).get(i)).longValue() == command.getAuthor().getLongID() ) {
                // If already on the follow list inform user and return
                command.replyWith(String.format(Utilities.getProperty("notifications.followFail"), "plug cafe", ""));
                Logger.logCommand(command, "Already follower");
                return;
            }
        }
        // Save to followers list and inform user
        guilds.getJSONObject(index).getJSONArray(plugCafeFollowers).put(followersLength, command.getAuthor().getLongID());
        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
        command.replyWith(String.format(Utilities.getProperty("notifications.followSuccess"), "plug cafe", ""));
        Logger.logCommand(command);
    }

    @BotCommand(command = {"plugcafe", "unfollow"},
            aliases = "plug",
            description = "Unfollow plug cafe notifications.",
            usage = "plugcafe unfollow",
            module = "Plug Cafe")
    public static void plugCafeUnfollow(CommandContext command) {
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

        String plugCafeFollowers = Utilities.getProperty("guilds.plugCafeFollowers");
        int followersLength = guilds.getJSONObject(index).getJSONArray(plugCafeFollowers).length();
        for(int i = 0; i < followersLength; i++) {
            if( ((Number)guilds.getJSONObject(index).getJSONArray(plugCafeFollowers).get(i)).longValue() == command.getAuthor().getLongID() ) {
                // If follower then remove from the list, inform user and return
                guilds.getJSONObject(index).getJSONArray(plugCafeFollowers).put(followersLength, command.getAuthor().getLongID());
                Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                command.replyWith(String.format(Utilities.getProperty("notifications.unfollowSuccess"), "plug cafe", ""));
                Logger.logCommand(command);
                return;
            }
        }
        // If already not follower then inform user
        command.replyWith(String.format(Utilities.getProperty("notifications.unfollowFail"), "plug cafe", ""));
        Logger.logCommand(command, "Already not follower");
    }
}
