package hu.springconfig.data.dto.account;

import hu.springconfig.data.dto.authentication.identity.IdentityCreate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountCreate {
    private IdentityCreate identity;
    private String lastName;
    private String firstName;
    private Long dailyCalorieGoal;
}
