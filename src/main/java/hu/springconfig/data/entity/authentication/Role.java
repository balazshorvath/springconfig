package hu.springconfig.data.entity.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link Privilege} is the smallest unit of authority.
 * A {@link Role} consists of multiple Privileges.
 * An {@link Identity} can have multiple Roles.
 */
@Data
@NoArgsConstructor
@Entity
@Table(
        name = "Role",
        uniqueConstraints = {
                @UniqueConstraint(name = "roleUnique", columnNames = "role")
        }
)
@EqualsAndHashCode(exclude = {"privileges", "identities"})
@ToString(exclude = {"privileges", "identities"})
public class Role {
    @Id
    private Integer id;
    @Enumerated(EnumType.STRING)
    private Roles role;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_privileges",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id")
    )
    private Set<Privilege> privileges;
    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<Identity> identities;

    public Collection<? extends GrantedAuthority> createGrantedAuthorities() {
        Collection<GrantedAuthority> authorities = privileges.stream().map(Privilege::createGrantedAuthority).collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    public Role(Roles role) {
        this.id = role.value;
        this.role = role;
    }

    /**
     * Role values(also id in the DB) should be defined in a way, that a Role with higher value
     * is superior to the ones with a smaller value.
     * <p>
     * For example when granting roles:
     * A user can only grant a role to another, if MAX(current.roles) <= roleToGrant
     * The same rule applies to deleting users, updating users, depriving roles etc.
     */
    public enum Roles {
        USER(0),
        ADMIN(100);

        private final Integer value;

        Roles(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return this.value;
        }
    }
}
