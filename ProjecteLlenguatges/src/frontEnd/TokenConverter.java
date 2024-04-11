package frontEnd;

import java.util.HashMap;
import java.util.Map;

public class TokenConverter {
    private final Map<String, String> tokensDictionary;

    public TokenConverter() {
        tokensDictionary = new HashMap<>();
        fillTokensDictionary();
    }

    /**
     * Fills the tokens dictionary.
     */
    public void fillTokensDictionary() {
        // Basic tokens
        tokensDictionary.put("fer:", " start ");
        tokensDictionary.put("fi", " end ");
        tokensDictionary.put("(", " ( ");
        tokensDictionary.put(")", ")");

        // Control structures
        tokensDictionary.put("si", "if");
        tokensDictionary.put("sino", "else");
        tokensDictionary.put("mentre", "while");
        tokensDictionary.put("per", "for");

        // Variable types
        tokensDictionary.put("enter:", "vartype");
        tokensDictionary.put("decimal:", "vartype");
        tokensDictionary.put("lletra:", "vartype");
        tokensDictionary.put("lletres:", "vartype");
        tokensDictionary.put("siono:", "vartype");
        tokensDictionary.put("res", "vartype");
        //TODO: Token de un nom de variable i de un valor

        // Operators
        tokensDictionary.put("+", "+");
        tokensDictionary.put("-", "-");
        tokensDictionary.put("·", "*");
        tokensDictionary.put("/", "/");
        tokensDictionary.put("i", "and");
        tokensDictionary.put("o", "or");
        tokensDictionary.put("no", "!");
        tokensDictionary.put("igual", "==");
        tokensDictionary.put("diferent", "!=");
        tokensDictionary.put("ç", ";");
        tokensDictionary.put(",", ",");
        tokensDictionary.put("<", "lower");
        tokensDictionary.put(">", "greater");
        tokensDictionary.put("<=", "lower_equal");
        tokensDictionary.put(">=", "greater_equal");
        tokensDictionary.put("=", "=");

        // Functions and function calls
        tokensDictionary.put("Calçot", "main");
        tokensDictionary.put("proces", "function");
        tokensDictionary.put("retorn", "return");
        tokensDictionary.put("crida", "call ");

        //TODO: Token del nom de la funció
    }

    /**
     * Given a Ç lexeme returns the corresponding token.
     *
     * @param proprietaryLexeme the Ç lexeme to convert
     * @return the token corresponding to the Ç lexeme
     */
    public String convertLexemeToToken(String proprietaryLexeme) {
        return tokensDictionary.getOrDefault(proprietaryLexeme, proprietaryLexeme);
    }

}
