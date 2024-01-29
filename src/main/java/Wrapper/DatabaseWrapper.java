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

import javax.print.Doc;

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

    public void initGuild(String guildID) throws DBConnectionException {
        logger.info("Attempting to init guild: " + guildID);
        if (guildExists(guildID)) {
            logger.info("Guild already exists: " + guildID);
            return;
        }
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Document defaultSettings = new Document("volume", 100)
                                                .append("loop", false)
                                                .append("shuffle", false)
                                                .append("repeat", false)
                                                .append("defaultPlatform", "SOUNDCLOUD")
                                                .append("status", "DEFAULT");
                Document guild = new Document("guildID", guildID)
                                      .append("queue", Arrays.asList(new Document()))
                                      .append("nowPlaying", new Document())
                                      .append("settings", defaultSettings)
                                      .append("activeChannel", "");

                collection.insertOne(guild);
                logger.info("Success! Init guild: " + guildID);
            } catch (MongoException e) {
                logger.warning("Error initing guild: " + e.getMessage());
                throw new DBConnectionException("Error accessing the queue collection: \n" + e.getMessage() + "\nPlease check your MongoDB database.");
            }
        }
    }

    public boolean guildExists(String guildID) throws DBConnectionException {
        logger.info("Checking if guild exists: " + guildID);
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection collection = database.getCollection("queue");

                Document result =(Document) collection.find(new Document("guildID", guildID)).first();

                if (result.get("guildID").equals(guildID)) {
                    logger.info("Guild exists: " + guildID);
                    return true;
                } else {
                    logger.info("Guild does not exist: " + guildID);
                    return false;
                }
            } catch (Exception e) {
                logger.info("Guild does not exist, creating now...");
                return false;
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

    public void createQueue(String guildID, String platform, String songTitle, String artist, String url) throws DBConnectionException{
        logger.info("Attempting to create Queue for guild: " + guildID);
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Document newSong = new Document("platform", platform)
                                        .append("songTitle", songTitle)
                                        .append("artist", artist)
                                        .append("url", url);

                collection.updateOne(Filters.eq("guildID", guildID), Updates.unset("queue"));
                collection.updateOne(Filters.eq("guildID", guildID), Updates.set("queue", Arrays.asList(newSong)));
                logger.info("Success! Created Queue for guild: " + guildID);
                logger.info("Added song " + songTitle + " with URL: " + url);
            } catch (MongoException e) {
                logger.warning("Error creating Queue: " + e.getMessage());
                throw new DBConnectionException("Error accessing the queue collection: \n" + e.getMessage() + "\nPlease check your MongoDB database.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addSong(String guildID, String platform, String songTitle, String artist, String url) throws DBConnectionException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to add song with ID: " + url + " to queue for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Document newSong = new Document("platform", platform)
                                        .append("songTitle", songTitle)
                                        .append("artist", artist)
                                        .append("url", url);

                collection.updateOne(Filters.eq("guildID", guildID), Updates.push("queue", newSong));
                logger.info("Success! Added song with URL: " + url + " to queue for guild: " + guildID);
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

    public void setActiveChannel(String guildID, String channelID) {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to set active channel to: " + channelID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("activeChannel", channelID)).getModifiedCount();
                logger.info("Success! set active channel for guild " + channelID + " to " + channelID + ", rows modified: " + modCount);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getActiveChannel(String guildID) throws DBEmptyQueueException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to get active channel for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                String activeChannel = (String) result.get("activeChannel");
                logger.info("Success! Got active channel for guild: " + guildID);
                return activeChannel;
            } catch(Exception e) {
                throw new DBEmptyQueueException("Queue is empty or does not exist: \n " + e.getMessage());
            }
        }
    }

    public void setNowPlaying(String guildID, Document song) {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to set now playing for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("nowPlaying", song.append("paused", false))).getModifiedCount();
                logger.info("Success! Set now playing for guild " + guildID + " to song " + song.get("songTitle") + ", rows modified: " + modCount);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Document getNowPlaying(String guildID) throws DBEmptyQueueException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to get now playing for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                Document nowPlaying = (Document) result.get("nowPlaying");
                logger.info("Success! Got now playing for guild: " + guildID);
                return nowPlaying;
            } catch(Exception e) {
                throw new DBEmptyQueueException("Queue is empty or does not exist: \n " + e.getMessage());
            }
        }
    }

    public void pauseSong(String guildID) {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to pause song for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("nowPlaying.paused", true)).getModifiedCount();
                logger.info("Success! Paused song for guild " + guildID + ", rows modified: " + modCount);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resumeSong(String guildID) {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to resume song for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("nowPlaying.paused", false)).getModifiedCount();
                logger.info("Success! Resumed song for guild " + guildID + ", rows modified: " + modCount);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setVolume(String guildID, int volume) {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to set volume for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("settings.volume", volume)).getModifiedCount();
                logger.info("Success! Set volume for guild " + guildID + " to " + volume + ", rows modified: " + modCount);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean loopToggle(String guildID) throws DBConnectionException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to set loop for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Boolean loop = (Boolean) collection.find(new Document("guildID", guildID)).first().get("settings.loop");

                loop = !loop;
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("settings.loop", loop)).getModifiedCount();
                logger.info("Success! Set loop for guild " + guildID + " to " + loop + ", rows modified: " + modCount);
                return loop;
            } catch(Exception e) {
                throw new DBConnectionException(e.getMessage());
            }
        }
    }

    public boolean shuffleToggle(String guildID) throws DBConnectionException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to set shuffle for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");

                Boolean shuffle = (Boolean) collection.find(new Document("guildID", guildID)).first().get("settings.shuffle");

                shuffle = !shuffle;
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("settings.shuffle", shuffle)).getModifiedCount();
                logger.info("Success! Set shuffle for guild " + guildID + " to " + shuffle + ", rows modified: " + modCount);
                return shuffle;
            } catch(Exception e) {
                throw new DBConnectionException(e.getMessage());
            }
        }
    }

    public void setDefaultPlatform(String guildID, String platform) {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to set default platform for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                long modCount = collection.updateOne(Filters.eq("guildID", guildID), Updates.set("settings.defaultPlatform", platform)).getModifiedCount();
                logger.info("Success! Set default platform for guild " + guildID + " to " + platform + ", rows modified: " + modCount);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getDefaultPlatform(String guildID) throws DBConnectionException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to get default platform for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                String defaultPlatform = (String) result.get("settings.defaultPlatform");
                logger.info("Success! Got default platform for guild: " + guildID);
                return defaultPlatform;
            } catch(Exception e) {
                throw new DBConnectionException(e.getMessage());
            }
        }
    }

    public Document getSettings(String guildID) throws DBConnectionException {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                logger.info("Attempting to get settings for guild: " + guildID);
                MongoDatabase database = mongoClient.getDatabase("guilds");
                MongoCollection<Document> collection = database.getCollection("queue");
                
                Document guildQuery = new Document("guildID", guildID);
                Document result = collection.find(guildQuery).first();

                Document settings = (Document) result.get("settings");
                logger.info("Success! Got settings for guild: " + guildID);
                return settings;
            } catch(Exception e) {
                throw new DBConnectionException(e.getMessage());
            }
        }
    }
}