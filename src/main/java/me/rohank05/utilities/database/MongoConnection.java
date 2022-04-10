package me.rohank05.utilities.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.net.UnknownHostException;

public class MongoConnection {
    private MongoClient dbClient;
    public MongoConnection() {
        try {
            this.dbClient = setUpConnection();
        }catch (UnknownHostException e){
            e.printStackTrace();
            this.dbClient = null;
        }

    }
    private MongoClient setUpConnection() throws UnknownHostException{
         return new MongoClient(new MongoClientURI("mongodb://localhost:27017/Beat"));
    }

    public MongoClient getDbClient() {
        return dbClient;
    }
}
