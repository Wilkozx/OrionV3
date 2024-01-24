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
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

    public static String[] SpotifySearch(String searchTerm) {

        String[] details = new String[4];

        SearchTracksRequest searchRequest = spotifyApi.searchTracks(searchTerm)
                .limit(1)
                .offset(0)
                .build();

        try {
            // 0 - songID
            // 1 - songTitle
            // 2 - Artist
            // 3 - URL
            final Paging<Track> trackPaging = searchRequest.execute();
            details[0] = Arrays.stream(trackPaging.getItems()).findFirst().get().getId();
            details[1] = Arrays.stream(trackPaging.getItems()).findFirst().get().getName();
            details[2] = Arrays.stream(Arrays.stream(trackPaging.getItems()).findFirst().get().getArtists()).findFirst().get().getName();
            details[3] = "https://open.spotify.com/track/" + details[0];
            logger.info("Found song with details: \n" + Arrays.toString(details));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.warning("Error " + e.getMessage());
        }

        return details;
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
