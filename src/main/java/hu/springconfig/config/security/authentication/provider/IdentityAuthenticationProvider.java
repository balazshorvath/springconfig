package hu.springconfig.config.security.authentication.provider;

import hu.springconfig.config.message.application.AuthenticationMessages;
import hu.springconfig.config.message.entity.IdentityMessages;
import hu.springconfig.config.security.authentication.JWTAuthenticationToken;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.authentication.AuthenticationAccountLockedException;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class IdentityAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IdentityService identityService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // This is already a valid token
        if (authentication.getPrincipal() != null && authentication.getPrincipal() instanceof Identity) {
            return authentication;
        }
        if (!(authentication.getCredentials() instanceof Credentials)) {
            return null;
        }
        Credentials credentials = (Credentials) authentication.getCredentials();
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (!Util.notNullAndNotEmpty(username) && !Util.notNullAndNotEmpty(password)) {
            throw new BadCredentialsException(AuthenticationMessages.AUTHENTICATION_FAILED_CREDENTIALS);
        }
        Identity identity = (Identity) userDetailsService.loadUserByUsername(username);
        if (identity == null) {
            throw new BadCredentialsException(AuthenticationMessages.AUTHENTICATION_FAILED_CREDENTIALS);
        }
        if (identity.isAccountLocked()) {
            throw new AuthenticationAccountLockedException(IdentityMessages.IDENTITY_LOCKED);
        }
        if (!passwordEncoder.matches(credentials.getPassword(), identity.getPassword())) {
            identityService.loginFailed(identity.getId());
            throw new BadCredentialsException(AuthenticationMessages.AUTHENTICATION_FAILED_CREDENTIALS);
        }
        identity.setPassword(null);
        identity.setLoginFails(0);
        return new JWTAuthenticationToken(identity, credentials, identity.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JWTAuthenticationToken.class);
    }
}
