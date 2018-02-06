package hu.springconfig.config.security.authentication.provider;

import hu.springconfig.config.security.authentication.AppUserDetailsService;
import hu.springconfig.config.security.authentication.JWTAuthenticationToken;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.entity.authentication.Identity;
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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(!(authentication.getCredentials() instanceof Credentials)){
            return null;
        }
        Credentials credentials = (Credentials) authentication.getCredentials();
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if(!Util.notNullAndNotEmpty(username) && !Util.notNullAndNotEmpty(password)){
            throw new BadCredentialsException("Empty credentials!");
        }
        Identity identity = (Identity) userDetailsService.loadUserByUsername(username);
        if(identity == null || !passwordEncoder.matches(credentials.getPassword(), identity.getPassword())){
            throw new BadCredentialsException("Bad credentials!");
        }
        identity.setPassword(null);
        JWTAuthenticationToken token = new JWTAuthenticationToken(identity, credentials, identity.getAuthorities());
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JWTAuthenticationToken.class);
    }
}
