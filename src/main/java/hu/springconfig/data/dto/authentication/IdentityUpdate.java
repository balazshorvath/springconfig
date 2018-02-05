package hu.springconfig.data.dto.authentication;

import hu.springconfig.data.entity.authentication.Role;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class IdentityUpdate {
    private String username;
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirm;
    private Set<Role.Roles> roles;
    @NotNull
    private long version;
}
