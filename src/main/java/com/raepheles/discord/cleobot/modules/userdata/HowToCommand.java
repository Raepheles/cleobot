package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.RaidFinderEntry;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by Rae on 27/12/2017.
 */
public class HowToCommand {

    @BotCommand(command = {"userdata", "howto"},
            aliases = {"data", "ud"},
            description = "How to use guide for **User Data** and **Raid Finder** modules",
            usage = "userdata howto",
            module = "User Data",
            allowPM = true)
    public static void howToCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(command.getArgCount() != 2) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        command.replyWith(Utilities.getProperty("userdata.howTo"));
        Logger.logCommand(command);
    }
}
