package hu.springconfig.helper;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Data
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
    private Sort sort;

    public CustomPageImpl() {
        super(new ArrayList<>());
    }

    public Page<T> pageImpl() {
        return new PageImpl<>(getContent(), new PageRequest(getNumber(),
                getSize(), getSort()), getTotalElements());
    }
}