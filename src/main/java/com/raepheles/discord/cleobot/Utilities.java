package com.raepheles.discord.cleobot;

import com.discordbolt.api.command.CommandContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

/**
 * Created by Rae on 19/12/2017.
 */
public class Utilities {
    private static JSONArray heroesArray = null;
    private static long feedbackChannelId;
    private static long loggerChannelId;
    private static long ownerId;
    private static boolean feedbackActive = true;
    private static boolean whitelistStatus = false;
    private static Properties properties = null;
    private static String defaultPrefix;
    public final static String CHANGELOG_URL = "https://gist.github.com/Raepheles/51f7623b7e87c5b4968e676590ee71f5";
    public final static String PLUG_CAFE_BASE_URL = "https://www.plug.game/kingsraid/1030449";
    public final static String PLUG_CAFE_NOTICES = "https://www.plug.game/kingsraid/1030449/posts?menuId=1#";
    public final static String PLUG_CAFE_EVENTS = "https://www.plug.game/kingsraid/1030449/posts?menuId=2#";
    public final static String PLUG_CAFE_PATCH_NOTES = "https://www.plug.game/kingsraid/1030449/posts?menuId=9#";
    public final static String PLUG_CAFE_GREEN_NOTES = "https://www.plug.game/kingsraid/1030449/posts?menuId=12#";

    public static String getSimilarClass(String className) {
        JSONArray heroes = readJsonFromFile(getProperty("files.heroes"));
        List<String> classes = new ArrayList<>();
        for(int i = 0; i < heroes.length(); i++) {
            String tempClass = heroes.getJSONObject(i).getString("class");
            if(!classes.contains(tempClass)) {
                classes.add(tempClass);
            }
        }
        String similarClassName = classes.get(0);
        int distance = getLevenshteinDistance(className, similarClassName);
        for(int i = 1; i < classes.size(); i++) {
            String tempClass = classes.get(i);
            int tempDistance = getLevenshteinDistance(className, tempClass);
            if(tempDistance < distance) {
                distance = tempDistance;
                similarClassName = tempClass;
            }
        }
        if(distance <= 3)
            return similarClassName;
        else
            return null;
    }

    public static String getSimilarHero(String heroName) {
        JSONArray heroes = readJsonFromFile(getProperty("files.heroes"));
        String similarHeroName = heroes.getJSONObject(0).getString("name");
        int distance = getLevenshteinDistance(heroName, similarHeroName);
        for(int i = 1; i < heroes.length(); i++) {
            String tempName = heroes.getJSONObject(i).getString("name");
            int tempDistance = getLevenshteinDistance(heroName, tempName);
            if(tempDistance < distance) {
                distance = tempDistance;
                similarHeroName = tempName;
            }
        }
        if(distance <= 3)
            return similarHeroName;
        else
            return null;
    }

    private static int getLevenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int lengthS1 = s1.length();
        int lengthS2 = s2.length();
        if(lengthS1 == 0)
            return lengthS2;
        if(lengthS2 == 0)
            return lengthS1;
        int[][] matrix = new int[lengthS1+1][lengthS2+1];
        // First column is 0 to S1
        for(int i = 0; i <= lengthS1; i++) {
            matrix[i][0] = i;
        }
        // First row is 0 to S2
        for(int i = 0; i <= lengthS2; i++) {
            matrix[0][i] = i;
        }

