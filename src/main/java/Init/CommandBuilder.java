package Init;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.logging.Logger;

public class CommandBuilder {
    public static void BuildCommands(JDA Orion) {
        Logger logger = Logger.getLogger("orion");

        for(Guild guild : Orion.getGuilds()) {
            logger.info("Adding commands for Guild: \"" + guild.getName() + "\" with ID: [" + guild.getId() + "]");

//            guild.upsertCommand("join", "joins the voice channel").queue();
//            logger.info("Added command /join.");

        }
    }
}
