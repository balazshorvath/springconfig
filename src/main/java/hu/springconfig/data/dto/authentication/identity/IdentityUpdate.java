package hu.springconfig.data.dto.authentication.identity;

import lombok.Data;

@Data
public class IdentityUpdate {
    private String email;
    private String username;
    private long version;
}
