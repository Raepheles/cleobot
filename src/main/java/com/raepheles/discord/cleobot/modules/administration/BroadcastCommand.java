package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rae on 26/12/2017.
 */
public class BroadcastCommand {

    @BotCommand(command = "broadcast",
            description = "Send broadcast message to all servers.",
            usage = "broadcast *message*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void broadcastCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            return;
        }
        String msg = "";
        for(int i = 0; i < command.getArguments().size(); i++) {
            if(i == 0)
                continue;
            if(i != command.getArguments().size()-1)
                msg += command.getArgument(i) + " ";
            else
                msg += command.getArgument(i);
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        int success = 0;
        int fail = 0;
        List<Long> fails = new ArrayList<>();
        for(int i = 0; i < guilds.length(); i++) {
            long guildId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            long botchannel = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.botchannel"))).longValue();
            if(botchannel != -1) {
                if(Utilities.hasPerms(command.getClient().getChannelByID(botchannel), command.getClient().getOurUser())) {
                    Utilities.sendMessage(command.getClient().getChannelByID(botchannel), msg);
                    success++;
                } else {
                    fails.add(guildId);
                    fail++;
                }
            } else {
                fails.add(guildId);
                fail++;
            }
        }
        String result = "Broadcast message has been sent to " + success + " guilds. Failed to sent " + fail + " guilds.\n" +
                "Fail list:\n";
        if(fails.size() > 0)
            result += "```";
        for(long failed: fails) {
            String guildName = command.getClient().getGuildByID(failed).getName();
            result += String.format("%-50s - %s\n", guildName.length() > 50 ? guildName.substring(0, 46) + "..." : guildName, failed);
        }
        if(fails.size() > 0)
            result += "```";
        command.replyWith(result);
    }
}
