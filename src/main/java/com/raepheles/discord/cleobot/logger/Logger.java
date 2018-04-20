package com.raepheles.discord.cleobot.logger;

import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Language;
import com.raepheles.discord.cleobot.Utilities;
import org.json.JSONArray;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Created by Rae on 19/12/2017.
 * Sends log messages to discord channel.
 * Use setLogger(IChannel logChannel) method to set log channel.
 */
public class Logger {
    private static IChannel logChannel;

    public static void setLogger(IChannel logChannel) {
        Logger.logChannel = logChannel;
    }

    public static void logCommand(CommandContext command) {
        if(Logger.logChannel == null) {
            return;
        }
        String log = String.format("`%s (%d)` used command `%s`. Channel: `%s` | Guild: `%s (%d)` | SUCCESS!",
                command.getAuthor().getName(),
                command.getAuthor().getLongID(),
                String.join(" ", command.getArguments()),
                command.isPrivateMessage() ? command.getAuthor().getName() : command.getChannel().getName(),
                command.isPrivateMessage() ? "PRIVATE" : command.getGuild().getName(),
                command.isPrivateMessage() ? 0 : command.getGuild().getLongID());

        Utilities.sendMessage(logChannel, log);
    }

    public static void logCommand(CommandContext command, Language language) {
        if(Logger.logChannel == null) {
            return;
        }
        String log = String.format("`%s (%d)` used command `%s` in %s. Channel: `%s` | Guild: `%s (%d)` | SUCCESS!",
                command.getAuthor().getName(),
                command.getAuthor().getLongID(),
                String.join(" ", command.getArguments()),
                language,
                command.isPrivateMessage() ? command.getAuthor().getName() : command.getChannel().getName(),
                command.isPrivateMessage() ? "PRIVATE" : command.getGuild().getName(),
                command.isPrivateMessage() ? 0 : command.getGuild().getLongID());

        Utilities.sendMessage(logChannel, log);
    }

    public static void logCommand(CommandContext command, String failReason) {
        if(Logger.logChannel == null) {
            return;
        }
        String log = String.format("`%s (%d)` used command `%s`. Channel: `%s` | Guild: `%s (%d)` | FAIL with reason: `%s`",
                command.getAuthor().getName(),
                command.getAuthor().getLongID(),
                String.join(" ", command.getArguments()),
                command.isPrivateMessage() ? command.getAuthor().getName() : command.getChannel().getName(),
                command.isPrivateMessage() ? "PRIVATE" : command.getGuild().getName(),
                command.isPrivateMessage() ? 0 : command.getGuild().getLongID(),
                failReason);

        Utilities.sendMessage(logChannel, log);
    }

    public static void logCommand(CommandContext command, Language language, String failReason) {
        if(Logger.logChannel == null) {
            return;
        }
        String log = String.format("`%s (%d)` used command `%s` in %s. Channel: `%s` | Guild: `%s (%d)` | FAIL with reason: `%s`",
                command.getAuthor().getName(),
                command.getAuthor().getLongID(),
                String.join(" ", command.getArguments()),
                language,
                command.isPrivateMessage() ? command.getAuthor().getName() : command.getChannel().getName(),
                command.isPrivateMessage() ? "PRIVATE" : command.getGuild().getName(),
                command.isPrivateMessage() ? 0 : command.getGuild().getLongID(),
                failReason);

        Utilities.sendMessage(logChannel, log);
    }

    public static void logGuildJoin(GuildCreateEvent event) {
        if(Logger.logChannel == null) {
            return;
        }
        String log = String.format("%s tried to join guild. Name: `%s`, ID: `%s`, Owner Name: `%s`, Owner ID: `%s`",
                event.getClient().getOurUser().getName(),
                event.getGuild().getName(),
                event.getGuild().getLongID(),
                event.getGuild().getOwner().getName(),
                event.getGuild().getOwner().getLongID());
        if(Utilities.getWhitelistStatus()) {
            JSONArray whitelist = Utilities.readJsonFromFile(Utilities.getProperty("files.whitelist"));
            if(whitelist == null) {
                Utilities.sendMessage(logChannel, log + " | `Cannot read whitelist file`"); // Error reading file
                return;
            }
            for(int i = 0; i < whitelist.length(); i++) {
                long guildId = ((Number)whitelist.getJSONObject(i).get("id")).longValue();
                if(guildId == event.getGuild().getLongID()) {
                    Utilities.sendMessage(logChannel, log + " | `Not on whitelist`"); // Not whitelisted
                    return;
                }
            }
            Utilities.sendMessage(logChannel, log + " | `SUCCESS`"); // Whitelisted
        } else {
            Utilities.sendMessage(logChannel, log + " | `SUCCESS`"); // No whitelist Success
        }
    }
}
