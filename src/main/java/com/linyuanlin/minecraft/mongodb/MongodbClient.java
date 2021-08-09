package com.linyuanlin.minecraft.mongodb;

import com.linyuanlin.minecraft.mongodb.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bukkit.plugin.java.JavaPlugin.*;

public class MongodbClient {
  private DB database;

  private DBCollection collection;

  public DBCursor cursor;

  public MongodbClient(String collectionName) {
    try {
      MongoClient mongoClient = new MongoClient(
        new MongoClientURI("<connect string>")
      );

      this.database = mongoClient.getDB("<database name>");
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
