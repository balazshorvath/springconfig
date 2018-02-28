package hu.springconfig.config.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import hu.springconfig.config.json.DateFormat;
import hu.springconfig.exception.ResponseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class APIError {
    private String message;
    private Integer status;
    @JsonFormat(pattern = DateFormat.DATE_TIME_24H_FORMAT)
    private Date when;
    private Class<? extends ResponseException> exception;
    private Class<? extends Throwable> originalException;

    public APIError(ResponseException e) {
        this.message = e.getMessage();
        this.status = e.getStatus().value();
        this.when = e.getTime();
        this.exception = e.getClass();
        this.originalException = e.getCause() == null ? null : e.getCause().getClass();
    }
}
