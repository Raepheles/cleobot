package com.raepheles.discord.cleobot.modules.heroes;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Language;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rae on 19/12/2017.
 * Command for getting heroes matches with the searched class.
 */
@SuppressWarnings("unused")
public class ClassCommand {

    @BotCommand(command = "class",
            description = "List of heroes matches class name.",
            usage = "class *class_name*",
            module = "Heroes",
            allowPM = true)
    public static void classCommand(CommandContext command) {
        // Check if bot channel still exists and bot has permissions on it
        if(!command.isPrivateMessage() && !Utilities.checkBotChannel(command)) {
            return;
        }
        if(command.getArgCount() <= 1) {
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
        JSONArray array = Utilities.getHeroesArray(language);
        String heroClass = arg;
        List<String> heroes = new ArrayList<>();

        for(int i = 0; i < array.length(); i++) {
            String currentClass = array.getJSONObject(i).getString("class");
            if(currentClass.equalsIgnoreCase(arg)) {
                heroes.add(array.getJSONObject(i).getString("name"));
                heroClass = currentClass;
            }
        }
        if(heroes.isEmpty()) {
            String didYouMean = Utilities.getSimilarClass(arg);
            String reply = "Could not find any hero matches the class: `" + arg + "`.";
            reply += didYouMean == null ? "" : " Did you mean: `" + didYouMean + "`?";
            command.replyWith(reply);
            Logger.logCommand(command, "Illegal argument");
            return;
        }

        StringBuilder resultHeroes = new StringBuilder("List of heroes matches with the class: " + heroClass + "\n\n");
        for(String hero: heroes)
            resultHeroes.append("- ").append(hero).append("\n");

        command.replyWith(resultHeroes.toString());
        Logger.logCommand(command, language);
    }
}
