package Listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.logging.Logger;

public class JoinListener extends ListenerAdapter {
    Logger logger = Logger.getLogger("orion");

    public void onJoin(GuildJoinEvent event) {
        logger.info("Joined " + event.getGuild().getName() + ": " + event.getGuild().getId());
        // add code here
    }
}
