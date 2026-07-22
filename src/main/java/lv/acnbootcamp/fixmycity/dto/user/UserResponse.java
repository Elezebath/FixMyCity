package lv.acnbootcamp.fixmycity.dto.user;

import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;

// What we send back after registration. Deliberately excludes the password hash.
public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}