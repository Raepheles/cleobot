package com.raepheles.discord.cleobot.modules.userdata;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

/**
 * Created by Rae on 28/12/2017.
 */
public class SettingsCommand {

    @BotCommand(command = {"userdata", "settings", "view"},
            aliases = {"data", "ud"},
            description = "Shows your settings.",
            usage = "userdata settings view",
            module = "User Data",
            allowPM = true)
    public static void settingsViewCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if (Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if (command.getArgCount() != 3) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        boolean allowHeroList = false;
        for (int i = 0; i < userData.length(); i++) {
            long id = ((Number) userData.getJSONObject(i).get("id")).longValue();
            if (id == command.getAuthor().getLongID()) {
                int currentAllowHeroListSetting = (int) userData.getJSONObject(i).get("allow herolist");
                if (currentAllowHeroListSetting == 0)
                    allowHeroList = false;
                else if (currentAllowHeroListSetting == 1)
                    allowHeroList = true;
            }
        }

        String reply = allowHeroList ? "Your settings\nHero List: Public" : "Your settings\nHero List: Private";
        command.replyWith(reply);
        Logger.logCommand(command);
    }

    @BotCommand(command = {"userdata", "settings", "change", "herolist"},
            aliases = {"data", "ud"},
            description = "Changes your hero list setting.",
            usage = "userdata settings change herolist *status_to_change*",
            module = "User Data",
            allowPM = true)
    public static void settingsChangeHeroListCommand(CommandContext command) {
        if (!Utilities.checkBotChannel(command)) {
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
        if(!command.getArgument(4).equalsIgnoreCase("public") &&
                !command.getArgument(4).equalsIgnoreCase("private")) {
            command.replyWith(String.format(Utilities.getProperty("userdata.illegalSetting"), "herolist", "`private`, `public`"));
            Logger.logCommand(command, "Illegal Argument");
            return;
        }
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        boolean allowHeroList = false;
        int index = -1;
        for (int i = 0; i < userData.length(); i++) {
            long id = ((Number) userData.getJSONObject(i).get("id")).longValue();
            if (id == command.getAuthor().getLongID()) {
                index = i;
                int currentAllowHeroListSetting = (int) userData.getJSONObject(i).get("allow herolist");
                if (currentAllowHeroListSetting == 0)
                    allowHeroList = false;
                else if (currentAllowHeroListSetting == 1)
                    allowHeroList = true;
            }
        }

        if(command.getArgument(4).equalsIgnoreCase("public")) {
            if(allowHeroList) {
                command.replyWith(String.format(Utilities.getProperty("userdata.settingHeroListSameStatus"), "public"));
                Logger.logCommand(command, "Same status");
            } else {
                userData.getJSONObject(index).put("allow herolist", 1);
                command.replyWith(String.format(Utilities.getProperty("userdata.settingHeroListSuccess"), "public"));
                Utilities.writeToJsonFile(userData, Utilities.getProperty("files.userdata"));
                Logger.logCommand(command);
            }
        } else if(command.getArgument(4).equalsIgnoreCase("private")) {
            if(!allowHeroList) {
                command.replyWith(String.format(Utilities.getProperty("userdata.settingHeroListSameStatus"), "private"));
                Logger.logCommand(command, "Same status");
            } else {
                userData.getJSONObject(index).put("allow herolist", 0);
                command.replyWith(String.format(Utilities.getProperty("userdata.settingHeroListSuccess"), "private"));
                Utilities.writeToJsonFile(userData, Utilities.getProperty("files.userdata"));
                Logger.logCommand(command);
            }
        }
    }
}
