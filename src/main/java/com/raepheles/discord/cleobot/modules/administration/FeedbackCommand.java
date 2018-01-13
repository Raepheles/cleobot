package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

/**
 * Created by Rae on 26/12/2017.
 * Command for sending feedback to bot owner.
 */
@SuppressWarnings("unused")
public class FeedbackCommand {

    @BotCommand(command = "feedbackstatus",
            description = "Activate/deactivate feedback system",
            usage = "feedbackstatus *status*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void feedbackCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            Logger.logCommand(command, "NOT ADMIN");
            return;
        }
        if(command.getArgCount() != 2) {
            command.sendUsage();
            return;
        }
        if(command.getArgument(1).equalsIgnoreCase("on")) {
            Utilities.setFeedbackActive(true);
            command.replyWith(String.format(Utilities.getProperty("administration.feedbackStatusChange"), "active"));
        } else if(command.getArgument(1).equalsIgnoreCase("off")) {
            Utilities.setFeedbackActive(false);
            command.replyWith(String.format(Utilities.getProperty("administration.feedbackStatusChange"), "inactive"));
        } else {
            command.replyWith(String.format(Utilities.getProperty("administration.illegalArg"), command.getArgument(1)));
        }
    }
}
