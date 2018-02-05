package hu.springconfig.data.entity.authentication;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * A {@link Privilege} is the smallest unit of authority.
 * A {@link Role} consists of multiple Privileges.
 * An {@link Identity} can have multiple Roles.
 */
@Data
@NoArgsConstructor
@Entity
public class Privilege {
    @Id
    private Integer id;
    @Enumerated(EnumType.STRING)
    private Privileges privilege;

    public GrantedAuthority createGrantedAuthority(){
        return new SimpleGrantedAuthority(privilege.name());
    }

    public Privilege(Privileges privilege) {
        this.id = privilege.value;
        this.privilege = privilege;
    }

    public enum Privileges {
        USER_CREATE(1),
        USER_UPDATE(2),
        USER_GET(3),
        USER_LIST(4),
        USER_DELETE(5);

        private final Integer value;

        Privileges(Integer value){
            this.value = value;
        }

        public Integer getValue(){
            return this.value;
        }
    }
}
