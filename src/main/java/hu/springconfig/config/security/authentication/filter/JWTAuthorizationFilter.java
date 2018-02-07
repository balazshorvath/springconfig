package hu.springconfig.config.security.authentication.filter;

import hu.springconfig.config.security.authentication.JWTAuthenticationToken;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.entity.authentication.Identity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authorizes requests.
 * Uses {@link JWTTokenParser} to extract and validate JWT token.
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter  {
    private JWTTokenParser tokenParser;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTTokenParser tokenParser) {
        super(authenticationManager);
        this.tokenParser = tokenParser;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        Identity identity = tokenParser.parseToken(request);
        if(identity != null){
            Authentication authentication = new JWTAuthenticationToken(identity, null, identity.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);

    }
}
