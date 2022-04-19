package me.rohank05.utilities.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class MongoDBMethod {
    private static final MongoConnection dbClient = new MongoConnection();
    private static final DB db = dbClient.getDbClient().getDB("KAAMusic");
    private static final DBCollection guildSettings = db.getCollection("guildSettings");

    public static DBObject getGuildSettings(Long guildId) {
        DBObject query = new BasicDBObject("guildId", String.valueOf(guildId));
        return guildSettings.findOne(query);
    }

    public static void initGuildSettings(Long guildId){
        List<String> djRoles = new ArrayList<>();
        DBObject newGuildSettings = new BasicDBObject("guildId", String.valueOf(guildId)).append("24/7", true).append("dj", djRoles);
        guildSettings.insert(newGuildSettings);
    }

    public static void set247(Long guildId, Boolean tfs){
        DBObject query = new BasicDBObject("guildId", String.valueOf(guildId));
        DBObject update = new BasicDBObject("24/7", tfs);
        guildSettings.update(query, update);
    }
}
