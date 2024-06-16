package com.pablolafontaine.imageboard.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class UploadService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private DatabaseService databaseService;

    @Value("${cdn.url}")
    private String cdnUrl;

    @Async
    public CompletableFuture<String> uploadImage(MultipartFile file, String title, String text) throws IOException, InterruptedException, ExecutionException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null){
            throw new IllegalArgumentException("Filename error");
        }
        
        int i = originalFileName.lastIndexOf('.');
        if(i == -1){
            throw new IllegalArgumentException("File extension not found");
        }


        String fileExtension = originalFileName.substring(i, originalFileName.length()).toLowerCase();
        if(!getAllowedFileExtensions().contains(fileExtension)){
            throw new IllegalArgumentException("File type not supported");
        }
        String newFileName = gridFsTemplate.store(file.getInputStream(), originalFileName, file.getContentType()).toString() + fileExtension;
        String cdnFilePath = cdnUrl + "/" + newFileName;

        uploadToS3Bucket(newFileName, file.getInputStream());
        
        Document metadata = new Document();
        metadata.append("img_path", cdnFilePath);
        metadata.append("title", title);
        metadata.append("text", text);
        metadata.append("original_filename", originalFileName);
        metadata.append("date", System.currentTimeMillis());
        String postId = databaseService.addImage(metadata).get();
        if(postId == null){
            throw new IOException("Couldn't add document to database");
        }
        return CompletableFuture.completedFuture(postId);
    }

    @Value("${bucket.name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;
    
    
    private void uploadToS3Bucket(String fileName, InputStream inputStream)
            throws S3Exception, AwsServiceException, SdkClientException, IOException {
         
        PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build();
         
                            s3Client.putObject(request,
                RequestBody.fromInputStream(inputStream, inputStream.available()));
    }

    private Set<String> getAllowedFileExtensions(){  
        return Set.of(".jpg", ".jpeg", ".png");
    }

}

