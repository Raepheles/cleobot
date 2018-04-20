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

/**
 * Created by Rae on 19/12/2017.
 * Command for basic hero info.
 */
@SuppressWarnings("unused")
public class HeroCommand {

    @BotCommand(command = "hero",
            description = "Get basic data on hero.",
            usage = "hero *hero_name*",
            module = "Heroes",
            allowPM = true)
    public static void heroCommand(CommandContext command) {
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

        //Getting main stats
        int hp = heroObj.getJSONObject("main stats").getInt("hp");
        int atk = heroObj.getJSONObject("main stats").getInt("atk");
        int pdef = heroObj.getJSONObject("main stats").getInt("pdef");
        int mdef = heroObj.getJSONObject("main stats").getInt("mdef");
        String mainStats = "HP: " + hp + "\n" +
                "ATK: " + atk + "\n" +
                "P.DEF: " + pdef + "\n" +
                "M.DEF: " + mdef + "\n";
        //Getting additional stats
        String additionalStats = "";
        int mppa = heroObj.getJSONObject("additional stats").getInt("mp/atk");
        int crit = heroObj.getJSONObject("additional stats").getInt("crit");
        int cdmg = heroObj.getJSONObject("additional stats").getInt("cdmg");
        int pen = heroObj.getJSONObject("additional stats").getInt("penetration");
        int acc = heroObj.getJSONObject("additional stats").getInt("accuracy");
        int pdodge = heroObj.getJSONObject("additional stats").getInt("p.dodge");
        int mdodge = heroObj.getJSONObject("additional stats").getInt("m.dodge");
        int pblock = heroObj.getJSONObject("additional stats").getInt("p.block");
        int mblock = heroObj.getJSONObject("additional stats").getInt("m.block");
        int ptough = heroObj.getJSONObject("additional stats").getInt("p.tough");
        int mtough = heroObj.getJSONObject("additional stats").getInt("m.tough");
        int mblockdef = heroObj.getJSONObject("additional stats").getInt("m.block def");
        int ccres = heroObj.getJSONObject("additional stats").getInt("cc resist");
        additionalStats += "Mp/Atk: " + mppa + "\n";
        if(crit != 0)
            additionalStats += "Crit: " + crit + "\n";
        if(cdmg != 0)
            additionalStats += "Crit DMG: " + cdmg + "\n";
        if(pen != 0)
            additionalStats += "Penetration: " + pen + "\n";
        if(acc != 0)
            additionalStats += "Accuracy: " + acc + "\n";
        if(pdodge != 0)
            additionalStats += "P.Dodge: " + pdodge + "\n";
        if(mdodge != 0)
            additionalStats += "M.Dodge: " + mdodge + "\n";
        if(pblock != 0)
            additionalStats += "P.Block: " + pblock + "\n";
        if(mblock != 0)
            additionalStats += "M.Block: " + mblock + "\n";
        if(ptough != 0)
            additionalStats += "P.Tough: " + ptough + "\n";
        if(mtough != 0)
            additionalStats += "M.Tough: " + mtough + "\n";
        if(mblockdef != 0)
            additionalStats += "M.Block DEF: " + mblockdef + "\n";
        if(ccres != 0)
            additionalStats += "CC Resist: " + ccres + "\n";

        EmbedBuilder embed = new EmbedBuilder();
        String heroName = heroObj.getString("name");
        String heroType = heroObj.getString("type");
        String heroClass = heroObj.getString("class");
        String thumbnail = heroObj.getString("thumbnail");
        String heroTitle = heroObj.getString("title");
        String heroPosition = heroObj.getString("position");

        embed.withThumbnail(thumbnail);
        embed.withTitle(heroName + ", " + heroTitle);
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroName);
        embed.appendField("Class", heroClass, true);
        embed.appendField("Type / Position", heroType + " / " + heroPosition, true);
        embed.appendField("Main Stats", mainStats, true);
        embed.appendField("Additional Stats", additionalStats, true);
        String heroInfo = "Skills: `" + command.getPrefix() + "skills " + heroName + "`\n" +
                "Skill Attributes: `" + command.getPrefix() + "attributes " + heroName + "`\n" +
                "Transcendence Perks: `" + command.getPrefix() + "perks " + heroName + "`\n" +
                "Unique Weapon: `" + command.getPrefix() + "uw " + heroName + "`\n";
        embed.appendField("Additional Info", heroInfo, false);
        embed.withColor(heroColor);
        embed.withFooterText("Last update: " + Utilities.getLastUpdate());

        command.replyWith(embed.build());
        Logger.logCommand(command, language);
    }
}
