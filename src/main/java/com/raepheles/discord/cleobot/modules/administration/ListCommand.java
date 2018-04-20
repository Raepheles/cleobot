package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

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
            Logger.logCommand(command, "NOT ADMIN");
            return;
        }
        List<IGuild> guilds = command.getClient().getGuilds();
        List<IUser> users = command.getClient().getUsers();
        long usersCount = 0;
        for(IGuild guild: guilds) {
            usersCount += guild.getUsers().stream().filter(user -> !user.isBot()).count();
        }
        long uniqueUsersCount = command.getClient().getUsers().stream().filter(u -> !u.isBot()).count();
        String result = String.format("Connected Guilds: %d\n" +
                "Total Users: %d\n" +
                "Total Unique Users: %d\n", guilds.size(), usersCount, uniqueUsersCount);
        List<String> lines = new ArrayList<>();
        for(IGuild guild: guilds) {
            lines.add(String.format("%-50s | %-20s | %-15s", guild.getName().length() > 50 ? guild.getName().substring(0, 46) + "..." : guild.getName(),
                    guild.getLongID(),
                    "User count: " + guild.getUsers().stream().filter(user -> !user.isBot()).count()));
        }
        Path file = Paths.get("send-file.txt");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch(IOException e) {
            e.printStackTrace();
        }

        Utilities.sendFile(command.getChannel(), result, file.toFile());
    }
}
