package CommandFunctions;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import MusicSearch.Spotify;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class PlayCommand {

    public static boolean playCommand(SlashCommandInteractionEvent event) {
        Logger logger = Logger.getLogger("orion");

        event.deferReply().queue();
        String song = Objects.requireNonNull(event.getOption("song")).getAsString();
        String[] details = new String[4];
        Platform platform = Platform.NULL;
        short goodURL;

        try {
            String platformInput = Objects.requireNonNull(event.getOption("platform")).getAsString();
            platformInput = platformInput.replaceAll("\\s+", "").toLowerCase();

            platform = switch (platformInput) {
                case "spotify" -> Platform.SPOTIFY;
                case "soundcloud" -> Platform.SOUNDCLOUD;
                case "youtube" -> Platform.YOUTUBE;
                default -> Platform.ERROR;
            };
        } catch (Exception ignore) {
        }

        if (platform.equals(Platform.NULL)) {
            platform = Platform.SPOTIFY;
        } else if (platform.equals(Platform.ERROR)) {
            MessageWrapper.errorResponse(event, "Invalid platform, Valid Options: Spotify, Soundcloud, Youtube");
            return false;
        }

        goodURL = isValidURL(song);

        if (goodURL == 2) {
            MessageWrapper.errorResponse(event, "It looks like you provided a valid URL however it was not for a song or a platform we support, Valid Options: Spotify, Soundcloud, Youtube");
            return false;
        }

        if (goodURL == 1) {
            MessageWrapper.genericResponse(event, "success","Adding song to queue");
        }

        if (goodURL == 0) {
            details = getURL(song, platform);
            // 0 - songID
            // 1 - songTitle
            // 2 - Artist
            // 3 - URL
            MessageWrapper.genericResponse(event, "Success!", "adding song " + details[1] + " by " + details[2] + " to queue");
        }

        if (Arrays.equals(details, new String[4])) {
            MessageWrapper.errorResponse(event, "There was an error while searching for the song, please try a different search term, platform or provide a direct link");
            return false;
        }

        //Add the song to the database
        try {
            DatabaseWrapper wrapper = new DatabaseWrapper();
            try {
                wrapper.getQueue(Objects.requireNonNull(event.getGuild()).getId());
            } catch (DBEmptyQueueException e) {
                logger.info("Queue not found, creating new queue...");
                wrapper.createQueue(event.getGuild().getId(), platform.toString(), details[0], details[1], details[2], details[3]);
                logger.info("Song successfully added to queue!");
                return true;
            }
            wrapper.addSong(event.getGuild().getId(), platform.toString(), details[0], details[1], details[2], details[3]);// add song to back of queue in database
            logger.info("Song successfully added to queue!");
            return true;
        } catch (DBConnectionException e) {
            logger.warning("Error adding song to queue: " + e.getMessage());
            MessageWrapper.errorResponse(event, "There was an error while adding the song to the queue, please try again later");
            return false;
        }
        
    }

    private enum Platform {
        SPOTIFY,
        SOUNDCLOUD,
        YOUTUBE,
        NULL,
        ERROR
    }

    private static short isValidURL(String potentialURL) {
        Logger logger = Logger.getLogger("orion");

        try {
            URI url = URI.create(potentialURL);
            String domain = url.getHost();
            if (domain.endsWith("youtube.com") || domain.endsWith("spotify.com") || domain.endsWith("soundcloud.com") || domain.endsWith("youtu.be")) {
                logger.info("Valid URL: " + url + " adding to queue.");
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

    private static String[] getURL(String searchTerm, Platform platform) {
        return Spotify.SpotifySearch(searchTerm); // remove when added the things below
//        return switch (platform) {
//            case SPOTIFY -> Spotify.SpotifySearch(searchTerm);
////            case "youtube" -> new YoutubeSearch().search(searchTerm);
////            case "soundcloud" -> new SoundCloudSearch().search(searchTerm);
//            default -> null;
        };
    }

