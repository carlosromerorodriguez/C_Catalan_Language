package frontEnd.syntactic;

import java.util.*;

/**
 * The type First follow.
 */
public class FirstFollow {
    /**
     * The Grammar.
     */
    private final Map<String, List<List<String>>> grammar;
    /**
     * The Terminals of the grammar.
     */
    private final Set<String> terminals;
    /**
     * The First set of the grammar.
     */
    private final Map<String, Set<String>> first;
    /**
     * The Follow set of the grammar.
     */
    private final Map<String, Set<String>> follow;

    /**
     * Instantiates a new First follow.
     *
     * @param grammar the grammar
     */
    public FirstFollow(Map<String, List<List<String>>> grammar) {
        this.grammar = grammar;
        terminals = new HashSet<>();
        first = new HashMap<>();
        follow = new HashMap<>();
        this.initializeTerminals();
        this.calculateFirsts();
        this.calculateFollows();

    }

    /**
     * Initialize terminals of the grammar.
     */
    private void initializeTerminals() {
        // Inicialitza els terminals
        Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
                "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL", "GREATER_EQUAL", "!", "==", "!=",
                "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
                "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT", "ENDELSE", "ENDIF",
                "PRINT", "STRING"
        ));
        terminals.addAll(terminalSymbols);
    }

    /**
     * Calculates the first set for each non-terminal symbol in the grammar.
     */
    public void calculateFirsts() {
        for (String noTerminal : grammar.keySet()){
            // Inicialitza el conjunt FIRST pel símbol no terminal
            first.put(noTerminal, new HashSet<>());
        }
        // Itera sobre cada símbol de la gramàtica
        for (String no_terminal : grammar.keySet()){
            first.put(no_terminal, computeFirst(no_terminal));
        }
    }

    /**
     * Compute first set for a given symbol.
     *
     * @param symbol the symbol
     * @return the set of symbols that can be derived from the given symbol
     */
    private Set<String> computeFirst(String symbol) {
        // Si el símbol és un terminal, el seu conjunt FIRST conté només ell mateix
        if (terminals.contains(symbol)){
            Set<String> symbol_list = new HashSet<>();
            symbol_list.add(symbol);
            return symbol_list;
        }

        if (terminals.contains("ε")){
            Set<String> symbol_list = new HashSet<>();
            symbol_list.add("ε");
            return symbol_list;
        }

        Set<String> first_set = new HashSet<>();
        // Ens assegurem que hi ha produccions per al símbol; si no, retornem un conjunt buit
        List<List<String>> rules = grammar.get(symbol);
        if (rules == null) {
            return new HashSet<>();  // Retornem un conjunt buit si no hi ha produccions
        }

        // Iterem sobre les regles de producció del símbol
        for (List<String> rule: grammar.get(symbol)){
            // Agafem el primer símbol de la regla
            String first_symbol = rule.get(0);

            // Si el primer símbol és un terminal o ε, l'afegim al conjunt FIRST
            if (terminals.contains(first_symbol) || Objects.equals(first_symbol, "ε")){
                first_set.add(first_symbol);
            }
            // Si el primer símbol és un no terminal, calculem el conjunt FIRST per a aquest símbol
            else{
                // Calculem el conjunt FIRST per al primer símbol de la regla i l'afegim al conjunt FIRST del símbol
                first_set.addAll(this.computeFirst(first_symbol));
                // Si el conjunt FIRST del primer símbol conté ε, considerem els següents símbols de la regla
                if (first.get(first_symbol).contains("ε")){
                    // Si hi ha més d'un símbol a la regla, calculem el conjunt FIRST per a la resta de símbols
                    // Repetim el procés fins que trobem un símbol que no produeix ε
                    for (String next_symbol : rule.subList(1, rule.size())){
                        // Si el següent símbol és un terminal o no produeix ε, l'afegim al conjunt FIRST
                        if (terminals.contains(next_symbol) || !first.get(next_symbol).contains("ε")){
                            first_set.add(next_symbol);
                            break;
                        }
                        // Si el següent símbol produeix ε, afegim el conjunt FIRST al conjunt FIRST del símbol
                        else{
                            first_set.addAll(this.computeFirst(next_symbol));
                        }
                    }
                }
            }
        }
        return first_set;

    }

    /**
     * Calculate follows for each non-terminal symbol in the grammar.
     */
    public void calculateFollows() {
        for (String nonTerminal : grammar.keySet()) {
            follow.put(nonTerminal, new HashSet<>());
        }
        follow.get("sortida").add("$");  // Assuming 'sortida' is the start symbol

        boolean changed;
        do {
            changed = false;
            for (String nonTerminal : grammar.keySet()) {
                Set<String> followSet = follow.get(nonTerminal);
                for (List<String> production : grammar.get(nonTerminal)) {
                    Set<String> trailer = new HashSet<>(followSet);
                    for (int i = production.size() - 1; i >= 0; i--) {
                        String symbol = production.get(i);
                        if (isNonTerminal(symbol)) {
                            if (follow.get(symbol).addAll(trailer)) {
                                changed = true;
                            }
                            if (first.get(symbol).contains("ε")) {
                                trailer.addAll(first.get(symbol));
                                trailer.remove("ε");
                            } else {
                                trailer.clear();
                                trailer.addAll(first.get(symbol));
                            }
                        } else {
                            trailer.clear();
                            trailer.add(symbol);
                        }
                    }
                }
            }
        } while (changed);
    }

    /**
     * Checks if a given symbol is a non-terminal.
     *
     * @param item the symbol to check
     * @return true if the symbol is a non-terminal, false otherwise
     */
    private boolean isNonTerminal(String item) {
        return grammar.containsKey(item);
    }

    /**
     * Shows the first set for each non-terminal symbol in the grammar.
     */
    public void showFIRST() {
        System.out.println("\n\n************************************************************************\n" +
                "* FIRST SET:\n" +
                "************************************************************************\n");
        String ANSI_RESET = "\u001B[0m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_GREEN = "\u001B[32m";

        for (String no_terminal : first.keySet()) {
            System.out.println(ANSI_GREEN + "FIRST(" + ANSI_CYAN + no_terminal + ANSI_GREEN + ") = " + ANSI_RESET + first.get(no_terminal));
        }
    }

    /**
     * Shows the follow set for each non-terminal symbol in the grammar.
     */
    public void showFOLLOW() {
        System.out.println("\n\n************************************************************************\n" +
                "* FOLLOW SET:\n" +
                "************************************************************************\n");
        String ANSI_RESET = "\u001B[0m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_GREEN = "\u001B[32m";

        for (String nonTerminal : follow.keySet()) {
            System.out.println(ANSI_GREEN + "FOLLOW(" + ANSI_CYAN + nonTerminal + ANSI_GREEN + ") = " + ANSI_RESET + follow.get(nonTerminal));
        }
    }

    /**
     * Gets follow.
     *
     * @return the follow
     */
    public Map<String, Set<String>> getFollow() {
        return this.follow;
    }

    /**
     * Gets grammar.
     *
     * @return the grammar
     */
    public Map<String, List<List<String>>> getGrammar() {
        return this.grammar;
    }

    /**
     * Gets first.
     *
     * @return the first
     */
    public Map<String, Set<String>> getFirst() {
        return this.first;
    }
}