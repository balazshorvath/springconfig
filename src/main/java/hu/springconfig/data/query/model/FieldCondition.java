package hu.springconfig.data.query.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hu.springconfig.util.SpecificationsUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

/**
 * Defines a condition for a field for supported types check
 * {@link SpecificationsUtils#createPredicateField(FieldCondition, Root, CriteriaBuilder)}
 *
 * For foreign tables use fieldName.otherField
 * Example: user.id
 */
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
