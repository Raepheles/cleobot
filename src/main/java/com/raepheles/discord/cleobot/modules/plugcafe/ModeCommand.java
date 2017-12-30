package com.raepheles.discord.cleobot.modules.plugcafe;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;

/**
 * Created by Rae on 19/12/2017.
 * Command for changing plug cafe notifications mode.
 */
@SuppressWarnings("unused")
public class ModeCommand {

    @BotCommand(command = {"plugcafe", "mode"},
            aliases = "plug",
            description = "Changes plug cafe mode. Modes: *embed*, *text*, *mixed*.",
            usage = "plugcafe mode *mode_name*",
            module = "Plug Cafe",
            permissions = Permissions.MANAGE_CHANNELS)
    public static void modeCommand(CommandContext command) {
        if (!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if (command.getArgCount() != 3) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        /*
        0 - embed (default)
        1 - text
        2 - mixed
         */
        String arg = command.getArgument(2);
        int mode;
        if(arg.equalsIgnoreCase("embed")) {
            mode = 0;
        } else if(arg.equalsIgnoreCase("text")) {
            mode = 1;
        } else if(arg.equalsIgnoreCase("mixed")) {
            mode = 2;
        } else {
            command.replyWith(String.format(Utilities.getProperty("notifications.illegalModeArg"), arg));
            Logger.logCommand(command, "Illegal argument");
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        long guildId = command.getGuild().getLongID();
        long channelId = -1;
        int index = -1;
        for (int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number) guilds.getJSONObject(i).get("id")).longValue();
            if (guildId == currentId) {
                index = i;
                channelId = ((Number) guilds.getJSONObject(i).get(Utilities.getProperty("guilds.plugCafeChannel"))).longValue();
                break;
            }
        }
        if(channelId == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.setChannelFirst"), "plug cafe"));
            Logger.logCommand(command, "Notification channel not set");
            return;
        }
        if(command.getClient().getChannelByID(channelId) == null) {
            command.replyWith(String.format(Utilities.getProperty("notifications.statusDeleted"), "Plug cafe", command.getPrefix(), "plugcafe"));
            Logger.logCommand(command, "Notification channel is deleted");
            return;
        }

        guilds.getJSONObject(index).put(Utilities.getProperty("guilds.plugCafeMode"), mode);
        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
        command.replyWith(String.format(Utilities.getProperty("notifications.modeChangeSuccess"), arg.toLowerCase()));
        sendSampleNotification(command, mode);
        Logger.logCommand(command);
    }

    private static void sendSampleNotification(CommandContext command, int mode) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.withTimestamp(LocalDateTime.now());
        embed.withAuthorName(command.getClient().getOurUser().getName());
        embed.withThumbnail(command.getClient().getOurUser().getAvatarURL());
        embed.withAuthorIcon(command.getClient().getOurUser().getAvatarURL());
        embed.withTitle("Title");
        embed.withDesc(Utilities.getProperty("misc.loremIpsum"));
        embed.withUrl("https://www.plug.game/kingsraid-en");
        embed.withAuthorUrl("https://www.plug.game/kingsraid-en");

        if(mode == 0) {
            command.replyWith(embed.build());
        }
        if(mode == 1) {
            String str = String.format("Author: %s\nTitle: Sample Title\nLink: %s\n%s",
                    command.getClient().getOurUser().getName(),
                    Utilities.PLUG_CAFE_BASE_URL,
                    Utilities.getProperty("misc.loremIpsum"));
            command.replyWith(str);
        }
        if(mode == 2) {
            String str = String.format("Sample Title: %s", Utilities.PLUG_CAFE_BASE_URL);
            command.replyWith(str, embed.build());
        }
    }
}
