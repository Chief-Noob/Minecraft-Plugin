package com.linyuanlin.minecraft.mongodb;

import com.linyuanlin.minecraft.App;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongodbClient {
    public FindIterable<Document> cursor;
    private App app;
    private MongoCollection<Document> collection;

    public MongodbClient(App app, String collectionName) {
        try {
            this.app = app;
            new MongoClient();
            MongoClient mongoClient = new MongoClient(new MongoClientURI(this.app.mongodbConnectString));
            MongoDatabase database = mongoClient.getDatabase("Minecraft");
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

    public void insert(Document dbObject) {
        try {
            this.collection.insertOne(dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateById(String objectId, Document dbObject) {
        try {
            this.collection.updateOne(new BasicDBObject("_id", objectId), dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String objectId) {
        try {
            this.collection.deleteOne(new BasicDBObject("_id", objectId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
