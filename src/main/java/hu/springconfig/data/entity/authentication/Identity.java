package hu.springconfig.data.entity.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * Simple class for Authentication and Authorization.
 * A User might inherit from this class, or simply create a one-to-one relation.
 * <p>
 * TODO: implement locking, password expiration
 */
@Data
@Entity
@Table(
        name = "Identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "identityUsernameUnique", columnNames = "username"),
                @UniqueConstraint(name = "identityEmailUnique", columnNames = "email")
        }
)
@EqualsAndHashCode(exclude = {"roles", "password", "version"})
@ToString(exclude = {"password"})
public class Identity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    @JsonIgnore
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
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
    @Version
    private long version;

    /**
     * True, if this {@link Identity} is superior to the one in the parameter,
     * which means, that this {@link Identity} has a higher "rank".
     *
     * @param identity
     * @return
     */
    public boolean isSuperiorTo(Identity identity) {
        Role max = identity.getHighestRole();
        if (max == null) {
            // Identity has no roles, cannot be superior to anyone.
            return true;
        }
        // does "this" have any role, which is higher, than the maximum of the parameter's?
        return this.roles.stream().anyMatch(
                role -> role.getRole().getValue() > max.getRole().getValue()
        );
    }

    @JsonIgnore
    public Role getHighestRole() {
        return this.roles.stream().max(Comparator.comparingInt(o -> o.getRole().getValue()))
                .orElse(null);
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.addAll(role.createGrantedAuthorities());
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
