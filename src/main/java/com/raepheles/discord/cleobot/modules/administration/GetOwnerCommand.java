package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by Rae on 28/12/2017.
 * Command for getting the name and id of server owner.
 */
@SuppressWarnings("unused")
public class GetOwnerCommand {

    @BotCommand(command = "getowner",
            aliases = "owner",
            description = "Gets owner id of the guild id",
            usage = "owner *guild_id*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void messageCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            return;
        }
        long guildId;
        try {
            guildId = Long.parseLong(command.getArgument(1));
        } catch (NumberFormatException nfe) {
            command.replyWith(Utilities.getProperty("administrator.numberFormatExceptionGuildId"));
            return;
        }
        IGuild guild = command.getClient().getGuildByID(guildId);
        if(guild == null) {
            command.replyWith(Utilities.getProperty("administrator.guildNotFound"));
        } else {
            command.replyWith(guild.getOwner().getName() + " - " + guild.getOwnerLongID());
        }
    }
}
