package com.raepheles.discord.cleobot.logfilters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import sx.blah.discord.util.LogMarkers;

/**
 * Created by Rae on 12/1/2018.
 * Filter rules:
 * Deny if log marker is MESSAGES
 * Deny if log marker is EVENTS and starts with "user"
 *     - More specifically logs that are marked as EVENTS and starts with "user" are user join/leave guild logs.
 * Deny if log marker is PRESENCES
 * Accept everything else
 *
 * Used for debug logs.
 */
public class DebugFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {

        if(event.getMarker().equals(LogMarkers.MESSAGES)
                || (event.getMarker().equals(LogMarkers.EVENTS) && event.getMessage().toLowerCase().startsWith("user"))
                || event.getMarker().equals(LogMarkers.PRESENCES))
            return FilterReply.DENY;
        else
            return FilterReply.ACCEPT;

    }
}
