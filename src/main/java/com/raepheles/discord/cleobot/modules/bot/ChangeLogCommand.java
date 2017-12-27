package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

/**
 * Created by Rae on 19/12/2017.
 */
public class ChangeLogCommand {

    @BotCommand(command = "changelog",
            description = "Changelog.",
            usage = "changelog",
            module = "Bot",
            allowPM = true)
    public static void changelogCommand(CommandContext command) {
        if(command.getArgCount() > 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        command.replyWith("Changelog: <" + Utilities.CHANGELOG_URL + ">");
        Logger.logCommand(command);
    }
}
