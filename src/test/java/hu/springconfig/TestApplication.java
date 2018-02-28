package hu.springconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.service.mail.MailingService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@Configuration
@ConfigurationProperties
@ComponentScan({"hu.springconfig.service.authentication", "hu.springconfig.data", "hu.springconfig.validator", "hu.springconfig.config.message"})
public class TestApplication {

    @Bean
    public MailingService mailingService() {
        return mock(MailingService.class);
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
