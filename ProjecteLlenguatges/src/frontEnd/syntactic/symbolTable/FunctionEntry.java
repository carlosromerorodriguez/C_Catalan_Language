package frontEnd.syntactic.symbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FunctionEntry extends SymbolTableEntry {
    private String returnType;
    private List<VariableEntry> parameters;
    private final List<Object> returnValue;

    public FunctionEntry(UUID id, String name, int line, String returnType, List<VariableEntry> parameters) {
        super(id, name, line);
        this.returnType = returnType;
        this.parameters = parameters;
        returnValue = new ArrayList<>();
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<VariableEntry> getParameters() {
        return parameters;
    }

    public void addArgument(VariableEntry argument) {
        parameters.add(argument);
    }

    public void setParameters(List<VariableEntry> parameters) {
        this.parameters = parameters;
    }

    public List<Object> getReturnValue() {
        return returnValue;
    }

    public void appendReturnValue(Object value) {
        this.returnValue.add(value);
    }


    @Override
    public String toString(int depth) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FunctionEntry{\n");
        stringBuilder.append(" ".repeat(depth*2)).append("name='").append(super.getName()).append("\n");
        stringBuilder.append(" ".repeat(depth*2)).append("type='").append(returnType).append("\n");
        stringBuilder.append(" ".repeat(depth*2)).append("line='").append(super.getLine()).append("\n");
        //stringBuilder.append("\t".repeat(depth*2)).append("parameters='").append(parameters).append("\n");
        stringBuilder.append(" ".repeat(depth*2)).append("return='").append(returnValue).append("\n");
        return stringBuilder.toString();
    }
}
