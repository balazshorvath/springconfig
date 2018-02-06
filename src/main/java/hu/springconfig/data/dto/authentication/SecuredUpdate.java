package hu.springconfig.data.dto.authentication;

import lombok.Data;

@Data
public class SecuredUpdate {
    private String value;
    private String password;
}
