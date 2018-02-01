package hu.springconfig.data.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
public class Authentication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private Set<Role> roles;

    public Collection<? extends GrantedAuthority> createGrantedAuthorities(){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
    }

    public enum Role {
        USER(0),
        ADMIN(100);

        private final Integer value;

        Role(Integer value){
            this.value = value;
        }

        public Integer getValue(){
            return this.value;
        }
    }
}
