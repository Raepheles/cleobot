package com.raepheles.discord.cleobot.events;

import com.discordbolt.api.command.CommandManager;
import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rae on 19/12/2017.
 * Bot's ReadyEvent
 */
public class MyReadyEvent {
    private CommandManager manager;
    private long privateChannelListener;

    public MyReadyEvent(CommandManager manager, long privateChannelListener) {
        this.manager = manager;
        this.privateChannelListener = privateChannelListener;
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        List<IGuild> guilds = event.getClient().getGuilds();
        JSONArray guildsJson = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guildsJson == null) {
            System.err.println("Could not read " + Utilities.getProperty("files.guilds"));
            event.getClient().logout();
            // Return here is redundant since logout() method shuts the bot down. I only used it
            // to get rid of the warning.
            return;
        }
        JSONArray whitelist = Utilities.readJsonFromFile(Utilities.getProperty("files.whitelist"));
        if(whitelist == null) {
            System.err.println("Could not read " + Utilities.getProperty("files.whitelist"));
            event.getClient().logout();
            // Return here is redundant since logout() method shuts the bot down. I only used it
            // to get rid of the warning.
            return;
        }

        for(IGuild guild: guilds) {
            long guildId = guild.getLongID();
            boolean isSavedGuild = false;
            for(int i = 0; i < guildsJson.length(); i++) {
                long currentId = ((Number)guildsJson.getJSONObject(i).get("id")).longValue();
                if(currentId == guildId)
                    isSavedGuild = true;
            }

            if(Utilities.getWhitelistStatus()) {
                boolean whitelisted = false;
                for(int j = 0; j < whitelist.length(); j++) {
                    if( ((Number)whitelist.get(j)).longValue() == guildId )
                        whitelisted = true;
                }
                if(!whitelisted) {
                    Utilities.sendMessage(guild.getOwner().getOrCreatePMChannel(), Utilities.getProperty("join.whitelistFail"));
                    Utilities.leaveGuild(guild);
                    continue;
                }
            }

            if(!isSavedGuild) {
                JSONObject newGuild = Utilities.newGuildEntry(guildId);
                guildsJson.put(newGuild);
                Utilities.writeToJsonFile(guildsJson, Utilities.getProperty("files.guilds"));
                Utilities.sendMessage(guild.getOwner().getOrCreatePMChannel(), String.join(Utilities.getProperty("join.success"), manager.getCommandPrefix(guild)));
            }
        }

        event.getClient().getDispatcher().registerListeners(new MyGuildCreateEvent(), new MyMessageReceivedEvent(privateChannelListener));

        Runnable notificationCheck = () -> {
            sendNotification(event, Utilities.PLUG_CAFE_PATCH_NOTES, Utilities.getProperty("guilds.lastPatchNote"));
            sendNotification(event, Utilities.PLUG_CAFE_EVENTS, Utilities.getProperty("guilds.lastEvent"));
            sendNotification(event, Utilities.PLUG_CAFE_NOTICES, Utilities.getProperty("guilds.lastNotice"));
        };
        Runnable euNewDay = () -> sendNewDayNotification(event, "EUROPE");
        Runnable americaNewDay = () -> sendNewDayNotification(event, "AMERICA");
        Runnable asiaNewDay = () -> sendNewDayNotification(event, "ASIA");
        Runnable euHotTime = () -> sendHotTimeNotification(event, "EUROPE");
        Runnable americaHotTime = () -> sendHotTimeNotification(event, "AMERICA");
        Runnable asiaHotTime = () -> sendHotTimeNotification(event, "ASIA");

        /*
         * EU - UTC+1
         * AMERICA - UTC-5
         * ASIA - UTC+7
         */
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = LocalTime.of(23, 0, 0, 0);
        LocalTime localHotTime1 = LocalTime.of(12, 0, 0, 0);
        LocalTime localHotTime2 = LocalTime.of(20, 0, 0, 0);

