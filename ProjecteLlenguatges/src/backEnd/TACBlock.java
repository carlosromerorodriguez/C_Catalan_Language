package backEnd;

import frontEnd.syntactic.symbolTable.VariableEntry;

import java.util.ArrayList;
import java.util.List;

public class TACBlock {
    private List<TACEntry> entries;
    private String label;
    private List<VariableEntry> functionArguments;

    public TACBlock() {
        entries = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void add(TACEntry entry) {
        entries.add(entry);
    }

    public void processCondition(String endLabel) {
        for(TACEntry entry : entries) {
            if(entry.getType().equals(Type.CONDITION)) {
                // Afegim el GOTO endLabel
                entry.setDestination(endLabel);
            }
        }
    }

    public void printBlock() {
        for (TACEntry entry : entries) {
            System.out.println("\t"+entry.toString());
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<TACEntry> getEntries() {
        return this.entries;
    }

    public void addArgument(VariableEntry argument) {
        if(functionArguments == null) functionArguments = new ArrayList<>();
        functionArguments.add(argument);
    }

    public List<VariableEntry> getFunctionArguments() {
        return functionArguments;
    }
}
