package frontEnd;

import java.util.*;

public class FirstFollow {
    private Map<String, List<List<String>>> grammar;
    private Set<String> terminals;
    private Map<String, Set<String>> first;
    private Map<String, Set<String>> follow;

    public FirstFollow(Map<String, List<List<String>>> grammar) {
        this.grammar = grammar;
        terminals = new HashSet<>();
        first = new HashMap<>();
        follow = new HashMap<>();
        initializeTerminals();
    }

    private void initializeTerminals() {
        // Inicialitza els terminals
        //String[] terminalSymbols = {"+", "-", "*", "/", "=", ";", ",", ":", "(", ")", "{", "}", "<", ">", "<=", ">=", "!", "==", "!=", "RETORN", "FUNCTION", "START", "END", "LITERAL", "VAR_NAME", "FOR", "DE", "FINS", "VAR_TYPE", "IF", "ELSE", "WHILE", "CALL", "FUNCTION_NAME", "AND", "OR", "CALÇOT"};
        //String[] terminalSymbols = {"+", "*", "(", ")", "id"};
        String[] terminalSymbols = {"+", "*", "(", ")", "id", "const"};

        terminals.addAll(Arrays.asList(terminalSymbols));
    }
    public void FIRST() {
        for (String noTerminal : grammar.keySet()){
            // Inicialitza el conjunt FIRST pel símbol no terminal
            first.put(noTerminal, new HashSet<>());
        }
        // Itera sobre cada símbol de la gramàtica
        for (String no_terminal : grammar.keySet()){
            first.put(no_terminal, compute_FIRST(no_terminal));
        }
    }

    private Set<String> compute_FIRST(String symbol) {
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
                first_set.addAll(compute_FIRST(first_symbol));
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
                            first_set.addAll(compute_FIRST(next_symbol));
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

    public void FOLLOW() {
        // Inicialitzem els conjunts FOLLOW per a cada no terminal
        for (String noTerminal : grammar.keySet()) {
            follow.put(noTerminal, new HashSet<>());
        }
        // El símbol inicial rep el símbol de finalització de cadena al seu FOLLOW
        follow.get("E").add("$");
        //follow.get("sortida").add("$");

        /* Definim una variable per saber si hi ha hagut canvis en algun conjunt FOLLOW durant l'iteració,
        * ho fem així perquè el conjunt FOLLOW d'un símbol pot dependre del conjunt FOLLOW d'un altre símbol
        * i ens cal passar varis cops per la gramàtica per calcular tots els FOLLOWs correctament. */
        boolean changed;
        do {
            // Inicialitzem changed a false al començament de cada iteració
            changed = false;
            // Iterem sobre cada no terminal de la gramàtica
            for (String noTerminal : grammar.keySet()) {
                // Computem el FOLLOW per cada símbol no terminal
                // Si la funció compute_FOLLOW retorna true significa que el conjunt FOLLOW d'algun símbol ha canviat
                changed = compute_FOLLOW(noTerminal) || changed;
            }
            // Continuem el bucle mentre hi hagi canvis en els conjunts FOLLOW
        } while (changed);
    }

    private boolean compute_FOLLOW(String symbol) {
        // Flag per rastrejar si el conjunt FOLLOW del símbol actual ha estat actualitzat
        boolean updated = false;

        // Iterem sobre cada no terminal i les seves regles de producció en la gramàtica
        for (String noTerminal : grammar.keySet()) {
            for (List<String> rule : grammar.get(noTerminal)) {
                for (int i = 0; i < rule.size(); i++) {
                    // Comprovem si el símbol actual de la regla és el símbol passat com a paràmetre per el que volem calcular el FOLLOW
                    if (rule.get(i).equals(symbol)) {
                        // Preparem un conjunt temporal per a calcular els elements a afegir a FOLLOW(symbol)
                        Set<String> tempFollow = new HashSet<>();
                        // Comprovem si el símbol és l'últim element de la regla
                        if (i == rule.size() - 1) {
                            // Afegim el FOLLOW del cap de la producció al FOLLOW del símbol,
                            // perquè la producció acaba amb aquest símbol
                            tempFollow.addAll(follow.get(noTerminal));
                        } else {
                            // Calculem el FIRST dels símbols que segueixen el símbol actual en la producció.
                            Set<String> firstOfNext = computeFirstOfSequence(rule.subList(i + 1, rule.size()));
                            // Comprovem si FIRST dels símbols seguents conté ε
                            if (firstOfNext.remove("ε")) {
                                // Si conté ε, afegim FOLLOW del cap de la producció al FOLLOW del símbol
                                tempFollow.addAll(follow.get(noTerminal));
                            }
                            // Afegim els símbols de FIRST que no són ε al FOLLOW del símbol
                            tempFollow.addAll(firstOfNext);
                        }
                        // Actualitzem el conjunt FOLLOW del símbol si afegim nous elements
                        if (follow.get(symbol).addAll(tempFollow)) {
                            updated = true; // Actualitzem el flag
                        }
                    }
                }
            }
        }
        // Retornem true si hi ha hagut alguna actualització al conjunt FOLLOW del símbol
        return updated;
    }

    // Funcio que calcula el conjunt FIRST d'una seqüència de símbols
    private Set<String> computeFirstOfSequence(List<String> symbols) {
        Set<String> firstSet = new HashSet<>();
        // Per cada símbol de la seqüència, calculem el conjunt FIRST i l'afegim al conjunt FIRST de la seqüència
        for (String symbol : symbols) {
            firstSet.addAll(compute_FIRST(symbol));
        }
        return firstSet;
    }

    public void showFOLLOW() {
        for (String no_terminal : follow.keySet()){
            System.out.println("FOLLOW(" + no_terminal + ") = " + follow.get(no_terminal));
        }
    }
}
