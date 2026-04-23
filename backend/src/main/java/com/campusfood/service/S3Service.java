package com.campusfood.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing image uploads and deletions on AWS S3.
 * Validates file types and sizes before uploading.
 * Generates unique filenames to avoid collisions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB

    /**
     * Uploads an image file to S3 under a categorized folder path.
     *
     * @param file     the multipart file to upload
     * @param folder   the S3 folder prefix (e.g., "products", "users")
     * @return the public URL of the uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String key = folder + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = buildPublicUrl(key);
            log.info("Image uploaded to S3: {}", imageUrl);
            return imageUrl;
        } catch (S3Exception e) {
            log.error("S3 upload failed: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload image to S3: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            log.error("IO error during S3 upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read image file for upload");
        }
    }

    /**
     * Deletes an image from S3 by its full URL.
     *
     * @param imageUrl the full public URL of the image to delete
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String key = extractKeyFromUrl(imageUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Image deleted from S3: {}", key);
        } catch (S3Exception e) {
            log.error("S3 delete failed: {}", e.awsErrorDetails().errorMessage(), e);
            // Non-critical — log but don't throw
        }
    }

    /**
     * Validates file type and size constraints.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 15MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, WebP, GIF"
            );
        }
    }

    /**
     * Extracts the S3 object key from a full public URL.
     */
    private String extractKeyFromUrl(String imageUrl) {
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        // Fallback: try to extract from path-style URL
        String pathPrefix = String.format("https://s3.%s.amazonaws.com/%s/", region, bucketName);
        if (imageUrl.startsWith(pathPrefix)) {
            return imageUrl.substring(pathPrefix.length());
        }
        throw new IllegalArgumentException("Cannot extract S3 key from URL: " + imageUrl);
    }

    /**
     * Builds the public S3 URL for a given object key.
     */
    private String buildPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Extracts the file extension from a filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // default
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
