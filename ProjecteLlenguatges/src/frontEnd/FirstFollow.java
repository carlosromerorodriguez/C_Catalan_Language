package frontEnd;

import java.util.*;

public class FirstFollow {
    private Map<String, List<List<String>>> grammar;
    private Set<String> terminals;
    private Map<String, List<String>> first;

    public FirstFollow(Map<String, List<List<String>>> grammar) {
        this.grammar = grammar;
        terminals = new HashSet<>();
        first = new HashMap<>();
        initializeTerminals();
    }

    private void initializeTerminals() {
        // Agrega los terminales al conjunto de terminales
        String[] terminalSymbols = {"+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "<", ">", "<=", ">=", "!", "==", "!=", "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF", "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR"};
        terminals.addAll(Arrays.asList(terminalSymbols));
    }
    public Map<String, List<String>> FIRST() {
        for (String noTerminal : grammar.keySet()){
            // Inicialitza el conjunt FIRST pel símbol no terminal
            first.put(noTerminal, new ArrayList<>());
        }
        // Itera sobre cada símbol de la gramàtica
        for (String no_terminal : grammar.keySet()){
            first.put(no_terminal, compute_FIRST(no_terminal));
        }
        return first;
    }

    private List<String> compute_FIRST(String symbol) {
        // Si el símbol és un terminal, el seu conjunt FIRST conté només ell mateix
        if (terminals.contains(symbol)){
            List<String> symbol_list = new ArrayList<>();
            symbol_list.add(symbol);
            return symbol_list;
        }
        if (terminals.contains("ε")){
            List<String> symbol_list = new ArrayList<>();
            symbol_list.add("ε");
            return symbol_list;
        }

        // Iterate over each production rule for the symbol
        List<String> first_set = new ArrayList<>();
        for (List<String> rule: grammar.get(symbol)){
            // Get the first symbol of the production rule
            String first_symbol = rule.get(0);

            // If the first symbol is a terminal or ε, add it to the FIRST set
            if (terminals.contains(first_symbol) || Objects.equals(first_symbol, "ε")){
                first_set.add(first_symbol);
            }
            // If the first symbol is a non-terminal, recursively compute its FIRST set
            else{
                // Compute the FIRST set for the non-terminal
                first_set.addAll(compute_FIRST(first_symbol));
                // If the FIRST set of the non-terminal contains ε, consider the next symbol in the rule
                if (first.get(first_symbol).contains("ε")){
                    // If there are more symbols in the rule, consider them for the FIRST set
                    // Repeat this process until a terminal or a symbol that doesn't produce ε is found
                    for (String next_symbol : rule.subList(1, rule.size())){
                        // If the next symbol is a terminal or doesn't produce ε, add it to the FIRST set and break
                        if (terminals.contains(next_symbol) || !first.get(next_symbol).contains("ε")){
                            first_set.add(next_symbol);
                            break;
                        }
                        // If the next symbol is a non-terminal, compute its FIRST set and add it to the FIRST set
                        else{
                            first_set.addAll(first.get(next_symbol));
                        }
                    }
                }
            }
        }
        return first_set;

    }


}
