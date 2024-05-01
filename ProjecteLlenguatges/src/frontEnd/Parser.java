package frontEnd;

import frontEnd.symbolTable.*;


import java.util.*;

public class Parser {
    private final TokenConverter tokenConverter;
    private Map<String, Map<String, List<String>>> parseTable;
    private Node rootNode;
    private ErrorHandler errorHandler;
    SymbolTable symbolTable;

    public Parser(FirstFollow firstFollow, TokenConverter tokenConverter, ErrorHandler errorHandler) {
        this.tokenConverter = tokenConverter;
        this.errorHandler = errorHandler;
        firstFollow.FIRST();
        firstFollow.FOLLOW();
        //firstFollow.showFIRST();
        //System.out.println("\n\nFOLLOW:");
        //firstFollow.showFOLLOW();
        this.buildParseTable(firstFollow.getGrammar(), firstFollow.getFollow(), firstFollow.getFirst());
        rootNode = new Node("sortida");
        symbolTable = new SymbolTable();
    }

    private void buildParseTable(Map<String, List<List<String>>> grammar, Map<String, Set<String>> follow, Map<String, Set<String>> first) {
        parseTable = new HashMap<>();

        System.out.println("\n\nBuilding parse table");
        for (String nonTerminal : grammar.keySet()) {
            Map<String, List<String>> row = new HashMap<>();
            parseTable.put(nonTerminal, row);

            System.out.println("\nAnalysing non-terminal: " + nonTerminal);
            for (List<String> production : grammar.get(nonTerminal)) {
                System.out.println("Production: " + production);

                // Calcular el conjunto First de toda la producción
                Set<String> productionFirstSet = new HashSet<>();
                for (String symbol : production) {
                    if (first.containsKey(symbol)) {
                        productionFirstSet.addAll(first.get(symbol));
                        if (!first.get(symbol).contains("ε")) {
                            break; // No existen más símbolos a verificar si no hay 'ε'
                        }
                    } else {
                        // Es un terminal o una producción que resulta directamente en epsilon
                        productionFirstSet.add(symbol.trim());
                        break;
                    }
                }

                // Añadir la producción para cada terminal en el conjunto First
                for (String terminal : productionFirstSet) {
                    if (!terminal.equals("ε")) {
                        row.put(terminal.trim(), production);
                        System.out.println("Adding to table: " + nonTerminal + " under " + terminal);
                    }
                }

                // Si la producción puede derivar en epsilon, agregar a Follow
                if (productionFirstSet.contains("ε") || production.contains("ε")) {
                    Set<String> nonTerminalFollowSet = follow.get(nonTerminal);
                    for (String followSymbol : nonTerminalFollowSet) {
                        row.put(followSymbol.trim(), production);
                        System.out.println("Adding to table: " + nonTerminal + " under " + followSymbol);
                    }
                }
            }
        }
    }
    public void printParseTable() {
        for (String nonTerminal : parseTable.keySet()) {
            System.out.println(nonTerminal + ":");
            for (String terminal : parseTable.get(nonTerminal).keySet()) {
                System.out.println("\t" + terminal + ": " + parseTable.get(nonTerminal).get(terminal));
            }
        }
    }

