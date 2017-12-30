package com.raepheles.discord.cleobot.modules.heroes;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.util.EmbedBuilder;

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
            Logger.logCommand(command, "Bot channel not set");
            return;
        }
        if(command.getArgCount() <= 1) {
            command.sendUsage();
            Logger.logCommand(command, "Arg count");
            return;
        }
        String arg = String.join(" ", command.getArguments());
        arg = arg.substring(arg.indexOf(" ")+1, arg.length());
        JSONArray heroes = Utilities.getHeroesArray();
        JSONObject heroObj = null;
        for(int i = 0; i < heroes.length(); i++) {
            String name = heroes.getJSONObject(i).getString("name").toLowerCase();
            if(name.equals(arg.toLowerCase())) {
                heroObj = heroes.getJSONObject(i);
                break;
            }
        }
        if(heroObj == null) {
            String didYouMean = Utilities.getSimilarHero(arg);
            String reply = didYouMean == null ? "Could not found hero: `" + arg + "`" : "Could not found hero: `" + arg + "`. Did you mean: `" + didYouMean + "`?";
            command.replyWith(reply);
            Logger.logCommand(command, "Illegal argument");
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
        embed.appendField("Type / Poisiton", heroType + " / " + heroPosition, true);
        embed.appendField("Main Stats", mainStats, true);
        embed.appendField("Additional Stats", additionalStats, true);
        String heroInfo = "Skills: `" + command.getPrefix() + "skills " + heroName + "`\n" +
                "Skill Attributes: `" + command.getPrefix() + "attributes " + heroName + "`\n" +
                "Transcendence Perks: `" + command.getPrefix() + "perks " + heroName + "`\n" +
                "Unique Weapon: `" + command.getPrefix() + "uw " + heroName + "`\n";
        embed.appendField("Additional Info", heroInfo, false);
        embed.withColor(heroColor);
        command.replyWith(embed.build());
        Logger.logCommand(command);
    }
}
