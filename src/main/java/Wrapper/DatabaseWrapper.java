package Wrapper;

import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseWrapper {
    private String username = Dotenv.load().get("MongoUser");
    private String password = Dotenv.load().get("MongoPass");
    private String dbConnectionString = "mongodb+srv://" + username + ":" + password + "@orionmongo.wnlq83d.mongodb.net/?retryWrites=true&w=majority";

    public DatabaseWrapper() {
        Logger logger = Logger.getLogger("orion");
        logger.info("Connecting to MongoDB...");

        ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(dbConnectionString))
            .serverApi(serverApi)
            .build();
        
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                logger.info("Ping Successful - You are connected to MongoDB!");
            } catch (MongoException e) {
                logger.warning("Error connecting to MongoDB: " + e.getMessage());;
            }
        }
    }

}
