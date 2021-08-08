package com.mongodb;

import com.mongodb.*;
import com.mongodb.MongoClient;
import org.bukkit.plugin.java.JavaPlugin.*;

public class MongodbClient {
  private DB database;
  private DBCollection collection;
  public DBCursor cursor;

  public MongodbClient() {
    try {
      MongoClient mongoClient = new MongoClient(
        new MongoClientURI(
          "mongodb+srv://Swarz:<password>@cluster0.vbdvc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"
        )
      );

      this.database = mongoClient.getDB("<database name>");
      this.collection = database.getCollection("<collection name>");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void getPlayerById(String playerId) {
    try {
      this.cursor = this.collection.find(new BasicDBObject("_id", playerId));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
