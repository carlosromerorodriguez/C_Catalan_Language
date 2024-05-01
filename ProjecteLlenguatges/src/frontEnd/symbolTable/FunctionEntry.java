package frontEnd.symbolTable;

import java.util.List;

public class FunctionEntry extends SymbolTableEntry {
    private String returnType;
    private List<VariableEntry> parameters;

    public FunctionEntry(UUID id, String name, int line, String returnType, List<VariableEntry> parameters) {
        super(id, name, line);
        this.returnType = returnType;
        this.parameters = parameters;
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

    @Override
    public String toString() {
        return "FunctionEntry{" +
                "name='" + getName() + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
