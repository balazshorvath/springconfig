package hu.springconfig.data.dto.authentication.identity;

import hu.springconfig.data.entity.authentication.Role;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class IdentityDTO {
    private Long id;
    private String username;
    private String email;
    private Date tokenExpiration;
    private Set<Role> roles;
    private long version;
}
