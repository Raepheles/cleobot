package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rae on 28/12/2017.
 */
public class AliasesCommand {

    @BotCommand(command = "aliases",
            description = "Aliases for certain commands.",
            usage = "aliases",
            module = "Bot",
            allowPM = true)
    public static void aliasesCommand(CommandContext command) {
        if(command.getArgCount() > 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        List<String> aliases = new ArrayList<>();
        aliases.add(String.format("%-20s | %-20s", "help", "h"));
        aliases.add(String.format("%-20s | %-20s", "feedback", "fb"));
        aliases.add(String.format("%-20s | %-20s", "attributes", "attr"));
        aliases.add(String.format("%-20s | %-20s", "perks", "perk"));
        aliases.add(String.format("%-20s | %-20s", "skills", "skill"));
        aliases.add(String.format("%-20s | %-20s", "plugcafe", "plug"));
        aliases.add(String.format("%-20s | %-20s", "raidfinder", "raid, " + "rf"));
        aliases.add(String.format("%-20s | %-20s", "userdata", "user, " + "ud"));
        command.replyWith("**Aliases**```" + String.join("\n", aliases) + "```");
        Logger.logCommand(command);
    }
}
