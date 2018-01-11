package com.raepheles.discord.cleobot.modules.raidfinder;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.RaidFinderEntry;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

import java.util.List;

/**
 * Created by Rae on 30/12/2017.
 * Command for setting user's raid finder entry to found.
 */
@SuppressWarnings("unused")
public class FoundCommand {

    @BotCommand(command = {"raidfinder", "found"},
            aliases = {"raid", "rf"},
            description = "Removes your entry from raid finder list.",
            usage = "raidfinder found",
            module = "Raid Finder",
            allowPM = true)
    public static void foundCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() < 2 || command.getArgCount() > 5) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }

        List<RaidFinderEntry> raidFinderEntries = Utilities.getRaidFinderEntries();
        for(RaidFinderEntry entry: raidFinderEntries) {
            if(entry.getUser() == command.getAuthor()) {
                if(entry.isFound()) {
                    command.replyWith(Utilities.getProperty("raidfinder.alreadyFound"));
                    Logger.logCommand(command, "Already set found");
                    return;
                } else {
                    entry.setFound();
                    command.replyWith(Utilities.getProperty("raidfinder.setFound"));
                    Logger.logCommand(command);
                    return;
                }
            }
        }

        command.replyWith(Utilities.getProperty("raidfinder.entryNotFound"));
        Logger.logCommand(command, "Entry not found");
    }
}
