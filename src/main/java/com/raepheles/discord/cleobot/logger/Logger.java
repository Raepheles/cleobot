package com.raepheles.discord.cleobot.logger;

import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import org.json.JSONArray;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Created by Rae on 19/12/2017.
 */
public class Logger {
    private static IChannel channel;

    public static void setLogger(IChannel channel) {
        Logger.channel = channel;
    }

    public static void logCommand(CommandContext command) {
        String log = command.getAuthor().getName() + " used command `" +
                String.join(" ", command.getArguments()) + "`. Channel: `" + command.getChannel().getName() + "` | Guild: `" + command.getGuild().getName() +
                " (" + command.getGuild().getLongID() + ")` | SUCCESS!";
        if(Logger.channel == null) {
            return;
        }

        Utilities.sendMessage(channel, log);
    }

    public static void logCommand(CommandContext command, String failReason) {
        String log = command.getAuthor().getName() + " used command `" +
                String.join(" ", command.getArguments()) + "`. Channel: `" + command.getChannel().getName() + "` | Guild: `" + command.getGuild().getName() +
                " (" + command.getGuild().getLongID() + ")` | " + "FAIL with reason: `" + failReason + "`";
        if(Logger.channel == null) {
            return;
        }

        Utilities.sendMessage(channel, log);
    }

    public static void logGuildJoin(GuildCreateEvent event) {
        String log = String.format("%s tried to join guild. Name: `%s`, ID: `%s`, Owner Name: `%s`, Owner ID: `%s`",
                event.getClient().getOurUser().getName(),
                event.getGuild().getName(),
                event.getGuild().getLongID(),
                event.getGuild().getOwner().getName(),
                event.getGuild().getOwner().getLongID());
        if(Utilities.getWhitelistStatus()) {
            JSONArray whitelist = Utilities.readJsonFromFile(Utilities.getProperty("files.whitelist"));
            if(whitelist == null) {
                Utilities.sendMessage(channel, log + " | `Cannot read whitelist file`"); // Error reading file
                return;
            }
            for(int i = 0; i < whitelist.length(); i++) {
                long guildId = ((Number)whitelist.getJSONObject(i).get("id")).longValue();
                if(guildId == event.getGuild().getLongID()) {
                    Utilities.sendMessage(channel, log + " | `Not on whitelist`"); // Not whitelisted
                    return;
                }
            }
            Utilities.sendMessage(channel, log + " | `SUCCESS`"); // Whitelisted
        } else {
            Utilities.sendMessage(channel, log + " | `SUCCESS`"); // No whitelist Success
        }
    }
}
