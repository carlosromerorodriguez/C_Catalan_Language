package backEnd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TACToRISCConverter {
    private final String MIPS_FILE_PATH;
    private Stack<String> freeRegisters; // Registres lliures
    private Map<String, String> variableToRegisterMap; // Mapa de variables a registres
    private Map<String, Integer> variableToStackOffsetMap; // Mapa de variables a offsets de la pila
    private Stack<String> usedRegisters; // Registres que estan en us
    private int stackOffset; // Offset de la pila
    private Map<String, Integer> lastUsedTimeMap = new HashMap<>(); // Mapa de temps de l'últim ús de cada registre
    private int currentTime = 0; //Contador de temps per veure el last used register
    private String currentFunction = null;
    private Set<HashMap<String, String>> functionVariables = new HashSet<>();
    private Map<String, Integer> varNameToOffsetMap = new HashMap<>();
    private int fpOffset = 0;
    private Stack<Integer> fpOffsetStack = new Stack<>();

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
        // Inicialitzem els registres temporals
        for (int i = 9; i >= 0; i--) {
            freeRegisters.push("$t" + i);
        }
    }

    public void convertTAC(LinkedHashMap<String, TACBlock> blocks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MIPS_FILE_PATH))) {
            for (Map.Entry<String, TACBlock> blockEntry : blocks.entrySet()) {
                writer.write(blockEntry.getKey() + ":\n");
                saveStackAndFramePointerIfNeeded(blockEntry.getValue(), writer);

                for (TACEntry entry : blockEntry.getValue().getEntries()) {
                    writer.write(translateToMIPS(entry, writer));
                }
            }
        } catch (IOException e) {
            System.out.println("Error Mips File: " + e.getMessage());
            System.exit(0);
        }

        // Formatejar el codi per fer-ho més llegible
    }

    private void saveStackAndFramePointerIfNeeded(TACBlock value, BufferedWriter writer) throws IOException {
        //Si el value no es L + numero, es una funcio i per tant hem de guardar el frame pointer i el stack pointer
        if(!value.getLabel().matches("L\\d+")) {
            currentFunction = value.getLabel();
            if(!currentFunction.equals("main")) {
                writer.write("sub $sp, $sp, 8\n");
                writer.write("sw $fp, 0($sp)\n");
                writer.write("sw $ra, 4($sp)\n");
            }
            writer.write("move $fp, $sp\n");
        }
    }

    private String translateToMIPS(TACEntry entry, BufferedWriter writer) throws IOException {
        StringBuilder localBuilder = new StringBuilder();

        // Carregar les variables del stack a registres si cal
        if (variableToStackOffsetMap.containsKey(entry.getOperand1())) {
            localBuilder.append(loadFromStack(entry.getOperand1(), writer)).append("\n");
        }
        if (variableToStackOffsetMap.containsKey(entry.getOperand2())) {
            localBuilder.append(loadFromStack(entry.getOperand2(), writer)).append("\n");
        }
        if (variableToStackOffsetMap.containsKey(entry.getDestination())) {
            localBuilder.append(loadFromStack(entry.getDestination(), writer)).append("\n");
        }

        // Afeegim el codi MIPS corresponent a l'entrada TAC
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
            // Busquem el registre menys utilitzat per fer el spill
            String leastUsedReg = findLeastUsedRegister();
            //stackOffset -= 4;

            spillRegisterToStack(leastUsedReg, writer);
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
        //TODO: get temp register
        String destination = varOrReg(entry.getDestination(), writer);

        String mipsCode = switch (entry.getOperation()) {
            case "+" -> "add " + destination + ", " + operand1 + ", " + operand2;
            case "-" -> "sub " + destination + ", " + operand1 + ", " + operand2;
            case "*" -> "mul " + destination + ", " + operand1 + ", " + operand2;
            case "/" -> "div " + destination + ", " + operand1 + ", " + operand2;
            default -> "";
        };

        freeRegister(operand1);
        freeRegister(operand2);
        if(!varNameToOffsetMap.containsKey(entry.getDestination())) {
            stackOffset -= 4;
            varNameToOffsetMap.put(entry.getDestination(), stackOffset);
        }

        return mipsCode;
    }


    private String processCondition(TACEntry entry, BufferedWriter writer) throws IOException {
        String operand1 = varOrReg(entry.getOperand1(), writer);
        String operand2 = varOrReg(entry.getOperand2(), writer);
        //TODO: get temp register
        String destination = entry.getDestination(); // Destino es una etiqueta en este caso

        String mipsCode = switch (entry.getOperation()) {
            case "and" -> "and " + operand1 + ", " + operand1 + ", " + operand2;
            case "or" -> "or " + operand1 + ", " + operand1 + ", " + operand2;
            case "==" -> "beq " + operand1 + ", " + operand2 + ", " + destination;
            case "!=" -> "bne " + operand1 + ", " + operand2 + ", " + destination;
            case "LOWER" -> "blt " + operand1 + ", " + operand2 + ", " + destination;
            case "LOWER_EQUAL" -> "ble " + operand1 + ", " + operand2 + ", " + destination;
            case "GREATER" -> "bgt " + operand1 + ", " + operand2 + ", " + destination;
            case "GREATER_EQUAL" -> "bge " + operand1 + ", " + operand2 + ", " + destination;
            default -> "";
        };

        freeRegister(operand1);
        freeRegister(operand2);
        return mipsCode;
    }


    private String processConditional(TACEntry entry, BufferedWriter writer) throws IOException {
        boolean isNegate = false;
        if(entry.getOperand1().contains("!")) {
            isNegate = true;
        }

        String operand1 = varOrReg(entry.getOperand1(), writer);
        String tempReg = allocateRegister("temp", writer);
        String notCondition = "";

        //Si es negate, fem el not de la condició de l'operand1
        if(isNegate) {
            notCondition = "nor " + tempReg + ", " + operand1 + ", $zero\n";
        }

        notCondition += "bne " + tempReg + ", $zero, " + entry.getDestination();
        freeRegister(tempReg);
        return notCondition;
    }

    private String processCall(TACEntry entry) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> removeRegisters = new ArrayList<>();
        for(String reg : usedRegisters) {
            String varName = getVarnameFromRegister(reg);
            int offset = varNameToOffsetMap.get(varName);
            stringBuilder.append("sw ").append(reg).append(", ").append(offset).append("($fp)\n");
            removeRegisters.add(reg);
        }

        for(String reg: removeRegisters) {
            freeRegister(reg);
        }

        stringBuilder.append("jal ").append(entry.getOperand2()).append("\n");
        return stringBuilder.toString();
    }

    private String processAssignment(TACEntry entry, BufferedWriter writer) throws IOException {
        String src = entry.getOperand1();
        String dest = varOrReg(entry.getDestination(), writer);

        //Mirem si la variable ja ha estat declarada a la funcio actual
        if(currentFunction != null) {
            HashMap<String, String> functionVariablesMap = new HashMap<>();
            functionVariablesMap.put(currentFunction, entry.getDestination());
            if(!functionVariables.contains(functionVariablesMap)) {
                functionVariables.add(functionVariablesMap);
                writer.write("sub $sp, $sp, 4\n");
                varNameToOffsetMap.put(entry.getDestination(), stackOffset);
                variableToRegisterMap.put(entry.getDestination(), dest);
                stackOffset -= 4; // Cada posició de la pila ocupa 4 bytes
            }

        }

        if (isNumeric(src)) {
            writer.write( "li " + dest + ", " + src+"\n");
            return "";
            //return "sw " + dest + ", " + varNameToOffsetMap.get(entry.getDestination()) + "($fp)\n";
        } else {
            String srcReg = varOrReg(src, writer);
            return "move " + dest + ", " + srcReg;
        }
    }

    private String processParameter(TACEntry entry, BufferedWriter writer) throws IOException {
        String param = varOrReg(entry.getOperand2(), writer);
        // Pot ser que tinguem més parametres, a0, a1, a2, a3
        return "move $a0, $zero, " + param;
    }

    private String processReturn(TACEntry entry, BufferedWriter writer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        if (entry.getOperand2() != null && !entry.getOperand2().isEmpty()) {
            stringBuilder.append("move $v0, ").append(varOrReg(entry.getOperand2(), writer)).append("\n");
        }

        List<String> removeRegisters = new ArrayList<>(usedRegisters);

        for(String reg: removeRegisters) {
            freeRegister(reg);
        }

        varNameToOffsetMap = new HashMap<>();
        variableToRegisterMap = new HashMap<>();
        stackOffset = 0;

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
        int offset = varNameToOffsetMap.get(getVarnameFromRegister(reg));

        String code = "sw " + reg + ", " + offset + "($fp)\n";
        writer.write(code);  // Escrivim el codi MIPS per fer el spill

        // Marquem el registre com a lliure
        freeRegister(reg);
    }

    private String getVarnameFromRegister(String reg) {
        for (Map.Entry<String, String> entry : variableToRegisterMap.entrySet()) {
            if (entry.getValue().equals(reg)) {
                return entry.getKey();
            }
        }
        return null;
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
