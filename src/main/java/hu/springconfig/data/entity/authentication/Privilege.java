package hu.springconfig.data.entity.authentication;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;

/**
 * A {@link Privilege} is the smallest unit of authority.
 * A {@link Role} consists of multiple Privileges.
 * An {@link Identity} can have multiple Roles.
 */
@Data
@NoArgsConstructor
@Entity
@Table(
        name = "Privilege",
        uniqueConstraints = {
                @UniqueConstraint(name = "privilegeUnique", columnNames = "privilege")
        }
)
public class Privilege {
    @Id
    private Integer id;
    @Enumerated(EnumType.STRING)
    private Privileges privilege;

    public GrantedAuthority createGrantedAuthority() {
        return new SimpleGrantedAuthority(privilege.name());
    }

    public Privilege(Privileges privilege) {
        this.id = privilege.value;
        this.privilege = privilege;
    }

    public enum Privileges {
        IDENTITY_GET(1),
        IDENTITY_LIST(2),
        IDENTITY_UPDATE(3),
        IDENTITY_GRANT(4),
        IDENTITY_DENY(5),
        IDENTITY_DELETE(6),
        ROLE_GET(7),
        ROLE_LIST(8),
        ROLE_UPDATE(9);

        private final Integer value;

        Privileges(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return this.value;
        }
    }
}
