package com.pablolafontaine.imageboard.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;



@Configuration
public class CredentialsConfig {

    @Value("${bucket.secret.access.key}")
    private String awsSecretKey;

    @Value("${bucket.access.key}")
    private String awsAccessKey;

    @Value("${bucket.region}")
    private String awsRegion;
@Bean
public S3Client s3Client(){
    AwsCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);        
    return S3Client.builder().region(Region.of(awsRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
}
}