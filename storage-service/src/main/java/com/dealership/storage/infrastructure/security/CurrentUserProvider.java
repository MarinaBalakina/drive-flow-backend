package com.dealership.storage.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String rawUserId = jwt.getClaimAsString("app_user_id");
        if (rawUserId == null || rawUserId.isBlank()) {
            throw new AccessDeniedException("Claim app_user_id is missing");
        }

        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Claim app_user_id is not UUID");
        }
    }

    public boolean hasRole(String role){
        String target = "ROLE_" + role;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(target));
    }

    public boolean hasAnyRole(String[] roles) {
        return Arrays.stream(roles).anyMatch(this::hasRole);
    }
}