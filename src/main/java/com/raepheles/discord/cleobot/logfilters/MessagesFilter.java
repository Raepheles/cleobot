package com.raepheles.discord.cleobot.logfilters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.raepheles.discord.cleobot.Utilities;
import sx.blah.discord.util.LogMarkers;

/**
 * Created by Rae on 12/1/2018.
 * Filter rules:
 * Accept if log marker is MESSAGES and message isn't coming from log channel
 * Deny everything else
 *
 * Used for message logs.
 */
public class MessagesFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {

        if(event.getMarker().equals(LogMarkers.MESSAGES) && !event.getFormattedMessage().toLowerCase().contains( String.format("channel id: %d", Utilities.getLoggerChannelId() )))
            return FilterReply.ACCEPT;
        else
            return FilterReply.DENY;

    }
}
