package Listeners;

import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.logging.Logger;

public class DisconnectListener extends ListenerAdapter {
    Logger logger = Logger.getLogger("orion");

    public void onDisconnect(SessionDisconnectEvent event) {
        logger.info("Disconnected");
        // put code here
    }

}
