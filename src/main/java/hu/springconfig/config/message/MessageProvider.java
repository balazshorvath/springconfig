package hu.springconfig.config.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageProvider {

    @Autowired
    private MessageSource messageSource;

    /**
     * Wrapper for {@link MessageSource#getMessage(String, Object[], String, Locale)}
     * Default message is the key itself.
     * Default locale is {@link Locale#ENGLISH}
     *
     * @param key
     * @param params
     * @return
     */
    public String getMessage(String key, Object... params) {
        return messageSource.getMessage(key, params, key, Locale.ENGLISH);
    }
}
