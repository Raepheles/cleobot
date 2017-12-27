package com.raepheles.discord.cleobot.modules.plugcafe;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

/**
 * Created by Rae on 19/12/2017.
 */
public class StatusCommand {

    @BotCommand(command = {"plugcafe", "status"},
            aliases = "plug",
            description = "Shows plug cafe notifications channel.",
            usage = "plugcafe status",
            module = "Plug Cafe")
    public static void plugCafeStatus(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(command.getArgCount() > 2) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }

        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        long guildId = command.getGuild().getLongID();
        long channelId = -1;
        int index = -1;
        int mode = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                channelId = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.plugCafeChannel"))).longValue();
                mode = (int)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.plugCafeMode"));
                break;
            }
        }
        if(channelId == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.statusOff"), "Plug cafe"));
            return;
        }
        if(command.getClient().getChannelByID(channelId) == null) {
            command.replyWith(String.format(Utilities.getProperty("notifications.statusDeleted"), "Plug cafe", command.getPrefix(), "plugcafe"));
            guilds.getJSONObject(index).put(Utilities.getProperty("guilds.plugCafeChannel"), -1);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            return;
        }

        String modeName = "";
        if(mode == 0)
            modeName = "embed";
        if(mode == 1)
            modeName = "text";
        if(mode == 2)
            modeName = "mixed";

        command.replyWith(String.format(Utilities.getProperty("notifications.statusOn"),
                "Plug cafe",
                command.getClient().getChannelByID(channelId).mention(),
                "\nNotification mode is `" + modeName + "`."));
        Logger.logCommand(command);

    }

}
