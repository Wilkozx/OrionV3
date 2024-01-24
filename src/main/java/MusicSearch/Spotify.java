package MusicSearch;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public class Spotify {
    static Dotenv dotenv = Dotenv.load();
    static Logger logger = Logger.getLogger("orion");
    static String SpotifyID = dotenv.get("SPOTIFYID");
    static String SpotifySecret = dotenv.get("SPOTIFYSECRET");
    static URI uri;

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(SpotifyID)
            .setClientSecret(SpotifySecret)
            .setRedirectUri(uri)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi
            .clientCredentials()
            .build();

    public Spotify() {
    }

    public static String SpotifySearch(String searchTerm) {
        String url = "null";
        String songName= "null";
        SearchTracksRequest searchRequest = spotifyApi.searchTracks(searchTerm)
                .limit(1)
                .offset(0)
                .build();

        try {
            final Paging<Track> trackPaging = searchRequest.execute();
            url = String.valueOf(new URI(Arrays.stream(trackPaging.getItems()).findFirst().get().getId()));
            songName = Arrays.stream(trackPaging.getItems()).findFirst().get().getName();
            logger.info("Grabbed URL: " + url);
        } catch (URISyntaxException | IOException | SpotifyWebApiException | ParseException e) {
            logger.warning("Error " + e.getMessage());
        }
        return url + " - " + songName;
    }

    public static void updateAccessToken() {
        logger.info("Updating Spotify API Token: " + new Date());

        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            logger.info("Received Access. Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.warning("Error: " + e.getMessage());
        }
    }



}
