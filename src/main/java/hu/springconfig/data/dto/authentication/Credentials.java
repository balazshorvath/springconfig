package hu.springconfig.data.dto.authentication;

import lombok.Data;
import org.springframework.security.core.CredentialsContainer;

import javax.validation.constraints.NotNull;

@Data
public class Credentials implements CredentialsContainer {
    @NotNull
    private String username;
    @NotNull
    private String password;

    @Override
    public void eraseCredentials() {
        password = null;
    }
}
