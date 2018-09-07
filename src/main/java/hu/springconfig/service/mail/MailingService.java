package hu.springconfig.service.mail;

import hu.springconfig.config.message.MailingMessages;
import hu.springconfig.config.message.MessageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailingService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MessageProvider messageProvider;

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendPasswordReset(String to, String password) {
        sendMail(
                to,
                messageProvider.getMessage(MailingMessages.MAIL_PASSWORD_RESET_SUBJECT),
                messageProvider.getMessage(MailingMessages.MAIL_PASSWORD_RESET_TEXT, password)
        );
    }

    public void sendInvite(String to, String key) {
        sendMail(
                to,
                messageProvider.getMessage(MailingMessages.MAIL_INVITE_SUBJECT),
                messageProvider.getMessage(MailingMessages.MAIL_INVITE_TEXT, key)
        );
    }

    public void sendVerification(String to, String firstName, String verificationCode) {
        sendMail(
                to,
                messageProvider.getMessage(MailingMessages.MAIL_VERIFICATION_SUBJECT),
                messageProvider.getMessage(MailingMessages.MAIL_VERIFICATION_TEXT, firstName, verificationCode)
        );
    }
}
