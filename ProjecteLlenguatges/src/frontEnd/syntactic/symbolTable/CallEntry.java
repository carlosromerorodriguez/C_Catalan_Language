package frontEnd.syntactic.symbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CallEntry extends SymbolTableEntry{
    private List<List<Object>> parameters;
    private List<Object> tempParameters;

    public CallEntry(UUID id, String name, int line) {
        super(id, name, line);
        parameters = new ArrayList<>();
        tempParameters = new ArrayList<>();
    }

    public void addParameter(Object parameter) {
        tempParameters.add(parameter);
    }

    public List<List<Object>> getParameters() {
        return parameters;
    }

    public void reArrangeParameters() {
        List<Object> currentParameters = new ArrayList<>();
        for (Object parameter : tempParameters) {
            if (parameter.equals(",")) {
                if (!currentParameters.isEmpty()) {
                    parameters.add(currentParameters);
                    currentParameters = new ArrayList<>();
                }
            } else {
                currentParameters.add(parameter);
            }
        }

        if (!currentParameters.isEmpty()) {
            parameters.add(currentParameters);
        }
    }

    public List<Object> getTempParameters() {
        return tempParameters;
    }

    public void setParameters(List<List<Object>> parameters) {
        this.parameters = parameters;
    }
}
