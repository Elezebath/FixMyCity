package lv.acnbootcamp.fixmycity.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;
}