        ZonedDateTime euTime = ZonedDateTime.now(ZoneId.of("UTC+1"));
        ZonedDateTime americaTime = ZonedDateTime.now(ZoneId.of("UTC-5"));
        ZonedDateTime asiaTime = ZonedDateTime.now(ZoneId.of("UTC+7"));
        ZonedDateTime eu2300 = ZonedDateTime.of(localDate, localTime, ZoneId.of("UTC+1"));
        ZonedDateTime america2300 = ZonedDateTime.of(localDate, localTime, ZoneId.of("UTC-5"));
        ZonedDateTime asia2300 = ZonedDateTime.of(localDate, localTime, ZoneId.of("UTC+7"));

        ZonedDateTime euHotTime1 = ZonedDateTime.of(localDate, localHotTime1, ZoneId.of("UTC+1"));
        ZonedDateTime americaHotTime1 = ZonedDateTime.of(localDate, localHotTime1, ZoneId.of("UTC-5"));
        ZonedDateTime asiaHotTime1 = ZonedDateTime.of(localDate, localHotTime1, ZoneId.of("UTC+7"));
        ZonedDateTime euHotTime2 = ZonedDateTime.of(localDate, localHotTime2, ZoneId.of("UTC+1"));
        ZonedDateTime americaHotTime2 = ZonedDateTime.of(localDate, localHotTime2, ZoneId.of("UTC-5"));
        ZonedDateTime asiaHotTime2 = ZonedDateTime.of(localDate, localHotTime2, ZoneId.of("UTC+7"));

        long euInitialDelayNewDay = eu2300.toEpochSecond() - euTime.toEpochSecond();
        if(euInitialDelayNewDay < 0)
            euInitialDelayNewDay += 86400L;
        if(euInitialDelayNewDay > 86400L)
            euInitialDelayNewDay -= 86400L;

        long americaInitialDelayNewDay = america2300.toEpochSecond() - americaTime.toEpochSecond();
        if(americaInitialDelayNewDay < 0)
            americaInitialDelayNewDay += 86400L;
        if(americaInitialDelayNewDay > 86400L)
            americaInitialDelayNewDay -= 86400L;

        long asiaInitialDelayNewDay = asia2300.toEpochSecond() - asiaTime.toEpochSecond();
        if(asiaInitialDelayNewDay < 0)
            asiaInitialDelayNewDay += 86400L;
        if(asiaInitialDelayNewDay > 86400L)
            asiaInitialDelayNewDay -= 86400L;

        long euHotTimeDelay1 = euHotTime1.toEpochSecond() - euTime.toEpochSecond();
        if(euHotTimeDelay1 < 0)
            euHotTimeDelay1 += 86400L;
        if(euHotTimeDelay1 > 86400L)
            euHotTimeDelay1 -= 86400L;

        long americaHotTimeDelay1 = americaHotTime1.toEpochSecond() - americaTime.toEpochSecond();
        if(americaHotTimeDelay1 < 0)
            americaHotTimeDelay1 += 86400L;
        if(americaHotTimeDelay1 > 86400L)
            americaHotTimeDelay1 -= 86400L;

        long asiaHotTimeDelay1 = asiaHotTime1.toEpochSecond() - asiaTime.toEpochSecond();
        if(asiaHotTimeDelay1 < 0)
            asiaHotTimeDelay1 += 86400L;
        if(asiaHotTimeDelay1 > 86400L)
            asiaHotTimeDelay1 -= 86400L;

        long euHotTimeDelay2 = euHotTime2.toEpochSecond() - euTime.toEpochSecond();
        if(euHotTimeDelay2 < 0)
            euHotTimeDelay2 += 86400L;
        if(euHotTimeDelay2 > 86400L)
            euHotTimeDelay2 -= 86400L;

        long americaHotTimeDelay2 = americaHotTime2.toEpochSecond() - americaTime.toEpochSecond();
        if(americaHotTimeDelay2 < 0)
            americaHotTimeDelay2 += 86400L;
        if(americaHotTimeDelay2 > 86400L)
            americaHotTimeDelay2 -= 86400L;

        long asiaHotTimeDelay2 = asiaHotTime2.toEpochSecond() - asiaTime.toEpochSecond();
        if(asiaHotTimeDelay2 < 0)
            asiaHotTimeDelay2 += 86400L;
        if(asiaHotTimeDelay2 > 86400L)
            asiaHotTimeDelay2 -= 86400L;

