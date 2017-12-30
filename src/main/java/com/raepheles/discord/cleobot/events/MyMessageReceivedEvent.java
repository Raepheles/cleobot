package com.raepheles.discord.cleobot.events;

import com.raepheles.discord.cleobot.Utilities;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Created by Rae on 28/12/2017.
 * Bot's Message Received Event.
 * Only using to listen to bot's private channel.
 */
public class MyMessageReceivedEvent {
    private long privateChannelListener;

    public MyMessageReceivedEvent(long privateChannelListener) {
        this.privateChannelListener = privateChannelListener;
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent event) {
        if(event.getChannel().isPrivate() && privateChannelListener != -1 && !event.getMessage().getContent().startsWith(Utilities.getDefaultPrefix())) {
            System.out.println(Utilities.getDefaultPrefix());
            IChannel privateChannelLogsChannel = event.getClient().getChannelByID(privateChannelListener);
            if(privateChannelLogsChannel != null) {
                String reply = String.format("User `%s` with id `%d` have sent the following message to Cleo on private channel:\n%s",
                        event.getAuthor().getName(),
                        event.getAuthor().getLongID(),
                        event.getMessage().getContent());
                Utilities.sendMessage(privateChannelLogsChannel, reply);
            }
        }
    }
}
