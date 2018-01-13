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
        }
    }
}
