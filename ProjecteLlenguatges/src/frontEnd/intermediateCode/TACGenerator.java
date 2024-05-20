//TODO: Revertir condicions nor $t0, $t1, $zero

package frontEnd.intermediateCode;

import frontEnd.syntactic.Node;
import global.symbolTable.*;
import global.symbolTable.entries.FunctionEntry;
import global.symbolTable.entries.SymbolTableEntry;
import global.symbolTable.entries.VariableEntry;

import java.util.*;

/**
 * TACGenerator class that generates the TAC of the program from the Syntax Tree.
 */
public class TACGenerator {
    /**
     * TAC object that contains the TAC of the program.
     */
    private final TAC code;
    /**
     * TACBlock object that contains the current block.
     */
    private TACBlock currentBlock;
    /**
     * TACBlock object that contains the last block.
     */
    private TACBlock lastBlock;
    /**
     * Integer variable that contains the counter of the temporary variables.
     */
    private static int tempCounter;
    /**
     * Stack that contains the labels of the conditionals.
     */
    private final Stack<String> conditionalLabels;
    /**
     * Stack that contains the labels of the iterators.
     */
    private final Stack<HashMap<Boolean, String>> wasIterator;
    /**
     * Stack that contains the endIf blocks.
     */
    private final Stack<TACBlock> endIfs = new Stack<>();

    /**
     * Constructor of the TACGenerator class.
     */
    public TACGenerator() {
        this.code = new TAC();
        this.conditionalLabels = new Stack<>();
        this.wasIterator = new Stack<>();
    }

    /**
     * Method to generate the TAC of the program.
     * @param rootNode It is the root node of the Parse Tree.
     */
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

    /**
     * Method to get the name of the function.
     * @param child It is the node that contains the function name.
     * @return The name of the function.
     */
    private String getFunctionName(Node child) {
        for (Node grandChild : child.getChildren()) {
            if (grandChild.getType().equalsIgnoreCase("FUNCTION_NAME")) {
                return (String) grandChild.getValue();
            }
        }
        return null;
    }

    /**
     * Method to generate the TAC for the functions.
     * @param node It is the node that contains the functions.
     */
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

    /**
     * Method to build the TAC of the program.
     * @param node It is the node that is going to be processed.
     */
    private void buildTAC(Node node) {
        for (Node child : node.getChildren()) {
            processNode(child);
        }
    }

    /**
     * Method to process the node of the Syntax Tree.
     * @param child It is the node that is going to be processed.
     */
    private void processNode(Node child) {
        // Casos que hem de controlar segons el tipus de node
        switch (child.getType().toLowerCase()) {
            case "condicionals": // If
                generateIfCode(); //DONE
                for(Node grandChild : child.getChildren()) {
                    processNode(grandChild);
                }
                break;
            case "condicional'": // Else
                generateElseCode(); //DONE
                for(Node grandChild : child.getChildren()) {
                    processNode(grandChild);
                }
                break;
            case "mentre": // While
                generateWhileCode(); //DONE
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
                addEndBlock();
                break;//DONE
            case "endif":
                addEndIfBlock();
                break;
            case "endelse":
                addEndElseBlock();
                break;
            case "print":
                generatePrintCode(child);
                break;
            default: //Si no és cap dels anteriors, cridem recursivament la funció per el node actual
                buildTAC(child);
        }
    }

    /**
     * Method to generate the TAC for the Print instruction of the program.
     * @param node It is the node that contains the print instruction.
     */
    private void generatePrintCode(Node node) {
        if (node.getType().equalsIgnoreCase("string") || node.getType().equalsIgnoreCase("var_name")) {
            TACEntry tacEntry;
            if (node.getType().equalsIgnoreCase("var_name")) {
                tacEntry = new TACEntry(Type.PRINT.getType(), "", node.getType() + "(" + node.getValue().toString() + ")", "", Type.PRINT);
            } else {
                tacEntry = new TACEntry(Type.PRINT.getType(), "", node.getValue().toString(), "", Type.PRINT);
            }
            this.currentBlock.add(tacEntry);
        }
        for(Node child : node.getChildren()) {
            if(!child.getType().equalsIgnoreCase("(") && !child.getType().equalsIgnoreCase(")")) {
                if (child.getType().equalsIgnoreCase("string") || child.getType().equalsIgnoreCase("var_name")) {
                    TACEntry tacEntry;
                    if (child.getType().equalsIgnoreCase("var_name")) {
                        tacEntry = new TACEntry(Type.PRINT.getType(), "", child.getType() + "(" + child.getValue().toString() + ")", "", Type.PRINT);
                    } else {
                        tacEntry = new TACEntry(Type.PRINT.getType(), "", child.getValue().toString(), "", Type.PRINT);
                    }
                    this.currentBlock.add(tacEntry);
                }
                for (Node grandChild : child.getChildren()) {
                    generatePrintCode(grandChild);
                }
            }
        }
    }

