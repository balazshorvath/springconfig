package hu.springconfig.config.security.authentication.user;

import lombok.Data;

@Data
public class Credentials {
    private String username;
    private String password;
}
