package com.vaultshare;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clients")
class AdminController {
    private final AllowedClientIpRepository allowed;
    private final ClientAccess access;

    AdminController(AllowedClientIpRepository allowed, ClientAccess access) {
        this.allowed = allowed;
        this.access = access;
    }

    record ClientIp(String ip) {}

    @GetMapping
    List<String> list(HttpServletRequest request) {
        access.requireAdmin(request);
        return allowed.findAll().stream().map(entry -> entry.ip).sorted().toList();
    }

    @PostMapping
    void allow(@RequestBody ClientIp client, HttpServletRequest request) {
        access.requireAdmin(request);
        if (!ClientAccess.isIpv4(client.ip())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter an IPv4 address");
        allowed.save(new AllowedClientIp(client.ip()));
    }

    @DeleteMapping("/{ip}")
    void remove(@PathVariable String ip, HttpServletRequest request) {
        access.requireAdmin(request);
        allowed.deleteById(ip);
    }
}
