package hu.springconfig.data.dto.authentication;

import lombok.Data;

@Data
public class ChangePassword {
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirm;

}
