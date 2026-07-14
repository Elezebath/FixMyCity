package lv.acnbootcamp.fixmycity.dto;

import lv.acnbootcamp.fixmycity.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder (toBuilder = true)
@AllArgsConstructor
public class UserAdminResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}