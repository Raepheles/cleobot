package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Rae on 12/1/2018.
 * Command for getting user info
 */
@SuppressWarnings("unused")
public class UserCommand {

    @BotCommand(command = "user",
            description = "Gets info on the user",
            usage = "user *user_id*",
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
        long userId;
        try {
            userId = Long.parseLong(command.getArgument(1));
        } catch (NumberFormatException nfe) {
            command.replyWith(Utilities.getProperty("administration.numberFormatExceptionGuildId"));
            return;
        }
        IUser user = command.getClient().getUserByID(userId);
        if(user == null) {
            command.replyWith(String.format(Utilities.getProperty("administration.userNotFound"), userId));
            return;
        }
        List<IGuild> connectedGuilds = command.getClient().getGuilds().stream().filter(g -> g.getUserByID(userId) != null).collect(Collectors.toList());

        String currentName = String.format("%s#%s", user.getName(), user.getDiscriminator());
        String ownerOfGuilds = String.format("Owner of guilds:\n%s",
                String.join("\n", connectedGuilds.stream().filter(g -> g.getOwner().equals(user)).map(g -> g.getName() + " - " + g.getLongID()).collect(Collectors.toList()))
        );
        String userOfGuilds = String.format("Connected guilds:\n%s",
                String.join("\n", connectedGuilds.stream().filter(g -> !g.getOwner().equals(user)).map(g -> g.getName() + " - " + g.getLongID()).collect(Collectors.toList()))
        );

        command.replyWith(String.format("%s\n%s\n%s", currentName, ownerOfGuilds, userOfGuilds));
    }
}
