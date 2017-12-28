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
public class UpdateHeroCommand {

    @BotCommand(command = {"userdata", "update", "hero"},
            aliases = {"data", "ud"},
            description = "Updates a hero in your acount.",
            usage = "userdata update hero *server_name* *account_name* *hero_name* *hero_rarity* *hero_level*",
            module = "User Data",
            allowPM = true)
    public static void updateHeroCommand(CommandContext command) {
        if(!Utilities.checkBotChannel(command)) {
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(Utilities.isBanned(command, "User Data Module")) {
            Logger.logCommand(command, "BANNED");
            return;
        }
        if(command.getArgCount() != 8) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String serverName = command.getArgument(3).toUpperCase();
        String accountName = command.getArgument(4);
        String heroName = command.getArgument(5);
        String heroRarity = command.getArgument(6).toUpperCase();
        int heroLevel;
        try {
            heroLevel = Integer.parseInt(command.getArgument(7));
        } catch(NumberFormatException nfe) {
            command.replyWith(String.format(Utilities.getProperty("userdata.numberFormatException"), "hero level"));
            Logger.logCommand(command, "Number Format Exception");
            return;
        }

        // Check if heroName exists in heroes list
        boolean heroFound = false;
        JSONArray heroes = Utilities.getHeroesArray();
        for(int i = 0; i < heroes.length(); i++) {
            String name = heroes.getJSONObject(i).getString("name");
            if(heroName.equalsIgnoreCase(name)) {
                heroName = name;
                heroFound = true;
                break;
            }
        }
        if(!heroFound) {
            String reply = String.format(Utilities.getProperty("userdata.illegalHeroName"), heroName);
            String didYouMean = Utilities.getSimilarHero(heroName);
            didYouMean = didYouMean == null ? "" : " Did you mean: `" + didYouMean + "`?";
            reply += didYouMean;
            command.replyWith(reply);
            Logger.logCommand(command, "Illegal Hero Name Argument");
            return;
        }

        // Check if heroRarity is legit
        if(heroRarity.equalsIgnoreCase("2") &&
                heroRarity.equalsIgnoreCase("3") &&
                heroRarity.equalsIgnoreCase("4") &&
                heroRarity.equalsIgnoreCase("5") &&
                heroRarity.equalsIgnoreCase("T1") &&
                heroRarity.equalsIgnoreCase("T2") &&
                heroRarity.equalsIgnoreCase("T3") &&
                heroRarity.equalsIgnoreCase("T4") &&
                heroRarity.equalsIgnoreCase("T5")) {
            command.replyWith(Utilities.getProperty("userdata.illegalHeroRarity"));
            Logger.logCommand(command, "Illegal Hero Rarity Argument");
            return;
        }

        // Check if heroLevel is legit
        if(heroLevel < 1 || heroLevel > 80) {
            command.replyWith(Utilities.getProperty("userdata.illegalHeroLevel"));
            Logger.logCommand(command, "Illegal Hero Level Argument");
            return;
        }

        // Check if user has the accountName in serverName and doesn't have the hero
        JSONArray userData = Utilities.readJsonFromFile(Utilities.getProperty("files.userdata"));
        int index = -1;
        int accountIndex = -1;
        int heroIndex = -1;
        boolean accountSaved = false;
        boolean hasHero = false;
        for(int i = 0; i < userData.length(); i++) {
            long id = ((Number)userData.getJSONObject(i).get("id")).longValue();
            if(id == command.getAuthor().getLongID()) {
                index = i;
                JSONArray accounts = userData.getJSONObject(i).getJSONArray("accounts");
                for(int j = 0; j < accounts.length(); j++) {
                    if(!accounts.getJSONObject(j).getString("server").equalsIgnoreCase(serverName))
                        continue;
                    accountIndex = j;
                    String name = accounts.getJSONObject(j).getString("name");
                    if(name.equalsIgnoreCase(accountName)) {
                        accountSaved = true;
                    }
                    JSONArray accountHeroes = accounts.getJSONObject(j).getJSONArray("heroes");
                    for(int k = 0; k < accountHeroes.length(); k++) {
                        String tempHeroName = accountHeroes.getJSONObject(k).getString("name");
                        if(tempHeroName.equalsIgnoreCase(heroName)) {
                            heroIndex = k;
                            hasHero = true;
                            break;
                        }
                    }
                }
            }
            if(accountSaved || hasHero)
                break;
        }
        if(!accountSaved) {
            command.replyWith(String.format(Utilities.getProperty("userdata.accountNotSaved"), accountName, serverName));
            Logger.logCommand(command, "Account not saved");
            return;
        }
        if(!hasHero) {
            command.replyWith(String.format(Utilities.getProperty("userdata.doesNotHaveTheHero"), heroName, accountName, serverName));
            Logger.logCommand(command, "Here is not saved");
            return;
        }

        // Get hero object
        JSONObject heroObj = userData.getJSONObject(index)
                .getJSONArray("accounts")
                .getJSONObject(accountIndex)
                .getJSONArray("heroes")
                .getJSONObject(heroIndex);
        String oldHeroRarity = heroObj.getString("rarity").toUpperCase();
        int oldHeroLevel = (int)heroObj.get("level");


        // Check if entered hero level and rarity is higher than current ones
        if(heroLevel <= oldHeroLevel && heroRarity.compareTo(oldHeroRarity) <= 0) {
            command.replyWith(String.format(Utilities.getProperty("userdata.lowerHeroData"), heroName, oldHeroLevel, oldHeroRarity));
            Logger.logCommand(command, "Low hero data");
            return;
        }

        // Passed all checks update hero
        heroObj.put("level", heroLevel);
        heroObj.put("rarity", heroRarity);
        Utilities.writeToJsonFile(userData, Utilities.getProperty("files.userdata"));
        command.replyWith(String.format(Utilities.getProperty("userdata.updateHeroSuccess"), heroName, accountName, serverName));
        Logger.logCommand(command);
    }
}
