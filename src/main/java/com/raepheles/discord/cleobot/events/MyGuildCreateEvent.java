package com.raepheles.discord.cleobot.events;

import com.raepheles.discord.cleobot.Utilities;
import com.raepheles.discord.cleobot.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by Rae on 19/12/2017.
 */
public class MyGuildCreateEvent {

    @EventSubscriber
    public void onGuildCreate(GuildCreateEvent event) {
        IGuild guild = event.getGuild();
        //Check if guild is saved inside guilds.json
        JSONArray guilds = Utilities.readJsonFromFile(Utilities.getProperty("files.guilds"));
        boolean isSavedGuild = false;
        for(int i = 0; i < guilds.length(); i++) {
            long currentId = (long)guilds.getJSONObject(i).get("id");
            if(currentId == guild.getLongID()) {
                isSavedGuild = true;
            }
        }

        JSONArray whitelist = Utilities.readJsonFromFile(Utilities.getProperty("files.whitelist"));
        if(Utilities.getWhitelistStatus()) {
            boolean whitelisted = false;
            for(int j = 0; j < whitelist.length(); j++) {
                if( ((Number)whitelist.get(j)).longValue() == guild.getLongID() )
                    whitelisted = true;
            }
            if(!whitelisted) {
                Utilities.sendMessage(guild.getOwner().getOrCreatePMChannel(), Utilities.getProperty("join.whitelistFail"));
                Utilities.leaveGuild(guild);
                return;
            }
        }

        if(!isSavedGuild) {
            JSONObject newGuild = Utilities.newGuildEntry(guild.getLongID());
            guilds.put(newGuild);
            Utilities.writeToJsonFile(guilds, Utilities.getProperty("files.guilds"));
            Utilities.sendMessage(guild.getOwner().getOrCreatePMChannel(), Utilities.getProperty("join.success"));
        }
        Logger.logGuildJoin(event);
    }
}
