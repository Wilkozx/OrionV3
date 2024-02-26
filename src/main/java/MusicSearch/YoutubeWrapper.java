package MusicSearch;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class YoutubeWrapper {
    static Dotenv dotenv = Dotenv.load();
    static Logger logger = Logger.getLogger("orion");
    static String youtubeKey = dotenv.get("YOUTUBEKEY");
    static String youtubeID = dotenv.get("YOUTUBEID");
    static String youtubeSecret = dotenv.get("YOUTUBESECRET");

    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    // Load client secrets from a file
    // Set up authorization code flow

    public static String[] searchYoutube(String searchTerm) {
        String[] details = new String[4];
        try {
            URL localurl = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8) + "&key=" + youtubeKey);
            HttpURLConnection httpURLConnection = (HttpURLConnection) localurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            JsonObject jsonObject = getJsonObject(localurl);

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

    public static String[] parseYoutube(String url) throws MalformedURLException {

        // TODO: add if statement to check if the url is a playlist or a video
        //  add playlist check

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
        dataObject = dataObject.get("items").getAsJsonArray().get(0).getAsJsonObject();

        return dataObject;
    }

}
