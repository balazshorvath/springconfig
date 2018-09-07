package hu.springconfig.config.message;

public class MailingMessages {

    /**
     * 1 param is passed: new password
     */
    public static final String MAIL_PASSWORD_RESET_TEXT = "mail.password_reset.text";
    public static final String MAIL_PASSWORD_RESET_SUBJECT = "mail.password_reset.subject";
    /**
     * 1 param is passed: invite code
     */
    public static final String MAIL_INVITE_TEXT = "mail.invite.text";
    public static final String MAIL_INVITE_SUBJECT = "mail.invite.subject";
    /**
     * 2 params are passed: firstName and verification code
     */
    public static final String MAIL_VERIFICATION_TEXT = "mail.verification.text";
    public static final String MAIL_VERIFICATION_SUBJECT = "mail.verification.subject";
}
