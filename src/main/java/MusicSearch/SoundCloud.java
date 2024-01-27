package MusicSearch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class SoundCloud {

    static Logger logger = Logger.getLogger("orion");
    static Dotenv dotenv = Dotenv.load();
    static String SoundcloudID = dotenv.get("SOUNDCLOUDID");

    public static String[] searchSoundCloud(String searchTerm) {
        String[] details = new String[4];

        try {
            URL localurl = new URL("https://api-v2.soundcloud.com/search/tracks?q=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8) + "&limit=5&client_id=" + SoundcloudID);
            HttpURLConnection httpURLConnection = (HttpURLConnection) localurl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            JsonArray jsonArray = getJsonObject(localurl);

            int timesSkipped = 0;
            int i = 0;
            Map<String, String> skippedTracks = new HashMap<>(4);

            while (i < 4) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                if(!jsonObject.get("policy").toString().contains("SNIP")) {
                    String title = jsonObject.get("title").getAsString();
                    String url = jsonObject.get("permalink_url").getAsString();
                    String artist = jsonObject.get("user").getAsJsonObject().get("username").toString();
                    // 0 - songID
                    // 1 - songTitle
                    // 2 - Artist (here replaced with title as soundcloud doesn't have an artist option)
                    // 3 - URL
                    System.out.println(jsonObject.get("policy").toString());
                    details[0] = "null";
                    details[1] = title;
                    details[2] = artist;
                    details[3] = url;
                    System.out.println("found " + url + " " + title);
                    i = 4;
                } else {
                    String tempTitle = jsonObject.get("title").getAsString();
                    String tempArtist = jsonObject.get("user").getAsJsonObject().get("username").toString();
                    System.out.println(jsonObject.get("policy").toString());
                    skippedTracks.put(tempTitle, tempArtist);
                    timesSkipped++;
                    i++;

                }
            }

            logger.info("Tracks skipped " + timesSkipped);

        } catch (IOException e) {throw new RuntimeException(e);}

        return details;
    }


    private static JsonArray getJsonObject(URL localurl) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(localurl.openStream());

        stringBuilder.append(scanner.nextLine());
        scanner.close();

        JsonParser jsonParser = new JsonParser();
        JsonObject dataObject = (JsonObject) jsonParser.parse(String.valueOf(stringBuilder));

        return (JsonArray) dataObject.get("collection");
    }


}