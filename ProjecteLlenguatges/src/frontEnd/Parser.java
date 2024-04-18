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
        this.buildParseTable(firstFollow.getGrammar(), firstFollow.getFollow());
    }

    private void buildParseTable(Map<String, List<List<String>>> grammar, Map<String, Set<String>> follow) {
        parseTable = new HashMap<>();

        for (String nonTerminal : grammar.keySet()) {
            Map<String, List<String>> row = new HashMap<>();
            parseTable.put(nonTerminal, row);

            for (List<String> production : grammar.get(nonTerminal)) {
                Set<String> firstSet = this.firstFollow.computeFirstOfSequence(production);

                for (String terminal : firstSet) {
                    if (!terminal.equals("ε")) {
                        row.put(terminal, production);
                    }
                }

                if (firstSet.contains("ε")) {
                    for (String followSymbol : follow.get(nonTerminal)) {
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
        String[] terminalSymbols = {"+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "<", ">", "<=", ">=", "!", "==", "!=", "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF", "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID"};

        Stack<Object> stack = new Stack<>();
        stack.push("$");  // Símbol de finalització
        stack.push("sortida");

        int tokenIndex = 0;  // Per recórrer la llista de tokens.
        while (!stack.isEmpty()) {
            Object top = stack.peek();
            if (top instanceof String) {
                String topSymbol = (String) top;
                Token token = tokens.get(tokenIndex);
                String tokenName = token.getStringToken().toUpperCase();

                if (Arrays.asList(terminalSymbols).contains(topSymbol)) {
                    if (topSymbol.equals(tokenName)) {
                        stack.pop();
                        tokenIndex++;
                    } else {
                        System.out.println("TOP SYMBOL " + topSymbol);
                        throw new RuntimeException("Error de sintaxi en la línia " + token.getLine() + ": símbol inesperat " + token.getStringToken());
                    }
                } else {  // topSymbol és un no-terminal
                    System.out.println("TOP SYMBOL " + topSymbol);
                    System.out.println(parseTable.get(topSymbol));
                    System.out.println("TokenName " + tokenName);
                    List<String> production = parseTable.get(topSymbol).get(tokenName);
                    if (production != null) {
                        stack.pop();
                        ListIterator<String> li = production.listIterator(production.size());
                        while (li.hasPrevious()) {
                            stack.push(li.previous());
                        }
                    } else {
                        throw new RuntimeException("Error de sintaxi: no es pot processar el token " + token.getStringToken());
                    }
                }
            } else {
                throw new IllegalStateException("Element desconegut al stack");
            }
        }

    }

}
