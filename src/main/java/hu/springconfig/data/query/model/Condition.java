package hu.springconfig.data.query.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hu.springconfig.config.json.ConditionDeserializer;
import lombok.Data;

@Data
@JsonDeserialize(using = ConditionDeserializer.class)
public abstract class Condition {
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
