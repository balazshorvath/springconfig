package hu.springconfig.config.security.authorization;

import hu.springconfig.data.entity.authentication.Identity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class IdentityAuthorization {
    public boolean isSelf(Authentication authentication, Long id) {
        return ((Identity) authentication.getPrincipal()).getId().equals(id);
    }
}
