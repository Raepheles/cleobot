package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

/**
 * Created by Rae on 28/12/2017.
 */
public class RemoveAccountCommand {

    @BotCommand(command = {"userdata", "remove", "account"},
            aliases = {"data", "ud"},
            description = "Removes an account from your data.",
            usage = "userdata remove account *server* *account_name*",
            module = "User Data",
            allowPM = true)
    public static void removeAccountCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if (Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if (command.getArgCount() != 5) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName = command.getArgument(3).toUpperCase();
        String accountName = command.getArgument(4);

        // Check if serverName is legit
        if(!serverName.equalsIgnoreCase("eu") &&
                !serverName.equalsIgnoreCase("america") &&
                !serverName.equalsIgnoreCase("asia")) {
            command.replyWith(String.format(Utilities.getProperty("userdata.illegalServerArgument"), serverName));
            Logger.logCommand(command, "Illegal server argument");
            return;
        }

        // Check if accountName exists under serverName
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        for(int i = 0; i < userData.length(); i++) {
            long id = ((Number) userData.getJSONObject(i).get("id")).longValue();
            if (id == command.getAuthor().getLongID()) {
                JSONArray accounts = userData.getJSONObject(i).getJSONArray("accounts");
                for (int j = 0; j < accounts.length(); j++) {
                    String tempAccountName = accounts.getJSONObject(j).getString("name");
                    if (accountName.equalsIgnoreCase(tempAccountName)) {
                        accounts.remove(j);
                        Utilities.writeToJsonFile(userData, Utilities.getProperty("files.userdata"));
                        command.replyWith(String.format(Utilities.getProperty("userdata.accountRemoved"), accountName, serverName));
                        Logger.logCommand(command);
                        return;
                    }
                }
            }
        }
        // If code comes to here it means author doesn't have the account
        command.replyWith(String.format(Utilities.getProperty("userdata.accountNotSaved"), accountName, serverName));
        Logger.logCommand(command, "Account not saved");
    }
}
