package hu.springconfig.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CustomPageImpl<T> extends PageImpl<T> {

    private static final long serialVersionUID = 1L;
    private int number;
    private int size;
    private int totalPages;
    private int numberOfElements;
    private long totalElements;
    private boolean previous;
    private boolean first;
    private boolean next;
    private boolean last;
    private List<T> content;

    public CustomPageImpl() {
        super(new ArrayList<>());
    }

    public Page<T> pageImpl() {
        return new PageImpl<>(getContent(), new PageRequest(
                getNumber(),
                getSize()
        ), getTotalElements());
    }
}
