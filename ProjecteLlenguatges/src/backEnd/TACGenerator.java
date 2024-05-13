//TODO: Revertir condicions, ELSE
//TODO: Quan fiquem operands als TACEntry no els fiquem com a ADD sino com a +, etc

package backEnd;

import frontEnd.syntactic.Node;

import java.util.*;

public class TACGenerator {
    private TAC code;
    private TACBlock currentBlock;
    private static int tempCounter;
    private Stack<String> conditionalLabels = new Stack<>();
    private Stack<HashMap<Boolean, String>> wasIterator = new Stack<>();

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
            processNode(child);
        }
    }

    private void processNode(Node child) {
        // Casos que hem de controlar segons el tipus de node
        switch (child.getType().toLowerCase()) {
            case "condicionals": // If
                generateIfCode(child); //DONE
                for(Node grandChild : child.getChildren()) {
                    processNode(grandChild);
                }
                break;
            case "condicional'": // Else
                generateElseCode(child); //DONE
                for(Node grandChild : child.getChildren()) {
                    processNode(grandChild);
                }
                break;
            case "mentre": // While
                generateWhileCode(child); //DONE
                for(Node grandChild : child.getChildren()) {
                    processNode(grandChild);
                }
                break;
            case "per": // For
                generateForCode(child);
                for(Node grandChild : child.getChildren()) {
                    processNode(grandChild);
                }
                break;
            case "assignació":
                generateAssignmentCode(child); //DONE
                break;
            case "condició":
                generateConditionCode(child); //DONE
                break;
            case "retorn":
                if(!child.getChildren().isEmpty()) generateReturnCode(child); //DONE
                break;
            case "crida":
                generateCallCode(child.getChildren().getFirst()); //DONE
                break;
            case "end":
                addEndBlock(); //DONE
            default: //Si no és cap dels anteriors, cridem recursivament la funció per el node actual
                buildTAC(child);
        }
    }

    private void generateConditionCode(Node child) {
        // Generem el codi de la condició explorant recursivament aquella part de l'arbre
        // Mètode especific recursiu per a la condició
        String not = "";
        if(child.getChildren().size() == 2) {
            // Si té dos fills, te un ! davant, ens el quedem i l'eliminem dels fills
            not = child.getChildren().getFirst().getType();
            child.getChildren().removeFirst();
        }

        String condition = not + generateConditionTACRecursive(child);
        TACEntry tacEntry = new TACEntry(Type.CONDITION.getType(), condition, "", "", Type.CONDITION);
        currentBlock.add(tacEntry);
    }

    private String generateConditionTACRecursive(Node node) {
        if ("literal".equalsIgnoreCase(node.getType()) || "var_name".equalsIgnoreCase(node.getType())) {
            return node.getValue().toString();
        } else if ("assignacio_crida".equalsIgnoreCase(node.getType())) {
            return generateFunctionCallRecursive(node);
        } else if ("crida".equalsIgnoreCase(node.getType())) {
            return generateFunctionCallRecursive(node.getChildren().getFirst());
        }

        if(node.getType().equalsIgnoreCase("expressió'") || node.getType().equalsIgnoreCase("terme'") ||
                node.getType().equalsIgnoreCase("condició'") || node.getType().equalsIgnoreCase("oració'") ||
                node.getType().equalsIgnoreCase("avaluació'") || node.getType().equalsIgnoreCase("operació'")
        ) {
            if(node.getChildren().size() == 2) {
                String operator = node.getChildren().get(0).getType();
                String op2 = generateConditionTACRecursive(node.getChildren().get(1));

                return operator + "," + op2;
            }

            String operator = node.getChildren().get(0).getType();
            String op1 = generateConditionTACRecursive(node.getChildren().get(1));
            String recursiveCondition = generateConditionTACRecursive(node.getChildren().get(2));
            if(recursiveCondition.split(",").length == 1) {
                return operator + "," + op1;
            }

            String op2 = recursiveCondition.split(",")[1];
            String recursiveOperand = recursiveCondition.split(",")[0];

            String temp = generateTempVariable();
            TACEntry tacEntry = new TACEntry(recursiveOperand, op1, op2, temp, code.convertOperandToType(operator));
            currentBlock.add(tacEntry);

            return operator + "," + temp;
        }

        if(node.getType().equalsIgnoreCase("expressió") || node.getType().equalsIgnoreCase("terme") ||
                node.getType().equalsIgnoreCase("condició") || node.getType().equalsIgnoreCase("oració") ||
                node.getType().equalsIgnoreCase("avaluació") || node.getType().equalsIgnoreCase("operació")
        ){
            if(node.getChildren().size() == 1) {
                return generateConditionTACRecursive(node.getChildren().getFirst());
            }

            String op1 = generateConditionTACRecursive(node.getChildren().getFirst());
            String recursiveCondition = generateConditionTACRecursive(node.getChildren().getLast());
            if(recursiveCondition.split(",").length == 1) {
                return op1;
            }

            String operator = recursiveCondition.split(",")[0];
            String operand = recursiveCondition.split(",")[1];

            String temp = generateTempVariable();
            TACEntry tacEntry = new TACEntry(operator, op1, operand, temp, code.convertOperandToType(operator));
            currentBlock.add(tacEntry);
            return temp;
        }

        if(node.getType().equalsIgnoreCase("factor") || node.getType().equals("component")) {
            for(Node child : node.getChildren()) {
                if(!child.getType().equals("(") && !child.getType().equals(")")) {
                    return generateConditionTACRecursive(child);
                }
            }
        }

        return "";
    }

    private void addEndBlock() {
        if(!conditionalLabels.isEmpty()) {
            if(!wasIterator.isEmpty()) {
                HashMap<Boolean, String> map = wasIterator.pop();
                if(map.containsKey(true)) {
                    String label = map.get(true);
                    TACEntry tacEntry = new TACEntry(Type.GOTO.getType(), "", label, label, Type.GOTO);
                    currentBlock.add(tacEntry);
                }
            }
            // Creem un nou bloc
            TACBlock endBlock = new TACBlock();
            // Afegim el bloc a la llista de blocs i ens quedem amb l'etiqueta del bloc per al salt de la condició
            String endLabel = code.addBlock(endBlock, "false");

            TACBlock conditionalBlock = code.getBlock(conditionalLabels.pop());
            conditionalBlock.processCondition(endLabel);

            // El bloc actual passa a ser el bloc creat, tenint en compte els punts negatius
            currentBlock = endBlock; //El bloc actual passa a ser el bloc final
        }

    }

    private void generateCallCode(Node node) {
        for(Node child : node.getChildren().getLast().getChildren()) {
            if(!child.getType().equalsIgnoreCase("(") && !child.getType().equalsIgnoreCase(")")) {
                // Pel que retorni cada fill, afegir-lo al bloc actual
                String result = generateExpressionTACRecursive(child);
                //Afegir el resultat al bloc actual
                //Ex: param tx
                TACEntry tacEntry = new TACEntry(Type.PARAM.getType(), "", result, "", Type.PARAM);
                currentBlock.add(tacEntry);
            }
        }

        //Afegir el call al tac
        TACEntry tacEntry = new TACEntry(Type.CALL.getType(), "" , node.getChildren().getFirst().getValue().toString(), "", Type.CALL);
        currentBlock.add(tacEntry);
    }

    private void generateReturnCode(Node node) {
        TACEntry tacEntry = new TACEntry(Type.RET.getType(), "", generateReturnExpressionTACRecursive(node.getChildren().get(1)), "", Type.RET);
        currentBlock.add(tacEntry);
    }

    private String generateReturnExpressionTACRecursive(Node node) {
        if ("literal".equalsIgnoreCase(node.getType()) || "var_name".equalsIgnoreCase(node.getType())) {
            return node.getValue().toString();
        } else if ("assignacio_crida".equalsIgnoreCase(node.getType())) {
            return generateFunctionCallRecursive(node);
        }

        if(node.getType().equalsIgnoreCase("return_expression'") || node.getType().equalsIgnoreCase("return_terme'")) {
            if(node.getChildren().size() == 2) {
                String operator = node.getChildren().get(0).getType();
                String op2 = generateReturnExpressionTACRecursive(node.getChildren().get(1));

                return operator + "," + op2;
            }

            String operator = node.getChildren().get(0).getType();
            String op1 = generateReturnExpressionTACRecursive(node.getChildren().get(1));
            String recursiveExpression = generateReturnExpressionTACRecursive(node.getChildren().get(2));

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

        if(node.getType().equalsIgnoreCase("return_expression") || node.getType().equalsIgnoreCase("return_terme")) {
            String op1 = generateReturnExpressionTACRecursive(node.getChildren().getFirst());
            String recursiveExpression = generateReturnExpressionTACRecursive(node.getChildren().getLast());
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

        if(node.getType().equalsIgnoreCase("return_factor")) {
            for(Node child : node.getChildren()) {
                if(!child.getType().equals("(") && !child.getType().equals(")")) {
                    return generateReturnExpressionTACRecursive(child);
                }
            }
        }

        return "";
    }

    private void generateAssignmentCode(Node child) {
        //Si assignació no té assignació final, no cal generar codi ja que es una declaració
        if(canDiscardAssignment(child)) {
            return;
        }

        generateAssigmentTACRecursive(child);
    }

    private boolean canDiscardAssignment(Node child) {
        for(Node grandChild : child.getChildren()) {
            if(grandChild.getType().equalsIgnoreCase("assignació_final")) {
                return false;
            }
        }
        return true;
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
            TACEntry tacEntry = new TACEntry("", generateExpressionTACRecursive(getFinalAssignation(node)), "", varname, Type.EQU);
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
    }

    private String generateFunctionCallRecursive(Node node) {
        for(Node child : node.getChildren().getLast().getChildren()) {
            if(!child.getType().equalsIgnoreCase("(") && !child.getType().equalsIgnoreCase(")")) {
                // Pel que retorni cada fill, afegir-lo al bloc actual
                String result = generateExpressionTACRecursive(child);

                //Afegir el resultat al bloc actual
                //Ex: param tx
                TACEntry tacEntry = new TACEntry(Type.PARAM.getType(), "", result, "", Type.PARAM);
                currentBlock.add(tacEntry);
            }
        }

        //Afegir el call al tac
        //Ex: tx = call node.getValue()
        String tempVar = generateTempVariable();
        TACEntry tacEntry = new TACEntry(Type.CALL_EXP.getType(), "" , node.getChildren().getFirst().getValue().toString(), tempVar, Type.CALL_EXP);
        currentBlock.add(tacEntry);

        return tempVar;
    }

    private String generateTempVariable() {
        return "t" + (tempCounter++);
    }

    private void generateForCode(Node child) {
        TACBlock forBlock = new TACBlock();
        String label = code.addBlock(forBlock, "false");
        conditionalLabels.push(label);

        currentBlock = forBlock;

        generateForTACCondition(child);
        HashMap<Boolean, String> map = new HashMap<>();
        map.put(true, code.getCurrentLabel());
        wasIterator.push(map);
    }

    private void generateForTACCondition(Node node) {
        // Assignment
        String value = node.getChildren().get(3).getValue().toString();
        String varName = node.getChildren().get(1).getValue().toString();
        TACEntry assignmentEntry = new TACEntry(Type.EQU.getType(), "" , value, varName, Type.EQU);
        currentBlock.add(assignmentEntry);

        // Condition
        String condition = Type.LT.getType();
        String op2 = node.getChildren().get(5).getValue().toString();
        String conditionOp = varName + " " + condition + " " + op2;
        TACEntry conditionEntry = new TACEntry(condition, conditionOp, "", "", Type.LT);
        currentBlock.add(conditionEntry);

        //increment or decrement
        op2 = node.getChildren().get(7).getValue().toString();
        String inc = node.getChildren().get(6).getType();
        if(inc.equals("SUMANT")) {
            condition = "ADD";
        } else {
            condition = "SUB";
        }
        TACEntry incrementEntry = new TACEntry(condition, varName, op2, varName, code.convertOperandToType(condition));
        currentBlock.add(incrementEntry);
    }

    private void generateWhileCode(Node child) {
        // Creem un nou bloc
        TACBlock whileBlock = new TACBlock();
        String label = code.addBlock(whileBlock, "false");
        conditionalLabels.push(label);
        currentBlock = whileBlock;
        HashMap<Boolean, String> map = new HashMap<>();
        map.put(true, label);
        wasIterator.push(map);
    }

    private void generateElseCode(Node child) {
        // Creem un nou bloc
        TACBlock elseBlock = new TACBlock();
        String label = code.addBlock(elseBlock, "false");
        conditionalLabels.push(label);
        currentBlock = elseBlock;

        // Push a new map with false
        HashMap<Boolean, String> map = new HashMap<>();
        map.put(false, label);
        wasIterator.push(map);
    }

    private void generateIfCode(Node child) {
        // Creem un nou bloc
        TACBlock ifBlock = new TACBlock();
        String label = code.addBlock(ifBlock, "false");
        conditionalLabels.push(label);
        currentBlock = ifBlock;

        // Push a new map with false
        HashMap<Boolean, String> map = new HashMap<>();
        map.put(false, label);
        wasIterator.push(map);
    }

    public void printTAC() {
        //code.removeEmptyBlocks();
        code.printTAC();
    }

    public LinkedHashMap<String, TACBlock> getTAC(){
        return this.code.getAllBlocks();
    }
}
