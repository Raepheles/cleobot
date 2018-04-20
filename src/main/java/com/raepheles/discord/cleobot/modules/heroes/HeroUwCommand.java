package com.raepheles.discord.cleobot.modules.heroes;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Language;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Rae on 19/12/2017.
 * Command for getting unique weapon information of hero.
 */
@SuppressWarnings("unused")
public class HeroUwCommand {

    @BotCommand(command = "uw",
            description = "Get unique weapon info of hero.",
            usage = "uw *hero_name*",
            module = "Heroes",
            allowPM = true)
    public static void heroUwCommand(CommandContext command) {
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
            String name = heroes.getJSONObject(i).getString("name");
            if(name.equalsIgnoreCase(arg)) {
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

        int effects = heroObj.getJSONObject("uw").getJSONArray("effects").length();

        EmbedBuilder embed = new EmbedBuilder();
        String name = heroObj.getJSONObject("uw").getString("name");
        String content = heroObj.getJSONObject("uw").getString("explanation");
        embed.appendField(name, content, false);
        for(int i = 0; i < effects; i++) {
            StringJoiner effectValues = new StringJoiner(", ");
            for(int j = 0; j < 6; j++)
                effectValues.add(heroObj.getJSONObject("uw").getJSONArray("effects").getJSONArray(i).get(j).toString());
            embed.appendField("{" + i + "}", effectValues.toString(), false);
        }
        embed.withThumbnail(heroObj.getJSONObject("uw").getString("thumbnail"));
        embed.withTitle(heroObj.getString("name") + ", " + heroObj.getString("title"));
        embed.withColor(heroColor);
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroObj.getString("name"));
        embed.withFooterText("Last update: " + Utilities.getLastUpdate());

        command.replyWith(embed.build());
        Logger.logCommand(command, language);
    }
}
