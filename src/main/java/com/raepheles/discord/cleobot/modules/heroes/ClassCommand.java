package com.raepheles.discord.cleobot.modules.heroes;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rae on 19/12/2017.
 */
public class ClassCommand {

    @BotCommand(command = "class",
            description = "List of heroes matches class name.",
            usage = "class *class_name*",
            module = "Heroes",
            allowPM = true)
    public static void classCommand(CommandContext command) {
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
        String heroClass = arg;
        JSONArray array = Utilities.getHeroesArray();
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
            String reply = "Could not found any hero matches the class: `" + arg + "`.";
            reply += didYouMean == null ? "" : " Did you mean: `" + didYouMean + "`?";
            command.replyWith(reply);
            Logger.logCommand(command, "Illegal argument");
            return;
        }

        StringBuilder sb = new StringBuilder("List of heroes matches with the class: " + heroClass + "\n\n");
        for(String hero: heroes)
            sb.append("- " + hero + "\n");

        command.replyWith(sb.toString());
        Logger.logCommand(command);
    }
}
