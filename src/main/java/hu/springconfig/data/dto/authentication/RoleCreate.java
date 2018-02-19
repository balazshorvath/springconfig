package hu.springconfig.data.dto.authentication;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class RoleCreate {
    @NotNull
    @Min(1)
    @Max(1000)
    private Integer id;
    @NotNull
    private String role;
    @NotNull
    private Set<Integer> privileges;
}
