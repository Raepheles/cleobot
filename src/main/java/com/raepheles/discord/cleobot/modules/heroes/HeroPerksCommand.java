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

        List<String> perksList = new ArrayList<>();
        List<String> skillName = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            skillName.add(heroObj.getJSONArray("skills").getJSONObject(i).getString("name"));
            for(int j = 0; j < 3; j++) {
                perksList.add(heroObj.getJSONArray("skills").getJSONObject(i).getJSONArray("perks").get(j).toString());
            }
        }

        EmbedBuilder embed = new EmbedBuilder();
        for(int i = 0; i < 4; i++) {
            String name = skillName.get(i);
            String tier = perksList.get(i*3);
            String perks = "Light: " + perksList.get((i*3)+1) + "\n" +
                    "Dark: " + perksList.get((i*3)+2) + "\n";
            embed.appendField(name + "(Tier: " + tier + ")", perks, false);
        }
        String t5perks = "Light: " + heroObj.getJSONArray("perks").get(0).toString() + "\n" +
                "Dark: " + heroObj.getJSONArray("perks").get(1).toString() + "\n";
        embed.appendField("T5 Perks", t5perks, false);
        embed.withColor(heroColor);

        embed.withThumbnail(heroObj.getString("thumbnail"));
        embed.withTitle(heroObj.getString("name") + ", " + heroObj.getString("title"));
        embed.withUrl("http://www.kingsraid.wiki/index.php?title=" + heroObj.getString("name"));

        command.replyWith(embed.build());
        Logger.logCommand(command);
    }
}
