package com.campusfood.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures the AWS S3 client bean used for image storage operations.
 * Reads credentials and region from application.properties.
 */
@Slf4j
@Configuration
public class S3Config {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            log.error("AWS credentials are not configured. Please set AWS_ACCESS_KEY and AWS_SECRET_KEY environment variables.");
            throw new IllegalStateException("AWS credentials are required for S3 operations. Please configure AWS_ACCESS_KEY and AWS_SECRET_KEY.");
        }
        
        log.info("Initializing S3 client with configured credentials for region: {}", region);
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
