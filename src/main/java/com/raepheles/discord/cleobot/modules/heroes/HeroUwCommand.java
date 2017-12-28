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
 */
public class HeroUwCommand {

    @BotCommand(command = "uw",
            description = "Get unique weapon info of hero.",
            usage = "uw *hero_name*",
            module = "Heroes",
            allowPM = true)
    public static void heroUwCommand(CommandContext command) {
        // Check if bot channel still exists and bot has permissions on it
        if(!Utilities.checkBotChannel(command)) {
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
            String name = heroes.getJSONObject(i).getString("name");
            if(name.equalsIgnoreCase(arg)) {
                heroObj = heroes.getJSONObject(i);
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

        int effects = heroObj.getJSONObject("uw").getJSONArray("effects").length();

        EmbedBuilder embed = new EmbedBuilder();
        String name = heroObj.getJSONObject("uw").getString("name");
        String content = heroObj.getJSONObject("uw").getString("explanation");
        embed.appendField(name, content, false);
        for(int i = 0; i < effects; i++) {
            String effectValues = "";
            for(int j = 0; j < 6; j++)
                effectValues = String.join(",", heroObj.getJSONObject("uw").getJSONArray("effects").getJSONArray(i).get(j).toString());
            embed.appendField("{" + i + "}", effectValues, false);
        }
        embed.withThumbnail(heroObj.getJSONObject("uw").getString("thumbnail"));
        embed.withTitle(heroObj.getString("name") + ", " + heroObj.getString("title"));
        embed.withColor(heroColor);
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroObj.getString("name"));

        command.replyWith(embed.build());
        Logger.logCommand(command);
    }
}
