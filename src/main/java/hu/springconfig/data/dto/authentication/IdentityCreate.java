package hu.springconfig.data.dto.authentication;

import lombok.Data;

@Data
public class IdentityCreate {
    private String username;
    private String email;
    private String password;
    private String passwordConfirm;
}
