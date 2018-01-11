package com.raepheles.discord.cleobot;

import com.discordbolt.api.command.CommandManager;
import com.raepheles.discord.cleobot.events.MyReadyEvent;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Rae on 19/12/2017.
 * Starting point for the bot.
 */
public class CleoBot {

    public static void main(String[] args) {
        String token = null;
        String prefix = null;
        long privateChannelListener = -1;
        long feedbackChannel = -1;
        long loggerChannel = -1;
        int raidFinderTimeOut = 300;
        Properties prop = new Properties();

        try(InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            token = prop.getProperty("token");
            prefix = prop.getProperty("prefix");
            privateChannelListener = Long.parseLong(prop.getProperty("private_channel_listener"));
            loggerChannel = Long.parseLong(prop.getProperty("logger_channel"));
            feedbackChannel = Long.parseLong(prop.getProperty("feedback_channel"));
            raidFinderTimeOut = Integer.parseInt(prop.getProperty("raid_finder_timeout"));
            Utilities.setWhitelistStatus(prop.getProperty("whitelist").equalsIgnoreCase("true"));
        } catch(IOException e) {
            System.out.println("Error reading \"config.properties\" file!");
            System.exit(1);
        }
        if(token == null) {
            System.out.println("Could not get token from config file.");
            System.exit(2);
        } else if(prefix == null) {
            System.out.println("Could not get prefix from config file.");
            System.exit(2);
        }
        Utilities.setLoggerChannelId(loggerChannel);
        Utilities.setFeedbackChannelId(feedbackChannel);
        Utilities.setDefaultPrefix(prefix);
        Utilities.setRaidFinderTimeOut(raidFinderTimeOut);

        IDiscordClient client = new ClientBuilder().withToken(token).build();
        CommandManager manager = new CommandManager(client, "com.raepheles.discord.cleobot", prefix);
        client.getDispatcher().registerListener(new MyReadyEvent(manager, privateChannelListener));

        try {
            client.login();
        } catch(DiscordException de) {
            de.printStackTrace();
            System.exit(3);
        }

    }
}
