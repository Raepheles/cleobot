package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Created by Rae on 27/12/2017.
 * Command for banning a user.
 */
@SuppressWarnings("unused")
public class BanCommand {

    @BotCommand(command = "ban",
            description = "Bans user from using certain feature for certain time.",
            usage = "ban *id* *banned_from* *banned_for* *ban_reason*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void banCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            Logger.logCommand(command, "Not ADMIN");
            return;
        }
        if(command.getArgCount() < 5) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        long id;
        long bannedFor;
        String reason = "";
        String bannedFrom = command.getArgument(2).replaceAll("-", " ");

        try {
            id = Long.parseLong(command.getArgument(1));
            bannedFor = Long.parseLong(command.getArgument(3));
        } catch(NumberFormatException nfe) {
            command.replyWith(Utilities.getProperty("administration.numberFormatException"));
            return;
        }

        // Check if user is already banned from using same thing
        String fileName = Utilities.getProperty("files.banlist");
        JSONArray banList = Utilities.readJsonFromFile(fileName);
        if(banList == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "ban list"));
            return;
        }

        for(int i = 0; i < banList.length(); i++) {
            long userId = ((Number)banList.getJSONObject(i).get("id")).longValue();
            if(userId == id) {
                String tempBannedFrom = banList.getJSONObject(i).getString("banned from");
                long current = ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond();
                long tempBannedAt = ((Number)banList.getJSONObject(i).get("banned at")).longValue();
                long tempBannedFor = ((Number)banList.getJSONObject(i).get("banned for")).longValue();
                // If ban is over erase it
                if(tempBannedAt + tempBannedFor < current) {
                    banList.remove(i);
                    break;
                }
                if(tempBannedFrom.equalsIgnoreCase(bannedFrom)) {
                    command.replyWith(String.format(Utilities.getProperty("administration.alreadyBanned"), id, bannedFrom));
                    Logger.logCommand(command, "User already banned");
                    return;
                }
            }
        }

        for(int i = 4; i < command.getArgCount(); i++) {
            if(i == 4)
                reason += command.getArgument(i);
            else
                reason += " " + command.getArgument(i);
        }

        JSONObject newBanObject = new JSONObject();
        newBanObject.put("id", id);
        newBanObject.put("reason", reason);
        newBanObject.put("banned from", bannedFrom.toLowerCase());
        newBanObject.put("banned for", bannedFor);
        newBanObject.put("banned at", ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond());

        banList.put(newBanObject);
        Utilities.writeToJsonFile(banList, fileName);

        // Record this ban
        JSONArray banRecords = Utilities.readJsonFromFile(Utilities.getProperty("files.banlog"));
        if(banRecords == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "ban records"));
            return;
        }

        JSONObject newBanRecord = new JSONObject();
        newBanRecord.put("reason", reason);
        newBanRecord.put("banned from", bannedFrom.toLowerCase());
        newBanRecord.put("banned for", bannedFor);
        newBanRecord.put("banned at", ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond());
        boolean hasRecord = false;
        // If user has a ban record add new one to it
        for(int i = 0; i < banRecords.length(); i++) {
            long userId = ((Number)banRecords.getJSONObject(i).get("id")).longValue();
            if(userId == id) {
                banRecords.getJSONObject(i).getJSONArray("records").put(newBanRecord);
                hasRecord = true;
                break;
            }
        }
        // If user didn't have ban record already create new one.
        if(!hasRecord) {
            JSONObject newBanUser = new JSONObject();
            newBanUser.put("id", id);
            JSONArray records = new JSONArray();
            records.put(newBanRecord);
            newBanUser.put("records", records);
            banRecords.put(newBanUser);
        }
        // Save ban record
        Utilities.writeToJsonFile(banRecords, Utilities.getProperty("files.banlog"));

        String date = LocalDateTime.ofEpochSecond(ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond() + bannedFor, 0, ZoneOffset.UTC).toString();
        date = date.replaceAll("-", "/").replace("T", " - ");
        date += " UTC";
        command.replyWith(String.format(Utilities.getProperty("administration.banSuccess"), id, bannedFrom, date));
        Logger.logCommand(command);
    }
}
