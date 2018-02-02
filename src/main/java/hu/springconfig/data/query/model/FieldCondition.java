package hu.springconfig.data.query.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize
public class FieldCondition extends Condition {
    @NotNull
    protected String fieldName;
    @NotNull
    protected RelationalOperator relationalOperator;
    @NotNull
    protected Object value;

    public FieldCondition() {
    }

    public FieldCondition(Integer order, String fieldName, RelationalOperator relationalOperator, Object value) {
        super(order);
        this.fieldName = fieldName;
        this.relationalOperator = relationalOperator;
        this.value = value;
    }

    public enum RelationalOperator {
        eq, ne, lt, gt, contains, startswith, endswith
    }
}
