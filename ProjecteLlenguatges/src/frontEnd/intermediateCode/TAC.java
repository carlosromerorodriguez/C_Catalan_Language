package frontEnd.intermediateCode;


import java.util.*;

/**
 * TAC class that contains the TACBlocks of the program and the current label.
 */
public class TAC {
    /**
     * It counts the number of blocks in the program.
     */
    private int blockCounter = 0;
    /**
     * LinkedHashMap that contains the TACBlocks of the program (Key: label, Value: TACBlock)
     */
    private final LinkedHashMap<String, TACBlock> blocks;
    /**
     * String that contains the current label.
     */
    private String currentLabel;

    /**
     * Constructor of the TAC class.
     */
    public TAC() {
        blocks = new LinkedHashMap<>();
    }

    /**
     * Method that adds a block to the TAC.
     * @param block It is the block that is going to be added.
     * @param name It is the name of the block.
     * @return The label of the block in an String format.
     */
    public String addBlock(TACBlock block, String name) {
        String label;
        if (name.equals("LOOP")) {
            label = "LOOP" + blockCounter++; // Si el label es "loop", el label es "LOOP" + blockCounter
        }
        else if (name.equals("false")) {
            label = "L" + blockCounter++; // Si no es una funció o el main, el label es "L" + blockCounter
        }
        else {
            if(name.equals("main")) {
                label = "main";
            } else {
                label = "FUNC_" + name; // Si el label no es "false", el label es "FUNC" + nom de la funció
            }
        }

        block.setLabel(label);
        blocks.put(label, block);
        currentLabel = label;
        return label;
    }

    /**
     * Method to get a block from the TAC by its label.
     * @param label It is the label of the block that is going to be returned.
     * @return The block with the label given.
     */
    public TACBlock getBlock(String label) {
        return blocks.get(label);
    }

    /**
     * Method to get all the blocks of the TAC as a LinkedHashMap.
     * @return The LinkedHashMap with all the blocks of the TAC (Key: label, Value: TACBlock).
     */
    public LinkedHashMap<String, TACBlock> getAllBlocks() {
        return blocks;
    }

    /**
     * Method to get the current label of the TAC.
     * @return The current label of the TAC in a String format.
     */
    public String getCurrentLabel() {
        return currentLabel;
    }

    /**
     * Method to convert an operand to a Type for the TAC.
     * @param operand It is the operand that is going to be converted.
     * @return The new Type of the operand.
     */
    public Type convertOperandToType(String operand) {
        return switch (operand) {
            case "+" -> Type.ADD;
            case "-" -> Type.SUB;
            case "*" -> Type.MUL;
            case "/" -> Type.DIV;
            case "AND" -> Type.AND;
            case "OR" -> Type.OR;
            case "==" -> Type.EQ;
            case "!=" -> Type.NE;
            case "GREATER" -> Type.LT;
            case "GRATER_EQUAL" -> Type.LE;
            case "LOWER" -> Type.GT;
            case "LOWER_EQUAL" -> Type.GE;
            case "RETORN" -> Type.RET;
            default -> Type.UNDEFINED;
        };
    }

    /**
     * Method to print the TAC of the program.
     */
    public void printTAC() {
        for (String label : blocks.keySet()) {
            System.out.println(label + ":");
            blocks.get(label).printBlock();
        }
    }
}
