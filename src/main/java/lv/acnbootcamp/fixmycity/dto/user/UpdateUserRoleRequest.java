package lv.acnbootcamp.fixmycity.dto.user;

import jakarta.validation.constraints.NotNull;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}