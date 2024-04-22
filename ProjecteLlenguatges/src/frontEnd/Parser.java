package frontEnd;

import java.util.*;

public class Parser {
    private final FirstFollow firstFollow;
    private Map<String, Map<String, List<String>>> parseTable;
    public Parser(FirstFollow firstFollow) {
        this.firstFollow = firstFollow;
        firstFollow.FIRST();
        firstFollow.FOLLOW();
        firstFollow.showFIRST();
        System.out.println("\n\nFOLLOW:");
        firstFollow.showFOLLOW();
        this.buildParseTable(firstFollow.getGrammar(), firstFollow.getFollow(), firstFollow.getFirst());
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

        Stack<Object> stack = new Stack<>();
        stack.push("$");  // Símbol de finalització
        stack.push("sortida");

        int tokenIndex = 0;  // Per recórrer la llista de tokens.
        int depth = 0;

        while (!stack.isEmpty()) {
            Object top = stack.peek();
            if (!(top instanceof String)) {
                throw new IllegalStateException("Element desconegut al stack");
            }

            String topSymbol = ((String) top).trim();
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

            //System.out.println("\nTOP SYMBOL " + topSymbol);
            //System.out.println("TOKEN NAME " + tokenName);

            if (terminalSymbols.contains(topSymbol)) {
                if (topSymbol.equals(tokenName)) {
                    printTreeStructure(depth, topSymbol, "\033[32mMATCH (" + token.getLine() + ")\033[0m", "\033[32m");

                    //System.out.printf("%sMATCH (Line: %d) -> %s\n", indent(depth), token.getLine(), tokenName);
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
                        stack.push(production.get(i));
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
}