    /**
     * Method to calculate the end of the else block.
     */
    private void addEndElseBlock() {
        lastBlock = currentBlock;
        // Creem un nou bloc
        TACBlock endBlock = new TACBlock();
        // Afegim el bloc a la llista de blocs i ens quedem amb l'etiqueta del bloc per al salt de la condició
        String endLabel = code.addBlock(endBlock, "false");

        TACBlock conditionalBlock = code.getBlock(conditionalLabels.pop());
        conditionalBlock.processCondition(endLabel);

        // El bloc actual passa a ser el bloc creat, tenint en compte els punts negatius
        currentBlock = endBlock; //El bloc actual passa a ser el bloc final

        if (!endIfs.isEmpty()) {
            TACBlock endIfBlock = endIfs.pop();
            String label = endIfBlock.getLabel();

            try {
                int labelNumber = Integer.parseInt(label.substring(1)) - 1;  // Substring per a eliminar 'L' i parsejar el número.
                String newLabel = "L" + labelNumber;  // Creem la nova label amb el número reduït.

                TACBlock ifBodyBlock = code.getBlock(newLabel);  // Obtenim el bloc utilitzant la nova label
                if (ifBodyBlock != null) {
                    TACEntry tacEntry = new TACEntry(Type.GOTO.getType(), "", endLabel, endLabel, Type.GOTO);
                    ifBodyBlock.add(tacEntry);
                } else {
                    System.err.println("Bloc no trobat per a la label: " + newLabel);
                }
            } catch (NumberFormatException e) {
                System.err.println("Format de label incorrecte: " + label);
            }
        }
    }

    /**
     * Method to generate the TAC for the If instruction of the program.
     */
    private void addEndIfBlock() {
        lastBlock = currentBlock;
        // Creem un nou bloc
        TACBlock endIfBlock = new TACBlock();
        // Afegim el bloc a la llista de blocs i ens quedem amb l'etiqueta del bloc per al salt de la condició
        String endLabel = code.addBlock(endIfBlock, "false");

        TACBlock conditionalBlock = code.getBlock(conditionalLabels.pop());
        conditionalBlock.processCondition(endLabel);
        endIfs.push(endIfBlock);

        // El bloc actual passa a ser el bloc creat, tenint en compte els punts negatius
        currentBlock = endIfBlock; //El bloc actual passa a ser el bloc final
    }

    /**
     * Method to generate the TAC for the If-Else instruction of the program.
     * @param child It is the node that contains the If instruction.
     */
    private void generateConditionCode(Node child) {
        // Generem el codi de la condició explorant recursivament aquella part de l'arbre
        // Mètode especific recursiu per a la condició
        boolean isNegate = false;
        if(child.getChildren().size() == 2) {
            // Si té dos fills, te un ! davant, ens el quedem i l'eliminem dels fills
            if(child.getChildren().getFirst().getType().equals("!")) {
                child.getChildren().removeFirst();
                isNegate = true;
            }
        }

        String condition;
        if(isNegate) {
            condition = generateConditionTACRecursive(child);
        } else {
            condition = "!" + generateConditionTACRecursive(child);
        }
        TACEntry tacEntry = new TACEntry(Type.CONDITION.getType(), condition, "", "", Type.CONDITION);
        currentBlock.add(tacEntry);
    }

