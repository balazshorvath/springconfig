package hu.springconfig.data.dto.simple;

import lombok.Data;

@Data
public class OKResponse {
    private String message;

    public OKResponse() {
        message = "OK";
    }
}
