package CommandFunctions;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import MusicPlayer.PlayerManager;
import MusicSearch.SoundCloud;
import MusicSearch.Spotify;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import org.bson.Document;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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
        
        try {
            requestedPlatform = Objects.requireNonNull(event.getOption("platform")).getAsString().toLowerCase().replaceAll("\\s+", "");
        } catch (Exception e) {
            logger.info("User did not specify a platform");
        }

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

        Boolean added = addSongToDB(details, guild, platform);
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

        Boolean added = addSongToDB(details, guild, platform);
        if (added) {
            MessageWrapper.genericResponse(event, "Added Song " + details[1], " by " + details[2] + " to the queue;");
            return true;
        }
        MessageWrapper.errorResponse(event, "There was an error while adding the song to the queue, please try again later");
        return false;

    }

    public static String[] parseDefault(String song, Guild guild, String requestedPlatform) {
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

    public static String[] getDefaultAndParse(String song, Guild guild) {

        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Document settings = db.getSettings(guild.getId());
            String platform = settings.getString("defaultPlatform");

            switch (platform) {
                case "SOUNDCLOUD":
                    return parseSoundcloud(song);
                case "YOUTUBE":
                    return parseYoutube(song);
                default:
                    throw new Exception("Invalid platform");
            }
        } catch (Exception e) {
            return parseSoundcloud(song);
        }
    }

    private static String[] parseSoundcloud(String song) {
        return SoundCloud.searchSoundCloud(song);
    }

    private static String[] parseYoutube(String song) {
        return SoundCloud.searchSoundCloud(song);
        //TODO: implement youtube search
    }

    @Deprecated
    public static boolean playCommandDeprecated(SlashCommandInteractionEvent event) {
        Logger logger = Logger.getLogger("orion");

        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel to queue a song").queue();
            return false;
        }

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            syncBotToUserChannel(event, memberVoiceState);

            try {
                new DatabaseWrapper().setActiveChannel(event.getGuild().getId(), event.getChannelId());
            } catch (DBConnectionException e) {
                throw new RuntimeException(e);
            }
        }

        if (selfVoiceState.inAudioChannel()) {
            if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel to queue a song").queue();
                return false;
            }
        }

        event.deferReply().queue();
        String song = Objects.requireNonNull(event.getOption("song")).getAsString();
        String[] details = new String[4];

        Platform platform = Platform.NULL;
        short goodURL;

        try {
            String platformInput = Objects.requireNonNull(event.getOption("platform")).getAsString();
            platformInput = platformInput.replaceAll("\\s+", "").toLowerCase();

            platform = switch (platformInput) {
                case "soundcloud" -> Platform.SOUNDCLOUD;
                case "youtube" -> Platform.YOUTUBE;
                default -> Platform.ERROR;
            };
        } catch (Exception ignore) {}

        if (platform.equals(Platform.NULL)) {
            platform = Platform.SOUNDCLOUD;
        } else if (platform.equals(Platform.ERROR)) {
            MessageWrapper.errorResponse(event, "Invalid platform, Valid Options: Soundcloud, Youtube");
            return false;
        }

        goodURL = isValidURL(song);

        if (goodURL == 2) {
            MessageWrapper.errorResponse(event, "It looks like you provided a valid URL however it was not for a song or a platform we support, Valid Options: Soundcloud, Youtube");
            return false;
        }

        if (goodURL == 1) {
            URI url = URI.create(song);
            String domain = url.getHost();

            // later make this a interpretURL function
            if (domain.endsWith("youtube.com") || domain.endsWith("youtu.be")) {
                if (song.contains("playlist")) {
                    platform = Platform.YOUTUBE;

                } else {
                    platform = Platform.YOUTUBE;
                    details[0] = "";
                    details[1] = "";
                    details[2] = "";
                    details[3] = song;
                    MessageWrapper.genericResponse(event, "success","Adding song to queue");
                }
            }

            if (domain.endsWith("spotify.com")) {
                String[] songDetails;
                songDetails = Spotify.interpretSpotifyLink(song);
                details = getURL(songDetails[0] + " " + songDetails[1], platform);
            }

        }

        if (goodURL == 0) {
            details = getURL(song, platform);
            // 0 - songID
            // 1 - songTitle
            // 2 - Artist
            // 3 - URL
        }

        if (Arrays.equals(details, new String[4])) {
            MessageWrapper.errorResponse(event, "There was an error while searching for the song, please try a different search term, platform or provide a direct link");
            return false;
        }

        // Add the song to the database
        try {
            DatabaseWrapper wrapper = new DatabaseWrapper();
            try {
                wrapper.getQueue(Objects.requireNonNull(event.getGuild()).getId());
            } catch (DBEmptyQueueException e) {
                logger.info("Queue not found, creating new queue...");
                wrapper.createQueue(event.getGuild().getId(), platform.toString(), details[1], details[2], details[3]);
                logger.info("Song successfully added to queue!");

                playLatest(event.getGuild());
                MessageWrapper.genericResponse(event, "Added Song " + details[1], " by " + details[2] + " to the queue;");

                return true;
            }

            wrapper.addSong(event.getGuild().getId(), platform.toString(), details[1], details[2], details[3]);// add song to back of queue in database
            logger.info("Song successfully added to queue!");

            playLatest(event.getGuild());
            MessageWrapper.genericResponse(event, "Added Song " + details[1], "by " + details[2] + " to the queue;");

            return true;
        } catch (DBConnectionException e) {
            logger.warning("Error adding song to queue: " + e.getMessage());
            MessageWrapper.errorResponse(event, "There was an error while adding the song to the queue, please try again later");
            return false;
        }
    }

    public static void playCommand(ButtonInteractionEvent event) {
        MessageWrapper.playModal(event);
    }

    public static boolean addSongToDB (String[] details, Guild guild, Platform platform) {
        Logger logger = Logger.getLogger("orion");
        String guildID = guild.getId();
        try {
            DatabaseWrapper wrapper = new DatabaseWrapper();
            try {
                wrapper.getQueue(guildID);
            } catch (DBEmptyQueueException e) {
                logger.info("Queue not found, creating new queue...");
                wrapper.createQueue(guildID, platform.toString(), details[1], details[2], details[3]);
                logger.info("Song successfully added to queue!");

                playLatest(guild);
                //MessageWrapper.genericResponse(event, "Added Song " + details[1], " by " + details[2] + " to the queue;");
                return true;
            }

            wrapper.addSong(guildID, platform.toString(), details[1], details[2], details[3]);// add song to back of queue in database
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
    public static void syncBotToUserChannel(SlashCommandInteractionEvent event, GuildVoiceState memberVoiceState) {
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

    public static void syncBotToUserChannel(ModalInteractionEvent event, GuildVoiceState memberVoiceState) {
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

    @Deprecated
    private static short isValidURL(String potentialURL) {
        Logger logger = Logger.getLogger("orion");
        try {
            URI url = URI.create(potentialURL);
            String domain = url.getHost();
            if (domain.endsWith("youtube.com") || /*domain.endsWith("spotify.com") ||*/ domain.endsWith("soundcloud.com") || domain.endsWith("youtu.be")) {
                logger.info("Valid URL: " + url);
                return 1;
            } else {
                logger.info("Valid URL, but unsupported");
                return 2;
            }
        } catch (Exception e) {
            logger.info("Invalid URL: " + e.getMessage());
            return 0;
        }
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

    private static String[] getURL(String searchTerm, Platform platform) {
        return switch (platform) {
//            case SPOTIFY -> Spotify.searchSpotify(searchTerm);
//            case YOUTUBE -> Youtube.searchYoutube(searchTerm);
            case SOUNDCLOUD -> SoundCloud.searchSoundCloud(searchTerm);
            default -> null;
        };
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

                String activeChannel = new DatabaseWrapper().getActiveChannel(guild.getId());
                TextChannel textChannel = guild.getTextChannelById(activeChannel);

                assert textChannel != null;
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

