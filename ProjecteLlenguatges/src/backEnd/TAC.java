package backEnd;


import java.util.*;



public class TAC {
    private int blockCounter = 0;
    private LinkedHashMap<String, TACBlock> blocks;
    private String currentLabel;

    public TAC() {
        blocks = new LinkedHashMap<>();
    }

    public String addBlock(TACBlock block, String name) {
        String label;
        if (name.equals("LOOP")) {
            label = "LOOP" + blockCounter++; // Si el label es "loop", el label es "LOOP" + blockCounter
        }
        else if (name.equals("false")) {
            label = "L" + blockCounter++; // Si no es una funció o el main, el label es "L" + blockCounter
        }
        else {
            label = name; // Si el label no es "false", el label es el nom de la funció o main
        }

        block.setLabel(label);
        blocks.put(label, block);
        currentLabel = label;
        return label;
    }

    public TACBlock getBlock(String label) {
        return blocks.get(label);
    }

    public LinkedHashMap<String, TACBlock> getAllBlocks() {
        return blocks;
    }

    public String getCurrentLabel() {
        return currentLabel;
    }

    public Type convertOperandToType(String operand) {
        return switch (operand) {
            case "+" -> Type.ADD;
            case "-" -> Type.SUB;
            case "*" -> Type.MUL;
            case "/" -> Type.DIV;
            case "&&" -> Type.AND;
            case "||" -> Type.OR;
            case "==" -> Type.EQ;
            case "!=" -> Type.NE;
            case "GREATER" -> Type.LT;
            case "GRATER_EQUAL" -> Type.LE;
            case "LOWER" -> Type.GT;
            case "LOWER_EQUAL" -> Type.GE;
            case "RETORN" -> Type.RET;
            default -> Type.UNDEFINED;
            //Altre casos...
        };
    }



    public void removeEmptyBlocks() {
        List<String> toRemove = new ArrayList<>();
        for (String label : blocks.keySet()) {
            if (blocks.get(label).isEmpty()) {
                toRemove.add(label);
            }
        }

        for (String label : toRemove) {
            blocks.remove(label);
        }
    }

    public void printTAC() {
        for (String label : blocks.keySet()) {
            System.out.println(label + ":");
            blocks.get(label).printBlock();
        }
    }

    public int getBlockCounter() {
        return blockCounter;
    }
}
