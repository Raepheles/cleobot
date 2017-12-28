package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

/**
 * Created by Rae on 27/12/2017.
 */
public class InviteCommand {

    @BotCommand(command = "invite",
            description = "Invite link for the bot.",
            usage = "invite",
            module = "Bot",
            allowPM = true)
    public static void inviteCommand(CommandContext command) {
        if(command.getArgCount() > 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        command.replyWith(Utilities.getProperty("misc.inviteLink"));
        Logger.logCommand(command);
    }
}
