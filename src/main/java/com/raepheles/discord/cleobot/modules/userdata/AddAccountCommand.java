package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Rae on 27/12/2017.
 */
public class AddAccountCommand {

    @BotCommand(command = {"userdata", "add", "account"},
            aliases = {"data", "ud"},
            description = "Adds an account to your info.",
            usage = "userdata add account *server* *account_name*",
            module = "User Data",
            allowPM = true)
    public static void addAccountCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() != 5) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName = command.getArgument(3).toUpperCase();
        String accountNameArg = command.getArgument(4);
        if(!serverName.equalsIgnoreCase("eu") &&
                !serverName.equalsIgnoreCase("asia") &&
                !serverName.equalsIgnoreCase("america")) {
            command.replyWith(String.format(Utilities.getProperty("userdata.illegalServerArgument"), serverName));
            Logger.logCommand(command, "Illegal Argument");
            return;
        }
        JSONArray myinfo = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        int index = -1;
        boolean accountExists = false;
        boolean hasAccountAtSameServer = false;
        for(int i = 0; i < myinfo.length(); i++) {
            long userId = ((Number)myinfo.getJSONObject(i).get("id")).longValue();
            if(command.getAuthor().getLongID() == userId)
                index = i;
            JSONArray accounts = myinfo.getJSONObject(i).getJSONArray("accounts");
            for(int j = 0; j < accounts.length(); j++) {
                // Check if user has an account under same server
                if(command.getAuthor().getLongID() == userId) {
                    if(accounts.getJSONObject(j).getString("server").equalsIgnoreCase(serverName)) {
                        hasAccountAtSameServer = true;
                        break;
                    }
                }
                // Continue for servers that are not same as serverName
                if(!accounts.getJSONObject(j).getString("server").equalsIgnoreCase(serverName))
                    continue;
                // If you find the same account break
                if(accounts.getJSONObject(j).getString("name").equalsIgnoreCase(accountNameArg)) {
                    accountExists = true;
                    break;
                }
            }
            if(accountExists)
                break;
            if(hasAccountAtSameServer)
                break;
        }

        // A user can have up to 5 accounts
        if(hasAccountAtSameServer) {
            command.replyWith(String.format(Utilities.getProperty("userdata.hasAccountAtSameServer"), serverName.toLowerCase()));
            Logger.logCommand(command, "Has account at this server");
            return;
        }

        // A user cannot create account if ign already exists
        if(accountExists) {
            command.replyWith(String.format(Utilities.getProperty("userdata.accountExists"), accountNameArg, serverName));
            Logger.logCommand(command, "Account exists");
            return;
        }

        Logger.logCommand(command);

        if(index == -1) {
            JSONObject newEntry = new JSONObject();
            newEntry.put("id", command.getAuthor().getLongID());
            newEntry.put("allow herolist", 1);
            JSONArray accounts = new JSONArray();
            JSONObject account = new JSONObject();
            account.put("server", serverName.toLowerCase());
            account.put("name", accountNameArg);
            account.put("heroes", new JSONArray());
            accounts.put(account);
            newEntry.put("accounts", accounts);
            myinfo.put(newEntry);
        } else {
            JSONObject userData = myinfo.getJSONObject(index);
            JSONObject newAccount = new JSONObject();
            newAccount.put("server", serverName.toLowerCase());
            newAccount.put("name", accountNameArg);
            newAccount.put("heroes", new JSONArray());
            userData.getJSONArray("accounts").put(newAccount);
        }
        command.replyWith(String.format(Utilities.getProperty("userdata.addAccountSuccess"), accountNameArg, serverName));
        Utilities.writeToJsonFile(myinfo, Utilities.getProperty("files.userdata"));
    }
}
