package hu.springconfig.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.config.message.application.HttpMessages;
import hu.springconfig.exception.ForbiddenException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AppAccessDeniedHandler implements AccessDeniedHandler {

    private ObjectMapper objectMapper;
    private MessageProvider messageProvider;

    public AppAccessDeniedHandler(ObjectMapper objectMapper, MessageProvider messageProvider) {
        this.objectMapper = objectMapper;
        this.messageProvider = messageProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException
            accessDeniedException) throws IOException, ServletException {
        ForbiddenException exception = new ForbiddenException(
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                accessDeniedException
        );
        APIError error = new APIError(exception);
        error.setMessage(messageProvider.getMessage(exception.getMessage()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().println(objectMapper.writeValueAsString(error));
        response.setStatus(exception.getStatus().value());
    }
}
