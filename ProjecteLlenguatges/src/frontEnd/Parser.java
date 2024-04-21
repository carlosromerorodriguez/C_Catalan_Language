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
                "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "<", ">", "<=", ">=", "!", "==", "!=",
                "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
                "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN"
        ));

        Stack<Object> stack = new Stack<>();
        stack.push("$");  // Símbol de finalització
        stack.push("sortida");

        int tokenIndex = 0;  // Per recórrer la llista de tokens.
        while (!stack.isEmpty()) {
            Object top = stack.peek();
            if (!(top instanceof String)) {
                throw new IllegalStateException("Element desconegut al stack");
            }

            String topSymbol = (String) top;
            if (topSymbol.equals("ε")) {
                stack.pop();
                continue;
            }

            if (tokenIndex >= tokens.size()) {
                System.out.println("Anàlisi sintàctic finalitzat");
                return;
            }

            Token token = tokens.get(tokenIndex);
            String tokenName = token.getStringToken().toUpperCase().trim();

            System.out.println("TOKEN NAME" + tokenName);
            System.out.println("TOP SYMBOL " + topSymbol);

            if (terminalSymbols.contains(topSymbol.trim())) {
                if (topSymbol.trim().equals(tokenName.trim())) {
                    System.out.println("MATCH " + tokenName);
                    stack.pop();
                    tokenIndex++;
                } else {
                    System.out.println("TOP SYMBOL " + topSymbol);
                    System.out.println("TOKEN NAME " + tokenName);
                    throw new RuntimeException("Error de sintaxi en la línia " + token.getLine() + ": símbol inesperat " + token.getStringToken());
                }
            } else {  // topSymbol és un no-terminal
                System.out.println("TOP SYMBOL " + topSymbol);
                System.out.println(parseTable.get(topSymbol));
                System.out.println("TokenName " + tokenName);
                Map<String, List<String>> mappings = parseTable.get(topSymbol);
                if (mappings == null) {
                    throw new RuntimeException("No productions found for non-terminal: " + topSymbol);
                }
                List<String> production = mappings.get(tokenName);
                if (production != null) {
                    stack.pop();
                    for (int i = production.size() - 1; i >= 0; i--) {
                        stack.push(production.get(i));
                    }
                } else {
                    throw new RuntimeException("Error de sintaxi: no es pot processar el token " + token.getStringToken());
                }
            }
        }
    }


}
