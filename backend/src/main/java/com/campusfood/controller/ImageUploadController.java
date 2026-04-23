package com.campusfood.controller;

import com.campusfood.dto.response.ApiResponse;
import com.campusfood.dto.response.ImageUploadResponse;
import com.campusfood.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for image upload and deletion operations using AWS S3.
 * Provides endpoints for uploading product images and general-purpose images.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final S3Service s3Service;

    /**
     * Upload a product image to S3.
     * POST /api/images/upload/product
     *
     * @param file the image file (JPEG, PNG, WebP, GIF — max 15MB)
     * @return the public S3 URL and metadata of the uploaded image
     */
    @PostMapping(value = "/upload/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadProductImage(
            @RequestParam("file") MultipartFile file) {
        String imageUrl = s3Service.uploadImage(file, "products");
        ImageUploadResponse response = ImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }

    /**
     * Upload a general-purpose image to a specified folder in S3.
     * POST /api/images/upload?folder=users
     *
     * @param file   the image file
     * @param folder the S3 folder/prefix (defaults to "general")
     * @return the public S3 URL and metadata
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        String imageUrl = s3Service.uploadImage(file, folder);
        ImageUploadResponse response = ImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }

    /**
     * Delete an image from S3 by its URL.
     * DELETE /api/images?imageUrl=https://...
     *
     * @param imageUrl the full public S3 URL of the image to delete
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam String imageUrl) {
        s3Service.deleteImage(imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully", null));
    }
}
