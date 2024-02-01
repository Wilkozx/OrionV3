package CommandFunctions;

import java.awt.Button;
import java.util.logging.Logger;

import Errors.DBConnectionException;
import MusicPlayer.PlayerManager;
import Wrapper.DatabaseWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class StopCommand {
    public static Boolean stopCommand(SlashCommandInteractionEvent event) {
        event.reply("Goodbye!").setEphemeral(true).queue();
        initShutdown(event.getGuild());
        return true;
    }

    public static Boolean stopCommand(ButtonInteractionEvent event) {
        event.reply("Goodbye!").setEphemeral(true).queue();
        initShutdown(event.getGuild());
        return true;
    }

    public static void initShutdown(Guild guild) {
        Logger logger = Logger.getLogger("orion");
        sendGoodbyeMessage(guild);
        destroyPlayer(guild);
        emptyQueue(guild, logger);
        unsetNowPlaying(guild, logger);
    }

    public static void unsetNowPlaying(Guild guild, Logger logger) {
        try {
            new DatabaseWrapper().unsetNowPlaying(guild.getId());
        } catch (DBConnectionException e) {
            logger.severe("Failed to unset nowPlaying for guild " + guild.getId() + "\n" + e.getStackTrace());
        }
    }

    public static void sendGoodbyeMessage(Guild guild) {

    }

    public static void destroyPlayer(Guild guild) {
        PlayerManager.get().getGuildMusicManager(guild).getTrackScheduler().stop();
        guild.getAudioManager().closeAudioConnection();
    }

    public static void unsetActiveChannel(Guild guild, Logger logger) {
        try {
            new DatabaseWrapper().unsetActiveChannel(guild.getId());
        } catch (DBConnectionException e) {
            logger.severe("Failed to unset active channel for guild " + guild.getId() + "\n" + e.getStackTrace());
        }
    }

    public static void emptyQueue(Guild guild, Logger logger) {
        try {
            new DatabaseWrapper().destroyQueue(guild.getId());
        }catch (DBConnectionException e) {
            logger.severe("Failed to unset queue for guild " + guild.getId() + "\n" + e.getStackTrace());
        }
    }
}
