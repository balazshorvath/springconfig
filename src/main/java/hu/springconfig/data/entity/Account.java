package hu.springconfig.data.entity;

import hu.springconfig.data.entity.authentication.Identity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = "identity")
public class Account {
    @Id
    private Long identityId;
    @OneToOne(mappedBy = "account", fetch = FetchType.EAGER)
    private Identity identity;
    private String firstName;
    private String lastName;
    private Long dailyCalorieGoal;
    @Version
    private long version;

    public Account(Account account) {
        this.identity = new Identity(account.identity);
        this.identityId = account.identityId;
        this.firstName = account.firstName;
        this.lastName = account.lastName;
        this.dailyCalorieGoal = account.dailyCalorieGoal;
        this.version = account.version;
    }

    public void setIdentity(Identity identity) {
        this.identityId = identity == null ? null : identity.getId();
        this.identity = identity;
    }
}
