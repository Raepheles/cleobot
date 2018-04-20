package com.raepheles.discord.cleobot.modules.administration;

import com.discordbolt.api.command.BotCommand;
import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IIDLinkedObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by Rae on 26/12/2017.
 * Command for sending broadcast message to bot channel in every server.
 */
@SuppressWarnings("unused")
public class BroadcastCommand {

    private static class Owner {
        private final long id;
        private final long guildId;
        private final String name;
        private final String guildName;

        private Owner(String name, long id, String guildName, long guildId) {
            this.name = name;
            this.id = id;
            this.guildName = guildName;
            this.guildId = guildId;
        }

        public long getId() {
            return id;
        }

        public long getGuildId() {
            return guildId;
        }

        public String getName() {
            return name;
        }

        public String getGuildName() {
            return guildName;
        }
    }

    @BotCommand(command = "broadcast",
            description = "Send broadcast message to all servers.",
            usage = "broadcast *message*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void broadcastCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            Logger.logCommand(command, "NOT ADMIN");
            return;
        }
        if(command.getArgCount() < 2) {
            command.sendUsage();
            return;
        }
        StringJoiner msg = new StringJoiner(" ");
        for(int i = 1; i < command.getArguments().size(); i++) {
            msg.add(command.getArgument(i));
        }
        // Flush the list first.
        Utilities.flushGuilds(command.getClient().getGuilds().stream().map(IIDLinkedObject::getLongID).collect(Collectors.toList()));
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        int success = 0;
        int fail = 0;
        List<Long> fails = new ArrayList<>();
        for(int i = 0; i < guilds.length(); i++) {
            long guildId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            long botchannel = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.botchannel"))).longValue();
            if(botchannel != -1) {
                if(Utilities.hasPerms(command.getClient().getChannelByID(botchannel), command.getClient().getOurUser())) {
                    Utilities.sendMessage(command.getClient().getChannelByID(botchannel), msg.toString());
                    success++;
                } else {
                    fails.add(guildId);
                    fail++;
                }
            } else {
                fails.add(guildId);
                fail++;
            }
        }

        String result = "Broadcast message has been sent to " + success + " guilds. Failed to sent " + fail + " guilds.\n" +
                "Fail list:\n";
        if(fails.size() > 0)
            result += "```";
        for(long failed: fails) {
            String guildName = command.getClient().getGuildByID(failed).getName();
            result += String.format("%-50s - %s\n", guildName.length() > 50 ? guildName.substring(0, 46) + "..." : guildName, failed);
        }
        if(fails.size() > 0)
            result += "```";
        command.replyWith(result);
    }

    @BotCommand(command = "broadcastowners",
            aliases = "bo",
            description = "Send broadcast message to all server owners.",
            usage = "broadcastowners *message*",
            module = "Administration",
            allowPM = true,
            secret = true)
    public static void broadcastOwnersCommand(CommandContext command) {
        if(!Utilities.isAdmin(command.getAuthor())) {
            command.replyWith(Utilities.getProperty("administration.notAdmin"));
            Logger.logCommand(command, "NOT ADMIN");
            return;
        }
        if(command.getArgCount() < 2) {
            command.sendUsage();
            return;
        }
        StringJoiner msg = new StringJoiner(" ");
        for(int i = 1; i < command.getArguments().size(); i++) {
            msg.add(command.getArgument(i));
        }
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            command.replyWith(String.format(Utilities.getProperty("misc.fileReadError"), "guilds"));
            return;
        }

        int count = 0;
        List<Long> owners = new ArrayList<>();
        List<Owner> ownersList = new ArrayList<>();
        for(int i = 0; i < guilds.length(); i++) {
            long guildId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            IGuild guild = command.getClient().getGuildByID(guildId);
            if(guild == null)
                continue;
            if(!owners.contains(guild.getOwnerLongID())) {
                Utilities.sendMessage(guild.getOwner().getOrCreatePMChannel(), msg.toString());
                count++;
                owners.add(guild.getOwnerLongID());
            }
            ownersList.add(new Owner(guild.getOwner().getName(),
                    guild.getOwnerLongID(),
                    guild.getName(),
                    guild.getLongID()));
        }

        List<String> lines = new ArrayList<>();
        for(Owner owner: ownersList) {
            lines.add(String.format("Owner Name: %s\tOwner ID: %d\tGuild Name: %s\tGuild ID:%d",
                    owner.getName(),
                    owner.getId(),
                    owner.getName(),
                    owner.getGuildId()));
        }

        boolean fileEdited = false;
        Path file = Paths.get("send-file.txt");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
            fileEdited = true;
        } catch(IOException e) {
            e.printStackTrace();
        }

        command.replyWith("Broadcast message has been sent to " + count + " owners.");
        if(fileEdited) {
            Utilities.sendFile(command.getChannel(), file.toFile());
        }

    }
}
