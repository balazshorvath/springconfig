package hu.springconfig.data.dto.account;

import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {
    private IdentityDTO identity;
    private String firstName;
    private String lastName;
    private Long dailyCalorieGoal;
    private long version;
}
