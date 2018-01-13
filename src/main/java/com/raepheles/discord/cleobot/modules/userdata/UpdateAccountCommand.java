package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

/**
 * Created by Rae on 28/12/2017.
 * Command to update user account.
 */
@SuppressWarnings("unused")
public class UpdateAccountCommand {

    @BotCommand(command = {"userdata", "update", "account"},
            aliases = {"data", "ud"},
            description = "Updates your account name.",
            usage = "userdata update account *server* *old_account_name* *new_account_name*",
            module = "User Data",
            allowPM = true)
    public static void updateAccountCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() != 6) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName = command.getArgument(3).toUpperCase();
        String oldAccountName = command.getArgument(4);
        String newAccountName = command.getArgument(5);

        // Check server name
        if(!serverName.equalsIgnoreCase("eu") &&
                !serverName.equalsIgnoreCase("asia") &&
                !serverName.equalsIgnoreCase("america")) {
            command.replyWith(String.format(Utilities.getProperty("userdata.illegalServerArgument"), serverName));
            Logger.logCommand(command, "Illegal Argument");
            return;
        }

        // Check oldAccountName
        // Check if accountName exists under serverName
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        if(userData == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "userdata"));
            return;
        }

        for(int i = 0; i < userData.length(); i++) {
            long id = ((Number) userData.getJSONObject(i).get("id")).longValue();
            if (id == command.getAuthor().getLongID()) {
                JSONArray accounts = userData.getJSONObject(i).getJSONArray("accounts");
                for (int j = 0; j < accounts.length(); j++) {
                    String tempAccountName = accounts.getJSONObject(j).getString("name");
                    if (oldAccountName.equalsIgnoreCase(tempAccountName)) {
                        oldAccountName = tempAccountName;
                        accounts.getJSONObject(j).put("name", newAccountName);
                        Utilities.writeToJsonFile(userData, Utilities.getProperty("files.userdata"));
                        command.replyWith(String.format(Utilities.getProperty("userdata.accountUpdated"), oldAccountName, newAccountName, serverName));
                        Logger.logCommand(command);
                        return;
                    }
                }
            }
        }
        // If code comes to here it means author doesn't have the account
        command.replyWith(String.format(Utilities.getProperty("userdata.accountNotSaved"), oldAccountName, serverName));
        Logger.logCommand(command, "Account not saved");

    }
}
