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
    private String typeDeclaration = "";
    private String lastTopSymbol = "";
    private String currentTopSymbol = "";
    private String currentVarname = ""; // Per saber a quina variable s'ha de guardar l'expressió
    private Boolean equalSeen = false;
    private Boolean retornSeen = false;
    private String functionType = "";
    private int tokenCounter = 0;
    private Boolean canChangeContext = true;

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
                    lastTopSymbol = currentTopSymbol;
                    currentTopSymbol = topSymbol;

                    if (topSymbol.equals("LITERAL") || topSymbol.equals("VAR_NAME") || topSymbol.equals("FUNCTION_NAME")) {
                        topNode.setValue(token.getValue());
                        topNode.setLine(token.getLine());
                    } else if (topSymbol.equals("VAR_TYPE")) {
                        topNode.setValue(token.getOriginalName());
                        topNode.setLine(token.getLine());
                    }

                    if(topSymbol.equals(";") || topSymbol.equals("END")) {
                        isFirstToken = false;
                        context = "";
                        typeDeclaration = "";
                        equalSeen = false;
                        retornSeen = false;
                        canChangeContext = true;
                    }

                    if(context.equals("FUNCTION")) {
                        tokenCounter++;
                        if(tokenCounter == 2) {
                            functionType = (String) topNode.getValue();
                            tokenCounter = 0;
                        }
                    }

                    if(isFirstToken && topSymbol.equals("VAR_TYPE")) {
                        isFirstToken = false;
                        context = "declaració";
                        typeDeclaration = (String) topNode.getValue(); // Guardem tipus de variable
                    }

                    processTopSymbol(topNode, tokenName, token);

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
                    List<String> production = mappings.get(tokenName);
                    System.out.println(production);
                    //System.out.println("\033[33mSelected production: " + tokenName + "=" + production + "\033[0m");
                    if (production != null) {
                        printTreeStructure(depth, tokenName, production + " (" + token.getLine() + ") ", "\033[33m");
                        stack.pop();
                        depth--;
                        for (int i = production.size() - 1; i >= 0; i--) {
                            Node newNode = new Node(production.get(i), 0);
                            checkContext(production.get(i));
                            newNode.setParent(topNode);
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
        return;
    }

    //EN enter: b = 12, a = 12 + b; a la , canvia de declaració a assignació i s'hauria de guardar el context i no canviar-lo
    private void checkContext(String production) {
        if (production.trim().equals("arguments")|| production.trim().equals("assignació") || production.trim().equals("retorn")) {
            if(canChangeContext) {
                canChangeContext = false;
                this.isFirstToken = true;
                this.context = production;
                this.typeDeclaration = "";
            }

        } else if (production.trim().equals("FUNCTION")) {
            this.tokenCounter = 0;
            this.context = production;
            this.functionType = "";
        } //else if(production.trim().equals(";") || production.trim().equals("END")) {
//            this.isFirstToken = false;
//            this.context = "";
//            this.typeDeclaration = "";
//            this.equalSeen = false;
//            this.retornSeen = false;
//            this.canChangeContext = true;
//        }
    }

    private void evaluateSemanticRules(Node newNode, List<String> production) {

    }

    public void processTopSymbol(Node topNode, String tokenName, Token token) {
        System.out.println(context);
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
            System.out.println("Entering new Scope");
            enterScope(topNode);
            System.out.println(symbolTable.getCurrentScope().getParentScope());
        } else if (tokenName.equals("END")) {
            // Si es END analitzem semanticament l'arbre del scope actual
            //analizeSemantic(symbolTable.getCurrentScope());

            // Sortim del scope actual
            symbolTable.leaveScope();
            System.out.println(symbolTable.getCurrentScope());
            retornSeen = false; 
            equalSeen = false;
        } else {
            // Si no es un nou scope, afegeixim el node com a fill del root del scope actual
            System.out.println(symbolTable.getCurrentScope());
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
    }

    private boolean requiresNewScope(String tokenName) {
        // Aquesta funció determina si el tipus de node requereix un nou àmbit
        // Per exemple, 'FUNCTION', 'IF', 'FOR', 'WHILE' poden iniciar nous àmbits
        return tokenName.equals("FUNCTION_MAIN") || tokenName.equals("FUNCTION") || tokenName.equals("FOR") || tokenName.equals("IF") || tokenName.equals("WHILE");
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
        System.out.println(context);
        switch (node.getType().toUpperCase()) {
            case "FUNCTION_NAME":
                //scope nou
                handleFunction(node);
                break;
            case "VAR_NAME": // enter: b = 12, a = 12 + b;
                handleVarname(node);
                break;
            case "=":
                equalSeen = true; // Ja hem vist =, podem guardar tots els tokens fins equalUnseen a currentVarname
                break;
            case "RETORN":
                retornSeen = true;
            default:
                if (equalSeen) {
                    // Guardar el valor de l'expressió a la variable currentVarname
                    VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
                    System.out.println("Current var: " + currentVar);
                    if(node.getValue() != null) currentVar.appendExpressionValue(node.getValue());
                    else if(!node.getType().equals(",")) currentVar.appendExpressionValue(node.getType());

                }
                if (retornSeen) {
                    // Guardem el que trobem al retornValue de la functionEntry del scope actual
                    System.out.println("SCOPE: " + symbolTable.getCurrentScope());
                    if(node.getValue() != null) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
                    else if(!node.getType().equals("RETORN")) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getType());
                }
                break;
        }
    }

    private void handleVarname(Node node) {
        if(lastTopSymbol.equals(",") || lastTopSymbol.equals(";") || lastTopSymbol.equals("START") || lastTopSymbol.equals("END") || lastTopSymbol.equals(":")) { //Si l'ultim top symbol
            VariableEntry lastVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
            if(lastVar != null) lastVar.setExpressionAlreadyAssigned(true);

            System.out.println("Varname: " + node.getValue());
            currentVarname = (String) node.getValue();
            equalSeen = false;
            retornSeen = false;
        }

        switch (context) {
            case "arguments" -> handleVarnameInArguments(node);
            case "assignació" -> handleVarnameInAssignation(node);
            case "declaració" -> handleVarnameInDeclaration(node);
            case "retorn" -> handleVarnameInRetorn(node);
        }
    }

    private void handleVarnameInRetorn(Node node) {
        // Si la varname no es troba a la taula de simbols -> ERROR
        if(symbolTable.getCurrentScope().lookup((String)node.getValue()) == null){
            errorHandler.recordError("Error: La variable del retorn no existe", node.getLine());
            return;
        }
        // Guardem el que trobem al retornValue de la functionEntry del scope actual
        if(node.getValue() != null) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
    }

    private void handleVarnameInAssignation(Node node) {
        VariableEntry variableEntry = (VariableEntry) symbolTable.getCurrentScope().lookup((String)node.getValue());
        // Si la varname no es troba a la taula de simbols -> ERROR
        if(variableEntry == null){
            errorHandler.recordError("Assignation error: variable " + node.getValue() + " isn't declared before.", node.getLine());
            return;
        }
        if(equalSeen){ //Si ja hem vist l'igual, vol dir que el varname trobat forma part de l'expressió del currentVarname
            //Guardem la variable al valor de l'expressió de currentVarname
            VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
            currentVar.appendExpressionValue(node.getValue());
        }
    }


    private void handleVarnameInArguments(Node node) {
        // Si la varname es troba a la taula de simbols -> Afegir-la com a argument de la funció creant de nou la variable entry amb un id diferent i afegint a true el isArgument
        if (symbolTable.getCurrentScope().lookup((String) node.getValue()) != null){
            VariableEntry symbolTableEntry = new VariableEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine(), typeDeclaration, true);
            symbolTable.addSymbolEntry(symbolTableEntry);
            symbolTable.getCurrentScope().getFunctionEntry().addArgument(symbolTableEntry);
        } else if (symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){ // Si la varname no es troba a la taula de símbols (hauria d'estar a la del pare) -> ERROR
            errorHandler.recordError("Error: La variable del argumento no existe", node.getLine());
        }
    }

    private void handleVarnameInDeclaration(Node node) {
        //Assignar a la variable el tipus de variable guardat a typeDeclaration
        // Si la varname no es troba a la taula de simbols, afegir-la amb el tipus typeDeclaration ja que es una variable nova
        if (symbolTable.getCurrentScope().lookup((String) node.getValue()) == null && node.getValue().equals(currentVarname)) {
            VariableEntry symbolTableEntry = new VariableEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine(), typeDeclaration, false);
            symbolTable.addSymbolEntry(symbolTableEntry);
            return;
        } else if (symbolTable.getCurrentScope().lookup((String) node.getValue()) == null) {
            //Error
            errorHandler.recordError("Error: La variable de la asignación no existe", node.getLine());
            return;
        }

        // Si la varname ja es troba a la taula de simbols l'afegim a l'expressió de currentVarname
        VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
        currentVar.appendExpressionValue(node.getValue());
    }

    // Poder hauriem d'afegir l'entrada de la funcio al scope actual i al pare
    private void handleFunction(Node node) {
        if(equalSeen) {
            // Comprovar si existeix la funcio a la taula de simbols del scope actual
            if(symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){
                errorHandler.recordError("Error: La funció no ha estat previament declarada:", node.getLine());
                return;
            }

            VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
            currentVar.appendExpressionValue(node.getValue());
            return;
        } else if (retornSeen) {
            // Comprovar si existeix la funcio a la taula de simbols del scope actual
            if(symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){
                errorHandler.recordError("Error: La funció no ha estat previament declarada: ", node.getLine());
                return;
            }

            symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
            return;
        }

        String functionName = (String) node.getValue(); //Agafem el nom de la funcio
        int line = node.getLine(); // Agafem la linia on es troba

        SymbolTableEntry functionEntry = new FunctionEntry(UUID.randomUUID(), functionName, line, functionType, new ArrayList<>());

        /* Afegim l'entrada de la funció al scope actual i al pare */
        symbolTable.addSymbolEntry(functionEntry);
        symbolTable.getCurrentScope().getParentScope().addEntry(functionEntry);
    }
}

