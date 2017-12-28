package com.raepheles.discord.cleobot.modules.heroes;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rae on 19/12/2017.
 */
public class HeroAttributesCommand {

    @BotCommand(command = "attributes",
            aliases = "attr",
            description = "Get skill attribute info of hero.",
            usage = "attribute *hero_name*",
            module = "Heroes",
            allowPM = true)
    public static void heroAttributesCommand(CommandContext command) {
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

        List<String> attributeExplanation = new ArrayList<>();
        List<String> skillName = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            skillName.add(heroObj.getJSONArray("skills").getJSONObject(i).getString("name"));
            for(int j = 0; j < 3; j++) {
                attributeExplanation.add(heroObj.getJSONArray("skills").getJSONObject(i).getJSONArray("attributes").getString(j));
            }
        }

        EmbedBuilder embed = new EmbedBuilder();
        for(int i = 0; i < 4; i++) {
            String name = skillName.get(i);
            StringBuilder attributes = new StringBuilder();
            attributes.append(attributeExplanation.get(i*3) + "\n");
            attributes.append(attributeExplanation.get((i*3)+1) + "\n");
            attributes.append(attributeExplanation.get((i*3)+2) + "\n");
            embed.appendField(name, attributes.toString(), false);
        }
        embed.withThumbnail(heroObj.getString("thumbnail"));
        embed.withTitle(heroObj.getString("name") + ", " + heroObj.getString("title"));
        embed.withColor(heroColor);
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroObj.getString("name"));

        command.replyWith(embed.build());
        Logger.logCommand(command);
    }
}
