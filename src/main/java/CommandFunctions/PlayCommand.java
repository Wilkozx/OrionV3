package CommandFunctions;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import MusicPlayer.PlayerManager;
import MusicSearch.SoundCloudWrapper;
import MusicSearch.YoutubeWrapper;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import org.bson.Document;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class PlayCommand {

    public static boolean playCommand(SlashCommandInteractionEvent event) {

        event.deferReply().queue();

        Logger logger = Logger.getLogger("orion");
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Member self = guild.getSelfMember();
        String song = Objects.requireNonNull(event.getOption("song")).getAsString();
        String requestedPlatform = null;

        short voiceStatus = VoiceCommandChecks.checkVoiceState(member.getVoiceState(), self.getVoiceState());

        switch (voiceStatus) {
            case 0:
                break;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to queue a song");
                return false;
            case 2:
                syncBotToUserChannel(event, member.getVoiceState());
                try {
                    new DatabaseWrapper().setActiveChannel(guild.getId(), event.getChannel().getId());
                } catch (Exception ignore) {
                }
                break;
            case 3:
                MessageWrapper.errorResponse(event, "You need to be in the same channel to queue a song");
                return false;
        }

        Platform platform = urlType(song);
        String[] details = new String[4];
        try {
            switch (platform) {
                case ERROR:
                    MessageWrapper.errorResponse(event, "Invalid platform, Valid Options: Soundcloud, Youtube");
                    break;
                case NULL:
                    details = parseDefault(song, guild, requestedPlatform); // this is the only case where you do not have a url
                    break;
                case SOUNDCLOUD:
                    details = parseSoundcloud(song);
                    break;
                case YOUTUBE:
                    details = parseYoutube(song);
                    break;
                case SPOTIFY:
                    MessageWrapper.errorResponse(event, "Spotify is not supported at this time");
                    return false;
                }
            } catch (MalformedURLException e) {
                MessageWrapper.errorResponse(event, "There was an error with the URL provided, please try again");
                return false;
            }

        Boolean added = addSongToDB(details, guild);
        if (added) {
            MessageWrapper.genericResponse(event, "Added Song " + details[1], " by " + details[2] + " to the queue;");
            return true;
        }
        MessageWrapper.errorResponse(event, "There was an error while adding the song to the queue, please try again later");
        return false;

    }

    public static boolean playCommand(ModalInteractionEvent event) {

        event.deferReply().queue();

        Logger logger = Logger.getLogger("orion");
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Member self = guild.getSelfMember();
        String song = Objects.requireNonNull(event.getValue("song")).getAsString();
        String requestedPlatform = null;

        short voiceStatus = VoiceCommandChecks.checkVoiceState(member.getVoiceState(), self.getVoiceState());

        switch (voiceStatus) {
            case 0:
                break;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to queue a song");
                return false;
            case 2:
                syncBotToUserChannel(event, member.getVoiceState());
                break;
            case 3:
                MessageWrapper.errorResponse(event, "You need to be in the same channel to queue a song");
                return false;
        }

        Platform platform = urlType(song);
        String[] details = new String[4];
        try {
            switch (platform) {
                case ERROR:
                    MessageWrapper.errorResponse(event, "Invalid platform, Valid Options: Soundcloud, Youtube");
                    break;
                case NULL:
                    details = parseDefault(song, guild, requestedPlatform); // this is the only case where you do not have a url
                    break;
                case SOUNDCLOUD:
                    details = parseSoundcloud(song);
                    break;
                case YOUTUBE:
                    details = parseYoutube(song);
                    break;
                case SPOTIFY:
                    MessageWrapper.errorResponse(event, "Spotify is not supported at this time");
                    return false;
                }
            } catch (MalformedURLException e) {
                MessageWrapper.errorResponse(event, "There was an error with the URL provided, please try again");
                return false;
            }

        Boolean added = addSongToDB(details, guild);
        if (added) {
            MessageWrapper.genericResponse(event, "Added Song " + details[1], " by " + details[2] + " to the queue;");
            return true;
        }
        MessageWrapper.errorResponse(event, "There was an error while adding the song to the queue, please try again later");
        return false;

    }

    public static String[] parseDefault(String song, Guild guild, String requestedPlatform) throws MalformedURLException {
        if (requestedPlatform == null) {
            return getDefaultAndParse(song, guild);
        }
        switch (requestedPlatform) {
            case "SOUNDCLOUD":
                return parseSoundcloud(song);
            case "YOUTUBE":
                return parseYoutube(song);
            default:
                return getDefaultAndParse(song, guild);
        }
    }

    public static String[] getDefaultAndParse(String song, Guild guild) throws MalformedURLException {

        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Document settings = db.getSettings(guild.getId());
            String platform = settings.getString("defaultPlatform");

            switch (platform) {
                case "SOUNDCLOUD":
                    return searchSoundcloud(song);
                case "YOUTUBE":
                    return searchYoutube(song);
                default:
                    throw new Exception("Invalid platform");
            }
        } catch (Exception e) {
            return parseSoundcloud(song);
        }
    }

    private static String[] parseSoundcloud(String song) throws MalformedURLException {
        return SoundCloudWrapper.parseSoundcloud(song);
    }

    private static String[] parseYoutube(String song) throws MalformedURLException {
        return YoutubeWrapper.parseYoutube(song);
        //TODO: implement youtube search
    }

    private static String[] searchSoundcloud(String song) {
        return SoundCloudWrapper.searchSoundCloud(song);
    }

    private static String[] searchYoutube(String song) {
        return SoundCloudWrapper.searchSoundCloud(song);
    }

    public static void playCommand(ButtonInteractionEvent event) {
        MessageWrapper.playModal(event);
    }

    public static boolean addSongToDB (String[] details, Guild guild) {
        Logger logger = Logger.getLogger("orion");
        String guildID = guild.getId();
        try {
            DatabaseWrapper wrapper = new DatabaseWrapper();
            try {
                wrapper.getQueue(guildID);
            } catch (DBEmptyQueueException e) {
                logger.info("Queue not found, creating new queue...");
                wrapper.createQueue(guildID, details[0], details[1], details[2], details[3]);
                logger.info("Song successfully added to queue!");

                playLatest(guild);
                //MessageWrapper.genericResponse(event, "Added Song " + details[1], " by " + details[2] + " to the queue;");
                return true;
            }

            wrapper.addSong(guildID, details[0].toString(), details[1], details[2], details[3]);// add song to back of queue in database
            logger.info("Song successfully added to queue!");

            playLatest(guild);
            //MessageWrapper.genericResponse(event, "Added Song " + details[1], "by " + details[2] + " to the queue;");

            return true;
        } catch (DBConnectionException e) {
            logger.warning("Error adding song to queue: " + e.getMessage());
            //MessageWrapper.errorResponse(event, "There was an error while adding the song to the queue, please try again later");
            return false;
        }
    }

    // eventually move this with the additional checks elsewhere but for now this is fine
    public static void syncBotToUserChannel(Interaction event, GuildVoiceState memberVoiceState) {
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                // add this
            }
        }
    }

    private enum Platform {
        SPOTIFY,
        SOUNDCLOUD,
        YOUTUBE,
        NULL,
        ERROR
    }

    private static Platform urlType(String potentialURL) {
        Logger logger = Logger.getLogger("orion");
        try {
            URI url = URI.create(potentialURL);
            String domain = url.getHost();
            if (domain.endsWith("youtube.com") || domain.endsWith("youtu.be")) {
                logger.info("Youtube URL: " + url);
                return Platform.YOUTUBE;
            } else if (domain.endsWith("spotify.com")) {
                logger.info("Spotify URL: " + url);
                return Platform.SPOTIFY;
            } else if (domain.endsWith("soundcloud.com")) {
                logger.info("Soundcloud URL: " + url);
                return Platform.SOUNDCLOUD;
            } else {
                logger.info("Valid URL, but unsupported");
                return Platform.ERROR;
            }
        } catch (Exception e) {
            logger.info("Invalid URL: " + e.getMessage());
            return Platform.NULL;
        }
    }

    public static boolean playLatest(Guild guild) {
        Logger logger = Logger.getLogger("orion");
        PlayerManager playerManager = PlayerManager.get();

        if (playerManager.getGuildMusicManager(guild).getTrackScheduler().getPlaying() == null) {
            logger.info("Attempting to play latest song...");
            try {
                DatabaseWrapper db = new DatabaseWrapper();
                Document song;
                Document settings = db.getSettings(guild.getId());
                if (settings.getBoolean("shuffle")) {
                    ArrayList<Document> queue = db.getQueue(guild.getId());
                    int randomIndex = (int) (Math.random() * queue.size()) + 1;
                    try {
                        song = db.removeSong(guild.getId(), randomIndex);
                    } catch (IndexOutOfBoundsException e) {
                        song = db.popNextSong(guild.getId());
                    }
                    
                } else {
                    song = db.popNextSong(guild.getId());
                }

                String songUrl = song.getString("url");

                String activeChannel = new DatabaseWrapper().getActiveChannel(guild);
                TextChannel textChannel = guild.getTextChannelById(activeChannel);

                MessageWrapper.startedPlaying(textChannel, song);
                new DatabaseWrapper().setNowPlaying(guild.getId(), song);
                logger.info("Playing song: " + song.get("songTitle").toString() + " by " + song.get("artist").toString());

                playerManager.play(guild, songUrl);
                logger.info("Song successfully playing!");
                return true;
            } catch (DBEmptyQueueException | DBConnectionException e) {
                logger.info("Error playing song, queue is empty in guild " + guild.getId());
                //StopCommand.initShutdown(guild);
            }
        } else {
            logger.info("Error playing song");
            return false;
        }

        return false;
    }

}

