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
    private SymbolTable symbolTable;
    private Map<String, List<List<String>>> grammar;
    private ParserControlVariables parserControlVariables;
    Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
            "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL", "GREATER_EQUAL", "!", "==", "!=",
            "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
            "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT", "ENDELSE", "ENDIF",
            "PRINT", "STRING"
    ));

    public Parser(FirstFollow firstFollow, TokenConverter tokenConverter, ErrorHandler errorHandler, Map<String, List<List<String>>> grammar) {
        this.tokenConverter = tokenConverter;
        this.errorHandler = errorHandler;
        this.grammar = grammar;
        this.rootNode = new Node("sortida", 0);
        this.symbolTable = new SymbolTable();
        this.symbolTable.setAllTree(rootNode);
        this.parseTable = new HashMap<>();
        this.parserControlVariables = new ParserControlVariables();

        this.buildParsingTable(firstFollow.getGrammar(), firstFollow.getFollow(), firstFollow.getFirst());
    }

    private void buildParsingTable(Map<String, List<List<String>>> grammar, Map<String, Set<String>> follow, Map<String, Set<String>> first) {

        for (String nonTerminal : grammar.keySet()) {
            Map<String, List<String>> row = new HashMap<>();
            this.parseTable.put(nonTerminal, row);

            for (List<String> production : grammar.get(nonTerminal)) {

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
                    }
                }

                // Si la producción puede derivar en epsilon, agregar a Follow
                if (productionFirstSet.contains("ε") || production.contains("ε")) {
                    Set<String> nonTerminalFollowSet = follow.get(nonTerminal);
                    for (String followSymbol : nonTerminalFollowSet) {
                        row.put(followSymbol.trim(), production);
                    }
                }
            }
        }
    }

    public void buildParsingTree(List<Token> tokens) {
        System.out.println("\n************************************************************************");
        System.out.println("* PARSING TREE:");
        System.out.println("************************************************************************\n");
        Stack<Node> stack = new Stack<>();
        stack.push(symbolTable.getAllTree());  // Símbol de finalització

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

                parserControlVariables.lastTopSymbol = parserControlVariables.currentTopSymbol;
                parserControlVariables.currentTopSymbol = topSymbol;
                switch (topSymbol) {
                    case "LITERAL", "VAR_NAME", "FUNCTION_NAME", "STRING":
                        topNode.setValue(token.getValue());
                        topNode.setLine(token.getLine());
                        break;
                    case "VAR_TYPE":
                        topNode.setValue(token.getOriginalName());
                        topNode.setLine(token.getLine());

                        if (parserControlVariables.context.equals("arguments")) {
                            parserControlVariables.lastVarTypeSeenInArguments = (String) topNode.getValue();
                        }
                        if (parserControlVariables.isFirstToken && !parserControlVariables.context.equals("arguments") && !parserControlVariables.context.equals("FUNCTION")) {
                            parserControlVariables.isFirstToken = false;
                            parserControlVariables.context = "declaració";
                            parserControlVariables.typeDeclaration = (String) topNode.getValue(); // Guardem tipus de variable
                        }
                        parserControlVariables.functionType = (String) topNode.getValue();
                        break;
                    case ";", "END", "ENDIF", "ENDELSE":
                        // Resetejem variables de control si trobem un punt i coma o un END
                        parserControlVariables.isFirstToken = false;
                        parserControlVariables.context = "";
                        parserControlVariables.typeDeclaration = "";
                        parserControlVariables.equalSeen = false;
                        parserControlVariables.retornSeen = false;
                        parserControlVariables.canChangeContext = true;
                        parserControlVariables.currentConditional = "";
                        parserControlVariables.insideCondition = false;
                        if(parserControlVariables.isCall) {
                            parserControlVariables.isCall = false;
                        }
                        break;
                    case "START":
                        // Si es tanca un parentesi, vol dir que hem acabat d'afegir arguments a una funció o que hem acabat una condició
                        if (parserControlVariables.context.equals("condicional")) {
                            parserControlVariables.insideCondition = false;
                            parserControlVariables.currentConditional = "";
                            parserControlVariables.isInArguments = false;
                        }
                        parserControlVariables.argumentsInFunctionSentence = false;
                        break;

                    default:
                        break;
                }

                printTreeStructure(depth, topSymbol, "\033[32mMATCH (" + token.getLine() + ") __" + (token.getOriginalName() == null ? this.tokenConverter.getKeyFromToken(tokenName) : token.getOriginalName())  + "__\033[0m", "\033[32m");

                // Se procesa el símbolo terminal y se saca de la pila
                processTopSymbol(topNode, tokenName, token);
                stack.pop();
                tokenIndex++;
                depth--;
            } else {  // topSymbol és un no-terminal
                Map<String, List<String>> mappings = parseTable.get(topSymbol);
                if (mappings == null) {
                    errorHandler.recordError("No productions found for non-terminal: " + topSymbol, token.getLine());
                } else {
                    //Actualitzem lastTopNode
                    parserControlVariables.lastTopNode = parserControlVariables.currentTopNode;
                    parserControlVariables.currentTopNode = topNode;
                    List<String> production = mappings.get(tokenName);

                    if (production != null) {
                        printTreeStructure(depth, tokenName, production + " (" + token.getLine() + ") ", "\033[33m");
                        stack.pop();
                        depth--;
                        for (int i = production.size() - 1; i >= 0; i--) {
                            Node newNode = new Node(production.get(i), 0);
                            checkContext(production.get(i));
                            topNode.addChild(newNode);
                            stack.push(newNode);
                            depth++;
                        }
                        //Afegir al scope actual
                        //if(symbolTable.getCurrentScope().getRootNode() != null) symbolTable.getCurrentScope().getRootNode().addChild(topNode);
                        //symbolTable.getAllTree().addChild(topNode);
                        if(parserControlVariables.lastTopNode != null){
                            //if(!parserControlVariables.lastTopNode.getChildren().contains(topNode) && doesBelongToProduction(topNode)) parserControlVariables.lastTopNode.addChild(topNode);
                        }
                        else symbolTable.setAllTree(topNode);

                        parserControlVariables.lastTopNode = parserControlVariables.currentTopNode;
                        parserControlVariables.currentTopNode = topNode;
                    } else {
                        stack.pop();
                        //errorHandler.recordError("Error de sintaxi: no es pot processar el token " + token.getStringToken(), token.getLine());
                    }
                }
            }
        }
    }

    private boolean doesBelongToProduction(Node topNode) {
        List<List<String>> production = grammar.get(parserControlVariables.lastTopNode.getType());

        for(List<String> prod : production) {
            if(prod.contains(topNode.getType())) return true;
        }

        return false;
    }

    private void checkContext(String production) {
        if (production.trim().equals("arguments") || production.trim().equals("assignació") || production.trim().equals("retorn")) {
            if(production.trim().equals("arguments") || parserControlVariables.isInArguments) {
                parserControlVariables.canChangeContext = true;
                parserControlVariables.isFirstToken = true;
                parserControlVariables.context = production;
                parserControlVariables.typeDeclaration = "";
            } else {
                if (parserControlVariables.canChangeContext) {
                    parserControlVariables.canChangeContext = false;
                    parserControlVariables.isFirstToken = true;
                    parserControlVariables.context = production;
                    parserControlVariables.typeDeclaration = "";
                }
            }
        } else if (production.trim().equals("FUNCTION")) {
            parserControlVariables.context = production;
            parserControlVariables.functionType = "";
        } else if(production.trim().equals("condicionals") || production.trim().equals("iteratives") || production.equals("ELSE")) {
            parserControlVariables.canChangeContext = true;
            parserControlVariables.isFirstToken = true;
            parserControlVariables.context = "condicional";
            parserControlVariables.typeDeclaration = "";
            parserControlVariables.currentConditional = "";
            parserControlVariables.insideCondition = false;
        }
    }

    public void processTopSymbol(Node topNode, String tokenName, Token token) {
        if (tokenName.equals("LITERAL") || tokenName.equals("VAR_NAME") || tokenName.equals("FUNCTION_NAME") || tokenName.equals("STRING")) {
            topNode.setValue(token.getValue());
            topNode.setLine(token.getLine());
        } else if (tokenName.equals("VAR_TYPE")) {
            topNode.setValue(token.getOriginalName());
            topNode.setLine(token.getLine());
        }

        //Comprovar si el lastTopNode conte el topNode a afegir
        if(!parserControlVariables.lastTopNode.getChildren().contains(topNode) && doesBelongToProduction(topNode)) parserControlVariables.lastTopNode.addChild(topNode);
        //symbolTable.getCurrentScope().getRootNode().addChild(topNode);

        // Mires si es IF, FUNCTION, ELSE, WHILE
        if (requiresNewScope(tokenName)) {
            // Si es crees nou scope i poses com a root el node actual
            enterScope(topNode);
            // Si es el main scope el marquem com a tal
            if(tokenName.equals("FUNCTION_MAIN")) symbolTable.getCurrentScope().setIsMainScope(true);
        } else if (tokenName.equals("END") || tokenName.equals("ENDIF") || tokenName.equals("ENDELSE")) {
            // Si es END analitzem semanticament l'arbre del scope actual
            //analizeSemantic(symbolTable.getCurrentScope());

            // Sortim del scope actual
            reArrangeParameters();
            symbolTable.leaveScope();
            parserControlVariables.retornSeen = false;
            parserControlVariables.equalSeen = false;
        } else {
            // Si no es un nou scope, afegeixim el node com a fill del root del scope actual
            symbolTable.getCurrentScope().getRootNode().addChild(topNode);
        }

        // Handle the symbolTable scope
        processNode(topNode);
    }

    private void reArrangeParameters() {
        if(parserControlVariables.callEntryStack.isEmpty()) return;

        while(!parserControlVariables.callEntryStack.isEmpty()) {
            CallEntry callEntry = parserControlVariables.callEntryStack.pop();
            callEntry.reArrangeParameters();
        }
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
        System.out.println("\n************************************************************************");
        System.out.println("* TREE:");
        System.out.println("************************************************************************\n");
        symbolTable.getAllTree().printTree();
        System.out.println("\n\n");
    }

    public void processNode(Node node) {
        switch (node.getType().toUpperCase()) {
            case "FUNCTION_NAME":
                //scope nou
                handleFunction(node);
                break;
            case "VAR_NAME": // enter: b = 12, a = 12 + b;
                handleVarname(node);
                break;
            case "STRING":
                handleString(node);
                break;
            case "=":
                parserControlVariables.equalSeen = true; // Ja hem vist =, podem guardar tots els tokens fins equalUnseen a currentVarname
                break;
            case "RETORN":
                parserControlVariables.retornSeen = true;
                break;
            case "IF", "ELSE", "WHILE", "FOR":
                //Creem conditionalEntry nou a la taula de simbols
                handleConditional(node);
                parserControlVariables.insideCondition = true; // Posem el flag a true per saber que estem dins d'una condició
                break;
            default:
                if (parserControlVariables.equalSeen) {
                    if(parserControlVariables.isCall) {
                        if(node.getType().equals("(") || node.getType().equals(")")) return;
                        if(node.getType().equals("+") || node.getType().equals("/") || node.getType().equals("-") || node.getType().equals("*") || node.getType().equals(",")) parserControlVariables.currentCallEntry.addParameter(node.getType());
                        else parserControlVariables.currentCallEntry.addParameter(node.getValue());
                        return;
                    }
                    // Guardar el valor de l'expressió a la variable currentVarname
                    VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname);
                    if (currentVar == null) {
                        if (node.getValue() != null) {
                            if (this.symbolTable.getCurrentScope().lookup(node.getValue().toString()) == null) {
                                errorHandler.recordVariableDoesntExist(node);
                            }
                        }
                        return;
                    }
                    currentVar.setLine(node.getLine());
                    if (!symbolTable.getCurrentScope().entryExists(parserControlVariables.currentVarname)) {
                        symbolTable.getCurrentScope().addEntry(currentVar);
                        currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname); // Agafem la variable del scope actual

                    }

                    if (currentVar == null) {
                        errorHandler.recordVariableDoesntExist(node);
                        return;
                    }
                    if(node.getValue() != null) currentVar.appendExpressionValue(node.getValue());
                    else if(parserControlVariables.argumentsInFunctionSentence) currentVar.appendExpressionValue(node.getType());
                    else if(!node.getType().equals(",")) currentVar.appendExpressionValue(node.getType());

                } else {
                    if(parserControlVariables.isCall) {
                        if(node.getType().equals("(") || node.getType().equals(")")) return;
                        if(node.getType().equals("+") || node.getType().equals("/") || node.getType().equals("-") || node.getType().equals("*") || node.getType().equals(",")) parserControlVariables.currentCallEntry.addParameter(node.getType());
                        else parserControlVariables.currentCallEntry.addParameter(node.getValue());
                        return;
                    }
                }
                if (parserControlVariables.retornSeen) {
                    // Guardem el que trobem al retornValue de la functionEntry del scope actual
                    if(node.getValue() != null) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
                    else if(!node.getType().equals("RETORN")) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getType());
                }
                if (parserControlVariables.insideCondition) {
                     ConditionalEntry currentConditionalEntry = (ConditionalEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentConditional);
                     if(node.getValue() != null) currentConditionalEntry.addCondition(node.getValue());
                     else if(!node.getType().equals("(") && !node.getType().equals(":") && !node.getType().equals("START") && !node.getType().equals(")")) currentConditionalEntry.addCondition(node.getType());
                }
                break;
        }
    }

    private void handleString(Node node) {

    }

    private void handleConditional(Node node) {
        //Ficar currentConditional el nom de la conditionalEntry creada
        parserControlVariables.currentConditional = node.getType();
        //Crear ConditionalEntry amb el nom de la condició
        ConditionalEntry conditionalEntry = new ConditionalEntry(UUID.randomUUID(),node.getType(), node.getLine(), node.getType());
        //Afegir la ConditionalEntry a la taula de simbols
        symbolTable.addSymbolEntry(conditionalEntry);
    }

    private void handleVarname(Node node) {
        if((!parserControlVariables.argumentsInFunctionSentence && parserControlVariables.lastTopSymbol.equals(",")) ||
                parserControlVariables.lastTopSymbol.equals(";") || parserControlVariables.lastTopSymbol.equals("START") ||
                parserControlVariables.lastTopSymbol.equals("END") || parserControlVariables.lastTopSymbol.equals(":")) { //Si l'ultim top symbol

            VariableEntry lastVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname);

            if(parserControlVariables.context.equals("declaració")) {
                VariableEntry searchVar = (VariableEntry) symbolTable.getCurrentScope().lookup((String) node.getValue());
                if(searchVar != null) {
                    errorHandler.recordVariableAlreadyDeclared(node);
                    return;
                }
            }
            if(lastVar != null) lastVar.setExpressionAlreadyAssigned(true);

            parserControlVariables.currentVarname = (String) node.getValue();
            parserControlVariables.equalSeen = false;
            parserControlVariables.retornSeen = false;
            parserControlVariables.insideCondition = false;
        }

        if(parserControlVariables.retornSeen) {
            // Si la varname no es troba a la taula de simbols -> ERROR
            if(symbolTable.getCurrentScope().lookup((String)node.getValue()) == null){
                errorHandler.recordVariableDoesntExist(node);
                return;
            }
            // Guardem el que trobem al retornValue de la functionEntry del scope actual
            if(node.getValue() != null) symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(node.getValue());
        }

        if(parserControlVariables.argumentsInFunctionSentence) {
            if(parserControlVariables.isCall) {
                parserControlVariables.currentCallEntry.addParameter(node.getValue());
                return;
            }

            // Si la varname no es troba a la taula de simbols -> ERROR
            if(symbolTable.getCurrentScope().lookup((String)node.getValue()) == null){
                errorHandler.recordError("Error: La variable de la crida a la funció no existe", node.getLine());
                return;
            }
            // Guardem el que trobem a la llista d'arguments de la functionEntry del scope actual
            VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname);

            if (currentVar == null) {
                errorHandler.recordError("Error: La variable de la crida a la funció no existe", node.getLine());
                return;
            }

            currentVar.appendExpressionValue(node.getValue());
            return;
        }

        switch (parserControlVariables.context) {
            case "arguments" -> handleVarnameInArguments(node);
            case "assignació" -> handleVarnameInAssignation(node);
            case "declaració" -> handleVarnameInDeclaration(node);
            case "condicional" -> handleVarnameInCondicional(node);
        }
    }

    private void handleVarnameInCondicional(Node node) {
        // Guardem el que trobem a la condició de la conditionalEntry del scope actual
        if(node.getValue() != null) {
            ConditionalEntry currentConditionalEntry = (ConditionalEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentConditional);
            if(currentConditionalEntry != null) currentConditionalEntry.addCondition(node.getValue());
        }
    }

    private void handleVarnameInAssignation(Node node) {
        VariableEntry variableEntry = (VariableEntry) symbolTable.getCurrentScope().lookup((String)node.getValue());
        // Si la varname no es troba a la taula de simbols -> ERROR
        if(variableEntry == null){
            errorHandler.recordVariableDoesntExist(node);
            return;
        }
        if(parserControlVariables.equalSeen){ //Si ja hem vist l'igual, vol dir que el varname trobat forma part de l'expressió del currentVarname
            //Guardem la variable al valor de l'expressió de currentVarname
            VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname);
            if(currentVar == null){
                //errorHandler.recordVariableDoesntExist(node);
                return;
            }
            currentVar.appendExpressionValue(node.getValue());
        } else { //Si no hem vist l'igual, vol dir que el varname trobat es el que s'ha d'assignar
            parserControlVariables.currentVarname = (String) node.getValue();
            // Marquem la variable com que ja s'ha assignat ja que ens estan fent una assignació i per tant ha estat previament declarada
            variableEntry.setExpressionAlreadyAssigned(true);
        }
    }


    private void handleVarnameInArguments(Node node) {
        // Busquem la function_entry del scope actual i no del pare
        FunctionEntry functionEntry = symbolTable.getCurrentScope().getFunctionEntry();

        // Afegim el varname a la llista d'arguments de la funció
        VariableEntry symbolTableEntry = new VariableEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine(), parserControlVariables.lastVarTypeSeenInArguments, true);
        functionEntry.addArgument(symbolTableEntry);
        symbolTable.addSymbolEntry(symbolTableEntry);
    }

    private void handleVarnameInDeclaration(Node node) {
        //Assignar a la variable el tipus de variable guardat a typeDeclaration
        // Si la varname no es troba a la taula de simbols, afegir-la amb el tipus typeDeclaration ja que es una variable nova
        if (symbolTable.getCurrentScope().lookup((String) node.getValue()) == null && node.getValue().equals(parserControlVariables.currentVarname)) {
            VariableEntry symbolTableEntry = new VariableEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine(), parserControlVariables.typeDeclaration, false);
            symbolTable.addSymbolEntry(symbolTableEntry);
            return;
        } else if (symbolTable.getCurrentScope().lookup((String) node.getValue()) == null) {
            //Error
            errorHandler.recordVariableDoesntExist(node);
            return;
        }

        // Si la varname ja es troba a la taula de simbols l'afegim a l'expressió de currentVarname
        VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname);
        if (currentVar == null) {
            errorHandler.recordVariableDoesntExist(node);
            return;
        }
        currentVar.appendExpressionValue(node.getValue());
    }

    private void handleFunction(Node node) {
        if(parserControlVariables.equalSeen) {
            // Comprovar si existeix la funcio a la taula de simbols del scope actual
            if(symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){
                errorHandler.recordFunctionIsNotDeclared(node);
                return;
            }

            parserControlVariables.argumentsInFunctionSentence = true; //Marquem que ens podem trobar arguments quan es crida una funció

            VariableEntry currentVar = (VariableEntry) symbolTable.getCurrentScope().lookup(parserControlVariables.currentVarname);
            if (currentVar == null) {
                errorHandler.recordVariableDoesntExist(node);
                return;
            }

            //Creem una crida
            CallEntry callEntry = new CallEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine());
            //Posem context a crida
            parserControlVariables.isCall = true;
            //Guardem lastCrida
            parserControlVariables.currentCallEntry = callEntry;
            parserControlVariables.callEntryStack.push(callEntry);

            currentVar.appendExpressionValue(callEntry);
        } else if (parserControlVariables.retornSeen) {
            // Comprovar si existeix la funcio a la taula de simbols del scope actual
            if(symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){
                errorHandler.recordError("Error: La funció no ha estat previament declarada: ", node.getLine());
                return;
            }

            parserControlVariables.argumentsInFunctionSentence = true; //Marquem que ens podem trobar arguments quan es crida una funció

            //Creem una crida
            CallEntry callEntry = new CallEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine());
            //Posem context a crida
            parserControlVariables.isCall = true;
            //Guardem lastCrida
            parserControlVariables.currentCallEntry = callEntry;

            symbolTable.getCurrentScope().getFunctionEntry().appendReturnValue(callEntry);
        } else if(parserControlVariables.context.equals("FUNCTION")) {
            String functionName = (String) node.getValue(); //Agafem el nom de la funcio
            int line = node.getLine(); // Agafem la linia on es troba

            SymbolTableEntry functionEntry = new FunctionEntry(UUID.randomUUID(), functionName, line, parserControlVariables.functionType, new ArrayList<>());

            //mirem si ja existeix la funció recursivament a la symbol table
            FunctionEntry checkFunctionEntry = (FunctionEntry) symbolTable.getCurrentScope().lookup(functionName);
            if(checkFunctionEntry != null) {
                errorHandler.recordFunctionAlreadyExists(node);
            }

            /* Afegim l'entrada de la funció al scope actual i al pare */
            symbolTable.addSymbolEntry(functionEntry);
            symbolTable.getCurrentScope().getParentScope().addEntry(functionEntry);
        } else {
            //Es una crida

            //Mirem si existeix en algun dels contexts pares
            if(symbolTable.getCurrentScope().lookup((String) node.getValue()) == null){
                errorHandler.recordFunctionIsNotDeclared(node);
                return;
            }

            parserControlVariables.argumentsInFunctionSentence = true; //Marquem que ens podem trobar arguments quan es crida una funció

            //Creem una crida
            CallEntry callEntry = new CallEntry(UUID.randomUUID(), (String) node.getValue(), node.getLine());
            //Posem context a crida
            parserControlVariables.isCall = true;
            //Guardem lastCrida
            parserControlVariables.currentCallEntry = callEntry;

            symbolTable.getCurrentScope().addEntry(callEntry);
        }
    }

    public void optimizeTree() {
        symbolTable.getAllTree().pruneEpsilonPaths();
        symbolTable.getAllTree().collapseSingleChildNodes();
        symbolTable.getAllTree().optimizeTree();
    }


    public Node getParsingTree(){
        return symbolTable.getAllTree();
    }


    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Set<String> getTerminalSymbols() {
        return this.terminalSymbols;
    }
}

