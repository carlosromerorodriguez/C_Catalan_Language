package frontEnd.syntactic.symbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VariableEntry extends SymbolTableEntry {
    private String type;
    private List<Object> expression;
    private List<List<Object>> pastExpressions;
    private final Boolean isArgument;
    private Boolean expressionAlreadyAssigned;

    public VariableEntry(UUID id, String name, int line, String type, Boolean isArgument) {
        super(id, name, line);
        this.type = type;
        expression = new ArrayList<>();
        this.isArgument = isArgument;
        expressionAlreadyAssigned = false;
        pastExpressions = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsArgument() {
        return isArgument;
    }

    public void setExpression(List<Object> expression){
        this.expression = expression;
    }

    public List<Object> getExpression() {
        return expression;
    }

    public void setExpressionAlreadyAssigned(Boolean expressionAlreadyAssigned) {
        this.expressionAlreadyAssigned = expressionAlreadyAssigned;
    }

    public void appendExpressionValue(Object value) {
        if(expressionAlreadyAssigned){
            pastExpressions.add(expression);
            expression = new ArrayList<>();
            expressionAlreadyAssigned = false;
        }
        this.expression.add(value);
    }
    @Override
    public String toString(int depth) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("VariableEntry{\n");
        stringBuilder.append("  ".repeat(depth)).append("name=").append(super.getName()).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("type=").append(type).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("line=").append(super.getLine()).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("expression=").append(expression).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("isArgument=").append(isArgument).append("\n");
        return stringBuilder.toString();
    }


}

