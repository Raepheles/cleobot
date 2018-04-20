package com.raepheles.discord.cleobot;

import com.discordbolt.api.command.CommandContext;
import com.raepheles.discord.cleobot.events.AutoDeleter;
import com.raepheles.discord.cleobot.events.ChannelListener;
import com.raepheles.discord.cleobot.logger.Logger;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.*;

/**
 * Created by Rae on 19/12/2017.
 * Utility methods.
 */
public class Utilities {
    private static JSONArray heroesArray = null;
    private static JSONArray heroesArrayFrench = null;
    private static JSONArray heroesArrayGerman = null;
    private static JSONArray heroesArrayJapanese = null;
    private static JSONArray heroesArrayKorean = null;
    private static JSONArray heroesArrayPortuguese = null;
    private static JSONArray heroesArrayRussian = null;
    private static JSONArray heroesArraySpanish = null;
    private static JSONArray heroesArrayThai = null;
    private static JSONArray heroesArrayVietnamese = null;
    private static long feedbackChannelId;
    private static long loggerChannelId;
    private static long privateChannelListenerId;
    private static int raidFinderTimeOut;
    private static boolean feedbackActive = true;
    private static boolean whitelistStatus = false;
    private static Properties properties = null;
    private static String defaultPrefix;
    private static String lastUpdate = "";
    private static List<RaidFinderEntry> raidFinderEntries = new ArrayList<>();
    private static List<String> bannedWords = null;
    private static Map<String, String> heroAliases = null;
    private static ChannelListener channelListener = null;
    private static AutoDeleter autoDeleter = new AutoDeleter();
    public final static String CHANGELOG_URL = "https://gist.github.com/Raepheles/51f7623b7e87c5b4968e676590ee71f5";
    public final static String PLUG_CAFE_BASE_URL = "https://www.plug.game/kingsraid/1030449";
    public final static String PLUG_CAFE_NOTICES = "https://www.plug.game/kingsraid/1030449/posts?menuId=1#";
    public final static String PLUG_CAFE_EVENTS = "https://www.plug.game/kingsraid/1030449/posts?menuId=2#";
    public final static String PLUG_CAFE_PATCH_NOTES = "https://www.plug.game/kingsraid/1030449/posts?menuId=9#";
    public final static String PLUG_CAFE_GREEN_NOTES = "https://www.plug.game/kingsraid/1030449/posts?menuId=12#";

