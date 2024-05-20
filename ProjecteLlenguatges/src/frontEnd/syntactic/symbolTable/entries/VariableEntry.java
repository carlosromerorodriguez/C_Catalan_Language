package frontEnd.syntactic.symbolTable.entries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Variable entry.
 */
public class VariableEntry extends SymbolTableEntry {
    /**
     * The Type.
     */
    public String type;
    /**
     * The Expression.
     */
    public List<Object> expression;
    /**
     * The Past expressions.
     */
    public List<List<Object>> pastExpressions;
    /**
     * The Is argument.
     */
    public final Boolean isArgument;
    /**
     * The Expression already assigned.
     */
    public Boolean expressionAlreadyAssigned;

    /**
     * Instantiates a new Variable entry.
     *
     * @param id         the id
     * @param name       the name
     * @param line       the line
     * @param type       the type
     * @param isArgument the is argument
     */
    public VariableEntry(UUID id, String name, int line, String type, Boolean isArgument) {
        super(id, name, line);
        this.type = type;
        expression = new ArrayList<>();
        this.isArgument = isArgument;
        expressionAlreadyAssigned = false;
        pastExpressions = new ArrayList<>();
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets is argument.
     *
     * @return the is argument
     */
    public Boolean getIsArgument() {
        return isArgument;
    }

    /**
     * Set expression.
     *
     * @param expression the expression
     */
    public void setExpression(List<Object> expression){
        this.expression = expression;
    }

    /**
     * Gets expression.
     *
     * @return the expression
     */
    public List<Object> getExpression() {
        return expression;
    }

    /**
     * Sets expression already assigned.
     *
     * @param expressionAlreadyAssigned the expression already assigned
     */
    public void setExpressionAlreadyAssigned(Boolean expressionAlreadyAssigned) {
        this.expressionAlreadyAssigned = expressionAlreadyAssigned;
    }

    /**
     * Append expression value.
     *
     * @param value the value
     */
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

    /**
     * Gets past expressions.
     *
     * @return the past expressions
     */
    public List<List<Object>> getPastExpressions() {
        return pastExpressions;
    }
}

