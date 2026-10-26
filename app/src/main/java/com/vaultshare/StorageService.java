package com.vaultshare;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
class StorageService {
    @Value("${STORAGE_MODE:local}") String mode;
    @Value("${STORAGE_PATH:/tmp/vaultshare-files}") String path;
    @Value("${S3_BUCKET:}") String bucket;

    void put(String objectKey, byte[] bytes) throws Exception {
        if (mode.equals("s3")) {
            try (S3Client s3 = S3Client.create()) {
                s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket).key(objectKey)
                        .serverSideEncryption(ServerSideEncryption.AES256).build(),
                        RequestBody.fromBytes(bytes));
            }
        } else {
            Path root = Path.of(path);
            Files.createDirectories(root);
            Files.write(root.resolve(objectKey), bytes);
        }
    }

    byte[] get(String objectKey) throws Exception {
        if (mode.equals("s3")) {
            try (S3Client s3 = S3Client.create()) {
                return s3.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucket).key(objectKey).build()).asByteArray();
            }
        }
        return Files.readAllBytes(Path.of(path).resolve(objectKey));
    }

    void delete(String objectKey) throws Exception {
        if (mode.equals("s3")) {
            try (S3Client s3 = S3Client.create()) {
                s3.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket).key(objectKey).build());
            }
        } else {
            Files.deleteIfExists(Path.of(path).resolve(objectKey));
        }
    }
}
