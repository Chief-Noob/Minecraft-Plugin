package com.linyuanlin.minecraft.mongodb;

import com.linyuanlin.minecraft.App;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongodbClient {
    public FindIterable<Document> cursor;
    private App app;
    private MongoClient client;
    private MongoDatabase database;

    public MongodbClient(App app, String databaseName) {
        try {
            this.app = app;
            this.client = MongoClients.create(this.app.mongodbConnectString);
            this.database = this.client.getDatabase(databaseName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.client.close();
    }

    public void getById(String collectionName, String id) {
        try {
            this.cursor = this.database.getCollection(collectionName).find(new BasicDBObject("_id", id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Document findOne(String collectionName, String fieldName, String value) {
        try {
            this.cursor = this.database.getCollection(collectionName)
                    .find(new BasicDBObject(fieldName, value));
            return cursor.explain();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(String collectionName, Document dbObject) {
        try {
            this.database.getCollection(collectionName).insertOne(dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateById(String collectionName, String objectId, Document dbObject) {
        try {
            this.database.getCollection(collectionName).updateOne(new BasicDBObject("_id", objectId), dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String collectionName, String objectId) {
        try {
            this.database.getCollection(collectionName).deleteOne(new BasicDBObject("_id", objectId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
