package com.vaultshare;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class AllowedClientIp {
    @Id
    public String ip;

    protected AllowedClientIp() {}

    AllowedClientIp(String ip) { this.ip = ip; }
}
