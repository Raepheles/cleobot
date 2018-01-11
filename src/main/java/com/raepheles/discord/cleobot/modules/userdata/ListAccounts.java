package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

/**
 * Created by Rae on 28/12/2017.
 */
public class ListAccounts {

    @BotCommand(command = {"userdata", "list", "accounts"},
            aliases = {"data", "ud"},
            description = "Lists user's accounts.",
            usage = "userdata list accounts",
            module = "User Data",
            allowPM = true)
    public static void listAccounts(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() != 3) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }

        // Find user data
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        JSONArray accounts = null;
        for(int i = 0; i < userData.length(); i++) {
            long id = ((Number) userData.getJSONObject(i).get("id")).longValue();
            if (id == command.getAuthor().getLongID()) {
                accounts = userData.getJSONObject(i).getJSONArray("accounts");
                break;
            }
        }
        if(accounts != null) {
            String reply = "Your accounts:\n";
            for(int i = 0; i < accounts.length(); i++) {
                reply += accounts.getJSONObject(i).getString("server").toUpperCase() + " - " + accounts.getJSONObject(i).getString("name") + "\n";
            }
            command.replyWith(reply);
            Logger.logCommand(command);
        } else {
            command.replyWith(Utilities.getProperty("userdata.noAccounts"));
            Logger.logCommand(command, "No accounts");
        }
    }
}