        for(int i = 1; i <= lengthS1; i++) {
            for(int j = 1; j <= lengthS2; j++) {
                int cost;
                if(s1.charAt(i-1) == s2.charAt(j-1)) {
                    cost = 0;
                } else {
                    cost = 1;
                }
                matrix[i][j] = Math.min(Math.min(matrix[i-1][j]+1, matrix[i][j-1]+1), matrix[i-1][j-1] + cost);
            }
        }
        return matrix[lengthS1][lengthS2];
    }

    public static boolean isAdmin(IUser user) {
        JSONArray adminList;
        try {
            adminList = readJsonFromFile(getProperty("files.administrators"));
        } catch(Exception e) {
            return false;
        }
        for(int i = 0; i < adminList.length(); i++) {
            long id = ((Number)adminList.getJSONObject(i).get("id")).longValue();
            if(id == user.getLongID())
                return true;
        }
        return false;
    }

    public static long getLoggerChannelId() {
        return loggerChannelId;
    }

    public static void setLoggerChannelId(long loggerChannelId) {
        Utilities.loggerChannelId = loggerChannelId;
    }

    public static String getDefaultPrefix() {
        return defaultPrefix;
    }

    public static void setDefaultPrefix(String defaultPrefix) {
        Utilities.defaultPrefix = defaultPrefix;
    }

    public static JSONArray getHeroesArray() {
        if(heroesArray == null)
            readHeroesArray();
        return heroesArray;
    }

    public static boolean hasPerms(IChannel channel, IUser user) {
        EnumSet<Permissions> perms = channel.getModifiedPermissions(user);
        return perms.contains(Permissions.READ_MESSAGES)
                && perms.contains(Permissions.EMBED_LINKS)
                && perms.contains(Permissions.SEND_MESSAGES);
    }

    public static int getHeroColor(String heroName) {
        /**
         * assassin: 7539556
         * knight: 934528
         * priest: 157551
         * warrior: 7617796
         * wizard: 7930893
         * archer: 3829273
         * mechanic: 333165
         */
        if(heroName.equals("assassin")) {
            return 7539556;
        } else if (heroName.equals("knight")) {
            return 934528;
        } else if (heroName.equals("priest")) {
            return 157551;
        } else if (heroName.equals("warrior")) {
            return 7617796;
        } else if (heroName.equals("wizard")) {
            return 7930893;
        } else if (heroName.equals("archer")) {
            return 3829273;
        } else if (heroName.equals("mechanic")) {
            return 333165;
        } else {
            return 0;
        }
    }

    public static JSONObject newGuildEntry(long guildId) {
        JSONObject newGuild = new JSONObject();
        newGuild.put("id", guildId);
        newGuild.put(getProperty("guilds.botchannel"), -1);
        newGuild.put(getProperty("guilds.hotTimeChannel"), -1);
        newGuild.put(getProperty("guilds.plugCafeChannel"), -1);
        newGuild.put(getProperty("guilds.newDayChannel"), -1);

        JSONObject statusObj = new JSONObject();
        statusObj.put("EUROPE", 0);
        statusObj.put("AMERICA", 0);
        statusObj.put("ASIA", 0);

        newGuild.put(getProperty("guilds.hotTimeStatus"), statusObj);
        newGuild.put(getProperty("guilds.newDayStatus"), statusObj);

        JSONArray emptyArray = new JSONArray();

        newGuild.put(getProperty("guilds.plugCafeFollowers"), emptyArray);

        JSONObject newDayHotTimeFollowers = new JSONObject();
        newDayHotTimeFollowers.put("EUROPE", emptyArray);
        newDayHotTimeFollowers.put("AMERICA", emptyArray);
        newDayHotTimeFollowers.put("ASIA", emptyArray);

        newGuild.put(properties.getProperty("guilds.hotTimeFollowers"), newDayHotTimeFollowers);
        newGuild.put(properties.getProperty("guilds.newDayFollowers"), newDayHotTimeFollowers);

        return newGuild;
    }

    public static void sendMessage(IChannel channel, EmbedObject embed) {
        RequestBuffer.request( () -> {
            try {
                channel.sendMessage(embed);
            } catch(DiscordException de) {
                de.printStackTrace();
                throw de;
            }
        }).get();
    }

    public static void sendMessage(IChannel channel, String message) {
        RequestBuffer.request( () -> {
            try {
                channel.sendMessage(message);
            } catch(DiscordException de) {
                de.printStackTrace();
                throw de;
            }
        }).get();
    }

    public static void sendMessage(IChannel channel, EmbedObject embed, String message) {
        RequestBuffer.request( () -> {
            try {
                channel.sendMessage(message, embed);
            } catch(DiscordException de) {
                de.printStackTrace();
                throw de;
            }
        }).get();
    }

    public static void leaveGuild(IGuild guild) {
        RequestBuffer.request( () -> {
            try {
                guild.leave();
            } catch(DiscordException de) {
                de.printStackTrace();
            }
        }).get();
    }

    public static boolean checkBotChannel(CommandContext command) {
        long guildId = command.getGuild().getLongID();
        IDiscordClient client = command.getClient();
        JSONArray guilds = readJsonFromFile("guilds.json");
        long botChannel = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long current = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(current == guildId) {
                botChannel = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.botchannel"))).longValue();
                break;
            }
        }
        // Bot channel doesn't exist
        if(botChannel == -1) {
            command.replyWith(String.format(getProperty("botchannel.notExists"), command.getPrefix() + getProperty("botchannel.setCommand")));
            return false;
        }
        // Bot channel was set but then deleted
        if(client.getChannelByID(botChannel) == null) {
            command.replyWith(String.format(getProperty("botchannel.deleted"), command.getPrefix() + getProperty("botchannel.setCommand")));
            return false;
        }
        // Bot doesn't have required perms on bot channel
        if(!hasPerms(client.getChannelByID(botChannel), client.getOurUser())) {
            command.replyWith(String.format(getProperty("botchannel.noPerms"), client.getChannelByID(botChannel).mention()));
            return false;
        }
        return true;
    }

    public static String getProperty(String property) {
        if(properties == null) {
            properties = new Properties();
            try {
                properties.load(Utilities.class.getResourceAsStream("/project.properties"));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return properties.getProperty(property);
    }

    public static boolean getWhitelistStatus() {
        return whitelistStatus;
    }

    public static void setWhitelistStatus(boolean whitelistStatus) {
        Utilities.whitelistStatus = whitelistStatus;
    }

    public static void setOwnerId(long id) {
        ownerId = id;
    }

    public static long getOwnerId() {
        return ownerId;
    }

    public static long getFeedbackChannelId() {
        return feedbackChannelId;
    }

    public static boolean isFeedbackActive() {
        return feedbackActive;
    }

    public static void setFeedbackActive(boolean feedbackActive) {
        Utilities.feedbackActive = feedbackActive;
    }

    public static void setFeedbackChannelId(long id) {
        feedbackChannelId = id;
    }

    public static JSONArray readJsonFromFile(String file) {
        String data;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null) {
                sb.append(line);
                line = br.readLine();
            }
            data = sb.toString();
            JSONArray array = new JSONArray(data);
            return array;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeToJsonFile(JSONArray array, String path) {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(array.toString());
            fw.flush();
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void readHeroesArray() {
        heroesArray = readJsonFromFile(getProperty("files.heroes"));
    }
}
