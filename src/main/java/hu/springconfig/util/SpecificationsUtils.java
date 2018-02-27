package hu.springconfig.util;

import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.query.model.ConditionSet;
import hu.springconfig.data.query.model.FieldCondition;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpecificationsUtils {

    public static <T> Specification<T> withQuery(Condition condition) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (condition != null) {
                if (condition instanceof ConditionSet) {
                    predicate = createPredicateSet((ConditionSet) condition, root, cb);
                } else if (condition instanceof FieldCondition) {
                    predicate = createPredicateField((FieldCondition) condition, root, cb);
                }
            }
            return predicate;
        };
    }

    public static <T> Predicate createPredicateSet(ConditionSet c, Root<T> root, CriteriaBuilder cb) {
        Predicate predicate = null;
        if (c.getConditions() == null) {
            return cb.conjunction();
        }
        List<Predicate> predicates = new ArrayList<>();
        for (Condition condition : c.getConditions()) {
            Predicate tmp = null;
            if (condition instanceof ConditionSet) {
                tmp = createPredicateSet((ConditionSet) condition, root, cb);
            } else if (condition instanceof FieldCondition) {
                tmp = createPredicateField((FieldCondition) condition, root, cb);
            }
            if (tmp != null) {
                predicates.add(tmp);
            }
        }
        switch (c.getLogicalOperator()) {
            case and:
                predicate = cb.and(predicates.toArray(new Predicate[0]));
                break;
            case or:
                predicate = cb.or(predicates.toArray(new Predicate[0]));
                break;
        }
        return predicate;
    }

    /**
     * Supported field types:
     * Any primitive and their class pair.
     * Date
     * String
     *
     * @param fieldCondition
     * @param root
     * @param cb
     * @param <T>
     * @return
     */
    public static <T> Predicate createPredicateField(FieldCondition fieldCondition, Root<T> root, CriteriaBuilder cb) {
        Predicate predicate = null;
        Path path = joinFields(fieldCondition.getFieldName(), root);
        Class<?> type = path.getJavaType();
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            predicate = getBoolean(fieldCondition.getRelationalOperator(),
                    (Boolean) fieldCondition.getValue(), path, cb);
        } else if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            predicate = getNumber(fieldCondition.getRelationalOperator(),
                    (Number) fieldCondition.getValue(), path, cb);
        } else if (type.equals(Date.class)) {
            predicate = getDate(fieldCondition.getRelationalOperator(),
                    (Date) fieldCondition.getValue(), path, cb);
        } else { //if (type.equals(String.class)) {
            // This is a bit bad, but in case there's an enum for example, this will work
            // Other cases should result in error anyways
            predicate = getString(fieldCondition.getRelationalOperator(),
                    (String) fieldCondition.getValue(), path, cb);
        }

        return predicate;
    }

    private static Predicate getDate(FieldCondition.RelationalOperator operator, Date value, Path<? extends Date> path, CriteriaBuilder cb) {
        Predicate predicate = null;
        switch (operator) {
            case ne:
                predicate = cb.notEqual(path, value);
                break;
            case lt:
                predicate = cb.lessThan(path, value);
                break;
            case gt:
                predicate = cb.greaterThan(path, value);
                break;
            case eq:
            default:
                predicate = cb.equal(path, value);
        }
        return predicate;
    }

    private static Predicate getBoolean(FieldCondition.RelationalOperator operator, Boolean value, Path<? extends Boolean> path, CriteriaBuilder cb) {
        Predicate predicate = null;
        switch (operator) {
            case ne:
                predicate = cb.notEqual(path, value);
                break;
            case eq:
            default:
                predicate = cb.equal(path, value);
        }
        return predicate;
    }

    private static Predicate getNumber(FieldCondition.RelationalOperator operator, Number value, Path<? extends Number> path, CriteriaBuilder cb) {
        Predicate predicate = null;
        switch (operator) {
            case ne:
                predicate = cb.notEqual(path, value);
                break;
            case lt:
                predicate = cb.lt(path, value);
                break;
            case gt:
                predicate = cb.gt(path, value);
                break;
            case eq:
            default:
                predicate = cb.equal(path, value);
        }
        return predicate;
    }

    private static Predicate getString(FieldCondition.RelationalOperator operator, String value, Path<? extends String> path, CriteriaBuilder cb) {
        String queryString = value.toLowerCase();
        Expression<String> field = cb.lower((Path<String>) path);
        switch (operator) {
            case contains:
                queryString = "%" + queryString + "%";
                break;
            case startswith:
                queryString = queryString + "%";
                break;
            case endswith:
                queryString = "%" + queryString;
                break;
        }
        return cb.like(field, queryString);
    }

    private static <T> Path joinFields(String fieldName, Root<T> root) {
        if (fieldName.contains(".")) {
            String[] joins = fieldName.split("\\.");
            int last = joins.length - 1;
            Join join = root.join(joins[0]);
            for (int i = 1; i < last; i++) {
                join = join.join(joins[i]);
            }
            return join.get(joins[last]);
        }
        return root.get(fieldName);
    }
}
