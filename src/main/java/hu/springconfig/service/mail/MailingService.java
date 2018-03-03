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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setText(messageProvider.getMessage(MailingMessages.MAIL_PASSWORD_RESET_TEXT, password));
        message.setSubject(messageProvider.getMessage(MailingMessages.MAIL_PASSWORD_RESET_SUBJECT));
        mailSender.send(message);
    }
}
