package com.pablolafontaine.imageboard.service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Sorts.descending;

@Service
public class DatabaseService {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Value("${mongo.collection.images}")
    private String imagesCollectionName;
    
    @Async
    public Future<String> addImage(Document doc) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(imagesCollectionName);
        collection.insertOne(doc);
        return CompletableFuture.completedFuture(doc.getObjectId("_id").toString());
    }

    @Async
    public Future<Document> getImage(String id){
        MongoCollection<Document> collection = mongoTemplate.getCollection(imagesCollectionName);
        return CompletableFuture.completedFuture(collection.find(Filters.eq("_id", new ObjectId(id))).first());
    }

    @Async
    public Future<ArrayList<Document>> getPage(int page){
        MongoCollection<Document> collection = mongoTemplate.getCollection(imagesCollectionName);
        MongoCursor<Document> cursor = collection.find().limit(10).skip((page-1)*10).sort(descending("date")).cursor();
        ArrayList<Document> result = new ArrayList<>();
        cursor.forEachRemaining(result::add);
        return CompletableFuture.completedFuture(result);
    }
}