package com.vaultshare;

import jakarta.servlet.http.HttpServletRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
class ClientAccess {
    private final AllowedClientIpRepository allowed;
    private final String adminUser;
    private final String adminPassword;
    private final MeterRegistry metrics;

    ClientAccess(AllowedClientIpRepository allowed, MeterRegistry metrics,
                 @Value("${vaultshare.admin.username:admin}") String adminUser,
                 @Value("${vaultshare.admin.password:admin}") String adminPassword) {
        this.allowed = allowed;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.metrics = metrics;
    }

    boolean isAdmin(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) return false;
        try {
            byte[] supplied = Base64.getDecoder().decode(header.substring(6));
            byte[] expected = (adminUser + ":" + adminPassword).getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(supplied, expected);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    void requireAdmin(HttpServletRequest request) {
        if (!isAdmin(request)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin credentials required");
    }

    void requireDownload(HttpServletRequest request) {
        boolean permitted = isAdmin(request) || allowed.existsById(request.getRemoteAddr());
        metrics.counter("vaultshare.client.requests", "decision", permitted ? "allowed" : "denied").increment();
        if (!permitted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This client IP is not allowed to download");
        }
    }

    static boolean isIpv4(String value) {
        if (value == null || !value.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) return false;
        for (String part : value.split("\\.")) {
            if (Integer.parseInt(part) > 255) return false;
        }
        return true;
    }
}
