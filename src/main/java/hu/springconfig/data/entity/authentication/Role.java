package hu.springconfig.data.entity.authentication;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Entity
public class Role {
    @Id
    private Integer id;
    @Enumerated(EnumType.STRING)
    private Roles role;

    @ManyToMany
    @JoinTable(
            name = "role_privileges",
            joinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "privilege_id", referencedColumnName = "id")
    )
    private Set<Privilege> privileges;

    public Collection<? extends GrantedAuthority> createGrantedAuthorities(){
        Collection<GrantedAuthority> authorities = privileges.stream().map(Privilege::createGrantedAuthority).collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    public Role(Roles role) {
        this.id = role.value;
        this.role = role;
    }

    public enum Roles {
        USER(0),
        ADMIN(100);

        private final Integer value;

        Roles(Integer value){
            this.value = value;
        }

        public Integer getValue(){
            return this.value;
        }
    }
}
