package Listeners;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.logging.Logger;

import CommandFunctions.StopCommand;
import Wrapper.MessageWrapper;

public class DisconnectListener extends ListenerAdapter {
    Logger logger = Logger.getLogger("orion");

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (!event.getMember().equals(event.getGuild().getSelfMember())) {
            return;
        }
        /*
         * This checks if the bot has been disconnected from a channel
         */
        if (event.getChannelJoined() == null) {

            logger.info("@" + event.getGuild().getName() + ": I have been disconnected");
            MessageWrapper.Disconnected(event);
            StopCommand.initShutdown(event.getGuild());
            return;
        }
        /*
         * This checks if the bot has been moved from one channel to another channel
         */
        if (event.getChannelLeft() != null) {
            logger.info("@" + event.getGuild().getName() + ": Bot has been moved from  🔊 " + event.getChannelLeft().getName() + " to  🔊 " + event.getChannelJoined().getName());
            MessageWrapper.Moved(event);
            return;
        }
    }

}
