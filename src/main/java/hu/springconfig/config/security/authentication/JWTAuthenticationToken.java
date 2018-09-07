package hu.springconfig.config.security.authentication;

import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.entity.authentication.Identity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Credentials contains the type {@link hu.springconfig.data.dto.authentication.Credentials}
 * Principal contains the type {@link hu.springconfig.data.entity.authentication.Identity}
 */
public class JWTAuthenticationToken extends AbstractAuthenticationToken {
    private Identity principal;
    private Credentials credentials;

    public JWTAuthenticationToken(Identity principal, Credentials credentials,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
    }

    public JWTAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Credentials credentials) {
        super(authorities);
        this.credentials = credentials;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        if (credentials != null) {
            this.credentials.eraseCredentials();
        }
    }
}