        ScheduledExecutorService euNewDayScheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService americaNewDayScheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService asiaNewDayScheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService notificationCheckScheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService euHotTimeScheduler1 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService americaHotTimeScheduler1 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService asiaHotTimeScheduler1 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService euHotTimeScheduler2 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService americaHotTimeScheduler2 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService asiaHotTimeScheduler2 = Executors.newSingleThreadScheduledExecutor();

        notificationCheckScheduler.scheduleWithFixedDelay(notificationCheck, 0, 60, TimeUnit.SECONDS);
        euNewDayScheduler.scheduleWithFixedDelay(euNewDay, euInitialDelayNewDay, 86400L, TimeUnit.SECONDS);
        americaNewDayScheduler.scheduleWithFixedDelay(americaNewDay, americaInitialDelayNewDay, 86400L, TimeUnit.SECONDS);
        asiaNewDayScheduler.scheduleWithFixedDelay(asiaNewDay, asiaInitialDelayNewDay, 86400L, TimeUnit.SECONDS);
        euHotTimeScheduler1.scheduleWithFixedDelay(euHotTime, euHotTimeDelay1, 86400L, TimeUnit.SECONDS);
        americaHotTimeScheduler1.scheduleWithFixedDelay(americaHotTime, americaHotTimeDelay1, 86400L, TimeUnit.SECONDS);
        asiaHotTimeScheduler1.scheduleWithFixedDelay(asiaHotTime, asiaHotTimeDelay1, 86400L, TimeUnit.SECONDS);
        euHotTimeScheduler2.scheduleWithFixedDelay(euHotTime, euHotTimeDelay2, 86400L, TimeUnit.SECONDS);
        americaHotTimeScheduler2.scheduleWithFixedDelay(americaHotTime, americaHotTimeDelay2, 86400L, TimeUnit.SECONDS);
        asiaHotTimeScheduler2.scheduleWithFixedDelay(asiaHotTime, asiaHotTimeDelay2, 86400L, TimeUnit.SECONDS);


        event.getClient().changePlayingText(Utilities.getDefaultPrefix() + "help");

