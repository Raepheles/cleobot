package com.raepheles.discord.cleobot.modules.raidfinder;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.RaidFinderEntry;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Rae on 28/12/2017.
 * Command for adding an entry to raid finder list
 */
@SuppressWarnings("unused")
public class FindCommand {

    @BotCommand(command = {"raidfinder", "find"},
            aliases = {"raid", "rf"},
            description = "Adds your account to raid finder list.",
            usage = "raidfinder find *server_name* *account_name* *raid_code* *raid_level* *user_note*",
            module = "Raid Finder",
            allowPM = true)
    public static void findCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() < 5) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName = command.getArgument(2).toUpperCase();
        String accountName = command.getArgument(3);
        String raidCode = command.getArgument(4);
        String raidLevel = "";
        if(!raidCode.equals("bdh")
                && !raidCode.equals("rdh")
                && !raidCode.equals("idh")
                && !raidCode.equals("pdh")) {
            raidLevel = command.getArgCount() > 5 ? command.getArgument(5) : "";
        }
        String userNote = "";
        if(!raidLevel.isEmpty())
            userNote += command.getArguments().stream().skip(6).collect(Collectors.joining(" "));
        else
            userNote += command.getArguments().stream().skip(5).collect(Collectors.joining(" "));

        // Check if serverName is valid
        if(!serverName.equalsIgnoreCase("eu") &&
                !serverName.equalsIgnoreCase("america") &&
                !serverName.equalsIgnoreCase("asia")) {
            command.replyWith(String.format(Utilities.getProperty("userdata.illegalServerArgument"), serverName));
            Logger.logCommand(command, "Illegal server argument");
            return;
        }

        // Check if raidCode is valid
        if(!raidCode.equalsIgnoreCase("bd") &&
                !raidCode.equalsIgnoreCase("fd") &&
                !raidCode.equalsIgnoreCase("id") &&
                !raidCode.equalsIgnoreCase("pd") &&
                !raidCode.equalsIgnoreCase("bdh") &&
                !raidCode.equalsIgnoreCase("fdh") &&
                !raidCode.equalsIgnoreCase("idh") &&
                !raidCode.equalsIgnoreCase("pdh") &&
                !raidCode.equalsIgnoreCase("cr")) {
            command.replyWith(String.format(Utilities.getProperty("raidfinder.illegalRaidCodeArg"), raidCode));
            Logger.logCommand(command, "Illegal raid code argument");
            return;
        }

        // Check if accountName is saved
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        if(userData == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "user data"));
            return;
        }

        boolean accountSaved = false;
        for(int i = 0; i < userData.length(); i++) {
            long id = ((Number) userData.getJSONObject(i).get("id")).longValue();
            if(id == command.getAuthor().getLongID()) {
                JSONArray accounts = userData.getJSONObject(i).getJSONArray("accounts");
                for(int j = 0; j < accounts.length(); j++) {
                    String server = accounts.getJSONObject(j).getString("server");
                    if(!serverName.equalsIgnoreCase(server))
                        continue;
                    String name = accounts.getJSONObject(j).getString("name");
                    if(accountName.equalsIgnoreCase(name)) {
                        accountSaved = true;
                        accountName = name;
                        break;
                    }
                }
                if(accountSaved)
                    break;
            }
        }

        if(accountSaved) {
            long currentEpochTime = ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond();
            RaidFinderEntry newEntry = new RaidFinderEntry(currentEpochTime, serverName, accountName, raidCode, raidLevel, command.getAuthor(), userNote);
            if(!newEntry.isLegitEntry()) {
                command.replyWith(Utilities.getProperty("raidfinder.notLegitEntry"));
                Logger.logCommand(command, "Not legit entry");
            } else {
                // Check if there is already entry from author
                List<RaidFinderEntry> entries = Utilities.getRaidFinderEntries();
                for(RaidFinderEntry entry: entries) {
                    if(entry.getUser().equals(command.getAuthor())) {
                        command.replyWith(String.format(Utilities.getProperty("raidfinder.alreadyHasEntry"), Utilities.getRaidFinderTimeOut()-entry.getTime()));
                        Logger.logCommand(command);
                        return;
                    }
                }
                Utilities.addRaidFinderEntry(newEntry);
                command.replyWith(Utilities.getProperty("raidfinder.success"));
                Logger.logCommand(command);
            }
        } else {
            command.replyWith(String.format(Utilities.getProperty("userdata.accountNotSaved"), accountName, serverName));
            Logger.logCommand(command, "Account not saved");
        }
    }
}
