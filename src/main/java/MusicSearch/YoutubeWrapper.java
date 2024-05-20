package MusicSearch;

import Errors.DBConnectionException;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class YoutubeWrapper {
    static Dotenv dotenv = Dotenv.load();
    static Logger logger = Logger.getLogger("orion");
    static String youtubeKey = dotenv.get("YOUTUBEKEY");

    // Load client secrets from a file
    // Set up authorization code flow

    public static String[] searchYoutube(String searchTerm) {
        String[] details = new String[4];
        try {
            URL localurl = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8) + "&key=" + youtubeKey);
            HttpURLConnection httpURLConnection = (HttpURLConnection) localurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            JsonObject jsonObject = getJsonObject(localurl).get("items").getAsJsonArray().get(0).getAsJsonObject();

            // 0 - PLATFORM
            // 1 - songTitle
            // 2 - Artist
            // 3 - URL
            details[0] = "YOUTUBE";
            details[1] = jsonObject.get("snippet").getAsJsonObject().get("title").getAsString();
            details[2] = jsonObject.get("snippet").getAsJsonObject().get("channelTitle").getAsString();
            details[3] = "https://www.youtube.com/watch?v=" + jsonObject.get("id").getAsJsonObject().get("videoId").getAsString();

            System.out.println(Arrays.toString(details));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return details;
    }

    public static void parseYoutubePlaylist(String url, SlashCommandInteractionEvent event) {
        try {
            URI localurl = new URI(url);
            String query = localurl.getQuery();

            String listQuery = query.split("&")[0];
            if (!listQuery.contains("list=")) {
                throw new MalformedURLException(url + " is not a valid youtube playlist URL");
            }

            String playlistID = listQuery.substring(5);
            ArrayList<String> songs = getPlaylistPage(playlistID, "");

            DatabaseWrapper db = new DatabaseWrapper();
            for (String song : songs) {
               try {
                   song = "https://www.youtube.com/watch?v=" + song;
                   String[] details = parseYoutube(song);
                   db.addSong(Objects.requireNonNull(event.getGuild()).getId(), details[0], details[1], details[2], details[3]);
               } catch (Exception e) {
                   logger.info(e.getMessage());
               }
            }

            MessageWrapper.genericResponse(event, "Playlist Added", "Added " + songs.size() + " songs to the queue.");
        } catch (URISyntaxException | MalformedURLException | DBConnectionException e) {
            logger.info(e.getMessage());
        }
    }

    public static void parseYoutubePlaylist(String url, ModalInteractionEvent event) {
        try {
            URI localurl = new URI(url);
            String query = localurl.getQuery();

            String listQuery = query.split("&")[0];
            if (!listQuery.contains("list=")) {
                throw new MalformedURLException(url + " is not a valid youtube playlist URL");
            }

            String playlistID = listQuery.substring(5);
            ArrayList<String> songs = getPlaylistPage(playlistID, "");

            DatabaseWrapper db = new DatabaseWrapper();
            for (String song : songs) {
                try {
                    song = "https://www.youtube.com/watch?v=" + song;
                    String[] details = parseYoutube(song);
                    db.addSong(event.getGuild().getId(), details[0], details[1], details[2], details[3]);
                } catch (DBConnectionException e) {
                    throw new RuntimeException(e);
                }
            }

            MessageWrapper.genericResponse(event, "Playlist Added", "Added " + songs.size() + " songs to the queue.");
        } catch (URISyntaxException | MalformedURLException | DBConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<String> getPlaylistPage(String playlistID, String pageToken) {
        try {
            Logger logger = Logger.getLogger("orion");
            logger.info("Getting songs from playlist with ID: " + playlistID);
            URL localurl = new URL("https://youtube.googleapis.com/youtube/v3/playlistItems?part=contentDetails&maxResults=50&playlistId=" + URLEncoder.encode(playlistID, StandardCharsets.UTF_8) + "&pageToken=" + pageToken + "&key=" + youtubeKey);
            HttpURLConnection httpURLConnection = (HttpURLConnection) localurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            JsonObject jsonObject = getJsonObject(localurl);

            ArrayList<String> songs = new ArrayList<>();
            System.out.println(jsonObject.toString());
            JsonArray jsonArray = jsonObject.get("items").getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                logger.info("Adding song " + i + " to the list");
                JsonObject playlist = jsonArray.get(i).getAsJsonObject();
                songs.add(playlist.get("contentDetails").getAsJsonObject().get("videoId").getAsString());
            }

            if (jsonObject.has("nextPageToken")) {
                ArrayList<String> nextSongs = getPlaylistPage(playlistID, jsonObject.get("nextPageToken").getAsString());
                songs.addAll(nextSongs);
            }

            return songs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] parseYoutube(String url) throws MalformedURLException {
        // TODO: if contins playlist popup choice?

        try {
            URL localurl = new URL("https://noembed.com/embed?url=" + url);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        try {
            logger.info("Parsing Youtube URL: " + url);
            URL localurl = new URL("https://noembed.com/embed?url=" + url);

            HttpURLConnection httpURLConnection = (HttpURLConnection) localurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            JsonObject jsonObject = getJsonObject(localurl);

            String title = jsonObject.get("title").getAsString();
            url = jsonObject.get("url").getAsString();
            String artist = jsonObject.get("author_name").getAsString();

            String[] details = new String[4];
            details[0] = "YOUTUBE";
            details[1] = title;
            details[2] = artist;
            details[3] = url;

            return details;

        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new MalformedURLException(url + " is not a valid youtube URL");
        }
    }

    public static String getTitle(String url) {
        // TODO: if contins playlist popup choice?

        try {
            URL localurl = new URL("https://noembed.com/embed?url=" + url);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        try {
            logger.info("Parsing Youtube URL: " + url);
            URL localurl = new URL("https://noembed.com/embed?url=" + url);

            HttpURLConnection httpURLConnection = (HttpURLConnection) localurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            JsonObject jsonObject = getJsonObject(localurl);

            return jsonObject.get("title").getAsString();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return url;
        }
        
    }

    private static JsonArray getJsonArray(URL localurl) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(localurl.openStream());

        stringBuilder.append(scanner.nextLine());
        scanner.close();

        JsonParser jsonParser = new JsonParser();
        JsonObject dataObject = (JsonObject) jsonParser.parse(String.valueOf(stringBuilder));

        return (JsonArray) dataObject.get("collection");
    }

    static JsonObject getJsonObject(URL localurl) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(localurl.openStream());

        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

        JsonParser jsonParser = new JsonParser();
        JsonObject dataObject = (JsonObject) jsonParser.parse(String.valueOf(stringBuilder));

        return dataObject;
    }

}
