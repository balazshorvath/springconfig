package hu.springconfig.config.message;

public class IdentityMessages {
    public static final String ENTITY_NAME = "identity";
    /* Authorization */
    public static final String IDENTITY_LOW_RANK = "identity.low_rank";
    public static final String IDENTITY_NOT_FOUND = "identity.not_found";
    public static final String IDENTITY_RESET_PASSWORD_FAILED = "identity.reset_password_failed";
    public static final String IDENTITY_CHECK_PASSWORD_FAILED = "identity.check_password_failed";
    /* Validation */
    public static final String IDENTITY_PASSWORD_INVALID = "identity.password.invalid";
    public static final String IDENTITY_PASSWORD_CONFIRM_MISMATCH = "identity.password.confirm.mismatch";
    public static final String IDENTITY_USERNAME_INVALID = "identity.username.invalid";
    public static final String IDENTITY_EMAIL_INVALID = "identity.email.invalid";
    public static final String IDENTITY_VALIDATION_ERROR = "identity.validation.error";
    /* Constraint */
    public static final String IDENTITY_USERNAME_UNIQUE_VIOLATION = "identity.username_unique.violation";

}
