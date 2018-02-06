package hu.springconfig.config.security.authentication;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.util.Util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Set;

/**
 * JWT parse logic implementation.
 */
@Component
public class JWTTokenParser {
    public static final String AUTHENTICATION_HEADER = "Authentication";
    public static final String TOKEN_PREFIX = "Bearer ";
    @Value("${jwt.expiration.time}")
    private Long expirationTime;
    @Value("${jwt.signature.secret}")
    private String signatureSecret;
    @Autowired
    private IRoleRepository roleRepository;

    public void createAndSetToken(HttpServletResponse response, Identity user) {
        Claims claims = Jwts.claims()
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setSubject(user.getUsername());
        claims.put("id", user.getId());

        response.setHeader(
                AUTHENTICATION_HEADER,
                Jwts.builder()
                        .setClaims(claims)
                        .signWith(SignatureAlgorithm.HS512, signatureSecret)
                        .compact()
        );
    }

    @SuppressWarnings("Unchecked")
    public Identity parseToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHENTICATION_HEADER);
        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        Claims claims = Jwts.parser()
                .setSigningKey(signatureSecret)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();

        String user = claims.getSubject();
        Long id = claims.get("id", Long.class);
        if (!Util.notNullAndNotEmpty(user) && id != null) {
            return null;
        }
        Identity identity = new Identity();
        identity.setId(id);
        identity.setUsername(user);
        Set<Role> roleSet = roleRepository.findByIdentity(identity);
        identity.setRoles(roleSet);

        return identity;
    }
}