        if(event.getClient().getChannelByID(Utilities.getLoggerChannelId()) == null) {
            Logger.setLogger(null);
        } else {
            Logger.setLogger(event.getClient().getChannelByID(Utilities.getLoggerChannelId()));
        }
    }

    private void sendHotTimeNotification(ReadyEvent event, String server) {
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            System.err.println("Could not read " + Utilities.getProperty("files.guilds"));
            return;
        }
        IUser botUser = event.getClient().getOurUser();

        for(int i = 0; i < guilds.length(); i++) {
            //Check if guild exists
            long guildId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(event.getClient().getGuildByID(guildId) == null) {
                guilds.remove(i);
                i--;
                continue;
            }
            long id = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.hotTimeChannel"))).longValue();
            //If there isn't hot time notification channel continue
            if(id == -1)
                continue;
            //Check hot time notifications status
            int status = (int)guilds.getJSONObject(i).getJSONObject(Utilities.getProperty("guilds.hotTimeStatus")).get(server);
            if(status == 0)
                continue;
            //Check if the channel still exist
            if(event.getClient().getChannelByID(id) == null) {
                guilds.getJSONObject(i).put(Utilities.getProperty("guilds.hotTimeChannel"), -1);
                continue;
            }

            IUser guildOwner = event.getClient().getChannelByID(id).getGuild().getOwner();
            IChannel channel = event.getClient().getChannelByID(id);
            IGuild guild = event.getClient().getGuildByID(guildId);
            //Check if bot has permission for channel
            if(!Utilities.hasPerms(channel, botUser)) {
                Utilities.sendMessage(guildOwner.getOrCreatePMChannel(),
                        String.format(Utilities.getProperty("notifications.privateNoPerm"),
                                botUser.getName(),
                                "hot time",
                                channel.mention(),
                                guild.getName()));
                continue;
            }

            List<String> followList = new ArrayList<>();
            JSONArray followListJson = guilds.getJSONObject(i).getJSONObject(Utilities.getProperty("guilds.hotTimeFollowers")).getJSONArray(server);
            for(int j = 0; j < followListJson.length(); j++) {
                long userId = ((Number)followListJson.get(j)).longValue();
                if(event.getClient().getUserByID(userId) != null) {
                    followList.add("<@" + followListJson.get(j) + ">");
                } else {
                    followListJson.remove(j);
                    j--;
                }
            }

            Utilities.sendMessage(event.getClient().getChannelByID(id), "Hot time has started at server: `" + server + "`.\n" +
                    String.join(", ", followList));

        }
    }

    private void sendNewDayNotification(ReadyEvent event, String server) {
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            System.err.println("Could not read " + Utilities.getProperty("files.guilds"));
            return;
        }
        IUser botUser = event.getClient().getOurUser();

        for(int i = 0; i < guilds.length(); i++) {
            //Check if guild exists
            long guildId = ((Number)guilds.getJSONObject(i).get("id")).longValue();
            if(event.getClient().getGuildByID(guildId) == null) {
                guilds.remove(i);
                i--;
                continue;
            }
            long id = ((Number)guilds.getJSONObject(i).get(Utilities.getProperty("guilds.newDayChannel"))).longValue();
            //If there isn't new day notification channel continue
            if(id == -1)
                continue;
            //Check new day notifications status
            int status = (int)guilds.getJSONObject(i).getJSONObject(Utilities.getProperty("guilds.newDayStatus")).get(server);
            if(status == 0)
                continue;
            //Check if the channel still exist
            if(event.getClient().getChannelByID(id) == null) {
                guilds.getJSONObject(i).put(Utilities.getProperty("guilds.newDayChannel"), -1);
                continue;
            }

            IUser guildOwner = event.getClient().getChannelByID(id).getGuild().getOwner();
            IChannel channel = event.getClient().getChannelByID(id);
            IGuild guild = event.getClient().getGuildByID(guildId);
            //Check if bot has permission for channel
            if(!Utilities.hasPerms(channel, botUser)) {
                Utilities.sendMessage(guildOwner.getOrCreatePMChannel(),
                        String.format(Utilities.getProperty("notifications.privateNoPerm"),
                                botUser.getName(),
                                "new day",
                                channel.mention(),
                                guild.getName()));
                continue;
            }

            List<String> followList = new ArrayList<>();
            JSONArray followListJson = guilds.getJSONObject(i).getJSONObject(Utilities.getProperty("guilds.newDayFollowers")).getJSONArray(server);
            for(int j = 0; j < followListJson.length(); j++) {
                long userId = ((Number)followListJson.get(j)).longValue();
                if(event.getClient().getUserByID(userId) != null) {
                    followList.add("<@" + followListJson.get(j) + ">");
                } else {
                    followListJson.remove(j);
                    j--;
                }
            }

            Utilities.sendMessage(event.getClient().getChannelByID(id), "1 hour left to new day at server: `" + server + "`.\n" +
                    String.join(", ", followList));

        }
    }

    private void sendNotification(ReadyEvent event, String link, String idName) {
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        if(guilds == null) {
            System.err.println("Could not read " + Utilities.getProperty("files.guilds"));
            return;
        }
        try {
            Document doc = Jsoup.connect(link).get();
            Elements elements = doc.getElementsByAttribute("data-articleid");
            // Check every element
            for(int i = elements.size() - 1; i >= 0; i--) {
                String articleId = elements.get(i).attributes().get("data-articleid");
                String articleAuthor = elements.get(i).getElementsByAttributeValue("class", "desc_thumb").get(0).getElementsByAttribute("href").text();
                String articleTitle = elements.get(i).getElementsByAttributeValue("class", "tit_feed").text();
                String articleThumbnail = "";
                if(elements.get(i).getElementsByAttributeValue("class", "img").size() != 0)
                    articleThumbnail = elements.get(i).getElementsByAttributeValue("class", "img").get(0).attributes().get("style");
                // Cleaning thumbnail (related to website design)
                if(!articleThumbnail.isEmpty()) {
                    StringBuilder sb = new StringBuilder(articleThumbnail);
                    sb.delete(0, 21);
                    sb.deleteCharAt(sb.length() - 1);
                    articleThumbnail = sb.toString();
                }
                String articleAuthorIcon = elements.get(i).getElementsByAttributeValue("class", "thumb").get(0).attributes().get("src");
                String articleAuthorUrl = "https://www.plug.game" +
                        elements.get(i).getElementsByAttributeValue("class", "img_thumb").get(0).attributes().get("href");
                String articleText = elements.get(i).getElementsByAttributeValue("class", "txt_feed").text();
                // For every single element check every guild
                for(int j = 0; j < guilds.length(); j++) {
                    long guildId = ((Number)guilds.getJSONObject(j).get("id")).longValue();
                    // Check if guild exists
                    if(event.getClient().getGuildByID(guildId) == null) {
                        guilds.remove(j);
                        j--;
                        continue;
                    }
                    long channelId = ((Number)guilds.getJSONObject(j).get(Utilities.getProperty("guilds.plugCafeChannel"))).longValue();
                    // If plug cafe notifications are off for guild continue
                    if(channelId == -1)
                        continue;
                    int lastId = (int)guilds.getJSONObject(j).get(idName);
                    int currentId = Integer.parseInt(articleId);
                    int mode = (int) guilds.getJSONObject(j).get(Utilities.getProperty("guilds.plugCafeMode"));
                    IUser owner = event.getClient().getChannelByID(channelId).getGuild().getOwner();
                    IUser bot = event.getClient().getOurUser();
                    //Check if channel exists
                    if(event.getClient().getChannelByID(channelId) == null) {
                        //Channel no longer exists. Delete it from guilds.json
                        guilds.getJSONObject(j).put(Utilities.getProperty("guilds.plugCafeChannel"), -1);
                        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                        Utilities.sendMessage(owner.getOrCreatePMChannel(),
                                String.format(Utilities.getProperty("notifications.statusDeleted"),
                                        "Plug cafe",
                                        manager.getCommandPrefix(event.getClient().getGuildByID(guildId)),
                                        "plugcafe"));
                        continue;
                    }
                    // There is new article
                    if(currentId > lastId) {
                        IChannel channel = event.getClient().getChannelByID(channelId);
                        IGuild guild = event.getClient().getGuildByID(guildId);
                        // Check if bot has perms
                        if(!Utilities.hasPerms(event.getClient().getChannelByID(channelId), bot)) {
                            Utilities.sendMessage(owner.getOrCreatePMChannel(),
                                    String.format(Utilities.getProperty("notifications.privateNoPerm"),
                                            bot.getName(),
                                            "plug cafe",
                                            channel.mention(),
                                            guild.getName()));
                            continue;
                        }
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.withTitle(articleTitle);
                        embed.withAuthorName(articleAuthor);
                        embed.withTimestamp(LocalDateTime.now());
                        if(!articleThumbnail.isEmpty())
                            embed.withThumbnail(articleThumbnail);
                        embed.withAuthorIcon(articleAuthorIcon);
                        embed.withAuthorUrl(articleAuthorUrl);
                        embed.appendDesc(articleText);
                        embed.withUrl(Utilities.PLUG_CAFE_BASE_URL + "/posts/" + articleId);

                        List<String> followList = new ArrayList<>();
                        JSONArray followListJson = guilds.getJSONObject(j).getJSONArray(Utilities.getProperty("guilds.plugCafeFollowers"));
                        for(int k = 0; k < followListJson.length(); k++) {
                            long userId = ((Number)followListJson.get(k)).longValue();
                            if(event.getClient().getUserByID(userId) != null) {
                                followList.add("<@" + followListJson.get(k) + ">");
                            } else {
                                followListJson.remove(k);
                                k--;
                            }
                        }

                        if(mode == 0) {
                            Utilities.sendMessage(channel, embed.build(), String.join(", ", followList));
                        } else if(mode == 1) {
                            Utilities.sendMessage(channel, String.join(", ", followList) + "\n\nAuthor: " + articleAuthor +
                                    "\nTitle: " + articleTitle +
                                    "\nLink: <" + Utilities.PLUG_CAFE_BASE_URL + "/posts/" + articleId + ">\n" +
                                    articleText + "\n");
                        } else if(mode == 2) {
                            Utilities.sendMessage(channel, embed.build(), String.join(", ", followList) + "\n\n" +
                                    articleTitle + ": <" + Utilities.PLUG_CAFE_BASE_URL + "/posts/" + articleId + ">\n");
                        }
                        guilds.getJSONObject(j).put(idName, currentId);
                        Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
