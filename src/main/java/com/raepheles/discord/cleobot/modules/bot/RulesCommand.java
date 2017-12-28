package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

/**
 * Created by Rae on 28/12/2017.
 */
public class RulesCommand {

    @BotCommand(command = "rules",
            description = "Rules for certain modules and features",
            usage = "invite",
            module = "Bot",
            allowPM = true)
    public static void rulesCommand(CommandContext command) {
        if(command.getArgCount() > 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        command.replyWith(Utilities.getProperty("misc.rules"));
        Logger.logCommand(command);
    }
}
