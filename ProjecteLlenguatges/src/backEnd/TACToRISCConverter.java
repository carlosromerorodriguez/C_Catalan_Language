package backEnd;

import frontEnd.syntactic.symbolTable.VariableEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class TACToRISCConverter {
    private final String MIPS_FILE_PATH;
    private Stack<String> freeRegisters; // Registres lliures
    private LinkedHashMap<String, String> variableToRegisterMap; // Mapa de variables a registres
    private LinkedHashMap<String, Integer> variableToStackOffsetMap; // Mapa de variables a offsets de la pila
    private Stack<String> usedRegisters; // Registres que estan en us
    private int stackOffset; // Offset de la pila
    private Map<String, Integer> lastUsedTimeMap = new HashMap<>(); // Mapa de temps de l'últim ús de cada registre
    private int currentTime = 0; //Contador de temps per veure el last used register
    private String currentFunction = null;
    private Set<HashMap<String, String>> functionVariables = new HashSet<>();
    private LinkedHashMap<String, Integer> varNameToOffsetMap = new LinkedHashMap<>();
    private TACBlock currentBlock;
    private LinkedHashMap<String, TACBlock> code;
    private LinkedHashMap<String, String> registerToValue = new LinkedHashMap<>();
    private List<String> registersToFree = new ArrayList<>();
    private int paramCount = 0;
    private Map<String, Boolean> functionReturns = new HashMap<>();

    public TACToRISCConverter(String path) {
        this.MIPS_FILE_PATH = path;
        this.freeRegisters = new Stack<>();
        this.variableToRegisterMap = new LinkedHashMap<>();
        this.variableToStackOffsetMap = new LinkedHashMap<>();
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
        code = blocks;
        boolean isLastBlock = false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MIPS_FILE_PATH))) {
            for (Map.Entry<String, TACBlock> blockEntry : blocks.entrySet()) {
                writer.write(blockEntry.getKey() + ":\n");
                saveStackAndFramePointerIfNeeded(blockEntry.getValue(), writer);
                currentBlock = blockEntry.getValue();
                isLastBlock = checkLastBlock(blockEntry.getKey());
                for (TACEntry entry : blockEntry.getValue().getEntries()) {
                    writer.write(translateToMIPS(entry, writer));
                }

                if(currentBlock.getLabel().contains("LOOP")) {
                    freeDeadRegisters();
                }

                if(isLastBlock) {
                    writer.write("\nmove $sp, $fp\n");
                    writer.write("jr $ra\n");
                }
                if(lastFunctionBlock()) {
                    clearAndReset();
                }
            }
        } catch (IOException e) {
            System.out.println("Error Mips File: " + e.getMessage());
            System.exit(0);
        }

        // Formatejar el codi per fer-ho més llegible
    }

    private void clearAndReset() {
        freeRegisters = new Stack<>();
        variableToRegisterMap = new LinkedHashMap<>();
        variableToStackOffsetMap = new LinkedHashMap<>();
        usedRegisters = new Stack<>();
        stackOffset = 0;
        lastUsedTimeMap = new HashMap<>();
        currentTime = 0;
        currentFunction = null;
        functionVariables = new HashSet<>();
        varNameToOffsetMap = new LinkedHashMap<>();
        registerToValue = new LinkedHashMap<>();
        registersToFree = new ArrayList<>();
        paramCount = 0;
        functionReturns = new HashMap<>();
        initializeRegisters();
    }

    private boolean lastFunctionBlock() {
        boolean canCheck = false;
        int canCheckCounter = 0;
        for(TACBlock block: code.values()) {
            if(block.getLabel().equals(currentBlock.getLabel())) {
                canCheck = true;
                continue;
            }
            if(canCheck) { //Mirem el seguent block desrpes del actual, si el seguent block comença per FUNC o es main es l'ultim block de la funcio
                return block.getLabel().contains("FUNC") || block.getLabel().equals("main");
            }
        }
        return false;
    }

    private void freeDeadRegisters() {
        for(String reg: registersToFree) {
            if (!freeRegisters.contains(reg)) freeRegister(reg);
        }
        registersToFree = new ArrayList<>();
    }

    private boolean checkLastBlock(String key) {
        boolean isLastBlock = false;
        for (Map.Entry<String, TACBlock> blockEntry : code.entrySet()) {
            isLastBlock = false;
            if(blockEntry.getKey().equals(key)) {
                isLastBlock = true;
            }
        }

        return isLastBlock;
    }

    private void saveStackAndFramePointerIfNeeded(TACBlock value, BufferedWriter writer) throws IOException {
        //Si el value no es L + numero, es una funcio i per tant hem de guardar el frame pointer i el stack pointer
        if(!value.getLabel().matches("L\\d+") && !value.getLabel().contains("LOOP")) {
            currentFunction = value.getLabel();
            if(!currentFunction.equals("main")) {
                writer.write("sub $sp, $sp, 8\n");
                writer.write("sw $fp, 0($sp)\n");
                writer.write("sw $ra, 4($sp)\n");

                // Declarar els arguments de la funció, move $t0, $a0, move $t1, $a1, ...
                for(VariableEntry argument: value.getFunctionArguments()) {
                    String reg = allocateRegister(argument.getName(), writer);
                    writer.write("sub $sp, $sp, 4\n");
                    writer.write("move " + reg + ", $a" + paramCount + "\n");
                    registerToValue.put(reg, argument.getName());
                    paramCount++;
                }
                paramCount = 0;
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
            case CALL, CALL_EXP -> processCall(entry, writer);
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
            if(!usedRegisters.contains(variableToRegisterMap.get(operand))) {
                //Si el seu registre esta free ficar el seu registre, sino fer allocateRegister
                if(freeRegisters.contains(variableToRegisterMap.get(operand))) {
                    if (!operand.matches("t\\d+")) {
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + varNameToOffsetMap.get(operand) + "($fp)\n");
                    }
                    usedRegisters.push(variableToRegisterMap.get(operand));
                    updateRegisterUsage(variableToRegisterMap.get(operand));
                    return variableToRegisterMap.get(operand);
                } else {
                    String reg = allocateRegister(operand, writer);
                    variableToRegisterMap.put(operand, reg);
                    if (!operand.matches("t\\d+")) {
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + varNameToOffsetMap.get(operand) + "($fp)\n");
                    }
                    updateRegisterUsage(reg);
                    return reg;
                }
            } else if (!registerToValue.get(variableToRegisterMap.get(operand)).equals(operand)) {
                if(freeRegisters.contains(variableToRegisterMap.get(operand))) {
                    if (!operand.matches("t\\d+")) {
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + varNameToOffsetMap.get(operand) + "($fp)\n");
                    }
                    usedRegisters.push(variableToRegisterMap.get(operand));
                    updateRegisterUsage(variableToRegisterMap.get(operand));
                    return variableToRegisterMap.get(operand);
                } else {
                    String reg = allocateRegister(operand, writer);
                    variableToRegisterMap.put(operand, reg);
                    if (!operand.matches("t\\d+")) {
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + varNameToOffsetMap.get(operand) + "($fp)\n");
                    }
                    updateRegisterUsage(reg);
                    return reg;
                }
            }
            updateRegisterUsage(variableToRegisterMap.get(operand));
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
        //Si operand1 comença amb $ fem aixo:
        if(operand1.startsWith("$")) {
            registerToValue.put(operand1, entry.getOperand1());
        }

        String operand2 = varOrReg(entry.getOperand2(), writer);
        if(operand2.startsWith("$")) {
            registerToValue.put(operand2, entry.getOperand2());
        }

        String destination = varOrReg(entry.getDestination(), writer);
        if(destination.startsWith("$")) {
            registerToValue.put(destination, entry.getDestination());
        }

        String code = "";
        switch (entry.getOperation()) {
            case "+":
                if (isNumeric(entry.getOperand2()) && isNumeric(entry.getOperand1())) {
                    code = "li " + destination + ", " + entry.getOperand1();
                    code += "\naddi " + destination + ", " + destination + ", " + entry.getOperand2();
                } else if (isNumeric(entry.getOperand1())) {
                    code = "li " + destination + ", " + entry.getOperand1();
                    code += "\nadd " + destination + ", " + destination + ", " + operand2;
                } else if (isNumeric(entry.getOperand2())) {
                    code = "addi " + destination + ", " + operand1 + ", " + entry.getOperand2();
                } else {
                    code = "add " + destination + ", " + operand1 + ", " + operand2;
                }
                break;
            case "-":
                if (isNumeric(entry.getOperand2()) && isNumeric(entry.getOperand1())) {
                    int result = Integer.parseInt(entry.getOperand1()) - Integer.parseInt(entry.getOperand2());
                    code = "li " + destination + ", " + result;  // Carrega el resultat directament al destinació
                } else if (isNumeric(entry.getOperand1())) {
                    //t5 = 2 - t3 -> t5 = (-t3) + 2
                    code = "sub " + operand2 + ", $zero, " + operand2;  // Neguem operand2 i guardem a operand2
                    // Sumem operand1
                    code += "\naddi " + destination + ", " + operand2 + ", " + entry.getOperand1();
                } else if (isNumeric(entry.getOperand2())) {
                    //t5 = t3 - 2 -> t5 = t3 + (-2)
                    code = "addi " + destination + ", " + operand1 + ", -" + entry.getOperand2();
                } else {
                    code = "sub " + destination + ", " + operand1 + ", " + operand2;
                }
                break;
            case "*":
                code = "mul " + destination + ", " + operand1 + ", " + operand2;
                break;
            case "/":
                code = "div " + destination + ", " + operand1 + ", " + operand2;
                break;
            default:
                code = "";
        }

        //Si no surt més endavant podem lliberar el registre
        if(!isNeededLater(entry.getDestination(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(destination);
            else if (!freeRegisters.contains(destination)) freeRegister(destination);
        }

        if(!isNeededLater(entry.getOperand1(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(operand1);
            else if (!freeRegisters.contains(operand1)) freeRegister(operand1);
        }

        if(!isNeededLater(entry.getOperand2(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(operand2);
            else if (!freeRegisters.contains(operand2)) freeRegister(operand2);
        }

        return code;
    }

    private boolean isNeededLater(String checkValue, TACEntry currentEntry) {
        boolean dontCheck = false;
        boolean checkEntry = false;
        boolean isNeeded = false;
        int checkEntryCounter = 0;
        for(TACBlock block: code.values()) {
            if(block.getEntries().equals(currentBlock.getEntries())) {
                dontCheck = true;
            }
            if(dontCheck) {
                for(TACEntry entry: block.getEntries()) {
                    if(entry.equals(currentEntry)) {
                        checkEntry = true;
                    }
                    if(checkEntry) {
                        checkEntryCounter++;
                        if(checkEntryCounter >= 2) {
                            if(entry.getOperand1().equals(checkValue) || entry.getOperand2().equals(checkValue) || entry.getDestination().equals(checkValue)) {
                                isNeeded = true;
                            }
                        }

                    }
                }
            }
        }

        return isNeeded;
    }


    private String processCondition(TACEntry entry, BufferedWriter writer) throws IOException {
        String operand1 = varOrReg(entry.getOperand1(), writer);
        String operand2 = varOrReg(entry.getOperand2(), writer);
        String destination = varOrReg(entry.getDestination(), writer);

        if(destination.startsWith("$")) {
            registerToValue.put(destination, entry.getDestination());
        }

        // Només podem fer operacions logiques amb registres, per tant si tenim un valor numeric el guardem a un registre
        if(isNumeric(operand1)) {
            String randomString = "temp" + UUID.randomUUID();
            operand1 = allocateRegister(randomString, writer); //D'aquesta forma posteriorment sempre farem freeRegister
            writer.write("li " + operand1 + ", " + entry.getOperand1() + "\n");
        }

        if(isNumeric(operand2)) {
            String randomString = "temp" + UUID.randomUUID();
            operand2 = allocateRegister(randomString, writer); //D'aquesta forma posteriorment sempre farem freeRegister
            writer.write("li " + operand2 + ", " + entry.getOperand2() + "\n");

        }

        if(operand1.startsWith("$")) {
            registerToValue.put(operand1, entry.getOperand1());
        }

        if(operand2.startsWith("$")) {
            registerToValue.put(operand2, entry.getOperand2());
        }

        String mipsCode = switch (entry.getOperation()) {
            case "and" -> "and " + operand1 + ", " + operand1 + ", " + operand2;
            case "or" -> "or " + operand1 + ", " + operand1 + ", " + operand2;

            case "==" -> "seq " + destination + ", " + operand1 + ", " + operand2;
            case "!=" -> "sne " + destination + ", " + operand1 + ", " + operand2;
            case "LOWER" -> "slt " + destination + ", " + operand1 + ", " + operand2;
            case "LOWER_EQUAL" -> "sle " + destination + ", " + operand1 + ", " + operand2;
            case "GREATER" -> "sgt " + destination + ", " + operand1 + ", " + operand2;
            case "GREATER_EQUAL" -> "sge " + destination + ", " + operand1 + ", " + operand2;
            default -> "";
        };

        //Si no surt més endavant podem lliberar el registre
        if(!isNeededLater(entry.getDestination(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(destination);
            else if (!freeRegisters.contains(destination)) freeRegister(destination);
        }
        if(!isNeededLater(entry.getOperand1(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(operand1);
            else if (!freeRegisters.contains(operand1)) freeRegister(operand1);
        }
        if(!isNeededLater(entry.getOperand2(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(operand2);
            else if (!freeRegisters.contains(operand2)) freeRegister(operand2);
        }

        return mipsCode;
    }

    private String processConditional(TACEntry entry, BufferedWriter writer) throws IOException {
        boolean isNegate = entry.getOperand1().contains("!");
        String operand1 = entry.getOperand1().replace("!", ""); // Neteja el '!'
        operand1 = varOrReg(operand1, writer);

        if(operand1.startsWith("$")) {
            registerToValue.put(operand1, entry.getOperand1());
        }

        String randomString = "temp" + UUID.randomUUID();
        String tempReg = allocateRegister(randomString, writer);

        String mipsCode = "";
        // Si es negate, fem el NOT de la condició de l'operand1
        if (isNegate) {
            mipsCode += "xori " + tempReg + ", " + operand1 + ", 0x1\n";
        } else {
            mipsCode += "move " + tempReg + ", " + operand1 + "\n";
        }

        mipsCode += "bne " + tempReg + ", $zero, " + entry.getDestination();

        if (!freeRegisters.contains(tempReg)) freeRegister(tempReg);

        if (!isNeededLater(entry.getOperand1(), entry)) {
            if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(operand1);
            else if (!freeRegisters.contains(operand1)) freeRegister(operand1);
        }

        return mipsCode;
    }

    private String processCall(TACEntry entry, BufferedWriter writer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> removeRegisters = new ArrayList<>();
        Set<String> usedRegister = new HashSet<>();

        // Convertim el conjunt de claus a una llista
        List<String> keys = new ArrayList<>(varNameToOffsetMap.keySet());
        // Invertim l'ordre de la llista
        Collections.reverse(keys);

        // Ara recorrem la llista invertida
        for (String var : keys) {
            String reg = variableToRegisterMap.get(var);
            if (!usedRegister.contains(reg)) {
                usedRegister.add(reg);
                stringBuilder.append("sw ").append(reg).append(", ").append(varNameToOffsetMap.get(var)).append("($fp)\n");
                removeRegisters.add(reg);
            }
        }

        for(String reg: removeRegisters) {
            if (!freeRegisters.contains(reg)) freeRegister(reg);
        }

        // Recorrem el registerToValue i buidem tot el que te cada registre per obligar a fer un lw quan es necessari
        for(Map.Entry<String, String> entryReg: registerToValue.entrySet()) {
            registerToValue.put(entryReg.getKey(), "");
        }

        String functionName = entry.getOperand2();
        stringBuilder.append("jal ").append(functionName).append("\n");

        if(entry.getDestination() != null) {
            String reg = varOrReg(entry.getDestination(), writer);
            stringBuilder.append("move ").append(reg).append(", $v0\n");
            registerToValue.put(reg, entry.getDestination());
        }

        return stringBuilder.toString();
    }

    private String processAssignment(TACEntry entry, BufferedWriter writer) throws IOException {
        String src = entry.getOperand1();
        String dest = varOrReg(entry.getDestination(), writer);
        if(dest.startsWith("$")) {
            registerToValue.put(dest, entry.getDestination());
        }

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
            if(!isNeededLater(entry.getDestination(), entry)) {
                if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(dest);
                else if (!freeRegisters.contains(dest)) freeRegister(dest);
            }
            return "";
            //return "sw " + dest + ", " + varNameToOffsetMap.get(entry.getDestination()) + "($fp)\n";
        } else {
            String srcReg = varOrReg(src, writer);
            if(!isNeededLater(src, entry)) {
                if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(srcReg);
                else if (!freeRegisters.contains(srcReg)) freeRegister(srcReg);
            }

            return "move " + dest + ", " + srcReg;
        }
    }

    private String processParameter(TACEntry entry, BufferedWriter writer) throws IOException {
        String param = varOrReg(entry.getOperand2(), writer);
        // Pot ser que tinguem més parametres, a0, a1, a2, a3
        String paramReturn = "";

        if(isNumeric(param)) {
            param = allocateRegister(param, writer);
            writer.write("li " + param + ", " + entry.getOperand2() + "\n");
        }

        if (paramCount < 4) {
            paramReturn = "move $a" + paramCount + ", " + param;
            paramCount++;
        }

        if(!freeRegisters.contains(param)) freeRegister(param);

        return paramReturn;
    }

    private String processReturn(TACEntry entry, BufferedWriter writer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        if (entry.getOperand2() != null && !entry.getOperand2().isEmpty()) {
            stringBuilder.append("move $v0, ").append(varOrReg(entry.getOperand2(), writer)).append("\n");
        }

        List<String> removeRegisters = new ArrayList<>(usedRegisters);

        for(String reg: removeRegisters) {
            if (!freeRegisters.contains(reg)) freeRegister(reg);
        }

        varNameToOffsetMap = new LinkedHashMap<>();
        variableToRegisterMap = new LinkedHashMap<>();
        stackOffset = 0;

        stringBuilder.append("jr $ra\n");
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
        if (!freeRegisters.contains(reg)) freeRegister(reg);
    }

    private String getVarnameFromRegister(String reg) {
        for (Map.Entry<String, String> entry : variableToRegisterMap.entrySet()) {
            if (entry.getValue().equals(reg) && !entry.getKey().matches("t\\d+")) {
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