    public void buildParsingTree(List<Token> tokens) {
        Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
                "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL", "GREATER_EQUAL", "!", "==", "!=",
                "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
                "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT"
        ));

        Stack<Node> stack = new Stack<>();
        stack.push(rootNode);  // Símbol de finalització

        int tokenIndex = 0;  // Per recórrer la llista de tokens.
        int depth = 0;

        while (!stack.isEmpty()) {
            Node topNode = stack.peek();

            String topSymbol = topNode.getType().trim();
            if (topSymbol.equals("ε")) {
                stack.pop();
                depth--;
                continue;
            }

            if (tokenIndex >= tokens.size()) {
                System.out.println("Anàlisi sintàctic finalitzat");
                return;
            }

            Token token = tokens.get(tokenIndex);
            String tokenName = token.getStringToken().toUpperCase().trim();

            if (terminalSymbols.contains(topSymbol)) {
                if (topSymbol.equals(tokenName)) {
                    if (topSymbol.equals("LITERAL") || topSymbol.equals("VAR_NAME") || topSymbol.equals("FUNCTION_NAME")) {
                        topNode.setValue(token.getValue());
                        topNode.setLine(token.getLine());
                    } else if (topSymbol.equals("VAR_TYPE")) {
                        topNode.setValue(token.getOriginalName());
                        topNode.setLine(token.getLine());
                    }

                    String tokenOriginalName = this.tokenConverter.getKeyFromToken(tokenName);
                    printTreeStructure(depth, topSymbol, "\033[32mMATCH (" + token.getLine() + ") __" + (token.getOriginalName() == null ? tokenOriginalName : token.getOriginalName())  + "__\033[0m", "\033[32m");

                    stack.pop();
                    tokenIndex++;
                    depth--;
                } else {
                    stack.pop();
                    tokenIndex++;
                    errorHandler.recordError("Error de sintaxi en la línia " + token.getLine() + ": símbol inesperat " + token.getStringToken() + " Falta el símbol: " + tokenName, token.getLine());
                }
            } else {  // topSymbol és un no-terminal
                //System.out.println("\033[33mNext production: " + parseTable.get(topSymbol) + "\033[0m");
                Map<String, List<String>> mappings = parseTable.get(topSymbol);
                if (mappings == null) {
                    errorHandler.recordError("No productions found for non-terminal: " + topSymbol, token.getLine());
                }

                assert mappings != null;
                List<String> production = mappings.get(tokenName);
                //System.out.println("\033[33mSelected production: " + tokenName + "=" + production + "\033[0m");
                if (production != null) {
                    printTreeStructure(depth, tokenName, production + " (" + token.getLine() + ") ", "\033[33m");
                    stack.pop();
                    depth--;
                    for (int i = production.size() - 1; i >= 0; i--) {
                        Node newNode = new Node(production.get(i), 0);
                        stack.push(newNode);
                        topNode.addChild(newNode);
                        depth++;
                    }
                } else {
                    stack.pop();
                    //errorHandler.recordError("Error de sintaxi: no es pot processar el token " + token.getStringToken(), token.getLine());
                }
            }
        }
    }

    private void printTreeStructure(int depth, String node, String action, String colorCode) {
        String indent = " ".repeat(depth * 4);
        String lineLead = indent + (depth > 0 ? "|-- " : "");
        System.out.println(lineLead + colorCode + node + "\033[0m" + (action.isEmpty() ? "" : " - " + action));
    }

    public void printTree() {
        rootNode.printTree(0);
    }

    public void createSymbolTable() {
        processNode(rootNode);
        System.out.println(symbolTable);
    }

    public void processNode(Node node) {
        switch (node.getType().toUpperCase()) {
            case "FUNCIO":
                //scope nou
                handleFunction(node);
                break;
            case "IF", "ELSE", "WHILE", "FOR":
                //scope nou
                symbolTable.addScope(); // Crea y entra a un nuevo ámbito para el 'if'

                node.getChildren().forEach(this::processNode);
                break;
            case "ARGUMENT":
                handleArgument(node);
                break;
            case "ASSIGNACIÓ":
                handleAssignment(node);
                break;
            case "RETURN":
                handleReturn(node);
                break;
            case "VAR_NAME":
                handleVariableUsage(node);
                break;
            case "END":
                symbolTable.leaveScope();
            default:
                // Processem recursivament els fills de qualsevol altre tipus de node
                node.getChildren().forEach(this::processNode);
                break;
        }
    }

    private void handleFunction(Node node) {
        String functionName = (String) node.getValue(); //Agafem el nom de la funcio
        String returnType = (String) node.getParent().getValue(); //Agafem el tipus de retorn de la funcio
        int line = node.getLine(); // Agafem la linia on es troba

        SymbolTableEntry functionEntry = new FunctionEntry(UUID.randomUUID(), functionName, line, returnType, node.getValue() != null ? new ArrayList<>().add(node.getValue()) : new ArrayList<>());

        symbolTable.addScope();
        symbolTable.addSymbolEntry(functionEntry);

        node.getChildren().forEach(this::processNode);
    }

    private void handleArgument(Node node) {
        String name = (String) node.getValue();
        String type = (String) node.getParent().getParent().getValue();

        //Agafem el necessari del argument
        VariableEntry variableEntry = new VariableEntry(UUID.randomUUID(), name, node.getLine(), type, null, true);

        symbolTable.getCurrentScope().getFunctionEntry().addArgument(variableEntry);

        // Processar els fills del nou argument
        node.getChildren().forEach(this::processNode);
    }

    private void handleAssignment(Node node) {
        if (node.getChildren().size() == 4 && node.getChildren().get(0).getType().equals("VAR_TYPE")) {
            // Cas: ["VAR_TYPE", ":", "VAR_NAME", "assignació_final"]
            String varType = (String) node.getChildren().get(0).getValue();
            String varName = (String) node.getChildren().get(2).getValue();
            Node assignFinal = node.getChildren().get(3);
            handleAssignmentFinal(varType, varName, assignFinal);
        } else if (node.getChildren().size() == 2) {
            // Cas: ["VAR_NAME", "assignació_final"]
            String varName = (String) node.getChildren().get(0).getValue();
            Node assignFinal = node.getChildren().get(1);
            handleAssignmentFinal(null, varName, assignFinal); // No type provided
        }
    }

    private void handleAssignmentFinal(String varType, String varName, Node assignFinal) {
        if (assignFinal.getChildren().size() == 2 && assignFinal.getChildren().get(0).getType().equals("=")) {
            // Cas: ["=", "següent_token"]
            Node nextToken = assignFinal.getChildren().get(1);
            Object value = evaluateNextToken(nextToken);
            // Assume the variable has been declared or declare it here if your language allows
            updateVariable(varType, varName, value);
        } else if (assignFinal.getType().equals("ε")) {
            // Cas: ["ε"]
            declareVariable(varType, varName); // Declare without assignment
        }
    }

    private Object evaluateNextToken(Node nextToken) {
        if (nextToken.getType().equals("expressió")) {
            return evaluateExpression(nextToken);
        } else if (nextToken.getType().equals("assignacio_crida")) {
            return handleFunctionCall(nextToken);
        }
        return null;
    }

    private void handleVariableUsage(Node node) {
        SymbolTable variableSymbolTable = new SymbolTable();
        symbolTable.addSubtable(variableSymbolTable);

        // Processar els fills de la variable en ús
        node.getChildren().forEach(this::processNode);
    }


}