    /**
     * Method to generate the TAC for the If-Else instruction of the program.
     * @param node It is the node that contains the If-Else instruction.
     */
    private String generateConditionTACRecursive(Node node) {
        if ("literal".equalsIgnoreCase(node.getType()) || "var_name".equalsIgnoreCase(node.getType())) {
            if(node.getValue() instanceof Boolean) {
                return node.getValue() == Boolean.TRUE ? "1" : "0";
            }
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

    /**
     * Method to add the end block.
     */
    private void addEndBlock() {
        lastBlock = currentBlock;
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

    /**
     * Method to generate the call instruction of the program.
     * @param node It is the node that contains the call instruction.
     */
    private void generateCallCode(Node node) {
        for(Node child : node.getChildren().getLast().getChildren()) {
            if(!child.getType().equalsIgnoreCase("(") && !child.getType().equalsIgnoreCase(")")) {
                // Pel que retorni cada fill, afegir-lo al bloc actual
                generateArgumentTACRecursive(child);
            }
        }

        //Afegir el call al tac
        String functionName = "FUNC_" + node.getChildren().getFirst().getValue().toString();
        TACEntry tacEntry = new TACEntry(Type.CALL.getType(), "" , functionName, "", Type.CALL);
        currentBlock.add(tacEntry);
    }

    /**
     * Method to generate the Return instruction of the function.
     * @param node It is the node that contains the Return instruction.
     */
    private void generateReturnCode(Node node) {
        TACEntry tacEntry = new TACEntry(Type.RET.getType(), "", generateReturnExpressionTACRecursive(node.getChildren().get(1)), "", Type.RET);
        currentBlock.add(tacEntry);
    }

    /**
     * Method to generate the Return expression of the function.
     * @param node It is the node that contains the Return expression.
     * @return The Return expression in a String format.
     */
    private String generateReturnExpressionTACRecursive(Node node) {
        if ("literal".equalsIgnoreCase(node.getType()) || "var_name".equalsIgnoreCase(node.getType())) {
            if(node.getValue() instanceof Boolean) {
                return node.getValue() == Boolean.TRUE ? "1" : "0";
            }
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

    /**
     * Method to make an assignment of a variable.
     * @param child It is the node that contains the assignment.
     */
    private void generateAssignmentCode(Node child) {
        //Si assignació no té assignació final, no cal generar codi ja que es una declaració
        if(canDiscardAssignment(child)) {
            return;
        }

        generateAssignmentTACRecursive(child);
    }

    /**
     * Method to know if the assignment can be discarded.
     * @param child It is the node that contains the assignment.
     * @return True if the assignment can be discarded, false otherwise.
     */
    private boolean canDiscardAssignment(Node child) {
        for(Node grandChild : child.getChildren()) {
            if(grandChild.getType().equalsIgnoreCase("assignació_final")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to get the final assignation of the node.
     * @param node It is the node that contains the assignation.
     * @return Node that contains the final assignation.
     */
    private Node getFinalAssignation(Node node) {
        for(Node child : node.getChildren()) {
            if(child.getType().equalsIgnoreCase("assignació_final")) {
                return child.getChildren().getLast();
            }
        }
        return node;
    }

    /**
     * Method to generate the TAC for the If instruction of the program.
     * @param node It is the node that contains the If instruction.
     */
    private void generateAssignmentTACRecursive(Node node) {
        //Explorem recursivament fins trobar un node que com a fill tingui VAR_NAME i assignació_final
        if(isAssignation(node)) {
            String varname = getVarName(node);
            TACEntry tacEntry = new TACEntry("", generateExpressionTACRecursive(getFinalAssignation(node)), "", varname, Type.EQU);
            currentBlock.add(tacEntry);
        }
        for(Node child : node.getChildren()) {
            generateAssignmentTACRecursive(child);
        }
    }

    /**
     * Method to know if it is a final assignation.
     * @param node It is the node that contains the final assignation.
     * @return True if it is a final assignation, false otherwise.
     */
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

    /**
     * Method to get the varname of the current node.
     * @param node It is the node that contains the varname.
     * @return The varname in a String format.
     */
    private String getVarName(Node node) {
        for(Node child : node.getChildren()) {
            if(child.getType().equalsIgnoreCase("VAR_NAME")) {
                return (String) child.getValue();
            }
        }
        return "";
    }

    /**
     * Method to generate the TAC for the expression given on the current node.
     * @param node It is the node that contains the expression.
     * @return The expression in a String format.
     */
    private String generateExpressionTACRecursive(Node node) {
        if ("literal".equalsIgnoreCase(node.getType()) || "var_name".equalsIgnoreCase(node.getType())) {
            if(node.getValue() instanceof Boolean) {
                return node.getValue() == Boolean.TRUE ? "1" : "0";
            }
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

    /**
     * Method to generate the TAC for the Call instruction of the program.
     * @param node It is the node that contains the Call instruction.
     *
     * @return The Call instruction in a String format.
     */
    private String generateFunctionCallRecursive(Node node) {
        for(Node child : node.getChildren().getLast().getChildren()) {
            if(!child.getType().equalsIgnoreCase("(") && !child.getType().equalsIgnoreCase(")")) {
               generateArgumentTACRecursive(child);
            }
        }

        //Afegir el call al tac
        //Ex: tx = call node.getValue()
        String tempVar = generateTempVariable();
        String functionName = "FUNC_" + node.getChildren().getFirst().getValue().toString();
        TACEntry tacEntry = new TACEntry(Type.CALL_EXP.getType(), "" , functionName, tempVar, Type.CALL_EXP);
        currentBlock.add(tacEntry);

        return tempVar;
    }

    /**
     * Method to generate the TAC for the arguments of the Call instruction of the program.
     * @param child It is the node that contains the arguments.
     */
    private void generateArgumentTACRecursive(Node child) {
        if("arguments_dins_crida".equalsIgnoreCase(child.getType())) {
            for(Node grandChild : child.getChildren()) {
                if(grandChild.getType().equalsIgnoreCase("arguments_crida'")){
                    generateArgumentTACRecursive(grandChild);
                } else {
                    String result = generateExpressionTACRecursive(grandChild);

                    TACEntry tacEntry = new TACEntry(Type.PARAM.getType(), "", result, "", Type.PARAM);
                    currentBlock.add(tacEntry);
                }

            }
        } else if ("arguments_crida'".equalsIgnoreCase(child.getType())) {
            for(Node grandChild : child.getChildren()) {
                if(!grandChild.getType().equalsIgnoreCase(",")) {
                    if(grandChild.getType().equalsIgnoreCase("arguments_crida'")) {
                        generateArgumentTACRecursive(grandChild);
                    } else {
                        String result = generateExpressionTACRecursive(grandChild);

                        TACEntry tacEntry = new TACEntry(Type.PARAM.getType(), "", result, "", Type.PARAM);
                        currentBlock.add(tacEntry);
                    }
                }
            }
        } else {
            String result = generateExpressionTACRecursive(child);

            TACEntry tacEntry = new TACEntry(Type.PARAM.getType(), "", result, "", Type.PARAM);
            currentBlock.add(tacEntry);
        }
    }

    /**
     * Method to generate a temporary variable.
     * @return The temporary variable in a String format.
     */
    private String generateTempVariable() {
        return "t" + (tempCounter++);
    }

    /**
     * Method to generate the TAC for the For instruction of the program.
     * @param child It is the node that contains the For instruction.
     */
    private void generateForCode(Node child) {
        lastBlock = currentBlock;
        TACBlock forBlock = new TACBlock();
        String label = code.addBlock(forBlock, "LOOP");
        conditionalLabels.push(label);

        currentBlock = forBlock;

        generateForTACCondition(child);
        HashMap<Boolean, String> map = new HashMap<>();
        map.put(true, code.getCurrentLabel());
        wasIterator.push(map);
    }

    /**
     * Method to generate the TAC for the For condition of the program.
     * @param node It is the node that contains the For condition.
     */
    private void generateForTACCondition(Node node) {
        // Assignment
        String value = node.getChildren().get(3).getValue().toString();
        String varName = node.getChildren().get(1).getValue().toString();
        TACEntry assignmentEntry = new TACEntry("", value, "", varName, Type.EQU);
        lastBlock.add(assignmentEntry);

        // Condition
        String condition = Type.LT.getType();
        String op2 = node.getChildren().get(5).getValue().toString();
        String temp = generateTempVariable();
        TACEntry conditionEntry = new TACEntry("LOWER", varName, op2, temp, Type.LT);
        currentBlock.add(conditionEntry);

        TACEntry ifEntry = new TACEntry(Type.CONDITION.getType(), "!"+temp, "", "", Type.CONDITION);
        currentBlock.add(ifEntry);

        //increment or decrement
        op2 = node.getChildren().get(7).getValue().toString();
        String inc = node.getChildren().get(6).getType();
        Type type;
        String reverseCondition;
        Type reverseType;
        if(inc.equals("SUMANT")) {
            condition = "+";
            reverseCondition = "-";
            type = Type.ADD;
            reverseType = Type.SUB;
        } else {
            condition = "-";
            reverseCondition = "+";
            type = Type.SUB;
            reverseType = Type.ADD;
        }
        TACEntry incrementEntry = new TACEntry(condition, varName, op2, varName, type);
        currentBlock.add(incrementEntry);

        // Decrementem el contador de la condició abans d'entrar al for
        TACEntry decrementEntry = new TACEntry(reverseCondition, varName, op2, varName, reverseType);
        lastBlock.add(decrementEntry);
    }

    /**
     * Method to generate the TAC for the While instruction of the program.
     */
    private void generateWhileCode() {
        lastBlock = currentBlock;
        // Creem un nou bloc
        TACBlock whileBlock = new TACBlock();
        String label = code.addBlock(whileBlock, "LOOP");
        conditionalLabels.push(label);
        currentBlock = whileBlock;
        HashMap<Boolean, String> map = new HashMap<>();
        map.put(true, label);
        wasIterator.push(map);
    }

    /**
     * Method to generate the TAC for the Else instruction of the program.
     */
    private void generateElseCode() {
        lastBlock = currentBlock;

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

    /**
     * Method to generate the TAC for the If instruction of the program.
     */
    private void generateIfCode() {
        lastBlock = currentBlock;
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

    /**
     * Method to print the TAC of the program.
     */
    public void printTAC() {
        System.out.println("************************************************************************\n" +
                "* TAC:\n" +
                "************************************************************************\n");
        code.printTAC();
    }

    /**
     * Method to process the function arguments of the program.
     * @param symbolTable It is the symbol table of the program.
     */
    public void processFunctionArguments(SymbolTable symbolTable) {
        addReturnsIfNecessary();

        Scope rootScope = symbolTable.getRootScope();
        Map<String, SymbolTableEntry> rootScopeSymbolTable = rootScope.getSymbolTable();

        for(Map.Entry<String, SymbolTableEntry> entry : rootScopeSymbolTable.entrySet()) {
            if(entry.getValue() instanceof FunctionEntry functionEntry) {
                List<VariableEntry> arguments = functionEntry.getArguments();
                TACBlock block = code.getBlock("FUNC_" + functionEntry.getName());
                if(block != null) {
                    for(VariableEntry argument : arguments) {
                        block.addArgument(argument);
                    }
                }
            }
        }

    }

    /**
     * Method to get the TAC of the program.
     * @return The TAC of the program.
     */
    public LinkedHashMap<String, TACBlock> getTAC(){
        return this.code.getAllBlocks();
    }

    /**
     * Method to add the returns if it is necessary based on the blocks of the program.
     */
    public void addReturnsIfNecessary() {
        //Ens guardem els labels dels blocks de la funció
        List<String> labels = code.getAllBlocks().keySet().stream().toList();
        int iterator = 0;
        for(String label : labels) {
            TACBlock block = code.getBlock(label);
            if(block != null) {
                if(!block.getEntries().isEmpty()) {
                    boolean hasReturn = false;
                    for(TACEntry entry : block.getEntries()) {
                        if(entry.getType().equals(Type.RET)) {
                            hasReturn = true;
                            break;
                        }
                    }
                    if(!hasReturn) {
                        // Si no hi ha return, afegim un return
                        if((iterator + 1) < labels.size() && labels.get(iterator + 1) != null) {
                            // Si el següent bloc és una funció, afegim un return o main
                            if (labels.get(iterator + 1).startsWith("FUNC_") || labels.get(iterator + 1).equals("main")) {
                                TACEntry tacEntry = new TACEntry(Type.RET.getType(), "", "", "", Type.RET);
                                block.add(tacEntry);
                            }
                        }
                    }
                } else {
                    if((iterator + 1) < labels.size() && labels.get(iterator + 1) != null) {
                        // Si el següent bloc és una funció, afegim un return o main
                        if(labels.get(iterator + 1).startsWith("FUNC_") || labels.get(iterator + 1).equals("main")) {
                            TACEntry tacEntry = new TACEntry(Type.RET.getType(), "", "", "", Type.RET);
                            block.add(tacEntry);
                        }
                    }
                }
            }
            iterator++;
        }
    }
}
