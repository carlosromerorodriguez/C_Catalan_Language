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
        if(!name.equals("false")) label = name; // Si el label no es "false", el label es el nom de la funció o main
        else  label = "L" + blockCounter++; // Si no es una funció o el main, el label es "L" + blockCounter

        blocks.put(label, block);
        currentLabel = label;
        return label;
    }

    public TACBlock getBlock(String label) {
        return blocks.get(label);
    }

    public Map<String, TACBlock> getAllBlocks() {
        return blocks;
    }

    public String getCurrentLabel() {
        return currentLabel;
    }

    public String convertOperandToType(String operand) {
        return switch (operand) {
            case "+" -> "ADD";
            case "-" -> "SUB";
            case "*" -> "MUL";
            case "/" -> "DIV";
            case "&&" -> "AND";
            case "||" -> "OR";
            case "==" -> "EQ";
            case "!=" -> "NE";
            case "<" -> "LT";
            case "<=" -> "LE";
            case ">" -> "GT";
            case ">=" -> "GE";
            case "RETORN" -> "RET";
            default -> operand;
            //Altre casos...
        };
    }

    public void removeEmptyBlocks() {
        //TODO: Si sobre temps només eliminar els blocs buits que no formin part d'un if o while
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
}
