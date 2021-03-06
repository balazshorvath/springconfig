package hu.springconfig.data.entity.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.springconfig.data.entity.Account;
import hu.springconfig.util.Util;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

/**
 * Simple class for Authentication and Authorization.
 * A User might inherit from this class, or simply create a one-to-one relation.
 * <p>
 * TODO: implement locking, password expiration
 */
@Data
@NoArgsConstructor
@Entity
@Table(
        name = "Identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "email_unique", columnNames = "email")
        }
)
@EqualsAndHashCode(exclude = {"roles", "password", "version", "account"})
@ToString(exclude = {"password", "roles", "account"})
public class Identity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    @JsonIgnore
    private String password;
    /**
     * This value (if set) shows the time, when something important
     * changed in the identity's data.
     * Tokens acquired before this, should be handled as expired.
     */
    private Long tokenExpiration;
    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    private Account account;
    private String verificationCode;
    private int loginFails;
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

    public Identity(Identity identity) {
        this.id = identity.id;
        this.email = identity.email;
        this.password = identity.password;
        this.tokenExpiration = identity.tokenExpiration;
        this.account = identity.account;
        this.roles = identity.roles == null ? null : new HashSet<>(identity.roles);
        this.version = identity.version;
    }

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
            return Util.notNullAndNotEmpty(this.roles);
        }
        // does "this" have any role, which is higher, than the maximum of the parameter's?
        return this.roles.stream().anyMatch(
                role -> role.getId() > max.getId()
        );
    }

    public void incrementLoginFails() {
        this.loginFails++;
    }

    @JsonIgnore
    public Role getHighestRole() {
        return this.roles == null
                ? null : this.roles.stream().max(Comparator.comparingInt(Role::getId)).orElse(null);
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
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    public boolean isAccountLocked() {
        return Util.notNullAndNotEmpty(this.verificationCode) || this.loginFails >= 3;
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
