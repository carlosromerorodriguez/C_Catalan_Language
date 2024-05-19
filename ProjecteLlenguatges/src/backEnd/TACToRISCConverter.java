package backEnd;

import frontEnd.syntactic.symbolTable.VariableEntry;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  TACToRISCConverter {
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
    private HashMap<String, Boolean> alreadyReservedStack = new HashMap<>();
    private int paramCount = 0;
    private int stackDecrement;
    private List<String> functionNames = new ArrayList<>();

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
            writer.write("j main\n\n");
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
                    writer.write("\nli $v0, 10\n");
                    writer.write("syscall # Finalitzem el programa\n");
                }
                if(lastFunctionBlock()) {
                    clearAndReset();
                }
            }
        } catch (IOException e) {
            System.out.println("Error Mips File: " + e.getMessage());
            System.exit(0);
        }
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
        alreadyReservedStack = new HashMap<>();
        stackDecrement = 0;
        initializeRegisters();
    }

    private boolean lastFunctionBlock() {
        boolean canCheck = false;
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
            stackDecrement = 0;
            stackOffset = 0;
            functionNames.add(currentFunction);
            if(!currentFunction.equals("main")) {
                int size = value.getFunctionArguments().size();
                int currentStack = 8 + (size * 4);
                writer.write("sub $sp, $sp, "+currentStack+"\n");
                writer.write("sw $fp, 0($sp)\n");
                writer.write("sw $ra, 4($sp)\n");

                stackDecrement += 8;

                for(VariableEntry argument: value.getFunctionArguments()) {
                    String reg;
                    switch (paramCount) {
                        case 0: reg = "$a0"; break;
                        case 1: reg = "$a1"; break;
                        case 2: reg = "$a2"; break;
                        case 3: reg = "$a3"; break;
                        default: reg ="";
                    }
                    alreadyReservedStack.put(argument.getName(), true);
                    registerToValue.put(reg, argument.getName());
                    varNameToOffsetMap.put(argument.getName(), stackDecrement);
                    variableToRegisterMap.put(argument.getName(), reg);
                    //usedRegisters.push(reg);
                    stackDecrement += 4;
                    paramCount++;
                    //Fiquem els arguments a la pila
                    writer.write("sw " + reg + ", " + (stackDecrement - 4) + "($sp)\n");
                }

                writer.write("move $fp, $sp\n");
                stackOffset -= 4;

                paramCount = 0;
            } else {
                writer.write("move $fp, $sp\n");
            }
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
            //Si el registre es $a0, $a1, $a2, $a3 retornar el registre
            if(variableToRegisterMap.get(operand).matches("\\$a\\d+")) {
                if(registerToValue.get(variableToRegisterMap.get(operand)).equals(operand)) {
                    return variableToRegisterMap.get(operand);
                } else {
                    String reg = variableToRegisterMap.get(operand);
                    writer.write("lw " + reg + ", " + varNameToOffsetMap.get(operand) + "($fp)\n");
                    variableToRegisterMap.put(operand, reg);
                    registerToValue.put(reg, operand);
                    return reg;
                }
            }

            if(!usedRegisters.contains(variableToRegisterMap.get(operand))) {
                //Si el seu registre esta free ficar el seu registre, sino fer allocateRegister
                if(freeRegisters.contains(variableToRegisterMap.get(operand))) {
                    if (!operand.matches("t\\d+")) {
                        int realOffset = varNameToOffsetMap.get(operand); // Si hem decrementat el stack, cal ajustar l'offset
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + realOffset + "($fp)\n");
                    }
                    usedRegisters.push(variableToRegisterMap.get(operand));
                    updateRegisterUsage(variableToRegisterMap.get(operand));
                    return variableToRegisterMap.get(operand);
                } else {
                    String reg = allocateRegister(operand, writer);
                    variableToRegisterMap.put(operand, reg);
                    if (!operand.matches("t\\d+")) {
                        int realOffset = varNameToOffsetMap.get(operand); // Si hem decrementat el stack, cal ajustar l'offset
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + realOffset + "($fp)\n");
                    }
                    updateRegisterUsage(reg);
                    return reg;
                }
            } else if (!registerToValue.get(variableToRegisterMap.get(operand)).equals(operand)) {
                if(freeRegisters.contains(variableToRegisterMap.get(operand))) {
                    if (!operand.matches("t\\d+")) {
                        int realOffset = varNameToOffsetMap.get(operand); // Si hem decrementat el stack, cal ajustar l'offset
                        writer.write("lw " + variableToRegisterMap.get(operand) + ", " + realOffset + "($fp)\n");
                    }
                    usedRegisters.push(variableToRegisterMap.get(operand));
                    updateRegisterUsage(variableToRegisterMap.get(operand));
                    return variableToRegisterMap.get(operand);
                } else {
                    String reg = allocateRegister(operand, writer);
                    variableToRegisterMap.put(operand, reg);
                    //teniem un match amb tnum
                    int realOffset = varNameToOffsetMap.get(operand); // Si hem decrementat el stack, cal ajustar l'offset
                    writer.write("lw " + variableToRegisterMap.get(operand) + ", " + realOffset + "($fp)\n");

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
            if(!reg.matches("a\\d+")) { //Si es un registre $a no el tenim en compte
                Integer lastUsedTime = lastUsedTimeMap.get(reg);
                if (lastUsedTime != null && lastUsedTime < oldestTime) {
                    oldestTime = lastUsedTime;
                    leastUsedReg = reg;
                }
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

        for (String var : keys) {
            String reg = variableToRegisterMap.get(var);
            //Si el reg es un $a l'omitim
            if(reg.matches("\\$a\\d+")) continue;
            if (!usedRegister.contains(reg)) {
                usedRegister.add(reg);
                if(!alreadyReservedStack.containsKey(var)) {
                    stringBuilder.append("sub $sp, $sp, 4\n");
                    alreadyReservedStack.put(var, true);
                }
                stringBuilder.append("sw ").append(reg).append(", ").append(varNameToOffsetMap.get(var)).append("($fp)\n");
                removeRegisters.add(reg);
            }
        }

        for(String reg: removeRegisters) {
            if (!freeRegisters.contains(reg)) freeRegister(reg);
        }

        String functionName = entry.getOperand2();
        stringBuilder.append("jal ").append(functionName).append("\n");

        String reg = "";
        if(entry.getDestination() != null) {
            reg = varOrReg(entry.getDestination(), writer);
            stringBuilder.append("move ").append(reg).append(", $v0\n");
            registerToValue.put(reg, entry.getDestination());
            varNameToOffsetMap.put(entry.getDestination(), stackDecrement);
            stackOffset -= 4;
        }

        List<String> regs = new ArrayList<>(registerToValue.keySet());

        // Recorrem el registerToValue i buidem tot el que te cada registre per obligar a fer un lw quan es necessari
        for (String reg2: regs) {
            if(!reg2.equalsIgnoreCase(reg)) registerToValue.put(reg2, "");
        }

        paramCount = 0;

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
                varNameToOffsetMap.put(entry.getDestination(), stackDecrement);
                variableToRegisterMap.put(entry.getDestination(), dest);
                stackOffset -= 4; // Cada posició de la pila ocupa 4 bytes
                stackDecrement += 4;
                alreadyReservedStack.put(entry.getDestination(), true);
            }
        }

        if (isNumeric(src)) {
            writer.write( "li " + dest + ", " + src);
            if(!isNeededLater(entry.getDestination(), entry)) {
                if(currentBlock.getLabel().contains("LOOP")) registersToFree.add(dest);
                else if (!freeRegisters.contains(dest)) freeRegister(dest);
            }
            return "";
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
            String reg = varOrReg(entry.getOperand2(), writer);
            stringBuilder.append("move $v0, ").append(reg).append("\n");
        }

        List<String> removeRegisters = new ArrayList<>(usedRegisters);

        for(String reg: removeRegisters) {
            if (!freeRegisters.contains(reg)) freeRegister(reg);
        }

        varNameToOffsetMap = new LinkedHashMap<>();
        variableToRegisterMap = new LinkedHashMap<>();
        stackOffset = 0;

        writer.write("move $sp, $fp\n");
        writer.write("lw $fp, 0($sp)\n");
        writer.write("lw $ra, 4($sp)\n");
        writer.write("lw $a0, 8($sp)\n");
        writer.write("addi $sp, $sp, 12\n");

        stringBuilder.append("jr $ra\n");
        stringBuilder.append("# END_FUNC\n");
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

        if(!alreadyReservedStack.containsKey(getVarnameFromRegister(reg))) {
            alreadyReservedStack.put(getVarnameFromRegister(reg), true);
            writer.write("sub $sp, $sp, 4\n");
        }

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
        int realOffset = offset; // Si hem decrementat el stack, cal ajustar l'offset
        String reg = allocateRegister(var, writer);
        return "lw " + reg + ", " + realOffset + "($sp)";
    }

    private void reprocessFunction() throws IOException {
        File inputFile = new File(this.MIPS_FILE_PATH);
        File tempFile = new File(this.MIPS_FILE_PATH + ".temp");
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String line;
        boolean inFunction = false;
        int totalSub = 0;
        List<String> functionContent = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String finalLine = line;
            boolean isFunctionStart = functionNames.stream().anyMatch(name -> finalLine.contains(name + ":"));
            boolean isFunctionEnd = line.trim().equals("# END_FUNC");
            boolean isLabel = line.trim().matches("L\\d+:");  // Regex to match labels like L0:, L1:, etc.

            if (isFunctionStart) {
                if (inFunction) {
                    functionContent.add("\tsub $sp, $sp, " + totalSub);
                    functionContent.add("\taddi $sp, $sp, " + totalSub);
                    for (String content : functionContent) {
                        writer.write(content + "\n");
                    }
                    functionContent.clear();
                    totalSub = 0;
                }
                inFunction = true;
                functionContent.add(line); // Start of the function label
                continue;
            }

            if (inFunction && isFunctionEnd) {
                functionContent.add(1, "\tsub $sp, $sp, " + totalSub); // Insert at the start of the function content
                functionContent.add("\taddi $sp, $sp, " + totalSub); // Add addi just before the end mark
                functionContent.add(line); // End mark
                for (String content : functionContent) {
                    writer.write(content + "\n");
                }
                functionContent.clear();
                inFunction = false;
                totalSub = 0;
                continue;
            }

            if (inFunction) {
                Matcher subMatcher = Pattern.compile("sub\\s+\\$sp,\\s+\\$sp,\\s+(\\d+)").matcher(line);
                if (subMatcher.find()) {
                    totalSub += Integer.parseInt(subMatcher.group(1));
                    continue; // Do not add sub lines to function content
                }
                functionContent.add((isLabel ? "\t" : "\t") + line); // Add a tab for labels and regular lines within function
            } else {
                writer.write(line + "\n"); // Write non-function lines directly to file
            }
        }

        // Write any remaining function content
        if (!functionContent.isEmpty()) {
            functionContent.add(1, "\tsub $sp, $sp, " + totalSub); // Correct placement of the sub instruction
            functionContent.add("\taddi $sp, $sp, " + totalSub);
            for (String content : functionContent) {
                writer.write(content + "\n");
            }
        }

        reader.close();
        writer.close();

        if (!inputFile.delete()) {
            System.out.println("Could not delete the original file");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename the temp file");
        }
    }

    public void reprocessSubValues() {
        try {
            reprocessFunction();
        } catch (IOException e) {
            System.out.println("Error reprocessing MIPS file");
        }
    }
}
