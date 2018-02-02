package hu.springconfig.data.query.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hu.springconfig.data.query.json.ConditionDeserializer;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonDeserialize(using = ConditionDeserializer.class)
public abstract class Condition {
    @NotNull
    protected Integer order;

    public Condition() {
    }

    public Condition(Integer order) {
        this.order = order;
    }

    public enum LogicalOperator {
        and, or
    }
}
