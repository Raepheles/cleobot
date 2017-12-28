package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Rae on 28/12/2017.
 */
public class ListHeroesCommand {

    @BotCommand(command = {"userdata", "list", "heroes"},
            aliases = {"data", "ud"},
            description = "Lists heroes of an account.",
            usage = "userdata list heroes *server_name* *account_name*",
            module = "User Data",
            allowPM = true)
    public static void listHeroesCommand(CommandContext command) {
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
        String accountName = command.getArgument(4);

        // Check if serverName is valid
        if(!serverName.equalsIgnoreCase("eu") &&
                !serverName.equalsIgnoreCase("america") &&
                !serverName.equalsIgnoreCase("asia")) {
            command.replyWith(String.format(Utilities.getProperty("userdata.illegalServerArgument"), serverName));
            Logger.logCommand(command, "Illegal server argument");
            return;
        }

        // Check if accountName is saved under the server
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        boolean accountSaved = false;
        boolean accountBelongsToAuthor = false;
        boolean allowHeroList = false;
        int userDataIndex = -1;
        int accountIndex = -1;
        for(int i = 0; i < userData.length(); i++) {
            long id = ((Number)userData.getJSONObject(i).get("id")).longValue();
            if(id == command.getAuthor().getLongID())
                accountBelongsToAuthor = true;
            JSONArray accounts = userData.getJSONObject(i).getJSONArray("accounts");
            for(int j = 0; j < accounts.length(); j++) {
                String tempServerName = accounts.getJSONObject(j).getString("server");
                if(!serverName.equalsIgnoreCase(tempServerName))
                    continue;
                String tempAccountName = accounts.getJSONObject(j).getString("name");
                if(accountName.equalsIgnoreCase(tempAccountName)) {
                    accountIndex = j;
                    userDataIndex = i;
                    accountName = tempAccountName;
                    accountSaved = true;
                    break;
                }
            }
            if(accountSaved) {
                int heroListSetting = (int)userData.getJSONObject(i).get("allow herolist");
                if(heroListSetting == 0)
                    allowHeroList = false;
                else if(heroListSetting == 1)
                    allowHeroList = true;
                break;
            }
        }

        // If account is not saved return
        if(!accountSaved) {
            command.replyWith(String.format(Utilities.getProperty("userdata.couldNotFoundAccount"), accountName, serverName));
            Logger.logCommand(command, "Could not found account");
            return;
        }

        if(!allowHeroList && !accountBelongsToAuthor) {
            command.replyWith(String.format(Utilities.getProperty("userdata.notAllowedToListHeroes"), accountName, serverName));
            Logger.logCommand(command, "Not allowed");
            return;
        }

        String reply = "`" + accountName + "` at server `" + serverName.toUpperCase() + "` has the following heroes:\n```";
        reply += String.format("%-6s | %-5s | %-15s\n\n", "Rarity", "Level", "Hero Name");
        JSONArray heroes = userData.getJSONObject(userDataIndex).getJSONArray("accounts").getJSONObject(accountIndex).getJSONArray("heroes");
        for(int i = 0; i < heroes.length(); i++) {
            JSONObject hero = heroes.getJSONObject(i);
            reply += String.format("%-6s | %-5s | %-15s\n", hero.get("rarity"), hero.get("level"), hero.get("name"));
        }
        reply += "```";

        command.replyWith(reply);
        Logger.logCommand(command);
    }

}