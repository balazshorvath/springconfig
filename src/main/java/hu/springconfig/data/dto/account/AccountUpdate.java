package hu.springconfig.data.dto.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdate {
    private String lastName;
    private String firstName;
    private Long dailyCalorieGoal;
    private long version;
}
