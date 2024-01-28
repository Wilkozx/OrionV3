package Wrapper;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

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

    //RETURNS AN ARRAY LIST OF DOCUMENTS IF THE QUEUE EXISTS AND THROWS A DBCONNECTIONEXCEPTION IF THE QUEUE DOES NOT EXIST
    public ArrayList<Document> getQueue(String guildID) throws DBConnectionException, DBEmptyQueueException{
        logger.info("Attempting to get Queue for guild: " + guildID);
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection collection = database.getCollection("queue");
                Document result;
                ArrayList<Document> queue;
                try {
                    result = (Document) collection.find(new Document("guildID", guildID)).first();
                    queue = (ArrayList<Document>) result.get("queue");
                    if (queue.get(0).isEmpty()) {
                        throw new DBEmptyQueueException("Queue is empty or does not exist.");
                    }
                    logger.info("Success! Got Queue for guild: " + guildID);
                    return queue;
                } catch (Exception e) {
                    throw new DBEmptyQueueException("Queue is empty or does not exist.");
                }
            } catch (DBEmptyQueueException e) {
                logger.info(guildID + " does not have a queue, you should create one.");
                throw new DBEmptyQueueException("Queue is empty or does not exist.");
            } catch (Exception e) {
                logger.warning("Error getting Queue: " + e.getMessage());
                throw new DBConnectionException("Error accessing the queue collection: \n" + e.getMessage() + "\nPlease check your MongoDB database.");
            }
        }
    }

    public void createQueue(String guildID, String platform, String songID, String songTitle, String artist, String url) throws DBConnectionException{
        logger.info("Attempting to create Queue for guild: " + guildID);
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Document newSong = new Document("platform", platform)
                                        .append("songID", songID)
                                        .append("songTitle", songTitle)
                                        .append("artist", artist)
                                        .append("url", url);

                collection.deleteMany(new Document("guildID", guildID));
                collection.insertOne(new Document("guildID", guildID)
                                          .append("queue", Arrays.asList(newSong)));
                logger.info("Success! Created Queue for guild: " + guildID);
            } catch (MongoException e) {
                logger.warning("Error creating Queue: " + e.getMessage());
                throw new DBConnectionException("Error accessing the queue collection: \n" + e.getMessage() + "\nPlease check your MongoDB database.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addSong(String guildID, String platform, String songID, String songTitle, String artist, String url) throws DBConnectionException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to add song with ID: " + songID + " to queue for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Document newSong = new Document("platform", platform)
                                        .append("songID", songID)
                                        .append("songTitle", songTitle)
                                        .append("artist", artist)
                                        .append("url", url);

                collection.updateOne(Filters.eq("guildID", guildID), Updates.push("queue", newSong));
                logger.info("Success! Added song with ID: " + songID + " to queue for guild: " + guildID);
            } catch (MongoException e) {
                logger.warning("Error adding song to Queue: " + e.getMessage());
                throw new DBConnectionException("Error accessing the queue collection: \n" + e.getMessage() + "\nPlease check your MongoDB database.");
            }
        }
    }

    public Document popNextSong(String guildID) throws DBEmptyQueueException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to pop next song from queue for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                ArrayList<Document> queue = (ArrayList<Document>) result.get("queue");

                Document song = (Document) queue.get(0);

                Document updateQuery = new Document("$pull", new Document("queue", song));
                collection.updateOne(guildQuery, updateQuery);

                logger.info(guildID + " queue modified, removed: " + song.get("songTitle"));
                return song;
            } catch(Exception e) {
                throw new DBEmptyQueueException("Queue is empty or does not exist: \n " + e.getMessage());
            }
        }
    }

    public Document peekNextSong(String guildID) throws DBEmptyQueueException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to peek at the next song from queue for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                ArrayList<Document> queue = (ArrayList<Document>) result.get("queue");
                logger.info("Success! Peeked song from queue for guild: " + guildID);
                return (Document) queue.get(0);
            } catch(Exception e) {
                throw new DBEmptyQueueException("Queue is empty or does not exist: \n " + e.getMessage());
            }
        }
    }

    public Document removeSong(String guildID, int index) throws DBEmptyQueueException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to pop next song from queue for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                ArrayList<Document> queue = (ArrayList<Document>) result.get("queue");

                Document song = (Document) queue.get(index - 1);

                Document updateQuery = new Document("$pull", new Document("queue", song));
                collection.updateOne(guildQuery, updateQuery);

                logger.info(guildID + " queue modified, removed: " + song.get("songTitle"));
                return song;
            } catch(Exception e) {
                throw new DBEmptyQueueException("Queue is empty or does not exist: \n " + e.getMessage());
            }
        }
    }
}