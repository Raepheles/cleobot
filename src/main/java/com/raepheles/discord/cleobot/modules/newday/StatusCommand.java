package com.raepheles.discord.cleobot.modules.newday;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Created by Rae on 23/12/2017.
 * Command for getting new day notifications status
 */
@SuppressWarnings("unused")
public class StatusCommand {

    @BotCommand(command = {"newday", "status"},
            description = "Shows new day notifications status.",
            usage = "newday status",
            module = "New Day")
    public static void newDayStatusCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            return;
        }
        if(command.getArgCount() > 2) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }
        long guildId = command.getGuild().getLongID();
        int index = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                index = i;
                break;
            }
        }
        Logger.logCommand(command);
        long newDayChannel = ((Number)guilds.getJSONObject(index).get(Utilities.getProperty("guilds.newDayChannel"))).longValue();
        if(newDayChannel == -1) {
            command.replyWith(String.format(Utilities.getProperty("notifications.statusOff"), "New day"));
            return;
        }
        IChannel newDayIChannel = command.getClient().getChannelByID(newDayChannel);
        if(newDayIChannel == null) {
            command.replyWith(String.format(Utilities.getProperty("notifications.statusDeleted"), "New day", command.getPrefix() + "newday"));
            guilds.getJSONObject(index).put(Utilities.getProperty("guilds.newDayChannel"), -1);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            return;
        }
        int statusEu = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).get("EUROPE");
        int statusAmerica = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).get("AMERICA");
        int statusAsia = (int)guilds.getJSONObject(index).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).get("ASIA");
        String eu = "OFF";
        String america = "OFF";
        String asia = "OFF";
        if(statusEu == 1)
            eu = "ON";
        if(statusAmerica == 1)
            america = "ON";
        if(statusAsia == 1)
            asia = "ON";
        String str = "New day notifications channel: " + newDayIChannel.mention() + "\nEUROPE: " + eu + "\nAMERICA: " +
                america + "\nASIA: " + asia;
        command.replyWith(str);
    }
}
