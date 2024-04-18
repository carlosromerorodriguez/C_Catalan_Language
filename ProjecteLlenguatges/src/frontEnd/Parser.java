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

                for(String symbol : production){
                    Set<String> firstSet = this.firstFollow.getFirst().get(symbol);

                    if(firstSet != null) {
                        for (String terminal : firstSet) {
                            if (!terminal.equals("ε")) {
                                System.out.println("Terminal: " + terminal);
                                row.put(terminal, production);
                            }
                        }
                        break;
                    }
                }

                if (production.contains("ε")) {
                    for (String followSymbol : follow.get(nonTerminal)) {
                        System.out.println("Follow symbol: " + followSymbol);
                        row.put(followSymbol, production);
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
                "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID"
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
            if (tokenIndex >= tokens.size()) {
                throw new RuntimeException("Error de sintaxi: falta de tokens per processar.");
            }

            Token token = tokens.get(tokenIndex);
            String tokenName = token.getStringToken().toUpperCase();

            if (terminalSymbols.contains(topSymbol)) {
                if (topSymbol.equals(tokenName)) {
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
