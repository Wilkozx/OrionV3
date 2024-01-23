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

import Errors.DBConnectionException;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseWrapper {
    private final String username = Dotenv.load().get("MongoUser");
    private final String password = Dotenv.load().get("MongoPass");
    private final String dbConnectionString = "mongodb+srv://" + username + ":" + password + "@orionmongo.wnlq83d.mongodb.net/?retryWrites=true&w=majority";
    private ServerApi serverApi;
    private MongoClientSettings settings;
    private Logger logger = Logger.getLogger("orion");


    public DatabaseWrapper() throws DBConnectionException{
        logger.info("Connecting to MongoDB...");

        this.serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

        this.settings = MongoClientSettings.builder()
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
                throw new DBConnectionException("Error connecting to MongoDB: \n" + e.getMessage() + "\nPlease check your MongoDB connection string.");
            }
        }
    }

    public void setDatabase(String db) {

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
