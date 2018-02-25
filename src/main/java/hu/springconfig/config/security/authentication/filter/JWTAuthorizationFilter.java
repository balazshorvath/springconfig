package hu.springconfig.config.security.authentication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.security.authentication.JWTAuthenticationToken;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authorizes requests.
 * Uses {@link JWTTokenParser} to extract and validate JWT token.
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper;
    private JWTTokenParser tokenParser;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTTokenParser tokenParser, ObjectMapper objectMapper) {
        super(authenticationManager);
        this.tokenParser = tokenParser;
        this.objectMapper = objectMapper;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            Identity identity = tokenParser.parseToken(request);

            log.debug("Token parsed: {}", identity);
            if (identity != null) {
                Authentication authentication = new JWTAuthenticationToken(identity, null, identity.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            chain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            APIError error = new APIError(e);

            response.getOutputStream().print(objectMapper.writeValueAsString(error));
            response.setContentType("application/json");
            response.sendError(e.getStatus().value());
        }
    }
}
