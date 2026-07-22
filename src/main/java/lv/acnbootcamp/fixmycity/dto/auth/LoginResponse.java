package lv.acnbootcamp.fixmycity.dto.auth;

import lv.acnbootcamp.fixmycity.entity.user.Role;

/**
 * Response returned after successful authentication.
 * Contains the JWT access token and basic information about the authenticated user.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String fullName,
        Role role
) {}