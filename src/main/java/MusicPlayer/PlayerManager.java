package MusicPlayer;

import com.google.api.client.util.Data;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import CommandFunctions.PlayCommand;
import Errors.AudioException;
import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import MusicSearch.YoutubeWrapper;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.internal.ws.RealWebSocket.Message;

import java.util.HashMap;
import java.util.logging.Logger;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final HashMap<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    private PlayerManager() {
        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager get() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());
            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL) {
        Logger logger = Logger.getLogger("orion");
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        logger.info("Opening audio stream for track: " + trackURL);
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                logger.info("Track loaded: " + audioTrack.getInfo().title);
                guildMusicManager.getTrackScheduler().queue(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                logger.warning(trackURL + " is a playlist, not a track. NOT IMPLEMENTED");
            }

            @Override
            public void noMatches() {
                logger.warning(trackURL + " NO MATCHES FOUND. Please provide a valid track URL.");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                logger.warning(trackURL + " LOAD FAILED. " + e.getMessage());

                TextChannel activeChannel;
                Message activeMessage;

                try {
                    DatabaseWrapper db = new DatabaseWrapper();
                    String activeChannelId = db.getActiveChannel(guild);
                    activeChannel = guild.getTextChannelById(activeChannelId);
                    activeChannel.deleteMessageById(db.getActiveMessage(guild.getId())).queue();;
                } catch (Exception e2) {
                    activeChannel = guild.getDefaultChannel().asTextChannel();
                }

                String songName = YoutubeWrapper.getTitle(trackURL);

                MessageWrapper.errorResponse(activeChannel, "Failed to load track", songName + " - " + e.getMessage());

                PlayCommand.playLatest(guild);
            }

        });

    }

}
