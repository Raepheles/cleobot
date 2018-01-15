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
@SuppressWarnings("unused")
public class CleoBot {

    public static void main(String[] args) {
        String token = null;
        String prefix = null;
        long privateChannelListenerId = -1;
        long feedbackChannel = -1;
        long loggerChannel = -1;
        int raidFinderTimeOut = 300;
        Properties prop = new Properties();

        try(InputStream input = new FileInputStream("config.properties")) {
            // Load config file
            prop.load(input);
            // Get token and prefix. These are must have
            token = prop.getProperty("token");
            prefix = prop.getProperty("prefix");
            // Set private channel listener if it's not empty
            if(!prop.getProperty("private_channel_listener").isEmpty())
                privateChannelListenerId = Long.parseLong(prop.getProperty("private_channel_listener"));
            // Set logger channel if it's not empty
            if(!prop.getProperty("logger_channel").isEmpty())
                loggerChannel = Long.parseLong(prop.getProperty("logger_channel"));
            // Set feedback channel if it's not empty
            if(!prop.getProperty("feedback_channel").isEmpty())
                feedbackChannel = Long.parseLong(prop.getProperty("feedback_channel"));
            // Set raid finder timeout if it's not empty
            if(!prop.getProperty("raid_finder_timeout").isEmpty())
                raidFinderTimeOut = Integer.parseInt(prop.getProperty("raid_finder_timeout"));
            // Check if raid finder timeout is between 5 min and 1h 0m 0s (3600 seconds)
            if(raidFinderTimeOut < 300 || raidFinderTimeOut > 3600) {
                System.err.println("Raid finder time out must have a value between 300 and 3600!");
                System.exit(1);
            }
            // Get whitelist property and set its value in Utilities
            String whitelist = prop.getProperty("whitelist").toLowerCase();
            if(whitelist.isEmpty()) {
                System.err.println("Whitelist property cannot be empty!");
                System.exit(1);
            } else if(whitelist.equals("true")) {
                Utilities.setWhitelistStatus(true);
            } else if(whitelist.equals("false")) {
                Utilities.setWhitelistStatus(false);
            } else {
                System.err.println("Whitelist property can only take values of \"true\" or \"false\". Case insensitive.");
                System.exit(1);
            }
        } catch(IOException e) {
            System.err.println("Error reading \"config.properties\" file!");
            System.exit(1);
        } catch(NumberFormatException nfe) {
            System.err.println( String.format("%s is not valid.", nfe.getMessage().substring( nfe.getMessage().indexOf("\"") )) );
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
        Utilities.setPrivateChannelListenerId(privateChannelListenerId);

        IDiscordClient client = new ClientBuilder().withToken(token).build();
        CommandManager manager = new CommandManager(client, "com.raepheles.discord.cleobot", prefix);
        client.getDispatcher().registerListener(new MyReadyEvent());
        try {
            client.login();
        } catch(DiscordException de) {
            de.printStackTrace();
            System.exit(3);
        }

    }
}
