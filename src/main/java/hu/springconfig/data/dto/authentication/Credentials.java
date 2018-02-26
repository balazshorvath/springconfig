package hu.springconfig.data.dto.authentication;

import lombok.Data;
import org.springframework.security.core.CredentialsContainer;

@Data
public class Credentials implements CredentialsContainer {
    private String username;
    private String password;

    @Override
    public void eraseCredentials() {
        password = null;
    }
}
