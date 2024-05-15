package backEnd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TACToRISCConverter {
    private final String MIPS_FILE_PATH;
    private Stack<String> freeRegisters;
    private Map<String, String> variableToRegisterMap;
    private Map<String, Integer> variableToStackOffsetMap;
    private Stack<String> usedRegisters;
    private int stackOffset;
    private Map<String, Integer> lastUsedTimeMap = new HashMap<>();
    private int currentTime = 0; // Este contador global ayudará a rastrear el último uso de cada registro.

    public TACToRISCConverter(String path) {
        this.MIPS_FILE_PATH = path;
        this.freeRegisters = new Stack<>();
        this.variableToRegisterMap = new HashMap<>();
        this.variableToStackOffsetMap = new HashMap<>();
        this.usedRegisters = new Stack<>();
        this.stackOffset = 0;
        initializeRegisters();
    }

    private void initializeRegisters() {
        // Inicializar la pila de registros temporales ($t0 - $t9)
        for (int i = 9; i >= 0; i--) {
            freeRegisters.push("$t" + i);
        }
    }

    public void convertTAC(LinkedHashMap<String, TACBlock> blocks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MIPS_FILE_PATH))) {
            for (Map.Entry<String, TACBlock> blockEntry : blocks.entrySet()) {
                writer.write(blockEntry.getKey() + ":\n");
                for (TACEntry entry : blockEntry.getValue().getEntries()) {
                    writer.write(translateToMIPS(entry, writer));
                }
            }
        } catch (IOException e) {
            System.out.println("Error Mips File: " + e.getMessage());
            System.exit(0);
        }
    }

    private String translateToMIPS(TACEntry entry, BufferedWriter writer) throws IOException {
        StringBuilder localBuilder = new StringBuilder();

        // Cargar variables desde la pila si es necesario
        if (variableToStackOffsetMap.containsKey(entry.getOperand1())) {
            localBuilder.append(loadFromStack(entry.getOperand1(), writer)).append("\n");
        }
        if (variableToStackOffsetMap.containsKey(entry.getOperand2())) {
            localBuilder.append(loadFromStack(entry.getOperand2(), writer)).append("\n");
        }
        if (variableToStackOffsetMap.containsKey(entry.getDestination())) {
            localBuilder.append(loadFromStack(entry.getDestination(), writer)).append("\n");
        }

        // Append the result of the switch to the local StringBuilder
        localBuilder.append(switch (entry.getType()) {
            case ADD, SUB, MUL, DIV -> processOperation(entry, writer);
            case AND, OR, EQ, NE, LT, LE, GT, GE -> processCondition(entry, writer);
            case CONDITION -> processConditional(entry, writer);
            case CALL, CALL_EXP -> processCall(entry);
            case EQU -> processAssignment(entry, writer);
            case PARAM -> processParameter(entry, writer);
            case RET -> processReturn(entry, writer);
            case GOTO -> processGoto(entry);
            default -> "";
        }).append("\n");

        return localBuilder.toString();
    }

    private String varOrReg(String operand, BufferedWriter writer) throws IOException {
        if (variableToRegisterMap.containsKey(operand)) {
            return variableToRegisterMap.get(operand);
        } else if (isNumeric(operand)) {
            return operand;
        } else {
            String reg = allocateRegister(operand, writer);
            variableToRegisterMap.put(operand, reg);
            return reg;
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String allocateRegister(String var, BufferedWriter writer) throws IOException {
        if (freeRegisters.isEmpty()) {
            if (usedRegisters.isEmpty()) {
                throw new RuntimeException("Attempted to allocate register but no registers are free and no registers are in use to spill.");
            }
            // Encontrar el registro menos usado para hacer spill
            String leastUsedReg = findLeastUsedRegister();
            spillRegisterToStack(leastUsedReg, writer);
            // Una vez realizado el spill, liberamos el registro
            freeRegister(leastUsedReg);
        }
        String reg = freeRegisters.pop();
        usedRegisters.push(reg);
        updateRegisterUsage(reg);
        variableToRegisterMap.put(var, reg);
        return reg;
    }

    private String findLeastUsedRegister() {
        String leastUsedReg = null;
        int oldestTime = Integer.MAX_VALUE;
        for (String reg : usedRegisters) {
            Integer lastUsedTime = lastUsedTimeMap.get(reg);
            if (lastUsedTime != null && lastUsedTime < oldestTime) {
                oldestTime = lastUsedTime;
                leastUsedReg = reg;
            }
        }
        return leastUsedReg;
    }

    private void freeRegister(String reg) {
        if (usedRegisters.contains(reg)) {
            usedRegisters.remove(reg);
            freeRegisters.push(reg);
        }
    }


    private String processOperation(TACEntry entry, BufferedWriter writer) throws IOException {
        String operand1 = varOrReg(entry.getOperand1(), writer);
        String operand2 = varOrReg(entry.getOperand2(), writer);
        String destination = varOrReg(entry.getDestination(), writer);

        String mipsCode = switch (entry.getOperation()) {
            case "+" -> "add " + destination + ", " + operand1 + ", " + operand2;
            case "-" -> "sub " + destination + ", " + operand1 + ", " + operand2;
            case "*" -> "mul " + destination + ", " + operand1 + ", " + operand2;
            case "/" -> "div " + destination + ", " + operand1 + ", " + operand2;
            case "GREATER" -> "slt " + destination + ", " + operand2 + ", " + operand1; // operand1 > operand2
            default -> "";
        };

        freeRegister(operand1);
        freeRegister(operand2);
        return mipsCode;
    }


    private String processCondition(TACEntry entry, BufferedWriter writer) throws IOException {
        String operand1 = varOrReg(entry.getOperand1(), writer);
        String operand2 = varOrReg(entry.getOperand2(), writer);
        String destination = entry.getDestination(); // Destino es una etiqueta en este caso

        String mipsCode = switch (entry.getOperation()) {
            case "&&" -> "and " + operand1 + ", " + operand1 + ", " + operand2;
            case "||" -> "or " + operand1 + ", " + operand1 + ", " + operand2;
            case "==" -> "beq " + operand1 + ", " + operand2 + ", " + destination;
            case "!=" -> "bne " + operand1 + ", " + operand2 + ", " + destination;
            case "<" -> "blt " + operand1 + ", " + operand2 + ", " + destination;
            case "<=" -> "ble " + operand1 + ", " + operand2 + ", " + destination;
            case ">" -> "bgt " + operand1 + ", " + operand2 + ", " + destination;
            case ">=" -> "bge " + operand1 + ", " + operand2 + ", " + destination;
            default -> "";
        };

        freeRegister(operand1);
        freeRegister(operand2);
        return mipsCode;
    }


    private String processConditional(TACEntry entry, BufferedWriter writer) throws IOException {
        String operand1 = varOrReg(entry.getOperand1(), writer);
        String tempReg = allocateRegister("temp", writer);
        String notCondition = "nor " + tempReg + ", " + operand1 + ", $zero\n";
        notCondition += "bne " + tempReg + ", $zero, " + entry.getDestination();
        freeRegister(tempReg);
        return notCondition;
    }

    private String processCall(TACEntry entry) {
        return "jal " + entry.getOperand1();
    }

    private String processAssignment(TACEntry entry, BufferedWriter writer) throws IOException {
        String src = entry.getOperand1();
        String dest = varOrReg(entry.getDestination(), writer);

        if (isNumeric(src)) {
            return "li " + dest + ", " + src;
        } else {
            String srcReg = varOrReg(src, writer);
            return "move " + dest + ", " + srcReg;
        }
    }

    private String processParameter(TACEntry entry, BufferedWriter writer) throws IOException {
        String param = varOrReg(entry.getOperand1(), writer);
        return "addi $a0, $zero, " + param;
    }

    private String processReturn(TACEntry entry, BufferedWriter writer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        if (entry.getOperand2() != null && !entry.getOperand2().isEmpty()) {
            stringBuilder.append("move $v0, ").append(varOrReg(entry.getOperand2(), writer)).append("\n");
        }
        stringBuilder.append("jr $ra");
        return stringBuilder.toString();
    }

    private String processGoto(TACEntry entry) {
        return "j " + entry.getDestination();
    }

    private void updateRegisterUsage(String reg) {
        lastUsedTimeMap.put(reg, currentTime++);
    }

    private void spillRegisterToStack(String reg, BufferedWriter writer) throws IOException {
        int offset = stackOffset;
        variableToStackOffsetMap.put(reg, offset);
        stackOffset += 4;  // Assume 4 bytes per stack slot for simplicity

        String code = "sw " + reg + ", " + offset + "($sp)\n";
        writer.write(code);  // Write directly to the MIPS output file

        // Este registro ya no está en uso, así que lo movemos de vuelta a los libres
        freeRegister(reg);
    }

    private String loadFromStack(String var, BufferedWriter writer) throws IOException {
        if (!variableToStackOffsetMap.containsKey(var)) {
            throw new IllegalStateException("Attempting to load a variable that was not spilled.");
        }
        int offset = variableToStackOffsetMap.get(var);
        String reg = allocateRegister(var, writer);
        return "lw " + reg + ", " + offset + "($sp)";
    }
}
