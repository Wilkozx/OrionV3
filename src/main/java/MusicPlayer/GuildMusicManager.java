package MusicPlayer;

import CommandFunctions.PlayCommand;
import Wrapper.DatabaseWrapper;

import java.util.logging.Logger;

import org.bson.Document;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager {

    private final TrackScheduler trackScheduler;
    private final AudioForwarder audioForwarder;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        Logger logger = Logger.getLogger("orion");
        logger.info("Creating new GuildMusicManager for guild: " + guild.getName());
        AudioPlayer player = manager.createPlayer();
        trackScheduler = new TrackScheduler(player) {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                logger.info("Track ended, playing next track...");
                try {
                    DatabaseWrapper db = new DatabaseWrapper();
                    try {
                        logger.info("Attempting to delete active message...");
                        // guild.getTextChannelById(db.getActiveChannel(guild.getId())).getHistory().getMessageById(db.getActiveMessage(guild.getId())).delete().queue();
                        guild.getTextChannelById(db.getActiveChannel(guild.getId())).deleteMessageById(db.getActiveMessage(guild.getId())).queue();
                        logger.info("Active message deleted successfully!");
                    } catch (Exception e) {
                        logger.info("No active message found: \n" + e.getMessage());
                    }

                    Document song = db.getNowPlaying(guild.getId());
                    
                    if (db.getSettings(guild.getId()).getBoolean("loop")) {
                        db.addSong(guild.getId(), song.getString("platform"), song.getString("songTitle"), song.getString("artist"), song.getString("url"));
                    }
                    try {
                        logger.info("Attempting to unset now playing...");
                        db.unsetNowPlaying(guild.getId());
                        logger.info("Now playing unset successfully!");
                    } catch (Exception e) {
                        logger.info("Error unsetting now playing: \n" + e.getMessage());
                    }
                } catch (Exception e) {
                    logger.info("No active message found: \n" + e.getMessage());
                }

                if (guild.getSelfMember().getVoiceState().getChannel() == null) {
                    return;
                }
                PlayCommand.playLatest(guild);
            }
        };
        player.addListener(trackScheduler);
        audioForwarder = new AudioForwarder(player, guild);
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public AudioForwarder getAudioForwarder() {
        return audioForwarder;
    }

}
