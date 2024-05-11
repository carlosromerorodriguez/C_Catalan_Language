package backEnd;

import frontEnd.syntactic.Node;

import java.util.*;

public class TACGenerator {
    private TAC code;
    private TACBlock currentBlock;
    private static int tempCounter;
    private final Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
            "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL",
            "GREATER_EQUAL", "!", "==", "!=",
            "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
            "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT"
    ));

    public TACGenerator() {
        this.code = new TAC();
    }

    public void generateTAC(Node rootNode) {
        if (rootNode.getType().equals("sortida")) {
            for (Node child : rootNode.getChildren()) {
                switch (child.getType()) {
                    case "llista de funcions" ->  // Si el fill és una llista de funcions, pot ser que hi hagi més d'una funció
                            generateTACForFunctions(child);
                    case "funcio" -> {
                        currentBlock = new TACBlock();
                        String functionName = getFunctionName(child);

                        //TODO: Cal procesar els arguments?

                        assert functionName != null;
                        code.addBlock(currentBlock, functionName);
                        buildTAC(child);  // Si el fill és el main, generem el tac per al main
                    }
                    case "main" -> {
                        currentBlock = new TACBlock(); //Creem un nou bloc

                        code.addBlock(currentBlock, "main"); // Afegim el bloc a la llista de blocs

                        buildTAC(child);  // Si el fill és el main, generem el tac per al main
                    }
                }
            }
        } else {
            System.out.println("Error: Invalid syntax tree");
        }
    }

    private String getFunctionName(Node child) {
        for (Node grandChild : child.getChildren()) {
            if (grandChild.getType().equalsIgnoreCase("FUNCTION_NAME")) {
                return (String) grandChild.getValue();
            }
        }
        return null;
    }

    private void generateTACForFunctions(Node node) {
        for (Node child : node.getChildren()) {
            if(child.getType().equalsIgnoreCase("funcio")) { // Si és funció generem el tac per la funció
                currentBlock = new TACBlock();
                String functionName = getFunctionName(child);

                //TODO: Cal procesar els arguments?

                assert functionName != null;
                code.addBlock(currentBlock, functionName);
                buildTAC(child);
            } else if(child.getType().equals("llista de funcions")) {
                // Si un dels fills és una llista de funcions, cridem recursivament la funció ja que vol dir que hi ha més d'una funció
                generateTACForFunctions(child);
            } else {
                System.out.println("Error: Invalid syntax tree"); // Si no és cap de les anteriors, error
            }
        }
    }

    private void buildTAC(Node node) {
        for (Node child : node.getChildren()) {
            // Casos que hem de controlar segons el tipus de node
            switch (child.getType().toLowerCase()) {
                case "condicionals": // If
                    generateIfCode(child);
                    break;
                case "condicional'": // Else
                    generateElseCode(child);
                    break;
                case "mentre": // While
                    generateWhileCode(child);
                    break;
                case "per": // For
                    generateForCode(child);
                    break;
                case "assignació":
                    generateAssignmentCode(child);
                    break;
                case "condició":
                    generateConditionCode(child);
                    break;
                case "retorn":
                    generateReturnCode(child);
                    break;
                case "crida":
                    generateCallCode(child);
                    break;
                case "end":
                    addEndBlock();
                default: //Si no és cap dels anteriors, cridem recursivament la funció per a cada fill
                    for(Node grandChild : child.getChildren()) {
                        buildTAC(grandChild);
                    }
            }
        }
    }

    private void generateConditionCode(Node child) {
        // Generem el codi de la condició explorant recursivament aquella part de l'arbre
        // Mètode especific recursiu per a la condició
    }

    private void addEndBlock() { //Punts negatius: sempre s'afegirà un bloc final, encara que no sigui necessari
        // Creem un nou bloc
        TACBlock endBlock = new TACBlock();
        // Afegim el bloc a la llista de blocs i ens quedem amb l'etiqueta del bloc per al salt de la condició
        String endLabel = code.addBlock(endBlock, "false");

        // Afegim el salt al final del bloc actual
        //Afegim el codi de de saltar si es compleix la condició
        // Ex: if num < 1 goto [L2] (afegim el L2 a la condició del bloc actual si s'escau, es pot donar el cas
        // que el bloc actual no sigui una condició)
        currentBlock.processCondition(endLabel);

        // El bloc actual passa a ser el bloc creat, tenint en compte els punts negatius
        currentBlock = endBlock; //El bloc actual passa a ser el bloc final
    }

    private void generateCallCode(Node child) {
        // Generar el codi de la crida explorant recursivament aquella part de l'arbre
        // Processem la crida recursivament
        // Afegim la crida al bloc actual
    }

    private void generateReturnCode(Node child) {
        // Crear un nou bloc
        // Actualitzar el bloc actual
        // Processem el return code recursivament
        // Mètode específic recursiu per al return
        // Afegir el returnCode al bloc actual

    }

    private boolean canDiscardAssignment(Node child) {
        for(Node grandChild : child.getChildren()) {
            if(grandChild.getType().equalsIgnoreCase("assignació_final")) {
                return false;
            }
        }
        return true;
    }

    private void generateAssignmentCode(Node child) {
        //Si assignació no té assignació final, no cal generar codi ja que es una declaració
        if(canDiscardAssignment(child)) {
            return;
        }

        // Generar el codi de l'assignació recursivament i afegir-lo al bloc actual
        // Mètode específic recursiu per a l'assignació
        // (contemplar assignacio_crida per a la crida de funcions)
        generateAssigmentTACRecursive(child);
    }

    private Node getFinalAssignation(Node node) {
        for(Node child : node.getChildren()) {
            if(child.getType().equalsIgnoreCase("assignació_final")) {
                return child.getChildren().getLast();
            }
        }
        return node;
    }

    private void generateAssigmentTACRecursive(Node node) {
        //Explorem recursivament fins trobar un node que com a fill tingui VAR_NAME i assignació_final
        if(isAssignation(node)) {
            String varname = getVarName(node);
            TACEntry tacEntry = new TACEntry("", generateExpressionTACRecursive(getFinalAssignation(node)), "", varname, "=");
            currentBlock.add(tacEntry);
        }
        for(Node child : node.getChildren()) {
            generateAssigmentTACRecursive(child);
        }
    }

    private Boolean isAssignation(Node node) {
        int count = 0;
        for(Node child : node.getChildren()) {
            if(child.getType().equalsIgnoreCase("assignació_final") || child.getType().equalsIgnoreCase("VAR_NAME")) {
                count++;
                if(count == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getVarName(Node node) {
        for(Node child : node.getChildren()) {
            if(child.getType().equalsIgnoreCase("VAR_NAME")) {
                return (String) child.getValue();
            }
        }
        return "";
    }



    private String generateExpressionTACRecursive(Node node) {
        if ("literal".equalsIgnoreCase(node.getType()) || "var_name".equalsIgnoreCase(node.getType())) {
            return node.getValue().toString();
        } else if ("assignacio_crida".equalsIgnoreCase(node.getType())) {
            return generateFunctionCallRecursive(node);
        }

        if(node.getType().equalsIgnoreCase("expressió'") || node.getType().equalsIgnoreCase("terme'")) {
            if(node.getChildren().size() == 2) {
                String operator = node.getChildren().get(0).getType();
                String op2 = generateExpressionTACRecursive(node.getChildren().get(1));

                return operator + "," + op2;
            }

            String operator = node.getChildren().get(0).getType();
            String op1 = generateExpressionTACRecursive(node.getChildren().get(1));
            String recursiveExpression = generateExpressionTACRecursive(node.getChildren().get(2));

            if(!recursiveExpression.contains(",")) {
                return operator + "," + op1;
            }

            String op2 = recursiveExpression.split(",")[1];
            String recursiveOperand = recursiveExpression.split(",")[0];


            String temp = generateTempVariable();
            TACEntry tacEntry = new TACEntry(recursiveOperand, op1, op2, temp, code.convertOperandToType(operator));
            currentBlock.add(tacEntry);

            return operator + "," + temp;
        }

        if(node.getType().equalsIgnoreCase("expressió") || node.getType().equalsIgnoreCase("terme")) {
            String op1 = generateExpressionTACRecursive(node.getChildren().getFirst());
            String recursiveExpression = generateExpressionTACRecursive(node.getChildren().getLast());
            if(!recursiveExpression.contains(",")) {
                return op1;
            }
            String operator = recursiveExpression.split(",")[0];
            String operand = recursiveExpression.split(",")[1];

            String temp = generateTempVariable();
            TACEntry tacEntry = new TACEntry(operator, op1, operand, temp, code.convertOperandToType(operator));
            currentBlock.add(tacEntry);
            return temp;
        }

        if(node.getType().equalsIgnoreCase("factor")) {
            for(Node child : node.getChildren()) {
                if(!child.getType().equals("(") && !child.getType().equals(")")) {
                    return generateExpressionTACRecursive(child);
                }
            }
        }

        return "";

        /*List<String> operands = new ArrayList<>();
        String operator = "";
        for (Node child : node.getChildren()) {
            String result = generateExpressionTACRecursive(child);
            if (isOperator(child.getType())) {
                if (!operands.isEmpty() && !operator.isEmpty()) {
                    result = handleBinaryOperation(operands.removeLast(), operator, result);
                    operator = ""; // Reset operator after use
                } else {
                    operator = child.getType(); // Save the operator for future use
                }
            } else {
                //result = generateExpressionTACRecursive(child);
            }
            operands.add(result);
        }

        System.out.println("Operands: " + operands);
        System.out.println("Operator: " + operator);

        // If there's still an operator left, process remaining operands
        if (!operator.isEmpty() && operands.size() >= 2) {
            String leftOperand = operands.remove(operands.size() - 2);
            String rightOperand = node.getParent().getChildren().get(1).getValue() != null ?
                    node.getParent().getChildren().get(1).getValue().toString() : node.getParent().getChildren().get(1).getType();
            return handleBinaryOperation(rightOperand, operator, leftOperand);
        }

        return operands.isEmpty() ? "" : operands.getLast(); // Return the last processed operand*/
    }

    private String generateFunctionCallRecursive(Node node) {
        if(node.getType().equalsIgnoreCase("expressió")) {
            return generateExpressionTACRecursive(node);
        }

        if(node.getType().equalsIgnoreCase("arguments_dins_crida")) {
            for(Node child : node.getChildren()) {
                // Pel que retorni cada fill, afegir-lo al bloc actual
                String result = generateFunctionCallRecursive(child);
                //Afegir el resultat al bloc actual
                //Ex: param tx
                TACEntry tacEntry = new TACEntry("PARAM", result, "", "", "PARAM");
                currentBlock.add(tacEntry);
                return "";
            }
        }

        if(node.getType().equalsIgnoreCase("FUNCTION_NAME")) {
            //Afegir el call al tac
            //Ex: tx = call node.getValue()
            String tempVar = generateTempVariable();
            TACEntry tacEntry = new TACEntry("CALL", node.getValue().toString(), "", tempVar, "CALL");
            currentBlock.add(tacEntry);
            return "";
        }

        for(Node child : node.getChildren()) {
            return generateFunctionCallRecursive(child);
        }

        return "";
    }

    private boolean isOperator(String type) {
        return "+".equals(type) || "-".equals(type) || "*".equals(type) || "/".equals(type);
    }

    private String handleBinaryOperation(String leftOperand, String operator, String rightOperand) {
        String tempVar = generateTempVariable();

        TACEntry tacEntry = new TACEntry(operator, rightOperand, leftOperand, tempVar, code.convertOperandToType(operator));
        currentBlock.add(tacEntry);
        return tempVar;
    }

    private String generateTempVariable() {
        return "t" + (tempCounter++);
    }

    private void generateForCode(Node child) {
        // Creem un nou bloc
        // Generem el codi de la condicióm i l'afegim al bloc
        // Explorem recursivament el cos del for
    }

    private void generateWhileCode(Node child) {
        // Creem un nou bloc
        // Generem el codi de la condició i l'afegim al bloc
        // Explorem recursivament el cos del while

    }

    private void generateElseCode(Node child) {
        // Creem un nou bloc
        // Explorem recursivament el cos de l'else

    }

    private void generateIfCode(Node child) {
        // Creem un nou bloc
        // Generem el codi de la condició i l'afegim al bloc
        // Ex:
        // L1:
        //      if num < 1 goto L2
        //      ... cos de l'if

        // Explorem recursivament el cos de l'if

    }

    public void printTAC() {
        //code.removeEmptyBlocks();
        code.printTAC();
    }
}
