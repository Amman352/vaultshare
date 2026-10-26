package com.vaultshare;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
class ShareRecord {
    @Id String id = UUID.randomUUID().toString();
    String fileId;
    Instant expiresAt;
    int remainingDownloads;
    protected ShareRecord() {}
    ShareRecord(String fileId, Instant expiresAt, int remainingDownloads) {
        this.fileId=fileId; this.expiresAt=expiresAt; this.remainingDownloads=remainingDownloads;
    }
}
