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
 * Command for getting hero skills.
 */
@SuppressWarnings("unused")
public class HeroSkillsCommand {

    @BotCommand(command = "skills",
            aliases = "skill",
            description = "Get skill info of hero.",
            usage = "skills *hero_name*",
            module = "Heroes",
            allowPM = true)
    public static void heroSkillCommand(CommandContext command) {
        // Check if bot channel still exists and bot has permissions on it
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
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

        List<String> skillName = new ArrayList<>();
        List<String> skillExplanation = new ArrayList<>();
        List<String> skillCost = new ArrayList<>();
        List<String> skillCooldown = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            skillName.add(heroObj.getJSONArray("skills").getJSONObject(i).getString("name"));
            skillExplanation.add(heroObj.getJSONArray("skills").getJSONObject(i).getString("explanation"));
            String cost = heroObj.getJSONArray("skills").getJSONObject(i).get("cost").toString();
            if(cost.equals("-1"))
                cost = "No Cost";
            skillCost.add(cost);
            String cooldown = heroObj.getJSONArray("skills").getJSONObject(i).get("cooldown").toString();
            if(cooldown.equals("-1"))
                cooldown = "No Cooldown";
            skillCooldown.add(cooldown);
        }
        EmbedBuilder embed = new EmbedBuilder();
        for(int i = 0; i < 4; i++) {
            String cost = skillCost.get(i);
            String name = skillName.get(i);
            String cd = skillCooldown.get(i);
            String content = skillExplanation.get(i);
            embed.appendField(name, "Cost: " + cost + "\nCooldown: " + cd + "\n" + content, false);
        }
        embed.withThumbnail(heroObj.getString("thumbnail"));
        embed.withTitle(heroObj.getString("name") + ", " + heroObj.getString("title"));
        embed.withColor(heroColor);
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroObj.getString("name"));

        command.replyWith(embed.build());
        Logger.logCommand(command);
    }
}
