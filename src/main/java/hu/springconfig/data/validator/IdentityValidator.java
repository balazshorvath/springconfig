package hu.springconfig.data.validator;

import hu.springconfig.exception.BadRequestException;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IdentityValidator {
    @Value("${password.length.min}")
    private Integer passwordMin;
    @Value("${password.length.max}")
    private Integer passwordMax;
    @Value("${password.charset}")
    private String passwordCharset;

    @Value("${username.length.min}")
    private Integer usernameMin;
    @Value("${username.length.max}")
    private Integer usernameMax;
    @Value("${username.charset}")
    private String usernameCharset;

    @Value("${email.length.min}")
    private Integer emailMin;
    @Value("${email.length.max}")
    private Integer emailMax;


    public void validatePasswordConfirm(String password, String passwordConfirm){
        if(!Util.notNullAndNotEmpty(password) || password.length() < passwordMin || password.length() > passwordMax
                || !checkCharset(password, passwordCharset)){
            throw new BadRequestException("identity.password.invalid");
        }
        if(!password.equals(passwordConfirm)){
            throw new BadRequestException("identity.password.confirm.mismatch");
        }
    }
    public void validateUsername(String username){
        if(!Util.notNullAndNotEmpty(username) || username.length() < usernameMin || username.length() > usernameMax
                || !checkCharset(username, usernameCharset)){
            throw new BadRequestException("identity.username.invalid");
        }
    }
    public void validateEmail(String email){
        if(!Util.notNullAndNotEmpty(email) || email.length() < emailMin || email.length() > emailMax){
            throw new BadRequestException("identity.email.invalid");
        }
    }
    private boolean checkCharset(String str, String charset){
        return str.matches("[" + charset + "]*");
    }
}
