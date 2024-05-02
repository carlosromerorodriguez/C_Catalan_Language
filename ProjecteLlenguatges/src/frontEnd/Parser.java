package frontEnd;

import frontEnd.symbolTable.*;

import java.util.*;

public class Parser {
    private final TokenConverter tokenConverter;
    private Map<String, Map<String, List<String>>> parseTable;
    private Node rootNode;
    private ErrorHandler errorHandler;
    SymbolTable symbolTable;
    private String context = "";
    private Boolean isFirstToken = false;

    public Parser(FirstFollow firstFollow, TokenConverter tokenConverter, ErrorHandler errorHandler) {
        this.tokenConverter = tokenConverter;
        this.errorHandler = errorHandler;
        firstFollow.FIRST();
        firstFollow.FOLLOW();
        //firstFollow.showFIRST();
        //System.out.println("\n\nFOLLOW:");
        //firstFollow.showFOLLOW();
        this.buildParseTable(firstFollow.getGrammar(), firstFollow.getFollow(), firstFollow.getFirst());
        rootNode = new Node("sortida", 0);
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
                    //processTopSymbol(topNode, tokenName, token);
                    if (topSymbol.equals("LITERAL") || topSymbol.equals("VAR_NAME") || topSymbol.equals("FUNCTION_NAME")) {
                        topNode.setValue(token.getValue());
                        topNode.setLine(token.getLine());
                    } else if (topSymbol.equals("VAR_TYPE")) {
                        topNode.setValue(token.getOriginalName());
                        topNode.setLine(token.getLine());
                    }

                    if(isFirstToken && topSymbol.equals("VAR_NAME")) {
                        isFirstToken = false;
                        context = "declaració";
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
                } else {
                    assert mappings != null;
                    List<String> production = mappings.get(tokenName);
                    //System.out.println("\033[33mSelected production: " + tokenName + "=" + production + "\033[0m");
                    if (production != null) {
                        printTreeStructure(depth, tokenName, production + " (" + token.getLine() + ") ", "\033[33m");
                        stack.pop();
                        depth--;
                        for (int i = production.size() - 1; i >= 0; i--) {
                            Node newNode = new Node(production.get(i), 0);
                            checkContext(production.get(i));
                            System.out.println("New node: " + production.get(i) + " - " + token.getLine()  );
                            stack.push(newNode);
                            depth++;
                        }
                    } else {
                        stack.pop();
                        //errorHandler.recordError("Error de sintaxi: no es pot processar el token " + token.getStringToken(), token.getLine());
                    }
                }
            }
        }
    }

    private void checkContext(String production) {
        if (production.trim().equals("arguments")|| production.trim().equals("assignació") || production.trim().equals("return")) {
            isFirstToken = true;
            this.context = production;
        } else if(production.trim().equals(";")) {
            this.context = "";
        }
    }

    private void evaluateSemanticRules(Node newNode, List<String> production) {

    }

    public void processTopSymbol(Node topNode, String tokenName, Token token) {
        if (tokenName.equals("LITERAL") || tokenName.equals("VAR_NAME") || tokenName.equals("FUNCTION_NAME")) {
            topNode.setValue(token.getValue());
            topNode.setLine(token.getLine());
        } else if (tokenName.equals("VAR_TYPE")) {
            topNode.setValue(token.getOriginalName());
            topNode.setLine(token.getLine());
        }

        // Mires si es IF, FUNCTION, ELSE, WHILE
        if (requiresNewScope(tokenName)) {
            // Si es crees nou scope i poses com a root el node actual
            enterScope(topNode);
        } else if (tokenName.equals("END")) {
            // Si es END analitzem semanticament l'arbre del scope actual
            analizeSemantic(symbolTable.getCurrentScope());

            // Sortim del scope actual
            symbolTable.leaveScope();
        } else {
            // Si no es un nou scope, afegeixes el node com a fill del root del scope actual
            symbolTable.getCurrentScope().getRootNode().addChild(topNode);
        }

        // Handle the symbolTable scope
        processNode(topNode);
    }

    private void analizeSemantic(Scope currentScope) {

    }

    private void enterScope(Node newNode) {
        symbolTable.addScope();
        symbolTable.getCurrentScope().setRootNode(newNode);
        System.out.println("Entering new scope: " + newNode.getType());
    }

    private boolean requiresNewScope(String tokenName) {
        // Aquesta funció determina si el tipus de node requereix un nou àmbit
        // Per exemple, 'FUNCTION', 'IF', 'FOR', 'WHILE' poden iniciar nous àmbits
        return tokenName.equals("FUNCTION") || tokenName.equals("FOR") || tokenName.equals("IF") || tokenName.equals("WHILE");
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
            case "VAR_NAME":
                //Mirem el context
                break;
            //Contexts: arguments, assignació i return
        }
    }

    private void handleFunction(Node node) {
        String functionName = (String) node.getValue(); //Agafem el nom de la funcio
        String returnType = (String) node.getParent().getValue(); //Agafem el tipus de retorn de la funcio
        int line = node.getLine(); // Agafem la linia on es troba

        SymbolTableEntry functionEntry = null;/*new FunctionEntry(UUID.randomUUID(), functionName, line, returnType, node.getValue() != null ? new ArrayList<>().add(node.getValue()) : new ArrayList<>());*/

        symbolTable.addSymbolEntry(functionEntry);
    }
}
