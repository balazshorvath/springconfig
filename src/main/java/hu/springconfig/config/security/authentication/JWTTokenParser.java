package hu.springconfig.config.security.authentication;

import hu.springconfig.Util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTTokenParser {
    public static final String AUTHENTICATION_HEADER = "Authentication";
    public static final String TOKEN_PREFIX = "Bearer ";
    @Value("${jwt.expiration.time}")
    private Long expirationTime;
    @Value("${jwt.signature.secret}")
    private String signatureSecret;

    public void createAndSetToken(HttpServletResponse response, User user){
        String roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        Claims claims = Jwts.claims()
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setSubject(user.getUsername());
        claims.put("roles", roles);

        response.setHeader(
                AUTHENTICATION_HEADER,
                Jwts.builder()
                        .setClaims(claims)
                        .signWith(SignatureAlgorithm.HS512, signatureSecret)
                        .compact()
        );
    }

    public UsernamePasswordAuthenticationToken parseToken(HttpServletRequest request){
        String token = request.getHeader(AUTHENTICATION_HEADER);
        if(token == null || !token.startsWith(TOKEN_PREFIX)){
            return null;
        }
        Claims claims = Jwts.parser()
                .setSigningKey(signatureSecret)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();

        String user = claims.getSubject();
        String roles = claims.get("roles", String.class);
        if(!Util.notNullAndNotEmpty(user) || !Util.notNullAndNotEmpty(roles)){
            return null;
        }
        List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(roles);

        return new UsernamePasswordAuthenticationToken(user, null, authorityList);
    }
}
