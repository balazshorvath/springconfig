package hu.springconfig.data.entity.authentication;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
public class Identity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    @ManyToMany
    @JoinTable(
            name = "identity_roles",
            joinColumns = @JoinColumn(
                    name = "identity_id", referencedColumnName = "id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"
            )
    )
    private Set<Role> roles;

    public Collection<? extends GrantedAuthority> createGrantedAuthorities(){
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles){
            authorities.addAll(role.createGrantedAuthorities());
        }
        return authorities;
    }
}
