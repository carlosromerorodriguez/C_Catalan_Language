//TODO:
// - S'ha d'afegir diverses expresions a una variable ja que es poden donar una en un if i una en un else
//   per el tema del boolean expressionAlreadyAssigned a VariableEntry
// - Arguments de funcions
// - Control d'errors

package frontEnd.syntactic;

import frontEnd.global.ErrorHandler;
import frontEnd.lexic.Token;
import frontEnd.lexic.TokenConverter;
import frontEnd.syntactic.symbolTable.*;

import java.util.*;

public class Parser {
    private final TokenConverter tokenConverter;
    private final Map<String, Map<String, List<String>>> parseTable;
    private final Node rootNode;
    private final ErrorHandler errorHandler;
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
    private Boolean insideCondition = false;
    private String currentConditional = "";
    private String lastVarTypeSeenInArguments = "";
    private Boolean isInArguments = false;
    private Boolean argumentsInFunctionSentence = false;

    public Parser(FirstFollow firstFollow, TokenConverter tokenConverter, ErrorHandler errorHandler) {
        this.tokenConverter = tokenConverter;
        this.errorHandler = errorHandler;
        this.rootNode = new Node("sortida", 0);
        this.symbolTable = new SymbolTable();
        this.parseTable = new HashMap<>();

        this.buildParsingTable(firstFollow.getGrammar(), firstFollow.getFollow(), firstFollow.getFirst());
    }

    private void buildParsingTable(Map<String, List<List<String>>> grammar, Map<String, Set<String>> follow, Map<String, Set<String>> first) {
        System.out.println("\n\nBuilding parsing table");

        for (String nonTerminal : grammar.keySet()) {
            Map<String, List<String>> row = new HashMap<>();
            this.parseTable.put(nonTerminal, row);

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
            if (tokenIndex >= tokens.size()) {
                System.out.println("Anàlisi sintàctic finalitzat");
                return;
            }

            Node topNode = stack.peek();

            String topSymbol = topNode.getType().trim();
            if (topSymbol.equals("ε")) {
                stack.pop();
                depth--;
                continue;
            }

            Token token = tokens.get(tokenIndex);
            String tokenName = token.getStringToken().toUpperCase().trim();

            if (terminalSymbols.contains(topSymbol)) {
                if (!topSymbol.equals(tokenName)) {
                    errorHandler.recordError("Error de sintaxi en la línia " + token.getLine() + ": símbol inesperat " + token.getStringToken() + " Falta el símbol: " + tokenName, token.getLine());
                    stack.pop();
                    tokenIndex++;
                    continue;
                }

                lastTopSymbol = currentTopSymbol;
                currentTopSymbol = topSymbol;

                switch (topSymbol) {
                    case "LITERAL", "VAR_NAME", "FUNCTION_NAME":
                        topNode.setValue(token.getValue());
                        topNode.setLine(token.getLine());
                        break;
                    case "VAR_TYPE":
                        topNode.setValue(token.getOriginalName());
                        topNode.setLine(token.getLine());

                        if (context.equals("arguments")) {
                            lastVarTypeSeenInArguments = (String) topNode.getValue();
                        }
                        if (isFirstToken && !context.equals("arguments")) {
                            isFirstToken = false;
                            context = "declaració";
                            typeDeclaration = (String) topNode.getValue(); // Guardem tipus de variable
                        }
                        break;
                    case ";", "END":
                        // Reseteamos el contexto
                        isFirstToken = false;
                        context = "";
                        typeDeclaration = "";
                        equalSeen = false;
                        retornSeen = false;
                        canChangeContext = true;
                        currentConditional = "";
                        insideCondition = false;
                        break;
                    case ")":
                        // Si se cierra la condición
                        if (context.equals("condicional")) {
                            insideCondition = false;
                            currentConditional = "";
                            isInArguments = false;
                            argumentsInFunctionSentence = false; // Ja no ens podem trobar arguments en la crida a una funció en una assignació/retorn
                        }
                        break;

                    default:
                        break;
                }

                if(context.equals("FUNCTION")) {
                    tokenCounter++;
                    if(tokenCounter == 2) {
                        functionType = (String) topNode.getValue();
                        tokenCounter = 0;
                    }
                }

                printTreeStructure(depth, topSymbol, "\033[32mMATCH (" + token.getLine() + ") __" + (token.getOriginalName() == null ? this.tokenConverter.getKeyFromToken(tokenName) : token.getOriginalName())  + "__\033[0m", "\033[32m");

                // Se procesa el símbolo terminal y se saca de la pila
                processTopSymbol(topNode, tokenName, token);
                stack.pop();
                tokenIndex++;
                depth--;
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
                            System.out.println("New node: " + production.get(i));
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
    }

    private void checkContext(String production) {
        if (production.trim().equals("arguments") || production.trim().equals("assignació") || production.trim().equals("retorn")) {
            if(production.trim().equals("arguments") || isInArguments) {
                canChangeContext = true;
                this.isFirstToken = true;
                this.context = production;
                this.typeDeclaration = "";
            } else {
                if (canChangeContext) {
                    canChangeContext = false;
                    this.isFirstToken = true;
                    this.context = production;
                    this.typeDeclaration = "";
                }
            }
        } else if (production.trim().equals("FUNCTION")) {
            this.tokenCounter = 0;
            this.context = production;
            this.functionType = "";
        } else if(production.trim().equals("condicionals") || production.trim().equals("iteratives") || production.equals("ELSE")) {
            canChangeContext = true;
            this.isFirstToken = true;
            this.context = "condicional";
            this.typeDeclaration = "";
            this.currentConditional = "";
            this.insideCondition = false;
        }
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
            enterScope(topNode);
            // Si es el main scope el marquem com a tal
            if(tokenName.equals("FUNCTION_MAIN")) symbolTable.getCurrentScope().setIsMainScope(true);
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
        return tokenName.equals("FUNCTION_MAIN") || tokenName.equals("FUNCTION") || tokenName.equals("FOR") || tokenName.equals("IF") || tokenName.equals("WHILE") || tokenName.equals("ELSE");
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
                break;
            case "IF", "ELSE", "WHILE", "FOR":
                //Creem conditionalEntry nou a la taula de simbols
                handleConditional(node);
                insideCondition = true; // Posem el flag a true per saber que estem dins d'una condició
                break;
            default:
                if (equalSeen) {
                    // Guardar el valor de l'expressió a la variable currentVarname
                    VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
                    System.out.println("Current var: " + currentVar);
                    if(node.getValue() != null) currentVar.appendExpressionValue(node.getValue());
                    else if(!argumentsInFunctionSentence && !node.getType().equals(",")) currentVar.appendExpressionValue(node.getType());

                }
                if (retornSeen) {
                    // Guardem el que trobem al retornValue de la functionEntry del scope actual
                    System.out.println("SCOPE: " + symbolTable.getCurrentScope());
                    if(node.getValue() != null) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
                    else if(!node.getType().equals("RETORN")) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getType());
                }
                if (insideCondition) {
                     ConditionalEntry currentConditionalEntry = (ConditionalEntry) symbolTable.getCurrentScope().lookup(currentConditional);
                     if(node.getValue() != null) currentConditionalEntry.addCondition(node.getValue());
                     else if(!node.getType().equals("(") && !node.getType().equals(":") && !node.getType().equals("START")) currentConditionalEntry.addCondition(node.getType());
                }
                break;
        }
    }

