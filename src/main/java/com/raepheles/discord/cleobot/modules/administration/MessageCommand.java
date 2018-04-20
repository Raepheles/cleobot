package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

/**
 * Created by Rae on 26/12/2017.
 * Command for sending message to a user.
 */
@SuppressWarnings("unused")
public class MessageCommand {

    @BotCommand(command = "message",
            aliases = "msg",
            description = "Sends message to user",
            usage = "message *user_id* *message_to_send*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void messageCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            Logger.logCommand(command, "NOT ADMIN");
            return;
        }
        if(command.getArgCount() <= 2) {
            command.sendUsage();
            return;
        }
        long id = Long.parseLong(command.getArgument(1));
        boolean channel = false;
        StringBuilder msg = new StringBuilder();

        if(command.getClient().getChannelByID(id) == null && command.getClient().getUserByID(id) == null) {
            command.replyWith(String.format(Utilities.getProperty("administration.notUserOrChannel"), id));
            return;
        }

        for(int i = 2; i < command.getArguments().size(); i++) {
            if(i == command.getArguments().size()-1)
                msg.append(command.getArgument(i));
            else
                msg.append(command.getArgument(i)).append(" ");
        }

        if(command.getClient().getChannelByID(id) != null)
            channel = true;

        if(channel) {
            Utilities.sendMessage(command.getClient().getChannelByID(id), msg.toString());
        } else {
            Utilities.sendMessage(command.getClient().getUserByID(id).getOrCreatePMChannel(), msg.toString());
        }

    }
}
