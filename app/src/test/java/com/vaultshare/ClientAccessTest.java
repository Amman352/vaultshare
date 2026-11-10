package com.vaultshare;

import jakarta.servlet.http.HttpServletRequest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientAccessTest {
    private final AllowedClientIpRepository allowed = mock(AllowedClientIpRepository.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final ClientAccess access = new ClientAccess(allowed, new SimpleMeterRegistry(), "admin", "admin");

    @Test void deniedWhenAddressIsNotListed() {
        when(request.getRemoteAddr()).thenReturn("203.0.113.10");
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> access.requireDownload(request)).getStatusCode().value());
    }

    @Test void listedAddressCanDownload() {
        when(request.getRemoteAddr()).thenReturn("203.0.113.10");
        when(allowed.existsById("203.0.113.10")).thenReturn(true);
        assertDoesNotThrow(() -> access.requireDownload(request));
    }

    @Test void adminCanDownloadAndManageList() {
        String header = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
        when(request.getHeader("Authorization")).thenReturn(header);
        assertDoesNotThrow(() -> access.requireAdmin(request));
        assertDoesNotThrow(() -> access.requireDownload(request));
        verifyNoInteractions(allowed);
    }

    @Test void invalidAdminCredentialsAreRejected() {
        when(request.getHeader("Authorization")).thenReturn("Basic Zm9vOmJhcg==");
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> access.requireAdmin(request)).getStatusCode().value());
    }
}
