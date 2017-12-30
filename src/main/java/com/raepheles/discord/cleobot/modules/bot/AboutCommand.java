package com.raepheles.discord.cleobot.modules.bot;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Rae on 19/12/2017.
 * Command for getting about message.
 */
@SuppressWarnings("unused")
public class AboutCommand {

    @BotCommand(command = "about",
            description = "About bot.",
            usage = "about",
            module = "Bot",
            allowPM = true)
    public static void aboutCommand(CommandContext command) {
        if(command.getArgCount() > 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String owner = command.getClient().getApplicationOwner().getName();
        String ownerAvatar = command.getClient().getApplicationOwner().getAvatarURL();
        String botAvatar = command.getClient().getOurUser().getAvatarURL();
        String ownerTag = command.getClient().getApplicationOwner().getDiscriminator();
        EmbedBuilder embed = new EmbedBuilder();
        embed.withAuthorName(owner + "#" + ownerTag);
        embed.withAuthorIcon(ownerAvatar);
        embed.withThumbnail(botAvatar);
        embed.withAuthorUrl("https://discord.gg/dXcVDYU");
        embed.withColor(0,0,0);
        String desc =  "**" + command.getClient().getOurUser().getName() + "** is a bot I made for mobile game called **King's Raid**. " +
                "All the information is provided statically except for the images which are taken from wiki. " +
                "For command list use **" + command.getPrefix() + "help** command. " +
                "You can contact me via `feedback` command or by simply sending direct message to the bot. If you have " +
                "servers in common with me like reddit community server you can also DM me. I also have private server " +
                "where I test my stuff if you want you can also join there by clicking my name above.";
        embed.withDesc(desc);

        embed.withFooterText("Version: " + Utilities.getProperty("application.version"));

        command.replyWith(embed.build());
        Logger.logCommand(command);
    }
}
