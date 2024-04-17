package frontEnd;

import java.util.List;
import java.util.Map;

public class Parser {
    private Map<String, Map<String, List<String>>> parseTable;
    public Parser(Map<String, Map<String, List<String>>> parseTable) {
        this.parseTable = parseTable;
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
