package frontEnd;

import javax.management.NotificationEmitter;
import java.util.*;

public class Parser {
    private final TokenConverter tokenConverter;
    private Map<String, Map<String, List<String>>> parseTable;
    private Node rootNode;
    public Parser(FirstFollow firstFollow, TokenConverter tokenConverter) {
        this.tokenConverter = tokenConverter;
        firstFollow.FIRST();
        firstFollow.FOLLOW();
        //firstFollow.showFIRST();
        //System.out.println("\n\nFOLLOW:");
        //firstFollow.showFOLLOW();
        this.buildParseTable(firstFollow.getGrammar(), firstFollow.getFollow(), firstFollow.getFirst());
        rootNode = new Node("sortida");
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
                        topNode.setValue(token.getValue());  // Assigna el valor del token al node
                    }

                    String tokenOriginalName = this.tokenConverter.getKeyFromToken(tokenName);
                    printTreeStructure(depth, topSymbol, "\033[32mMATCH (" + token.getLine() + ") __" + (token.getOriginalName() == null ? tokenOriginalName : token.getOriginalName())  + "__\033[0m", "\033[32m");

                    stack.pop();
                    tokenIndex++;
                    depth--;
                } else {
                    throw new RuntimeException("Error de sintaxi en la línia " + token.getLine() + ": símbol inesperat " + token.getStringToken());
                }
            } else {  // topSymbol és un no-terminal
                //System.out.println("\033[33mNext production: " + parseTable.get(topSymbol) + "\033[0m");
                Map<String, List<String>> mappings = parseTable.get(topSymbol);
                if (mappings == null) {
                    throw new RuntimeException("No productions found for non-terminal: " + topSymbol);
                }
                List<String> production = mappings.get(tokenName);
                //System.out.println("\033[33mSelected production: " + tokenName + "=" + production + "\033[0m");
                if (production != null) {
                    printTreeStructure(depth, tokenName, production + " (" + token.getLine() + ") ", "\033[33m");
                    stack.pop();
                    depth--;
                    for (int i = production.size() - 1; i >= 0; i--) {
                        Node newNode = new Node(production.get(i));
                        stack.push(newNode);
                        topNode.addChild(newNode);
                        depth++;
                    }
                } else {
                    throw new RuntimeException("Error de sintaxi: no es pot processar el token " + token.getStringToken());
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

    /*public void createSymbolTable() {
        SymbolTable symbolTable = new SymbolTable();
        processNode(rootNode, symbolTable);
        System.out.println(symbolTable);
    }*/

    /*public void processNode(Node node, SymbolTable symbolTable) {
        switch (node.getType()) {
            case "funcio":
                handleFunction(node, symbolTable);
                break;
            case "argument":
                handleArgument(node, symbolTable);
                break;
            case "assignació":
                handleAssignment(node, symbolTable);
                break;
            case "VAR_NAME":
                handleVariableUsage(node, symbolTable);
                break;
                case"funcio":
                    break;
            default:
                // Processa recursivament els fills de qualsevol altre tipus de node
                node.getChildren().forEach(child -> processNode(child, symbolTable));
                break;
        }*/
    //}


}
