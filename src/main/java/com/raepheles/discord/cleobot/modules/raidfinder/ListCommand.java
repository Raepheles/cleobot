package com.raepheles.discord.cleobot.modules.raidfinder;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.RaidFinderEntry;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Rae on 28/12/2017.
 * Command for getting raid finder list.
 */
@SuppressWarnings("unused")
public class ListCommand {

    @BotCommand(command = {"raidfinder", "list"},
            aliases = {"raid", "rf"},
            description = "Shows the raid finder list.",
            usage = "raidfinder list *args*",
            module = "Raid Finder",
            allowPM = true)
    public static void findCommand(CommandContext command) {
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() < 2 || command.getArgCount() > 5) {
            command.replyWith(Utilities.getProperty("raidfinder.argCount"));
            Logger.logCommand(command, "Arg count");
            return;
        }
        int argsCount = command.getArgCount() - 2;
        String serverName = "";
        String raidCode = "";
        String raidLevel = "";
        for(int i = 2; i < command.getArgCount(); i++) {
            String currentArg = command.getArgument(i);
            // Check for server arg
            if(currentArg.equalsIgnoreCase("eu") ||
                    currentArg.equalsIgnoreCase("america") ||
                    currentArg.equalsIgnoreCase("asia")) {
                if(serverName.isEmpty()) {
                    serverName = currentArg.toUpperCase();
                } else {
                    command.replyWith(String.format(Utilities.getProperty("raidfinder.duplicateArgument"), "server name"));
                    Logger.logCommand(command, "Duplicate argument");
                    return;
                }
                continue;
            }

            // Check for raid code arg
            if(currentArg.equalsIgnoreCase("bd") ||
                    currentArg.equalsIgnoreCase("fd") ||
                    currentArg.equalsIgnoreCase("id") ||
                    currentArg.equalsIgnoreCase("pd") ||
                    currentArg.equalsIgnoreCase("bdh") ||
                    currentArg.equalsIgnoreCase("fdh") ||
                    currentArg.equalsIgnoreCase("idh") ||
                    currentArg.equalsIgnoreCase("pdh") ||
                    currentArg.equalsIgnoreCase("cr")) {
                if(raidCode.isEmpty()) {
                    raidCode = currentArg.toUpperCase();
                } else {
                    command.replyWith(String.format(Utilities.getProperty("raidfinder.duplicateArgument"), "raid code"));
                    Logger.logCommand(command, "Duplicate argument");
                    return;
                }
                continue;
            }

            // Check for raid level arg
            if(currentArg.equalsIgnoreCase("easy") ||
                    currentArg.equalsIgnoreCase("normal") ||
                    currentArg.equalsIgnoreCase("hard") ||
                    currentArg.equalsIgnoreCase("hell")) {
                if(raidLevel.isEmpty()) {
                    raidLevel = currentArg.toUpperCase();
                } else {
                    command.replyWith(String.format(Utilities.getProperty("raidfinder.duplicateArgument"), "raid level"));
                    Logger.logCommand(command, "Duplicate argument");
                    return;
                }
                continue;
            }

            if(currentArg.matches("\\^[3][5-9]|[4-9][0-9]?$|^100$")) {
                if(raidLevel.isEmpty()) {
                    raidLevel = currentArg;
                } else {
                    command.replyWith(String.format(Utilities.getProperty("raidfinder.duplicateArgument"), "raid level"));
                    Logger.logCommand(command, "Duplicate argument");
                    return;
                }
            } else if(currentArg.matches("\\d+")){
                command.replyWith(Utilities.getProperty("raidfinder.raidLevelOutOfBounds"));
                Logger.logCommand(command, "Raid level out of bounds");
                return;
            }

        }

