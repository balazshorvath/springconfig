package hu.springconfig.config.security.authentication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.config.security.authentication.JWTAuthenticationToken;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Defines the URL, that is used for authentication (see the constructor).
 * Extracts the authentication data from the request ({@link #attemptAuthentication(HttpServletRequest, HttpServletResponse)}).
 * If the authentication was successful, creates the JWT token and {@link JWTTokenParser} puts the token into the response
 * ({@link #successfulAuthentication(HttpServletRequest, HttpServletResponse, FilterChain, Authentication)}).
 */
public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private JWTTokenParser tokenParser;
    private ObjectMapper objectMapper;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTTokenParser tokenParser, ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher("/auth", "POST"));
        setAuthenticationManager(authenticationManager);
        this.tokenParser = tokenParser;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Authentication authentication = null;
        try {
            Credentials credentials = objectMapper.readValue(request.getInputStream(), Credentials.class);
            authentication = getAuthenticationManager().authenticate(new JWTAuthenticationToken(null, credentials, new ArrayList<>()));
        } catch (IOException e) {
            throw new BadRequestException("Could not parse credentials.", e);
        }
        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        tokenParser.createAndSetToken(response, (Identity) authResult.getPrincipal());
    }
}
