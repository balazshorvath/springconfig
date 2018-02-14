package hu.springconfig.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.InvalidTokenException;
import hu.springconfig.service.authentication.IdentityService;
import hu.springconfig.service.base.LoggingComponent;
import hu.springconfig.util.Util;
import io.jsonwebtoken.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

/**
 * JWT parse logic implementation.
 */
@Component
public class JWTTokenParser extends LoggingComponent {
    public static final String AUTHENTICATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    @Value("${jwt.expiration.time}")
    private Long expirationTime;
    @Value("${jwt.signature.secret}")
    private String signatureSecret;
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public void createAndSetToken(HttpServletResponse response, Identity identity) {
        Claims claims = Jwts.claims()
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setSubject(identity.getUsername());
        claims.put("id", identity.getId());

        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");

            TokenResponse response1 = new TokenResponse();
            response1.identity = identity;
            response1.token = Jwts.builder()
                            .setClaims(claims)
                            .signWith(SignatureAlgorithm.HS512, signatureSecret)
                            .compact();

            ServletOutputStream out = response.getOutputStream();
            out.println(objectMapper.writeValueAsString(response1));
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("Could not send response!", e);
        }
    }

    @SuppressWarnings("Unchecked")
    public Identity parseToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHENTICATION_HEADER);
        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(signatureSecret)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
        }catch (ExpiredJwtException e){
            throw new InvalidTokenException("jwt.expired");
        }catch (ClaimJwtException e){
            throw new InvalidTokenException("jwt.invalid");
        }
        String user = claims.getSubject();
        Long id = claims.get("id", Long.class);
        if (!Util.notNullAndNotEmpty(user) && id == null) {
            return null;
        }
        Identity identity = identityRepository.findOne(id);
        if (identity.getTokenExpiration() != null && identity.getTokenExpiration().before(claims.getExpiration())){
            throw new InvalidTokenException("jwt.expired");
        }
        return identity;
    }
    @Data
    public static class TokenResponse {
        private String token;
        private Identity identity;
    }
}
