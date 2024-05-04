package frontEnd.symbolTable;

import frontEnd.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FunctionEntry extends SymbolTableEntry {
    private String returnType;
    private List<VariableEntry> parameters;
    private List<Object> returnValue;
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
    public String toString() {
        return "FunctionEntry{" +
                "name='" + getName() + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
