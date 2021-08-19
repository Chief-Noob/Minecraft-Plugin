package com.linyuanlin.minecraft.mongodb;

import com.linyuanlin.minecraft.App;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

public class MongodbClient {
    public FindIterable<Document> cursor;
    private MongoClient client;
    private MongoDatabase database;

    public MongodbClient(String databaseName) {
        try {
            this.client = MongoClients.create(App.getPlugin().mongodbConnectString);
            this.database = this.client.getDatabase(databaseName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.client.close();
    }

    public Document findOne(String collectionName, String fieldName, String value) {
        try {
            this.cursor = this.database.getCollection(collectionName).find(new BasicDBObject(fieldName, value));
            return cursor.first();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Document> findMany(String collectionName, Bson condition) {
        List<Document> res = new ArrayList<>();
        try {
            this.cursor = this.database.getCollection(collectionName).find(condition);
            cursor.forEach(res::add);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public void insert(String collectionName, Document dbObject) {
        try {
            this.database.getCollection(collectionName).insertOne(dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Replace a document which match the condition with new document
    public void replaceOne(String collectionName, Bson condition, Document dbObject) {
        try {
            database.getCollection(collectionName).replaceOne(condition, dbObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
