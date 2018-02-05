package hu.springconfig.data.dto.authentication;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Credentials {
    @NotNull
    private String username;
    @NotNull
    private String password;
}
