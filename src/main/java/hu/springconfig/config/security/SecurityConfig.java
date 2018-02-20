package hu.springconfig.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.config.security.authentication.AppUserDetailsService;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.config.security.authentication.filter.JWTAuthenticationFilter;
import hu.springconfig.config.security.authentication.filter.JWTAuthorizationFilter;
import hu.springconfig.config.security.authentication.provider.IdentityAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private AppUserDetailsService userDetailsService;
    @Autowired
    private JWTTokenParser jwtTokenParser;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IdentityAuthenticationProvider identityAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/auth/register", "/auth").permitAll()
                .anyRequest().authenticated()
                .and().httpBasic().disable()
                .addFilterBefore(new JWTAuthenticationFilter(authenticationManager(), jwtTokenParser, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTAuthorizationFilter(authenticationManager(), jwtTokenParser), BasicAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(identityAuthenticationProvider);
        //.userDetailsService(userDetailsService)
        //.passwordEncoder(bCryptPasswordEncoder)
    }

//    @Bean
//    public AuthenticationEntryPoint unauthorizedEntryPoint() {
//        return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//    }

}
