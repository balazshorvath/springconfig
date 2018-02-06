package hu.springconfig.config.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import hu.springconfig.config.json.DateFormat;
import hu.springconfig.exception.ResponseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIError {
    private String message;
    private HttpStatus status;
    @JsonFormat(pattern = DateFormat.DATE_TIME_24H_FORMAT)
    private Date when;
    private Class<? extends ResponseException> exception;
    private Class<? extends Throwable> originalException;
}
