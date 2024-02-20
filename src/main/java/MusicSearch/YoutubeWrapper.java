package MusicSearch;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static MusicSearch.SoundCloudWrapper.getJsonObject;

public class YoutubeWrapper {
    static Dotenv dotenv = Dotenv.load();
    static Logger logger = Logger.getLogger("orion");
    static String[] details;

    public static String[] parseYoutube(String url) throws MalformedURLException {
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

}
