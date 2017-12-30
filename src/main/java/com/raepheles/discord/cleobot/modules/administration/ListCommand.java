package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;

/**
 * Created by Rae on 26/12/2017.
 * Command for listing connected guilds.
 */
@SuppressWarnings("unused")
public class ListCommand {

    @BotCommand(command = "guilds",
            description = "Lists connected guilds.",
            usage = "guilds",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void listCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            return;
        }
        List<IGuild> guilds = command.getClient().getGuilds();
        int usersCount = 0;
        for(IGuild guild: guilds) {
            usersCount += guild.getUsers().stream().filter(user -> !user.isBot()).count();
        }
        String result = "Connected guilds: " + guilds.size() + "\n" +
                "Total users: " + usersCount + "\n```";
        int counter = 0;
        for(IGuild guild: guilds) {
            result += String.format("%-50s | %-20s | %-15s\n", guild.getName().length() > 50 ? guild.getName().substring(0, 46) + "..." : guild.getName(),
                    guild.getLongID(),
                    "Users: " + guild.getUsers().stream().filter(user -> !user.isBot()).count());
            counter++;
            if(counter == 20) {
                result += "```";
                command.replyWith(result);
                result = "```";
                counter = 0;
            }
        }
        if(result.length() > 0) {
            result += "```";
            command.replyWith(result);
        }
    }
}
