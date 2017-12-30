package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;

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
            return;
        }
        if(command.getArgCount() <= 2) {
            command.sendUsage();
            return;
        }
        long userId = Long.parseLong(command.getArgument(1));
        if(command.getClient().getUserByID(userId) == null) {
            command.replyWith(String.format(Utilities.getProperty("administration.userNotFound"), command.getArgument(1)));
            return;
        }
        String msg = "";
        for(int i = 2; i < command.getArguments().size(); i++) {
            if(i == command.getArguments().size()-1)
                msg += command.getArgument(i);
            else
                msg += command.getArgument(i) + " ";
        }
        Utilities.sendMessage(command.getClient().getUserByID(userId).getOrCreatePMChannel(), msg);
    }
}
