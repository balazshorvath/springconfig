package hu.springconfig.data.query.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.query.model.ConditionSet;
import hu.springconfig.data.query.model.FieldCondition;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConditionDeserializer extends StdDeserializer<Condition> {

    protected ConditionDeserializer() {
        super(Condition.class);
    }

    protected ConditionDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected ConditionDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public Condition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        Condition condition = null;

        if (node.has("fieldName")) {
            condition = p.getCodec().treeToValue(node, FieldCondition.class);
            // If this is a date, create the date object for the specifications
            if (node.get("value").isTextual()) {
                try {
                    String val = node.get("value").textValue();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date date = formatter.parse(val);
                    ((FieldCondition) condition).setValue(date);
                } catch (ParseException e) {
                    // Do nothing
                }
            }
        } else if (node.has("logicalOperator")) {
            condition = p.getCodec().treeToValue(node, ConditionSet.class);
        }
        // If the json doesn't have any of the fields, might be an empty, or a bad json
        return condition;
    }

}