    private void handleConditional(Node node) {
        //Ficar currentConditional el nom de la conditionalEntry creada
        currentConditional = node.getType();
        //Crear ConditionalEntry amb el nom de la condició
        ConditionalEntry conditionalEntry = new ConditionalEntry(UUID.randomUUID(),node.getType(), node.getLine(), node.getType());
        //Afegir la ConditionalEntry a la taula de simbols
        symbolTable.addSymbolEntry(conditionalEntry);
    }

    private void handleVarname(Node node) {
        if(lastTopSymbol.equals(",") || lastTopSymbol.equals(";") || lastTopSymbol.equals("START") || lastTopSymbol.equals("END") || lastTopSymbol.equals(":")) { //Si l'ultim top symbol
            VariableEntry lastVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
            if(lastVar != null) lastVar.setExpressionAlreadyAssigned(true);

            System.out.println("Varname: " + node.getValue());
            currentVarname = (String) node.getValue();
            equalSeen = false;
            retornSeen = false;
            insideCondition = false;
        }

        if(retornSeen) {
            // Si la varname no es troba a la taula de simbols -> ERROR
            if(symbolTable.getCurrentScope().lookup((String)node.getValue()) == null){
                errorHandler.recordError("Error: La variable del retorn no existe", node.getLine());
                return;
            }
            // Guardem el que trobem al retornValue de la functionEntry del scope actual
            if(node.getValue() != null) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
        }

        switch (context) {
            case "arguments" -> handleVarnameInArguments(node);
            case "assignació" -> handleVarnameInAssignation(node);
            case "declaració" -> handleVarnameInDeclaration(node);
            case "condicional" -> handleVarnameInCondicional(node);
        }
    }

    private void handleVarnameInCondicional(Node node) {
        // Si la varname no es troba a la taula de simbols -> ERROR
        if(symbolTable.getCurrentScope().lookup((String)node.getValue()) == null){
            errorHandler.recordError("Error: La variable de la condició no existe", node.getLine());
            return;
        }

        // Guardem el que trobem a la condició de la conditionalEntry del scope actual
        if(node.getValue() != null) {
            ConditionalEntry currentConditionalEntry = (ConditionalEntry) symbolTable.getCurrentScope().lookup(currentConditional);
            currentConditionalEntry.addCondition(node.getValue());
        }
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
        } else { //Si no hem vist l'igual, vol dir que el varname trobat es el que s'ha d'assignar
            currentVarname = (String) node.getValue();
            // Marquem la variable com que ja s'ha assignat ja que ens estan fent una assignació i per tant ha estat previament declarada
            variableEntry.setExpressionAlreadyAssigned(true);
        }
    }


    private void handleVarnameInArguments(Node node) {
        // Busquem la function_entry del scope actual i no del pare
        FunctionEntry functionEntry = symbolTable.getCurrentScope().getFunctionEntry();

        // Afegim el varname a la llista d'arguments de la funció
        VariableEntry symbolTableEntry = new VariableEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine(), lastVarTypeSeenInArguments, false);
        functionEntry.addArgument(symbolTableEntry);
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

            argumentsInFunctionSentence = true; //Marquem que ens podem trobar arguments quan es crida una funció a una assignació

            VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(currentVarname);
            currentVar.appendExpressionValue(node.getValue());
            return;
        } else if (retornSeen) {
            // Comprovar si existeix la funcio a la taula de simbols del scope actual
            if(symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){
                errorHandler.recordError("Error: La funció no ha estat previament declarada: ", node.getLine());
                return;
            }

            argumentsInFunctionSentence = true; //Marquem que ens podem trobar arguments quan es crida una funció a un retorn

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
