package hu.springconfig.data.query.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize
public class ConditionSet extends Condition {
    @NotNull
    protected LogicalOperator logicalOperator;
    @NotNull
    protected List<Condition> conditions;

    public ConditionSet() {
    }

    public ConditionSet(LogicalOperator logicalOperator, Integer order, List<Condition> conditions) {
        super(order);
        this.logicalOperator = logicalOperator;
        this.conditions = conditions;
    }
}
