package frontEnd.intermediateCode;

import global.symbolTable.entries.VariableEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * TACBlock class that contains the TACEntries of the block, the label of the block,
 * the arguments of the function and the function arguments if the block is a function.
 */
public class TACBlock {
    /**
     * List that contains the TACEntries of the block.
     */
    private final List<TACEntry> entries;
    /**
     * String that contains the label of the block.
     */
    private String label;
    /**
     * List that contains the arguments of the function if the block is a function.
     */
    private List<VariableEntry> functionArguments;

    /**
     * Constructor of the TACBlock class.
     */
    public TACBlock() {
        entries = new ArrayList<>();
    }

    /**
     * Method to get the label of the block.
     * @return The label of the block in a String format.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Method to set the label of the block.
     * @param label It is the label that is going to be set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Method to add a TACEntry to the block.
     * @param entry It is the TACEntry that is going to be added.
     */
    public void add(TACEntry entry) {
        entries.add(entry);
    }

    /**
     * Method to process the condition of the block.
     * @param endLabel It is the label where the block is going to jump if the condition is true.
     */
    public void processCondition(String endLabel) {
        for(TACEntry entry : entries) {
            if(entry.getType().equals(Type.CONDITION)) {
                // Afegim el GOTO endLabel
                entry.setDestination(endLabel);
            }
        }
    }

    /**
     * Method to print the block.
     */
    public void printBlock() {
        for (TACEntry entry : entries) {
            System.out.println("\t"+entry.toString());
        }
    }

    /**
     * Method to get the TACEntries of the block.
     * @return The TACEntries of the block in a List format.
     */
    public List<TACEntry> getEntries() {
        return this.entries;
    }

    /**
     * Method to add an argument to the function if the block is a function.
     * @param argument It is the argument that is going to be added.
     */
    public void addArgument(VariableEntry argument) {
        if(functionArguments == null) functionArguments = new ArrayList<>();
        functionArguments.add(argument);
    }

    /**
     * Method to get the arguments of the function if the block is a function.
     * @return The arguments of the function in a List format.
     */
    public List<VariableEntry> getFunctionArguments() {
        return functionArguments;
    }
}