    public static List<String> getBannedWords() {
        if(bannedWords == null) {
            try {
                bannedWords = Files.readAllLines(Paths.get(getProperty("files.bannedWords")));
            } catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return bannedWords;
    }

    public static AutoDeleter getAutoDeleter() {
        return autoDeleter;
    }

    public static void setChannelListener(ChannelListener channelListener) {
        Utilities.channelListener = channelListener;
    }

    public static ChannelListener getChannelListener() {
        return channelListener;
    }

    public static void setPrivateChannelListenerId(long privateChannelListenerId) {
        Utilities.privateChannelListenerId = privateChannelListenerId;
    }

    public static long getPrivateChannelListenerId() {
        return privateChannelListenerId;
    }

    public static boolean flushGuilds(List<Long> guildIds) {
        JSONArray guilds = readJsonFromFile(getProperty("files.guilds"));
        if(guilds == null)
            return false;
        for(int i = 0; i < guilds.length(); i++) {
            if(!guildIds.contains( ((Number)guilds.getJSONObject(i).get("id")).longValue() )) {
                guilds.remove(i);
                i--;
            }
        }
        writeToJsonFile(guilds, getProperty("files.guilds"));
        return true;
    }

    public static int getRaidFinderTimeOut() {
        return raidFinderTimeOut;
    }

    public static void setRaidFinderTimeOut(int raidFinderTimeOut) {
        Utilities.raidFinderTimeOut = raidFinderTimeOut;
    }

    public static void addRaidFinderEntry(RaidFinderEntry entry) {
        raidFinderEntries.add(entry);
    }

    public static List<RaidFinderEntry> getRaidFinderEntries() {
        // Remove old logs
        raidFinderEntries.removeIf(entry -> entry.getTime() > raidFinderTimeOut);
        // Copy the list and reverse it
        List<RaidFinderEntry> reversedList = new ArrayList<>(raidFinderEntries);
        Collections.reverse(reversedList);
        return reversedList;
    }

    public static boolean isBanned(CommandContext command, String bannedFrom) {
        JSONArray banList = readJsonFromFile(getProperty("files.banlist"));
        if(banList != null) {
            for(int i = 0; i < banList.length(); i++) {
                long userId = ((Number)banList.getJSONObject(i).get("id")).longValue();
                // If user is banned for something check the ban condition
                if(userId == command.getAuthor().getLongID()) {
                    String tempBannedFrom = banList.getJSONObject(i).getString("banned from");
                    String banReason = banList.getJSONObject(i).getString("reason");
                    long bannedAt = ((Number)banList.getJSONObject(i).get("banned at")).longValue();
                    long bannedFor = ((Number)banList.getJSONObject(i).get("banned for")).longValue();
                    // If user is banned from what we are looking for check the ban condition
                    // Else return false
                    if(tempBannedFrom.equalsIgnoreCase(bannedFrom)) {
                        // PERM BAN
                        if(bannedFor == -1) {
                            command.replyWith(String.format(getProperty("banlist.bannedFrom"), bannedFrom, "for PERMENANTLY", banReason));
                            return true;
                        }
                        // If temp ban is not over return true
                        // Else delete ban from list return false
                        if(ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond() < bannedFor + bannedAt) {
                            String date = LocalDateTime.ofEpochSecond(bannedAt + bannedFor, 0, ZoneOffset.UTC).toString();
                            date = date.replaceAll("-", "/").replace("T", " - ");
                            date += " UTC";
                            command.replyWith(String.format(getProperty("banlist.bannedFrom"), bannedFrom, "until " + date, banReason));
                            return true;
                        } else {
                            banList.remove(i);
                            writeToJsonFile(banList, getProperty("files.banlist"));
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        // If user is not at ban list or ban list cannot be read return false
        return false;
    }

    public static String getSimilarClass(String className) {
        JSONArray heroes = getHeroesArray(Language.ENGLISH);
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
        return getSimilarHero(heroName, Language.ENGLISH);
    }

    public static String getSimilarHero(String heroName, Language language) {
        JSONArray heroes = getHeroesArray(language);
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

    public static int getLevenshteinDistance(String s1, String s2) {
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
        JSONArray adminList = readJsonFromFile(getProperty("files.administrators"));
        if(adminList == null)
            return false;
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
        return getHeroesArray(Language.ENGLISH);
    }

    public static JSONArray getHeroesArray(Language language) {
        switch(language) {
            case FRENCH:
                if(heroesArrayFrench == null)
                    heroesArrayFrench = readJsonFromFile(getProperty("files.heroes_french"));
                return heroesArrayFrench;
            case GERMAN:
                if(heroesArrayGerman == null)
                    heroesArrayGerman = readJsonFromFile(getProperty("files.heroes_german"));
                return heroesArrayGerman;
            case THAI:
                if(heroesArrayThai == null)
                    heroesArrayThai = readJsonFromFile(getProperty("files.heroes_thai"));
                return heroesArrayThai;
            case KOREAN:
                if(heroesArrayKorean == null)
                    heroesArrayKorean = readJsonFromFile(getProperty("files.heroes_korean"));
                return heroesArrayKorean;
            case RUSSIAN:
                if(heroesArrayRussian == null)
                    heroesArrayRussian = readJsonFromFile(getProperty("files.heroes_russian"));
                return heroesArrayRussian;
            case SPANISH:
                if(heroesArraySpanish == null)
                    heroesArraySpanish = readJsonFromFile(getProperty("files.heroes_spanish"));
                return heroesArraySpanish;
            case JAPANESE:
                if(heroesArrayJapanese == null)
                    heroesArrayJapanese = readJsonFromFile(getProperty("files.heroes_japanese"));
                return heroesArrayJapanese;
            case PORTUGUESE:
                if(heroesArrayPortuguese == null)
                    heroesArrayPortuguese = readJsonFromFile(getProperty("files.heroes_portuguese"));
                return heroesArrayPortuguese;
            case VIETNAMESE:
                if(heroesArrayVietnamese == null)
                    heroesArrayVietnamese = readJsonFromFile(getProperty("files.heroes_vietnamese"));
                return heroesArrayVietnamese;
            default:
                if(heroesArray == null)
                    heroesArray = readJsonFromFile(getProperty("files.heroes"));
                return heroesArray;
        }
    }

    public static boolean hasPerms(IChannel channel, IUser user) {
        EnumSet<Permissions> perms = channel.getModifiedPermissions(user);
        return perms.contains(Permissions.READ_MESSAGES)
                && perms.contains(Permissions.EMBED_LINKS)
                && perms.contains(Permissions.SEND_MESSAGES);
    }

    public static Language getLanguageForCode(String code) {
        switch(code) {
            case "en":
                return Language.ENGLISH;
            case "fr":
                return Language.FRENCH;
            case "de":
                return Language.GERMAN;
            case "jp":
                return Language.JAPANESE;
            case "ru":
                return Language.RUSSIAN;
            case "pt":
                return Language.PORTUGUESE;
            case "kr":
                return Language.KOREAN;
            case "es":
                return Language.SPANISH;
            case "vn":
                return Language.VIETNAMESE;
            case "th":
                return Language.THAI;
            default:
                return Language.ENGLISH;
        }
    }

    public static int getHeroColor(String heroName) {
        /*
         * assassin: 7539556
         * knight: 934528
         * priest: 157551
         * warrior: 7617796
         * wizard: 7930893
         * archer: 3829273
         * mechanic: 333165
         */
        switch (heroName) {
            case "assassin":
                return 7539556;
            case "knight":
                return 934528;
            case "priest":
                return 157551;
            case "warrior":
                return 7617796;
            case "wizard":
                return 7930893;
            case "archer":
                return 3829273;
            case "mechanic":
                return 333165;
            default:
                return 0;
        }
    }

    public static JSONObject newGuildEntry(long guildId) {
        System.out.println(String.format("[%s]New guild entry with id %d", Instant.now().atZone(ZoneId.of("UTC+3")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")), guildId));
        JSONObject newGuild = new JSONObject();
        newGuild.put("language", "en");
        newGuild.put("id", guildId);
        newGuild.put(getProperty("guilds.botchannel"), -1);
        newGuild.put(getProperty("guilds.hotTimeChannel"), -1);
        newGuild.put(getProperty("guilds.plugCafeChannel"), -1);
        newGuild.put(getProperty("guilds.newDayChannel"), -1);

        JSONObject statusObj = new JSONObject();
        statusObj.put("EUROPE", 0);
        statusObj.put("AMERICA", 0);
        statusObj.put("ASIA", 0);
        statusObj.put("JAPAN", 0);

        newGuild.put(getProperty("guilds.hotTimeStatus"), statusObj);
        newGuild.put(getProperty("guilds.newDayStatus"), statusObj);

        JSONArray emptyArray = new JSONArray();

        newGuild.put(getProperty("guilds.plugCafeFollowers"), emptyArray);

        JSONObject newDayHotTimeFollowers = new JSONObject();
        newDayHotTimeFollowers.put("EUROPE", emptyArray);
        newDayHotTimeFollowers.put("AMERICA", emptyArray);
        newDayHotTimeFollowers.put("ASIA", emptyArray);
        newDayHotTimeFollowers.put("JAPAN", emptyArray);

        newGuild.put(properties.getProperty("guilds.hotTimeFollowers"), newDayHotTimeFollowers);
        newGuild.put(properties.getProperty("guilds.newDayFollowers"), newDayHotTimeFollowers);
        newGuild.put("muted channels", new JSONArray());

        return newGuild;
    }

    public static void sendFile(IChannel channel, File file) {
        RequestBuffer.request( () -> {
           try {
               channel.sendFile(file);
           } catch(FileNotFoundException fnfe) {
               fnfe.printStackTrace();
           }
        }).get();
    }

    public static void sendFile(IChannel channel, String message, File file) {
        RequestBuffer.request( () -> {
            try {
                channel.sendFile(message, file);
            } catch(FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }).get();
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
        if(guilds == null)
            return false;

        // Check if channel is muted
        JSONArray mutedChannels = null;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(guildId == currentId) {
                mutedChannels = guilds.getJSONObject(i).getJSONArray("muted channels");
                break;
            }
        }

        if(mutedChannels == null)
            return false;

        for(int i = 0; i < mutedChannels.length(); i++) {
            long currentChannelId = ((Number)mutedChannels.get(i)).longValue();
            if(currentChannelId == command.getChannel().getLongID()) {
                sendMessage(command.getAuthor().getOrCreatePMChannel(),
                        String.format("Channel `%s` is muted at server `%s`.", command.getChannel().getName(), command.getGuild().getName()));
                return false;
            }
        }

        if(isBanned(command, "Bot")) {
            Logger.logCommand(command, "Banned from using bot.");
            return false;
        }

        long botChannel = -1;
        for(int i = 0; i < guilds.length(); i++) {
            long current = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(current == guildId) {
                botChannel = ((Number)guilds.getJSONObject(i).get(getProperty("guilds.botchannel"))).longValue();
                break;
            }
        }
        // Bot channel doesn't exist
        if(botChannel == -1) {
            command.replyWith(String.format(getProperty("botchannel.notExists"), command.getPrefix() + getProperty("botchannel.setCommand")));
            Logger.logCommand(command, "Bot channel doesn't exist");
            return false;
        }
        // Bot channel was set but then deleted
        if(client.getChannelByID(botChannel) == null) {
            command.replyWith(String.format(getProperty("botchannel.deleted"), command.getPrefix() + getProperty("botchannel.setCommand")));
            Logger.logCommand(command, "Bot channel is deleted");
            return false;
        }
        // Bot doesn't have required perms on bot channel
        if(!hasPerms(client.getChannelByID(botChannel), client.getOurUser())) {
            command.replyWith(String.format(getProperty("botchannel.noPerms"), client.getChannelByID(botChannel).mention()));
            Logger.logCommand(command, "Bot doesn't have perms");
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
            return new JSONArray(data);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeToJsonFile(JSONArray array, String path) {
        System.out.println(String.format("[%s]Writing to to file named: %s", Instant.now().atZone(ZoneId.of("UTC+3")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")), path));
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(array.toString());
            fw.flush();
            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void setLastUpdate(String lastUpdate) {
        Utilities.lastUpdate = lastUpdate;
    }

    public static String getLastUpdate() {
        return lastUpdate;
    }

    public static Map<String, String> getHeroAliasesMap() {
        if(heroAliases != null)
            return heroAliases;
        heroAliases = new HashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(getProperty("files.heroAliases")));
            for(String line: lines) {
                String[] parts = line.split(",", 2);
                heroAliases.put(parts[0], parts[1]);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return heroAliases;
    }
}
