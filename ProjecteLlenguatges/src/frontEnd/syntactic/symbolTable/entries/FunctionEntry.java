package frontEnd.syntactic.symbolTable.entries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Function entry.
 */
public class FunctionEntry extends SymbolTableEntry {
    /**
     * The Return type.
     */
    private String returnType;
    /**
     * The Parameters.
     */
    private List<VariableEntry> parameters;
    /**
     * The Return value.
     */
    private final List<Object> returnValue;

    /**
     * Instantiates a new Function entry.
     *
     * @param id         the id
     * @param name       the name
     * @param line       the line
     * @param returnType the return type
     * @param parameters the parameters
     */
    public FunctionEntry(UUID id, String name, int line, String returnType, List<VariableEntry> parameters) {
        super(id, name, line);
        this.returnType = returnType;
        this.parameters = parameters;
        returnValue = new ArrayList<>();
    }

    /**
     * Gets return type.
     *
     * @return the return type
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Sets return type.
     *
     * @param returnType the return type
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public List<VariableEntry> getParameters() {
        return parameters;
    }

    /**
     * Adds an argument.
     *
     * @param argument the argument
     */
    public void addArgument(VariableEntry argument) {
        parameters.add(argument);
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(List<VariableEntry> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets return value.
     *
     * @return the return value
     */
    public List<Object> getReturnValue() {
        return returnValue;
    }

    /**
     * Append return value.
     *
     * @param value the value
     */
    public void appendReturnValue(Object value) {
        this.returnValue.add(value);
    }


    /**
     * To string method.
     *
     * @param depth the depth
     * @return the string
     */
    @Override
    public String toString(int depth) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FunctionEntry{\n");
        stringBuilder.append("  ".repeat(depth)).append("name=").append(super.getName()).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("type=").append(returnType).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("line=").append(super.getLine()).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("parameters=").append("\n");

        for (VariableEntry var: parameters) {
            stringBuilder.append("  ".repeat(depth)).append(var.toString(depth)).append("\n");
        }

        stringBuilder.append("\t".repeat(depth*2)).append("parameters='").append(parameters).append("\n");
        stringBuilder.append("  ".repeat(depth)).append("return='").append(returnValue).append("\n");
        return stringBuilder.toString();
    }

    /**
     * Gets arguments.
     *
     * @return the arguments
     */
    public List<VariableEntry> getArguments() {
        return parameters;
    }
}
