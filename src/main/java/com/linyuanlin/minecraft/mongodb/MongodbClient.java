package com.linyuanlin.minecraft.mongodb;

import com.linyuanlin.minecraft.App;
import com.mongodb.*;

import java.util.logging.Level;

public class MongodbClient {
    public DBCursor cursor;
    private DB database;
    private App app;
    private DBCollection collection;

    public MongodbClient(App app, String collectionName) {
        try {
            this.app = app;
            MongoClient mongoClient = new MongoClient(
                    new MongoClientURI(this.app.mongodbConnectString)
            );

            app.getLogger().log(Level.INFO, this.app.mongodbConnectString);

            this.database = mongoClient.getDB("Minecraft");
            this.collection = database.getCollection(collectionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getById(String playerId) {
        try {
            this.cursor = this.collection.find(new BasicDBObject("_id", playerId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert(DBObject dbObject) {
        try {
            this.collection.insert(dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateById(String objectId, DBObject dbObject) {
        try {
            this.collection.update(new BasicDBObject("_id", objectId), dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String objectId) {
        try {
            this.collection.remove(new BasicDBObject("_id", objectId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
