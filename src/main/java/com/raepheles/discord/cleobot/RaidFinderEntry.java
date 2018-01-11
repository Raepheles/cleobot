package com.raepheles.discord.cleobot;

import sx.blah.discord.handle.obj.IUser;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by Rae on 27/12/2017.
 * Raid Finder Entry class. Used for Raid Finder.
 */
public class RaidFinderEntry {
    private long epochTime;
    private String accountName;
    private String serverName;
    private String raidCode;
    private String raidName;
    private String raidLevel;
    private String userNote;
    private boolean found;
    private IUser user;


    public RaidFinderEntry(long epochTime,
                           String serverName,
                           String accountName,
                           String raidCode,
                           String raidLevel,
                           IUser user,
                           String userNote) {
        this.epochTime = epochTime;
        this.accountName = accountName;
        this.serverName = serverName;
        this.raidCode = raidCode;
        this.raidLevel = raidLevel;
        this.found = false;
        this.user = user;
        this.userNote = userNote;
    }

    public IUser getUser() {
        return user;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRaidCode() {
        return raidCode;
    }

    public boolean isFound() {
        return found;
    }

    public String getRaidLevel() {
        return raidLevel;
    }

    public void setFound() {
        found = true;
    }

    public boolean isLegitEntry() {
        if(raidCode.equalsIgnoreCase("bd")) {
            raidName = "Black Dragon";
            int intRaidLevel;
            try {
                intRaidLevel = Integer.parseInt(raidLevel);
                if(intRaidLevel < 35 || intRaidLevel > 100)
                    return false;
            } catch(NumberFormatException nfe) {
                return false;
            }
        } else if(raidCode.equalsIgnoreCase("fd")) {
            raidName = "Fire Dragon";
            int intRaidLevel;
            try {
                intRaidLevel = Integer.parseInt(raidLevel);
                if(intRaidLevel < 35 || intRaidLevel > 100)
                    return false;
            } catch(NumberFormatException nfe) {
                return false;
            }
        } else if(raidCode.equalsIgnoreCase("id")) {
            raidName = "Ice Dragon";
            int intRaidLevel;
            try {
                intRaidLevel = Integer.parseInt(raidLevel);
                if(intRaidLevel < 35 || intRaidLevel > 100)
                    return false;
            } catch(NumberFormatException nfe) {
                return false;
            }
        } else if(raidCode.equalsIgnoreCase("pd")) {
            raidName = "Poison Dragon";
            int intRaidLevel;
            try {
                intRaidLevel = Integer.parseInt(raidLevel);
                if(intRaidLevel < 35 || intRaidLevel > 100)
                    return false;
            } catch(NumberFormatException nfe) {
                return false;
            }
        } else if(raidCode.equalsIgnoreCase("bdh")) {
            raidName = "Black Dragon Hard";
            if(!raidLevel.isEmpty())
                return false;
        } else if(raidCode.equalsIgnoreCase("fdh")) {
            raidName = "Fire Dragon Hard";
            if(!raidLevel.isEmpty())
                return false;
        } else if(raidCode.equalsIgnoreCase("idh")) {
            raidName = "Ice Dragon Hard";
            if(!raidLevel.isEmpty())
                return false;
        } else if(raidCode.equalsIgnoreCase("pdh")) {
            raidName = "Poison Dragon Hard";
            if(!raidLevel.isEmpty())
                return false;
        } else if(raidCode.equalsIgnoreCase("cr")) {
            raidName = "Challenge Raid";
            if(!raidLevel.equalsIgnoreCase("easy") &&
                    !raidLevel.equalsIgnoreCase("normal") &&
                    !raidLevel.equalsIgnoreCase("hard") &&
                    !raidLevel.equalsIgnoreCase("hell"))
                return false;
        }
        return true;
    }

    public int getTime() {
        return (int)(ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond() - epochTime);
    }

    @Override
    public String toString() {
        String time;
        int seconds = getTime();
        int hours, minutes;
        minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        hours = minutes / 60;
        minutes = minutes - hours * 60;
        if(hours != 0)
            time = String.format("%dh %dm %ds", hours, minutes, seconds);
        else if(minutes != 0)
            time = String.format("%dm %ds", minutes, seconds);
        else
            time = String.format("%ds", seconds);
        if(isLegitEntry())
            return String.format("%-12s | %-12s | %-7s | %-25s | %-50s",
                    time,
                    accountName,
                    serverName.toUpperCase(),
                    raidName + (raidLevel.isEmpty() ? "" : " " + raidLevel),
                    userNote.length() > 50 ? userNote.substring(0, 46) + "..." : userNote);
        else
            return "Not legit raid entry!";
    }
}
