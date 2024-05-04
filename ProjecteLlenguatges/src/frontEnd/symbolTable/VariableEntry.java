package frontEnd.symbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VariableEntry extends SymbolTableEntry {
    private String type;
    private List<Object> expression;
    private final Boolean isArgument;
    private Boolean expressionAlreadyAssigned;

    public VariableEntry(UUID id, String name, int line, String type, Boolean isArgument) {
        super(id, name, line);
        this.type = type;
        expression = new ArrayList<>();
        this.isArgument = isArgument;
        expressionAlreadyAssigned = false;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Object> getExpression() {
        return expression;
    }

    public void setExpressionAlreadyAssigned(Boolean expressionAlreadyAssigned) {
        this.expressionAlreadyAssigned = expressionAlreadyAssigned;
    }

    public void appendExpressionValue(Object value) {
        if(expressionAlreadyAssigned){
            expression = new ArrayList<>();
            expressionAlreadyAssigned = false;
        }
        this.expression.add(value);
    }
}

