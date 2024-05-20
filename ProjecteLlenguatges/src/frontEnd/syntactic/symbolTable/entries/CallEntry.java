package frontEnd.syntactic.symbolTable.entries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Call entry.
 */
public class CallEntry extends SymbolTableEntry {
    /**
     * The Parameters.
     */
    private List<List<Object>> parameters;
    /**
     * The Temporal parameters.
     */
    private List<Object> tempParameters;

    /**
     * Instantiates a new Call entry.
     *
     * @param id   the id
     * @param name the name
     * @param line the line
     */
    public CallEntry(UUID id, String name, int line) {
        super(id, name, line);
        parameters = new ArrayList<>();
        tempParameters = new ArrayList<>();
    }

    /**
     * Adds parameter.
     *
     * @param parameter the parameter
     */
    public void addParameter(Object parameter) {
        tempParameters.add(parameter);
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public List<List<Object>> getParameters() {
        return parameters;
    }

    /**
     * Re arrange parameters.
     */
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

    /**
     * Gets temp parameters.
     *
     * @return the temp parameters
     */
    public List<Object> getTempParameters() {
        return tempParameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(List<List<Object>> parameters) {
        this.parameters = parameters;
    }
}
