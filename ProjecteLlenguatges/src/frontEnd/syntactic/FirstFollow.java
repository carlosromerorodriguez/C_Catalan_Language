package frontEnd.syntactic;

import java.util.*;

public class FirstFollow {
    private final Map<String, List<List<String>>> grammar;
    private final Set<String> terminals;
    private final Map<String, Set<String>> first;
    private final Map<String, Set<String>> follow;

    public FirstFollow(Map<String, List<List<String>>> grammar) {
        this.grammar = grammar;
        terminals = new HashSet<>();
        first = new HashMap<>();
        follow = new HashMap<>();
        this.initializeTerminals();
        this.calculateFirsts();
        this.calculateFollows();

    }

    private void initializeTerminals() {
        // Inicialitza els terminals
        Set<String> terminalSymbols = new HashSet<>(Arrays.asList(
                "+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "GREATER", "LOWER", "LOWER_EQUAL", "GREATER_EQUAL", "!", "==", "!=",
                "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF",
                "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT", "VOID", "FUNCTION_MAIN", "SUMANT", "RESTANT", "ENDELSE", "ENDIF"
        ));        //String[] terminalSymbols = {"+", "*", "(", ")", "id"};
        //String[] terminalSymbols = {"+", "*", "(", ")", "id", "const"};
        terminals.addAll(terminalSymbols);
    }

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

    public void showFIRST() {
        for (String no_terminal : first.keySet()){
            System.out.println("FIRST(" + no_terminal + ") = " + first.get(no_terminal));
        }
    }

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

    private boolean isNonTerminal(String item) {
        return grammar.containsKey(item);
    }

    public void showFOLLOW() {
        for (String nonTerminal : follow.keySet()) {
            System.out.println("FOLLOW(" + nonTerminal + ") = " + follow.get(nonTerminal));
        }
    }

    public Map<String, Set<String>> getFollow() {
        return this.follow;
    }

    public Map<String, List<List<String>>> getGrammar() {
        return this.grammar;
    }

    public Map<String, Set<String>> getFirst() {
        return this.first;
    }
}