        // If raid level and raid code are filled check them
        if(!raidLevel.isEmpty() && !raidCode.isEmpty()) {
            // If raid code is CR raid level must be easy, normal, hard or hell
            if(raidCode.equalsIgnoreCase("cr") &&
                    ( !raidLevel.equalsIgnoreCase("easy")
                    && !raidLevel.equalsIgnoreCase("normal")
                    && !raidLevel.equalsIgnoreCase("hard")
                    && !raidLevel.equalsIgnoreCase("hell")
                    )) {
                command.replyWith(String.format(Utilities.getProperty("raidfinder.illegalRaidLevel"), raidCode, raidLevel));
                Logger.logCommand(command, "Illegal raid level");
                return;
            }
            // If raid code is bdh, fdh, idh or pdh raid level must be empty
            if((raidCode.equalsIgnoreCase("bdh")
                    || raidCode.equalsIgnoreCase("fdh")
                    || raidCode.equalsIgnoreCase("idh")
                    || raidCode.equalsIgnoreCase("pdh")
                    ) && !raidLevel.isEmpty()) {
                command.replyWith(String.format(Utilities.getProperty("raidfinder.illegalRaidLevel"), raidCode, raidLevel));
                Logger.logCommand(command, "Illegal raid level");
                return;
            }
            // If raid code is bd, fd, id or pd raid level must be number between 35 and 100
            if(raidCode.equalsIgnoreCase("bd")
            || raidCode.equalsIgnoreCase("fd")
            || raidCode.equalsIgnoreCase("id")
            || raidCode.equalsIgnoreCase("pd")) {
                if(!raidLevel.matches("\\^[3][5-9]|[4-9][0-9]?$|^100$")) {
                    command.replyWith(String.format(Utilities.getProperty("raidfinder.illegalRaidLevel"), raidCode, raidLevel));
                    Logger.logCommand(command, "Illegal raid level");
                    return;
                }
            }
        }

        List<RaidFinderEntry> entries = Utilities.getRaidFinderEntries().stream().filter(entry -> !entry.isFound()).collect(Collectors.toList());
        if(entries.isEmpty()) {
            command.replyWith(Utilities.getProperty("raidfinder.noEntry"));
            Logger.logCommand(command);
            return;
        }
        String reply = String.format("```%-12s | %-12s | %-7s | %-25s | %-50s\n\n", "Time Passed", "Account Name", "Server", "Raid", "User Note");
        if(argsCount == 3) {
            // If there are 3 arguments all arguments must be filled
            if(!serverName.isEmpty() && !raidCode.isEmpty() && !raidLevel.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getRaidCode().equalsIgnoreCase(raidCode)
                            && entry.getRaidLevel().equalsIgnoreCase(raidLevel)
                            && entry.getServerName().equalsIgnoreCase(serverName)) {
                        if(reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
                reply += "```";
                command.replyWith(reply);
                Logger.logCommand(command);
            } else {
                command.replyWith(Utilities.getProperty("raidfinder.illegalArgument"));
                Logger.logCommand(command);
            }
        } else if(argsCount == 2) {
            // If there are 2 arguments only 1 argument can be empty
            if(serverName.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getRaidLevel().equalsIgnoreCase(raidLevel)
                            && entry.getRaidCode().equalsIgnoreCase(raidCode)) {
                        if(reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
            } else if(raidCode.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getServerName().equalsIgnoreCase(serverName)
                            && entry.getRaidLevel().equalsIgnoreCase(raidLevel)) {
                        if(reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
            } else if(raidLevel.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getServerName().equalsIgnoreCase(serverName)
                            && entry.getRaidCode().equalsIgnoreCase(raidCode)) {
                        if(reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
                reply += "```";
                command.replyWith(reply);
                Logger.logCommand(command);
            } else {
                command.replyWith(Utilities.getProperty("raidfinder.illegalArgument"));
                Logger.logCommand(command);
            }
        } else if(argsCount == 1) {
            // If there is 1 argument then at least 1 of the arguments must be filled
            if(!serverName.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getServerName().equalsIgnoreCase(serverName)) {
                        if (reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
            } else if(!raidCode.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getRaidCode().equalsIgnoreCase(raidCode)) {
                        if(reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
            } else if(!raidLevel.isEmpty()) {
                for(RaidFinderEntry entry: entries) {
                    if(entry.getRaidLevel().equalsIgnoreCase(raidLevel)) {
                        if(reply.length() < 1800) {
                            reply += entry.toString() + "\n";
                        } else {
                            break;
                        }
                    }
                }
            }
            reply += "```";
            command.replyWith(reply);
            Logger.logCommand(command);
        } else if(argsCount == 0) {
            for(RaidFinderEntry entry: entries) {
                if(reply.length() < 1800) {
                    reply += entry.toString() + "\n";
                } else {
                    break;
                }
            }
            reply += "```";
            command.replyWith(reply);
            Logger.logCommand(command);
        }
    }
}
