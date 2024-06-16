package com.pablolafontaine.imageboard.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@Service
public class ViewService {

    @Autowired
    private DatabaseService databaseService;

    @Async
    public Future<ResponseEntity<String>> viewImage(String id) {
        Document document;
        try {
            document = databaseService.getImage(id).get();
        } catch (InterruptedException | ExecutionException e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
        }
        if (document == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found"));
        }
        return CompletableFuture.completedFuture(ResponseEntity.ok(document.toJson()));
    }

    @Async
    public Future<ResponseEntity<String>> viewPage(int page) {
        try {
            ArrayList<Document> documents = databaseService.getPage(page).get();
            if (documents == null) {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database failed to load"));
            }
            Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
    
            return CompletableFuture.completedFuture(ResponseEntity.ok(gson.toJson(documents)));
        } catch (InterruptedException | ExecutionException e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch page from database: " +e.getMessage()));
        }
       
    }

}
class ObjectIdTypeAdapter extends TypeAdapter<ObjectId> {
    @Override
    public void write(final JsonWriter out, final ObjectId value) throws IOException {
        out.beginObject()
           .name("$oid")
           .value(value.toString())
           .endObject();
    }

    @Override
    public ObjectId read(final JsonReader in) throws IOException {
        in.beginObject();
        assert "$oid".equals(in.nextName());
        String objectId = in.nextString();
        in.endObject();
        return new ObjectId(objectId);
    }
}