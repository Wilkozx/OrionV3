package Init;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.logging.Logger;

public class CommandBuilder {
    public static void BuildCommands(JDA Orion) {
        Logger logger = Logger.getLogger("orion");

        for(Guild guild : Orion.getGuilds()) {
            logger.info("Adding commands for Guild: \"" + guild.getName() + "\" with ID: [" + guild.getId() + "]");

            guild.upsertCommand("join", "joins the voice channel").queue();
            logger.info("Added command /join.");

            guild.upsertCommand("play", "adds a song to the back of the queue")
                    .addOption(OptionType.STRING, "song", "the song you want to add, either a query or url", true)
                    .addOption(OptionType.STRING, "platform", "the platform u want to search", false).queue();
            logger.info("Added command /play.");

            guild.upsertCommand("queue", "shows you the current queue").queue();
            logger.info("Added command /queue.");

            guild.upsertCommand("pause", "pauses the current track").queue();
            logger.info("Added command /pause.");

            guild.upsertCommand("resume", "resumes the paused track").queue();
            logger.info("Added command /resume.");

            guild.upsertCommand("skip", "skips the current track").queue();
            logger.info("Added command /skip");


        }
    }
}
