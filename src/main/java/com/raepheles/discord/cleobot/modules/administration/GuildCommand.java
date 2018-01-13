package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created by Rae on 28/12/2017.
 * Command for getting info on the guild.
 */
@SuppressWarnings("unused")
public class GuildCommand {

    @BotCommand(command = "guild",
            description = "Gets info on the guild",
            usage = "guild *guild_id*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void messageCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            Logger.logCommand(command, "NOT ADMIN");
            return;
        }
        if(command.getArgCount() != 2) {
            command.sendUsage();
            return;
        }
        long guildId;
        try {
            guildId = Long.parseLong(command.getArgument(1));
        } catch (NumberFormatException nfe) {
            command.replyWith(Utilities.getProperty("administration.numberFormatExceptionGuildId"));
            return;
        }
        IGuild guild = command.getClient().getGuildByID(guildId);
        if(guild == null) {
            command.replyWith(String.format(Utilities.getProperty("administration.guildNotFound"), guildId));
            return;
        }

        String owner = String.format("Owner: %s - %d", guild.getOwner().getName(), guild.getOwnerLongID());
        String users = String.format("Users: %d", guild.getUsers().stream().filter(u -> !u.isBot()).count());
        String botUsers = String.format("Bot Users: %d", guild.getUsers().stream().filter(IUser::isBot).count());

        String botChannel = "Couldn't find guild entry in guilds file!";
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds != null) {
            for(int i = 0; i < guilds.length(); i++) {
                long tempId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
                if(tempId == guild.getLongID()) {
                    long botChannelId = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.botchannel"))).longValue();
                    if(guild.getChannelByID(botChannelId) == null) {
                        botChannel = "Bot channel is deleted";
                    } else {
                        if(!Utilities.hasPerms(guild.getChannelByID(botChannelId), command.getClient().getOurUser())) {
                            botChannel = String.format("Bot channel: %s - %d | %s",
                                    guild.getChannelByID(botChannelId).getName(),
                                    guild.getChannelByID(botChannelId).getLongID(),
                                    "Bot doesn't have perms!");
                        } else {
                            botChannel = String.format("Bot channel: %s - %d | %s",
                                    guild.getChannelByID(botChannelId).getName(),
                                    guild.getChannelByID(botChannelId).getLongID(),
                                    "Bot has perms.");
                        }
                    }
                }
            }
        } else {
            botChannel = "Error reading guilds file!";
        }

        command.replyWith(String.format("%s\n%s\n%s\n%s", owner, users, botUsers, botChannel));
    }
}
