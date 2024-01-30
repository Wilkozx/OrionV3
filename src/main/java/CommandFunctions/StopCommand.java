package CommandFunctions;

import java.util.logging.Logger;

import Errors.DBConnectionException;
import MusicPlayer.PlayerManager;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopCommand {
    public static Boolean stopCommand(SlashCommandInteractionEvent event) {
        MessageWrapper.genericResponse(event, "Shutting Down...", "Goodbye!");
        initShutdown(event.getGuild());
        return true;
    }

    public static void initShutdown(Guild guild) {
        Logger logger = Logger.getLogger("orion");
        unsetNowPlaying(guild, logger);
        sendGoodbyeMessage(guild);
        emptyQueue(guild, logger);
        destroyPlayer(guild);
        unsetActiveChannel(guild, logger);
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
