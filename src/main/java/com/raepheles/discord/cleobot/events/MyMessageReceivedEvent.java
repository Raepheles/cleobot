package com.raepheles.discord.cleobot.events;

import com.raepheles.discord.cleobot.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Created by Rae on 28/12/2017.
 * Bot's Message Received Event.
 * Only using to listen to bot's private channel.
 */
public class MyMessageReceivedEvent {

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent event) {
        long privateChannelListenerId = Utilities.getPrivateChannelListenerId();
        if(event.getChannel().isPrivate() && privateChannelListenerId != -1 && !event.getMessage().getContent().startsWith(Utilities.getDefaultPrefix())) {
            IChannel privateChannelLogsChannel = event.getClient().getChannelByID(privateChannelListenerId);
            if(privateChannelLogsChannel != null) {
                String reply = String.format("User `%s` with id `%d` have sent the following message to Cleo on private channel:\n%s",
                        event.getAuthor().getName(),
                        event.getAuthor().getLongID(),
                        event.getMessage().getContent());
                Utilities.sendMessage(privateChannelLogsChannel, reply);
            }
        } else if(!event.getChannel().isPrivate()
                && event.getMessage().getMentions().contains(event.getClient().getOurUser())
                && event.getMessage().getContent().contains("who loves you?")){
            String fileName = Utilities.getProperty("files.cleoLovers");
            JSONArray cleoLovers = Utilities.readJsonFromFile(fileName);
            long loverId = 0;
            boolean nobodyLovesCleo = true;
            if(cleoLovers == null)
                return;
            for(int i = 0; i < cleoLovers.length(); i++) {
                if(event.getGuild().getLongID() == ((Number)cleoLovers.getJSONObject(i).get("id")).longValue()) {
                    int size = cleoLovers.getJSONObject(i).getJSONArray("lovers").length();
                    if(size == 0)
                        break;
                    Random rand = new Random();
                    loverId = ((Number)cleoLovers.getJSONObject(i).getJSONArray("lovers").get(rand.nextInt(size))).longValue();
                    nobodyLovesCleo = false;
                    break;
                }
            }
            if(!nobodyLovesCleo) {
                Utilities.sendMessage(event.getChannel(), event.getClient().getUserByID(loverId).getDisplayName(event.getGuild()) + " :heart:");
            } else {
                Utilities.sendMessage(event.getChannel(), "No body :frowning:");
            }
        } else if(!event.getChannel().isPrivate()
                && event.getMessage().getMentions().contains(event.getClient().getOurUser())
                && event.getMessage().getContent().contains("I love you")) {
            String fileName = Utilities.getProperty("files.cleoLovers");
            JSONArray cleoLovers = Utilities.readJsonFromFile(fileName);
            boolean alreadyLoves = false;
            boolean guildFound = false;
            if(cleoLovers == null)
                return;
            for(int i = 0; i < cleoLovers.length(); i++) {
                if(event.getGuild().getLongID() == ((Number)cleoLovers.getJSONObject(i).get("id")).longValue()) {
                    guildFound = true;
                    for(int j = 0; j < cleoLovers.getJSONObject(i).getJSONArray("lovers").length(); j++) {
                        long currentLover = ((Number)cleoLovers.getJSONObject(i).getJSONArray("lovers").get(j)).longValue();
                        if(currentLover == event.getAuthor().getLongID()) {
                            alreadyLoves = true;
                            break;
                        }
                    }
                    if(!alreadyLoves) {
                        cleoLovers.getJSONObject(i).getJSONArray("lovers").put(event.getAuthor().getLongID());
                    }
                    break;
                }
            }
            if(!guildFound) {
                JSONObject obj = new JSONObject();
                obj.put("id", event.getGuild().getLongID());
                JSONArray arr = new JSONArray();
                arr.put(event.getAuthor().getLongID());
                obj.put("lovers", arr);
                cleoLovers.put(obj);
            }
            if(alreadyLoves) {
                Utilities.sendMessage(event.getChannel(), "Of course you do!");
            } else {
                Utilities.sendMessage(event.getChannel(), "You do?");
            }
            Utilities.writeToJsonFile(cleoLovers, fileName);
        } else if(!event.getChannel().isPrivate()
                && event.getMessage().getMentions().contains(event.getClient().getOurUser())
                && event.getMessage().getContent().contains("I don't love you anymore")) {
            String fileName = Utilities.getProperty("files.cleoLovers");
            JSONArray cleoLovers = Utilities.readJsonFromFile(fileName);
            boolean deleted = false;
            if(cleoLovers == null)
                return;
            for(int i = 0; i < cleoLovers.length(); i++) {
                if(event.getGuild().getLongID() == ((Number)cleoLovers.getJSONObject(i).get("id")).longValue()) {
                    for(int j = 0; j < cleoLovers.getJSONObject(i).getJSONArray("lovers").length(); j++) {
                        long currentLover = ((Number)cleoLovers.getJSONObject(i).getJSONArray("lovers").get(j)).longValue();
                        if(currentLover == event.getAuthor().getLongID()) {
                            cleoLovers.getJSONObject(i).getJSONArray("lovers").remove(j);
                            deleted = true;
                            break;
                        }
                    }
                    break;
                }
            }
            if(deleted) {
                Utilities.sendMessage(event.getChannel(), "But... why?");
            } else {
                Utilities.sendMessage(event.getChannel(), "You didn't love me to begin with, idiot!");
            }
            Utilities.writeToJsonFile(cleoLovers, fileName);
        } else if(!event.getChannel().isPrivate()
                && event.getMessage().getMentions().contains(event.getClient().getOurUser())) {
            List<String> lines;
            String answer = "";
            try {
                lines = Files.readAllLines(Paths.get("cleo talks.txt"));
                for(String line: lines) {
                    String[] parts = line.split("-", 2);
                    if(event.getMessage().getContent().toLowerCase().contains(parts[0].toLowerCase())) {
                        answer = parts[1];
                        break;
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            if(!answer.isEmpty())
                Utilities.sendMessage(event.getChannel(), answer);
        }
    }
}
