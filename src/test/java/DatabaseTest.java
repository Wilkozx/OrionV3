import static org.junit.Assert.*;
import static org.junit.Test.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Rule;

import com.mongodb.client.MongoCollection;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import Wrapper.DatabaseWrapper;
import junit.framework.TestCase;

public class DatabaseTest extends TestCase{

    @Test
    public void testdbConnect() {
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            assert(db != null);
        } catch (DBConnectionException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testdbGetQueueA() {
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            ArrayList<Document> result = db.getQueue("446352184351653919");
            System.out.println(result);
            assert(result != null);
        } catch (DBConnectionException e) {
            assert(false);
        } catch (DBEmptyQueueException e) {
            assert(false);
        }
    }

    @Test
    public void testdbGetQueueB() {
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            ArrayList<Document> result = db.getQueue("1");
            System.out.println(result);
            assert(false);
        } catch (DBConnectionException e) {
            assert(false);
        } catch (DBEmptyQueueException e) {
            assert(true);
        }
    }

    @Test
    public void testdbGetQueueC() {
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            ArrayList<Document> result = db.getQueue("0");
            System.out.println(result);
            assert(false);
        } catch (DBConnectionException e) {
            fail(e.getMessage());
        } catch (DBEmptyQueueException e) {
            assert(true);
        }
    }


    // Add more test methods as needed

}
