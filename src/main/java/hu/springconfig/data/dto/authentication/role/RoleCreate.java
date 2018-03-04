package hu.springconfig.data.dto.authentication.role;

import lombok.Data;

import java.util.Set;

@Data
public class RoleCreate {
    private Integer id;
    private String role;
    private Set<Integer> privileges;
}
