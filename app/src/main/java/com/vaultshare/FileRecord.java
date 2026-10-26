package com.vaultshare;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
class FileRecord {
    @Id String id = UUID.randomUUID().toString();
    String filename;
    String contentType;
    String objectKey;
    long sizeBytes;
    Instant createdAt = Instant.now();
    protected FileRecord() {}
    FileRecord(String filename, String contentType, String objectKey, long sizeBytes) {
        this.filename=filename; this.contentType=contentType; this.objectKey=objectKey; this.sizeBytes=sizeBytes;
    }
}
