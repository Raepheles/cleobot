package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;

/**
 * Created by Rae on 19/12/2017.
 * Command for sending feedback to bot owner
 */
@SuppressWarnings("unused")
public class FeedbackCommand {

    @BotCommand(command = "feedback",
            aliases = "fb",
            description = "Sends feedback to bot owner.",
            usage = "feedback *this is sample feedback*.",
            module = "Bot",
            allowPM = true)
    public static void feedbackCommand(CommandContext command) {
        if(command.getArgCount() <= 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        if(Utilities.isBanned(command, "Feedback")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(!Utilities.isFeedbackActive()) {
            command.replyWith(Utilities.getProperty("misc.feedbackNotActive"));
            Logger.logCommand(command, "Feedback not active");
            return;
        }
        long feedbackChannelId = Utilities.getFeedbackChannelId();
        if(feedbackChannelId == -1) {
            command.replyWith(Utilities.getProperty("misc.feedbackChannelNotSet"));
            Logger.logCommand(command, "Feedback not active");
            return;
        }
        IChannel feedbackChannel = command.getClient().getChannelByID(Utilities.getFeedbackChannelId());
        if(feedbackChannel == null) {
            command.replyWith(Utilities.getProperty("misc.feedbackChannelCannotConnect"));
            Logger.logCommand(command, "Cannot connect feedback channel");
            return;
        }
        String arg = String.join(" ", command.getArguments());
        arg = arg.substring(arg.indexOf(" ")+1, arg.length());

        if(arg.length() >= 1024) {
            command.replyWith("Feedback must contain less than 1024 characters. If you need to send wall of text, " +
                    "send pastebin link.");
            Logger.logCommand(command, "Character limit exceeded");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.withAuthorName(command.getAuthor().getName() + "(" + command.getAuthor().getLongID() + ")");
        embed.withAuthorIcon(command.getAuthor().getAvatarURL());
        embed.withThumbnail(command.getAuthor().getAvatarURL());
        embed.appendField("Feedback", arg, false);
        embed.withTimestamp(LocalDateTime.now());

        try {
            Utilities.sendMessage(command.getClient().getChannelByID(Utilities.getFeedbackChannelId()), embed.build());
            command.replyWith("Feedback has been successfully sent.");
            Logger.logCommand(command);
        } catch(DiscordException de) {
            command.replyWith("There was an error. Please try again later.");
            Logger.logCommand(command, "DiscordException caught");
        }

    }
}
