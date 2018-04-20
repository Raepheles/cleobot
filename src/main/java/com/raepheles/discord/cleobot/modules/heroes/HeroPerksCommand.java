package com.raepheles.discord.cleobot.modules.heroes;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Language;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rae on 19/12/2017.
 * Command for getting hero perks.
 */
@SuppressWarnings("unused")
public class HeroPerksCommand {

    @BotCommand(command = "perks",
            aliases = "perk",
            description = "Get transcendence perk info of hero.",
            usage = "perk *hero_name*",
            module = "Heroes",
            allowPM = true)
    public static void heroPerksCommand(CommandContext command) {
        // Check if bot channel still exists and bot has permissions on it
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if(command.getArgCount() <= 1 || command.getArgCount() > 3) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }

        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        long guildId = command.getGuild().getLongID();
        JSONObject guild = null;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                guild = guilds.getJSONObject(i);
                break;
            }
        }

        if(guild == null)
            return;

        String defaultLanguage = guild.getString("language");

        String arg = command.getArgument(1);
        String languageArg = command.getArgCount() == 3 ? command.getArgument(2) : defaultLanguage;
        Language language = Utilities.getLanguageForCode(languageArg);
        JSONArray heroes = Utilities.getHeroesArray(language);
        JSONArray heroesEnglish = Utilities.getHeroesArray(Language.ENGLISH);
        JSONObject heroObj = null;

        // Banned word check
        List<String> bannedWords = Utilities.getBannedWords();
        if(bannedWords == null) {
            command.replyWith("Error opening a file. Please contact bot owner.");
            return;
        }
        for(String bannedWord: bannedWords) {
            if(arg.equalsIgnoreCase(bannedWord)) {
                command.replyWith(String.format("`%s`? Did you mean `%s`?", bannedWord, command.getAuthor().getDisplayName(command.getGuild())));
                Logger.logCommand(command, "Banned word");
                return;
            }
        }

        arg = Utilities.getHeroAliasesMap().getOrDefault(arg, arg);

        for(int i = 0; i < heroes.length(); i++) {
            String name = heroes.getJSONObject(i).getString("name").toLowerCase();
            if(name.equals(arg.toLowerCase())) {
                heroObj = heroes.getJSONObject(i);
                break;
            }
        }

        if(heroObj == null) {
            for(int i = 0; i < heroesEnglish.length(); i++) {
                String name = heroesEnglish.getJSONObject(i).getString("name");
                if(name.equalsIgnoreCase(arg))
                    heroObj = heroes.getJSONObject(i);
            }
        }

        if(heroObj == null) {
            String didYouMean = Utilities.getSimilarHero(arg, language);
            if(language != Language.ENGLISH) {
                String didYouMeanEnglish = Utilities.getSimilarHero(arg, Language.ENGLISH);
                didYouMean = Utilities.getLevenshteinDistance(arg, didYouMean) <= Utilities.getLevenshteinDistance(arg, didYouMeanEnglish) ? didYouMean : didYouMeanEnglish;
            }
            String reply = didYouMean == null ? "Could not find hero: `" + arg + "`" : "Could not find hero: `" + arg + "`. Did you mean: `" + didYouMean + "`?";
            command.replyWith(reply);
            Logger.logCommand(command, language, "Illegal argument");
            return;
        }

        int heroColor = Utilities.getHeroColor(heroObj.getString("class").toLowerCase());

        List<String> perksList = new ArrayList<>();
        List<String> skillName = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            skillName.add(heroObj.getJSONArray("skills").getJSONObject(i).getString("name"));
            for(int j = 0; j < 2; j++) {
                perksList.add(heroObj.getJSONArray("skills").getJSONObject(i).getJSONArray("perks").get(j).toString());
            }
        }

        EmbedBuilder embed = new EmbedBuilder();
        for(int i = 0; i < 4; i++) {
            String name = skillName.get(i);
            String perks = perksList.get((i*2)) + "\n" +
                    perksList.get((i*2)+1) + "\n";
            embed.appendField(name, perks, false);
        }
        String t5perks = heroObj.getJSONArray("perks").get(0).toString() + "\n" +
                heroObj.getJSONArray("perks").get(1).toString() + "\n";
        embed.appendField("T5 Perks", t5perks, false);
        embed.withColor(heroColor);

        embed.withThumbnail(heroObj.getString("thumbnail"));
        embed.withTitle(heroObj.getString("name") + ", " + heroObj.getString("title"));
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroObj.getString("name"));
        embed.withFooterText("Last update: " + Utilities.getLastUpdate());

        command.replyWith(embed.build());
        Logger.logCommand(command, language);
    }
}
