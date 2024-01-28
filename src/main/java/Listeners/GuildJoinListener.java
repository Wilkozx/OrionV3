package Listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.logging.Logger;

import Wrapper.DatabaseWrapper;

public class GuildJoinListener extends ListenerAdapter {
    Logger logger = Logger.getLogger("orion");

    public void onJoin(GuildJoinEvent event) {
        logger.info("Joined " + event.getGuild().getName() + ": " + event.getGuild().getId());
            try {
                DatabaseWrapper db = new DatabaseWrapper();
                db.initGuild(event.getGuild().getId());
            } catch (Exception e) {
                logger.warning("Error initializing guild: " + e.getMessage());
            }
    }
}
