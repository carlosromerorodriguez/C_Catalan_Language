package frontEnd.lexic;

import java.util.HashMap;
import java.util.Map;

/**
 * TokenConverter class
 */
public class TokenConverter {
    /**
     * Tokens dictionary
     */
    private final Map<String, String> tokensDictionary;

    /**
     * Constructor of the class TokenConverter
     */
    public TokenConverter() {
        tokensDictionary = new HashMap<>();
        this.fillTokensDictionary();
    }

    /**
     * Fills the tokens dictionary.
     */
    public void fillTokensDictionary() {
        // Basic tokens
        tokensDictionary.put("fer", "start");
        tokensDictionary.put("fi", "end");
        tokensDictionary.put("(", "(");
        tokensDictionary.put(")", ")");

        // Control structures
        tokensDictionary.put("si", "if");
        tokensDictionary.put("sino", "else");
        tokensDictionary.put("mentre", "while");
        tokensDictionary.put("per", "for");

        tokensDictionary.put("de", "de");
        tokensDictionary.put("fins", "fins");
        tokensDictionary.put("sumant", "sumant");
        tokensDictionary.put("restant", "restant");

        // Variable types
        tokensDictionary.put("enter", "var_type");
        tokensDictionary.put("decimal", "var_type");
        tokensDictionary.put("lletra", "var_type");
        tokensDictionary.put("lletres", "var_type");
        tokensDictionary.put("siono", "var_type");
        tokensDictionary.put("res", "var_type");

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
        tokensDictionary.put(":", ":");

        // Functions and function calls
        tokensDictionary.put("Calçot", "Calçot");
        tokensDictionary.put("proces", "function");
        tokensDictionary.put("retorna", "retorn");
        tokensDictionary.put("crida", "call ");
        tokensDictionary.put("fisi", "endif");
        tokensDictionary.put("fisino", "endelse");

        // Print
        tokensDictionary.put("mostra", "print");
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

    /**
     * Given a Ç lexeme returns the corresponding token.
     *
     * @param proprietaryLexeme the Ç lexeme to convert
     * @return the token corresponding to the Ç lexeme
     */
    public String getToken(String proprietaryLexeme) {
        return tokensDictionary.getOrDefault(proprietaryLexeme, "");
    }

    /**
     * Given a token returns the corresponding Ç lexeme.
     *
     * @param token the token to convert
     * @return the Ç lexeme corresponding to the token
     */
    public String getKeyFromToken(String token) {
        for (Map.Entry<String, String> entry : tokensDictionary.entrySet()) {
            if (entry.getValue().equals(token)) {
                return entry.getKey();
            }
        }
        return "";
    }
}
