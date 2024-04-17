package frontEnd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
